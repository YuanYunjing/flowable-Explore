package org.flowable.ui.task.service.runtime;

import org.flowable.bpmn.model.*;
import org.flowable.bpmn.model.Process;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.FlowableEngineAgenda;
import org.flowable.engine.HistoryService;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.history.HistoricActivityInstance;
import org.flowable.engine.impl.cmd.AbstractDynamicInjectionCmd;
import org.flowable.engine.impl.persistence.entity.DeploymentEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityManager;
import org.flowable.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.identitylink.service.impl.persistence.entity.IdentityLinkEntity;
import org.flowable.job.service.impl.persistence.entity.DeadLetterJobEntity;
import org.flowable.job.service.impl.persistence.entity.JobEntity;
import org.flowable.job.service.impl.persistence.entity.SuspendedJobEntity;
import org.flowable.job.service.impl.persistence.entity.TimerJobEntity;
import org.flowable.task.service.impl.persistence.entity.HistoricTaskInstanceEntity;
import org.flowable.task.service.impl.persistence.entity.TaskEntity;
import org.flowable.ui.task.service.local.LocalCurrentActivityCommand;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

public class CustomSaveEditProcessInstanceCmd extends AbstractDynamicInjectionCmd implements Command<Void> {



    public  String processInstanceId;
    protected BpmnModel bpmnModel;

    public CustomSaveEditProcessInstanceCmd(String processInstanceId,BpmnModel bpmnModel) {
        this.processInstanceId = processInstanceId;
        this.bpmnModel=bpmnModel;
    }

    @Override
    public Void execute(CommandContext commandContext) {
        createDerivedProcessDefinitionForProcessInstance(commandContext,processInstanceId);
        return null;
    }

    @Override
    protected void updateBpmnProcess(CommandContext commandContext, Process process, BpmnModel bpmnModel, ProcessDefinitionEntity processDefinitionEntity, DeploymentEntity deploymentEntity) {

    }

    @Override
    protected void updateExecutions(CommandContext commandContext, ProcessDefinitionEntity processDefinitionEntity, ExecutionEntity processInstance, List<ExecutionEntity> list) {
    }

    @Override
    protected void createDerivedProcessDefinition(CommandContext commandContext, ProcessInstance processInstance) {
        ProcessDefinitionEntity originalProcessDefinitionEntity = (ProcessDefinitionEntity) CommandContextUtil.getProcessDefinitionEntityManager(commandContext).findById(processInstance.getProcessDefinitionId());
        DeploymentEntity deploymentEntity = this.createDerivedDeployment(commandContext, originalProcessDefinitionEntity);
//        BpmnModel bpmnModel = this.createBpmnModel(commandContext, originalProcessDefinitionEntity, deploymentEntity);
        this.storeBpmnModelAsByteArray(commandContext, bpmnModel, deploymentEntity, originalProcessDefinitionEntity.getResourceName());
        ProcessDefinitionEntity derivedProcessDefinitionEntity = this.deployDerivedDeploymentEntity(commandContext, deploymentEntity, originalProcessDefinitionEntity);
        this.updateExecutions(commandContext, derivedProcessDefinitionEntity, (ExecutionEntity)processInstance, bpmnModel);
    }

}
