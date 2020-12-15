package org.flowable.ui.task.rest.edit;
import org.flowable.cmmn.api.CmmnHistoryService;
import org.flowable.engine.HistoryService;
import org.flowable.ui.task.service.runtime.FlowableProcessInstanceService;
import org.flowable.variable.api.history.HistoricVariableInstance;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.flowable.engine.RuntimeService;
import javax.servlet.http.HttpServletResponse;
import java.util.List;

/**
 * @author 陈高建 云计算
 * @version 1.0
 * @date 2020/11/4 0004 21:18
 * 通过流程变量key和流程实例获取流程变量的值
 */

//获取对应的流程变量进行返回
@RestController
@RequestMapping("/app")
public class getprocecssVarible {
    @Autowired private  RuntimeService runtimeService;
    @Autowired private HistoryService historyService;
    @Autowired
    protected FlowableProcessInstanceService processInstanceService;

    @GetMapping(value = "/rest/GetprocessVarible/{processInstanceId}/{key}")
        public String  getPCVarible(@PathVariable String processInstanceId, @PathVariable String key,HttpServletResponse response) {
        Object variable=null;

        List<HistoricVariableInstance> list = historyService.createHistoricVariableInstanceQuery().processInstanceId(processInstanceId).list();
        for (int i = 0; i < list.size(); i++) {
            HistoricVariableInstance historicVariableInstance = list.get(i);
            if (historicVariableInstance.getVariableName().equals(key)) {
                variable = historicVariableInstance.getValue();
            }
        }
        if(variable==null){
            variable = runtimeService.getVariable(processInstanceId, key);
        }
 return variable.toString();
    }
}