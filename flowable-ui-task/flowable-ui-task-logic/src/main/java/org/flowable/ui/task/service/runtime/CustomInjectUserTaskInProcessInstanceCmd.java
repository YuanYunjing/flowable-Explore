package org.flowable.ui.task.service.runtime;

import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.flowable.common.engine.impl.EngineDeployer;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.cmd.AbstractDynamicInjectionCmd;
import org.flowable.engine.impl.cmd.GetProcessDefinitionInfoCmd;
import org.flowable.engine.impl.context.Context;
import org.flowable.engine.impl.dynamic.BaseDynamicSubProcessInjectUtil;
import org.flowable.engine.impl.dynamic.DynamicUserTaskBuilder;
import org.flowable.engine.impl.persistence.entity.DeploymentEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityManager;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.engine.impl.util.ProcessDefinitionUtil;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.identitylink.service.impl.persistence.entity.IdentityLinkEntity;
import org.flowable.job.service.impl.persistence.entity.DeadLetterJobEntity;
import org.flowable.job.service.impl.persistence.entity.JobEntity;
import org.flowable.job.service.impl.persistence.entity.SuspendedJobEntity;
import org.flowable.job.service.impl.persistence.entity.TimerJobEntity;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.util.*;


public class CustomInjectUserTaskInProcessInstanceCmd extends AbstractDynamicInjectionCmd implements Command<Void> {

    public String processInstanceId;
    public DynamicUserTaskBuilder dynamicUserTaskBuilder;
    public FlowElement currentFlowElemet;

    public  CustomInjectUserTaskInProcessInstanceCmd(String processInstanceId, DynamicUserTaskBuilder dynamicUserTaskBuilder, FlowElement currentFlowElemet) {
        this.processInstanceId = processInstanceId;
        this.dynamicUserTaskBuilder = dynamicUserTaskBuilder;
        this.currentFlowElemet = currentFlowElemet;
    }


    @Override
    public Void execute(CommandContext commandContext) {
        //AbstractDynamicInjectCmd提供的修改方法入口
        createDerivedProcessDefinitionForProcessInstance(commandContext,processInstanceId);
        return null;
    }

//    @Override
//    protected void createDerivedProcessDefinition(CommandContext commandContext, ProcessInstance processInstance) {
////        super.createDerivedProcessDefinition(commandContext, processInstance);
//
//    }


