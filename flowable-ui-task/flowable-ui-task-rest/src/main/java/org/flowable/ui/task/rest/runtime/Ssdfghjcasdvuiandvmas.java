package org.flowable.ui.task.rest.runtime;

import org.flowable.bpmn.model.Process;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.impl.dynamic.DynamicUserTaskBuilder;
import org.flowable.ui.task.service.runtime.CustomInjectUserTaskInProcessInstanceCmd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/app")
@ResponseBody
public class Ssdfghjcasdvuiandvmas {
    @Autowired RepositoryService repositoryService;
    @Qualifier("processEngine")
    @Autowired ProcessEngine processEngine;

    @GetMapping(value = "/rest/injectnode1")
    public String testinjectnode(@RequestParam(value="def1_id") String def1_id, @RequestParam(value="def2_id") String def2_id)throws IOException
    {
//        String processInstaceId = "a12:1:f530a931-bae7-11ea-90ba-fae4e3d6256d";
//        String dynamicUserId = "sid-8422C349-AFF7-40A2-8702-B684C8FDCD0";
//        System.out.printf(dynamicUserId);
//        DynamicUserTaskBuilder dynamicUserTaskBuilder1 = new DynamicUserTaskBuilder();
//        dynamicUserTaskBuilder1.setId(dynamicUserId);
//        dynamicUserTaskBuilder1.setName("新增节点1");
//        dynamicUserTaskBuilder1.setAssignee("632511");
//        System.out.println("1111111111111111111111111111");
//        processEngine.getDynamicBpmnService().injectUserTaskInProcessInstance(processInstaceId,dynamicUserTaskBuilder1);
        System.out.println(def2_id);
        String processInstaceId = "3ff8cb0f-bcc5-11ea-85be-fae4e3d6256d";
        String taskInstaceId ="1";
        String dynamicUserId = "UserTaskAdd"+UUID.randomUUID().toString().replaceAll("-","");
        DynamicUserTaskBuilder dynamicUserTaskBuilder = new DynamicUserTaskBuilder();
        dynamicUserTaskBuilder.setId(dynamicUserId);
        dynamicUserTaskBuilder.setName("新增节点3");
        dynamicUserTaskBuilder.setAssignee("gaojian");


        Process process = repositoryService.getBpmnModel("a666:49:79bad621-bcc8-11ea-85be-fae4e3d6256d").getProcessById("a666");

        try {
            processEngine.getManagementService().executeCommand(new CustomInjectUserTaskInProcessInstanceCmd(processInstaceId, dynamicUserTaskBuilder,process.getFlowElement("sid-56E997BD-32AE-4155-B81C-C3FCA6055152")));
        }catch (Exception e){

            e.printStackTrace();
        }
        return "ok";
    }
}