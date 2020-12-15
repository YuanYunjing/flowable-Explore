package org.flowable.ui.task.rest.runtime;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.UserTask;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.impl.dynamic.DynamicUserTaskBuilder;
import org.flowable.engine.repository.ModelQuery;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ActivityInstance;
import org.flowable.engine.runtime.ActivityInstanceQuery;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.task.api.history.HistoricTaskInstanceQuery;
import org.flowable.ui.task.service.runtime.CustomInjectUserTaskInProcessInstanceCmd;
import org.flowable.ui.task.service.runtime.InjectNodeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;

import javax.swing.*;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 *06 28任务:选定任务id后，添加推荐服务
 * author:li
 */
@RestController
@RequestMapping("/app")
public class InjectNode {

    @Autowired
    RepositoryService repositoryService;

    @Qualifier("processEngine")
    @Autowired
    ProcessEngine processEngine;

    @Autowired InjectNodeService injectNodeService;
    @GetMapping(value = "/rest/inject3")

    public void injectTest3(@RequestParam("currentTaskDefId") String currentTaskDefId, @RequestParam("currentProInsId") String currentProInsId, @RequestParam("recommendation_service_id") String recommendation_service_id) {

        if (currentTaskDefId == null) {
            JOptionPane.showMessageDialog(null, "您选择的任务为空", "提示", JOptionPane.ERROR_MESSAGE);
        } else if (currentProInsId==null){
            JOptionPane.showMessageDialog(null, "没有运行时的任务实例，请确认！", "提示", JOptionPane.ERROR_MESSAGE);
        }else if(recommendation_service_id == null) {
            JOptionPane.showMessageDialog(null, "推荐服务流程定义id不存在", "提示", JOptionPane.ERROR_MESSAGE);
        } else {
            injectNodeService.injectUserTask(currentTaskDefId,currentProInsId,recommendation_service_id);
            JOptionPane.showMessageDialog(null, "选择服务成功！", "提示", JOptionPane.INFORMATION_MESSAGE);
        }
    }
}




