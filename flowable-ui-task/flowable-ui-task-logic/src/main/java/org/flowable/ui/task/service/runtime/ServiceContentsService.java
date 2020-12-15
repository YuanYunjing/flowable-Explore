package org.flowable.ui.task.service.runtime;

import org.flowable.ui.common.model.ResultListDataRepresentation;
import org.flowable.ui.task.model.servicecontents.InputParamEntity;
import org.flowable.ui.task.model.servicecontents.OutputParamEntity;
import org.flowable.ui.task.model.servicecontents.ServiceContentsFieldRepresentation;
import org.flowable.ui.task.model.servicecontents.ServiceInfoRepresentation;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;


@Service
@Transactional
public class ServiceContentsService {

    public BackEndHttpRequest backEndHttpRequest = new BackEndHttpRequest();
    /**
     * 解析json获得领域树
     * @return  服务领域的List
     */
    public ResultListDataRepresentation getServiceContentsFields(){
        String json = "";
        json = backEndHttpRequest.sendGetForField("http://10.61.4.24:30010/dp-pro/sys/field/list","");
        JSONArray jsonArray = new JSONArray(json);
        ArrayList<ServiceContentsFieldRepresentation> serviceContentsFieldRepresentationArrayList = new ArrayList<ServiceContentsFieldRepresentation>();
        for (int i=0; i<jsonArray.length(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            ServiceContentsFieldRepresentation serviceContentsField = new ServiceContentsFieldRepresentation();
            serviceContentsField.setFieldId(object.getInt("fieldId"));
            serviceContentsField.setFieldName(object.getString("fieldName"));
            serviceContentsField.setFieldDescription(object.getString("fieldDescription"));
            serviceContentsField.setParentId(object.getInt("parentId"));
            serviceContentsFieldRepresentationArrayList.add(serviceContentsField);
        }

        ResultListDataRepresentation result = new ResultListDataRepresentation(serviceContentsFieldRepresentationArrayList);
        return result;
    }

    /**
     * 根据id获得服务目录
     * @param  id   接口参数
     * @param limitNum 限制获取服务个数
     * @return   服务
     */
    public ResultListDataRepresentation getServices(int id, int limitNum){

        String json = "";
        String param = "id=" + id + "&limitnum=" + limitNum;
        json = backEndHttpRequest.sendGetForField("http://10.61.4.24:30010/dp-pro/sys/service/getServiceInfoByFieldName", param);

        ArrayList<ServiceInfoRepresentation> serviceInfoRepresentationArrayList = new ArrayList<ServiceInfoRepresentation>();
        ResultListDataRepresentation result = null;
        try{
            JSONArray jsonArray = new JSONArray(json);
            int length = jsonArray.length();
            for (int i=0; i<length; i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                ServiceInfoRepresentation serviceInfo = new ServiceInfoRepresentation();
                serviceInfo.setServiceId(object.getString("serviceId"));
                serviceInfo.setServiceName(object.getString("serviceName"));
                serviceInfo.setServiceDescription(object.getString("serviceDescription"));
                serviceInfo.setAccessPoint(object.getString("accessPoint"));
                serviceInfo.setName(object.getString("name"));
                serviceInfo.setInterfaceType(object.getString("interfaceType"));
                serviceInfoRepresentationArrayList.add(serviceInfo);
            }

            result = new ResultListDataRepresentation(serviceInfoRepresentationArrayList);

        }catch (JSONException jsonException){
//            System.out.println("不存在服务列表！");
            result = new ResultListDataRepresentation(serviceInfoRepresentationArrayList);

        }finally {

            return result;
        }

    }

    public ServiceInfoRepresentation getServiceInfo(int id) {
        String json = "";
        String param = "id=" + id;
        json = backEndHttpRequest.sendGetForField("http://10.61.4.24:30010/dp-pro/sys/service/getServiceParamInfoById", param);

        ServiceInfoRepresentation serviceInfo = new ServiceInfoRepresentation();
        try{
            JSONArray jsonArray = new JSONArray(json);
            int length = jsonArray.length();
            for (int i=0; i<length; i++) {
                JSONObject object = jsonArray.getJSONObject(i);
                serviceInfo = new ServiceInfoRepresentation();
                serviceInfo.setAccessPoint(object.getString("accessPoint"));
//                JSONArray inputArray = object.getJSONArray("inputParamEntityList");
//                System.out.println(inputArray);
//                for (int j = 0; j < inputArray.length(); j++) {
//                    String parameterName = inputArray.getJSONObject(j).getString("parameterName");
////                    System.out.println("parameterName:" + parameterName);
//                    String xsdType = inputArray.getJSONObject(j).getString("xsdType");
////                    System.out.println("parameterName:" + parameterName);
//                }
                JSONArray json_inputs = new JSONArray(object.getString("inputParamEntityList"));
                ArrayList<InputParamEntity> inputs = new ArrayList();
                for (int j=0; j<json_inputs.length(); j++ ) {
                    JSONObject inputObject = json_inputs.getJSONObject(j);
                    InputParamEntity inputParamEntity = new InputParamEntity();
                    inputParamEntity.setParameterName(inputObject.getString("parameterName"));
                    inputParamEntity.setXsdType(inputObject.getString("xsdType"));
                    inputs.add(inputParamEntity);
                }
//                System.out.println(inputs);
                serviceInfo.setInputParamEntityList(inputs);
                JSONArray json_outputs = new JSONArray(object.getString("outputParamEntityList"));
                ArrayList<OutputParamEntity> outputs = new ArrayList();
                for (int j=0; j<json_outputs.length(); j++ ) {
                    JSONObject outputObject = json_outputs.getJSONObject(j);
                    OutputParamEntity outputParamEntity = new OutputParamEntity();
                    outputParamEntity.setParameterName(outputObject.getString("parameterName"));
                    outputParamEntity.setXsdType(outputObject.getString("xsdType"));
                    outputs.add(outputParamEntity);
                }
                serviceInfo.setOutputParamEntityList(outputs);
            }

        }catch (JSONException jsonException){
//            System.out.println("不存在服务列表！");

        }catch (NullPointerException nullPointerException) {

        }
        finally {
            return serviceInfo;
        }

    }
}
