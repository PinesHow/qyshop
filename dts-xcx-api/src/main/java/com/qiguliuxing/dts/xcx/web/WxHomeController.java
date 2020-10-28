package com.qiguliuxing.dts.xcx.web;

import com.alibaba.fastjson.JSONObject;
import com.qiguliuxing.dts.core.system.SystemConfig;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.db.dao.DtsShopMapper;
import com.qiguliuxing.dts.db.domain.DtsCategory;
import com.qiguliuxing.dts.db.domain.DtsCoupon;
import com.qiguliuxing.dts.db.domain.DtsGoods;
import com.qiguliuxing.dts.db.domain.DtsShop;
import com.qiguliuxing.dts.db.service.DtsAdService;
import com.qiguliuxing.dts.db.service.DtsArticleService;
import com.qiguliuxing.dts.db.service.DtsCategoryService;
import com.qiguliuxing.dts.db.service.DtsGoodsService;
import com.qiguliuxing.dts.xcx.annotation.LoginUser;
import com.qiguliuxing.dts.xcx.dao.CouponVo;
import com.qiguliuxing.dts.xcx.service.HomeCacheManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * 首页服务
 */
@RestController
@RequestMapping("/xcx/home")
@Validated
public class WxHomeController {
	private static final Logger logger = LoggerFactory.getLogger(WxHomeController.class);

	@Autowired
	private DtsAdService adService;

	@Autowired
	private DtsGoodsService goodsService;

	@Autowired
	private DtsCategoryService categoryService;

	@Autowired
	private DtsArticleService articleService;

	@Autowired
	private DtsShopMapper dtsShopMapper;

	private final static ArrayBlockingQueue<Runnable> WORK_QUEUE = new ArrayBlockingQueue<>(9);

	private final static RejectedExecutionHandler HANDLER = new ThreadPoolExecutor.CallerRunsPolicy();

	@SuppressWarnings("unused")
	private static ThreadPoolExecutor executorService = new ThreadPoolExecutor(9, 9, 1000, TimeUnit.MILLISECONDS,
			WORK_QUEUE, HANDLER);

	@GetMapping("/cache")
	public Object cache(@NotNull String key) {
		logger.info("【请求开始】缓存已清除,请求参数,key:{}", key);

		if (!key.equals("Dts_cache")) {
			logger.error("缓存已清除出错:非本平台标识！！！");
			return ResponseUtil.fail();
		}

		// 清除缓存
		HomeCacheManager.clearAll();

		logger.info("【请求结束】缓存已清除成功!");
		return ResponseUtil.ok("缓存已清除");
	}

	private List<CouponVo> changeTwo(List<DtsCoupon> couponList){
		List<CouponVo> couponVoList = new ArrayList<>(couponList.size());
		for(DtsCoupon coupon:couponList){
			Integer shopId = coupon.getShopId();
			DtsShop dtsShop = dtsShopMapper.selectShopById(shopId);
			CouponVo couponVo = new CouponVo();
			couponVo.setId(coupon.getId());
			couponVo.setName(coupon.getName());
			couponVo.setDesc(coupon.getDesc());
			couponVo.setTag(coupon.getTag());
			couponVo.setMin(coupon.getMin().toPlainString());
			couponVo.setDiscount(coupon.getDiscount().toPlainString());
			couponVo.setStartTime(coupon.getStartTime());
			couponVo.setEndTime(coupon.getEndTime());
			couponVo.setType(coupon.getType());
			couponVo.setPercentage(coupon.getPercentage());
			couponVo.setShopId(shopId);
			couponVo.setShopName(dtsShop.getShopName());
			couponVo.setAddress(dtsShop.getAddress());
			couponVo.setPhoneNum(dtsShop.getPhoneNum());
			couponVo.setDays(coupon.getDays());
			couponVoList.add(couponVo);
		}
		return couponVoList;
	}

