package com.qiguliuxing.dts.db.dao;

import com.qiguliuxing.dts.db.domain.DtsShop;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DtsShopMapper {

    DtsShop selectShopById(@Param("id") Integer shopId);

    int update(DtsShop dtsShop);

    List<DtsShop> selectShopHotList();

    Integer selectShopApplyByShopIdf(@Param("shopId") Integer shopId);

    int updateApplyByShopId(@Param("shopId") Integer shopId, @Param("shopMsg") String shopMsg);

    void updatePermissionByShopId(@Param("userId") Integer userId);

    DtsShop selectShopByPhoneNum(@Param("phoneNum") String phoneNum);
}
