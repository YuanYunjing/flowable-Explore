package org.flowable.ui.task.rest.edit;

import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.flowable.engine.*;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.ui.task.service.local.LocalCompleteTaskCmd;
import org.flowable.ui.task.service.local.LocalCurrentActivityCommand;
import org.flowable.ui.task.service.runtime.FlowableTaskFormService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RestController
@RequestMapping("/app/rest/task-run-test")
public class EditTaskRunResource {
    @Autowired
    protected FlowableTaskFormService taskFormService;
    @Autowired
    protected  TaskService taskService;
    @Autowired
    protected RepositoryService repositoryService;

    @Autowired
    protected RuntimeService runtimeService;

    @Autowired
    protected HistoryService historyService;

    @Qualifier("processEngine")
    @Autowired
    ProcessEngine processEngine;

    @PostMapping(value="/{processInstanceId}",produces = "application/json")
//    public void completeTask(@PathVariable String taskId, @RequestBody CompleteFormRepresentation completeTaskFormRepresentation) {
//        taskFormService.completeTaskForm(taskId, completeTaskFormRepresentation);
//    }
    public void completeTask(@PathVariable String processInstanceId){
        //根据流程实例id获取当前活动任务id
        Task currentTask = taskService.createTaskQuery().processInstanceId(processInstanceId).active().singleResult();
        if(currentTask != null) {
            String taskId=currentTask.getId();
            complete(taskId);  // 完成方法修改：主要修改EndExecutionOperation：保存时不会提醒...无法保存

//            taskService.complete(taskId);  // 原始完成方法调用：无结束节点但执行完后，再次修改后无法保存

        } else {  // 流程中无当前节点时

            // 获得流程定义中所有的流程节点
            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
            String processDefinitionId = processInstance.getProcessDefinitionId();
            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId(processInstance.getProcessDefinitionId()).singleResult();
            Process process = repositoryService.getBpmnModel(processDefinitionId).getProcesses().get(0);
            Collection<FlowElement> flowElements = process.getFlowElements();

            // 获得流程实例中所有的完成节点
            List<HistoricActivityInstance> historyActivities = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).finished().list();
            List<String> historyActivityIds = new ArrayList<>();
            for (HistoricActivityInstance historyActivitie : historyActivities) {
                historyActivityIds.add(historyActivitie.getActivityId());
            }


            for (FlowElement flowElement : flowElements) {
                if (!(flowElement instanceof SequenceFlow)) {
                    if(!historyActivityIds.contains(flowElement.getId())) {
                        SequenceFlow incomingFlowElement = ((FlowNode)flowElement).getIncomingFlows().get(0);
                        FlowElement sourceElement = incomingFlowElement.getSourceFlowElement();
                        if(historyActivityIds.contains(sourceElement.getId()) && !(sourceElement instanceof ExclusiveGateway)){
                            // 设置新的当前节点
                            processEngine.getManagementService().executeCommand(new LocalCurrentActivityCommand(processInstanceId, flowElement));
                        }
                    }
                }
            }

        }

    }

    public void complete(String taskId) {
        processEngine.getManagementService().executeCommand(new LocalCompleteTaskCmd(taskId, null));
    }

}
