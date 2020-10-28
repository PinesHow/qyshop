package com.qiguliuxing.dts.admin.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageInfo;
import com.qiguliuxing.dts.admin.dao.GoodsAllinone;
import com.qiguliuxing.dts.admin.util.AdminResponseUtil;
import com.qiguliuxing.dts.admin.util.CatVo;
import com.qiguliuxing.dts.admin.util.UUIDHexGenerator;
import com.qiguliuxing.dts.admin.util.address.LatAndLngUtils;
import com.qiguliuxing.dts.core.qcode.QCodeService;
import com.qiguliuxing.dts.core.util.JacksonUtil;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.db.domain.*;
import com.qiguliuxing.dts.db.service.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

import static com.qiguliuxing.dts.admin.util.AdminResponseCode.GOODS_NAME_EXIST;

@Service
public class AdminGoodsService {
	private static final Logger logger = LoggerFactory.getLogger(AdminGoodsService.class);

	@Autowired
	private DtsGoodsService goodsService;
	@Autowired
	private DtsCartService cartService;
	@Autowired
	private DtsGoodsSpecificationService specificationService;
	@Autowired
	private DtsGoodsAttributeService attributeService;
	@Autowired
	private DtsGoodsProductService productService;
	@Autowired
	private DtsCategoryService categoryService;
	@Autowired
	private DtsOrderGoodsService orderGoodsService;
	@Autowired
	private QCodeService qCodeService;
	@Autowired
	private DtsShopCategoryService dtsShopCategoryService;
	@Value("${tengxun.map.key}")
	private String key;

