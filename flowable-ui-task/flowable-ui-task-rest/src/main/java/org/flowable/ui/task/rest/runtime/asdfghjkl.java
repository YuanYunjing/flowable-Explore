package org.flowable.ui.task.rest.runtime;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.Node;
import org.dom4j.XPath;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;
import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.converter.BaseBpmnXMLConverter;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.common.engine.impl.util.io.InputStreamSource;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.repository.DeploymentBuilder;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.ui.common.service.exception.BadRequestException;
import org.flowable.ui.common.util.XmlUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/app")
public class asdfghjkl {
    @Autowired
    protected RepositoryService repositoryService;

//    public String mergePro(HttpServletResponse response,@PathVariable("def1_id") String def1_id) throws IOException {
//        InputStream ainputStream=repositoryService.getProcessModel(def1_id);
//        //读取输入流
//        int count=ainputStream.available();
//        byte[] contents = new byte[count];
//        ainputStream.read(contents);
//        String axml = new String(contents);
//        return "true";
//
//    }
    @GetMapping(value = "/rest/mergepro")
    public String mergePro(@RequestParam(value="def1_id") String def1_id,@RequestParam(value="def2_id") String def2_id)throws IOException
    {
        System.out.println(def1_id+"      "+def2_id);
        File file = new File("D:/testmerage1.bpmn20.xml");
        if(def1_id ==null){
            throw new BadRequestException("No process model id is provided");
        }
        InputStream ainuStream1=repositoryService.getProcessModel(def2_id);
        InputStream ainuStream2=repositoryService.getProcessModel(def1_id);
        //读取输入流
        //int count=ainuStream.available();
       // byte[] contents = new byte[count];
       // ainuStream.read(contents);
        //String axml = new String(contents);
        try {
            //创建解析器
            System.out.printf("1");
            SAXReader saxReader = new SAXReader();
            //ͨ通过解析器的read方法将配置文件读取到内存中，生成一个Document[org.dom4j]对象树
            //Document a = saxReader.read(ainuStream);
            Document a = saxReader.read(ainuStream1);
            Document b = saxReader.read(ainuStream2);
            //删除a中definitions 下bpmndi:BPMNDiagram所有元素
            Node dela = (Node) a.selectSingleNode("/definitions/bpmndi:BPMNDiagram");
            dela.getParent().remove(dela);
            //删除b中bpmndi:BPMNDiagram下所有元素
            Node delb = (Node) b.selectSingleNode("/definitions/bpmndi:BPMNDiagram");
            delb.getParent().remove(delb);


            Map xMap=new HashMap();
            //获得命名空间
            String nsURI = a.getRootElement().getNamespaceURI();
            xMap.put("xmlns", nsURI);
            XPath aendEp = a.createXPath("//xmlns:endEvent");
            XPath bstaEp = b.createXPath("//xmlns:startEvent");
            XPath bdocEp = b.createXPath("//xmlns:documentation");
            aendEp.setNamespaceURIs(xMap);
            bstaEp.setNamespaceURIs(xMap);
            bdocEp.setNamespaceURIs(xMap);



            //获取a中endEvent节点对象
            Element a_end_Elt = (Element) aendEp.selectSingleNode(a);
            //获取a中endEvent节点对象的id属性值ֵ
            String a_end_Elt_Attr_id = a_end_Elt.attributeValue("id");
            //获取b中startEvent节点对象
            Element b_start_Elt = (Element) bstaEp.selectSingleNode(b);
            //获取b中starEvent节点对象的属性值ֵ
            String b_start_Elt_Attr_id =b_start_Elt.attributeValue("id");

            //删除a中的结束节点
//			Node a_end = aendEp.selectSingleNode(a);
//			a_end.getParent().remove(a_end);
            a_end_Elt.getParent().remove(a_end_Elt);
            //删除b中的开始节点
            b_start_Elt.getParent().remove(b_start_Elt);
            //获取b中process下documentation节点对象
            Element b_documentation_Elt = (Element) bdocEp.selectSingleNode(b);
            //删除b中的documentation节点
            b_documentation_Elt.getParent().remove(b_documentation_Elt);


            //遍历获得要修改的属性值ֵ
            Element b_root = b.getRootElement();
            List<Element> b_proElts = b_root.element("process").elements();
            String insert_a = null;
            for(Element b_proElt:b_proElts){
                if(b_proElt.attributeValue("sourceRef") != null)
                {
                    if(b_proElt.attributeValue("sourceRef").equals(b_start_Elt_Attr_id)){
                        insert_a=b_proElt.attributeValue("targetRef");
                        b_proElt.getParent().remove(b_proElt);
                    }
                }
            }

            //获取a下所有sequenceFlow节点并进行遍历,修改待改属性
            Element a_root = a.getRootElement();

            List<Element> a_proElts = a_root.element("process").elements();

            for(Element a_proElt:a_proElts){
                if(a_proElt.attributeValue("targetRef")!=null)
                {
                    if(a_proElt.attributeValue("targetRef").equals(a_end_Elt_Attr_id)){
                        a_proElt.attribute("targetRef").setValue(insert_a);
                    }
                }
            }

            Element element1=(Element) b_root.element("process");//获取第一可操作元素
            List<Element> elements = element1.elements();//获得b根节点下的节点信息
//			List<Element> elements=b.selectNodes("/definitions/process/*");
            Element parent = (Element) a.getRootElement().element("process");//获得第一个xml的根节点下的process节点,作为父节点

            for (Element element : elements) {
                parent.add((Element) element.clone());//将b的process下的节点添加到a的根节点下
            }



            //格式化为缩进格式
            OutputFormat format = OutputFormat.createPrettyPrint();
            //设置编码格式:XML自身编码格式
            format.setEncoding("utf-8");
            try {
                XMLWriter writer=new XMLWriter(new FileWriter(file),format);
                //写入数据
                writer.write(a);
                writer.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }


//            System.out.println(b.asXML());
//            System.out.println(a.asXML());
//            String cd=a.asXML();
//            String resourceName="new.bpmn";//资源的名称必须是以bpmn或者bpmn20.xml结尾
//            //InputStream inputStream=repositoryService.getProcessModel(def1_id);
//            DeploymentBuilder deploymentBuilder = repositoryService.createDeployment();
//            InputStream inputStream = new ByteArrayInputStream(cd.getBytes());
//            Deployment deploy = deploymentBuilder.name("8.zip")
//                    .addInputStream(resourceName,inputStream).deploy();
//            System.out.println("deploymentBuilder"+deploymentBuilder);
//            System.out.println("deploy"+deploy);
            //BpmnModel bpmnModel = getBpmnModel();
//            String depores=a.asXML();
//            String processName="gain.bpmn20.xml";
//            repositoryService.createDeployment()
//                    .addString(processName, depores)
//                    .deploy();
            //转换成bpmnModel
            InputStream xmlStream = new ByteArrayInputStream(a.asXML().getBytes("utf-8"));//获取流程资源
            InputStreamSource xmlSource = new InputStreamSource(xmlStream);
//        xml-->bpmnmodel
            BpmnXMLConverter converter = new BpmnXMLConverter();//创建转换对象
            BpmnModel bpmnmodel = converter.convertToBpmnModel(xmlSource,true,false,"UTF-8");

            //生成bpmn自动布局
            if (bpmnmodel.getLocationMap().size() == 0) {
                BpmnAutoLayout bpmnLayout = new BpmnAutoLayout(bpmnmodel);
                bpmnLayout.execute();
            }
            //bpmnmodel--xml-->测试xml文件
            BpmnXMLConverter bpmnXMLConverter=new BpmnXMLConverter();
            byte[] convertToXML = bpmnXMLConverter.convertToXML(bpmnmodel);
            String bytes=new String(convertToXML);
            //System.out.println(bytes);
            String resource2="new_bpmnmodel.bpmn";
            repositoryService.createDeployment().addBpmnModel(resource2,bpmnmodel).deploy();

//            String resourceName="anew.bpmn";
//            //String depores=a.asXML();
//            Deployment deployment = repositoryService.createDeployment().name("9.zip").addString(resourceName, bytes).deploy();
//            ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery().deploymentId(deployment.getId()).singleResult();
//            if(processDefinition!=null){
//                System.out.printf(processDefinition.getName());
//            }else {
//                System.out.printf("没有流程定义");
//            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return "123456";
    }


}


