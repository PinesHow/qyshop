package com.qiguliuxing.dts.db.dao;

import com.qiguliuxing.dts.db.domain.DtsAdminEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

/**
 * 管理员表
 * 
 * @author mh
 * @email software8888@163.com
 * @date 2020-06-30 16:22:33
 */
@Mapper
public interface DtsAdminDao {

    @Insert("insert into dts_admin(username,tel,password,shop_id,qr_code,role_ids,add_time,update_time) values(#{ad.username},#{ad.tel},#{ad.password},#{ad.shopId},#{ad.qrCode},#{ad.roleIds},#{ad.addTime},#{ad.updateTime})")
    int insert(@Param("ad")DtsAdminEntity adminEntity);

    @Select("select * from dts_admin where tel=#{tel}")
    DtsAdminEntity selectOne(@Param("tel") String tel);
}
