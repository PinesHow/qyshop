package com.qiguliuxing.dts.wx.dao;

import com.qiguliuxing.dts.db.domain.DtsOrderGoods;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

/**
 * 用于拆单时存储临时的订单商品
 * 
 * @author CHENBO
 * @QQ 623659388
 * @since 1.0.0
 */
public class BrandOrderGoods implements Serializable {

	private static final long serialVersionUID = 4756437344642762485L;

	private Integer shopId;

	private List<DtsOrderGoods> orderGoodsList;

	private BigDecimal bandGoodsTotalPrice;

	private BigDecimal bandFreightPrice;

	private String subMchId;

	public Integer getShopId() {
		return shopId;
	}

	public void setShopId(Integer shopId) {
		this.shopId = shopId;
	}

	public List<DtsOrderGoods> getOrderGoodsList() {
		return orderGoodsList;
	}

	public void setOrderGoodsList(List<DtsOrderGoods> orderGoodsList) {
		this.orderGoodsList = orderGoodsList;
	}

	public BigDecimal getBandGoodsTotalPrice() {
		return bandGoodsTotalPrice;
	}

	public void setBandGoodsTotalPrice(BigDecimal bandGoodsTotalPrice) {
		this.bandGoodsTotalPrice = bandGoodsTotalPrice;
	}

	public BigDecimal getBandFreightPrice() {
		return bandFreightPrice;
	}

	public void setBandFreightPrice(BigDecimal bandFreightPrice) {
		this.bandFreightPrice = bandFreightPrice;
	}

	public String getSubMchId() {
		return subMchId;
	}

	public void setSubMchId(String subMchId) {
		this.subMchId = subMchId;
	}
}
