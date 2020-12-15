package org.flowable.ui.task.rest.edit;

import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.runtime.ActivityInstanceQuery;
import org.flowable.task.api.Task;
import org.flowable.ui.common.model.ResultListDataRepresentation;
import org.flowable.ui.task.service.StateDisplay.StateDisplayService;
import org.json.JSONArray;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/app")
public class EditStateDisplayResource {
    @Resource
    protected StateDisplayService stateDisplayService;

    @GetMapping(value = "/rest/completedStateDisplay")
    public ResultListDataRepresentation GetProcessInstanceFinishedState(@RequestParam(value = "processInstanceId", required = false) String processInstanceId){
         return stateDisplayService.getFinshedTaskDefnitionId(processInstanceId);
    }
    @GetMapping(value = "/rest/currentStateDisplay")
    public ResultListDataRepresentation GetProcessInstanceCurrentState(@RequestParam(value = "processInstanceId", required = false) String processInstanceId){
        return stateDisplayService.getCurrentTaskDefinitionId(processInstanceId);
    }




}
