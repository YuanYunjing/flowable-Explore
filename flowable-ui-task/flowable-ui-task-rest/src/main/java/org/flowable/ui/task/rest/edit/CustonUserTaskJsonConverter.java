package org.flowable.ui.task.rest.edit;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.lang3.StringUtils;
import org.flowable.bpmn.model.*;
import org.flowable.editor.constants.StencilConstants;
import org.flowable.editor.language.json.converter.ActivityProcessor;
import org.flowable.editor.language.json.converter.BaseBpmnJsonConverter;
import org.flowable.editor.language.json.converter.UserTaskJsonConverter;
import org.flowable.editor.language.json.converter.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class CustonUserTaskJsonConverter extends UserTaskJsonConverter {


    public static void fillTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,
                                 Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        fillJsonTypes(convertersToBpmnMap);
        fillBpmnTypes(convertersToJsonMap);
    }

    public static void setCustomTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,
                                      Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        removeTypes(convertersToBpmnMap,convertersToJsonMap);
        fillTypes(convertersToBpmnMap,convertersToJsonMap);
    }

    public static void removeTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap,
                                   Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        convertersToJsonMap.remove(UserTask.class);
        convertersToBpmnMap.remove(StencilConstants.STENCIL_TASK_USER);
    }

    public static void fillJsonTypes(Map<String, Class<? extends BaseBpmnJsonConverter>> convertersToBpmnMap) {
        convertersToBpmnMap.put(STENCIL_TASK_USER, CustonUserTaskJsonConverter.class);
    }

    public static void fillBpmnTypes(
            Map<Class<? extends BaseElement>, Class<? extends BaseBpmnJsonConverter>> convertersToJsonMap) {
        convertersToJsonMap.put(UserTask.class, CustonUserTaskJsonConverter.class);
    }

    @Override
    public void convertToJson(BaseElement baseElement, ActivityProcessor processor, BpmnModel model, FlowElementsContainer container, ArrayNode shapesArrayNode, double subProcessX, double subProcessY){
        super.convertToJson(baseElement, processor, model, container, shapesArrayNode, subProcessX, subProcessY);
    }

    @Override
    protected FlowElement convertJsonToElement(JsonNode elementNode, JsonNode modelNode,
                                               Map<String, JsonNode> shapeMap) {
        UserTask flowElement = (UserTask) super.convertJsonToElement(elementNode, modelNode, shapeMap);
        List<CustomProperty> customProperties = new ArrayList<>();
        // 扩展 节点类型
        String nodetype = getPropertyValueAsString("nodetype", elementNode);
        if (StringUtils.isNotBlank(nodetype)) {
            CustomProperty nodeType = this.createProperty("nodetype", nodetype);
            customProperties.add(nodeType);
        }
        if (CollectionUtils.isNotEmpty(customProperties)) {
            flowElement.setCustomProperties(customProperties);
        }
        return flowElement;
    }

    /**
     * 创建自定义属性
     *
     * @param propertyName  属性名称
     * @param propertyValue 属性值
     */
    private CustomProperty createProperty(String propertyName, String propertyValue) {
        CustomProperty customProperty = new CustomProperty();
        customProperty.setId(propertyName);
        customProperty.setName(propertyName);
        customProperty.setSimpleValue(propertyValue);
        return customProperty;
    }
}

