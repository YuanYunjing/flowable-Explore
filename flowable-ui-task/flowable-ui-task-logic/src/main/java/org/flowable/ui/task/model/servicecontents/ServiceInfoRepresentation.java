package org.flowable.ui.task.model.servicecontents;

import java.util.ArrayList;

public class ServiceInfoRepresentation {
    private String serviceId;
    private String serviceName;
    private String serviceDescription;
    private String accessPoint;
    private String name;
    private String interfaceType;
    private ArrayList<InputParamEntity> inputParamEntityList;
    private ArrayList<OutputParamEntity> outputParamEntityList;

    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getServiceDescription() {
        return serviceDescription;
    }

    public void setServiceDescription(String serviceDescription) {
        this.serviceDescription = serviceDescription;
    }

    public String getAccessPoint() {
        return accessPoint;
    }

    public void setAccessPoint(String accessPoint) {
        this.accessPoint = accessPoint;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInterfaceType() {
        return interfaceType;
    }

    public void setInterfaceType(String interfaceType) {
        this.interfaceType = interfaceType;
    }

    public ArrayList<InputParamEntity> getInputParamEntityList() {
        return inputParamEntityList;
    }

    public void setInputParamEntityList(ArrayList<InputParamEntity> inputParamEntityList) {
        this.inputParamEntityList = inputParamEntityList;
    }

    public ArrayList<OutputParamEntity> getOutputParamEntityList() {
        return outputParamEntityList;
    }

    public void setOutputParamEntityList(ArrayList<OutputParamEntity> outputParamEntityList) {
        this.outputParamEntityList = outputParamEntityList;
    }

    @Override
    public String toString() {
        return "ServiceInfoRepresentation{" +
                "serviceId='" + serviceId + '\'' +
                ", serviceName='" + serviceName + '\'' +
                ", serviceDescription='" + serviceDescription + '\'' +
                ", accessPoint='" + accessPoint + '\'' +
                ", name='" + name + '\'' +
                ", interfaceType='" + interfaceType + '\'' +
                '}';
    }
}