	public Object list(String goodsSn, String name, Integer page, Integer limit, String sort, String order, Integer shopId,Boolean isOnSale,Integer shopType) {
		Map<String, Object> data = new HashMap<>();
		if(shopType == 4){//软文
			List<DtsTourismAttack> dtsTourismAttackList = goodsService.queryTrourismAttack(name,page,limit,sort,order);
			long total = PageInfo.of(dtsTourismAttackList).getTotal();
			data.put("total", total);
			data.put("items", dtsTourismAttackList);
		}else{
			List<DtsGoods> goodsList = goodsService.querySelective(goodsSn, name, page, limit, sort, order,shopId,isOnSale,shopType);
			long total = PageInfo.of(goodsList).getTotal();
			data.put("total", total);
			data.put("items", goodsList);
		}

		logger.info("【请求结束】商品管理->商品管理->查询,响应结果:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}

	private Object validate(GoodsAllinone goodsAllinone) {
		String goodsSn = UUIDHexGenerator.generateUnionPaySn();
		DtsGoods goods = goodsAllinone.getGoods();
		goodsAllinone.getGoods().setGoodsSn(goodsSn);
		String name = goods.getName();
		if (StringUtils.isEmpty(name)) {
			return ResponseUtil.fail(500,"商品名称不能为空");
		}
		if (StringUtils.isEmpty(goodsSn)) {
			return ResponseUtil.fail(500,"商品编号为空，请联系管理员");
		}
		if(StringUtils.isEmpty(goods.getStartTime())){
			return ResponseUtil.fail(500,"商品起售时间不能为空");
		}
		if(StringUtils.isEmpty(goods.getEndTime())){
			return ResponseUtil.fail(500,"商品下架时间不能为空");
		}
		// 品牌商可以不设置，如果设置则需要验证品牌商存在
//		Integer brandId = goods.getBrandId();
//		if (brandId != null && brandId != 0) {
//			if (brandService.findById(brandId) == null) {
//				return ResponseUtil.badArgumentValue();
//			}
//		}
		// 平台商品分类可以不设置，如果设置则需要验证分类存在
		Integer categoryId = goods.getCategoryId();
		if (categoryId != null && categoryId != 0) {
			if (categoryService.findById(categoryId) == null) {
				return ResponseUtil.badArgumentValue();
			}
		}

		// 商铺商品分类可以不设置，如果设置则需要验证分类存在
		Integer shopCategoryId = goods.getShopCategoryId();
		if (shopCategoryId != null && shopCategoryId != 0) {
			//验证商铺商品分类是否存在
			if (dtsShopCategoryService.findById(shopCategoryId) == null) {
				return ResponseUtil.badArgumentValue();
			}
		}

		DtsGoodsAttribute[] attributes = goodsAllinone.getAttributes();
		for (DtsGoodsAttribute attribute : attributes) {
			String attr = attribute.getAttribute();
			if (StringUtils.isEmpty(attr)) {
				return ResponseUtil.badArgument();
			}
			String value = attribute.getValue();
			if (StringUtils.isEmpty(value)) {
				return ResponseUtil.badArgument();
			}
		}

		DtsGoodsSpecification[] specifications = goodsAllinone.getSpecifications();
		for (DtsGoodsSpecification specification : specifications) {
			String spec = specification.getSpecification();
			if (StringUtils.isEmpty(spec)) {
				return ResponseUtil.badArgument();
			}
			String value = specification.getValue();
			if (StringUtils.isEmpty(value)) {
				return ResponseUtil.badArgument();
			}
		}

		DtsGoodsProduct[] products = goodsAllinone.getProducts();
		for (DtsGoodsProduct product : products) {
			Integer number = product.getNumber();
			if (number == null || number < 0) {
				return ResponseUtil.badArgument();
			}

			BigDecimal price = product.getPrice();
			if (price == null) {
				return ResponseUtil.badArgument();
			}

			String[] productSpecifications = product.getSpecifications();
			if (productSpecifications.length == 0) {
				return ResponseUtil.badArgument();
			}
		}

		return null;
	}

	/**
	 * 编辑商品
	 * <p>
	 * TODO 目前商品修改的逻辑是 1. 更新Dts_goods表 2.
	 * 逻辑删除Dts_goods_specification、Dts_goods_attribute、Dts_goods_product 3.
	 * 添加Dts_goods_specification、Dts_goods_attribute、Dts_goods_product
	 * <p>
	 * 这里商品三个表的数据采用删除再添加的策略是因为 商品编辑页面，支持管理员添加删除商品规格、添加删除商品属性，因此这里仅仅更新是不可能的，
	 * 只能删除三个表旧的数据，然后添加新的数据。 但是这里又会引入新的问题，就是存在订单商品货品ID指向了失效的商品货品表。
	 * 因此这里会拒绝管理员编辑商品，如果订单或购物车中存在商品。 所以这里可能需要重新设计。
	 */
	@Transactional
	public Object update(GoodsAllinone goodsAllinone) {
		Object error = validate(goodsAllinone);
		if (error != null) {
			return error;
		}
		DtsGoods goods = goodsAllinone.getGoods();
		DtsGoodsAttribute[] attributes = goodsAllinone.getAttributes();
		DtsGoodsSpecification[] specifications = goodsAllinone.getSpecifications();
		DtsGoodsProduct[] products = goodsAllinone.getProducts();

		//平台专区不为空  检查是否满足条件
		Integer categoryId = goods.getCategoryId();
		if(!StringUtils.isEmpty(categoryId)){
			DtsCategory dtsCategory = categoryService.findById(categoryId);
			if(dtsCategory == null){
				return ResponseUtil.fail(500,"该平台专区不存在，请联系管理员");
			}
			List<DtsCategory> dtsCategoryList = categoryService.queryByPid(categoryId);
			if(dtsCategoryList.size() > 0){
				return ResponseUtil.fail(500,"该平台专区下有子分类，不能直接将商品上架到该专区下，请联系管理员授权子区");
			}
		}

		//地址不为空  检查其地理位置是否存在
		if(!StringUtils.isEmpty(goods.getAddress())){
			String latAndLng = getLatAndLng(goods.getAddress(), key);
			Integer status = JacksonUtil.parseInteger(latAndLng, "status");
			if(!StringUtils.isEmpty(status) && status.equals(0)){
				StringBuilder sb = new StringBuilder();
				Object lng = JSON.parseObject(JSON.parseObject(JSON.parseObject(latAndLng).get("result").toString()).get("location").toString()).get("lng");
				Object lat = JSON.parseObject(JSON.parseObject(JSON.parseObject(latAndLng).get("result").toString()).get("location").toString()).get("lat");
				sb.append(lng+","+lat);
				goods.setLatitudeAndLongitude(sb.toString());
				logger.info("地理位置信息："+latAndLng);
			}else if(!StringUtils.isEmpty(status) && status.equals(347)){
				return ResponseUtil.fail(500,"该商品地理位置定位无结果，请检查");
			}else{
				return ResponseUtil.fail(500,"该商品地理位置定位异常，请联系管理员");
			}
		}

//		// 检查是否存在购物车商品或者订单商品
//		Integer id = goods.getId();
//		// 如果存在则拒绝修改商品。|| cartService.checkExist(id)
//		if (orderGoodsService.checkExist(id) || cartService.checkExist(id)) {
//			logger.error("商品管理->商品管理->编辑错误:{}", GOODS_UPDATE_NOT_ALLOWED.desc());
//			return AdminResponseUtil.fail(GOODS_UPDATE_NOT_ALLOWED);
//		}

		//检查是否存在购物车商品 如果存在，则修改购物车的价格
		Integer id = goods.getId();
		if(cartService.checkExist(id)){
			cartService.updateCheckedFalseByGoodsId(id);
		}

		// 将生成的分享图片地址写入数据库
		String url = qCodeService.createGoodShareImage(goods.getId().toString(), goods.getPicUrl(), goods.getName(),goods.getCounterPrice(),goods.getRetailPrice());
		goods.setShareUrl(url);

		//判断并设置是否上下架逻辑
		goods = setIsOnSale(goods);

		// 商品基本信息表Dts_goods
		if (goodsService.updateById(goods) == 0) {
			logger.error("商品管理->商品管理->编辑错误:{}", "更新数据失败");
			throw new RuntimeException("更新数据失败");
		}

		Integer gid = goods.getId();
		specificationService.deleteByGid(gid);
		attributeService.deleteByGid(gid);
		productService.deleteByGid(gid);

		// 商品规格表Dts_goods_specification
		for (DtsGoodsSpecification specification : specifications) {
			specification.setGoodsId(goods.getId());
			specificationService.add(specification);
		}

		// 商品参数表Dts_goods_attribute
		for (DtsGoodsAttribute attribute : attributes) {
			attribute.setGoodsId(goods.getId());
			attributeService.add(attribute);
		}

		// 商品货品表Dts_product
		for (DtsGoodsProduct product : products) {
			product.setGoodsId(goods.getId());
			product.setShopId(goods.getShopId());
			productService.add(product);
		}
		//qCodeService.createGoodShareImage(goods.getId().toString(), goods.getPicUrl(), goods.getName());

		logger.info("【请求结束】商品管理->商品管理->编辑,响应结果:{}", "成功!");
		return ResponseUtil.ok();
	}

	@Transactional
	public Object delete(DtsGoods goods) {
		Integer id = goods.getId();
		if (id == null) {
			return ResponseUtil.badArgument();
		}

		Integer gid = goods.getId();
		goodsService.deleteById(gid);
		specificationService.deleteByGid(gid);
		attributeService.deleteByGid(gid);
		productService.deleteByGid(gid);

		logger.info("【请求结束】商品管理->商品管理->删除,响应结果:{}", "成功!");
		return ResponseUtil.ok();
	}

	public String getLatAndLng(String address,String key){
		return LatAndLngUtils.getLatAndLng(address,key);
	}

	@Transactional(rollbackFor = Exception.class)
	public Object create(GoodsAllinone goodsAllinone) {
		Object error = validate(goodsAllinone);
		if (error != null) {
			return error;
		}

		DtsGoods goods = goodsAllinone.getGoods();
		DtsGoodsAttribute[] attributes = goodsAllinone.getAttributes();
		DtsGoodsSpecification[] specifications = goodsAllinone.getSpecifications();
		DtsGoodsProduct[] products = goodsAllinone.getProducts();

		String name = goods.getName();
		if (goodsService.checkExistByName(name)) {
			logger.error("商品管理->商品管理->上架错误:{}", GOODS_NAME_EXIST.desc());
			return AdminResponseUtil.fail( );
		}

		if(StringUtils.isEmpty(goods.getShopType())){
			return ResponseUtil.fail(500,"商品类型不能为空");
		}

		if(!StringUtils.isEmpty(goods.getAddress())){
			String latAndLng = getLatAndLng(goods.getAddress(), key);
			Integer status = JacksonUtil.parseInteger(latAndLng, "status");
			if(!StringUtils.isEmpty(status) && status.equals(0)){
				StringBuilder sb = new StringBuilder();
				Object lng = JSON.parseObject(JSON.parseObject(JSON.parseObject(latAndLng).get("result").toString()).get("location").toString()).get("lng");
				Object lat = JSON.parseObject(JSON.parseObject(JSON.parseObject(latAndLng).get("result").toString()).get("location").toString()).get("lat");
				sb.append(lng+","+lat);
				goods.setLatitudeAndLongitude(sb.toString());
			}else if(!StringUtils.isEmpty(status) && status.equals(347)){
				return ResponseUtil.fail(500,"该商品地理位置定位无结果，请检查");
			}else{
				return ResponseUtil.fail(500,"该商品地理位置定位异常，请联系管理员");
			}
		}

		Integer categoryId = goods.getCategoryId();
		DtsCategory dtsCategory = categoryService.findById(categoryId);
		if(dtsCategory == null){
			return ResponseUtil.fail(500,"该平台专区不存在，请检查");
		}
		List<DtsCategory> dtsCategoryList = categoryService.queryByPid(categoryId);
		if(dtsCategoryList.size() > 0){//该分类下有子分类，不能上架商品
			return ResponseUtil.fail(500,"该平台专区下有子区域，不能直接将商品上架到该专区下，请联系管理员授权子区");
		}
		goods.setApproveStatus(1);
		goods.setApproveMsg("暂时所有商品上架，先通过");

		//判断是否上架逻辑
		goods = setIsOnSale(goods);
		// 商品基本信息表Dts_goods
		goodsService.add(goods);

		// 将生成的分享图片地址写入数据库
		String url = qCodeService.createGoodShareImage(goods.getId().toString(), goods.getPicUrl(), goods.getName(),goods.getCounterPrice(),goods.getRetailPrice());
		if (!StringUtils.isEmpty(url)) {
			goods.setShareUrl(url);
			if (goodsService.updateById(goods) == 0) {
				logger.error("商品管理->商品管理->上架错误:{}", "更新数据失败");
				throw new RuntimeException("更新数据失败");
			}
		}

		// 商品规格表Dts_goods_specification
		for (DtsGoodsSpecification specification : specifications) {
			specification.setGoodsId(goods.getId());
			specificationService.add(specification);
		}

		// 商品参数表Dts_goods_attribute
		for (DtsGoodsAttribute attribute : attributes) {
			attribute.setGoodsId(goods.getId());
			attributeService.add(attribute);
		}

		// 商品货品表Dts_product
		for (DtsGoodsProduct product : products) {
			product.setGoodsId(goods.getId());
			product.setShopId(goods.getShopId());
			productService.add(product);
		}

		logger.info("【请求结束】商品管理->商品管理->上架,响应结果:{}", "成功!");
		return ResponseUtil.ok();
	}

	public DtsGoods setIsOnSale(DtsGoods goods){
		LocalDateTime startTime = goods.getStartTime();
		LocalDateTime endTime = goods.getEndTime();
		LocalDateTime nowTime = LocalDateTime.now();
		if(nowTime.isAfter(startTime) && nowTime.isBefore(endTime)){//当前时间在起售时间之后并且在结束时间之前
			goods.setIsOnSale(true);
		}else if(nowTime.equals(startTime)){//开始时间等于当前时间
			goods.setIsOnSale(true);
		}else{
			goods.setIsOnSale(false);
		}
		return goods;
	}

	public Object list2(Integer shopId) {
		// http://element-cn.eleme.io/#/zh-CN/component/cascader
		// 管理员设置“所属分类”
//		List<DtsCategory> l1CatList = categoryService.queryL1();
		List<DtsShopCategoryEntity> l1CatList = dtsShopCategoryService.queryL1(shopId);
		List<CatVo> categoryList = new ArrayList<>(l1CatList.size());

		for (DtsShopCategoryEntity l1 : l1CatList) {
			CatVo l1CatVo = new CatVo();
			l1CatVo.setValue(l1.getId());
			l1CatVo.setLabel(l1.getName());

			List<DtsShopCategoryEntity> l2CatList = dtsShopCategoryService.queryByPid(l1.getId(),shopId);
//			List<DtsCategory> l2CatList = categoryService.queryByPid(l1.getId());
			List<CatVo> children = new ArrayList<>(l2CatList.size());
			for (DtsShopCategoryEntity l2 : l2CatList) {
				CatVo l2CatVo = new CatVo();
				l2CatVo.setValue(l2.getId());
				l2CatVo.setLabel(l2.getName());
				children.add(l2CatVo);
			}
			l1CatVo.setChildren(children);

			categoryList.add(l1CatVo);
		}

		// http://element-cn.eleme.io/#/zh-CN/component/select
		// 管理员设置“所属品牌商”
//		List<DtsBrand> list = brandService.all();
//		List<Map<String, Object>> brandList = new ArrayList<>(l1CatList.size());
//		for (DtsBrand brand : list) {
//			Map<String, Object> b = new HashMap<>(2);
//			b.put("value", brand.getId());
//			b.put("label", brand.getName());
//			brandList.add(b);
//		}

		Map<String, Object> data = new HashMap<>();
		data.put("categoryList", categoryList);
//		data.put("brandList", brandList);
		return ResponseUtil.ok(data);
	}

	public Object detail(Integer id,Integer shopType) {
		Map<String, Object> data = new HashMap<>();
		if(shopType == 4){//查询软文详情
			DtsTourismAttack dtsTourismAttack = goodsService.selectTourismAttackById(id);
			data.put("dtsTourismAttack",dtsTourismAttack);
			return ResponseUtil.ok(data);
		}
		DtsGoods goods = goodsService.findById(id);
		List<DtsGoodsProduct> products = productService.queryByGid(id);
		List<DtsGoodsSpecification> specifications = specificationService.queryByGid(id);
		List<DtsGoodsAttribute> attributes = attributeService.queryByGid(id);

		Integer categoryId = goods.getCategoryId();
		DtsCategory category = categoryService.findById(categoryId);
		Integer[] categoryIds = new Integer[] {};
		if (category != null) {
			Integer parentCategoryId = category.getPid();
			categoryIds = new Integer[] { parentCategoryId, categoryId };
//			categoryIds = new Integer[] { categoryId };
		}

		Integer shopCategoryId = goods.getShopCategoryId();
		DtsShopCategoryEntity dtsShopCategoryEntity = dtsShopCategoryService.findById(shopCategoryId);
		Integer[] shopCategoryIds = new Integer[] {};
		if(dtsShopCategoryEntity != null){
			Integer parentShopCategoryId = dtsShopCategoryEntity.getPid();
			shopCategoryIds = new Integer[] {parentShopCategoryId,shopCategoryId};
		}

		data.put("goods", goods);
		data.put("specifications", specifications);
		data.put("products", products);
		data.put("attributes", attributes);
		data.put("categoryIds", categoryIds);
		data.put("shopCategoryIds",shopCategoryIds);

		logger.info("【请求结束】商品管理->商品管理->详情,响应结果:{}", "成功!");
		return ResponseUtil.ok(data);
	}

	public List<DtsGoodsType> selectShopGoodsPermission(Integer shopId) {
		return goodsService.selectShopGoodsPermission(shopId);
	}

	public Object selectCategory(Integer shopId) {
		List<DtsCategory> dtsCategoryList = goodsService.selectCategory(shopId);
		List<CatVo> categoryList = new ArrayList<>(dtsCategoryList.size());

		for (DtsCategory l1 : dtsCategoryList) {
			CatVo l1CatVo = new CatVo();
			l1CatVo.setValue(l1.getId());
			l1CatVo.setLabel(l1.getName());

//			List<DtsCategory> l2CatList = dtsShopCategoryService.queryByPid(l1.getId(),shopId);
			List<DtsCategory> l2CatList = categoryService.queryByPid(l1.getId());
			List<CatVo> children = new ArrayList<>(l2CatList.size());
			for (DtsCategory l2 : l2CatList) {
				CatVo l2CatVo = new CatVo();
				l2CatVo.setValue(l2.getId());
				l2CatVo.setLabel(l2.getName());
				children.add(l2CatVo);
			}
			l1CatVo.setChildren(children);

			categoryList.add(l1CatVo);
		}

		// http://element-cn.eleme.io/#/zh-CN/component/select
		// 管理员设置“所属品牌商”
//		List<DtsBrand> list = brandService.all();
//		List<Map<String, Object>> brandList = new ArrayList<>(l1CatList.size());
//		for (DtsBrand brand : list) {
//			Map<String, Object> b = new HashMap<>(2);
//			b.put("value", brand.getId());
//			b.put("label", brand.getName());
//			brandList.add(b);
//		}

		Map<String, Object> data = new HashMap<>();
		data.put("categoryList", categoryList);
//		data.put("brandList", brandList);
		return ResponseUtil.ok(data);
	}

	public void insertTourismAttack(DtsTourismAttack dtsTourismAttack) {
		dtsTourismAttack.setAddTime(LocalDateTime.now());
		dtsTourismAttack.setUpdateTime(LocalDateTime.now());
		dtsTourismAttack.setDeleted(false);
		goodsService.insertTourismAttack(dtsTourismAttack);
	}

	public void updateTourismAttack(DtsTourismAttack dtsTourismAttack) {
		dtsTourismAttack.setUpdateTime(LocalDateTime.now());
		goodsService.updateTourismAttack(dtsTourismAttack);
	}

	public void deletetourism(Integer id) {
		goodsService.deletetourism(id);
	}
}
