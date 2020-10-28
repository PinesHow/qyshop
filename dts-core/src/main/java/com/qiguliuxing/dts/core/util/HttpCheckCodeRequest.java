package com.qiguliuxing.dts.core.util;


import com.alibaba.fastjson.JSONObject;
import com.qiguliuxing.dts.db.domain.TellCheckCodeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Date;

@Component
public class HttpCheckCodeRequest {

    @Autowired
    private  BaseRedisService baseRedisService;
    /**
     * 向指定 URL 发送POST方法的请求
     *
     * @param url 发送请求的 URL
     * @return 所代表远程资源的响应结果
     */
    public static String sendPost(String url, String param) {
        PrintWriter out = null;
        BufferedReader in = null;
        String result = "";
        try {
            URL realUrl = new URL(url);
            // 打开和URL之间的连接
            URLConnection conn = realUrl.openConnection();
            // 设置通用的请求属性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            // 发送POST请求必须设置如下两行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 获取URLConnection对象对应的输出流
            out = new PrintWriter(conn.getOutputStream());
            // 发送请求参数
            out.print(param);
            // flush输出流的缓冲
            out.flush();
            // 定义BufferedReader输入流来读取URL的响应
            in = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        //使用finally块来关闭输出流、输入流
        finally {
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        return result;
    }

    // 创建验证码
    public static String smsCode() {
        String random = (int) ((Math.random() * 9 + 1) * 100000) + "";
        return random;
    }

    /**
     * 抽出发送短信方法
     * @param tell
     * @return
     * @throws UnsupportedEncodingException
     * 2019.4.26yjw
     */
    public  String toSendVerificationCode(String tell,String mark) throws UnsupportedEncodingException {
        //String url = "http://jk.106txt.com/smsUTF8.aspx";
        String url = "http://jk.smstcby.com/smsUTF8.aspx";
        //获取随机验证码
        String  smscode=smsCode();
        //短信内容
        String content = URLEncoder.encode("【青岩商城】验证码:"+smscode+"。您正在使用验证功能,该验证码仅用于身份验证！验证码有效日期为5分钟。", "UTF-8");
        //拼接参数
        TellCheckCodeEntity checkCodeEntity=new TellCheckCodeEntity();
        checkCodeEntity.setCode(smscode);
        checkCodeEntity.setCreateDate(new Date());
        checkCodeEntity.setTell(tell);
        String checkCodeStr= JSONObject.toJSONString(checkCodeEntity);
        String postData = "type=send&username=1283289245&password=BC3DE8B8D7AB7C5EFD7579DC876F71F6&gwid=e2fb5a77&mobile=" + tell + "&message=" + content + "&rece=json";
        //发送并把结果赋给result,返回一个XML信息,解析xml 信息判断
        String result= HttpCheckCodeRequest.sendPost(url, postData);
        //String result = null;
        if(null != result){
            baseRedisService.setString(mark+tell,checkCodeStr,300l);//五分钟
            return result;
           }
        return result;
    }


    public  String toSendNotRegister(String tell) throws UnsupportedEncodingException {
        String url = "http://jk.106txt.com/smsUTF8.aspx";
        //短信内容
        String content = URLEncoder.encode("【青岩商城】你注册的账号未通过审核，请重新注册。谢谢", "UTF-8");
        //拼接参数
        String postData = "type=send&username=1283289245&password=BC3DE8B8D7AB7C5EFD7579DC876F71F6&gwid=e2fb5a77&mobile=" + tell + "&message=" + content + "&rece=json";
        //发送并把结果赋给result,返回一个XML信息,解析xml 信息判断
        String result= HttpCheckCodeRequest.sendPost(url, postData);
        //String result = null;
        if(null != result){
            baseRedisService.setString("yzm"+tell,300l);
        }
        return result;
    }

    public  String toSendRegister(String tell,String smsCode) throws UnsupportedEncodingException {
        String url = "http://jk.106txt.com/smsUTF8.aspx";
//        String  smscode=smsCode();
        //短信内容
        String content = URLEncoder.encode("【青岩商城】你已通过审核，请登录，默认密码为"+smsCode, "UTF-8");
        //拼接参数
        String postData = "type=send&username=1283289245&password=BC3DE8B8D7AB7C5EFD7579DC876F71F6&gwid=e2fb5a77&mobile=" + tell + "&message=" + content + "&rece=json";
        //发送并把结果赋给result,返回一个XML信息,解析xml 信息判断
        String result= HttpCheckCodeRequest.sendPost(url, postData);
        //String result = null;
        if(null != result){
//            baseRedisService.setString("yzm"+tell,300l);
        }
        return result;
    }

    public  String toSendShopOrderNotify(String tell,String notify) throws UnsupportedEncodingException {
        String url = "http://jk.106txt.com/smsUTF8.aspx";
//        String  smscode=smsCode();
        //短信内容
        String content = URLEncoder.encode("【贵州云鲤易游数据信息服务有限公司】(代发货)"+notify+"请尽快打开魅力青岩后台管理系统处理订单,超过7天未发货,系统将自动退款!", "UTF-8");
        //拼接参数
        String postData = "type=send&username=1283289245&password=BC3DE8B8D7AB7C5EFD7579DC876F71F6&gwid=e2fb5a77&mobile=" + tell + "&message=" + content + "&rece=json";
        //发送并把结果赋给result,返回一个XML信息,解析xml 信息判断
        String result= HttpCheckCodeRequest.sendPost(url, postData);
        //String result = null;
        if(null != result){
//            baseRedisService.setString("yzm"+tell,300l);
        }
        return result;
    }

    /**
     * 退款信息
     * @param tell
     * @param notify
     * @return
     * @throws UnsupportedEncodingException
     */
    public  String toRefund(String tell,String notify) throws UnsupportedEncodingException {
        String url = "http://jk.106txt.com/smsUTF8.aspx";
//        String  smscode=smsCode();
        //短信内容
        String content = URLEncoder.encode("【贵州云鲤易游数据信息服务有限公司】(代退款)"+notify+"请尽快打开魅力青岩后台管理系统处理订单,超过7天未处理退款,系统将自动退款!", "UTF-8");
        //拼接参数
        String postData = "type=send&username=1283289245&password=BC3DE8B8D7AB7C5EFD7579DC876F71F6&gwid=e2fb5a77&mobile=" + tell + "&message=" + content + "&rece=json";
        //发送并把结果赋给result,返回一个XML信息,解析xml 信息判断
        String result= HttpCheckCodeRequest.sendPost(url, postData);
        //String result = null;
        if(null != result){
//            baseRedisService.setString("yzm"+tell,300l);
        }
        return result;
    }


    public String getTellCheckCode(String tell,int getInterval,String mark) throws UnsupportedEncodingException {
        String codeKye=mark+tell;
        if(baseRedisService.hasKey(codeKye)){
            String checkStr=baseRedisService.getString(codeKye);
            TellCheckCodeEntity checkCodeEntity=JSONObject.parseObject(checkStr,TellCheckCodeEntity.class);
            Date createDate=checkCodeEntity.getCreateDate();
//           Date createDate=simpleDateFormat.parse(dateStr);
            long interval=DateUtils.getTimeInterval(createDate,new Date());
            if(interval<=getInterval){
                return "请勿频繁获取验证码!";
            }
        }
        String result=toSendVerificationCode(tell,mark);
        if(result!=null){
            return "验证码发送成功!";
        }
        return "验证码发送失败!";
    }

    public boolean checkCode(String tell,String mark,String code){
        String codeKye=mark+tell;
        if(baseRedisService.hasKey(codeKye)){
            String checkStr=baseRedisService.getString(codeKye);
            TellCheckCodeEntity checkCodeEntity=JSONObject.parseObject(checkStr,TellCheckCodeEntity.class);
             return checkCodeEntity.getCode().equals(code);
        }
        return false;
    }
}

