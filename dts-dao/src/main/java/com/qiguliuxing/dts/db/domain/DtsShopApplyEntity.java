package com.qiguliuxing.dts.db.domain;

import java.io.Serializable;

/**
 * 
 * 
 * @author mh
 * @email software8888@163.com
 * @date 2020-06-29 15:20:24
 */
public class DtsShopApplyEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 店铺审批id
	 */
	private Integer id;
	/**
	 * 店铺id
	 */
//	@NotBlank(message = "修改店铺id不能为空!",groups = {UpdateGroup.class}

	private Integer shopId;
	/**
	 * 审批状态
	 */
	private Integer checkStatusId;

	private DtsShopEntity dtsShopEntity;

	private String applyStatus;

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

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

	public DtsShopEntity getDtsShopEntity() {
		return dtsShopEntity;
	}

	public void setDtsShopEntity(DtsShopEntity dtsShopEntity) {
		this.dtsShopEntity = dtsShopEntity;
	}

	public String getApplyStatus() {
		return applyStatus;
	}

	public void setApplyStatus(String applyStatus) {
		this.applyStatus = applyStatus;
	}
}
