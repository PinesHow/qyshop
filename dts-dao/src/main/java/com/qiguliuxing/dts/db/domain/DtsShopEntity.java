package com.qiguliuxing.dts.db.domain;

import java.io.Serializable;

/**
 * 店铺表
 * 
 * @author mh
 * @email software8888@163.com
 * @date 2020-06-29 15:20:24
 */
public class DtsShopEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 商铺id
	 */
	private Integer id;
	/**
	 * 商铺名称
	 */
	private String shopName;
	/**
	 * 店铺简介
	 */
	private String shopIntroduction;
	/**
	 * 营业执照url
	 */
	private String businessLicense;

	/**
	 * 法人姓名
	 */
	private String legalPerson;
	/**
	 * 店主身份证号
	 */
	private String shopKeeperNum;
	/**
	 * logourl
	 */
	private String logoUrl;
	/**
	 * 店铺地址
	 */
	private String address;
	/**
	 * 身份证正面
	 */
	private String idCardPositive;
	/**
	 * 身份证反面
	 */
	private String idCardObverse;
	/**
	 * 营业执照编号
	 */
	private String businessLicenseNum;
	/**
	 * 营业执照开始时间
	 */
	private String businessLicenseStart;
	/**
	 * 营业执照结束时间
	 */
	private String businessLicenseEnd;
	/**
	 * 营业执照url
	 */
	private String businessLicenseUrl;
	/**
	 * 商铺类型(普通商户,供应商,平台自营)
	 */
	private Integer shopType;
	/**
	 * 联系电话
	 */
	private String contactPhone;
	/**
	 * 手机号码
	 */
	private String phoneNum;

	/**
	 * 管理员名称
	 */
	private String username;

	/**
	 * 管理员密码
	 */
	private String password;

	/**
	 * 是否开启订单通知
	 */
	private Boolean receiveSms;

	public Boolean getReceiveSms() {
		return receiveSms;
	}

	public void setReceiveSms(Boolean receiveSms) {
		this.receiveSms = receiveSms;
	}

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getShopName() {
		return shopName;
	}

	public void setShopName(String shopName) {
		this.shopName = shopName;
	}

	public String getShopIntroduction() {
		return shopIntroduction;
	}

	public void setShopIntroduction(String shopIntroduction) {
		this.shopIntroduction = shopIntroduction;
	}

	public String getBusinessLicense() {
		return businessLicense;
	}

	public void setBusinessLicense(String businessLicense) {
		this.businessLicense = businessLicense;
	}

	public String getLegalPerson() {
		return legalPerson;
	}

	public void setLegalPerson(String legalPerson) {
		this.legalPerson = legalPerson;
	}

	public String getShopKeeperNum() {
		return shopKeeperNum;
	}

	public void setShopKeeperNum(String shopKeeperNum) {
		this.shopKeeperNum = shopKeeperNum;
	}

	public String getLogoUrl() {
		return logoUrl;
	}

	public void setLogoUrl(String logoUrl) {
		this.logoUrl = logoUrl;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getIdCardPositive() {
		return idCardPositive;
	}

	public void setIdCardPositive(String idCardPositive) {
		this.idCardPositive = idCardPositive;
	}

	public String getIdCardObverse() {
		return idCardObverse;
	}

	public void setIdCardObverse(String idCardObverse) {
		this.idCardObverse = idCardObverse;
	}

	public String getBusinessLicenseNum() {
		return businessLicenseNum;
	}

	public void setBusinessLicenseNum(String businessLicenseNum) {
		this.businessLicenseNum = businessLicenseNum;
	}

	public String getBusinessLicenseStart() {
		return businessLicenseStart;
	}

	public void setBusinessLicenseStart(String businessLicenseStart) {
		this.businessLicenseStart = businessLicenseStart;
	}

	public String getBusinessLicenseEnd() {
		return businessLicenseEnd;
	}

	public void setBusinessLicenseEnd(String businessLicenseEnd) {
		this.businessLicenseEnd = businessLicenseEnd;
	}

	public String getBusinessLicenseUrl() {
		return businessLicenseUrl;
	}

	public void setBusinessLicenseUrl(String businessLicenseUrl) {
		this.businessLicenseUrl = businessLicenseUrl;
	}

	public Integer getShopType() {
		return shopType;
	}

	public void setShopType(Integer shopType) {
		this.shopType = shopType;
	}

	public String getContactPhone() {
		return contactPhone;
	}

	public void setContactPhone(String contactPhone) {
		this.contactPhone = contactPhone;
	}

	public String getPhoneNum() {
		return phoneNum;
	}

	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
}
