package com.qiguliuxing.dts.xcx.dao;

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

	private Integer shopId;

	private String shopName;

	private String shopCommpany;

	private Integer couponCount;

	private List<DtsCart> cartList;

	private BigDecimal bandGoodsTotalPrice;

	private BigDecimal bandFreightPrice;

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
