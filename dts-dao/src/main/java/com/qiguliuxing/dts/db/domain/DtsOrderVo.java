package com.qiguliuxing.dts.db.domain;

import com.qiguliuxing.dts.db.util.OrderHandleOption;

import java.io.Serializable;
import java.util.List;

public class DtsOrderVo implements Serializable {
    private static final long serialVersionUID = -7908381028314100446L;

    private Integer type;

    private DtsOrder dtsOrder;

    private String orderStatusText;

    private OrderHandleOption orderHandleOption;

    private List<DtsOrderGoods> goodsList;

    private Boolean isShopConsumption;

    private String userName;

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public DtsOrder getDtsOrder() {
        return dtsOrder;
    }

    public void setDtsOrder(DtsOrder dtsOrder) {
        this.dtsOrder = dtsOrder;
    }

    public String getOrderStatusText() {
        return orderStatusText;
    }

    public void setOrderStatusText(String orderStatusText) {
        this.orderStatusText = orderStatusText;
    }

    public OrderHandleOption getOrderHandleOption() {
        return orderHandleOption;
    }

    public void setOrderHandleOption(OrderHandleOption orderHandleOption) {
        this.orderHandleOption = orderHandleOption;
    }

    public List<DtsOrderGoods> getGoodsList() {
        return goodsList;
    }

    public void setGoodsList(List<DtsOrderGoods> goodsList) {
        this.goodsList = goodsList;
    }

    public Boolean getIsShopConsumption() {
        return isShopConsumption;
    }

    public void setIsShopConsumption(Boolean isShopConsumption) {
        this.isShopConsumption = isShopConsumption;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