	/**
	 * 首页数据
	 * 
	 * @param userId
	 *            当用户已经登录时，非空。为登录状态为null
	 * @return 首页数据
	 */
	@SuppressWarnings("rawtypes")
	@GetMapping("/index")
	public Object index(@LoginUser Integer userId, @RequestParam(defaultValue = "1") Integer page,@RequestParam(defaultValue = "10") Integer size) {
		logger.info("【请求开始】访问首页,请求参数,userId:{}", userId);

		Map<String, Object> data = new HashMap<String, Object>();
		ExecutorService executorService = Executors.newFixedThreadPool(10);
		// 先查询和用户有关的信息
		Callable<List> couponListCallable = null;
		try {
//			List<DtsCoupon> dtsCouponList = new ArrayList<>();
			if (userId == null) {// 调整，用户未登录，不发送优惠券
//				dtsCouponList = couponService.queryList(page, size);
////				List<CouponVo> couponVos = changeTwo(dtsCouponList);
////				couponListCallable = () -> couponVos;
			} else {
//				dtsCouponList = couponService.queryAvailableList(userId, page, size);
//				List<CouponVo> couponVos = changeTwo(dtsCouponList);
//				couponListCallable = () -> couponVos;
			}
//			long total = PageInfo.of(dtsCouponList).getTotal();
//			FutureTask<List> couponListTask = new FutureTask<>(couponListCallable);
//			executorService.submit(couponListTask);

			 //优先从缓存中读取
			if (HomeCacheManager.hasData(HomeCacheManager.INDEX)) {
				data = HomeCacheManager.getCacheData(HomeCacheManager.INDEX);
				if (data != null) {// 加上这个判断，排除判断后到获取数据之间时间段清理的情况
					LocalDateTime expire = (LocalDateTime) data.get("expireTime");
					logger.info("访问首页,存在缓存数 据，除用户优惠券信息外，加载缓存数据,有效期时间点："+ expire.toString());
//					data.put("couponList", couponListTask.get());
					return ResponseUtil.ok(data);
				}
			}

			Callable<List> bannerListCallable = () -> adService.queryIndex();//广告

			Callable<List> articleListCallable = () -> articleService.queryList(page, size, "add_time", "desc");//公告

			Callable<List> channelListCallable = () -> categoryService.queryChannel();//平台分类

//			Callable<List> newGoodsListCallable = () -> goodsService.queryByNew(0, SystemConfig.getNewLimit());//新商品推荐

			Callable<List> hotGoodsListCallable = () -> goodsService.queryByHot(0, SystemConfig.getHotLimit());//人气推荐商品

			Callable<List> travelGoodsListCallable = () -> goodsService.queryByType(2);//旅游商品

//			Callable<List> hotShopListCallable = () -> dtsShopService.queryByHot();//推荐商铺

//			Callable<List> brandListCallable = () -> brandService.queryVO(0, SystemConfig.getBrandLimit());//品牌推荐

//			Callable<List> topicListCallable = () -> topicService.queryList(0, SystemConfig.getTopicLimit());//专题推荐

			// 团购专区
//			Callable<List> grouponListCallable = () -> grouponRulesService.queryList(page, size);//团购专区

//			Callable<List> floorGoodsListCallable = this::getCategoryList;

			FutureTask<List> bannerTask = new FutureTask<>(bannerListCallable);
			FutureTask<List> articleTask = new FutureTask<>(articleListCallable);
			FutureTask<List> channelTask = new FutureTask<>(channelListCallable);
//			FutureTask<List> newGoodsListTask = new FutureTask<>(newGoodsListCallable);
			FutureTask<List> hotGoodsListTask = new FutureTask<>(hotGoodsListCallable);
//			FutureTask<List> brandListTask = new FutureTask<>(brandListCallable);//品牌管理
//			FutureTask<List> topicListTask = new FutureTask<>(topicListCallable);
//			FutureTask<List> grouponListTask = new FutureTask<>(grouponListCallable);
//			FutureTask<List> floorGoodsListTask = new FutureTask<>(floorGoodsListCallable);
//			FutureTask<List> hosShopListTask = new FutureTask<>(hotShopListCallable);
			FutureTask<List> travelShopListTask = new FutureTask<>(travelGoodsListCallable);

			executorService.submit(bannerTask);
			executorService.submit(articleTask);
			executorService.submit(channelTask);
//			executorService.submit(newGoodsListTask);
			executorService.submit(hotGoodsListTask);
//			executorService.submit(brandListTask);
//			executorService.submit(topicListTask);
//			executorService.submit(grouponListTask);
//			executorService.submit(floorGoodsListTask);
//			executorService.submit(hosShopListTask);
			executorService.submit(travelShopListTask);

			data.put("banner", bannerTask.get());
			data.put("articles", articleTask.get());
			data.put("channel", channelTask.get());
//			data.put("couponList", couponListTask.get());
//			data.put("newGoodsList", newGoodsListTask.get());
			data.put("hotGoodsList", hotGoodsListTask.get());//推荐商品
//			data.put("brandList", brandListTask.get());
//			data.put("topicList", topicListTask.get());
//			data.put("grouponList", grouponListTask.get());
			data.put("travelShopListTask",travelShopListTask.get());//旅游商品
//			data.put("floorGoodsList", floorGoodsListTask.get());
//			data.put("hosShopList",hosShopListTask.get());
//			data.put("total",total);

			// 缓存数据首页缓存数据
			HomeCacheManager.loadData(HomeCacheManager.INDEX, data);
			executorService.shutdown();

		} catch (Exception e) {
			logger.error("首页信息获取失败：{}", e.getMessage());
			e.printStackTrace();
		}

		// logger.info("【请求结束】访问首页成功!");//暂不打印首页信息
		logger.info("【请求结束】访问首页,响应结果,优惠券信息：{}", JSONObject.toJSONString(data.get("couponList")));
		return ResponseUtil.ok(data);
	}

	@SuppressWarnings("rawtypes")
	private List<Map> getCategoryList() {
		List<Map> categoryList = new ArrayList<>();
		List<DtsCategory> catL1List = categoryService.queryL1WithoutRecommend(0, SystemConfig.getCatlogListLimit());
		for (DtsCategory catL1 : catL1List) {
			List<DtsCategory> catL2List = categoryService.queryByPid(catL1.getId());
			List<Integer> l2List = new ArrayList<>();
			for (DtsCategory catL2 : catL2List) {
				l2List.add(catL2.getId());
			}

			List<DtsGoods> categoryGoods;
			if (l2List.size() == 0) {
				categoryGoods = new ArrayList<>();
			} else {
				categoryGoods = goodsService.queryByCategory(l2List, 0, SystemConfig.getCatlogMoreLimit());
			}

			Map<String, Object> catGoods = new HashMap<>();
			catGoods.put("id", catL1.getId());
			catGoods.put("name", catL1.getName());
			catGoods.put("goodsList", categoryGoods);
			categoryList.add(catGoods);
		}
		return categoryList;
	}
}