    @Override
    protected void updateBpmnProcess(CommandContext commandContext, Process process, BpmnModel bpmnModel, ProcessDefinitionEntity originalProcessDefinitionEntity, DeploymentEntity newDeploymentEntity) {
        List<StartEvent> startEvents = process.findFlowElementsOfType(StartEvent.class);
        StartEvent initialStartEvent = null;
        for (StartEvent startEvent : startEvents) {
            if (startEvent.getEventDefinitions().size() == 0) {
                initialStartEvent = startEvent;
                break;
            } else if (initialStartEvent == null) {
                initialStartEvent = startEvent;
            }
        }

        if(currentFlowElemet != null ){
            UserTask userTask = new UserTask();
            BeanUtils.copyProperties(currentFlowElemet,userTask);
            if (dynamicUserTaskBuilder.getId() != null) {
                userTask.setId(dynamicUserTaskBuilder.getId());
            } else {
                userTask.setId(dynamicUserTaskBuilder.nextTaskId(process.getFlowElementMap()));
            }
            dynamicUserTaskBuilder.setDynamicTaskId(userTask.getId());

            userTask.setName(dynamicUserTaskBuilder.getName());
            userTask.setAssignee(dynamicUserTaskBuilder.getAssignee());
            //设置用户任务其他属性 ?将某一节点属性及其属性值copy~~
//            userTask.setFormKey();


            UserTask currentFlowElemet = (UserTask) this.currentFlowElemet;
            SequenceFlow sequenceFlow = null;


            List<SequenceFlow> outgoingFlows = new ArrayList<>();

            for (SequenceFlow sequenceFlow1 : currentFlowElemet.getOutgoingFlows()) {
                sequenceFlow = new SequenceFlow(userTask.getId(),sequenceFlow1.getTargetRef());
                sequenceFlow.setSkipExpression(sequenceFlow1.getSkipExpression());
                sequenceFlow.setConditionExpression(sequenceFlow1.getConditionExpression());
                sequenceFlow.setExtensionElements(sequenceFlow1.getExtensionElements());
                sequenceFlow.setExecutionListeners(sequenceFlow1.getExecutionListeners());
                sequenceFlow.setName(sequenceFlow1.getName());
                sequenceFlow.setId("seq_"+ UUID.randomUUID().toString() );
                outgoingFlows.add(sequenceFlow);
                //删除原先节点的出线
                process.removeFlowElement(sequenceFlow1.getId());
                process.addFlowElement(sequenceFlow);
            }

            List<SequenceFlow> incomingFlows = new ArrayList<>();
            SequenceFlow incomingFlow = new  SequenceFlow(currentFlowElemet.getId(),userTask.getId());
            // 可以设置唯一编号，这里通过雪花算法设置--改为UUID
            incomingFlow.setId("sid-"+UUID.randomUUID().toString());
            incomingFlows.add(incomingFlow);

            process.addFlowElement(incomingFlow);
            userTask.setOutgoingFlows(outgoingFlows);
            userTask.setIncomingFlows(incomingFlows);
            process.addFlowElement(userTask);

            //新增坐标 点
//            GraphicInfo elementGraphicInfo = bpmnModel.getGraphicInfo(currentFlowElemet.getId());
//            if (elementGraphicInfo != null) {
//                double yDiff = 0;
//                double xDiff = 80;
//                if (elementGraphicInfo.getY() < 173) {
//                    yDiff = 173 - elementGraphicInfo.getY();
//                    elementGraphicInfo.setY(173);
//                }

//                Map<String, GraphicInfo> locationMap = bpmnModel.getLocationMap();
//                for (String locationId : locationMap.keySet()) {
//                    if (initialStartEvent.getId().equals(locationId)) {
//                        continue;
//                    }
//
//                    GraphicInfo locationGraphicInfo = locationMap.get(locationId);
//                    locationGraphicInfo.setX(locationGraphicInfo.getX() + xDiff);
//                    locationGraphicInfo.setY(locationGraphicInfo.getY() + yDiff);
//                }

//                Map<String, List<GraphicInfo>> flowLocationMap = bpmnModel.getFlowLocationMap();
//                for (String flowId : flowLocationMap.keySet()) {
////                    if (flowFromStart.getId().equals(flowId)) {
////                        continue;
////                    }
//                    List<GraphicInfo> flowGraphicInfoList = flowLocationMap.get(flowId);
//                    for (GraphicInfo flowGraphicInfo : flowGraphicInfoList) {
//                        flowGraphicInfo.setX(flowGraphicInfo.getX() + xDiff);
//                        flowGraphicInfo.setY(flowGraphicInfo.getY() + yDiff);
//
//                    }
//                }

				/* 以下代码 可以替换以下步骤,推荐使用这种
				 步骤一： 引入 自动排版jar
				<dependency>
            		<groupId>org.flowable</groupId>
            		<artifactId>flowable-bpmn-layout</artifactId>
            		<version>6.4.1</version>
       			 </dependency>
       			 步骤二 调用自动排版方法：
       			         new BpmnAutoLayout(bpmnModel).execute();
				*/

                new BpmnAutoLayout(bpmnModel).execute();
//                /* 手动绘制节点 */
//                GraphicInfo newTaskGraphicInfo = new GraphicInfo(elementGraphicInfo.getX() + 185, elementGraphicInfo.getY() - 163, 80, 100);
//                bpmnModel.addGraphicInfo(userTask.getId(), newTaskGraphicInfo);
//
//                bpmnModel.addFlowGraphicInfoList(userTask.getId(), createWayPoints(elementGraphicInfo.getX() + 95, elementGraphicInfo.getY() - 5,
//                        elementGraphicInfo.getX() + 95, elementGraphicInfo.getY() - 123, elementGraphicInfo.getX() + 185, elementGraphicInfo.getY() - 123));
//
//                List<SequenceFlow> addFlows = new ArrayList<>();
//                addFlows.addAll(outgoingFlows);
//                addFlows.addAll(incomingFlows);

//                /* 绘制连线 */
//                for(SequenceFlow sequenceFlow1 :  addFlows){
//                    bpmnModel.addFlowGraphicInfoList(sequenceFlow1.getId(), createWayPoints(elementGraphicInfo.getX() + 30, elementGraphicInfo.getY() + 15,
//                            elementGraphicInfo.getX() + 75, elementGraphicInfo.getY() + 15));
//                }

//            }//end if elementGraphicInfo != null
        }//end if currentFlowElemet != null
        else {
            ParallelGateway parallelGateway = new ParallelGateway();
            parallelGateway.setId(dynamicUserTaskBuilder.nextForkGatewayId(process.getFlowElementMap()));
            process.addFlowElement(parallelGateway);

            UserTask userTask = new UserTask();
            if (dynamicUserTaskBuilder.getId() != null) {
                userTask.setId(dynamicUserTaskBuilder.getId());
            } else {
                userTask.setId(dynamicUserTaskBuilder.nextTaskId(process.getFlowElementMap()));
            }
            dynamicUserTaskBuilder.setDynamicTaskId(userTask.getId());

            userTask.setName(dynamicUserTaskBuilder.getName());
            userTask.setAssignee(dynamicUserTaskBuilder.getAssignee());
            process.addFlowElement(userTask);

            EndEvent endEvent = new EndEvent();
            endEvent.setId(dynamicUserTaskBuilder.nextEndEventId(process.getFlowElementMap()));
            process.addFlowElement(endEvent);

            SequenceFlow flowToUserTask = new SequenceFlow(parallelGateway.getId(), userTask.getId());
            flowToUserTask.setId(dynamicUserTaskBuilder.nextFlowId(process.getFlowElementMap()));
            process.addFlowElement(flowToUserTask);

            SequenceFlow flowFromUserTask = new SequenceFlow(userTask.getId(), endEvent.getId());
            flowFromUserTask.setId(dynamicUserTaskBuilder.nextFlowId(process.getFlowElementMap()));
            process.addFlowElement(flowFromUserTask);

            SequenceFlow initialFlow = initialStartEvent.getOutgoingFlows().get(0);
            initialFlow.setSourceRef(parallelGateway.getId());

            SequenceFlow flowFromStart = new SequenceFlow(initialStartEvent.getId(), parallelGateway.getId());
            flowFromStart.setId(dynamicUserTaskBuilder.nextFlowId(process.getFlowElementMap()));
            process.addFlowElement(flowFromStart);
            //跳整节点的布局
            GraphicInfo elementGraphicInfo = bpmnModel.getGraphicInfo(initialStartEvent.getId());
            if (elementGraphicInfo != null) {
                double yDiff = 0;
                double xDiff = 80;
                if (elementGraphicInfo.getY() < 173) {
                    yDiff = 173 - elementGraphicInfo.getY();
                    elementGraphicInfo.setY(173);
                }

                Map<String, GraphicInfo> locationMap = bpmnModel.getLocationMap();
                for (String locationId : locationMap.keySet()) {
                    if (initialStartEvent.getId().equals(locationId)) {
                        continue;
                    }

                    GraphicInfo locationGraphicInfo = locationMap.get(locationId);
                    locationGraphicInfo.setX(locationGraphicInfo.getX() + xDiff);
                    locationGraphicInfo.setY(locationGraphicInfo.getY() + yDiff);
                }

                Map<String, List<GraphicInfo>> flowLocationMap = bpmnModel.getFlowLocationMap();
                for (String flowId : flowLocationMap.keySet()) {
                    if (flowFromStart.getId().equals(flowId)) {
                        continue;
                    }

                    List<GraphicInfo> flowGraphicInfoList = flowLocationMap.get(flowId);
                    for (GraphicInfo flowGraphicInfo : flowGraphicInfoList) {
                        flowGraphicInfo.setX(flowGraphicInfo.getX() + xDiff);
                        flowGraphicInfo.setY(flowGraphicInfo.getY() + yDiff);
                    }
                }

                GraphicInfo forkGraphicInfo = new GraphicInfo(elementGraphicInfo.getX() + 75, elementGraphicInfo.getY() - 5, 40, 40);
                bpmnModel.addGraphicInfo(parallelGateway.getId(), forkGraphicInfo);

                bpmnModel.addFlowGraphicInfoList(flowFromStart.getId(), createWayPoints(elementGraphicInfo.getX() + 30, elementGraphicInfo.getY() + 15,
                        elementGraphicInfo.getX() + 75, elementGraphicInfo.getY() + 15));

                GraphicInfo newTaskGraphicInfo = new GraphicInfo(elementGraphicInfo.getX() + 185, elementGraphicInfo.getY() - 163, 80, 100);
                bpmnModel.addGraphicInfo(userTask.getId(), newTaskGraphicInfo);

                bpmnModel.addFlowGraphicInfoList(flowToUserTask.getId(), createWayPoints(elementGraphicInfo.getX() + 95, elementGraphicInfo.getY() - 5,
                        elementGraphicInfo.getX() + 95, elementGraphicInfo.getY() - 123, elementGraphicInfo.getX() + 185, elementGraphicInfo.getY() - 123));

                GraphicInfo endGraphicInfo = new GraphicInfo(elementGraphicInfo.getX() + 335, elementGraphicInfo.getY() - 137, 28, 28);
                bpmnModel.addGraphicInfo(endEvent.getId(), endGraphicInfo);

                bpmnModel.addFlowGraphicInfoList(flowFromUserTask.getId(), createWayPoints(elementGraphicInfo.getX() + 285, elementGraphicInfo.getY() - 123,
                        elementGraphicInfo.getX() + 335, elementGraphicInfo.getY() - 123));
            }
        }
        BaseDynamicSubProcessInjectUtil.processFlowElements(commandContext, process, bpmnModel, originalProcessDefinitionEntity, newDeploymentEntity);
    }

