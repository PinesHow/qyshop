package com.qiguliuxing.dts.core.wxpay;

import com.github.binarywang.wxpay.bean.request.WxPayUnifiedOrderRequest;
import com.thoughtworks.xstream.annotations.XStreamAlias;

public class WxPayUnifiedOrderRequestChildren extends WxPayUnifiedOrderRequest {

    public WxPayUnifiedOrderRequestChildren() {
        super();
    }

    @XStreamAlias("profit_sharing")
    private String profitSharing;

//    @Required
//    @XStreamAlias("combine_appid")
//    private String combineAppid;
//
//    @Required
//    @XStreamAlias("combine_mchid")
//    private String combineMchid;
//
//    @Required
//    @XStreamAlias("combine_out_trade_no")
//    private String combineOutTradeNo;
//
//    @Required
//    @XStreamAlias("scene_info")
//    private String sceneInfo;
//
//    @Required
//    @XStreamAlias("sub_orders")
//    private String subOrders;
//
//    @Required
//    @XStreamAlias("combine_payer_info")
//    private String combinePayerInfo;


//    public String getCombineAppid() {
//        return combineAppid;
//    }
//
//    public void setCombineAppid(String combineAppid) {
//        this.combineAppid = combineAppid;
//    }
//
//    public String getCombineMchid() {
//        return combineMchid;
//    }
//
//    public void setCombineMchid(String combineMchid) {
//        this.combineMchid = combineMchid;
//    }
//
//    public String getCombineOutTradeNo() {
//        return combineOutTradeNo;
//    }
//
//    public void setCombineOutTradeNo(String combineOutTradeNo) {
//        this.combineOutTradeNo = combineOutTradeNo;
//    }
//
//    public String getSceneInfo() {
//        return sceneInfo;
//    }
//
//    public void setSceneInfo(String sceneInfo) {
//        this.sceneInfo = sceneInfo;
//    }
//
//    public String getSubOrders() {
//        return subOrders;
//    }
//
//    public void setSubOrders(String subOrders) {
//        this.subOrders = subOrders;
//    }
//
//    public String getCombinePayerInfo() {
//        return combinePayerInfo;
//    }
//
//    public void setCombinePayerInfo(String combinePayerInfo) {
//        this.combinePayerInfo = combinePayerInfo;
//    }


    public String getProfitSharing() {
        return profitSharing;
    }

    public void setProfitSharing(String profitSharing) {
        this.profitSharing = profitSharing;
    }
}
