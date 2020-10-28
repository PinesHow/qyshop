package com.qiguliuxing.dts.admin.web;

import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.db.domain.DtsShopTypeEntity;
import com.qiguliuxing.dts.db.service.DtsShopTypeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/admin/shopType")
@Validated
public class AdminShopTypeController {
    private static final Logger logger = LoggerFactory.getLogger(AdminShopController.class);

    @Autowired
    private DtsShopTypeService dtsShopTypeService;

    /**
     * 查询商铺类型列表
     * @return
     */
    @GetMapping("/list")
    public Object list(){
        List<DtsShopTypeEntity> shopList = dtsShopTypeService.queryShopListAll();
        return ResponseUtil.ok(shopList);
    }
}
