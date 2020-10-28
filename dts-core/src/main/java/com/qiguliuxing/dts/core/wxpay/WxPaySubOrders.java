package com.qiguliuxing.dts.core.wxpay;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import me.chanjar.weixin.common.annotation.Required;

import java.io.Serializable;

public class WxPaySubOrders implements Serializable {

    @Required
    @XStreamAlias("mchid")
    private String mchId;

    @Required
    @XStreamAlias("attach")
    private String attach;

    @Required
    @XStreamAlias("amount")
    private String amount;

    @Required
    @XStreamAlias("out_trade_no")
    private String outTradeNo;

    @Required
    @XStreamAlias("sub_mchid")
    private String subMchid;

    @Required
    @XStreamAlias("description")
    private String description;

    @Required
    @XStreamAlias("settle_info")
    private String settleInfo;

    public String getMchId() {
        return mchId;
    }

    public void setMchId(String mchId) {
        this.mchId = mchId;
    }

    public String getAttach() {
        return attach;
    }

    public void setAttach(String attach) {
        this.attach = attach;
    }

    public String getAmount() {
        return amount;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getOutTradeNo() {
        return outTradeNo;
    }

    public void setOutTradeNo(String outTradeNo) {
        this.outTradeNo = outTradeNo;
    }

    public String getSubMchid() {
        return subMchid;
    }

    public void setSubMchid(String subMchid) {
        this.subMchid = subMchid;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSettleInfo() {
        return settleInfo;
    }

    public void setSettleInfo(String settleInfo) {
        this.settleInfo = settleInfo;
    }
}
