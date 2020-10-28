package com.qiguliuxing.dts.db.service;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.qiguliuxing.dts.db.dao.DtsShopMapper;
import com.qiguliuxing.dts.db.domain.DtsOrderGoods;
import com.qiguliuxing.dts.db.domain.DtsShop;
import com.qiguliuxing.dts.db.domain.DtsShopVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class DtsShopService {
    @Autowired
    private DtsShopMapper dtsShopMapper;
    @Autowired
    private DtsOrderGoodsService dtsOrderGoodsService;
    @Autowired
    private DtsUserService dtsUserService;

    public DtsShop selectShopById(Integer id) {
        return dtsShopMapper.selectShopById(id);
    }

    public int update(DtsShop dtsShop) {
        return dtsShopMapper.update(dtsShop);
    }

    public List<DtsShop> queryByHot(int offset,int limit) {
        Page page= PageHelper.startPage(offset, limit);

        return dtsShopMapper.selectShopHotList();
    }
    public Map<String,Object> getHotShopList(int offset, int limit) {
        Page page= PageHelper.startPage(offset, limit);
        Map<String,Object> res=new HashMap<>();
        List<DtsShop> dtsShopList = dtsShopMapper.selectShopHotList();
        List<DtsShopVo> dtsShopVoList = new ArrayList<DtsShopVo>();
        for(DtsShop dtsShop:dtsShopList){
            DtsShopVo dtsShopVo = new DtsShopVo();
            //店铺信息
            dtsShopVo.setDtsShop(dtsShop);
            List<DtsOrderGoods> dtsOrderGoodsList = dtsOrderGoodsService.selectOderGoodsByShopId(dtsShop.getId());
            Integer goodsSales = 0;
            //店铺商品销量
            for(DtsOrderGoods dtsOrderGoods:dtsOrderGoodsList){
                goodsSales += dtsOrderGoods.getNumber();
            }
            dtsShopVo.setGoodsSales(goodsSales);
            dtsShopVoList.add(dtsShopVo);
        }
        res.put("data",dtsShopVoList);
        res.put("count",page.getTotal());
        return res;
    }



    public Integer selectShopApplyByShopId(Integer shopId) {
        return dtsShopMapper.selectShopApplyByShopIdf(shopId);
    }

    @Transactional(rollbackFor = Exception.class)
    public void submit(Integer shopId,Integer userId,String shopMsg) {
        dtsShopMapper.updateApplyByShopId(shopId,shopMsg);//更改审批状态
        dtsShopMapper.updatePermissionByShopId(userId);//更新该商铺的角色权限
    }

    public DtsShop selectShopByPhoneNum(String phoneNum) {
        return dtsShopMapper.selectShopByPhoneNum(phoneNum);
    }

}
