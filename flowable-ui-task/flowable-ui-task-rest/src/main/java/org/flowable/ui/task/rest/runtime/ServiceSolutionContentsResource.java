package org.flowable.ui.task.rest.runtime;

import org.flowable.ui.common.model.ResultListDataRepresentation;
import org.flowable.ui.task.service.runtime.ServiceSolutionContentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/app")
public class ServiceSolutionContentsResource {
    @Autowired
    protected ServiceSolutionContentsService servicesolutionContentsService;

    @GetMapping(value="/rest/service-solution-contents")
    public ResultListDataRepresentation getServiceSolutionContents(@RequestParam(value = "userId", required = false) String userId) {
        return servicesolutionContentsService.getServiceSolutionContentsList(userId);
    }

}
