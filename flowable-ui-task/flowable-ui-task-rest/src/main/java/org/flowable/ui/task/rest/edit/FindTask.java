package org.flowable.ui.task.rest.edit;

import org.flowable.bpmn.model.FlowElement;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.ServiceTask;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;


@RestController
@RequestMapping("/app")
public class FindTask {
    @Autowired private TaskService taskService;
    @Autowired private RepositoryService repositoryService;
    @Autowired private  RuntimeService runtimeService;
    @Autowired private HistoryService historyService;

    @GetMapping(value = "/rest/getTaskByProcessInstanceId/{processInstanceId}")
    public  IdentityHashMap<String ,String> getTaskByProcessInstanceId(@PathVariable String processInstanceId){

        //根据流程实例获得流程定义id
        String currentProDefId = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult().getProcessDefinitionId();

        if(currentProDefId ==null){

            currentProDefId=runtimeService.createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult().getProcessDefinitionId();

        }
        //获取所有节点信息
        Process process = repositoryService.getBpmnModel(currentProDefId).getProcesses().get(0);
        Collection<FlowElement> flowElements = process.getFlowElements();
        //保存流程实例中，每个任务的名称与对呀ID的map
        //HashMap<String, String> TaskNameMapId = new HashMap<String, String>();

        IdentityHashMap<String ,String> TaskNameMapId = new IdentityHashMap<String ,String>();
        for(FlowElement flowElement:flowElements){
            if((flowElement instanceof UserTask)||(flowElement instanceof ServiceTask)){
                TaskNameMapId.put(flowElement.getId(),flowElement.getName());
//                System.out.println("UserTask"+flowElement.getName());
//                System.out.println("UserTaskId"+flowElement.getId());
            }
        }
//        JSONObject TaskNameMapIdJson =  new JSONObject(TaskNameMapId);
//        System.out.println(TaskNameMapIdJson);

//        //当前正在活动的任务
//        Task task = taskService.createTaskQuery().processInstanceId(processInstanceId).singleResult();
//        System.out.println(task.getName());
        return TaskNameMapId;
    }


}
