package org.flowable.ui.task.service.local;

import org.flowable.bpmn.model.FlowElement;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.FlowableEngineAgenda;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityManager;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.engine.repository.ProcessDefinition;

public class LocalCurrentActivityCommand implements Command<Void> {
    protected String processInstanceId;
    protected FlowElement targetFlowElement;

    public LocalCurrentActivityCommand( String processInstanceId, FlowElement targetFlowElement) {
        this.processInstanceId = processInstanceId;
        this.targetFlowElement = targetFlowElement;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        ExecutionEntityManager executionEntityManager = CommandContextUtil.getExecutionEntityManager();

        // 已执行结束的执行对象
        ExecutionEntity executionEntity = executionEntityManager.findById(this.processInstanceId);

        // 新建子执行对象
        ExecutionEntity childExecutionEntity = executionEntityManager.createChildExecution(executionEntity);

        // 设置当前节点并继续执行
        childExecutionEntity.setCurrentFlowElement(this.targetFlowElement);
        FlowableEngineAgenda agenda = CommandContextUtil.getAgenda();
        agenda.planContinueProcessInCompensation(childExecutionEntity);

        return null;
    }
}
