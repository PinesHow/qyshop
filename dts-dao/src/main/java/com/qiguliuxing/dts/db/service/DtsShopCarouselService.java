package com.qiguliuxing.dts.db.service;

import com.github.pagehelper.PageHelper;
import com.qiguliuxing.dts.db.dao.DtsShopCarouselMapper;
import com.qiguliuxing.dts.db.domain.DtsShopCarouselEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DtsShopCarouselService {

    @Autowired
    private DtsShopCarouselMapper dtsShopCarouselMapper;

    public List<DtsShopCarouselEntity> queryIndex(Integer shopId) {
        return dtsShopCarouselMapper.selectByExample(shopId);
    }

    public void create(DtsShopCarouselEntity dtsShopCarouselEntity) {
        dtsShopCarouselEntity.setAddTime(LocalDateTime.now());
        dtsShopCarouselEntity.setUpdateTime(LocalDateTime.now());
        dtsShopCarouselEntity.setEnabled(true);
        dtsShopCarouselEntity.setDeleted(false);
        dtsShopCarouselMapper.createShopCarouselEntity(dtsShopCarouselEntity);
    }

    public void update(DtsShopCarouselEntity dtsShopCarouselEntity) {
        dtsShopCarouselEntity.setUpdateTime(LocalDateTime.now());
        dtsShopCarouselMapper.updateShopCarouselEntity(dtsShopCarouselEntity);
    }

    public DtsShopCarouselEntity queryById(Integer id) {
        return dtsShopCarouselMapper.queryById(id);
    }

    public void deleteById(Integer id) {
        dtsShopCarouselMapper.deleteById(id);
    }

    public List<DtsShopCarouselEntity> queryListByShopId(Integer shopId, Integer page, Integer limit, Boolean enabled) {
        PageHelper.startPage(page,limit);
        return dtsShopCarouselMapper.queryListByShopId(shopId,enabled);
    }

    public void updateEnabledById(List<Integer> ids) {
        dtsShopCarouselMapper.updateEnableById(ids);
    }
}
