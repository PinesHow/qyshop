package com.qiguliuxing.dts.db.dao;

import com.qiguliuxing.dts.db.domain.DtsShop;
import com.qiguliuxing.dts.db.domain.DtsShopEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 店铺表
 * 
 * @author mh
 * @email software8888@163.com
 * @date 2020-06-29 15:20:24
 */
@Mapper
public interface DtsShopDao{

    @Insert("insert into dts_shop (shop_name,contact_phone,phone_num,address,legal_person,shop_type) " +
            "values(#{shop.shopName},#{shop.contactPhone},#{shop.phoneNum},#{shop.address},#{shop.legalPerson},#{shop.shopType})")
    int insert(@Param("shop") DtsShopEntity dtsShop);

    @Select("select * from dts_shop where phone_num=#{tell}")
    DtsShopEntity selectByTel(@Param("tell") String tel);

    DtsShop getShopColumnInfo(@Param("shopId") Integer shopId);

}
