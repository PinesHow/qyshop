package com.qiguliuxing.dts.db.service;

import com.qiguliuxing.dts.db.dao.DtsShopTypeMapper;
import com.qiguliuxing.dts.db.domain.DtsShopTypeEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DtsShopTypeService {

    @Autowired
    private DtsShopTypeMapper dtsShopTypeMapper;

    public List<DtsShopTypeEntity> queryShopListAll() {
        return dtsShopTypeMapper.queryShopListAll();

    }
}
