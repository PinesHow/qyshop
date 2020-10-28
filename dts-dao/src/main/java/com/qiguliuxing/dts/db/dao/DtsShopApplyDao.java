package com.qiguliuxing.dts.db.dao;

import com.qiguliuxing.dts.db.domain.DtsShopApplyEntity;
import com.qiguliuxing.dts.db.domain.DtsShopEntity;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 
 * 
 * @author mh
 * @email software8888@163.com
 * @date 2020-06-29 15:20:24
 */
@Mapper
public interface DtsShopApplyDao {

    @Insert("insert into dts_shop_apply(check_status_id,shop_id) values(#{apply.checkStatusId},#{apply.shopId})")
    int insert(@Param("apply") DtsShopApplyEntity shopApplyEntity);
}
