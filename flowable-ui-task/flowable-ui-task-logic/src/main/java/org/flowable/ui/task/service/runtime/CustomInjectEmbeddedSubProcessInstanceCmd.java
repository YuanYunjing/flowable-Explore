package org.flowable.ui.task.service.runtime;

import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.impl.EngineDeployer;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.common.engine.impl.util.IoUtil;
import org.flowable.common.engine.impl.util.io.BytesStreamSource;
import org.flowable.dmn.api.DmnDecisionTable;
import org.flowable.dmn.api.DmnDeployment;
import org.flowable.dmn.api.DmnRepositoryService;
import org.flowable.dmn.engine.test.FlowableDmnRule;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.impl.cmd.AbstractDynamicInjectionCmd;
import org.flowable.engine.impl.context.Context;
import org.flowable.engine.impl.dynamic.BaseDynamicSubProcessInjectUtil;
import org.flowable.engine.impl.dynamic.DynamicEmbeddedSubProcessBuilder;
import org.flowable.engine.impl.dynamic.DynamicSubProcessParallelInjectUtil;
import org.flowable.engine.impl.dynamic.DynamicUserTaskBuilder;
import org.flowable.engine.impl.persistence.entity.*;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.engine.impl.util.ProcessDefinitionUtil;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.form.api.FormDefinition;
import org.flowable.form.api.FormDeployment;
import org.flowable.form.api.FormRepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.InputStream;
import java.util.*;
//继承AbstractDynamicInjectionCmd
public class CustomInjectEmbeddedSubProcessInstanceCmd extends AbstractDynamicInjectionCmd implements Command<Void> {

    @Autowired ProcessEngine processEngine;

    public String processInstanceId;
    public DynamicEmbeddedSubProcessBuilder dynamicEmbeddedSubProcessBuilder;
    //节点
    public FlowElement currentFlowElemet;




    public  CustomInjectEmbeddedSubProcessInstanceCmd(String processInstanceId, DynamicEmbeddedSubProcessBuilder dynamicEmbeddedSubProcessBuilder, FlowElement currentFlowElemet) {
        this.processInstanceId = processInstanceId;
        this.currentFlowElemet=currentFlowElemet;
        this.dynamicEmbeddedSubProcessBuilder = dynamicEmbeddedSubProcessBuilder;
    }
    @Override
    public Void execute(CommandContext commandContext) {
        createDerivedProcessDefinitionForProcessInstance(commandContext,processInstanceId);
        return null;
    }

