package org.flowable.ui.task.service.runtime;

import org.apache.commons.codec.binary.Base64;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class BackEndHttpRequest {
    /**
     * 向指定的URL发送GET方法的请求
     * @param url    发送请求的URL
     * @param param  请求参数，请求参数应该是 name1=value1&name2=value2 的形式 
     * @return       远程资源的响应结果 
     */
    public static String sendGet(String url, String param) {
        String result = "";
        BufferedReader bufferedReader = null;
        try {
            //1、读取初始URL
            String urlNameString = url + "?" + param;
            //2、将url转变为URL类对象
            URL realUrl = new URL(urlNameString);

            //设置认证的用户名和密码
            String theUserName="admin";
            String thePassword="test";

            //将用户名、密码合并到一个URL上
            String userPassword = theUserName + ":" + thePassword;

//            String authString = username + ":" + password;

            //对字符串进行编码
//            String encoding = new sun.misc.BASE64Encoder().encode(userPassword.getBytes());
            String authStringEnc = new String(Base64.encodeBase64(userPassword.getBytes()));

            //3、打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            //4、设置通用的请求属性
            connection.setRequestProperty("Authorization", "Basic " + authStringEnc);
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            
            //5、建立实际的连接 
            connection.connect();
            //获取所有响应头字段
            Map<String, List<String>> map = connection.getHeaderFields();
            //遍历所有的响应头字段
            for(String key : map.keySet()) {
                System.out.println(key + "---->" + map.get(key));
            }
            
            //6、定义BufferedReader输入流来读取URL的响应内容 ，UTF-8是后续自己加的设置编码格式，也可以去掉这个参数
            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
            String line = "";
            while(null != (line = bufferedReader.readLine())) {
                result += line;
            }
//            int tmp;
//            while((tmp = bufferedReader.read()) != -1){
//                result += (char)tmp;
//            }
            
        }catch (Exception e) {
            // TODO: handle exception
            System.out.println("发送GET请求出现异常！！！"  + e);
            e.printStackTrace();
        }finally {        //使用finally块来关闭输入流 
            try {
                if(null != bufferedReader) {
                    bufferedReader.close();
                }
            }catch (Exception e2) {
                // TODO: handle exception
                e2.printStackTrace();
            }
        }
        return result;
    }
    /**
     * 向指定的URL发送POST方法的请求
     * @param url    发送请求的URL
     * @param param  请求参数，请求参数应该是 name1=value1&name2=value2 的形式 
     * @return       远程资源的响应结果 
     */
    public static String sendPost(String url, String param) {
        String result = "";
        BufferedReader bufferedReader = null;
        PrintWriter out = null;
        try {
            //1、2、读取并将url转变为URL类对象
            URL realUrl = new URL(url);

            //设置认证的用户名和密码
            String theUserName="admin";
            String thePassword="test";

            //将用户名、密码合并到一个URL上
            String userPassword = theUserName + ":" + thePassword;

//            String authString = username + ":" + password;

            //对字符串进行编码
//            String encoding = new sun.misc.BASE64Encoder().encode(userPassword.getBytes());
            String authStringEnc = new String(Base64.encodeBase64(userPassword.getBytes()));
            
            //3、打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            //4、设置通用的请求属性
            connection.setRequestProperty("Authorization", "Basic " + authStringEnc);
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
//            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Content-Type", "application/raw; charset=utf-8");

            // 发送POST请求必须设置如下两行  
            connection.setDoInput(true);
            connection.setDoOutput(true);
            
            //5、建立实际的连接
            connection.connect();
            //获取URLConnection对象对应的输出流
            out = new PrintWriter(connection.getOutputStream());
            //发送请求参数
            out.print(param);
            //flush输出流的缓冲
            out.flush();
            //
            
            //6、定义BufferedReader输入流来读取URL的响应内容
            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));
            String line;
            while(null != (line = bufferedReader.readLine())) {
                result += line;
            }
        }catch (Exception e) {
            // TODO: handle exception
            System.out.println("发送POST请求出现异常！！！"  + e);
            e.printStackTrace();
        }finally {        //使用finally块来关闭输出流、输入流 
            try {
                if(null != out) {
                    out.close();
                }
                if(null != bufferedReader) {
                    bufferedReader.close();
                }
            }catch (Exception e2) {
                // TODO: handle exception
                e2.printStackTrace();
            }
        }
        return result;
    }

    /**
     * 向指定的URL发送GET方法的请求
     * @param url    发送请求的URL
     * @param param  请求参数，请求参数应该是 name1=value1&name2=value2 的形式
     * @return       远程资源的响应结果
     */
    public static String sendGetForField(String url, String param) {
        String result = "";
        BufferedReader bufferedReader = null;

        try {

            //1、读取初始URL
            String urlNameString = url + "?" + param;
            //2、将url转变为URL类对象
            URL realUrl = new URL(urlNameString);

            //3、打开和URL之间的连接
            URLConnection connection = realUrl.openConnection();
            //4、设置通用的请求属性
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
//            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
            connection.setRequestProperty("Content-Type", "text/html; charset=utf-8");
            //5、建立实际的连接
            connection.connect();

//            //获取所有响应头字段
//            Map<String, List<String>> map = connection.getHeaderFields();
//            //遍历所有的响应头字段
//            for(String key : map.keySet()) {
//                System.out.println(key + "---->" + map.get(key));
//            }


            //6、定义BufferedReader输入流来读取URL的响应内容 ，UTF-8是后续自己加的设置编码格式，也可以去掉这个参数
//            long startTime = System.currentTimeMillis();
            bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream(),"UTF-8"));

            String line = "";
            while(null != (line = bufferedReader.readLine())) {
                result += line;
            }
//            long endTime = System.currentTimeMillis();
//            System.out.println(param + "程序运行时间： " + (endTime - startTime) + "ms");

//            int tmp;
//            while((tmp = bufferedReader.read()) != -1){
//                result += (char)tmp;
//            }

        }catch (Exception e) {
            // TODO: handle exception
            System.out.println("发送GET请求出现异常！！！"  + e);
            e.printStackTrace();
        }finally {        //使用finally块来关闭输入流
            try {
                if(null != bufferedReader) {
                    bufferedReader.close();
                }
            }catch (Exception e2) {
                // TODO: handle exception
                e2.printStackTrace();
            }
        }

        return result;
    }
    
    public static void main(String args[]){
        BackEndHttpRequest backEndHttpRequest = new BackEndHttpRequest();
        backEndHttpRequest.sendGetForField("http://39.96.139.30:8080/dp-pro/sys/service/getServiceInfoByFieldName", "id=3&limitnum=2");
    }

}

