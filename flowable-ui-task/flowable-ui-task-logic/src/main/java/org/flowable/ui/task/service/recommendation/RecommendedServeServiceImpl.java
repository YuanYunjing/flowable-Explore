package org.flowable.ui.task.service.recommendation;

import org.flowable.ui.common.model.ResultListDataRepresentation;
import org.flowable.ui.task.model.recommendation.RecommendedServe;
import org.flowable.ui.task.model.recommendation.RecommendedServeRepresentation;
import org.flowable.ui.task.repository.RecommendedServeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import javax.annotation.Resource;
import java.util.List;

@Service
@Transactional
public class RecommendedServeServiceImpl implements RecommendedServeService {

    @Resource
    private RecommendedServeRepository recommendedServeRepository;

    public ResultListDataRepresentation getRecommendedServes(String actName){

        List<RecommendedServe> recommendedServes = recommendedServeRepository.findByActIdOrderByRelevanceDesc(actName);

        ResultListDataRepresentation result = new ResultListDataRepresentation(recommendedServes);
        return result;

    }

    @Override
    public RecommendedServeRepresentation getRMDServeByServiceName(String serviceName) {

        RecommendedServe recommendedServe = recommendedServeRepository.findByServiceName(serviceName);

        RecommendedServeRepresentation result = new RecommendedServeRepresentation(recommendedServe.getServiceName(), recommendedServe.getAccessPoint());
        return result;

    }
}
