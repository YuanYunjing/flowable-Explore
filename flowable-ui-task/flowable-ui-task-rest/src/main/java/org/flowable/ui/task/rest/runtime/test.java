package org.flowable.ui.task.rest.runtime;

import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.ProcessDefinitionQuery;
import org.flowable.ui.modeler.serviceapi.ModelService;
import org.flowable.ui.task.model.runtime.CreateProcessInstanceRepresentation;
import org.flowable.ui.task.model.runtime.ProcessInstanceRepresentation;
import org.flowable.ui.task.service.runtime.FlowableProcessInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@RestController
@RequestMapping("/app")
public class test {
    @Qualifier("processEngine")
    @Autowired private ProcessEngine processEngine;
    @Autowired private RepositoryService repositoryService;

    @GetMapping(value = "/rest/test/identifyBPMN/{processInstanceId}")
    public void test(@PathVariable String processInstanceId) {
        System.out.println("成功");
//        processInstanceId="07623215-1f5c-11eb-b1ac-7266552707c1";
        String currentProDefId = processEngine.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult().getProcessDefinitionId();
        if(currentProDefId ==null){
            currentProDefId=processEngine.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult().getProcessDefinitionId();
        }
        ProcessDefinitionQuery processDefinition = repositoryService.createProcessDefinitionQuery().processDefinitionId("yijian:6:eeaf3cde-2001-11eb-ac5f-7266552707c1");
        BpmnModel bpmnModel = repositoryService.getBpmnModel(currentProDefId);
        BpmnXMLConverter converter = new BpmnXMLConverter();
        byte[] bytes = converter.convertToXML(bpmnModel);
        String xmlContenxt=new String(bytes);
       System.out.println(xmlContenxt);
    }
}