    @Override
    protected void updateExecutions(CommandContext commandContext, ProcessDefinitionEntity processDefinitionEntity, ExecutionEntity processInstance, List<ExecutionEntity> list) {

        ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager(commandContext);
        List<ExecutionEntity>  oldExecution = executionEntityManager.findChildExecutionsByProcessInstanceId(processInstance.getProcessInstanceId());
        ExecutionEntity execution = executionEntityManager.createChildExecution(processInstance);
        BpmnModel bpmnModel = ProcessDefinitionUtil.getBpmnModel(processDefinitionEntity.getId());

        org.flowable.task.service.TaskService taskService = CommandContextUtil.getTaskService(commandContext);
        List<TaskEntity> taskEntities= taskService.findTasksByProcessInstanceId(processInstanceId);
        // 删除当前活动任务
        for (TaskEntity taskEntity:taskEntities) {
            taskEntity.getIdentityLinks().stream().forEach(identityLinkEntity -> {
                if(identityLinkEntity.isGroup()){
                    taskEntity.deleteGroupIdentityLink(identityLinkEntity.getGroupId(),"candidate");
                }else{
                    taskEntity.deleteUserIdentityLink(identityLinkEntity.getUserId(),"participant");
                }
            });
//            if(taskEntity.getTaskDefinitionKey().equals(currentFlowElemet.getId())){
//                taskService.deleteTask(taskEntity,false);
//            }
        }
        //设置活动后的节点
//        UserTask userTask = (UserTask) bpmnModel.getProcessById(processDefinitionEntity.getKey()).getFlowElement(dynamicUserTaskBuilder.getId());
//        execution.setCurrentFlowElement(userTask);
//        Context.getAgenda().planContinueProcessOperation(execution);
    }


