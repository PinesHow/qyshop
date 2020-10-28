package com.qiguliuxing.dts.core.wxpay;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import me.chanjar.weixin.common.annotation.Required;

import java.io.Serializable;

public class WxSettleInfo implements Serializable {

    @Required
    @XStreamAlias("profit_sharing")
    private Boolean profitSharing;

    @Required
    @XStreamAlias("subsidy_amount")
    private Integer subsidyAmount;

    public Boolean getProfitSharing() {
        return profitSharing;
    }

    public void setProfitSharing(Boolean profitSharing) {
        this.profitSharing = profitSharing;
    }

    public Integer getSubsidyAmount() {
        return subsidyAmount;
    }

    public void setSubsidyAmount(Integer subsidyAmount) {
        this.subsidyAmount = subsidyAmount;
    }
}