    @Override
    protected void updateBpmnProcess(CommandContext commandContext, Process process, BpmnModel bpmnModel, ProcessDefinitionEntity originalprocessDefinitionEntity, DeploymentEntity newDeploymentEntity) {

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
        GraphicInfo elementGraphicInfo = bpmnModel.getGraphicInfo(initialStartEvent.getId());

        if(currentFlowElemet!=null){
            SubProcess subProcess = new SubProcess();
            if (dynamicEmbeddedSubProcessBuilder.getId() != null) {
                subProcess.setId(dynamicEmbeddedSubProcessBuilder.getId());
            } else {
                subProcess.setId(dynamicEmbeddedSubProcessBuilder.nextSubProcessId(process.getFlowElementMap()));
            }
            dynamicEmbeddedSubProcessBuilder.setDynamicSubProcessId(subProcess.getId());

            ProcessDefinition subProcessDefinition = ProcessDefinitionUtil.getProcessDefinition(dynamicEmbeddedSubProcessBuilder.getProcessDefinitionId());
            ResourceEntity subProcessBpmnResource = CommandContextUtil.getResourceEntityManager(commandContext).findResourceByDeploymentIdAndResourceName(subProcessDefinition.getDeploymentId(), subProcessDefinition.getResourceName());
            BpmnModel bpmnModelSubProcess = new BpmnXMLConverter().convertToBpmnModel(new BytesStreamSource(subProcessBpmnResource.getBytes()), false, false);
            for(FlowElement flowElement:bpmnModelSubProcess.getMainProcess().getFlowElements()){
                subProcess.addFlowElement(flowElement);
            }
            BaseDynamicSubProcessInjectUtil.processFlowElements(commandContext,process,bpmnModel,originalprocessDefinitionEntity,newDeploymentEntity);

            Map<String, FlowElement> generatedIds = new HashMap<>();
            //1.
            processSubProcessFlowElements(commandContext, subProcess.getId(), process, bpmnModel, subProcess, bpmnModelSubProcess,
                    subProcessDefinition, newDeploymentEntity, generatedIds, (elementGraphicInfo != null));

            for (String originalFlowElementId : generatedIds.keySet()) {
                FlowElement duplicateFlowElement = generatedIds.get(originalFlowElementId);
                duplicateFlowElement.getParentContainer().removeFlowElementFromMap(originalFlowElementId);
                duplicateFlowElement.getParentContainer().addFlowElementToMap(duplicateFlowElement);
            }

            process.addFlowElement(subProcess);


//            //sequenceFlow加线
//            SequenceFlow sequenceFlow = null;
//            List<SequenceFlow> outgoingFlows = new ArrayList<>();
////            for (SequenceFlow sequenceFlow1 : ((FlowNode)currentFlowElemet).getOutgoingFlows()) {//获取currentFlowElement的所有出线信息
//            SequenceFlow sequenceFlow1= (SequenceFlow) ((FlowNode)currentFlowElemet).getOutgoingFlows();
//            Collection<FlowElement> flowElement2 = subProcess.getFlowElements();
//                for(FlowElement flowElement:flowElement2){
//                    if(flowElement instanceof EndEvent){
////                        List<SequenceFlow> sequenceFlow2 = ((EndEvent) flowElement).getIncomingFlows();
//                        for(SequenceFlow sequenceFlow2:((EndEvent) flowElement).getIncomingFlows()){
//                            sequenceFlow=new SequenceFlow(sequenceFlow2.getSourceRef(),sequenceFlow1.getTargetRef());
//                            sequenceFlow.setSkipExpression(sequenceFlow1.getSkipExpression());
//                            sequenceFlow.setConditionExpression(sequenceFlow1.getConditionExpression());
//                            sequenceFlow.setExtensionElements(sequenceFlow1.getExtensionElements());
//                            sequenceFlow.setExecutionListeners(sequenceFlow1.getExecutionListeners());
//                            sequenceFlow.setName(sequenceFlow1.getName());
//                            sequenceFlow.setId("sid-"+ UUID.randomUUID().toString() );
//                            outgoingFlows.add(sequenceFlow);
//                            //删除原先节点的出线
//                            process.removeFlowElement(sequenceFlow1.getId());
//                            process.addFlowElement(sequenceFlow);
//                        }
//                    }
//                }
//
////            }
            SequenceFlow flowToSubProcess = new SequenceFlow(currentFlowElemet.getId(), subProcess.getId());
            flowToSubProcess.setId(dynamicEmbeddedSubProcessBuilder.nextFlowId(process.getFlowElementMap()));
            process.addFlowElement(flowToSubProcess);
            SequenceFlow sequenceFlow1 = (((FlowNode) currentFlowElemet).getOutgoingFlows()).get(0);

            SequenceFlow flowFromSubProcess = new SequenceFlow(subProcess.getId(), sequenceFlow1.getTargetRef());
            flowFromSubProcess.setId(dynamicEmbeddedSubProcessBuilder.nextFlowId(process.getFlowElementMap()));
            process.addFlowElement(flowFromSubProcess);


            //移除sequenceFlow1
            process.removeFlowElement(sequenceFlow1.getId());

            new BpmnAutoLayout(bpmnModel).execute();

        }

    }

    @Override
    protected void updateExecutions(CommandContext commandContext, ProcessDefinitionEntity processDefinitionEntity, ExecutionEntity processInstance, List<ExecutionEntity> childExecutions) {
        ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager(commandContext);

        BpmnModel bpmnModel = ProcessDefinitionUtil.getBpmnModel(processDefinitionEntity.getId());
        SubProcess subProcess = (SubProcess) bpmnModel.getFlowElement(dynamicEmbeddedSubProcessBuilder.getDynamicSubProcessId());
        ExecutionEntity subProcessExecution = executionEntityManager.createChildExecution(processInstance);
        subProcessExecution.setScope(true);
        subProcessExecution.setCurrentFlowElement(subProcess);
        CommandContextUtil.getActivityInstanceEntityManager(commandContext).recordActivityStart(subProcessExecution);

//        ExecutionEntity childExecution = executionEntityManager.createChildExecution(subProcessExecution);

        StartEvent initialEvent = null;
        for (FlowElement subElement : subProcess.getFlowElements()) {
            if (subElement instanceof StartEvent) {
                StartEvent startEvent = (StartEvent) subElement;
                if (startEvent.getEventDefinitions().size() == 0) {
                    initialEvent = startEvent;
                    break;
                }
            }
        }

        if (initialEvent == null) {
            throw new FlowableException("Could not find a none start event in dynamic sub process");
        }

//        childExecution.setCurrentFlowElement(initialEvent);

//        Context.getAgenda().planContinueProcessOperation(childExecution);
    }



