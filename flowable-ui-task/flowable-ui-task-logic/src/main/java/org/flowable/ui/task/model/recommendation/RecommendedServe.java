package org.flowable.ui.task.model.recommendation;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name = "act_proc")
public class RecommendedServe {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    @Column(name = "ID_", unique = true, nullable = false)
    private Integer id;

    @Column(name = "SERVICE_ID_", unique = true)
    private String serviceID;

    @Column(name = "SERVICE_NAME_", unique = true)
    private String serviceName;

    @Column(name = "ACCESS_POINT_", unique = true)
    private String accessPoint;
    @Column(name = "relevance", unique = true)
    private Integer relevance;

    public Integer getRelevance() {
        return relevance;
    }

    public void setRelevance(Integer relevance) {
        this.relevance = relevance;
    }

    //    @Column(name = "ACT_ID_")
//    private String actId;
//
//    @Column(name = "IS_PROCESS_")
//    private Integer isProcess;


    public RecommendedServe(String serviceName, String accessPoint) {
        this.serviceName = serviceName;
        this.accessPoint = accessPoint;
//        this.actId = actId;
//        this.isProcess = isProcess;
    }

    public RecommendedServe() {

    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
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

    public String getServiceID() {
        return serviceID;
    }

    public void setServiceID(String serviceID) {
        this.serviceID = serviceID;
    }

    //    public String getActId() {
//        return actId;
//    }
//
//    public void setActId(String actId) {
//        this.actId = actId;
//    }
//
//    public Integer getIsProcess() {
//        return isProcess;
//    }
//
//    public void setIsProcess(Integer isProcess) {
//        this.isProcess = isProcess;
//    }
}
