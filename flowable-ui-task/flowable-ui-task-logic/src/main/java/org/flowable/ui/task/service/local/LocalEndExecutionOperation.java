package org.flowable.ui.task.service.local;

import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.flowable.cmmn.engine.impl.task.TaskHelper;
import org.flowable.common.engine.api.FlowableException;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.common.engine.impl.logging.LoggingSessionConstants;
import org.flowable.common.engine.impl.util.CollectionUtil;
import org.flowable.engine.FlowableEngineAgenda;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.delegate.event.impl.FlowableEventBuilder;
import org.flowable.engine.impl.agenda.EndExecutionOperation;
import org.flowable.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.flowable.engine.impl.delegate.SubProcessActivityBehavior;
import org.flowable.engine.impl.dynamic.DynamicUserTaskBuilder;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityManager;
import org.flowable.engine.impl.runtime.callback.ProcessInstanceState;
import org.flowable.engine.impl.util.BpmnLoggingSessionUtil;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.engine.impl.util.ProcessDefinitionUtil;
import org.flowable.engine.impl.util.ProcessInstanceHelper;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.flowable.ui.task.service.runtime.CustomInjectUserTaskInProcessInstanceCmd;


import java.util.*;

import static org.flowable.engine.impl.util.CommandContextUtil.*;

public class LocalEndExecutionOperation extends EndExecutionOperation {

    public LocalEndExecutionOperation(CommandContext commandContext, ExecutionEntity execution) {
        super(commandContext, execution);
    }

    @Override
    public void run() {
        if (execution.isProcessInstanceType()) {
            handleProcessInstanceExecution(execution);
        } else {
            handleRegularExecution();
        }
    }

