package org.flowable.ui.task.service.local;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.api.delegate.event.FlowableEventDispatcher;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.common.engine.impl.logging.LoggingSessionConstants;
import org.flowable.engine.delegate.TaskListener;
import org.flowable.engine.delegate.event.impl.FlowableEventBuilder;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.util.*;
import org.flowable.task.api.DelegationState;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;



public class LocalTaskHelper extends TaskHelper {

    public static void completeTask(TaskEntity taskEntity, Map<String, Object> variables,
                                    Map<String, Object> transientVariables, boolean localScope, CommandContext commandContext) {

        // Task complete logic

        if (taskEntity.getDelegationState() != null && taskEntity.getDelegationState() == DelegationState.PENDING) {
            throw new FlowableException("A delegated task cannot be completed, but should be resolved instead.");
        }

        if (variables != null) {
            if (localScope) {
                taskEntity.setVariablesLocal(variables);

            } else if (taskEntity.getExecutionId() != null) {
                ExecutionEntity execution = CommandContextUtil.getExecutionEntityManager().findById(taskEntity.getExecutionId());  // 获得execution的方法
                if (execution != null) {
                    execution.setVariables(variables);
                }

            } else {
                taskEntity.setVariables(variables);
            }
        }

        if (transientVariables != null) {
            if (localScope) {
                taskEntity.setTransientVariablesLocal(transientVariables);
            } else {
                taskEntity.setTransientVariables(transientVariables);
            }
        }

        // 执行节点监听事件
        ProcessEngineConfigurationImpl processEngineConfiguration = CommandContextUtil.getProcessEngineConfiguration(commandContext);
        processEngineConfiguration.getListenerNotificationHelper().executeTaskListeners(taskEntity, TaskListener.EVENTNAME_COMPLETE);  // 完成监听：完成时触发？


        if (processEngineConfiguration.getIdentityLinkInterceptor() != null) {
            processEngineConfiguration.getIdentityLinkInterceptor().handleCompleteTask(taskEntity);
        }

        logUserTaskCompleted(taskEntity);

        //
        FlowableEventDispatcher eventDispatcher = CommandContextUtil.getProcessEngineConfiguration(commandContext).getEventDispatcher();
        if (eventDispatcher != null && eventDispatcher.isEnabled()) {
            if (variables != null) {
                eventDispatcher.dispatchEvent(FlowableEventBuilder.createEntityWithVariablesEvent(
                        FlowableEngineEventType.TASK_COMPLETED, taskEntity, variables, localScope));
            } else { //
                eventDispatcher.dispatchEvent(
                        FlowableEventBuilder.createEntityEvent(FlowableEngineEventType.TASK_COMPLETED, taskEntity));
            }
        }

        if (processEngineConfiguration.isLoggingSessionEnabled() && taskEntity.getExecutionId() != null) {
            String taskLabel = null;
            if (StringUtils.isNotEmpty(taskEntity.getName())) {
                taskLabel = taskEntity.getName();
            } else {
                taskLabel = taskEntity.getId();
            }

            ExecutionEntity execution = CommandContextUtil.getExecutionEntityManager().findById(taskEntity.getExecutionId());
            if (execution != null) {
                BpmnLoggingSessionUtil.addLoggingData(LoggingSessionConstants.TYPE_USER_TASK_COMPLETE,
                        "User task '" + taskLabel + "' completed", taskEntity, execution);
            }
        }

        // 删除已有的任务相关数据（重点）
        deleteTask(taskEntity, null, false, true, true);

        // Continue process (if not a standalone task)
        // 激活下个步骤工作
        if (taskEntity.getExecutionId() != null) {  // 如果当前活动节点的id不为空，如果删去该判断及其内容，执行不会向前走
            ExecutionEntity executionEntity = CommandContextUtil.getExecutionEntityManager(commandContext).findById(taskEntity.getExecutionId());
            CommandContextUtil.getAgenda(commandContext).planTriggerExecutionOperation(executionEntity);  // 已解决（报错，提示没有为org.flowable.ui.task.service.singlerlun.SingleRunFlowableEngineAgenda配置会话工厂）

        }
    }


}
