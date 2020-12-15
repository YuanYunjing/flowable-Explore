package org.flowable.ui.task.rest.runtime;

import org.flowable.bpmn.model.Process;
import org.flowable.engine.DynamicBpmnService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.impl.dynamic.DynamicEmbeddedSubProcessBuilder;
import org.flowable.ui.task.service.runtime.CustomInjectEmbeddedSubProcessInstanceCmd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.swing.*;
import java.util.UUID;

/**
 * created 2020/7/13
 */
@RestController
@RequestMapping("/app")
@ResponseBody
public class InjectSubProcessComplexResource {
    @Autowired
    RuntimeService runtimeService;
    @Autowired
    TaskService taskService;
    @Qualifier("processEngine")
    @Autowired
    ProcessEngine processEngine;
    protected DynamicBpmnService dynamicBpmnService;

    @GetMapping(value = "/rest/InjectSubProcess")
    public void TestInjectParallelSubProcess(@RequestParam("currentTaskDefId") String currentTaskDefId,@RequestParam("currentProcessInstanceId") String currentProcessInstanceId,@RequestParam("recomment_service_processDefId") String recomment_service_processDefId){
        System.out.println(currentTaskDefId);
        //根据流程实例获得流程定义id
        String currentProDefId = processEngine.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(currentProcessInstanceId).singleResult().getProcessDefinitionId();

        if(currentProDefId ==null){

            currentProDefId=processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(currentProcessInstanceId).singleResult().getProcessDefinitionId();

        }
        //构建子流程buider
        DynamicEmbeddedSubProcessBuilder dynamicEmbeddedSubProcessBuilder=new DynamicEmbeddedSubProcessBuilder().id("injectedProcess"+ UUID.randomUUID().toString()).processDefinitionId(recomment_service_processDefId);

        String currentProcessDefinitonId = processEngine.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(currentProcessInstanceId).singleResult().getProcessDefinitionId();

        if (currentProcessDefinitonId==null){
            currentProcessDefinitonId=processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(currentProcessInstanceId).singleResult().getProcessDefinitionId();
        }
        //得到流程的process
        Process process = processEngine.getRepositoryService().getBpmnModel(currentProcessDefinitonId).getProcesses().get(0);
        try {
            processEngine.getManagementService().executeCommand(new CustomInjectEmbeddedSubProcessInstanceCmd(currentProcessInstanceId, dynamicEmbeddedSubProcessBuilder,process.getFlowElement(currentTaskDefId)));
            JOptionPane.showMessageDialog(null, "选择服务成功！", "提示", JOptionPane.INFORMATION_MESSAGE);
        }catch (Exception e){
            e.printStackTrace();
        }

    }
}
