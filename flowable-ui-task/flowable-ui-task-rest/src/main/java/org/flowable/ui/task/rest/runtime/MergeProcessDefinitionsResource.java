package org.flowable.ui.task.rest.runtime;

import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.common.engine.impl.util.io.InputStreamSource;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.ProcessEngines;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.ui.common.service.exception.BadRequestException;
import org.flowable.ui.task.service.runtime.MergeProcessDefinitionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.Configuration;
import javax.servlet.http.HttpServletResponse;
import javax.swing.*;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/app")
@ResponseBody
public class MergeProcessDefinitionsResource {

    @Autowired
    protected MergeProcessDefinitionsService mergeProcessDefinitionsService;

    @GetMapping(value = "/rest/mergepro1")
    public void mergePro(@RequestParam(value="def1_id") String def1_id,@RequestParam(value="def2_id") String def2_id) throws IOException, DocumentException {
        if (def1_id == null || def2_id == null)
        {
            JOptionPane.showMessageDialog(null, "存在流程定义id为空，无法写回服务库", "提示", JOptionPane.ERROR_MESSAGE);
        }
        else {
            String merProResource = mergeProcessDefinitionsService.mergeProcessDenitions(def1_id, def2_id);
            if (merProResource != null) {
                mergeProcessDefinitionsService.DeployMergeProcessDefinitions(merProResource);
                JOptionPane.showMessageDialog(null, "流程写回服务库成功，请重新认领任务", "提示", JOptionPane.INFORMATION_MESSAGE);
            } else
                JOptionPane.showMessageDialog(null, "您未再次合并已有流程", "提示", JOptionPane.INFORMATION_MESSAGE);
        }

    }
    }


