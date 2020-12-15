package org.flowable.ui.task.rest.edit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.ui.common.service.exception.BadRequestException;
import org.flowable.ui.common.service.exception.InternalServerErrorException;
import org.flowable.ui.task.service.runtime.SaveAndEvolutionEditorProcessService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;

import java.util.Date;

@RestController
@RequestMapping("/app")
public class EditProcessInstanceResource {
    private static final Logger LOGGER = LoggerFactory.getLogger(EditProcessInstanceResource.class);

    private static final String RESOLVE_ACTION_OVERWRITE = "overwrite";
    private static final String RESOLVE_ACTION_SAVE_AS = "saveAs";
    private static final String RESOLVE_ACTION_NEW_VERSION = "newVersion";

    @Autowired protected HistoryService historyService;

    @Autowired protected RepositoryService repositoryService;

    @Autowired protected ObjectMapper objectMapper;

    @Qualifier("processEngine") @Autowired ProcessEngine processEngine;

    @Autowired protected SaveAndEvolutionEditorProcessService saveAndEvolutionEditorProcessService;

    @GetMapping(value = "/rest/models2/{processInstanceId}/editor/json", produces = "application/json")
    public ObjectNode getBpmnModelJSON(@PathVariable String processInstanceId) {
//        Model model = modelService.getModel(modelId);
        HistoricProcessInstance processInstance = historyService.createHistoricProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        String processDefId = processInstance.getProcessDefinitionId();
        ProcessDefinition processDefiniton = repositoryService.createProcessDefinitionQuery().processDefinitionId(processDefId).singleResult();
        BpmnModel bpmnModel = repositoryService.getBpmnModel(processDefId);
        Date deploymentTime = repositoryService.createDeploymentQuery().deploymentId(processDefiniton.getDeploymentId()).singleResult().getDeploymentTime();

        ObjectNode BpmnmodelNode = objectMapper.createObjectNode();
        String modelId=processInstanceId;
        BpmnmodelNode.put("modelId",modelId );
        BpmnmodelNode.put("name",processDefiniton.getName());
        BpmnmodelNode.put("key",processDefiniton.getKey());
        BpmnmodelNode.putPOJO("lastUpdated",deploymentTime);
        BpmnmodelNode.put("lastUpdatedBy","admin");
        BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();
        String BpmnmodelJson = bpmnJsonConverter.convertToJson(bpmnModel).toString();

        if (BpmnmodelJson!=null) {
            try {
                ObjectNode editorJsonNode = (ObjectNode) objectMapper.readTree(String.valueOf(BpmnmodelJson));
                editorJsonNode.put("modelType", "model");
                editorJsonNode.put("modelId",modelId);
                BpmnmodelNode.set("model", editorJsonNode);
            } catch (Exception e) {
                //LOGGER.error("Error reading editor json {}", processInstanceId, e);
                throw new InternalServerErrorException("Error reading editor json " + processInstanceId);
            }

        } else {
            ObjectNode editorJsonNode = objectMapper.createObjectNode();
            editorJsonNode.put("id", "canvas");
            editorJsonNode.put("resourceId", "canvas");
            ObjectNode stencilSetNode = objectMapper.createObjectNode();
            stencilSetNode.put("namespace", "http://b3mn.org/stencilset/bpmn2.0#");
            editorJsonNode.put("modelType", "model");
            // 能不能在这里把状态放上
            BpmnmodelNode.set("model", editorJsonNode);
        }
//        System.out.println(BpmnmodelJson);
        return BpmnmodelNode;
    }
    @GetMapping(value = "/rest/stencil-sets/editor", produces = "application/json")
    public JsonNode getStencilSetForEditor() {
        try {
            JsonNode stencilNode = objectMapper.readTree(this.getClass().getClassLoader().getResourceAsStream("stencilset_bpmn.json"));
            return stencilNode;
        } catch (Exception e) {
            //LOGGER.error("Error reading bpmn stencil set json", e);
            throw new InternalServerErrorException("Error reading bpmn stencil set json");
        }
    }
    @GetMapping(value = "/rest/stencil-sets/cmmneditor", produces = "application/json")
    public JsonNode getCmmnStencilSetForEditor() {
        try {
            JsonNode stencilNode = objectMapper.readTree(this.getClass().getClassLoader().getResourceAsStream("stencilset_cmmn.json"));
            return stencilNode;
        } catch (Exception e) {
            //LOGGER.error("Error reading bpmn stencil set json", e);
            throw new InternalServerErrorException("Error reading bpmn stencil set json");
        }
    }

    @PostMapping(value = "/rest/models2/{processInstanceId}/editor/json")
    public void saveEditorProcess(@PathVariable String processInstanceId, @RequestBody MultiValueMap<String, String> values) throws JsonProcessingException {
        saveAndEvolutionEditorProcessService.saveProcessInstance(processInstanceId,values);
    }

    @PostMapping(value = "/rest/evolution/{processInstanceId}/editor/json")
    public void EvolutionProcessDefinition(@PathVariable String processInstanceId, @RequestBody MultiValueMap<String, String> values) throws JsonProcessingException {
        saveAndEvolutionEditorProcessService.evolutionProcessDefinition(processInstanceId,values);
    }

}
