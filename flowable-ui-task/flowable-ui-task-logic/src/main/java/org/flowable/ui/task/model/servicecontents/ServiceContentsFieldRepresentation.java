package org.flowable.ui.task.model.servicecontents;

public class ServiceContentsFieldRepresentation {
    private int fieldId;
    private String fieldName;
    private String fieldDescription;
    private int parentId;

    public int getFieldId() {
        return fieldId;
    }

    public void setFieldId(int fieldId) {
        this.fieldId = fieldId;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldDescription() {
        return fieldDescription;
    }

    public void setFieldDescription(String fieldDescription) {
        this.fieldDescription = fieldDescription;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    @Override
    public String toString() {
        return "ServiceContentsFieldRepresentation{" +
                "fieldId=" + fieldId +
                ", fieldName='" + fieldName + '\'' +
                ", fieldDescription='" + fieldDescription + '\'' +
                ", parentId=" + parentId +
                '}';
    }
}
