package org.flowable.ui.task.validation.model;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

public class Validation {

    private Integer id;

    private Integer type;

    private String keyActName;

    private String actAName;

    private String actBName;

    public Validation(Integer id, Integer type, String keyActName, String actAName, String actBName) {
        this.id = id;
        this.type = type;
        this.keyActName = keyActName;
        this.actAName = actAName;
        this.actBName = actBName;
    }

    public Validation() {
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getKeyActName() {
        return keyActName;
    }

    public void setKeyActName(String keyActName) {
        this.keyActName = keyActName;
    }

    public String getActAName() {
        return actAName;
    }

    public void setActAName(String actAName) {
        this.actAName = actAName;
    }

    public String getActBName() {
        return actBName;
    }

    public void setActBName(String actBName) {
        this.actBName = actBName;
    }
}
