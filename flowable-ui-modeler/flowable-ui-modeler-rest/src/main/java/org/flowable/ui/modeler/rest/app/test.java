package org.flowable.ui.modeler.rest.app;

import com.fasterxml.jackson.databind.node.ObjectNode;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.ui.modeler.serviceapi.ModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app")
public class test {

    @Autowired ModelService modelService;
    @Autowired RepositoryService repositoryService;

    @Qualifier("processEngine")
    @Autowired ProcessEngine processEngine;
    @GetMapping(value = "/rest/test")
    public ObjectNode getProcessInstance(){
        String processInstanceId="15beef2a-c44a-11ea-970b-7266552707c1";
        HistoricProcessInstance processInstance = processEngine.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        String processDefId = processInstance.getProcessDefinitionId();
        ProcessDefinition processDefiniton = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefId).singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefId);
        BpmnJsonConverter bpmnJsonConverter=new BpmnJsonConverter();
        ObjectNode projoJson = bpmnJsonConverter.convertToJson(bpmnModel);
        System.out.println(projoJson);
        return projoJson;
//        String deploymentId = processDefiniton.getDeploymentId();
//        String modelName = processDefiniton.getName();
//        List<Model> model = repositoryService.createModelQuery().list();
//
//        System.out.println("111111");
////        Model model2 = model.singleResult();
////        String modelId = model2.getId();
//        System.out.println("2222");
////        modelService.getModelRepresentation(modelId);
//
//
//
//        String modelId2="47780521-bb35-11ea-8ae1-7266552707c1";
//        byte[] a = repositoryService.getModelEditorSource(modelId2);
//        Model c = repositoryService.getModel(modelId2);
//        ModelRepresentation b = modelService.getModelRepresentation(modelId2);
//        org.flowable.ui.modeler.domain.Model e = modelService.getModel(modelId2);
//        System.out.println(a);
//        System.out.println("2222");




    }
}
