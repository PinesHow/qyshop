package com.qiguliuxing.dts.db.dao;

import com.qiguliuxing.dts.db.domain.*;
import org.apache.ibatis.annotations.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DtsShopCategoryMapper {

    DtsShopCategoryEntity selectByPrimaryKey(@Param("id") Integer shopCategoryId);

    List<DtsShopCategoryEntity> selectByExample(DtsShopCategoryEntityExample example);

    void insertSelective(DtsShopCategoryEntity shopCategoryEntity);

    int updateByPrimaryKeySelective(DtsShopCategoryEntity shopCategoryEntity);

    int deleteByPrimaryKey(@Param("id") Integer id);

    DtsShopCategoryEntity selectShopCategoryByExample(DtsShopCategoryEntityExample example);

    List<DtsShopCategoryEntity> selectByExampleSelective(@Param("example") DtsShopCategoryEntityExample example, @Param("selective") DtsShopCategoryEntity.Column ... selective);

    List<DtsShop> selectShopListByCategoryId(@Param("categoryId") Integer categoryId);
}
