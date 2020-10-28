package com.qiguliuxing.dts.db.domain;


import java.io.Serializable;

/**
 * 
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2020-06-29 15:20:24
 */
public class DtsShopHomeEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 店铺信息id
	 */
	private Integer id;
	/**
	 * 所属店铺id
	 */
	private Integer shopId;
	/**
	 * 服务电话
	 */
	private String servicePhone;
	/**
	 * 联系人名称
	 */
	private String contactPerson;
	/**
	 * 店铺地址
	 */
	private String shopAddress;

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

	public String getServicePhone() {
		return servicePhone;
	}

	public void setServicePhone(String servicePhone) {
		this.servicePhone = servicePhone;
	}

	public String getContactPerson() {
		return contactPerson;
	}

	public void setContactPerson(String contactPerson) {
		this.contactPerson = contactPerson;
	}

	public String getShopAddress() {
		return shopAddress;
	}

	public void setShopAddress(String shopAddress) {
		this.shopAddress = shopAddress;
	}
}
