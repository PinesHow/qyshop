package com.qiguliuxing.dts.core.config;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.thoughtworks.xstream.annotations.XStreamAlias;
import me.chanjar.weixin.common.annotation.Required;

public class WxPayConfigChildren extends WxPayConfig {

    @Required
    @XStreamAlias("combine_appid")
    @JsonProperty("combine_appid")
    private String combineAppid;

    @Required
    @XStreamAlias("combine_mchid")
    @JsonProperty("combine_mchid")
    private String combineMchid;

    /**
     * 证书秘钥  32位  自定义设置
     */
    @Required
    @JsonProperty("private_key")
    private String privateKey = "MIIEvgIBADANBgkqhkiG9w0BAQEFAASCBKgwggSkAgEAAoIBAQCqy5vdHA/x2oY1\n" +
            "A1NfHtBVxz80XtTYlCWwI9wHNiy8HHJ72VYKfciO5G5ifcBVsNoyUMYtTDhQcC1Q\n" +
            "h2Tg2m0mBImzW7QWFH73JPFqwEqiVQ0b6NPTX1a9MHIJyTL9ZYU4xn+KuRECk3UR\n" +
            "9DZpI8LbXk8XIFNtKnSnXucXno/3lxUi4nDrpLwviTl3jcFVFan/SEzVljVpCuen\n" +
            "xnQvEPal3/2gq7qZ9CVqq00l74JSpbkTRKGk4JqQaF/Ufk2MsmNDYS/LyZksub+W\n" +
            "cxitmwLyKfkfwx+4ptk+OhoQTHc7YY///+dYyoI+xpwjfx8DWnB+r9hMLeUH3WWy\n" +
            "kj0OLvgtAgMBAAECggEBAJ/njP/QfCUlQK11daMCyG7m4of3mTNEUJy+lguybuOX\n" +
            "hp/pdSLRbOpLeAWCUoZn04F0Gnbie37j56kTeiECn1oOuZVQ8CQUAGMHFXeqnLWr\n" +
            "B2Dv9RJammz6ZGpBExom6DmlrleEuSY+REcQ67g4IQEdJNM0EqeMLjddJOL6MzPL\n" +
            "maiTAXKXQE/TJ+WoqWJjPBWNjS2CFZ2ZVe4RreMdrpcYpaJqGDjryfYb31M3XgID\n" +
            "DUg8SrN3QTyp169xv5ShMQf0lvUzM0v7JKCPOJz00J6BIlko7M8E5QrrpX5vKBPm\n" +
            "aLjMAZiUXqKeRKrqx8nyDkeXH89jOai9fDbXIrjIMcECgYEA2xL5xdL/2v5pE5+Z\n" +
            "5WCs/vatDfL/OScmHhEObxJMQgyCpX9RDFLbtPV2Tp3E3Clq1mv162ud5hdTl3sw\n" +
            "6mNZiMl0ywBaIX9hmDs1C0caMqgupWFAH4X4L7HKYxuWuX8Zc85fVSwkvAul8Z/n\n" +
            "AP1oopt9/tr7dhlk2JqG79KqToUCgYEAx5VoxcZ02nyexrz9QC8iePJi+dBCk41j\n" +
            "962AOH6K1nlxHhtUpSLjhfhmQytBogqeOq6taup0oINqXK7gkBYo8N43mT+3BxBG\n" +
            "SSLVm6oe7SnoGRCMmvrTXGvhuTNB4meu9AB5qtz9y4LSZd4TWpN1hVEtZVwwjSwK\n" +
            "O7egYsUZF4kCgYAYwaY0MsnzB3j8XLbZcvlZvqqDvwgUf1YRY8AL0TeI5z0EUsTn\n" +
            "7Z0ib/K0o6ORdaS7Imwh9IFddRlfLu6yLhP6HznxjtKgonJ274npAln1i4fp7HmA\n" +
            "lnUbfq3uUTnRMInBynp/d4OXNIHGMlm6hOiEJ6ovWLO9/tSknaHODDJuuQKBgQCd\n" +
            "hKIoVbUsK2b023vVCF5grozHNUMWSWU4uUqVS+ov07EfXdq0apC/z4iGQsGOLmsr\n" +
            "TWm5FEnv1UhA1hSOI680S23+GXdOr92JI2ELdnzqGUUBYzpVptjh/JMuI/I/yi0e\n" +
            "jrF31OrJj/qWQ2tq9S6vhZ4afWQkRzncX9TJn11qYQKBgEN58qeYtpzi8uIF/Qzf\n" +
            "2xu+iBbn9QflppWCUZfJ/5ljY70rZF77e5clci1ev4h8Fw0NSydGKs18nSM/OZ+9\n" +
            "qmjvH7NIA/frLoKngQ8pEH9H1PF6wRCJ1lQLslx7rk/TM/jiMF1C5hq46OQEhMzH\n" +
            "PO4IrAyqGoNaX5QRIFrzsMOw";

//    @Required
//    @JsonProperty("private_key")
//    private String privateKey = "eb1f3a45f0b337a92672833d45ce0335";

    /**
     * 证书序列号
     */
    @Required
    @JsonProperty("serial_no")
    private String serialNo = "3379184CB45875E7A3940AA592489C9029E3FE9E";


    public String getCombineAppid() {
        return combineAppid;
    }

    public void setCombineAppid(String combineAppid) {
        this.combineAppid = combineAppid;
    }

    public String getCombineMchid() {
        return combineMchid;
    }

    public void setCombineMchid(String combineMchid) {
        this.combineMchid = combineMchid;
    }

    public String getPrivateKey() {
        return privateKey;
    }

    public void setPrivateKey(String privateKey) {
        this.privateKey = privateKey;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }
}
