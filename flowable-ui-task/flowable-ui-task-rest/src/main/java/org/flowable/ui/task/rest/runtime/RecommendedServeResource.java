package org.flowable.ui.task.rest.runtime;

import org.flowable.ui.common.model.ResultListDataRepresentation;
import org.flowable.ui.task.model.recommendation.RecommendedServeRepresentation;
import org.flowable.ui.task.service.recommendation.RecommendedServeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/app")
public class RecommendedServeResource {

    @Resource
    protected RecommendedServeService recommendedServeService;


    @GetMapping(value = "/rest/rmd-serves")
    public ResultListDataRepresentation getRmdServes(@RequestParam(value = "actName", required = false) String actName) {
        return recommendedServeService.getRecommendedServes(actName);
    }

    @GetMapping(value = "/rest/rmd-serve")
    public RecommendedServeRepresentation getRMDServeByServiceName(@RequestParam(value = "serviceName", required = false) String serviceName) {
        return recommendedServeService.getRMDServeByServiceName(serviceName);
    }
}
