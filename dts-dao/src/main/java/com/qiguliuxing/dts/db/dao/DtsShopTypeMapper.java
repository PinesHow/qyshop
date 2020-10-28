package com.qiguliuxing.dts.db.dao;

import com.qiguliuxing.dts.db.domain.DtsShopTypeEntity;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DtsShopTypeMapper {

    List<DtsShopTypeEntity> queryShopListAll();

}
