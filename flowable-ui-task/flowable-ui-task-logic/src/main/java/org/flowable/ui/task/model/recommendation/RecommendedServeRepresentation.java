package org.flowable.ui.task.model.recommendation;


import org.flowable.ui.common.model.AbstractRepresentation;

public class RecommendedServeRepresentation  extends AbstractRepresentation {
    protected String serviceName;
    protected String accessPoint;

    public RecommendedServeRepresentation(String serviceName, String accessPoint) {
        this.serviceName = serviceName;
        this.accessPoint = accessPoint;
    }

    public RecommendedServeRepresentation() {
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getAccessPoint() {
        return accessPoint;
    }

    public void setAccessPoint(String accessPoint) {
        this.accessPoint = accessPoint;
    }
}
