package org.flowable.ui.task.service.runtime;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.ui.common.service.exception.BadRequestException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.MultiValueMap;

@Service
@Transactional
public class SaveAndEvolutionEditorProcessService {
    private static final Logger LOGGER = LoggerFactory.getLogger(SaveAndEvolutionEditorProcessService.class);
    @Autowired
    protected ObjectMapper objectMapper;

    @Qualifier("processEngine")
    @Autowired
    protected ProcessEngine processEngine;

    @Autowired protected RepositoryService repositoryService;

    public BpmnModel getNewProcessInstanceBpmnModel(MultiValueMap<String, String> values) throws JsonProcessingException {
        //得到画板json数据
        String canavsProcessInstancejson = values.getFirst("json_xml");

        //画板json--转成BPMN
        BpmnJsonConverter bpmnJsonConverter = new BpmnJsonConverter();
        JsonNode modelNode = objectMapper.readTree(canavsProcessInstancejson);
        BpmnModel bpmnModel = bpmnJsonConverter.convertToBpmnModel(modelNode);//得到画板上的BpmnModel
        Process process = bpmnModel.getProcesses().get(0);
        return bpmnModel;
    }

    public void saveProcessInstance(String processInstanceId, MultiValueMap<String, String> values) throws JsonProcessingException {
        BpmnModel newBpmnModel = getNewProcessInstanceBpmnModel(values);
        try{
            processEngine.getManagementService().executeCommand(new CustomSaveEditProcessInstanceCmd(processInstanceId,newBpmnModel));
//           JOptionPane.showMessageDialog(null, "流程实例保存成功！", "提示", JOptionPane.INFORMATION_MESSAGE);
        }catch(Exception e){
            LOGGER.error("Error saving processInstance {}",processInstanceId, e);
            throw new BadRequestException("ProcessInstance could not be saved " + processInstanceId);
        }
    }


    public void evolutionProcessDefinition(String processInstanceId, MultiValueMap<String, String> values) throws JsonProcessingException {
        BpmnModel newBpmnModel = getNewProcessInstanceBpmnModel(values);
        try{
            /**
             * 部署成最新版本-->得到最新版本流程定义【成功】
             */
            //生成bpmn自动布局
            if (newBpmnModel.getLocationMap().size() == 0) {
                BpmnAutoLayout bpmnLayout = new BpmnAutoLayout(newBpmnModel);
                bpmnLayout.execute();
            }
            //Bpmnmodel形式部署
            String resource3="new_bpmnmodel.bpmn";
            repositoryService.createDeployment().addBpmnModel(resource3,newBpmnModel).deploy();
            //end 结束部署成新版本
            //调服务库更新新版本
//            String html;
//            html = HttpClientUtil.get(HttpConfig.custom().url("http://10.61.4.24:30010/dp-pro/sys/plan/returnForRegister?planId=14"));
//            System.out.println(html);
        }catch(Exception e){
            LOGGER.error("Error saving processInstance {}",processInstanceId, e);
            throw new BadRequestException("ProcessInstance "+processInstanceId+" could not be evolved,please validate before evolution");
        }
    }
}
