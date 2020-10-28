package com.qiguliuxing.dts.admin.web;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.qiguliuxing.dts.admin.util.CatVo;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.core.validator.Order;
import com.qiguliuxing.dts.core.validator.Sort;
import com.qiguliuxing.dts.db.domain.DtsAdmin;
import com.qiguliuxing.dts.db.domain.DtsCategory;
import com.qiguliuxing.dts.db.domain.DtsShopCategoryEntity;
import com.qiguliuxing.dts.db.service.DtsCategoryService;
import com.qiguliuxing.dts.db.service.DtsShopCategoryService;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/category")
@Validated
public class AdminCategoryController {
	private static final Logger logger = LoggerFactory.getLogger(AdminCategoryController.class);

	@Autowired
	private DtsCategoryService categoryService;
	@Autowired
	private DtsShopCategoryService dtsShopCategoryService;

	@RequiresPermissions("admin:category:list")
//	@RequiresPermissionsDesc(menu = { "商场管理", "类目管理" }, button = "查询")
	@GetMapping("/list")
	public Object list(String id, String name, @RequestParam(defaultValue = "1") Integer page,
			@RequestParam(defaultValue = "10") Integer limit,
			@Sort @RequestParam(defaultValue = "add_time") String sort,
			@Order @RequestParam(defaultValue = "desc") String order) {
		logger.info("【请求开始】商场管理->类目管理->查询,请求参数:name:{},page:{}", name, page);

		List<DtsCategory> collectList = categoryService.querySelective(id, name, page, limit, sort, order);
		long total = PageInfo.of(collectList).getTotal();
		Map<String, Object> data = new HashMap<>();
		data.put("total", total);
		data.put("items", collectList);

		logger.info("【请求结束】商场管理->类目管理->查询:total:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}

	@RequiresPermissions("admin:category:select")
//	@RequiresPermissionsDesc(menu = { "商场管理", "类目管理" }, button = "查询")
	@GetMapping("/select")
	public Object select(@RequestParam("shopType") Integer shopType){
		DtsAdmin dtsAdmin = (DtsAdmin) SecurityUtils.getSubject().getPrincipal();
		Integer shopId = dtsAdmin.getShopId();
		List<DtsCategory> l1CatList = categoryService.selectTree(shopId);

		List<CatVo> categoryList = new ArrayList<>(l1CatList.size());

		for (DtsCategory l1 : l1CatList) {
			CatVo l1CatVo = new CatVo();
			l1CatVo.setValue(l1.getId());
			l1CatVo.setLabel(l1.getName());

			List<DtsCategory> l2CatList = categoryService.queryByPid(l1.getId());
			Boolean flag = false;
			if(l2CatList.size() > 0){//该平台专区有子分区
				flag = true;
			}
			List<Integer> l2IdList = new ArrayList<>();
			for(DtsCategory dtsCategory:l2CatList){
				Integer id = dtsCategory.getId();
				l2IdList.add(id);
			}
			if(l2IdList.size() > 0){
				l2CatList = categoryService.queryPermissionByIds(l2IdList,shopType);//过滤有权限的子集
			}else{
				l2CatList = new ArrayList<>();
			}

			List<CatVo> children = new ArrayList<>(l2CatList.size());
			for (DtsCategory l2 : l2CatList) {
				CatVo l2CatVo = new CatVo();
				l2CatVo.setValue(l2.getId());
				l2CatVo.setLabel(l2.getName());
				children.add(l2CatVo);
			}

			if(l2CatList.size() > 0){
				l1CatVo.setChildren(children);
				categoryList.add(l1CatVo);
			}else{
				if(!flag){
					if(shopType == 1 && l1.getType() <= shopType){
						l1CatVo.setChildren(children);
						categoryList.add(l1CatVo);
					}else if(shopType == l1.getType()){
						l1CatVo.setChildren(children);
						categoryList.add(l1CatVo);
					}
				}
			}
//			l1CatVo.setChildren(children);
//			categoryList.add(l1CatVo);
		}

		Map<String, Object> data = new HashMap<>();
		data.put("categoryList", categoryList);
		return ResponseUtil.ok(data);
	}

	private Object validate(DtsCategory category) {
		String name = category.getName();
		if (StringUtils.isEmpty(name)) {
			return ResponseUtil.badArgument();
		}

		String level = category.getLevel();
		if (StringUtils.isEmpty(level)) {
			return ResponseUtil.badArgument();
		}
		if (!level.equals("L1") && !level.equals("L2")) {
			return ResponseUtil.badArgumentValue();
		}

		Integer pid = category.getPid();
		if (level.equals("L2") && (pid == null)) {
			return ResponseUtil.badArgument();
		}

		return null;
	}

	@RequiresPermissions("admin:category:create")
//	@RequiresPermissionsDesc(menu = { "商场管理", "类目管理" }, button = "添加")
	@PostMapping("/create")
	public Object create(@RequestBody DtsCategory category) {
		logger.info("【请求开始】商场管理->类目管理->添加,请求参数:{}", JSONObject.toJSONString(category));

		Object error = validate(category);
		if (error != null) {
			return error;
		}
		categoryService.add(category);

		logger.info("【请求结束】商场管理->类目管理->添加:响应结果:{}", JSONObject.toJSONString(category));
		return ResponseUtil.ok(category);
	}

	@RequiresPermissions("admin:category:read")
//	@RequiresPermissionsDesc(menu = { "商场管理", "类目管理" }, button = "详情")
	@GetMapping("/read")
	public Object read(@NotNull Integer id) {
		logger.info("【请求开始】商场管理->类目管理->详情,请求参数,id:{}", id);

		DtsCategory category = categoryService.findById(id);

		logger.info("【请求结束】商场管理->类目管理->详情:响应结果:{}", JSONObject.toJSONString(category));
		return ResponseUtil.ok(category);
	}

	@RequiresPermissions("admin:category:update")
//	@RequiresPermissionsDesc(menu = { "商场管理", "类目管理" }, button = "编辑")
	@PostMapping("/update")
	public Object update(@RequestBody DtsCategory category) {
		logger.info("【请求开始】商场管理->类目管理->编辑,请求参数:{}", JSONObject.toJSONString(category));

		Object error = validate(category);
		if (error != null) {
			return error;
		}

		if (categoryService.updateById(category) == 0) {
			logger.error("商场管理->类目管理->编辑 失败，更新数据失败！");
			return ResponseUtil.updatedDataFailed();
		}

		logger.info("【请求结束】商场管理->类目管理->编辑:响应结果:{}", "成功!");
		return ResponseUtil.ok();
	}

	@RequiresPermissions("admin:category:delete")
//	@RequiresPermissionsDesc(menu = { "商场管理", "类目管理" }, button = "删除")
	@PostMapping("/delete")
	public Object delete(@RequestBody DtsCategory category) {
		logger.info("【请求开始】商场管理->类目管理->删除,请求参数:{}", JSONObject.toJSONString(category));

		Integer id = category.getId();
		if (id == null) {
			return ResponseUtil.badArgument();
		}
		categoryService.deleteById(id);

		logger.info("【请求结束】商场管理->类目管理->删除:响应结果:{}", "成功!");
		return ResponseUtil.ok();
	}

//	@RequiresPermissions("admin:category:list")
//	@GetMapping("/l1")
//	public Object catL1() {
//		logger.info("【请求开始】商场管理->类目管理->一级分类目录查询");
//
//		// 所有一级分类目录
//		List<DtsCategory> l1CatList = categoryService.queryL1();
//		List<Map<String, Object>> data = new ArrayList<>(l1CatList.size());
//		for (DtsCategory category : l1CatList) {
//			Map<String, Object> d = new HashMap<>(2);
//			d.put("value", category.getId());
//			d.put("label", category.getName());
//			data.add(d);
//		}
//
//		logger.info("【请求结束】商场管理->类目管理->一级分类目录查询:total:{}", JSONObject.toJSONString(data));
//		return ResponseUtil.ok(data);
//	}

	@RequiresPermissions("admin:category:list")
	@GetMapping("/l1")
	public Object catL1(@RequestParam("shopId") Integer shopId) {
		logger.info("【请求开始】商场管理->类目管理->一级分类目录查询");

		// 所有一级分类目录
		List<DtsShopCategoryEntity> l1CatList = dtsShopCategoryService.queryL1(shopId);
		List<Map<String, Object>> data = new ArrayList<>(l1CatList.size());
		for (DtsShopCategoryEntity dtsShopCategoryEntity : l1CatList) {
			Map<String, Object> d = new HashMap<>(2);
			d.put("value", dtsShopCategoryEntity.getId());
			d.put("label", dtsShopCategoryEntity.getName());
			data.add(d);
		}

		logger.info("【请求结束】商场管理->类目管理->一级分类目录查询:total:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}
}
