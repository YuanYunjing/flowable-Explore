package org.flowable.ui.task.service.runtime;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.impl.dynamic.DynamicUserTaskBuilder;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.*;
import java.util.Collection;
import java.util.UUID;

/**
 * 0628任务：给定任务id后添加
 */
@Service
@Transactional
public class InjectNodeService {

    @Autowired
    protected RepositoryService repositoryService;

    @Qualifier("processEngine")
    @Autowired
    protected ProcessEngine processEngine;

    public void injectUserTask(String currentTaskDefId, String currentProInsId,String recommendation_service_id) {
        /**
         * 1、功能：根据服务推荐的流程定义id获取需添加的服务节点参数信息
         */
        BpmnModel bpmnModel = repositoryService.getBpmnModel(recommendation_service_id);//根据推荐服务流程定义id获取bpmnmodel
        //获取到bpmnmodel后，可进行任意的获取操作，
        // 获取当前模型下面所有任务节点
        Process recomProcess = bpmnModel.getProcesses().get(0);//获取process
        Collection<UserTask> flowElements_recom = recomProcess.findFlowElementsOfType(UserTask.class);
        //节点属性定义
        String newUserTaskId = null;
        String newUserTaskName = null;
        String newUserTaskAssignee = null;

        for (UserTask userTask : flowElements_recom) {
            newUserTaskName = userTask.getName();
            newUserTaskAssignee = userTask.getAssignee();
            newUserTaskId = userTask.getId();


            String dynamicUserId = "sid-"+ UUID.randomUUID().toString().replaceAll("-","");

            DynamicUserTaskBuilder dynamicUserTaskBuilder = new DynamicUserTaskBuilder();
            dynamicUserTaskBuilder.setId(dynamicUserId);
            dynamicUserTaskBuilder.setName(newUserTaskName+"新增节点");
            dynamicUserTaskBuilder.setAssignee(newUserTaskAssignee);

//                //方式1、通过当前流程实例的taskId获取流程实例id及流程定义id(单个节点添加)
//                HistoricTaskInstance taskInstance = processEngine.getHistoryService().createHistoricTaskInstanceQuery().taskId(CurrentTaskId).singleResult();
//                String currentProInsId = taskInstance.getProcessInstanceId();//得到流程实例id
//                String currentProDefId = taskInstance.getProcessDefinitionId();//得到流程定义id
//                String currentTaskDefId = taskInstance.getTaskDefinitionKey();//得到任务定义key

//                方式2：currentProInsId已知，传入参数：currentTaskDefId+recommendation_service_id
//                String curredntProInsId="b1891113-c0cd-11ea-a2ab-7266552707c1";

            String currentProDefId = processEngine.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(currentProInsId).singleResult().getProcessDefinitionId();

            if(currentProDefId ==null){

                currentProDefId=processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(currentProInsId).singleResult().getProcessDefinitionId();

            }
//        end 方式3

            Process CurrentProcess = repositoryService.getBpmnModel(currentProDefId).getProcesses().get(0);

            try {
                //通过流程定义获取该process
                processEngine.getManagementService().executeCommand(new CustomInjectUserTaskInProcessInstanceCmd(currentProInsId, dynamicUserTaskBuilder,CurrentProcess.getFlowElement(currentTaskDefId)));
                currentTaskDefId=dynamicUserId;

            }catch (Exception e){
                JOptionPane.showMessageDialog(null, "选择服务失败", "提示", JOptionPane.ERROR_MESSAGE);
            }

        }
    }

    public void injectUserTask2(String currentTaskId, String recommendation_service_id){
        /**
         * 1、功能：根据服务推荐的流程定义id获取需添加的服务节点参数信息
         */
        BpmnModel bpmnModel = repositoryService.getBpmnModel(recommendation_service_id);//根据推荐服务流程定义id获取bpmnmodel
        //获取到bpmnmodel后，可进行任意的获取操作，
        // 获取当前模型下面所有任务节点
        Process recomProcess = bpmnModel.getProcesses().get(0);//获取process
        Collection<UserTask> flowElements_recom = recomProcess.findFlowElementsOfType(UserTask.class);
        //节点属性定义
        String newUserTaskId = null;
        String newUserTaskName = null;
        String newUserTaskAssignee = null;

        for (UserTask userTask : flowElements_recom) {
            newUserTaskName = userTask.getName();
            newUserTaskAssignee = userTask.getAssignee();
            newUserTaskId = userTask.getId();


            String dynamicUserId = "sid-"+ UUID.randomUUID().toString().replaceAll("-","");

            DynamicUserTaskBuilder dynamicUserTaskBuilder = new DynamicUserTaskBuilder();
            dynamicUserTaskBuilder.setId(dynamicUserId);
            dynamicUserTaskBuilder.setName(newUserTaskName+"新增节点");
            dynamicUserTaskBuilder.setAssignee(newUserTaskAssignee);

            //方式1、通过当前流程实例的taskId获取流程实例id及流程定义id(单个节点添加)
            HistoricTaskInstance taskInstance = processEngine.getHistoryService().createHistoricTaskInstanceQuery().taskId(currentTaskId).singleResult();
            String currentProInsId = taskInstance.getProcessInstanceId();//得到流程实例id
            String currentProDefId = taskInstance.getProcessDefinitionId();//得到流程定义id
            String currentTaskDefId = taskInstance.getTaskDefinitionKey();//得到任务定义key

////                方式2：currentProInsId已知，传入参数：currentTaskDefId+recommendation_service_id
////                String curredntProInsId="b1891113-c0cd-11ea-a2ab-7266552707c1";
//
//            String currentProDefId = processEngine.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(currentProInsId).singleResult().getProcessDefinitionId();
//
//            if(currentProDefId ==null){
//
//                currentProDefId=processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(currentProInsId).singleResult().getProcessDefinitionId();
//
//            }
//        end 方式2

            Process CurrentProcess = repositoryService.getBpmnModel(currentProDefId).getProcesses().get(0);

            try {
                //通过流程定义获取该process
                processEngine.getManagementService().executeCommand(new CustomInjectUserTaskInProcessInstanceCmd(currentProInsId, dynamicUserTaskBuilder,CurrentProcess.getFlowElement(currentTaskDefId)));
                currentTaskDefId=dynamicUserId;

            }catch (Exception e){
                JOptionPane.showMessageDialog(null, "选择服务失败！", "提示", JOptionPane.ERROR_MESSAGE);
            }

        }
    }

}

