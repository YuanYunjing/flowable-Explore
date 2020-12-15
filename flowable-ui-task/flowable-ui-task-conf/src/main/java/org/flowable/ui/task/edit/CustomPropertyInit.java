package org.flowable.ui.task.edit;

import org.flowable.bpmn.model.BaseElement;
import org.flowable.editor.language.json.converter.BaseBpmnJsonConverter;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.flowable.ui.task.rest.edit.CustonUserTaskJsonConverter;

import java.util.Map;

public class CustomPropertyInit extends BpmnJsonConverter {

    public CustomPropertyInit(){
        Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap = BpmnJsonConverter.convertersToJsonMap;
        Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap = BpmnJsonConverter.convertersToBpmnMap;
        //添加自定义的任务json转化器
        CustonUserTaskJsonConverter.setCustomTypes(convertersToBpmnMap, convertersToJsonMap);
    }

}

