package com.qiguliuxing.dts.core.util.wxpay;

import okhttp3.HttpUrl;

import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.Random;

/**
 * @ClassName WxAPIV3SignUtils
 * @Description: 微信API-V3签名工具
 * @Author wangshiwen
 * @Date 2020/3/30
 * @Version V1.0
 */
public class WxAPIV3SignUtils {
    // Authorization: <schema> <token>
    // GET - getToken("GET", httpurl, "")
    // POST - getToken("POST", httpurl, json)

    public static final String sign_type = "HMAC-SHA256";//签名类型，仅支持HMAC-SHA256。示例值：HMAC-SHA256
    public static final String schema = "WECHATPAY2-SHA256-RSA2048";
    private static final String SYMBOLS = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    private static final Random RANDOM = new SecureRandom();

    /**
     *
     * @param method
     * @param url
     * @param body
     * @param mchId 商户号
     * @param privateKey 商户证书私钥
     * @param serialNo 商户API证书序列号
     * @return
     * @throws Exception
     */
    public static String getToken(String method,HttpUrl url,String body,String mchId,String privateKey,String serialNo) throws Exception{
        String nonceStr = generateNonceStr();
        long timestamp = System.currentTimeMillis() / 1000;
        String message = buildMessage(method, url, timestamp, nonceStr, body);
        PrivateKey privateKey1 = getPrivateKey(privateKey);
        String signature = sign(message.getBytes("utf-8"),privateKey1);

        return schema + " " +"mchid=\"" + mchId + "\","
                + "nonce_str=\"" + nonceStr + "\","
                + "timestamp=\"" + timestamp + "\","
                + "serial_no=\"" + serialNo + "\","
                + "signature=\"" + signature + "\"";
    }

    /**
     *
     * @param message
     * @param privateKey 商户私钥
     * @return
     */
    private static String sign(byte[] message, PrivateKey privateKey) throws Exception{
        Signature sign = Signature.getInstance("SHA256withRSA");
        sign.initSign(privateKey);
        sign.update(message);

        return Base64.getEncoder().encodeToString(sign.sign());
    }

    private static String buildMessage(String method, HttpUrl url, long timestamp, String nonceStr, String body) {
        String canonicalUrl = url.encodedPath();
        if (url.encodedQuery() != null) {
            canonicalUrl += "?" + url.encodedQuery();
        }

        return method + "\n"
                + canonicalUrl + "\n"
                + timestamp + "\n"
                + nonceStr + "\n"
                + body + "\n";
    }

    /**
     * String转私钥PrivateKey
     * @param key
     * @return
     * @throws Exception
     */
    public static PrivateKey getPrivateKey(String key) throws Exception {
        byte[] keyBytes;
        try {
//          keyBytes=Base64.getDecoder().decode(key);
            keyBytes=Base64.getMimeDecoder().decode(key);
//          keyBytes = (new BASE64Decoder()).decodeBuffer(key);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(keyBytes);
            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePrivate(keySpec);
        }catch (NoSuchAlgorithmException e){
            throw new RuntimeException("当前Java环境不支持RSA", e);
        }catch (InvalidKeySpecException e){
            throw new RuntimeException("无效的密钥格式");
        }

//        String content = new String(Files.readAllBytes(Paths.get(key)), "utf-8");
//        try {
//            String privateKey = content.replace("-----BEGIN PRIVATE KEY-----", "")
//                    .replace("-----END PRIVATE KEY-----", "")
//                    .replaceAll("\\s+", "");
//
//            KeyFactory kf = KeyFactory.getInstance("RSA");
//            return kf.generatePrivate(
//                    new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)));
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException("当前Java环境不支持RSA", e);
//        } catch (InvalidKeySpecException e) {
//            throw new RuntimeException("无效的密钥格式");
//        }
    }

    /**
     * 获取私钥。
     *
     * @param filename 私钥文件路径  (required)
     * @return 私钥对象
     */
//    public static PrivateKey getPrivateKey(String filename) throws IOException {
//
//        String content = new String(Files.readAllBytes(Paths.get(filename)), "utf-8");
//        try {
//            String privateKey = content.replace("-----BEGIN PRIVATE KEY-----", "")
//                    .replace("-----END PRIVATE KEY-----", "")
//                    .replaceAll("\\s+", "");
//
//            KeyFactory kf = KeyFactory.getInstance("RSA");
//            return kf.generatePrivate(
//                    new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privateKey)));
//        } catch (NoSuchAlgorithmException e) {
//            throw new RuntimeException("当前Java环境不支持RSA", e);
//        } catch (InvalidKeySpecException e) {
//            throw new RuntimeException("无效的密钥格式");
//        }
//    }

    /**
     * 获取随机字符串 Nonce Str
     *
     * @return String 随机字符串
     */
    public static String generateNonceStr() {
        char[] nonceChars = new char[32];
        for (int index = 0; index < nonceChars.length; ++index) {
            nonceChars[index] = SYMBOLS.charAt(RANDOM.nextInt(SYMBOLS.length()));
        }
        return new String(nonceChars);
    }

}