    protected static void processSubProcessFlowElements(CommandContext commandContext, String prefix, Process process, BpmnModel bpmnModel,
                                                        SubProcess subProcess, BpmnModel subProcessBpmnModel, ProcessDefinition originalProcessDefinition,
                                                        DeploymentEntity newDeploymentEntity, Map<String, FlowElement> generatedIds, boolean includeDiInfo) {

        Collection<FlowElement> flowElementsOfSubProcess = subProcess.getFlowElementMap().values();
        for (FlowElement flowElement : flowElementsOfSubProcess) {

            if (process.getFlowElement(flowElement.getId(), true) != null) {
                generateIdForDuplicateFlowElement(prefix, process, bpmnModel, subProcessBpmnModel, flowElement, generatedIds, includeDiInfo);
            } else {
                if (includeDiInfo) {
                    if (flowElement instanceof SequenceFlow) {
                        List<GraphicInfo> wayPoints = subProcessBpmnModel.getFlowLocationGraphicInfo(flowElement.getId());
                        if (wayPoints != null) {
                            bpmnModel.addFlowGraphicInfoList(flowElement.getId(), wayPoints);
                        }

                    } else {
                        GraphicInfo graphicInfo = subProcessBpmnModel.getGraphicInfo(flowElement.getId());
                        if (graphicInfo != null) {
                            bpmnModel.addGraphicInfo(flowElement.getId(), subProcessBpmnModel.getGraphicInfo(flowElement.getId()));
                        }
                    }
                }
            }

            processUserTask(flowElement, originalProcessDefinition, newDeploymentEntity, commandContext);
            processDecisionTask(flowElement, originalProcessDefinition, newDeploymentEntity, commandContext);

            if (flowElement instanceof SubProcess) {
                processSubProcessFlowElements(commandContext, prefix, process, bpmnModel, (SubProcess) flowElement,
                        subProcessBpmnModel, originalProcessDefinition, newDeploymentEntity, generatedIds, includeDiInfo);
            }
        }
    }

    protected static void generateIdForDuplicateFlowElement(String prefix, org.flowable.bpmn.model.Process process, BpmnModel bpmnModel,
                                                            BpmnModel subProcessBpmnModel, FlowElement duplicateFlowElement, Map<String, FlowElement> generatedIds, boolean includeDiInfo) {

        String originalFlowElementId = duplicateFlowElement.getId();
        if (process.getFlowElement(originalFlowElementId, true) != null) {
            String newFlowElementId = prefix + "-" + originalFlowElementId;
            int counter = 0;
            boolean maxLengthReached = false;
            while (!maxLengthReached && process.getFlowElement(newFlowElementId, true) != null) {
                newFlowElementId = prefix + counter++ + "-" + originalFlowElementId;
                if (newFlowElementId.length() > 255) {
                    maxLengthReached = true;
                }
            }

            if (maxLengthReached) {
                newFlowElementId = prefix + "-" + UUID.randomUUID().toString();
            }

            duplicateFlowElement.setId(newFlowElementId);
            generatedIds.put(originalFlowElementId, duplicateFlowElement);

            if (includeDiInfo) {
                if (duplicateFlowElement instanceof SequenceFlow) {
                    bpmnModel.addFlowGraphicInfoList(newFlowElementId, subProcessBpmnModel.getFlowLocationGraphicInfo(originalFlowElementId));

                } else {
                    bpmnModel.addGraphicInfo(newFlowElementId, subProcessBpmnModel.getGraphicInfo(originalFlowElementId));
                }
            }

            for (FlowElement flowElement : duplicateFlowElement.getParentContainer().getFlowElements()) {
                if (flowElement instanceof SequenceFlow) {
                    SequenceFlow sequenceFlow = (SequenceFlow) flowElement;
                    if (sequenceFlow.getSourceRef().equals(originalFlowElementId)) {
                        sequenceFlow.setSourceRef(newFlowElementId);
                    }
                    if (sequenceFlow.getTargetRef().equals(originalFlowElementId)) {
                        sequenceFlow.setTargetRef(newFlowElementId);
                    }

                } else if (flowElement instanceof BoundaryEvent) {
                    BoundaryEvent boundaryEvent = (BoundaryEvent) flowElement;
                    if (boundaryEvent.getAttachedToRefId().equals(originalFlowElementId)) {
                        boundaryEvent.setAttachedToRefId(newFlowElementId);
                    }
                    if (boundaryEvent.getEventDefinitions() != null
                            && boundaryEvent.getEventDefinitions().size() > 0
                            && (boundaryEvent.getEventDefinitions().get(0) instanceof CompensateEventDefinition)) {

                        CompensateEventDefinition compensateEventDefinition = (CompensateEventDefinition) boundaryEvent.getEventDefinitions().get(0);
                        if (compensateEventDefinition.getActivityRef().equals(originalFlowElementId)) {
                            compensateEventDefinition.setActivityRef(newFlowElementId);
                        }
                    }
                }
            }


        }

        if (duplicateFlowElement instanceof FlowElementsContainer) {
            FlowElementsContainer flowElementsContainer = (FlowElementsContainer) duplicateFlowElement;
            for (FlowElement childFlowElement : flowElementsContainer.getFlowElements()) {
                generateIdForDuplicateFlowElement(prefix, process, bpmnModel, subProcessBpmnModel, childFlowElement, generatedIds, includeDiInfo);
            }
        }
    }

