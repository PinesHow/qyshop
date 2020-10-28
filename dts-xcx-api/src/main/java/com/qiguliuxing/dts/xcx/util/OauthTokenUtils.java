package com.qiguliuxing.dts.xcx.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class OauthTokenUtils {

    /**
     * 测试环境
     */
//    private static String appId = "wx4ce4acd887dde734";
//    private static String appSecret = "6f688c3eb18cb8430a5b0d6cf43497a4";

    /**
     * 线上环境
     */
    private static String appId = "wxf27c3ccd080e8c6e";
    private static String appSecret = "b89315a433f76b6f8fd7813ea9f0f1a2";


    private static String getToken(URL url) throws IOException{
            StringBuffer buffer = new StringBuffer();
            //http协议传输
            HttpURLConnection httpUrlConn = (HttpURLConnection) url.openConnection();
            httpUrlConn.setDoOutput(true);
            httpUrlConn.setDoInput(true);
            httpUrlConn.setUseCaches(false);
            httpUrlConn.connect();
            //将返回的输入流转换成字符串
            InputStream inputStream = httpUrlConn.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, "utf-8");
            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
            String str = null;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            bufferedReader.close();
            inputStreamReader.close();
            //释放资源
            inputStream.close();
//			inputStream = null;
            httpUrlConn.disconnect();
            return buffer.toString();
    }

    /**
     * 获取token凭证
     * @param code
     * @return
     * @throws IOException
     */
    public static String oauthToken(String code) throws IOException {
        URL url = new URL("https://api.weixin.qq.com/sns/oauth2/access_token?appid="+appId+"&secret="+appSecret+"&code="+code+"&grant_type=authorization_code");
        //发送并把结果赋给result
        String result= getToken(url);
        return result;
    }

    /**
     * 获取用户信息
     * @return
     */
    public static String getUserInfo(String accessToken, String openId) throws IOException{
        URL url = new URL("https://api.weixin.qq.com/sns/userinfo?access_token="+accessToken+"&openid="+openId+"&lang=zh_CN");
        //发送并把结果赋给result
        String result= getToken(url);
        return result;
    }

    public static String getOauthJson(String request) throws IOException{
        URL url = new URL(request);
        //发送并把结果赋给result
        String result= getToken(url);
        return result;
    }
}
