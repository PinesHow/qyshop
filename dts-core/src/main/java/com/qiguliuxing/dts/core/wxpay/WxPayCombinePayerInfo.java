package com.qiguliuxing.dts.core.wxpay;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import me.chanjar.weixin.common.annotation.Required;

import java.io.Serializable;

public class WxPayCombinePayerInfo implements Serializable {

    @Required
    @XStreamAlias("openid")
    private String openId;

    public String getOpenId() {
        return openId;
    }

    public void setOpenId(String openId) {
        this.openId = openId;
    }
}
