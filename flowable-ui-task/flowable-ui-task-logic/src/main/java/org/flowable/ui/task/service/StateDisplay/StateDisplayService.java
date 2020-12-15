package org.flowable.ui.task.service.StateDisplay;

import org.flowable.engine.HistoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.runtime.ActivityInstanceQuery;
import org.flowable.task.api.Task;
import org.flowable.ui.common.model.ResultListDataRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class StateDisplayService {
    @Autowired private HistoryService historyService;

    @Autowired protected RuntimeService runtimeService;

    @Autowired protected TaskService taskService;

    public ResultListDataRepresentation getFinshedTaskDefnitionId(String processInstanceId){
//        processInstanceId="16e5a32d-063a-11eb-8804-6c4b9075233c";
        ActivityInstanceQuery processInstance = runtimeService.createActivityInstanceQuery().processInstanceId(processInstanceId);
        if(processInstance==null){
            return null;
        }
        List<HistoricActivityInstance> historyProcess = getHistoryProcess(processInstanceId);

//        ArrayList<Object> activiytIds = new ArrayList<>();
//        ArrayList<Object> flows = new ArrayList<>();
////        得到已经完成的活动id和sequenceFlow Id
//        for(HistoricActivityInstance historicActivityInstance:historyProcess){
//            String activityType=historicActivityInstance.getActivityType();
//            if(activityType.equals("sequenceFlow")||activityType.equals("exclusiveGateway")||activityType.equals("parallelGateway")){
//                flows.add(historicActivityInstance.getActivityId());
//            }else if(activityType.equals("userTask")||activityType.equals("HttpServiceTask")||activityType.equals("ServiceTask")||activityType.equals("startEvent")){
//                activiytIds.add(historicActivityInstance.getActivityId());
//            }
//        }
        //已完成的所有节点的id's
        ArrayList<Object> historyActivityIds = new ArrayList<>();


        for(HistoricActivityInstance historicActivityInstance:historyProcess){
            historyActivityIds.add(historicActivityInstance.getActivityId());
        }

        ResultListDataRepresentation result = new ResultListDataRepresentation(historyActivityIds);

        return result;



    }

    public List<HistoricActivityInstance> getHistoryProcess(String processInstanceId){
        List<HistoricActivityInstance> list = historyService.createHistoricActivityInstanceQuery().processInstanceId(processInstanceId).finished().list();
        return list;
    }

    public ResultListDataRepresentation getCurrentTaskDefinitionId(String processInstanceId){
//        processInstanceId="16e5a32d-063a-11eb-8804-6c4b9075233c";
        ActivityInstanceQuery processInstance = runtimeService.createActivityInstanceQuery().processInstanceId(processInstanceId);
        if(processInstance==null){
            return null;
        }
        ArrayList<Object> currentActivityId = new ArrayList<>();
        //当前活动节点ID
        List<Task> tasks = taskService.createTaskQuery().processInstanceId(processInstanceId).list();
        for(Task task:tasks){
            currentActivityId.add(task.getTaskDefinitionKey());
        }
        ResultListDataRepresentation result = new ResultListDataRepresentation(currentActivityId);
        return result;

    }

}