    @Override
    protected void handleRegularExecution() {

        /**
         * 添加判断，如果最后一个节点是结束节点，执行结束代码，源码里没有这一项判断
         *
         * if (execution.getCurrentFlowElement() instanceof EndEvent) {
         *
         */
        if (execution.getCurrentFlowElement() instanceof EndEvent) {
            ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager(commandContext);

            // There will be a parent execution (or else we would be in the process instance handling method)
            ExecutionEntity parentExecution = executionEntityManager.findById(execution.getParentId());

            // If the execution is a scope, all the child executions must be deleted first.
            if (execution.isScope()) {
                executionEntityManager.deleteChildExecutions(execution, null, false);
            }

            // Delete current execution
            executionEntityManager.deleteExecutionAndRelatedData(execution, null, false);  // 没有这一句会不标蓝，但其实已完成，有，则标蓝


            // When ending an execution in a multi instance subprocess , special care is needed
            if (isEndEventInMultiInstanceSubprocess(execution)) {
                handleMultiInstanceSubProcess(executionEntityManager, parentExecution);
                return;
            }

            SubProcess subProcess = execution.getCurrentFlowElement().getSubProcess();

            if (subProcess instanceof EventSubProcess) {
                EventSubProcess eventSubProcess = (EventSubProcess) subProcess;

                boolean hasNonInterruptingStartEvent = false;
                for (FlowElement eventSubElement : eventSubProcess.getFlowElements()) {
                    if (eventSubElement instanceof StartEvent) {
                        StartEvent subStartEvent = (StartEvent) eventSubElement;
                        if (!subStartEvent.isInterrupting()) {
                            hasNonInterruptingStartEvent = true;
                            break;
                        }
                    }
                }

                if (hasNonInterruptingStartEvent) {
                    executionEntityManager.deleteChildExecutions(parentExecution, null, false);
                    executionEntityManager.deleteExecutionAndRelatedData(parentExecution, null, false);

                    CommandContextUtil.getEventDispatcher(commandContext).dispatchEvent(
                            FlowableEventBuilder.createActivityEvent(FlowableEngineEventType.ACTIVITY_COMPLETED, subProcess.getId(), subProcess.getName(),
                                    parentExecution.getId(), parentExecution.getProcessInstanceId(), parentExecution.getProcessDefinitionId(), subProcess));

                    ExecutionEntity subProcessParentExecution = parentExecution.getParent();
                    if (getNumberOfActiveChildExecutionsForExecution(executionEntityManager, subProcessParentExecution.getId()) == 0) {
                        if (subProcessParentExecution.getCurrentFlowElement() instanceof SubProcess) {
                            SubProcess parentSubProcess = (SubProcess) subProcessParentExecution.getCurrentFlowElement();
                            if (parentSubProcess.getOutgoingFlows().size() > 0) {
                                ExecutionEntity executionToContinue = handleSubProcessEnd(executionEntityManager, subProcessParentExecution, parentSubProcess);
                                agenda.planTakeOutgoingSequenceFlowsOperation(executionToContinue, true);
                                return;
                            }

                        }

                        agenda.planEndExecutionOperation(subProcessParentExecution);
                    }

                    return;
                }
            }

            // If there are no more active child executions, the process can be continued
            // If not (eg an embedded subprocess still has active elements, we cannot continue)
            List<ExecutionEntity> eventScopeExecutions = getEventScopeExecutions(executionEntityManager, parentExecution);

            // Event scoped executions need to be deleted when there are no active siblings anymore,
            // unless instances of the event subprocess itself. If there are no active siblings anymore,
            // the current scope had ended and the event subprocess start event should stop listening to any trigger.
            if (!eventScopeExecutions.isEmpty()) {
                List<? extends ExecutionEntity> childExecutions = parentExecution.getExecutions();
                boolean activeSiblings = false;
                for (ExecutionEntity childExecutionEntity : childExecutions) {
                    if (!isInEventSubProcess(childExecutionEntity) && childExecutionEntity.isActive() && !childExecutionEntity.isEnded()) {
                        activeSiblings = true;
                    }
                }

                if (!activeSiblings) {
                    for (ExecutionEntity eventScopeExecution : eventScopeExecutions) {
                        executionEntityManager.deleteExecutionAndRelatedData(eventScopeExecution, null, false);
                    }
                }
            }

            if (getNumberOfActiveChildExecutionsForExecution(executionEntityManager, parentExecution.getId()) == 0) {

                ExecutionEntity executionToContinue = null;

                if (subProcess != null) {

                    // In case of ending a subprocess: go up in the scopes and continue via the parent scope
                    // unless its a compensation, then we don't need to do anything and can just end it

                    if (subProcess.isForCompensation()) {
                        agenda.planEndExecutionOperation(parentExecution);
                    } else {
                        executionToContinue = handleSubProcessEnd(executionEntityManager, parentExecution, subProcess);
                    }

                } else {

                    // In the 'regular' case (not being in a subprocess), we use the parent execution to
                    // continue process instance execution

                    executionToContinue = handleRegularExecutionEnd(executionEntityManager, parentExecution);
                }

                if (executionToContinue != null) {
                    // only continue with outgoing sequence flows if the execution is
                    // not the process instance root execution (otherwise the process instance is finished)
                    if (executionToContinue.isProcessInstanceType()) {
                        handleProcessInstanceExecution(executionToContinue);

                    } else {
                        agenda.planTakeOutgoingSequenceFlowsOperation(executionToContinue, true);
                    }
                }

            }

        } else {
            ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager(commandContext);


            // If the execution is a scope, all the child executions must be deleted first.
            if (execution.isScope()) {
                executionEntityManager.deleteChildExecutions(execution, null, false);
            }

            // Delete current execution
            executionEntityManager.deleteExecutionAndRelatedData(execution, null, false);  // 没有这一句会不标蓝，但其实已完成，有，则标蓝

            ProcessInstance processInstance = getProcessEngineConfiguration().getRuntimeService().
                    createProcessInstanceQuery().processInstanceId(execution.getProcessInstanceId()).singleResult();
            if (processInstance == null) {
                System.out.println("流程已结束");
            } else {
                System.out.println("流程正在执行");
            }
        }
        /**
         * 如果最后一个节点不是结束节点，则执行以下代码
         * 之前思路：一是流程挂起，下次再激活，存在问题：没有当前节点，下次无法执行
         * 二是代码自动添加节点，存在问题如下面代码中注释所介绍
         *

         } else {

         ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager(commandContext);

         // If the execution is a scope, all the child executions must be deleted first.
         if (execution.isScope()) {
         executionEntityManager.deleteChildExecutions(execution, null, false);
         }

         //            //流程挂起方案
         //            ProcessEngineConfigurationImpl processEngineConfiguration = LocalCommandContextUtil.getProcessEngineConfiguration(commandContext);
         //            processEngineConfiguration.getRuntimeService().suspendProcessInstanceById(execution.getProcessInstanceId());


         // 使用代码为最后一个非结束节点后，自动添加一个临时节点，作为“当前节点”，
         // 但是此前测试结果是最后一个节点与该当前节点，在流程实例图中，均没有边框颜色显示，
         // 后由于该部分可交由用户手动添加、删除，因此没有做进一步调整使用
         ProcessEngineConfigurationImpl processEngineConfiguration = LocalCommandContextUtil.getProcessEngineConfiguration(commandContext);

         String dynamicUserId = "UserTaskAdd"+ UUID.randomUUID().toString().replaceAll("-","");
         DynamicUserTaskBuilder dynamicUserTaskBuilder = new DynamicUserTaskBuilder();
         dynamicUserTaskBuilder.setId(dynamicUserId);
         dynamicUserTaskBuilder.setName("临时节点");
         dynamicUserTaskBuilder.setAssignee("632511");

         processEngineConfiguration.getManagementService().executeCommand(new CustomInjectUserTaskInProcessInstanceCmd(execution.getProcessInstanceId(), dynamicUserTaskBuilder, execution.getCurrentFlowElement()));

         agenda.planTakeOutgoingSequenceFlowsOperation(execution, true);  // 加这句话后 会提示custom中null?


         }
         */

    }

}
