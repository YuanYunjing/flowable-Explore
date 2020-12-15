package org.flowable.ui.task.validation;

import org.flowable.validation.ProcessValidator;
import org.flowable.validation.ProcessValidatorFactory;
import org.flowable.validation.ProcessValidatorImpl;

public class LocalProcessValidatorFactory extends ProcessValidatorFactory {

    @Override
    public ProcessValidator createDefaultProcessValidator() {
        ProcessValidatorImpl processValidator = new ProcessValidatorImpl();
        processValidator.addValidatorSet(new LocalValidatorSetFactory().createFlowableExecutableProcessValidatorSet());
        return processValidator;
    }
}
