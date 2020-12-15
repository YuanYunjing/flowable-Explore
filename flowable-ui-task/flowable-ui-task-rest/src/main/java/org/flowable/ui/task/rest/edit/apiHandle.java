/**
 * @author 陈高建 云计算
 * @version 1.0
 * @date 2020/11/4 0004 19:18
 */
package org.flowable.ui.task.rest.edit;
import com.arronlong.httpclientutil.HttpClientUtil;
import com.arronlong.httpclientutil.common.HttpConfig;
import com.arronlong.httpclientutil.exception.HttpProcessException;
import org.apache.commons.lang3.ObjectUtils;
import org.flowable.common.engine.impl.el.FixedValue;
import org.flowable.engine.TaskService;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.task.service.delegate.DelegateTask;
import org.flowable.task.service.delegate.TaskListener;
import org.flowable.ui.task.model.servicecontents.ServiceContentsFieldRepresentation;
import org.flowable.ui.task.model.servicecontents.ServiceInfoRepresentation;
import org.flowable.ui.task.service.runtime.BackEndHttpRequest;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.groovy.GroovyBeanDefinitionReader;
import org.springframework.context.annotation.Scope;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.flowable.engine.RuntimeService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Scope
@Component(value = "apiHandler")
public class apiHandle implements TaskListener {

    @Autowired
    RuntimeService runtimeService;
    @Autowired
    TaskService taskService;
    public BackEndHttpRequest backEndHttpRequest = new BackEndHttpRequest();
    //获取对应的服务id
    private FixedValue service_name;
    //获取对应的keyword
    private FixedValue keyword= null;
    //获取对应的propertys
    private FixedValue output_var;
    //获取对应输出的变量名
    private FixedValue properties;

    private  static final org.slf4j.Logger log= LoggerFactory.getLogger(testHandler.class);

    @Override
    public void notify(DelegateTask delegateTask) {
        String processInstanceId = delegateTask.getProcessInstanceId();
        String task_id=delegateTask.getId();
        String Service_name = null, Output_var = null,Keyword = null;
        Service_name = service_name.getExpressionText();
        if(output_var!=null)
        Output_var = output_var.getExpressionText();
        if(keyword!=null)
        Keyword= keyword.getExpressionText();
        //执行回调
        this.callBack(processInstanceId, Service_name, Output_var,Keyword);
        //结束我们的服务,中间不进行停顿
        taskService.complete(task_id);
    }

    //回调函数
    public void callBack(String pocessInstanceId, String Service_name, String Output_var,String Keyword) {
        ServiceInfoRepresentation serviceInfo = new ServiceInfoRepresentation();
        String url = null;
        try {
                //通过对应的服务id找到对应的具体服务
                String json = "";
                String param = "id=" + Service_name;
                json = backEndHttpRequest.sendGetForField("http://39.96.139.30:8080/dp-pro/sys/service/getServiceParamInfoById", param);
                JSONArray jsonArray = new JSONArray(json);
                int length = jsonArray.length();
                for (int i=0; i<length; i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                serviceInfo = new ServiceInfoRepresentation();
                serviceInfo.setAccessPoint(object.getString("accessPoint"));
                }
                url=serviceInfo.getAccessPoint();
                //如果有keyword，需要进行拼接
                if(keyword!=null) {
                    //拼接url
                    int index = url.indexOf("{keyword}");
                    if(index != -1) {
                        StringBuilder sb = new StringBuilder();
                        sb.append(url.substring(0, index)).append(Keyword);
                        url = sb.toString();
                    }
                }
                //获取url对应的值
                String html = null;
                try {
                    html = HttpClientUtil.get(HttpConfig.custom().url(url));
                    System.out.println(html);

                } catch (HttpProcessException e) {
                    e.printStackTrace();

                }
            //List<Map<String, Object>> patent_HAS = restTemplate.getForObject(url, List.class);
               System.out.println(html);
               String process_name=Service_name;
               String value=html;
//                RestTemplate restTemplate = new RestTemplate();
//                List<Map<String, Object>> patent_HAS = restTemplate.getForObject(url, List.class);
                //System.out.println(patent_HAS);
               // String process_name=Service_name;
              //  String value=patent_HAS.toString();
            //设置流程变量进行保存
            runtimeService.setVariable(pocessInstanceId, process_name, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
