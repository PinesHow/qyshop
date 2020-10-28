package com.qiguliuxing.dts.admin.web;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.qiguliuxing.dts.admin.annotation.RequiresPermissionsDesc;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.db.domain.DtsAdmin;
import com.qiguliuxing.dts.db.domain.DtsShopCarouselEntity;
import com.qiguliuxing.dts.db.service.DtsShopCarouselService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/admin/carousel")
@Validated
public class AdminShopCarouselController {
    private static final Logger logger = LoggerFactory.getLogger(AdminShopCarouselController.class);

    @Autowired
    private DtsShopCarouselService dtsShopCarouselService;

    private Object validate(DtsShopCarouselEntity dtsShopCarouselEntity) {
        String name = dtsShopCarouselEntity.getName();
        if (StringUtils.isEmpty(name)) {
            return ResponseUtil.badArgumentValue("广告名称不能为空");
        }
        if(StringUtils.isEmpty(dtsShopCarouselEntity.getUrl())){
            return ResponseUtil.badArgumentValue("图片地址不能为空");
        }
        return null;
    }

    @RequiresPermissions("admin:carousel:create")
    @RequiresPermissionsDesc(menu = { "店铺管理", "轮播图管理" }, button = "添加")
    @PostMapping("/create")
    public Object create(@RequestBody DtsShopCarouselEntity dtsShopCarouselEntity) {
        logger.info("【请求开始】店铺管理->轮播图管理->广告添加");
        Object validate = validate(dtsShopCarouselEntity);
        if(validate != null){
            return validate;
        }
        DtsAdmin dtsAdmin = (DtsAdmin) SecurityUtils.getSubject().getPrincipal();
        Integer shopId = dtsAdmin.getShopId();
        dtsShopCarouselEntity.setShopId(shopId);//设置商铺id
        try {
            dtsShopCarouselService.create(dtsShopCarouselEntity);
        }catch (Exception e){
            logger.info("【请求异常】店铺管理->轮播图管理->广告添加",e.getMessage());
            return ResponseUtil.fail(500,"广告添加异常，请联系管理员");
        }
        logger.info("【请求结束】店铺管理->轮播图管理->广告添加,响应结果:{}", JSONObject.toJSONString(dtsShopCarouselEntity));
        return ResponseUtil.ok();
    }

    @RequiresPermissions("admin:carousel:update")
    @RequiresPermissionsDesc(menu = { "店铺管理", "轮播图管理" }, button = "编辑")
    @PostMapping("/update")
    public Object update(@RequestBody DtsShopCarouselEntity dtsShopCarouselEntity) {
        logger.info("【请求开始】店铺管理->轮播图管理->广告修改");
        Object validate = validate(dtsShopCarouselEntity);
        if(validate != null){
            return validate;
        }
        if(StringUtils.isEmpty(dtsShopCarouselEntity.getId())){
            return ResponseUtil.fail(500,"广告Id不能为空");
        }
        DtsAdmin dtsAdmin = (DtsAdmin) SecurityUtils.getSubject().getPrincipal();
        Integer shopId = dtsAdmin.getShopId();
        dtsShopCarouselEntity.setShopId(shopId);//设置商铺id
        try {
            dtsShopCarouselService.update(dtsShopCarouselEntity);
        }catch (Exception e){
            logger.info("【请求异常】店铺管理->轮播图管理->广告修改",e.getMessage());
            return ResponseUtil.fail(500,"广告修改，请联系管理员");
        }
        logger.info("【请求结束】店铺管理->轮播图管理->广告修改,响应结果:{}", JSONObject.toJSONString(dtsShopCarouselEntity));
        return ResponseUtil.ok();
    }

    @RequiresPermissions("admin:carousel:delete")
    @RequiresPermissionsDesc(menu = { "店铺管理", "轮播图管理" }, button = "删除")
    @PostMapping("/delete")
    public Object delete(@RequestBody DtsShopCarouselEntity dtsShopCarouselEntityTmp) {
        if(StringUtils.isEmpty(dtsShopCarouselEntityTmp)){
            return ResponseUtil.badArgument();
        }
        Integer id = dtsShopCarouselEntityTmp.getId();
        logger.info("【请求开始】店铺管理->轮播图管理->广告删除");
        DtsShopCarouselEntity dtsShopCarouselEntity = dtsShopCarouselService.queryById(id);
        if(StringUtils.isEmpty(dtsShopCarouselEntity)){
            return ResponseUtil.fail(500,"该广告不存在，请重新操作");
        }
        dtsShopCarouselService.deleteById(id);
        logger.info("【请求结束】店铺管理->轮播图管理->广告删除成功");
        return ResponseUtil.ok();
    }

    @RequiresPermissions("admin:carousel:list")
    @RequiresPermissionsDesc(menu = { "店铺管理", "轮播图管理" }, button = "查找")
    @GetMapping("/list")
    public Object list(@RequestParam(defaultValue = "1") Integer page,@RequestParam(defaultValue = "10") Integer limit,Boolean enabled) {
        logger.info("【请求开始】店铺管理->轮播图管理->广告列表");
        DtsAdmin dtsAdmin = (DtsAdmin) SecurityUtils.getSubject().getPrincipal();
        Integer shopId = dtsAdmin.getShopId();
        List<DtsShopCarouselEntity> dtsShopCarouselEntities = dtsShopCarouselService.queryListByShopId(shopId,page,limit,enabled);
        long total = PageInfo.of(dtsShopCarouselEntities).getTotal();
        Map<String,Object> map = new HashMap<>();
        map.put("total",total);
        map.put("dtsShopCarouselList",dtsShopCarouselEntities);
        logger.info("【请求结束】店铺管理->轮播图管理->广告列表，响应结果:{}",JSONObject.toJSONString(dtsShopCarouselEntities));
        return ResponseUtil.ok(map);
    }

    @RequiresPermissions("admin:carousel:status")
    @RequiresPermissionsDesc(menu = { "店铺管理", "轮播图管理" }, button = "是否启用")
    @PostMapping("/status")
    public Object status(@RequestBody Map<String,List<Integer>> param) {
        logger.info("【请求开始】店铺管理->轮播图管理->广告启用/禁用");
        if(StringUtils.isEmpty(param)){
            return ResponseUtil.fail(500,"id不能为空");
        }
        List<Integer> ids = param.get("ids");
        dtsShopCarouselService.updateEnabledById(ids);
        return ResponseUtil.ok();
    }
}