    @Override
    protected ProcessDefinitionEntity deployDerivedDeploymentEntity(CommandContext commandContext, DeploymentEntity deploymentEntity, ProcessDefinitionEntity originalProcessDefinitionEntity) {
        Map<String, Object> deploymentSettings = new HashMap();
//        deploymentSettings.put("isDerivedDeployment", true);
//        deploymentSettings.put("derivedProcessDefinitionId", originalProcessDefinitionEntity.getId());
//        if (originalProcessDefinitionEntity.getDerivedFromRoot() != null) {
//            deploymentSettings.put("derivedProcessDefinitionRootId", originalProcessDefinitionEntity.getDerivedFromRoot());
//        } else {
//            deploymentSettings.put("derivedProcessDefinitionRootId", originalProcessDefinitionEntity.getId());
//        }

        deploymentEntity.setNew(true);
        List<EngineDeployer> deployers = CommandContextUtil.getProcessEngineConfiguration(commandContext).getDeploymentManager().getDeployers();
        Iterator var6 = deployers.iterator();

        while(var6.hasNext()) {
            EngineDeployer engineDeployer = (EngineDeployer)var6.next();
            engineDeployer.deploy(deploymentEntity, deploymentSettings);
        }

        return (ProcessDefinitionEntity)deploymentEntity.getDeployedArtifacts(ProcessDefinitionEntity.class).get(0);

    }

