package com.qiguliuxing.dts.admin.form;

/**
 * @ClassName: RegForm
 * @Description:
 * @author: mh
 * @date: 2020/6/30
 * @Version: 1.0
 */


import javax.validation.constraints.NotBlank;

/**
 * 注册表单
 *
 * @author Mark software8888@163.com
 */
public class RegForm {

    @NotBlank(message="店铺名称不能为空")
    private String shopName;


    @NotBlank(message="店铺简介不能为空")
    private String shopIntroduction;


    @NotBlank(message="法人姓名不能为空")
    private String legalPerson;


    @NotBlank(message="店主身份证号不能为空")
    private String shopKeeperNum;


    @NotBlank(message="联系电话不能为空")
    private String contactPhone;


    @NotBlank(message="店铺类型不能为空")
    private Integer shopTypeId;

    @NotBlank(message="店铺地址不能为空")
    private String address;

    @NotBlank(message="手机号码不能为空")
    private String phoneNum;

    @NotBlank(message="验证码不能为空")
    private String code;

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

    public String getContactPhone() {
        return contactPhone;
    }

    public void setContactPhone(String contactPhone) {
        this.contactPhone = contactPhone;
    }

    public Integer getShopTypeId() {
        return shopTypeId;
    }

    public void setShopTypeId(Integer shopTypeId) {
        this.shopTypeId = shopTypeId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
