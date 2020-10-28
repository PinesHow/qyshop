package com.qiguliuxing.dts.db.dao;

import com.qiguliuxing.dts.db.domain.DtsShopCarouselEntity;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DtsShopCarouselMapper {

    List<DtsShopCarouselEntity> selectByExample(@Param("shopId") Integer shopId);

    void createShopCarouselEntity(DtsShopCarouselEntity dtsShopCarouselEntity);

    void updateShopCarouselEntity(DtsShopCarouselEntity dtsShopCarouselEntity);

    DtsShopCarouselEntity queryById(@Param("id") Integer id);

    void deleteById(@Param("id") Integer id);

    List<DtsShopCarouselEntity> queryListByShopId(@Param("shopId") Integer shopId,@Param("enabled") Boolean enabled);

    void updateEnableById(@Param("list") List<Integer> ids);
}
