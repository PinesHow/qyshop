package com.qiguliuxing.dts.db.domain;


import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Date;

/**
 * 管理员表
 * 
 * @author mh
 * @email software8888@163.com
 * @date 2020-06-30 16:22:33
 */
public class DtsAdminEntity implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 
	 */
	private Integer id;
	/**
	 * 管理员名称
	 */
	private String username;
	/**
	 * 管理员密码
	 */
	private String password;
	/**
	 * 最近一次登录IP地址
	 */
	private String lastLoginIp;
	/**
	 * 最近一次登录时间
	 */
	private Date lastLoginTime;
	/**
	 * 头像图片
	 */
	private String avatar;
	/**
	 * 创建时间
	 */
	private LocalDateTime addTime;
	/**
	 * 更新时间
	 */
	private LocalDateTime updateTime;
	/**
	 * 逻辑删除
	 */
	private Integer deleted;
	/**
	 * 角色列表
	 */
	private String roleIds;
	/**
	 * 用户描述
	 */
	private String desc;
	/**
	 * 联系电话
	 */
	private String tel;
	/**
	 * 邮箱地址
	 */
	private String mail;
	/**
	 * 商铺id
	 */
	private Integer shopId;

	private String qrCode;

	public static long getSerialVersionUID() {
		return serialVersionUID;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	public String getLastLoginIp() {
		return lastLoginIp;
	}

	public void setLastLoginIp(String lastLoginIp) {
		this.lastLoginIp = lastLoginIp;
	}

	public Date getLastLoginTime() {
		return lastLoginTime;
	}

	public void setLastLoginTime(Date lastLoginTime) {
		this.lastLoginTime = lastLoginTime;
	}

	public String getAvatar() {
		return avatar;
	}

	public void setAvatar(String avatar) {
		this.avatar = avatar;
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

	public Integer getDeleted() {
		return deleted;
	}

	public void setDeleted(Integer deleted) {
		this.deleted = deleted;
	}

	public String getRoleIds() {
		return roleIds;
	}

	public void setRoleIds(String roleIds) {
		this.roleIds = roleIds;
	}

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public String getTel() {
		return tel;
	}

	public void setTel(String tel) {
		this.tel = tel;
	}

	public String getMail() {
		return mail;
	}

	public void setMail(String mail) {
		this.mail = mail;
	}

	public Integer getShopId() {
		return shopId;
	}

	public void setShopId(Integer shopId) {
		this.shopId = shopId;
	}

	public String getQrCode() {
		return qrCode;
	}

	public void setQrCode(String qrCode) {
		this.qrCode = qrCode;
	}
}
