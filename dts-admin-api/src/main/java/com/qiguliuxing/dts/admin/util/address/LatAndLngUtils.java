package com.qiguliuxing.dts.admin.util.address;

import net.sf.json.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.*;
import java.io.*;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

public class LatAndLngUtils {

    private static final Logger logger = LoggerFactory.getLogger(LatAndLngUtils.class);
    private static final String HS_BASE_URL = "https://apis.map.qq.com";

    /**
     * 通过腾讯地图将地理位置转成经纬度
     *
     * @param address 具体地址
     * @return
     */

    public static String getLatAndLng(String address,String key) {
        System.out.println("------------- 经纬度查询 -------------");
//        String Key = "填写你申请的KEY";
        StringBuffer sb = new StringBuffer(HS_BASE_URL + "/ws/geocoder/v1/?");
        sb.append("address="+address);
        sb.append("&key="+key);
        logger.info("===>>> 查询链接："+sb.toString());
        return getURLContent(sb.toString());
    }


    public static String getURLContent(String urlStr) {
        //请求的url
        URL url = null;
        //请求的输入流
        BufferedReader in = null;
        //输入流的缓冲
        StringBuffer sb = new StringBuffer();
        try{
            url = new URL(urlStr);
            in = new BufferedReader(new InputStreamReader(url.openStream(),"UTF-8") );
            String str = null;
            //一行一行进行读入
            while((str = in.readLine()) != null) {
                sb.append( str );
            }
        } catch (Exception ex) {
            logger.info("获取经纬度失败:"+ex.getMessage());
        } finally{
            try{
                if(in!=null) {
                    in.close(); //关闭流
                }
            }catch(IOException ex) {
                logger.info("关流失败："+ex.getMessage());
            }
        }
        return sb.toString();
    }



    /**
     * 通过腾讯地图将经纬度转成详细地址
     *
     * @param lat 纬度
     * @param lng 经度
     * @param key 开发密钥（Key） 需自行申请
     * @return
     */

    public static JSONObject getAddress(String lat, String lng, String key) {
        try {
            String hsUrl = "https://apis.map.qq.com/ws/geocoder/v1/?location=" + lat + "," + lng + "&key=" + key + "&get_poi=1";

            URL url;

            url = new URL(hsUrl);
            HttpsURLConnection con = (HttpsURLConnection) url.openConnection();
            con.setRequestMethod("GET");// 提交模式
            X509TrustManager xtm = new X509TrustManager() {
                @Override
                public X509Certificate[] getAcceptedIssuers() {
                    // TODO Auto-generated method stub
                    return null;
                }

                @Override
                public void checkServerTrusted(X509Certificate[] arg0, String arg1)
                        throws CertificateException {
                    // TODO Auto-generated method stub

                }

                @Override
                public void checkClientTrusted(X509Certificate[] arg0, String arg1)
                        throws CertificateException {
                    // TODO Auto-generated method stub

                }
            };

            TrustManager[] tm = {xtm};

            SSLContext ctx = SSLContext.getInstance("TLS");
            ctx.init(null, tm, null);

            con.setSSLSocketFactory(ctx.getSocketFactory());
            con.setHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String arg0, SSLSession arg1) {
                    return true;
                }
            });


            InputStream inStream = con.getInputStream();
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = inStream.read(buffer)) != -1) {
                outStream.write(buffer, 0, len);
            }
            byte[] b = outStream.toByteArray();//网页的二进制数据
            outStream.close();
            inStream.close();
            String rtn = new String(b, "utf-8");
            if (StringUtils.isNotBlank(rtn)) {
                JSONObject object = JSONObject.fromObject(rtn);
                if (object != null) {
                    if (object.has("status") && object.getInt("status") == 0) {
                        JSONObject result = JSONObject.fromObject(object.get("result"));
                        if (result != null) {
                            JSONObject addressComponent = JSONObject.fromObject(result.get("address_component"));
                            if (addressComponent != null) {
                                return addressComponent;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
