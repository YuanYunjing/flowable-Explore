package org.flowable.ui.task.rest.edit;

import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service(value = "testHandler")
public class testHandler implements TaskListener {
    private  static final org.slf4j.Logger log= LoggerFactory.getLogger(testHandler.class);
    @Override
    public void notify(DelegateTask delegateTask) {
        System.out.println("testHandler notify"+Thread.currentThread());
        log.info("delegateTask" + delegateTask.getCandidates());
        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> forObject = restTemplate.getForEntity("http://39.96.139.30:8082/getID", String.class);
        log.info("返回的字符串" + forObject);

    }
}