    protected static void processUserTask(FlowElement flowElement, ProcessDefinition originalProcessDefinitionEntity,
                                          DeploymentEntity newDeploymentEntity, CommandContext commandContext) {

        if (flowElement instanceof UserTask) {
            FormRepositoryService formRepositoryService;
            formRepositoryService = CommandContextUtil.getFormRepositoryService();
            if (formRepositoryService != null) {
                UserTask userTask = (UserTask) flowElement;
                if (StringUtils.isNotEmpty(userTask.getFormKey())) {
                    Deployment deployment = CommandContextUtil.getDeploymentEntityManager().findById(originalProcessDefinitionEntity.getDeploymentId());
                    if (deployment.getParentDeploymentId() != null) {
                        List<FormDeployment> formDeployments = formRepositoryService.createDeploymentQuery().parentDeploymentId(deployment.getParentDeploymentId()).list();

                        if (formDeployments != null && formDeployments.size() > 0) {

                            FormDefinition formDefinition = formRepositoryService.createFormDefinitionQuery()
                                    .formDefinitionKey(userTask.getFormKey()).deploymentId(formDeployments.get(0).getId()).latestVersion().singleResult();
                            if (formDefinition != null) {
                                String name = formDefinition.getResourceName();
                                InputStream inputStream = formRepositoryService.getFormDefinitionResource(formDefinition.getId());
                                addResource(commandContext, newDeploymentEntity, name, IoUtil.readInputStream(inputStream, name));
                                IoUtil.closeSilently(inputStream);
                            }
                        }
                    }
                }
            }
        }
    }

    protected static void processDecisionTask(FlowElement flowElement, ProcessDefinition originalProcessDefinitionEntity,
                                              DeploymentEntity newDeploymentEntity, CommandContext commandContext) {

        if (flowElement instanceof ServiceTask && ServiceTask.DMN_TASK.equals(((ServiceTask) flowElement).getType())) {

            DmnRepositoryService dmnRepositoryService = CommandContextUtil.getDmnRepositoryService();
            if (dmnRepositoryService != null) {
                ServiceTask serviceTask = (ServiceTask) flowElement;
                if (serviceTask.getFieldExtensions() != null && serviceTask.getFieldExtensions().size() > 0) {
                    String decisionTableReferenceKey = null;
                    for (FieldExtension fieldExtension : serviceTask.getFieldExtensions()) {
                        if ("decisionTableReferenceKey".equals(fieldExtension.getFieldName())) {
                            decisionTableReferenceKey = fieldExtension.getStringValue();
                            break;
                        }
                    }

                    if (decisionTableReferenceKey != null) {
                        Deployment deployment = CommandContextUtil.getDeploymentEntityManager().findById(originalProcessDefinitionEntity.getDeploymentId());
                        if (deployment.getParentDeploymentId() != null) {
                            List<DmnDeployment> dmnDeployments = dmnRepositoryService.createDeploymentQuery().parentDeploymentId(deployment.getParentDeploymentId()).list();

                            if (dmnDeployments != null && dmnDeployments.size() > 0) {
                                DmnDecisionTable dmnDecisionTable = dmnRepositoryService.createDecisionTableQuery()
                                        .decisionTableKey(decisionTableReferenceKey).deploymentId(dmnDeployments.get(0).getId()).latestVersion().singleResult();
                                if (dmnDecisionTable != null) {
                                    String name = dmnDecisionTable.getResourceName();
                                    InputStream inputStream = dmnRepositoryService.getDmnResource(dmnDecisionTable.getId());
                                    addResource(commandContext, newDeploymentEntity, name, IoUtil.readInputStream(inputStream, name));
                                    IoUtil.closeSilently(inputStream);
                                }
                            }
                        }
                    }
                }
            }
        }
    }


    public static void addResource(CommandContext commandContext, DeploymentEntity deploymentEntity, String resourceName, byte[] bytes) {
        if (!deploymentEntity.getResources().containsKey(resourceName)) {
            ResourceEntityManager resourceEntityManager = CommandContextUtil.getResourceEntityManager(commandContext);
            ResourceEntity resourceEntity = resourceEntityManager.create();
            resourceEntity.setDeploymentId(deploymentEntity.getId());
            resourceEntity.setName(resourceName);
            resourceEntity.setBytes(bytes);
            resourceEntityManager.insert(resourceEntity);
            deploymentEntity.addResource(resourceEntity);
        }
    }


    // @Override 有个这个
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
}
