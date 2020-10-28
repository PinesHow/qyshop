package com.qiguliuxing.dts.db.domain;


import java.io.Serializable;

/**
 * 
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2020-06-29 15:20:24
 */
public class DtsShopApplyStatusEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 店铺状态id
	 */
	private Integer id;
	/**
	 * 店铺状态名称
	 */
	private String shopApplyStatus;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getShopApplyStatus() {
		return shopApplyStatus;
	}

	public void setShopApplyStatus(String shopApplyStatus) {
		this.shopApplyStatus = shopApplyStatus;
	}
}
