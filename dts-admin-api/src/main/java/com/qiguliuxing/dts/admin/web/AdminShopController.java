package com.qiguliuxing.dts.admin.web;

import com.qiguliuxing.dts.admin.annotation.RequiresPermissionsDesc;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.db.domain.DtsAdmin;
import com.qiguliuxing.dts.db.domain.DtsShop;
import com.qiguliuxing.dts.db.service.DtsShopService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;


@RestController
@RequestMapping("/admin/shop")
@Validated
public class AdminShopController {

    private static final Logger logger = LoggerFactory.getLogger(AdminShopController.class);

    @Autowired
    private DtsShopService dtsShopService;

    @RequiresPermissions("admin:shop:get")
    @RequiresPermissionsDesc(menu = {"店铺管理", "店铺资料"}, button = "店铺查询")
    @GetMapping("/get")
    public Object get(@RequestParam("id") Integer id) {
        if (id == null) {
            return ResponseUtil.fail(500, "店铺id不能为空");
        }
        DtsAdmin dtsAdmin = (DtsAdmin) SecurityUtils.getSubject().getPrincipal();
        DtsShop dtsShop = dtsShopService.selectShopById(dtsAdmin.getShopId());
        if (dtsShop == null) {
            return ResponseUtil.fail(500, "该店铺不存在,请联系管理员");
        }
        Integer applyStatus = dtsShopService.selectShopApplyByShopId(dtsAdmin.getShopId());
        Map<String, Object> map = new HashMap<>();
        map.put("dtsShop", dtsShop);
        map.put("applyStatus", applyStatus);
        return ResponseUtil.ok(map);
    }

    @RequiresPermissions("admin:shop:select")
    @RequiresPermissionsDesc(menu = {"店铺管理", "店铺装修"}, button = "店铺装修查询")
    @GetMapping("/select")
    public Object select(@RequestParam("id") Integer id) {
        return get(id);
    }

    @RequiresPermissions("admin:shop:update")
    @RequiresPermissionsDesc(menu = {"店铺管理", "店铺资料"}, button = "编辑")
    @PostMapping("/update")
    public Object update(@RequestBody DtsShop dtsShop) {
        if (dtsShop == null) {
            return ResponseUtil.fail(500, "参数不对");
        }
        if (StringUtils.isEmpty(dtsShop.getId())) {
            return ResponseUtil.fail(500, "店铺id不能为空");
        }
        DtsShop oldShop = dtsShopService.selectShopById(dtsShop.getId());
        if (dtsShop.getReceiveSms() != null){
            if (!StringUtils.isEmpty(oldShop.getContactPhone())) {
                dtsShopService.update(dtsShop);
                return ResponseUtil.ok();
            }else{
                return ResponseUtil.fail(500, "店铺联系方式为空,不能设置接收订单信息!");
            }
        }
        dtsShopService.update(dtsShop);
        return ResponseUtil.ok();
    }

    @RequiresPermissions("admin:shop:submit")
    @RequiresPermissionsDesc(menu = {"店铺管理", "店铺资料"}, button = "审核")
    @PostMapping("/submit")
    public Object update(@RequestParam("shopMsg") String shopMsg) {
        DtsAdmin dtsAdmin = (DtsAdmin) SecurityUtils.getSubject().getPrincipal();
        Integer shopId = dtsAdmin.getShopId();
        dtsShopService.submit(shopId, dtsAdmin.getId(), shopMsg);
        return ResponseUtil.ok();
    }

}
