package com.qiguliuxing.dts.admin.web;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.qiguliuxing.dts.admin.annotation.RequiresPermissionsDesc;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.core.validator.Order;
import com.qiguliuxing.dts.core.validator.Sort;
import com.qiguliuxing.dts.db.domain.DtsAdmin;
import com.qiguliuxing.dts.db.domain.DtsShopCategoryEntity;
import com.qiguliuxing.dts.db.service.DtsShopCategoryService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.apache.shiro.subject.Subject;
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
@RequestMapping("/admin/shopCategory")
@Validated
public class AdminShopCategoryController {
    private static final Logger logger = LoggerFactory.getLogger(AdminShopCategoryController.class);

    @Autowired
    private DtsShopCategoryService dtsShopCategoryService;

    @RequiresPermissions("admin:shopCategory:list")
    @RequiresPermissionsDesc(menu = { "商品管理", "商品类目" }, button = "查找")
    @GetMapping("/list")
    public Object list(String id, String name, @RequestParam(defaultValue = "1") Integer page,
                       @RequestParam(defaultValue = "10") Integer limit,
                       @Sort @RequestParam(defaultValue = "add_time") String sort,
                       @Order @RequestParam(defaultValue = "desc") String order){

        logger.info("【请求开始】商场管理->商品类目->查询,请求参数:name:{},page:{}", name, page);
        Subject currentUser = SecurityUtils.getSubject();
        DtsAdmin dtsAdmin = (DtsAdmin)currentUser.getPrincipal();

        List<DtsShopCategoryEntity> collectList = dtsShopCategoryService.querySelective(id, name, page, limit, sort, order,dtsAdmin.getShopId());
        long total = PageInfo.of(collectList).getTotal();
        Map<String, Object> data = new HashMap<>();
        data.put("total", total);
        data.put("items", collectList);

        logger.info("【请求结束】商场管理->商品类目->查询:total:{}", JSONObject.toJSONString(data));
        return ResponseUtil.ok(data);
    }

    private Object validate(DtsShopCategoryEntity dtsShopCategoryEntity) {
        String name = dtsShopCategoryEntity.getName();
        if (StringUtils.isEmpty(name)) {
            return ResponseUtil.badArgument();
        }
        String level = dtsShopCategoryEntity.getLevel();
        if (StringUtils.isEmpty(level)) {
            return ResponseUtil.badArgument();
        }
        Integer shopId = dtsShopCategoryEntity.getShopId();
        if(StringUtils.isEmpty(shopId)){
            return ResponseUtil.badArgument();
        }
        if (!level.equals("L1") && !level.equals("L2")) {
            return ResponseUtil.badArgumentValue("类目等级只能是一级或二级");
        }
        Integer pid = dtsShopCategoryEntity.getPid();
        if (level.equals("L2") && (pid == null)) {
            return ResponseUtil.badArgumentValue("父级id不能为空");
        }
        if(level.equals("L2") && (pid != null)){
            if(pid==dtsShopCategoryEntity.getId()){
                return ResponseUtil.badArgumentValue("父级id不能为本身");
            }
            if(dtsShopCategoryService.selectShopCategoryById(pid,shopId) == null){
                return ResponseUtil.badArgumentValue("父级id不存在,请检查");
            }
        }
        return null;
    }

    @RequiresPermissions("admin:shopCategory:create")
    @RequiresPermissionsDesc(menu = { "商品管理", "商品类目" }, button = "添加")
    @PostMapping("/create")
    public Object create(@RequestBody DtsShopCategoryEntity shopCategoryEntity) {
        logger.info("【请求开始】商场管理->商品类目->添加,请求参数:{}", JSONObject.toJSONString(shopCategoryEntity));

        Object error = validate(shopCategoryEntity);
        if (error != null) {
            return error;
        }
        dtsShopCategoryService.add(shopCategoryEntity);

        logger.info("【请求结束】商场管理->商品类目->添加:响应结果:{}", JSONObject.toJSONString(shopCategoryEntity));
        return ResponseUtil.ok();
    }

    @RequiresPermissions("admin:shopCategory:update")
    @RequiresPermissionsDesc(menu = { "商品管理", "商品类目" }, button = "编辑")
    @PostMapping("/update")
    public Object update(@RequestBody DtsShopCategoryEntity shopCategoryEntity) {
        logger.info("【请求开始】商场管理->类目管理->编辑,请求参数:{}", JSONObject.toJSONString(shopCategoryEntity));

        Object error = validate(shopCategoryEntity);
        if (error != null) {
            return error;
        }

        if (dtsShopCategoryService.updateById(shopCategoryEntity) == 0) {
            logger.error("商场管理->类目管理->编辑 失败，更新数据失败！");
            return ResponseUtil.updatedDataFailed();
        }

        logger.info("【请求结束】商场管理->类目管理->编辑:响应结果:{}", "成功!");
        return ResponseUtil.ok();
    }

    @RequiresPermissions("admin:shopCategory:delete")
    @RequiresPermissionsDesc(menu = { "商品管理", "商品类目" }, button = "删除")
    @PostMapping("/delete")
    public Object delete(@RequestBody DtsShopCategoryEntity dtsShopCategoryEntity) {
        logger.info("【请求开始】商场管理->类目管理->删除,请求参数:{}", JSONObject.toJSONString(dtsShopCategoryEntity));

        Integer id = dtsShopCategoryEntity.getId();
        if (id == null) {
            return ResponseUtil.badArgument();
        }
        int count = dtsShopCategoryService.deleteById(id);
        if(count == 0){
            return ResponseUtil.fail(205,"无数据可删");
        }else if(count == -1){
            return ResponseUtil.fail(206,"删除失败，未知错误，请联系管理员");
        }else if(count == -2){
            return ResponseUtil.fail(207,"该类目不存在，请重新操作");
        }
        logger.info("【请求结束】商场管理->类目管理->删除:响应结果:{}", "成功!");
        return ResponseUtil.ok();
    }
    
}
