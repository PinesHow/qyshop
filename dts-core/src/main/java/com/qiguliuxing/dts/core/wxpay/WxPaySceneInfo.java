package com.qiguliuxing.dts.core.wxpay;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import me.chanjar.weixin.common.annotation.Required;

import java.io.Serializable;

public class WxPaySceneInfo implements Serializable {

    @Required
    @XStreamAlias("device_id")
    private String deviceId;

    @Required
    @XStreamAlias("payer_client_ip")
    private String payerClientIp;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getPayerClientIp() {
        return payerClientIp;
    }

    public void setPayerClientIp(String payerClientIp) {
        this.payerClientIp = payerClientIp;
    }
}
