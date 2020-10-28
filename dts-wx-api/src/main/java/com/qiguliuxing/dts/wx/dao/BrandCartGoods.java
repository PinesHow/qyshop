package com.qiguliuxing.dts.wx.dao;

import com.qiguliuxing.dts.db.domain.DtsCart;
import com.qiguliuxing.dts.db.domain.DtsShop;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 用于存储 品牌入驻商购物车商品的对象
 * 用于存储 商铺入驻  购物车商品的对象
 * @author CHENBO
 * @QQ 623659388
 * @since 1.0.0
 */
public class BrandCartGoods implements Serializable {

	private static final long serialVersionUID = -7908381028314100456L;

	private static final Integer DEFAULT_BRAND_ID = 1001000;

	private static final String DEFAULT_BRAND_COMMPANY = "青岩自营店";

	private static final String DEFAULT_BRAND_NAME = "青岩自营店";

	private Integer shopId;//店铺id

	private String shopName;//店铺名称

	private String shopCommpany;

	private Integer couponCount;//优惠券数量

	private List<DtsCart> cartList;//购物车集合

	private BigDecimal bandGoodsTotalPrice;//商品总价格

	private BigDecimal bandCouponPrice;//优惠券优惠价格

	private BigDecimal bandFreightPrice;//运费

	private Integer couponId;//优惠券id

	private String couponName;//优惠券名称

	public Integer getShopId() {
		return shopId;
	}

	public void setShopId(Integer shopId) {
		this.shopId = shopId;
	}

	public String getShopName() {
		return shopName;
	}

	public void setShopName(String shopName) {
		this.shopName = shopName;
	}

	public String getShopCommpany() {
		return shopCommpany;
	}

	public void setShopCommpany(String shopCommpany) {
		this.shopCommpany = shopCommpany;
	}

	public List<DtsCart> getCartList() {
		return cartList;
	}

	public void setCartList(List<DtsCart> cartList) {
		this.cartList = cartList;
	}

	public BigDecimal getBandGoodsTotalPrice() {
		return bandGoodsTotalPrice;
	}

	public void setBandGoodsTotalPrice(BigDecimal bandGoodsTotalPrice) {
		this.bandGoodsTotalPrice = bandGoodsTotalPrice;
	}

	public BigDecimal getBandCouponPrice() {
		return bandCouponPrice;
	}

	public void setBandCouponPrice(BigDecimal bandCouponPrice) {
		this.bandCouponPrice = bandCouponPrice;
	}

	public BigDecimal getBandFreightPrice() {
		return bandFreightPrice;
	}

	public void setBandFreightPrice(BigDecimal bandFreightPrice) {
		this.bandFreightPrice = bandFreightPrice;
	}

	public Integer getCouponCount() {
		return couponCount;
	}

	public void setCouponCount(Integer couponCount) {
		this.couponCount = couponCount;
	}

	public Integer getCouponId() {
		return couponId;
	}

	public void setCouponId(Integer couponId) {
		this.couponId = couponId;
	}

	public String getCouponName() {
		return couponName;
	}

	public void setCouponName(String couponName) {
		this.couponName = couponName;
	}

	public static BrandCartGoods init(DtsShop dtsShop) {
		BrandCartGoods bcg = new BrandCartGoods();
		if (dtsShop != null) {
			bcg.setShopId(dtsShop.getId());
			bcg.setShopCommpany(null);
			bcg.setShopName(dtsShop.getShopName());
		} else {
			bcg.setShopId(DEFAULT_BRAND_ID);
			bcg.setShopCommpany(DEFAULT_BRAND_COMMPANY);
			bcg.setShopName(DEFAULT_BRAND_NAME);
		}
		List<DtsCart> dtsCartList = new ArrayList<DtsCart>();
		bcg.setCartList(dtsCartList);
		return bcg;
	}

}
