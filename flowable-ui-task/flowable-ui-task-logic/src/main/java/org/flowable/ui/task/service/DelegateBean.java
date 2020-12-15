package org.flowable.ui.task.service;

import org.flowable.engine.delegate.TaskListener;
import org.flowable.task.service.delegate.DelegateTask;

import java.io.Serializable;

public class DelegateBean implements TaskListener, Serializable {
    @Override
    public void notify(DelegateTask delegateTask) {
        System.out.println("使用DelegateBean");
    }
}
