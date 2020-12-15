package org.flowable.ui.task.service.runtime;

import org.flowable.editor.language.json.converter.util.CollectionUtils;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.TaskService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.task.api.Task;
import org.flowable.task.api.history.HistoricTaskInstance;
import org.flowable.ui.common.model.ResultListDataRepresentation;

import org.flowable.ui.task.model.runtime.ProcessDefinitionRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


@Service
@Transactional
public class ServiceSolutionContentsService {

    @Autowired
    TaskService taskService;

    @Autowired
    HistoryService historyService;

    @Autowired
    RepositoryService repositoryService;

    /**
     * 根据用户id获得该用户被分配的任务->流程
     * @param userId
     * @return  服务方案的List
     */
    public ResultListDataRepresentation getServiceSolutionContentsList(String userId){
        List<Task> runningTaskList = taskService.createTaskQuery().taskAssignee(userId).list();
        List<HistoricTaskInstance> historicTaskList = historyService.createHistoricTaskInstanceQuery().taskAssignee(userId).list();

        Set<String> processDefinitionIdSet = new HashSet<>();
        Set<String> processDefinitionNameSet = new HashSet<>();

        ArrayList<ProcessDefinition> processDefinitionList = new ArrayList();

        for(HistoricTaskInstance historicTask : historicTaskList) {
            processDefinitionIdSet.add(historicTask.getProcessDefinitionId());
        }

        for(Task runningTask : runningTaskList) {
            processDefinitionIdSet.add(runningTask.getProcessDefinitionId());
        }

        for(String processDefinitionId : processDefinitionIdSet) {  // 通过id获得流程定义，再获得该流程定义的名字
            ProcessDefinition processDefinitionGetById = repositoryService.getProcessDefinition(processDefinitionId);
            processDefinitionNameSet.add(processDefinitionGetById.getName());
        }

        for(String processDefinitionName : processDefinitionNameSet) {  // 通过流程名称获得流程定义

            List<ProcessDefinition> processDefinitions = repositoryService.createProcessDefinitionQuery().processDefinitionName(processDefinitionName).list();
            processDefinitionList.addAll(processDefinitions);
        }

        ResultListDataRepresentation result = new ResultListDataRepresentation(convertDefinitionList(processDefinitionList));

        return result;
    }


    protected List<ProcessDefinitionRepresentation> convertDefinitionList(List<ProcessDefinition> definitions) {
        List<ProcessDefinitionRepresentation> result = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(definitions)) {
            for (ProcessDefinition processDefinition : definitions) {
                ProcessDefinitionRepresentation rep = new ProcessDefinitionRepresentation(processDefinition);
                result.add(rep);
            }
        }
        return result;
    }
}
