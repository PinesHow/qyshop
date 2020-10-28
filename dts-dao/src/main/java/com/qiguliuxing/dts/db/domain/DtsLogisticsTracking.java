package com.qiguliuxing.dts.db.domain;

public class DtsLogisticsTracking {

    private String mobile;//收件人/寄件人手机号（顺丰快递需要）

    private String number;//快递单号

    private String type;//快递公司 自动识别请写auto


    public String getMobile() {
        return mobile;
    }

    public void setMobile(String mobile) {
        this.mobile = mobile;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
