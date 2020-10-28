package com.qiguliuxing.dts.core.wxpay;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import me.chanjar.weixin.common.annotation.Required;

import java.io.Serializable;

public class WxPayAmount implements Serializable {

    /**
     * 子单金额，单位为分。
     * 境外场景下，标价金额要超过商户结算币种的最小单位金额，例如结算币种为美元，则标价金额必须大于1美分
     */
    @Required
    @XStreamAlias("total_amount")
    private Integer totalAmount;

    /**
     * 符合ISO 4217标准的三位字母代码，人民币：CNY
     */
    @Required
    @XStreamAlias("currency")
    private String currency;

    public int getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Integer totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }
}
