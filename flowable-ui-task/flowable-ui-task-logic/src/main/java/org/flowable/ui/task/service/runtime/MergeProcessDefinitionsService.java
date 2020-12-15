package org.flowable.ui.task.service.runtime;

import org.dom4j.*;
import org.dom4j.io.SAXReader;
import org.flowable.bpmn.BpmnAutoLayout;
import org.flowable.bpmn.converter.BpmnXMLConverter;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.common.engine.impl.util.io.InputStreamSource;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RepositoryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.swing.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static java.lang.String.valueOf;

/**
 * author Li
 */
@Service
@Transactional
public class MergeProcessDefinitionsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MergeProcessDefinitionsService.class);

    @Autowired
    protected RepositoryService repositoryService;

    @Qualifier("processEngine")
    @Autowired
    protected ProcessEngine processEngine;

    /**
     * 根据给定的两个流程定义Id,解析xml文件，将两个流程定义合并，返回合并后的流程定义
     * @param processDefinitionId1
     * @param processDefinitionId2
     * @return
     * @throws DocumentException
     */
    public String mergeProcessDenitions(String processDefinitionId1,String processDefinitionId2) throws DocumentException {

        //获得流程定义文件的输入流
        InputStream ainputStream = GetProcessXml(processDefinitionId1);
        InputStream binputStream = GetProcessXml(processDefinitionId2);

//        //创建解析器
//        SAXReader saxReader = new SAXReader();
//        //通过解析器的read方法将配置文件读取到内存中，生成一个Document[org.dom4j]对象树
//        Document a = saxReader.read(ainputStream);
//        Document b = saxReader.read(binputStream);

        //根据输入流解析成dom4j对象树
        Document a=GetXmlDocument(ainputStream);
        Document b=GetXmlDocument(binputStream);


       //删除a中definitions 下bpmndi:BPMNDiagram所有元素
        Node dela = (Node) a.selectSingleNode("/definitions/bpmndi:BPMNDiagram");
        dela.getParent().remove(dela);
       //删除b中bpmndi:BPMNDiagram下所有元素
        Node delb = (Node) b.selectSingleNode("/definitions/bpmndi:BPMNDiagram");
        delb.getParent().remove(delb);
        //判断b中节点是否在A中存在，若存在，为重复id生成新id
        List<Element> aTestElts = a.getRootElement().element("process").elements();
        List<Element> bTestElts = b.getRootElement().element("process").elements();
        for(Element aTestElt:aTestElts){

            if(aTestElt.attributeValue("id")!=null){

                if(aTestElt.attributeValue("id").equals(bTestElts.get(2).attributeValue("id"))){
                    int res=JOptionPane.showConfirmDialog(null, "该流程定义内容已存在，是否继续写回服务库", "是否继续", JOptionPane.YES_NO_OPTION);
                    if(res==JOptionPane.YES_OPTION){
                        //修改重复id为新生成id
                        for(Element bTestElt:bTestElts){

                            if(bTestElt.attributeValue("id")!=null){

//                            System.out.println("未修改"+bTestElt.attributeValue("id"));
                                String beforeSourceEltId = bTestElt.attributeValue("id");
                                bTestElt.addAttribute("id", "sid-" + UUID.randomUUID().toString());
                                String afterSourceEltId = bTestElt.attributeValue("id");
//                            System.out.println("修改后"+bTestElt.attributeValue("id"));
                                for(Element bTestElt2:bTestElts){
                                    if((bTestElt2.attributeValue("sourceRef")!=null)&&(bTestElt2.attributeValue("targetRef")!=null)){
                                        if (bTestElt2.attributeValue("sourceRef").equals(beforeSourceEltId))
                                            bTestElt2.addAttribute("sourceRef",afterSourceEltId);
                                        if (bTestElt2.attributeValue("targetRef").equals(beforeSourceEltId))
                                            bTestElt2.addAttribute("targetRef",afterSourceEltId);
                                    }
                                }

                            }

                         }

                    }//end if res==JOptionPane.YES_OPTION
                    else{
                        return null;
                    }
                }

            }
        }

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

  //System.out.println(b.getRootElement().element("process").element("startEvent").attributeValue("id"));

        //获取a中endEvent节点对象
        Element a_end_Elt = (Element) aendEp.selectSingleNode(a);
        //获取a中endEvent节点对象的id属性值ֵ
        String a_end_Elt_Attr_id = a_end_Elt.attributeValue("id");
        //获取b中startEvent节点对象
        Element b_start_Elt = (Element) bstaEp.selectSingleNode(b);

        //获取b中starEvent节点对象的属性值ֵ
        String b_start_Elt_Attr_id =b_start_Elt.attributeValue("id");

        //获取b中process下documentation节点对象
        Element b_documentation_Elt = (Element) bdocEp.selectSingleNode(b);

        //删除a中的结束节点
        a_end_Elt.getParent().remove(a_end_Elt);
        //删除b中的开始节点
        b_start_Elt.getParent().remove(b_start_Elt);
        //删除b中的documentation节点
        b_documentation_Elt.getParent().remove(b_documentation_Elt);

        //遍历b中获得要修改的属性值ֵ+需删除的节点
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
        Element parent = (Element) a.getRootElement().element("process");//获得第一个xml的根节点下的process节点,作为父节点

        for (Element element : elements) {
            parent.add((Element) element.clone());//将b的process下的节点添加到a的根节点下

        }
        return a.asXML();
    }


    /**
     * 根据流程定义id获取流程资源
     * @param defnitionId
     * @return
     */
    public InputStream GetProcessXml(String defnitionId){
        return repositoryService.getProcessModel(defnitionId);
    }


    /**
     * 根据流程定义xml文件，解析得到documen对象树
     * @param inputStream
     * @return
     * @throws DocumentException
     */
    public Document GetXmlDocument(InputStream inputStream) throws DocumentException {
        //创建解析器
        SAXReader saxReader = new SAXReader();
        //通过解析器的read方法将配置文件读取到内存中，生成一个Document[org.dom4j]对象树
        Document document = saxReader.read(inputStream);
        return document;
    }


    /**
     * 发布合并后的流程定义
     * @param resource
     * @throws UnsupportedEncodingException
     */
    public void DeployMergeProcessDefinitions(String resource) throws UnsupportedEncodingException {

        //转换成bpmnModel
        InputStream xmlStream = new ByteArrayInputStream(resource.getBytes("utf-8"));//获取流程资源
        InputStreamSource xmlSource = new InputStreamSource(xmlStream);

        //xml-->bpmnmodel
        BpmnXMLConverter converter = new BpmnXMLConverter();//创建转换对象
        BpmnModel bpmnmodel = converter.convertToBpmnModel(xmlSource,true,false,"UTF-8");

        //生成bpmn自动布局
        if (bpmnmodel.getLocationMap().size() == 0) {
            BpmnAutoLayout bpmnLayout = new BpmnAutoLayout(bpmnmodel);
            bpmnLayout.execute();
        }

        //bpmnmodel--xml-->测试xml文件
//        BpmnXMLConverter bpmnXMLConverter=new BpmnXMLConverter();
//        byte[] convertToXML = bpmnXMLConverter.convertToXML(bpmnmodel);
//        String bytes=new String(convertToXML);
//        System.out.println(bytes);

//        Bpmnmodel形式部署
        String resource2="new_bpmnmodel.bpmn";
        repositoryService.createDeployment().addBpmnModel(resource2,bpmnmodel).deploy();
    }
}
