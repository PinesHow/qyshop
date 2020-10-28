package com.qiguliuxing.dts.admin.web;

import com.alibaba.fastjson.JSONObject;
import com.qiguliuxing.dts.admin.annotation.RequiresPermissionsDesc;
import com.qiguliuxing.dts.admin.dao.GoodsAllinone;
import com.qiguliuxing.dts.admin.service.AdminGoodsService;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.core.validator.Order;
import com.qiguliuxing.dts.core.validator.Sort;
import com.qiguliuxing.dts.db.domain.DtsAdmin;
import com.qiguliuxing.dts.db.domain.DtsGoods;
import com.qiguliuxing.dts.db.domain.DtsGoodsType;
import com.qiguliuxing.dts.db.domain.DtsTourismAttack;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/goods")
@Validated
public class AdminGoodsController {
	private static final Logger logger = LoggerFactory.getLogger(AdminGoodsController.class);

	@Autowired
	private AdminGoodsService adminGoodsService;

	/**
	 * 查询商品
	 *
	 * @param goodsSn
	 * @param name
	 * @param page
	 * @param limit
	 * @param sort
	 * @param order
	 * @return
	 */
	@RequiresPermissions("admin:goods:list")
	@RequiresPermissionsDesc(menu = { "商品管理", "商品列表" }, button = "查询")
	@GetMapping("/list")
	public Object list(String goodsSn, String name, Boolean isOnSale,@RequestParam("shopType") Integer shopType, @RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer limit,
			@Sort @RequestParam(defaultValue = "add_time") String sort,
			@Order @RequestParam(defaultValue = "desc") String order,@RequestParam("shopId") Integer shopId) {
		logger.info("【请求开始】商品管理->商品管理->查询,请求参数:goodsSn:{},name:{},page:{}", goodsSn, name, page);
		return adminGoodsService.list(goodsSn, name, page, limit, sort, order,shopId,isOnSale,shopType);
	}

	@GetMapping("/catAndBrand")
	public Object list2() {
		DtsAdmin dtsAdmin = (DtsAdmin)SecurityUtils.getSubject().getPrincipal();
		Integer shopId = dtsAdmin.getShopId();
		return adminGoodsService.list2(shopId);
	}

	/**
	 * 编辑商品
	 *
	 * @param goodsAllinone
	 * @return
	 */
	@RequiresPermissions("admin:goods:update")
	@RequiresPermissionsDesc(menu = { "商品管理", "商品列表" }, button = "编辑")
	@PostMapping("/update")
	public Object update(@RequestBody GoodsAllinone goodsAllinone) {
		logger.info("【请求开始】商品管理->商品管理->编辑,请求参数:{}", JSONObject.toJSONString(goodsAllinone));
		return adminGoodsService.update(goodsAllinone);
	}

	/**
	 * 删除商品
	 *
	 * @param goods
	 * @return
	 */
	@RequiresPermissions("admin:goods:delete")
	@RequiresPermissionsDesc(menu = { "商品管理", "商品列表" }, button = "删除")
	@PostMapping("/delete")
	public Object delete(@RequestBody DtsGoods goods) {
		logger.info("【请求开始】商品管理->商品管理->删除,请求参数:{}", JSONObject.toJSONString(goods));

		return adminGoodsService.delete(goods);
	}

	/**
	 * 添加商品
	 *
	 * @param goodsAllinone
	 * @return
	 */
	@RequiresPermissions("admin:goods:create")
	@RequiresPermissionsDesc(menu = { "商品管理", "商品列表" }, button = "上架")
	@PostMapping("/create")
	public Object create(@RequestBody GoodsAllinone goodsAllinone) {
		logger.info("【请求开始】商品管理->商品管理->上架,请求参数:{}", JSONObject.toJSONString(goodsAllinone));

		return adminGoodsService.create(goodsAllinone);
	}

	/**
	 * 查询权限
	 *
	 */
	@RequiresPermissions("admin:goods:permission")
//	@RequiresPermissionsDesc(menu = { "商品管理", "商品权限" }, button = "查询")
	@GetMapping("/permission")
	public Object permission() {
		logger.info("【请求开始】商品管理->商品权限->查询,请求参数:{}");
		DtsAdmin dtsAdmin = (DtsAdmin)SecurityUtils.getSubject().getPrincipal();
		Integer shopId = dtsAdmin.getShopId();
		List<DtsGoodsType> dtsGoodsTypeList = adminGoodsService.selectShopGoodsPermission(shopId);
		logger.info("【请求结束】商品管理->商品权限->查询 结果{}",JSONObject.toJSONString(dtsGoodsTypeList));
		return ResponseUtil.ok(dtsGoodsTypeList);
	}

