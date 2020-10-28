package com.qiguliuxing.dts.db.domain;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 轮播图表
 * 
 * @author chenshun
 * @email sunlightcs@gmail.com
 * @date 2020-06-29 15:20:24
 */
public class DtsShopCarouselEntity implements Serializable {
	private static final long serialVersionUID = 1L;
	/**
	 * 轮播图id
	 */
	private Integer id;
	/**
	 * 广告标题
	 */
	private String name;
	/**
	 * 广告的商品页面或活动页面链接地址
	 */
	private String link;
	/**
	 * 图片url
	 */
	private String url;
	/**
	 * 广告内容
	 */
	private String content;
	/**
	 * 是否启用
	 */
	private Boolean enabled;
	/**
	 * 广告创建时间
	 */
	private LocalDateTime addTime;
	/**
	 * 广告更新时间
	 */
	private LocalDateTime updateTime;
	/**
	 * 店铺Id
	 */
	private Integer shopId;
	/**
	 * 逻辑删除
	 */
	private Boolean deleted;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Boolean getEnabled() {
		return enabled;
	}

	public void setEnabled(Boolean enabled) {
		this.enabled = enabled;
	}

	public LocalDateTime getAddTime() {
		return addTime;
	}

	public void setAddTime(LocalDateTime addTime) {
		this.addTime = addTime;
	}

	public LocalDateTime getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(LocalDateTime updateTime) {
		this.updateTime = updateTime;
	}

	public Integer getShopId() {
		return shopId;
	}

	public void setShopId(Integer shopId) {
		this.shopId = shopId;
	}

	public Boolean getDeleted() {
		return deleted;
	}

	public void setDeleted(Boolean deleted) {
		this.deleted = deleted;
	}
}
