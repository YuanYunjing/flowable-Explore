package org.flowable.ui.task.validation;

import org.flowable.bpmn.model.BpmnModel;
import org.flowable.bpmn.model.Process;
import org.flowable.bpmn.model.UserTask;
import org.flowable.ui.task.validation.model.Validation;
import org.flowable.ui.task.validation.repository.ValidationRepository;
import org.flowable.validation.ValidationError;
import org.flowable.validation.validator.Problems;
import org.flowable.validation.validator.ProcessLevelValidator;

import java.util.List;


public class AppendedTaskValidator extends ProcessLevelValidator {

    private ValidationRepository validationRepository = new ValidationRepository();

    @Override
    protected void executeValidation(BpmnModel bpmnModel, Process process, List<ValidationError> errors) {

        List<UserTask> userTasks = process.findFlowElementsOfType(UserTask.class);
        List<Validation> validations = validationRepository.getRules();
        for(Validation validation : validations){
            if(validation.getType() == 1) {  // 两个节点之间的约束
                verifyTogetherAct(process, userTasks, validation.getActAName(), validation.getActBName(), errors);
            } else {
                verifyKeyAct(process, userTasks, validation.getKeyActName(), errors);
            }
        }

    }

    // 验证关键活动
    protected void verifyKeyAct(Process process, List<UserTask> userTasks, String actName, List<ValidationError> errors) {
        UserTask targetUserTask = null;
        for (UserTask userTaskA : userTasks) {
            if (userTaskA.getName().equals(actName)) {
                targetUserTask = userTaskA;
            }
        }

        if ((targetUserTask == null)) {
            targetUserTask = new UserTask();
            targetUserTask.setName(actName);
            addError(errors, Problems.USER_TASK_LISTENER_IMPLEMENTATION_MISSING, process, targetUserTask, "当前流程必须包含'" + actName + "'任务");
        }

    }

    // 验证A与B必须同时出现
    protected void verifyTogetherAct(Process process, List<UserTask> userTasks, String actNameA, String actNameB, List<ValidationError> errors) {
        UserTask targetUserTaskA = null;
        UserTask targetUserTaskB = null;
        for (UserTask userTaskA : userTasks) {
            if (userTaskA.getName().equals(actNameA)) {
                targetUserTaskA = userTaskA;
            }
        }
        for (UserTask userTaskB : userTasks) {
            if (userTaskB.getName().equals(actNameB)) {
                targetUserTaskB = userTaskB;
            }
        }

        if ((targetUserTaskA != null && targetUserTaskB == null)) {
            addError(errors, Problems.USER_TASK_LISTENER_IMPLEMENTATION_MISSING, process, targetUserTaskA, "'" + actNameA + "'任务必须与'" + actNameB + "'任务同时出现");
        }
        if ((targetUserTaskA == null && targetUserTaskB != null)) {
            addError(errors, Problems.USER_TASK_LISTENER_IMPLEMENTATION_MISSING, process, targetUserTaskB, "'" + actNameB + "'任务必须与'" + actNameA + "'任务同时出现");
        }
    }
}
