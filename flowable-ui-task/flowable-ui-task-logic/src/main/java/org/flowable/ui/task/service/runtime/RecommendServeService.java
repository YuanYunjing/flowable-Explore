package org.flowable.ui.task.service.runtime;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.flowable.app.api.AppRepositoryService;
import org.flowable.editor.language.json.converter.util.CollectionUtils;
import org.flowable.engine.ProcessEngineConfiguration;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.NativeProcessDefinitionQuery;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.form.api.FormRepositoryService;
import org.flowable.ui.common.model.ResultListDataRepresentation;
import org.flowable.ui.common.security.SecurityUtils;
import org.flowable.ui.task.model.runtime.ProcessDefinitionRepresentation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class RecommendServeService {


    private static final Logger LOGGER = LoggerFactory.getLogger(FlowableProcessDefinitionService.class);

//    @Autowired
//    public RecommendRepositoryService recommendRepositoryService;

    @Autowired
    protected RepositoryService repositoryService;

    @Autowired
    protected ProcessEngineConfiguration processEngineConfiguration;

    @Autowired
    protected AppRepositoryService appRepositoryService;

    @Autowired
    protected FormRepositoryService formRepositoryService;

    @Autowired
    protected PermissionService permissionService;

    @Autowired
    protected ObjectMapper objectMapper;


    public ResultListDataRepresentation getRmdProcessDefinitions(String actId){
        // native的方法只有Flowable定义的对象可以用？
        NativeProcessDefinitionQuery nativeProcessDefinitionQuery = repositoryService.createNativeProcessDefinitionQuery();
        List<ProcessDefinition> definitions = nativeProcessDefinitionQuery.sql("SELECT act_re_procdef.* " +
                "FROM act_re_procdef,act_proc " +
                "WHERE act_proc.ACT_ID_ = #{actId} AND act_re_procdef.ID_ = act_proc.PROC_DEF_ID_ " +
                "ORDER BY relevance " +
                "DESC")
                .parameter("actId", actId).list();

        List<ProcessDefinition> startableDefinitions = new ArrayList<>();
        for (ProcessDefinition definition : definitions) {
            if (SecurityUtils.getCurrentUserObject() == null ||
                    permissionService.canStartProcess(SecurityUtils.getCurrentUserObject(), definition)) {
                startableDefinitions.add(definition);
            }
        }

        ResultListDataRepresentation result = new ResultListDataRepresentation(convertDefinitionList(startableDefinitions));
        return result;
    }

    public ResultListDataRepresentation getRmdServes(String actId){
//        NativeRecommendServeQuery nativeRecommendServeQuery = recommendRepositoryService.createNativeRecommendServiceQuery();
//
//        List<RecommendServe> recommendServes = nativeRecommendServeQuery.sql("SELECT * " +
//                "FROM act_proc " +
//                "WHERE act_proc.ACT_ID_ = #{actId} " +
//                "ORDER BY relevance " +
//                "DESC")
//                .parameter("actId", actId).list();
//
//        ResultListDataRepresentation result = new ResultListDataRepresentation(convertRecommendList(recommendServes));
        return null;
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

//    protected List<RecommendServeRepresentation> convertRecommendList(List<RecommendServe> recommendServes) {
//        List<RecommendServeRepresentation> result = new ArrayList<>();
//        if (CollectionUtils.isNotEmpty(recommendServes)) {
//            for (RecommendServe recommendServe : recommendServes) {
//                RecommendServeRepresentation rep = new RecommendServeRepresentation(recommendServe);
//                result.add(rep);
//            }
//        }
//        return result;
//    }

}
