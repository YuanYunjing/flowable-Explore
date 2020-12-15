package org.flowable.ui.task.service.local;

import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.context.annotation.Configuration;

// 取代xml的注入方式
@Configuration
public class LocalProcessEngineConfiguration implements ProcessEngineConfigurationConfigurer {

    @Override
    public void configure(SpringProcessEngineConfiguration springProcessEngineConfiguration) {

        springProcessEngineConfiguration.setAgendaFactory(new LocalDefaultFlowableEngineAgendaFactory());

//        springProcessEngineConfiguration.setActivityBehaviorFactory(new LocalActivityBehaviorFactory());
    }

}
