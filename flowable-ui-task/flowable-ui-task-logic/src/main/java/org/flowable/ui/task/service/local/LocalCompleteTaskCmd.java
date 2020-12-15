package org.flowable.ui.task.service.local;

import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.compatibility.Flowable5CompatibilityHandler;
import org.flowable.engine.impl.cmd.NeedsActiveTaskCmd;
import org.flowable.engine.impl.util.Flowable5Util;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;

import java.util.Map;

public class LocalCompleteTaskCmd extends NeedsActiveTaskCmd<Void> {

    private static final long serialVersionUID = 1L;
    protected Map<String, Object> variables;
    protected Map<String, Object> transientVariables;
    protected boolean localScope;

    public LocalCompleteTaskCmd(String taskId, Map<String, Object> variables) {
        super(taskId);
        this.variables = variables;
    }

    @Override
    protected Void execute(CommandContext commandContext, TaskEntity task) {

        // Backwards compatibility
        if (task.getProcessDefinitionId() != null) {
            if (Flowable5Util.isFlowable5ProcessDefinitionId(commandContext, task.getProcessDefinitionId())) {
                Flowable5CompatibilityHandler compatibilityHandler = Flowable5Util.getFlowable5CompatibilityHandler();

                if (transientVariables == null) {
                    compatibilityHandler.completeTask(task, variables, localScope);
                } else {
                    compatibilityHandler.completeTask(task, variables, transientVariables);
                }
                return null;
            }
        }

        // 该方法控制“运行完成”逻辑
        LocalTaskHelper.completeTask(task, variables, transientVariables, localScope, commandContext);
        return null;
    }

    @Override
    protected String getSuspendedTaskException() {
        return "Cannot complete a suspended task";
    }

}
