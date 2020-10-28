package com.qiguliuxing.dts.db.domain;


import java.io.Serializable;

/**
 * 店铺类型表
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2020-06-29 15:20:24
 */
public class DtsShopTypeEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 店铺类型id
	 */
	private Integer id;
	/**
	 * 店铺类型名称
	 */
	private String shopTypeName;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getShopTypeName() {
		return shopTypeName;
	}

	public void setShopTypeName(String shopTypeName) {
		this.shopTypeName = shopTypeName;
	}
}
