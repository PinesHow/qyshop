package com.qiguliuxing.dts.db.domain;

import java.util.ArrayList;
import java.util.Arrays;

public class DtsShop {

    public static final Boolean NOT_DELETED = false;

    public static final Boolean IS_DELETED = true;

    private Integer id;

    private String shopName;

    private String shopIntroduction;

    private String businessLicense;

    private String shopKeeperNum;

    private String logoUrl;

    private String address;

    private String idCardPositive;

    private String idCardObverse;

    private String businessLicenseNum;

    private String businessLicenseStart;

    private String businessLicenseEnd;

    private String businessLicenseUrl;

    private Integer shopType;

    private String contactPhone;

    private String phoneNum;

    private String legalPerson;

    private Integer categoryId;

    private String shopPic;

    private Boolean isPopularity;

    private Boolean isHot;

    private Integer isPopularityValue;
    /**
     * 是否开启订单短信通知
     */
    private Boolean receiveSms;

    /**
     * 商户号
     * @return
     */
    private String subMchId;

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

    public String getLegalPerson() {
        return legalPerson;
    }

    public void setLegalPerson(String legalPerson) {
        this.legalPerson = legalPerson;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer categoryId) {
        this.categoryId = categoryId;
    }

    public String getShopPic() {
        return shopPic;
    }

    public void setShopPic(String shopPic) {
        this.shopPic = shopPic;
    }

    public Boolean getPopularity() {
        return isPopularity;
    }

    public void setPopularity(Boolean popularity) {
        isPopularity = popularity;
    }

    public Boolean getHot() {
        return isHot;
    }

    public void setHot(Boolean hot) {
        isHot = hot;
    }

    public Integer getIsPopularityValue() {
        return isPopularityValue;
    }

    public void setIsPopularityValue(Integer isPopularityValue) {
        this.isPopularityValue = isPopularityValue;
    }

    public Boolean getReceiveSms() {
        return receiveSms;
    }

    public void setReceiveSms(Boolean receiveSms) {
        this.receiveSms = receiveSms;
    }

    public String getSubMchId() {
        return subMchId;
    }

    public void setSubMchId(String subMchId) {
        this.subMchId = subMchId;
    }

    @Override
    public String toString() {
        return "DtsShop {" +
                "id=" + id +
                ", shopName='" + shopName + '\'' +
                ", shopIntroduction='" + shopIntroduction + '\'' +
                ", businessLicense='" + businessLicense + '\'' +
                ", shopKeeperNum='" + shopKeeperNum + '\'' +
                ", logoUrl='" + logoUrl + '\'' +
                ", address='" + address + '\'' +
                ", idCardPositive='" + idCardPositive + '\'' +
                ", idCardObverse='" + idCardObverse + '\'' +
                ", businessLicenseNum='" + businessLicenseNum + '\'' +
                ", businessLicenseStart='" + businessLicenseStart + '\'' +
                ", businessLicenseEnd='" + businessLicenseEnd + '\'' +
                ", businessLicenseUrl='" + businessLicenseUrl + '\'' +
                ", shopType=" + shopType +
                ", contactPhone='" + contactPhone + '\'' +
                ", phoneNum='" + phoneNum + '\'' +
                ", legalPerson='" + legalPerson + '\'' +
                ", categoryId=" + categoryId +
                ", shopPic='" + shopPic + '\'' +
                ", isPopularity=" + isPopularity +
                ", isHot=" + isHot +
                ", isPopularityValue=" + isPopularityValue +
                ", receiveSms=" + receiveSms +
                ", subMchId=" + subMchId +
                '}';
    }

    public enum Column {
        id("id", "id", "INTEGER", true),
        shopName("shop_name", "shopName", "VARCHAR", false),
        shopIntroduction("shop_introduction", "shopIntroduction", "VARCHAR", false),
        businessLicense("business_license", "businessLicense", "VARCHAR", false),
        shopKeeperNum("shop_keeper_num", "shopKeeperNum", "VARCHAR", false),
        logoUrl("logo_url", "logoUrl", "VARCHAR", false),
        address("address", "address", "VARCHAR", false),
        idCardPositive("id_card_positive", "idCardPositive", "VARCHAR", false),
        idCardObverse("id_card_obverse", "idCardObverse", "VARCHAR", false),
        businessLicenseNum("business_license_num", "businessLicenseNum", "VARCHAR", false),
        businessLicenseStart("business_license_start", "businessLicenseStart", "VARCHAR", false),
        businessLicenseEnd("business_license_end", "businessLicenseEnd", "VARCHAR", false),
        businessLicenseUrl("business_license_url", "businessLicenseUrl", "VARCHAR", false),
        shopType("shop_type", "shopType", "INTEGER", false),
        contactPhone("contact_phone", "contactPhone", "VARCHAR", false),
        phoneNum("phone_num", "phoneNum", "VARCHAR", false),
        legalPerson("legal_person", "legalPerson", "VARCHAR", false),
        categoryId("category_id", "categoryId", "INTEGER", false),
        shopPic("shop_pic", "shopPic", "VARCHAR", false),
        isPopularity("is_popularity", "isPopularity", "BIT", false),
        isHot("is_hot", "isHot", "BIT", false),
        isPopularityValue("is_popularity_value", "isPopularityValue", "INTEGER", false),
        receiveSms("receive_sms","receiveSms","BIT",false),
        subMchId("sub_mch_id","subMchId","VARCHAR",true);
        /**
         * This field was generated by MyBatis Generator.
         * This field corresponds to the database table dts_groupon
         *
         * @mbg.generated
         * @project https://github.com/itfsw/mybatis-generator-plugin
         */
        private static final String BEGINNING_DELIMITER = "`";

