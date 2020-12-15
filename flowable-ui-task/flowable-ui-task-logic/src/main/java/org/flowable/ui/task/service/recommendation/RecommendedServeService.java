package org.flowable.ui.task.service.recommendation;

import org.flowable.ui.common.model.ResultListDataRepresentation;
import org.flowable.ui.task.model.recommendation.RecommendedServeRepresentation;


public interface RecommendedServeService {

    ResultListDataRepresentation getRecommendedServes(String actName);
    RecommendedServeRepresentation getRMDServeByServiceName(String serviceName);

}