	/**
	 * 查询商铺  平台类目权限树
	 * @return
	 */
	@RequiresPermissions("admin:goods:category")
//	@RequiresPermissionsDesc(menu = { "商品管理", "商铺平台类目权限" }, button = "查询")
	@GetMapping("/category")
	public Object category() {
		logger.info("【请求开始】商品管理->商品权限->查询,请求参数:{}");
		DtsAdmin dtsAdmin = (DtsAdmin)SecurityUtils.getSubject().getPrincipal();
		Integer shopId = dtsAdmin.getShopId();
		return adminGoodsService.selectCategory(shopId);
	}

	/**
	 * 商品详情
	 *
	 * @param id
	 * @return
	 */
	@RequiresPermissions("admin:goods:read")
	@RequiresPermissionsDesc(menu = { "商品管理", "商品列表" }, button = "查看")
	@GetMapping("/detail")
	public Object detail(@NotNull Integer id,@RequestParam("shopType") Integer shopType) {
		logger.info("【请求开始】商品管理->商品管理->详情,请求参数,id:{}", id);
		return adminGoodsService.detail(id,shopType);
	}

	/**
	 * 添加软文
	 *
	 * @param dtsTourismAttack
	 * @return
	 */
	@RequiresPermissions("admin:goods:insert")
	@RequiresPermissionsDesc(menu = { "商品管理", "商品列表" }, button = "添加软文")
	@PostMapping("/insert")
	public Object insert(@RequestBody DtsTourismAttack dtsTourismAttack) {
		logger.info("【请求开始】商品管理->商品管理->添加软文,请求参数,dtsTourismAttack:{}", dtsTourismAttack);
		if(StringUtils.isEmpty(dtsTourismAttack)){
			return ResponseUtil.fail(500,"参数不能为空");
		}
		if(StringUtils.isEmpty(dtsTourismAttack.getName())){
			return ResponseUtil.fail(500,"软文名称不能为空");
		}
		adminGoodsService.insertTourismAttack(dtsTourismAttack);
		return ResponseUtil.ok();
	}

	/**
	 * 修改软文
	 *
	 * @param dtsTourismAttack
	 * @return
	 */
	@RequiresPermissions("admin:goods:updatetourism")
	@RequiresPermissionsDesc(menu = { "商品管理", "商品列表" }, button = "修改软文")
	@PostMapping("/updatetourism")
	public Object updateTourismAttack(@RequestBody DtsTourismAttack dtsTourismAttack) {
		logger.info("【请求开始】商品管理->商品管理->修改软文,请求参数,dtsTourismAttack:{}", dtsTourismAttack);

		if(StringUtils.isEmpty(dtsTourismAttack)){
			return ResponseUtil.fail(500,"参数不能为空");
		}
		if(StringUtils.isEmpty(dtsTourismAttack.getId())){
			return ResponseUtil.fail(500,"软文id不能为空");
		}
		if(StringUtils.isEmpty(dtsTourismAttack.getName())){
			return ResponseUtil.fail(500,"软文名称不能为空");
		}
		adminGoodsService.updateTourismAttack(dtsTourismAttack);
		return ResponseUtil.ok();
	}

	/**
	 * 删除软文
	 * @param param
	 * @return
	 */
	@RequiresPermissions("admin:goods:deletetourism")
	@RequiresPermissionsDesc(menu = { "商品管理", "商品列表" }, button = "删除软文")
	@PostMapping("/deletetourism")
	public Object deletetourism(@RequestBody Map<String,Integer> param){
		logger.info("【请求开始】商品管理->商品管理->修改软文,请求参数,param:{}", param);
		if(StringUtils.isEmpty(param.get("id"))){
			return ResponseUtil.fail(500,"请选择要删除的软文数据");
		}
		adminGoodsService.deletetourism(param.get("id"));
		return ResponseUtil.ok();
	}
}