        /**
         * This field was generated by MyBatis Generator.
         * This field corresponds to the database table dts_groupon
         *
         * @mbg.generated
         * @project https://github.com/itfsw/mybatis-generator-plugin
         */
        private static final String ENDING_DELIMITER = "`";

        /**
         * This field was generated by MyBatis Generator.
         * This field corresponds to the database table dts_groupon
         *
         * @mbg.generated
         * @project https://github.com/itfsw/mybatis-generator-plugin
         */
        private final String column;

        /**
         * This field was generated by MyBatis Generator.
         * This field corresponds to the database table dts_groupon
         *
         * @mbg.generated
         * @project https://github.com/itfsw/mybatis-generator-plugin
         */
        private final boolean isColumnNameDelimited;

        /**
         * This field was generated by MyBatis Generator.
         * This field corresponds to the database table dts_groupon
         *
         * @mbg.generated
         * @project https://github.com/itfsw/mybatis-generator-plugin
         */
        private final String javaProperty;

        /**
         * This field was generated by MyBatis Generator.
         * This field corresponds to the database table dts_groupon
         *
         * @mbg.generated
         * @project https://github.com/itfsw/mybatis-generator-plugin
         */
        private final String jdbcType;

        /**
         * This method was generated by MyBatis Generator.
         * This method corresponds to the database table dts_groupon
         *
         * @mbg.generated
         * @project https://github.com/itfsw/mybatis-generator-plugin
         */
        public String value() {
            return this.column;
        }

        /**
         * This method was generated by MyBatis Generator.
         * This method corresponds to the database table dts_groupon
         *
         * @mbg.generated
         * @project https://github.com/itfsw/mybatis-generator-plugin
         */
        public String getValue() {
            return this.column;
        }

        /**
         * This method was generated by MyBatis Generator.
         * This method corresponds to the database table dts_groupon
         *
         * @mbg.generated
         * @project https://github.com/itfsw/mybatis-generator-plugin
         */
        public String getJavaProperty() {
            return this.javaProperty;
        }

        /**
         * This method was generated by MyBatis Generator.
         * This method corresponds to the database table dts_groupon
         *
         * @mbg.generated
         * @project https://github.com/itfsw/mybatis-generator-plugin
         */
        public String getJdbcType() {
            return this.jdbcType;
        }

        /**
         * This method was generated by MyBatis Generator.
         * This method corresponds to the database table dts_groupon
         *
         * @mbg.generated
         * @project https://github.com/itfsw/mybatis-generator-plugin
         */
        Column(String column, String javaProperty, String jdbcType, boolean isColumnNameDelimited) {
            this.column = column;
            this.javaProperty = javaProperty;
            this.jdbcType = jdbcType;
            this.isColumnNameDelimited = isColumnNameDelimited;
        }

        /**
         * This method was generated by MyBatis Generator.
         * This method corresponds to the database table dts_groupon
         *
         * @mbg.generated
         * @project https://github.com/itfsw/mybatis-generator-plugin
         */
        public String desc() {
            return this.getEscapedColumnName() + " DESC";
        }

        /**
         * This method was generated by MyBatis Generator.
         * This method corresponds to the database table dts_groupon
         *
         * @mbg.generated
         * @project https://github.com/itfsw/mybatis-generator-plugin
         */
        public String asc() {
            return this.getEscapedColumnName() + " ASC";
        }

        /**
         * This method was generated by MyBatis Generator.
         * This method corresponds to the database table dts_groupon
         *
         * @mbg.generated
         * @project https://github.com/itfsw/mybatis-generator-plugin
         */
        public static DtsGroupon.Column[] excludes(DtsGroupon.Column... excludes) {
            ArrayList<DtsGroupon.Column> columns = new ArrayList<>(Arrays.asList(DtsGroupon.Column.values()));
            if (excludes != null && excludes.length > 0) {
                columns.removeAll(new ArrayList<>(Arrays.asList(excludes)));
            }
            return columns.toArray(new DtsGroupon.Column[]{});
        }

        /**
         * This method was generated by MyBatis Generator.
         * This method corresponds to the database table dts_groupon
         *
         * @mbg.generated
         * @project https://github.com/itfsw/mybatis-generator-plugin
         */
        public String getEscapedColumnName() {
            if (this.isColumnNameDelimited) {
                return new StringBuilder().append(BEGINNING_DELIMITER).append(this.column).append(ENDING_DELIMITER).toString();
            } else {
                return this.column;
            }
        }
    }
}
