package com.qiguliuxing.dts.xcx.service;

import com.alibaba.fastjson.JSON;
import com.qiguliuxing.dts.core.util.BaseRedisService;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.db.domain.AccessToken;
import com.qiguliuxing.dts.db.domain.JsapiTicket;
import com.qiguliuxing.dts.db.domain.ShareParam;
import com.qiguliuxing.dts.xcx.util.OauthTokenUtils;
import com.qiguliuxing.dts.xcx.util.PayUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.weixin4j.util.SHA1;

import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class WxAuthService {
    private static final Logger logger = LoggerFactory.getLogger(WxAuthService.class);

//    @Value("${xcx.appid}")
    private String appId;

//    @Value("${xcx.secret}")
    private String appSecret;

    @Autowired
    private BaseRedisService baseRedisService;

    public Object getSignature(String request) {
        ShareParam shareParam = null;
        try {
            String url = URLDecoder.decode(request, "UTF-8");
            logger.info("通过转码获取到的页面地址："+url);
            String shareAccessToken = baseRedisService.getString("shareAccessToken");
//            String shareAccessToken = "";//临时写法
            // redis取出的是Empty, 从新获取token
            logger.info("redis获取到的token:"+shareAccessToken);
            if (StringUtils.isEmpty(shareAccessToken)) {
                //appid:服务号的appid; secret:服务号的AppSecret
                String getShareAccessTokenUrl = "https://api.weixin.qq.com/cgi-bin/token?"
                        + "grant_type=client_credential&appid="
                        + appId
                        + "&secret=" + appSecret;
                String accessTokenJson = OauthTokenUtils.getOauthJson(getShareAccessTokenUrl);//获取到的token和网页授权获取的token不一样，该token具有时效性(7200秒有效期，每天获取次数上限2000次)
                //这里用的阿里的fastjson
                AccessToken token = JSON.parseObject(accessTokenJson,
                        AccessToken.class);
                shareAccessToken = token.getAccess_token();
                // 设置token7150秒过期
                logger.info("通过请求获取到的token："+shareAccessToken);
                baseRedisService.setString("shareAccessToken",shareAccessToken,7140L);//暂时注释掉 部署的时候  打开
            }

            // redis 获取jsapiTicket
            String ticket = baseRedisService.getString("jsapiTicket");
//            String ticket = "";//临时写法
            logger.info("redis获取到的jsapiTicket："+ticket);
            // ticket is Empty 从新获取
            if (StringUtils.isEmpty(ticket)) {
                // 如果不是empty, 直接使用redis中的token获取jsapi
                String getJsapiUrl = "https://api.weixin.qq.com/cgi-bin/ticket/getticket?"
                        + "access_token=" + shareAccessToken + "&type=jsapi";
                String jsapiTicketJson = OauthTokenUtils.getOauthJson(getJsapiUrl);//获取到的jsapiTicket具有时效性(7200秒有效期，每天获取次数上限2000次)
                JsapiTicket jsapiTicket = JSON.parseObject(jsapiTicketJson,
                        JsapiTicket.class);
                // jsapiTicket 获取失败,
                //DataException自定义的异常类型
                if (jsapiTicket.getErrcode() != 0) {
                    return ResponseUtil.fail(500,"jsapiTicket 获取失败");
                }
                // 签名需要的参数ticket
                ticket = jsapiTicket.getTicket();
                // 设置过期时间
                logger.info("通过请求获取到的jsapiTicket："+ticket);
                baseRedisService.setString("jsapiTicket",ticket,7140L);//暂时注释掉 部署的时候  打开
            }

            // 获取随机字符串,这里是UUID (32位)
            String nonceStr = UUID.randomUUID().toString();


            // 获时间戳
            String timestamp = System.currentTimeMillis() / 1000 + "";
            logger.info("时间戳："+timestamp);

            // 参数
            Map<String, String> packageParams = new HashMap<>();
            packageParams.put("url", url);
            packageParams.put("noncestr", nonceStr);
            packageParams.put("jsapi_ticket", ticket);
            packageParams.put("timestamp", timestamp);
            // 获得拼接好的参数,按照ASCLL大小排序
            String createLinkString = PayUtil.createLinkString(packageParams);
            //SHA1签名,该类继承了weixin4J的WeixinSupport类, 使用的是父类的方法
            String signature = SHA1.encode(createLinkString);
            // 参数封装,返回前台
            shareParam = new ShareParam();
            shareParam.setAppId(appId);
            shareParam.setNonceStr(nonceStr);
            shareParam.setSignature(signature);
            shareParam.setTimestamp(timestamp);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("获取签名异常："+e.getMessage());
            return ResponseUtil.fail(500,"获取签名失败，请联系管理员");
        }
        logger.info("获取签名成功："+shareParam.getSignature());
        return shareParam;
    }
}
