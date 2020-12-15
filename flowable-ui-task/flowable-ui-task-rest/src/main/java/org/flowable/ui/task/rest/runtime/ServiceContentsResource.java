package org.flowable.ui.task.rest.runtime;

import org.flowable.ui.common.model.ResultListDataRepresentation;
import org.flowable.ui.task.model.servicecontents.ServiceInfoRepresentation;
import org.flowable.ui.task.service.runtime.ServiceContentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/app")
public class ServiceContentsResource {
    @Autowired
    protected ServiceContentsService serviceContentsService;

    @GetMapping(value="/rest/service-contents")
    public ResultListDataRepresentation getServiceContentsFields() {
        return serviceContentsService.getServiceContentsFields();
    }

    @GetMapping(value="/rest/service-contents/services")
    public ResultListDataRepresentation getServices(@RequestParam(value = "id", required = false) int id,
                                                    @RequestParam(value = "limitnum", required = false) int limitnum) {
        return serviceContentsService.getServices(id, limitnum);
    }

    @GetMapping(value="/rest/service-contents/services/serviceInfo")
    public ServiceInfoRepresentation getServices(@RequestParam(value = "id", required = false) int id) {
        return serviceContentsService.getServiceInfo(id);
    }
}
