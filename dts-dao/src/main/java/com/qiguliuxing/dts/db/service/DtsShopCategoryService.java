package com.qiguliuxing.dts.db.service;

import com.github.pagehelper.PageHelper;
import com.qiguliuxing.dts.db.dao.DtsOrderGoodsMapper;
import com.qiguliuxing.dts.db.dao.DtsShopCategoryMapper;
import com.qiguliuxing.dts.db.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class DtsShopCategoryService {
    @Autowired
    private DtsShopCategoryMapper dtsShopCategoryMapper;
    @Autowired
    private DtsOrderGoodsMapper dtsOrderGoodsMapper;

    private DtsShopCategoryEntity.Column[] CHANNEL = { DtsShopCategoryEntity.Column.id, DtsShopCategoryEntity.Column.name,
            DtsShopCategoryEntity.Column.iconUrl };

    public DtsShopCategoryEntity findById(Integer id) {
        return dtsShopCategoryMapper.selectByPrimaryKey(id);
    }

    public List<DtsShopCategoryEntity> querySelective(String id, String name, Integer page, Integer size, String sort, String order,Integer shopId) {
        DtsShopCategoryEntityExample example = new DtsShopCategoryEntityExample();
        DtsShopCategoryEntityExample.Criteria criteria = example.createCriteria();
        criteria.andDeletedEqualTo(false);
        if (!StringUtils.isEmpty(id)) {
            criteria.andIdEqualTo(Integer.valueOf(id));
        }
        if(!StringUtils.isEmpty(shopId)){
            criteria.andShopIdEqualTo(shopId);
        }
        if (!StringUtils.isEmpty(name)) {
            criteria.andNameLike("%" + name + "%");
        }
        if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
            example.setOrderByClause(sort + " " + order);
        }

        PageHelper.startPage(page, size);
        return dtsShopCategoryMapper.selectByExample(example);
    }

    public void add(DtsShopCategoryEntity shopCategoryEntity) {
        shopCategoryEntity.setAddTime(LocalDateTime.now());
        shopCategoryEntity.setUpdateTime(LocalDateTime.now());
        shopCategoryEntity.setDeleted(false);
        dtsShopCategoryMapper.insertSelective(shopCategoryEntity);
    }

    public int updateById(DtsShopCategoryEntity shopCategoryEntity) {
        shopCategoryEntity.setUpdateTime(LocalDateTime.now());
        return dtsShopCategoryMapper.updateByPrimaryKeySelective(shopCategoryEntity);
    }

    public int deleteById(Integer id) {
        if(dtsShopCategoryMapper.selectByPrimaryKey(id) == null){
            return -2;
        }
        return dtsShopCategoryMapper.deleteByPrimaryKey(id);
    }

    public List<DtsShopCategoryEntity> queryL1(Integer shopId) {
        DtsShopCategoryEntityExample example = new DtsShopCategoryEntityExample();
        example.or().andLevelEqualTo("L1").andDeletedEqualTo(false).andShopIdEqualTo(shopId);
        return dtsShopCategoryMapper.selectByExample(example);
    }

    public List<DtsShopCategoryEntity> queryByPid(Integer pid,Integer shopId) {
        DtsShopCategoryEntityExample example = new DtsShopCategoryEntityExample();
        example.or().andPidEqualTo(pid).andDeletedEqualTo(false).andShopIdEqualTo(shopId);
        return dtsShopCategoryMapper.selectByExample(example);
    }

    public DtsShopCategoryEntity selectShopCategoryById(Integer pid, Integer shopId) {
        DtsShopCategoryEntityExample example = new DtsShopCategoryEntityExample();
        example.or().andIdEqualTo(pid).andDeletedEqualTo(false).andShopIdEqualTo(shopId);
        return dtsShopCategoryMapper.selectShopCategoryByExample(example);
    }

    public List<DtsShopCategoryEntity> queryL1WithoutRecommend(int page, int limit) {
        DtsShopCategoryEntityExample example = new DtsShopCategoryEntityExample();
        example.or().andLevelEqualTo("L1").andDeletedEqualTo(false);
        PageHelper.startPage(page, limit);
        return dtsShopCategoryMapper.selectByExample(example);
    }

    public List queryChannel(Integer shopId) {
        DtsShopCategoryEntityExample example = new DtsShopCategoryEntityExample();
        example.or().andLevelEqualTo("L1").andDeletedEqualTo(false).andShopIdEqualTo(shopId);
        List<DtsShopCategoryEntity> dtsShopCategoryList = dtsShopCategoryMapper.selectByExampleSelective(example,CHANNEL);
        return dtsShopCategoryList;
    }

    public List<DtsShopVo> selectShopListByCategoryId(Integer categoryId,Integer page,Integer limit) {
        PageHelper.startPage(page,limit);
        List<DtsShop> dtsShopList = dtsShopCategoryMapper.selectShopListByCategoryId(categoryId);
        List<DtsShopVo> dtsShopVoList = new ArrayList<>();
        for(DtsShop dtsShop:dtsShopList){
            DtsShopVo dtsShopVo = new DtsShopVo();
            dtsShopVo.setDtsShop(dtsShop);
            List<DtsOrderGoods> dtsOrderGoodsList = dtsOrderGoodsMapper.selectOderGoodsByShopId(dtsShop.getId());
            Integer goodsSales = 0;
            for(DtsOrderGoods dtsOrderGoods:dtsOrderGoodsList){
                goodsSales += dtsOrderGoods.getNumber();
            }
            dtsShopVo.setGoodsSales(goodsSales);
            dtsShopVoList.add(dtsShopVo);
        }
        return dtsShopVoList;
    }
}
