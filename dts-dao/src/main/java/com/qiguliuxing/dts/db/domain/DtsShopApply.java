package com.qiguliuxing.dts.db.domain;

import java.time.LocalDateTime;

public class DtsShopApply {

    private Integer id;

    private Integer shopId;

    private Integer checkStatusId;

    private LocalDateTime addTime;

    private LocalDateTime updateTime;

    private String approveMsg;

    private String shopMsg;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getShopId() {
        return shopId;
    }

    public void setShopId(Integer shopId) {
        this.shopId = shopId;
    }

    public Integer getCheckStatusId() {
        return checkStatusId;
    }

    public void setCheckStatusId(Integer checkStatusId) {
        this.checkStatusId = checkStatusId;
    }

    public LocalDateTime getAddTime() {
        return addTime;
    }

    public void setAddTime(LocalDateTime addTime) {
        this.addTime = addTime;
    }

    public LocalDateTime getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(LocalDateTime updateTime) {
        this.updateTime = updateTime;
    }

    public String getApproveMsg() {
        return approveMsg;
    }

    public void setApproveMsg(String approveMsg) {
        this.approveMsg = approveMsg;
    }

    public String getShopMsg() {
        return shopMsg;
    }

    public void setShopMsg(String shopMsg) {
        this.shopMsg = shopMsg;
    }
}