    @Override
    protected void updateExecutions(CommandContext commandContext, ProcessDefinitionEntity processDefinitionEntity, ExecutionEntity processInstance, BpmnModel bpmnModel) {
        String previousProcessDefinitionId = processInstance.getProcessDefinitionId();
        Integer previousProcessDefinitionVersion = processInstance.getProcessDefinitionVersion();

        processInstance.setProcessDefinitionId(processDefinitionEntity.getId());
        processInstance.setProcessDefinitionVersion(processDefinitionEntity.getVersion());
//        processInstance.setProcessDefinitionId(previousProcessDefinitionId);
//        processInstance.setProcessDefinitionVersion(previousProcessDefinitionVersion);
        List<TaskEntity> currentTasks = CommandContextUtil.getTaskService(commandContext).findTasksByProcessInstanceId(processInstance.getId());
        Iterator var7 = currentTasks.iterator();

        while(var7.hasNext()) {
            TaskEntity currentTask = (TaskEntity)var7.next();
            currentTask.setProcessDefinitionId(previousProcessDefinitionId);
        }

        List<JobEntity> currentJobs = CommandContextUtil.getJobService(commandContext).findJobsByProcessInstanceId(processInstance.getId());
        Iterator var16 = currentJobs.iterator();

        while(var16.hasNext()) {
            JobEntity currentJob = (JobEntity)var16.next();
            currentJob.setProcessDefinitionId(processDefinitionEntity.getId());
        }

        List<TimerJobEntity> currentTimerJobs = CommandContextUtil.getTimerJobService(commandContext).findTimerJobsByProcessInstanceId(processInstance.getId());
        Iterator var18 = currentTimerJobs.iterator();

        while(var18.hasNext()) {
            TimerJobEntity currentTimerJob = (TimerJobEntity)var18.next();
            currentTimerJob.setProcessDefinitionId(processDefinitionEntity.getId());
        }

        List<SuspendedJobEntity> currentSuspendedJobs = CommandContextUtil.getJobService(commandContext).findSuspendedJobsByProcessInstanceId(processInstance.getId());
        Iterator var20 = currentSuspendedJobs.iterator();

        while(var20.hasNext()) {
            SuspendedJobEntity currentSuspendedJob = (SuspendedJobEntity)var20.next();
            currentSuspendedJob.setProcessDefinitionId(processDefinitionEntity.getId());
        }

        List<DeadLetterJobEntity> currentDeadLetterJobs = CommandContextUtil.getJobService(commandContext).findDeadLetterJobsByProcessInstanceId(processInstance.getId());
        Iterator var22 = currentDeadLetterJobs.iterator();

        while(var22.hasNext()) {
            DeadLetterJobEntity currentDeadLetterJob = (DeadLetterJobEntity)var22.next();
            currentDeadLetterJob.setProcessDefinitionId(processDefinitionEntity.getId());
        }

        List<IdentityLinkEntity> identityLinks = CommandContextUtil.getIdentityLinkService().findIdentityLinksByProcessDefinitionId(previousProcessDefinitionId);
        Iterator var24 = identityLinks.iterator();

        while(true) {
            IdentityLinkEntity identityLinkEntity;
            do {
                if (!var24.hasNext()) {
                    CommandContextUtil.getActivityInstanceEntityManager(commandContext).updateActivityInstancesProcessDefinitionId(processDefinitionEntity.getId(), processInstance.getId());
                    CommandContextUtil.getHistoryManager(commandContext).updateProcessDefinitionIdInHistory(processDefinitionEntity, processInstance);
                    List<ExecutionEntity> childExecutions = CommandContextUtil.getExecutionEntityManager(commandContext).findChildExecutionsByProcessInstanceId(processInstance.getId());
                    Iterator var26 = childExecutions.iterator();

                    while(var26.hasNext()) {
                        ExecutionEntity childExecution = (ExecutionEntity)var26.next();
                        childExecution.setProcessDefinitionId(processDefinitionEntity.getId());
                        childExecution.setProcessDefinitionVersion(processDefinitionEntity.getVersion());
                    }

                    this.updateExecutions(commandContext, processDefinitionEntity, processInstance, childExecutions);
                    return;
                }

                identityLinkEntity = (IdentityLinkEntity)var24.next();
            } while(identityLinkEntity.getTaskId() == null && identityLinkEntity.getProcessInstanceId() == null && identityLinkEntity.getScopeId() == null);

            identityLinkEntity.setProcessDefId(processDefinitionEntity.getId());
        }


    }
}
