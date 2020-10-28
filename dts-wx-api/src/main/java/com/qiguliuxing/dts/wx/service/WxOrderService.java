package com.qiguliuxing.dts.wx.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayOrderNotifyResult;
import com.github.binarywang.wxpay.bean.order.WxPayMpOrderResult;
import com.github.binarywang.wxpay.bean.result.BaseWxPayResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.pagehelper.PageInfo;
import com.qiguliuxing.dts.core.consts.CommConsts;
import com.qiguliuxing.dts.core.qcode.QCodeService;
import com.qiguliuxing.dts.core.system.SystemConfig;
import com.qiguliuxing.dts.core.util.HttpCheckCodeRequest;
import com.qiguliuxing.dts.core.util.JacksonUtil;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.core.wxpay.WxPayUnifiedOrderRequestChildren;
import com.qiguliuxing.dts.db.dao.DtsCouponUserMapper;
import com.qiguliuxing.dts.db.dao.DtsShopDao;
import com.qiguliuxing.dts.db.domain.*;
import com.qiguliuxing.dts.db.service.*;
import com.qiguliuxing.dts.db.util.CouponUserConstant;
import com.qiguliuxing.dts.db.util.OrderHandleOption;
import com.qiguliuxing.dts.db.util.OrderUtil;
import com.qiguliuxing.dts.wx.dao.BrandCartGoods;
import com.qiguliuxing.dts.wx.dao.BrandOrderGoods;
import com.qiguliuxing.dts.wx.util.IpUtil;
import com.qiguliuxing.dts.wx.util.WxResponseCode;
import com.qiguliuxing.dts.wx.util.WxResponseUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.qiguliuxing.dts.wx.util.WxResponseCode.*;

/**
 * 订单服务
 *
 * <p>
 * 订单状态： 101 订单生成，未支付；102，下单后未支付用户取消；103，下单后未支付超时系统自动取消 201
 * 支付完成，商家未发货；202，订单生产，已付款未发货，但是退款取消； 301 商家发货，用户未确认； 401 用户确认收货； 402
 * 用户没有确认收货超过一定时间，系统自动确认收货；
 *
 * <p>
 * 用户操作： 当101用户未付款时，此时用户可以进行的操作是取消订单，或者付款操作 当201支付完成而商家未发货时，此时用户可以取消订单并申请退款
 * 当301商家已发货时，此时用户可以有确认收货的操作 当401用户确认收货以后，此时用户可以进行的操作是删除订单，评价商品，或者再次购买
 * 当402系统自动确认收货以后，此时用户可以删除订单，评价商品，或者再次购买
 *
 * <p>
 * 注意：目前不支持订单退货和售后服务
 */
@Service
public class WxOrderService {
	private static final Logger logger = LoggerFactory.getLogger(WxOrderService.class);

	@Autowired
	private DtsUserService userService;
	@Autowired
	private DtsOrderService orderService;
	@Autowired
	private DtsOrderGoodsService orderGoodsService;
	@Autowired
	private DtsAddressService addressService;
	@Autowired
	private DtsCartService cartService;
	@Autowired
	private DtsRegionService regionService;
	@Autowired
	private DtsGoodsProductService productService;
	@Autowired
	private WxPayService wxPayService;
	@Autowired
	private DtsUserFormIdService formIdService;
	@Autowired
	private DtsGrouponRulesService grouponRulesService;
	@Autowired
	private DtsGrouponService grouponService;
	@Autowired
	private QCodeService qCodeService;
	@Autowired
	private DtsCommentService commentService;
	@Autowired
	private DtsCouponUserService couponUserService;
	@Autowired
	private DtsCouponUserMapper couponUserMapper;
	@Autowired
	private DtsCouponService couponService;
	@Autowired
	private CouponVerifyService couponVerifyService;
	@Autowired
	private DtsAccountService accountService;
	@Autowired
	private DtsGoodsService dtsGoodsService;
	@Autowired
	private DtsShopDao dtsShopDao;
	@Autowired
	private DtsShopService dtsShopService;
	@Autowired
	private DtsIdCartService dtsIdCartService;
	@Autowired
	private HttpCheckCodeRequest httpCheckCodeRequest;
//	@Autowired
//	private WxProperties wxProperties;

	private String detailedAddress(DtsAddress DtsAddress) {
		Integer provinceId = DtsAddress.getProvinceId();
		Integer cityId = DtsAddress.getCityId();
		Integer areaId = DtsAddress.getAreaId();
		String provinceName = regionService.findById(provinceId).getName();
		String cityName = regionService.findById(cityId).getName();
		String areaName = regionService.findById(areaId).getName();
		String fullRegion = provinceName + " " + cityName + " " + areaName;
		return fullRegion + " " + DtsAddress.getAddress();
	}

	/**
	 * 订单列表
	 *
	 * @param userId
	 *            用户ID
	 * @param showType
	 *            订单信息： 0，全部订单； 1，待付款； 2，待发货； 3，待收货； 4，待评价。
	 * @param page
	 *            分页页数
	 * @param size
	 *            分页大小
	 * @return 订单列表
	 */
	public Object list(Integer userId, Integer showType, Integer page, Integer size) {
		if (userId == null) {
			logger.error("订单列表获取失败：用户未登录!");
			return ResponseUtil.unlogin();
		}
		List<Short> orderStatus = OrderUtil.orderStatus(showType);
		List<DtsOrder> orderList = orderService.queryByOrderStatus(userId, orderStatus, page, size);
		long count = PageInfo.of(orderList).getTotal();
		int totalPages = (int) Math.ceil((double) count / size);
		List<DtsOrderVo> dtsOrderVoList = new ArrayList<>(orderList.size());
//		List<Map<String, Object>> orderVoList = new ArrayList<>(orderList.size());
		for (DtsOrder order : orderList) {
			List<DtsOrderGoods> dtsOrderGoodsList = orderGoodsService.queryByOid(order.getId());
			DtsGoods dtsGoods = dtsGoodsService.queryById(dtsOrderGoodsList.get(0).getGoodsId());
//			DtsGoods dtsGoods = dtsGoodsService.findById(dtsOrderGoodsList.get(0).getGoodsId());
			DtsOrderVo dtsOrderVo = new DtsOrderVo();
			dtsOrderVo.setDtsOrder(order);
			dtsOrderVo.setIsShopConsumption(dtsGoods.getIsShopConsumption());
			List<Integer> shopTypeList = orderGoodsService.selectShopTypeByOrderId(order.getId());
			if(shopTypeList.size() == 0 && shopTypeList.get(0).equals(3)){//单旅游产品
				dtsOrderVo.setType(1);
			}else if(shopTypeList.size() == 0 && shopTypeList.get(0).equals(2)){//单住宿产品
				dtsOrderVo.setType(1);
			}else{//混合产品 或 纯普通商品
				dtsOrderVo.setType(0);
			}
			dtsOrderVo.setOrderStatusText(OrderUtil.orderStatusText(order));
			dtsOrderVo.setOrderHandleOption(OrderUtil.build(order));
//			Map<String, Object> orderVo = new HashMap<>();
//			orderVo.put("id", order.getId());
//			orderVo.put("orderSn", order.getOrderSn());
//			orderVo.put("addTime", order.getAddTime());
//			orderVo.put("consignee", order.getConsignee());
//			orderVo.put("mobile", order.getMobile());
//			orderVo.put("address", order.getAddress());
//			orderVo.put("goodsPrice", order.getGoodsPrice());
//			orderVo.put("freightPrice", order.getFreightPrice());
//			orderVo.put("actualPrice", order.getActualPrice());
//			orderVo.put("orderStatusText", OrderUtil.orderStatusText(order));
//			orderVo.put("handleOption", OrderUtil.build(order));
//			orderVo.put("expCode", order.getShipChannel());
//			orderVo.put("expNo", order.getShipSn());

//			DtsGroupon groupon = grouponService.queryByOrderId(order.getId());
//			if (groupon != null) {
//				orderVo.put("isGroupin", true);
//			} else {
//				orderVo.put("isGroupin", false);
//			}
			List<DtsOrderGoods> orderGoodsList = orderGoodsService.queryByOid(order.getId());
			dtsOrderVo.setGoodsList(orderGoodsList);

			dtsOrderVoList.add(dtsOrderVo);
		}

		Map<String, Object> result = new HashMap<>();
		result.put("count", count);
		result.put("data", dtsOrderVoList);
		result.put("totalPages", totalPages);

		logger.info("【请求结束】获取订单列表,响应结果:{}", JSONObject.toJSONString(result));
		return ResponseUtil.ok(result);
	}

	/**
	 * 订单详情
	 *
	 * @param userId
	 *            用户ID
	 * @param orderId
	 *            订单ID
	 * @return 订单详情
	 */
	public Object detail(Integer userId, Integer orderId) {
		if (userId == null) {
			logger.error("获取订单详情失败：用户未登录!");
			return ResponseUtil.unlogin();
		}

		// 订单信息
		DtsOrder order = orderService.findById(orderId);
		if (null == order) {
			logger.error("获取订单详情失败：{}", ORDER_UNKNOWN.desc());
			return WxResponseUtil.fail(ORDER_UNKNOWN);
		}
		if (!order.getUserId().equals(userId)) {
			logger.error("获取订单详情失败：{}", ORDER_INVALID.desc());
			return WxResponseUtil.fail(ORDER_INVALID);
		}
		Map<String, Object> orderVo = new HashMap<String, Object>();
		orderVo.put("id", order.getId());
		orderVo.put("orderSn", order.getOrderSn());
		orderVo.put("addTime", order.getAddTime());
		orderVo.put("consignee", order.getConsignee());
		orderVo.put("mobile", order.getMobile());
		orderVo.put("address", order.getAddress());
		orderVo.put("goodsPrice", order.getGoodsPrice());
		orderVo.put("freightPrice", order.getFreightPrice());
		orderVo.put("discountPrice", order.getIntegralPrice().add(order.getGrouponPrice()).add(order.getCouponPrice()));
		orderVo.put("actualPrice", order.getActualPrice());
		orderVo.put("orderStatusText", OrderUtil.orderStatusText(order));
		orderVo.put("handleOption", OrderUtil.build(order));
		orderVo.put("expCode", order.getShipChannel());
		orderVo.put("expNo", order.getShipSn());

		List<DtsOrderGoods> orderGoodsList = orderGoodsService.queryByOid(order.getId());
		DtsRefundTrace refundTrace = orderService.findRefundTraceByOrderId(order.getId());

		Map<String, Object> result = new HashMap<>();
		result.put("orderInfo", orderVo);
		result.put("orderGoods", orderGoodsList);
		result.put("refundTrace",refundTrace);

		// 订单状态为已发货且物流信息不为空
		// "YTO", "800669400640887922"
		/*
		 * if (order.getOrderStatus().equals(OrderUtil.STATUS_SHIP)) { ExpressInfo ei =
		 * expressService.getExpressInfo(order.getShipChannel(), order.getShipSn());
		 * result.put("expressInfo", ei); }
		 */

		logger.info("【请求结束】获取订单详情,响应结果:{}", JSONObject.toJSONString(result));
		return ResponseUtil.ok(result);

	}

	/**
	 * 提交订单
	 * <p>
	 * 1. 创建订单表项和订单商品表项; 2. 购物车清空; 3. 优惠券设置已用; 4. 商品货品库存减少; 5. 如果是团购商品，则创建团购活动表项。
	 *
	 * @param userId
	 *            用户ID
	 * @param body
	 *            订单信息，{ cartId：xxx, addressId: xxx, couponId: xxx, message: xxx,
	 *            grouponRulesId: xxx, grouponLinkId: xxx}
	 * @return 提交订单操作结果
	 */
	@Transactional(rollbackFor = Exception.class)
	public Object submit(Integer userId, String body) {
		if (userId == null) {
			logger.error("提交订单详情失败：用户未登录!");
			return ResponseUtil.unlogin();
		}
		if (body == null) {
			return ResponseUtil.badArgument();
		}
		Integer cartId = JacksonUtil.parseInteger(body, "cartId");
		Integer addressId = JacksonUtil.parseInteger(body, "addressId");
		List<Integer> couponList = JacksonUtil.parseIntegerList(body,"couponIdList");
		String message = JacksonUtil.parseString(body, "message");
		Integer grouponRulesId = JacksonUtil.parseInteger(body, "grouponRulesId");
		Integer grouponLinkId = JacksonUtil.parseInteger(body, "grouponLinkId");

		// 如果是团购项目,验证活动是否有效
		if (grouponRulesId != null && grouponRulesId > 0) {
			DtsGrouponRules rules = grouponRulesService.queryById(grouponRulesId);
			// 找不到记录
			if (rules == null) {
				return ResponseUtil.badArgument();
			}
			// 团购活动已经过期
			if (grouponRulesService.isExpired(rules)) {
				logger.error("提交订单详情失败：{}", GROUPON_EXPIRED.desc());
				return WxResponseUtil.fail(GROUPON_EXPIRED);
			}
		}

//		if (cartId == null || addressId == null || couponId == null) {
//			return ResponseUtil.badArgument();
//		}
		if (cartId == null) {
			return ResponseUtil.badArgument();
		}
		DtsAddress checkedAddress = null;
		if(!StringUtils.isEmpty(addressId)){
			// 收货地址
			 checkedAddress = addressService.findById(addressId);
			if (checkedAddress == null) {
				return ResponseUtil.badArgument();
			}
		}

		// 团购优惠
		BigDecimal grouponPrice = new BigDecimal(0.00);
		DtsGrouponRules grouponRules = grouponRulesService.queryById(grouponRulesId);
		if (grouponRules != null) {
			grouponPrice = grouponRules.getDiscount();
		}

		// 货品价格
		List<DtsCart> checkedGoodsList = null;
		if (cartId.equals(0)) {
			checkedGoodsList = cartService.queryByUidAndChecked(userId);
		} else {
			DtsCart cart = cartService.findById(cartId);
			checkedGoodsList = new ArrayList<>(1);
			checkedGoodsList.add(cart);
		}
		if (checkedGoodsList.size() == 0) {
			return ResponseUtil.badArgumentValue();
		}

		if(checkedGoodsList.size() == 1){//只有一个商品
			DtsCart dtsCart = checkedGoodsList.get(0);
			Integer goodsId = dtsCart.getGoodsId();
			DtsGoods dtsGoods = dtsGoodsService.findById(goodsId);
			if(dtsGoods.getIsShopConsumption()){//需要身份证信息
				Integer personNumber = dtsGoods.getPersonNumber();//身份证信息人数
				try {
					if(personNumber != 0){//身份验证人数非0
						List<Map<String,String>> dtsIdCartList = JacksonUtil.parseObject(body,"dtsIdCartList",List.class);
						if(dtsIdCartList.size() != personNumber){//所需身份证信息人数不匹配
							return WxResponseUtil.fail(500,"所需身份证信息人数不匹配");
						}

						for(Map<String,String> hashMap:dtsIdCartList){//添加身份证信息
							DtsIdCart dtsIdCart = new DtsIdCart();
							dtsIdCart.setPersonName(hashMap.get("personName"));
							dtsIdCart.setPersonNumber(hashMap.get("personNumber"));
							dtsIdCart.setGoodsId(goodsId);
							dtsIdCart.setUserId(userId);
							dtsIdCart.setAddTime(LocalDateTime.now());
							dtsIdCart.setUpdateTime(LocalDateTime.now());
							dtsIdCart.setDeleted(false);
							dtsIdCartService.add(dtsIdCart);
						}
					}

				}catch (Exception e){
					return WxResponseUtil.fail(500,"身份证信息获取失败");
				}
			}
		}

		Integer orderId = null;
		DtsOrder order = null;
		DtsCoupon coupon = null;
		Set<Integer> newCouponIdList = new HashSet<>();

		BigDecimal goodsTotalPrice = new BigDecimal(0.00);// 商品总价 （包含团购减免，即减免团购后的商品总价，多店铺需将所有商品相加）
		BigDecimal totalFreightPrice = new BigDecimal(0.00);// 总配送费 （单店铺模式一个，多店铺模式多个配送费的总和）
		BigDecimal couponPrice = new BigDecimal(0.00);// 获取可用的优惠券信息 使用优惠券减免的金额
		// 如果需要拆订单，则按店铺进行归类，在计算邮费
		if (SystemConfig.isMultiOrderModel()) {
			// a.按入驻店铺归类checkout商品
			List<BrandCartGoods> brandCartgoodsList = new ArrayList<BrandCartGoods>();
			for (DtsCart cart : checkedGoodsList) {
				Integer goodsId = cart.getGoodsId();
				DtsGoods dtsGoods = dtsGoodsService.findById(goodsId);
				boolean hasExsist = false;
				for (int i = 0; i < brandCartgoodsList.size(); i++) {
					if (brandCartgoodsList.get(i).getShopId().intValue() == dtsGoods.getShopId().intValue()) {
						brandCartgoodsList.get(i).getCartList().add(cart);
						hasExsist = true;
						break;
					}
				}
				if (!hasExsist) {// 还尚未加入，则新增一类
					BrandCartGoods bandCartGoods = new BrandCartGoods();
					Integer shopId = dtsGoodsService.findById(goodsId).getShopId();
					bandCartGoods.setShopId(shopId);
					List<DtsCart> dtsCartList = new ArrayList<DtsCart>();
					dtsCartList.add(cart);
					bandCartGoods.setCartList(dtsCartList);
					brandCartgoodsList.add(bandCartGoods);
				}
			}
			if(brandCartgoodsList.size() == 1){//说明该购物车里的已选中商品属于一家店铺
				order = new DtsOrder();
				BrandCartGoods brandCartGoods = brandCartgoodsList.get(0);
				Integer shopId = brandCartGoods.getShopId();
				List<DtsCart> cartList = brandCartGoods.getCartList();
				if(cartList.size() == 1){
					DtsGoods goods = dtsGoodsService.findById(cartList.get(0).getGoodsId());
					if(goods.getIsShopConsumption()){
						order.setFreightType(true);
					}
				}
//				Integer goodsId = cartService.findById(cartId).getGoodsId();//查询商品详情
//				order.setShopId(dtsGoodsService.findById(goodsId).getShopId());
				order.setShopId(shopId);
			}
			/**
			 * TODO 临时改法  购物车下单的商品必须是同一家店铺的商品
			 */
			if(brandCartgoodsList.size() != 1){
				return ResponseUtil.fail(500,"购物车选中的商品非同一家店铺的商品，不能下单,请重新选择！");
			}
			// b.核算每个店铺的商品总价，用于计算邮费

			for (BrandCartGoods bcg : brandCartgoodsList) {
				List<DtsCart> bandCarts = bcg.getCartList();
				BigDecimal bandGoodsTotalPrice = new BigDecimal(0.00);//单商铺商品总价格
				BigDecimal bandFreightPrice = new BigDecimal(0.00);//单商铺配送费
				for (DtsCart cart : bandCarts) {// 循环店铺各自的购物商品
					// 只有当团购规格商品ID符合才进行团购优惠
					if (grouponRules != null && grouponRules.getGoodsId().equals(cart.getGoodsId())) {
						bandGoodsTotalPrice = bandGoodsTotalPrice
								.add(cart.getPrice().subtract(grouponPrice).multiply(new BigDecimal(cart.getNumber())));
					} else {
						bandGoodsTotalPrice = bandGoodsTotalPrice
								.add(cart.getPrice().multiply(new BigDecimal(cart.getNumber())));
					}
				}

				// 每个店铺都单独计算运费，满66则免运费，否则6元；
				if (bandGoodsTotalPrice.compareTo(SystemConfig.getFreightLimit()) < 0) {
					bandFreightPrice = SystemConfig.getFreight();
				}
				goodsTotalPrice = goodsTotalPrice.add(bandGoodsTotalPrice);
				totalFreightPrice = totalFreightPrice.add(bandFreightPrice);

				BigDecimal couponChildPrice = new BigDecimal(0.00);
				for(Integer couponId:couponList){//使用优惠券
					logger.info("优惠券Id集合:{}",couponList.toString());
					if(!bcg.getShopId().equals(couponService.findById(couponId).getShopId())){
						continue;
					}
					coupon = couponVerifyService.checkCoupon(userId, couponId, bandGoodsTotalPrice);
					if(coupon == null){
						continue;
					}
					Short type = coupon.getType();
					if(type.equals(CouponUserConstant.STATUS_USABLE)){//满减
						int flag = bandGoodsTotalPrice.compareTo(coupon.getMin());
						if(flag == -1){//订单金额小于满减 不满足条件
							return WxResponseUtil.fail(500,"订单金额未达到满减，不能使用该优惠券");
						}
						couponChildPrice = coupon.getDiscount();
					}else if(type.equals(CouponUserConstant.STATUS_OUT)){//打折券
						logger.info("进入打折券判断");
						BigDecimal percentagePrice = new BigDecimal(Double.parseDouble(coupon.getPercentage()));
						couponChildPrice = goodsTotalPrice.subtract(goodsTotalPrice.multiply(percentagePrice).setScale(2, BigDecimal.ROUND_HALF_DOWN));
					}
					logger.info("添加优惠券id:{}",couponId);
					newCouponIdList.add(couponId);
				}
				couponPrice = couponPrice.add(couponChildPrice);
			}
			if (couponList != null && couponList.size() > 0 && coupon == null) {//使用优惠券的情况下，未找到优惠券使用匹配条件
				return ResponseUtil.badArgumentValue("优惠券不满足使用条件，请检查");
			}
		} else {// 单个店铺模式
			order = new DtsOrder();
			for (DtsCart checkGoods : checkedGoodsList) {
				// 只有当团购规格商品ID符合才进行团购优惠
				if (grouponRules != null && grouponRules.getGoodsId().equals(checkGoods.getGoodsId())) {
					goodsTotalPrice = goodsTotalPrice.add(checkGoods.getPrice().subtract(grouponPrice)
							.multiply(new BigDecimal(checkGoods.getNumber())));
				} else {
					goodsTotalPrice = goodsTotalPrice
							.add(checkGoods.getPrice().multiply(new BigDecimal(checkGoods.getNumber())));
				}
			}
			// 根据订单商品总价计算运费，满足条件（例如66元）则免运费，否则需要支付运费（例如6元）；
			if (goodsTotalPrice.compareTo(SystemConfig.getFreightLimit()) < 0) {
				totalFreightPrice = SystemConfig.getFreight();
			}
			//单店铺方式  计算优惠券
			if(couponList != null && couponList.size() > 0){//有可使用优惠券
				Integer couponId = couponList.get(0);
				if(couponId > 0){//小于等于0  表示不使用优惠券
					coupon = couponVerifyService.checkCoupon(userId, couponId, goodsTotalPrice);
					if (coupon == null) {
						return ResponseUtil.fail(500,"优惠券不满足使用条件，请检查");
					}
					Short type = coupon.getType();
					if(type.equals(CouponUserConstant.STATUS_USABLE)){//满减
						couponPrice = coupon.getDiscount();
					}else if(type.equals(CouponUserConstant.STATUS_OUT)){//打折券
						BigDecimal percentagePrice = new BigDecimal(Double.parseDouble(coupon.getPercentage()));
						couponPrice = goodsTotalPrice.subtract(goodsTotalPrice.multiply(percentagePrice).setScale(2, BigDecimal.ROUND_HALF_DOWN));//最终优惠价格  四舍五入
					}
				}
				newCouponIdList.add(couponId);
			}
		}


		/**
		 * 这里存在一个问题，如果是多商铺模式，需将以下代码 放到 区别于单、多商铺模式下进行单独运算
		 */
//		// 获取可用的优惠券信息 使用优惠券减免的金额
//		BigDecimal couponPrice = new BigDecimal(0.00);
//		// 如果couponId=0则没有优惠券，couponId=-1则不使用优惠券
//		if (couponId != 0 && couponId != -1) {
//			DtsCoupon coupon = couponVerifyService.checkCoupon(userId, couponId, goodsTotalPrice);
//			if (coupon == null) {
//				return ResponseUtil.badArgumentValue();
//			}
//			Short type = coupon.getType();
//			if(type.equals(0)){//满减
//				couponPrice = coupon.getDiscount();
//			}else if(type.equals(3)){//打折券
//				Integer percentage = Integer.parseInt(coupon.getPercentage());
//				BigDecimal percentagePrice = new BigDecimal(percentage.doubleValue());
//				couponPrice = goodsTotalPrice.subtract(goodsTotalPrice.multiply(percentagePrice).setScale(2, BigDecimal.ROUND_UP));
//			}
//		}

		// 可以使用的其他钱，例如用户积分
		BigDecimal integralPrice = new BigDecimal(0.00);

		// 订单费用
		BigDecimal orderTotalPrice = goodsTotalPrice.add(totalFreightPrice).subtract(couponPrice);
		// 最终支付费用
		BigDecimal actualPrice = orderTotalPrice.subtract(integralPrice);

//		Integer orderId = null;
//		DtsOrder order = null;
		// 订单
		if(order==null){
			order = new DtsOrder();
		}
		order.setUserId(userId);//用户id
		order.setOrderSn(orderService.generateOrderSn(userId));//订单编号
		order.setOrderStatus(OrderUtil.STATUS_CREATE);//订单状态
		if(!StringUtils.isEmpty(checkedAddress)){
			order.setConsignee(checkedAddress.getName());//收货人名称
			order.setMobile(checkedAddress.getMobile());//收货人手机号
			String detailedAddress = detailedAddress(checkedAddress);
			order.setAddress(detailedAddress);//收货具体地址
		}
		order.setMessage(message);//用户订单留言
		order.setGoodsPrice(goodsTotalPrice);//商品总费用
		order.setFreightPrice(totalFreightPrice);//配送费用
		order.setCouponPrice(couponPrice);//优惠券减免
		order.setIntegralPrice(integralPrice);//用户积分减免
		order.setOrderPrice(orderTotalPrice);//订单费用
		order.setActualPrice(actualPrice);//实付费用
		if(newCouponIdList.size() > 0){
			logger.info("优惠券id集合:{}",newCouponIdList.toString());
			order.setCouponIds(JSONArray.toJSONString(newCouponIdList));//优惠券id集合
		}

		// 有团购活动
		if (grouponRules != null) {
			order.setGrouponPrice(grouponPrice); // 团购价格
		} else {
			order.setGrouponPrice(new BigDecimal(0.00)); // 团购价格
		}

		// 新增代理的结算金额计算
//		DtsUser user = userService.findById(userId);
//		Integer shareUserId = 1;//
//		if (user != null && user.getShareUserId() != null) {
//			shareUserId = user.getShareUserId();
//		}
//		Integer settlementRate = 3;// 默认百分之3
//		DtsUserAccount userAccount = accountService.findShareUserAccountByUserId(shareUserId);
//		if (userAccount != null && userAccount.getSettlementRate() > 0 && userAccount.getSettlementRate() < 15) {
//			settlementRate = userAccount.getSettlementRate();
//		}
//		BigDecimal rate = new BigDecimal(settlementRate * 0.01);
//		BigDecimal settlementMoney = (actualPrice.subtract(totalFreightPrice)).multiply(rate);
//		order.setSettlementMoney(settlementMoney.setScale(2, BigDecimal.ROUND_DOWN));

		// 添加订单表项
		orderService.add(order);
		orderId = order.getId();

		// 添加订单商品表项
		for (DtsCart cartGoods : checkedGoodsList) {
			// 订单商品
			DtsOrderGoods orderGoods = new DtsOrderGoods();
			orderGoods.setOrderId(order.getId());
			orderGoods.setGoodsId(cartGoods.getGoodsId());
			orderGoods.setGoodsSn(cartGoods.getGoodsSn());
			orderGoods.setProductId(cartGoods.getProductId());
			orderGoods.setGoodsName(cartGoods.getGoodsName());
			orderGoods.setPicUrl(cartGoods.getPicUrl());
			orderGoods.setPrice(cartGoods.getPrice());
			orderGoods.setNumber(cartGoods.getNumber());
			orderGoods.setSpecifications(cartGoods.getSpecifications());
			orderGoods.setAddTime(LocalDateTime.now());

			DtsGoods goods = dtsGoodsService.findById(cartGoods.getGoodsId());
			orderGoods.setShopId(goods.getShopId());
			orderGoods.setBrandId(cartGoods.getBrandId());// 订单商品需加上入驻店铺标志

			orderGoodsService.add(orderGoods);
		}

		// 删除购物车里面的商品信息
		cartService.clearGoods(userId);

		// 商品货品数量减少
		for (DtsCart checkGoods : checkedGoodsList) {
			Integer productId = checkGoods.getProductId();
			DtsGoodsProduct product = productService.findById(productId);

			Integer remainNumber = product.getNumber() - checkGoods.getNumber();
			if (remainNumber < 0) {
				throw new RuntimeException("下单的商品货品数量大于库存量");
			}
			if (productService.reduceStock(productId, checkGoods.getGoodsId(), checkGoods.getNumber()) == 0) {
				throw new RuntimeException("商品货品库存减少失败");
			}
		}

		if(couponList != null && couponList.size() > 0 && coupon != null){// 如果使用了优惠券，设置优惠券使用状态
			for(Integer couponId:couponList){
				DtsCouponUser couponUser = couponUserService.queryOne(userId, couponId);
				couponUser.setStatus(CouponUserConstant.STATUS_USED);
				couponUser.setUsedTime(LocalDateTime.now());
				couponUser.setOrderSn(order.getOrderSn());
				couponUserService.update(couponUser);
			}
		}

//		if (couponId != 0 && couponId != -1) {
//			DtsCouponUser couponUser = couponUserService.queryOne(userId, couponId);
//			couponUser.setStatus(CouponUserConstant.STATUS_USED);
//			couponUser.setUsedTime(LocalDateTime.now());
//			couponUser.setOrderSn(order.getOrderSn());
//			couponUserService.update(couponUser);
//		}

		// 如果是团购项目，添加团购信息
		if (grouponRulesId != null && grouponRulesId > 0) {
			DtsGroupon groupon = new DtsGroupon();
			groupon.setOrderId(orderId);
			groupon.setPayed(false);
			groupon.setUserId(userId);
			groupon.setRulesId(grouponRulesId);

			// 参与者
			if (grouponLinkId != null && grouponLinkId > 0) {
				// 参与的团购记录
				DtsGroupon baseGroupon = grouponService.queryById(grouponLinkId);
				groupon.setCreatorUserId(baseGroupon.getCreatorUserId());
				groupon.setGrouponId(grouponLinkId);
				groupon.setShareUrl(baseGroupon.getShareUrl());
			} else {
				groupon.setCreatorUserId(userId);
				groupon.setGrouponId(0);
			}

			grouponService.createGroupon(groupon);
		}

		Map<String, Object> data = new HashMap<>();
		data.put("orderId", orderId);

		logger.info("【请求结束】提交订单,响应结果:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}

	/**
	 * 取消订单
	 * <p>
	 * 1. 检测当前订单是否能够取消； 2. 设置订单取消状态； 3. 商品货品库存恢复； 4. TODO 优惠券； 5. TODO 团购活动。
	 *
	 * @param userId
	 *            用户ID
	 * @param body
	 *            订单信息，{ orderId：xxx }
	 * @return 取消订单操作结果
	 */
	@Transactional
	public Object cancel(Integer userId, String body) {
		if (userId == null) {
			logger.error("取消订单失败：用户未登录!");
			return ResponseUtil.unlogin();
		}
		Integer orderId = JacksonUtil.parseInteger(body, "orderId");
		if (orderId == null) {
			return ResponseUtil.badArgument();
		}

		DtsOrder order = orderService.findById(orderId);
		if (order == null) {
			return ResponseUtil.badArgumentValue();
		}
		if (!order.getUserId().equals(userId)) {
			return ResponseUtil.badArgumentValue();
		}

		// 检测是否能够取消
		OrderHandleOption handleOption = OrderUtil.build(order);
		if (!handleOption.isCancel()) {
			logger.error("取消订单失败：{}", ORDER_INVALID_OPERATION.desc());
			return WxResponseUtil.fail(ORDER_INVALID_OPERATION);
		}

		// 设置订单已取消状态
		order.setOrderStatus(OrderUtil.STATUS_CANCEL);
		order.setEndTime(LocalDateTime.now());
		if (orderService.updateWithOptimisticLocker(order) == 0) {
			throw new RuntimeException("更新数据已失效");
		}

		// 商品货品数量增加
		List<DtsOrderGoods> orderGoodsList = orderGoodsService.queryByOid(orderId);
		for (DtsOrderGoods orderGoods : orderGoodsList) {
			Integer productId = orderGoods.getProductId();
			Short number = orderGoods.getNumber();
			if (productService.addStock(productId, number) == 0) {
				throw new RuntimeException("商品货品库存增加失败");
			}
		}

		/**
		 * TODO 优惠券退回
		 */
		String couponIds = order.getCouponIds();
		List<DtsCouponUser> used = couponUserMapper.selectByExample(DtsCouponUserExample.newAndCreateCriteria().andUserIdEqualTo(userId).example());
		Map<Integer, List<DtsCouponUser>> collect = used.stream().collect(Collectors.groupingBy(t -> t.getCouponId()));
		if(!StringUtils.isEmpty(couponIds)){//使用了优惠券，订单取消  退回优惠券
			List<Integer> couponIdListTmp = (List<Integer>) JSON.parse(couponIds);
			Set<Integer> couponIdList = new HashSet<>();
			couponIdList.addAll(couponIdListTmp);
			LocalDate nowTime = LocalDateTime.now().toLocalDate();//当前服务器日期
			for(Integer couponId:couponIdList){
				DtsCoupon dtsCoupon = couponService.findById(couponId);
				Short couponLimit = dtsCoupon.getLimit();
				DtsCouponUserExample example = DtsCouponUserExample.newAndCreateCriteria().andUserIdEqualTo(userId).andCouponIdEqualTo(couponId).andOrderSnEqualTo(order.getOrderSn()).example();
				DtsCouponUser dtsCouponUser = couponUserMapper.selectByExample(example).get(0);
				//判断当前使用的优惠券是否过期
				if(nowTime.isAfter(dtsCouponUser.getEndTime())){
					couponIdList.remove(couponId);
					continue;
				}
				List<DtsCouponUser> dtsCouponUserList = collect.get(couponId);
				dtsCouponUserList.remove(dtsCouponUser);

				Short couponUserLimit = (short) dtsCouponUserList.size();//用户拥有当前优惠券数量
				for(DtsCouponUser dtsCouponUserTmp:dtsCouponUserList){
					LocalDate endTimeDate = dtsCouponUserTmp.getEndTime();//优惠券过期时间
					LocalDate addTime = dtsCouponUserTmp.getAddTime().toLocalDate();//优惠券领取日期
					if(nowTime.equals(addTime)){//当前服务器时间 在 领取时间是同一天  今天已领取过   不可以退
						couponIdList.remove(couponId);
					}else{//非当前领取情况  看优惠券是否过期 如果过期可以领 未过期再判断是否使用 如果使用可以领  未使用不可以退回
						if(nowTime.isBefore(endTimeDate)){//当前时间在过期时间之前  未过期
							if(dtsCouponUserTmp.getStatus().equals(CouponUserConstant.STATUS_USABLE)){
								couponIdList.remove(couponId);
							}
						}
					}
				}

				if(!couponLimit.equals(CouponUserConstant.STATUS_USABLE)){//限领
					if((couponLimit <= (couponUserLimit - 1))){//用户拥有当前优惠券数量大于等于 限领张数 不可退回该优惠券
						couponIdList.remove(couponId);
					}
				}
			}
			if(couponIdList.size() > 0){
				couponUserService.updateStatusAllByCouponIdList(couponIdList,userId,order.getOrderSn());
			}
		}
		logger.info("【请求结束】用户取消订单,响应结果:{}", "成功");
		return ResponseUtil.ok();
	}


	@Transactional(rollbackFor = Exception.class)
	public Object prepay(Integer userId,String body,HttpServletRequest request){
		if (userId == null) {
			logger.error("付款订单的预支付会话标识失败：用户未登录!");
			return ResponseUtil.unlogin();
		}
		Integer orderId = JacksonUtil.parseInteger(body, "orderId");
//		List<Integer> couponIdList = JacksonUtil.parseIntegerList(body,"couponIdList");
		if (orderId == null) {
			return ResponseUtil.badArgument();
		}

		DtsOrder order = orderService.findById(orderId);
		if (order == null) {
			return ResponseUtil.badArgumentValue();
		}
		List<Integer> couponIdList = JSONObject.parseArray(order.getCouponIds(), Integer.class);

		if (!order.getUserId().equals(userId)) {
			return ResponseUtil.badArgumentValue();
		}

		// 检测是否能够取消
		OrderHandleOption handleOption = OrderUtil.build(order);
		if (!handleOption.isPay()) {
			logger.error("付款订单的预支付会话标识失败：{}", WxResponseCode.ORDER_REPAY_OPERATION.desc());
			return WxResponseUtil.fail(WxResponseCode.ORDER_REPAY_OPERATION);
		}

		DtsUser user = userService.findById(userId);
		String openid = user.getWeixinOpenid();
		if (openid == null) {
			logger.error("付款订单的预支付会话标识失败：{}", AUTH_OPENID_UNACCESS.desc());
			return WxResponseUtil.fail(AUTH_OPENID_UNACCESS);
		}
		WxPayMpOrderResult result = null;
		try {
			WxPayUnifiedOrderRequestChildren orderRequestChildren = new WxPayUnifiedOrderRequestChildren();
//			WxPayUnifiedOrderRequest orderRequest = new WxPayUnifiedOrderRequest();
//			orderRequest.setOutTradeNo(order.getOrderSn());
//			orderRequest.setOpenid(openid);
//			orderRequest.setBody(CommConsts.DEFAULT_ORDER_FIX + order.getOrderSn());
//			orderRequest.setNotifyUrl("https://www.gzwakjaqjy.cn/wx/order/dtsNotify");

			orderRequestChildren.setOutTradeNo(order.getOrderSn());
//			orderRequestChildren.setOpenid(openid);
			orderRequestChildren.setSubOpenid(openid);
			orderRequestChildren.setBody(CommConsts.DEFAULT_ORDER_FIX + order.getOrderSn());
			orderRequestChildren.setNotifyUrl("https://www.gzwakjaqjy.cn/wx/order/dtsNotify");
			orderRequestChildren.setProfitSharing("Y");//是否需要分账标识
			/**
			 * TODO 添加子商户号
			 */
			DtsShop dtsShop = dtsShopService.selectShopById(order.getShopId());
			if(StringUtils.isEmpty(dtsShop) || StringUtils.isEmpty(dtsShop.getSubMchId())){
				logger.info("支付款订单的预支付会话表示失败：该订单所在店铺不存在或该订单所在店铺的子商户号不存在,店铺信息:{}",dtsShop);
				return ResponseUtil.fail(500,"支付款订单的预支付会话表示失败：该订单所在店铺不存在或该订单所在店铺的子商户号不存在，请商家!");
			}
			orderRequestChildren.setSubMchId(dtsShop.getSubMchId());
			StringBuffer sb = new StringBuffer();
			if(couponIdList != null && couponIdList.size() > 0){
				for(Integer couponId:couponIdList){
					sb.append(couponId.toString()).append(",");
				}
				if(sb.length() > 0){
					sb.deleteCharAt(sb.length()-1);//删除字符串最后一个逗号
				}
			}
			orderRequestChildren.setAttach(sb.toString());//该值放优惠券id数组
			// 元转成分
			int fee = 0;
			BigDecimal actualPrice = order.getActualPrice();
			fee = actualPrice.multiply(new BigDecimal(100)).intValue();
//			orderRequest.setTotalFee(fee);
//			orderRequest.setSpbillCreateIp(IpUtil.getIpAddr(request));

			orderRequestChildren.setTotalFee(fee);
			orderRequestChildren.setSpbillCreateIp(IpUtil.getIpAddr(request));

//			result = wxPayService.createOrder(orderRequest);
			logger.info("发起预支付会话请求,请求参数:{}",orderRequestChildren.toString());
			result = wxPayService.createOrder(orderRequestChildren);

			// 缓存prepayID用于后续模版通知
			String prepayId = result.getPackageValue();
			prepayId = prepayId.replace("prepay_id=", "");
			DtsUserFormid userFormid = new DtsUserFormid();
			userFormid.setOpenid(user.getWeixinOpenid());
			userFormid.setFormid(prepayId);
			userFormid.setIsprepay(true);
			userFormid.setUseamount(3);
			userFormid.setExpireTime(LocalDateTime.now().plusDays(7));
			formIdService.addUserFormid(userFormid);
		} catch (WxPayException e) {
			logger.error("付款订单的预支付会话标识失败：{}", ORDER_PAY_FAIL.desc());
			e.printStackTrace();
			return WxResponseUtil.fail(500,e.getReturnMsg());
		}
		if (orderService.updateWithOptimisticLocker(order) == 0) {
			logger.error("付款订单的预支付会话标识失败：{}", "更新订单信息失败");
			return ResponseUtil.updatedDateExpired();
		}

		logger.info("【请求结束】购物车商品删除成功:清理的productIds:{}", JSONObject.toJSONString(result));
		return ResponseUtil.ok(result);
	}


	/**
	 * 付款订单的预支付会话标识
	 * <p>
	 * 1. 检测当前订单是否能够付款 2. 微信商户平台返回支付订单ID 3. 设置订单付款状态
	 *
	 * @param userId
	 *            用户ID
	 * @param body
	 *            订单信息，{ orderId：xxx }
	 * @return 支付订单ID
	 */
//	@Transactional(rollbackFor = Exception.class)
//	public Object prepay(Integer userId, String body, HttpServletRequest request) {
//		if (userId == null) {
//			logger.error("付款订单的预支付会话标识失败：用户未登录!");
//			return ResponseUtil.unlogin();
//		}
//		Integer orderId = JacksonUtil.parseInteger(body, "orderId");
//		List<Integer> couponIdList = JacksonUtil.parseIntegerList(body,"couponIdList");
//		if (orderId == null) {
//			return ResponseUtil.badArgument();
//		}
//
//		DtsOrder order = orderService.findById(orderId);
//		if (order == null) {
//			return ResponseUtil.badArgumentValue();
//		}
//
//		if (!order.getUserId().equals(userId)) {
//			return ResponseUtil.badArgumentValue();
//		}
//
//		// 检测是否能够取消
//		OrderHandleOption handleOption = OrderUtil.build(order);
//		if (!handleOption.isPay()) {
//			logger.error("付款订单的预支付会话标识失败：{}", WxResponseCode.ORDER_REPAY_OPERATION.desc());
//			return WxResponseUtil.fail(WxResponseCode.ORDER_REPAY_OPERATION);
//		}
//
//		DtsUser user = userService.findById(userId);
//		String openid = user.getWeixinOpenid();
//		if (openid == null) {
//			logger.error("付款订单的预支付会话标识失败：{}", AUTH_OPENID_UNACCESS.desc());
//			return WxResponseUtil.fail(AUTH_OPENID_UNACCESS);
//		}
//		WxPayMpOrderResult result = null;
//		try {
//			StringBuffer sb = new StringBuffer();
//			if(couponIdList != null && couponIdList.size() > 0){
//				for(Integer couponId:couponIdList){
//					sb.append(couponId.toString()).append(",");
//				}
//				if(sb.length() > 0){
//					sb.deleteCharAt(sb.length()-1);//删除字符串最后一个逗号
//				}
//			}
//
//			WxPayUnifiedOrderRequestChildren orderRequestChildren = new WxPayUnifiedOrderRequestChildren();
////			WxPayUnifiedOrderRequest orderRequest = new WxPayUnifiedOrderRequest();
////			orderRequest.setOutTradeNo(order.getOrderSn());
////			orderRequest.setOpenid(openid);
////			orderRequest.setBody(CommConsts.DEFAULT_ORDER_FIX + order.getOrderSn());
////			orderRequest.setNotifyUrl("https://www.gzwakjaqjy.cn/wx/order/dtsNotify");
////			orderRequestChildren.setCombineAppid(wxProperties.getAppId());
////			orderRequestChildren.setCombineMchid(wxProperties.getMchId());
////			orderRequestChildren.setCombineOutTradeNo(order.getOrderSn());
////
////			WxPayCombinePayerInfo wxPayCombinePayerInfo = new WxPayCombinePayerInfo();
////			wxPayCombinePayerInfo.setOpenId(openid);
////			orderRequestChildren.setCombinePayerInfo(JSON.toJSONString(wxPayCombinePayerInfo));
////
////			WxPaySceneInfo wxPaySceneInfo = new WxPaySceneInfo();
////			wxPaySceneInfo.setPayerClientIp(user.getLastLoginIp());
////			orderRequestChildren.setSceneInfo(JSON.toJSONString(wxPaySceneInfo));
////
////			List<WxPaySubOrders> wxPaySubOrdersList = new ArrayList<>();
////			WxPaySubOrders wxPaySubOrders = new WxPaySubOrders();
////			wxPaySubOrders.setMchId(wxProperties.getMchId());//子单商户号
////			wxPaySubOrders.setAttach(sb.toString());
////
////			WxPayAmount wxPayAmount = new WxPayAmount();
////			wxPayAmount.setCurrency("CNY");
////			wxPayAmount.setTotalAmount(1);
////			wxPaySubOrders.setAmount(JSON.toJSONString(wxPayAmount));
////
////			wxPaySubOrders.setOutTradeNo(order.getOrderSn()+"_01");
////			wxPaySubOrders.setSubMchid("1602268595");
////			wxPaySubOrders.setDescription("青岩微购-购买test商品");
//
////			WxSettleInfo wxSettleInfo = new WxSettleInfo();
////			wxSettleInfo.setProfitSharing(true);
////			wxPaySubOrders.setSettleInfo(JSON.toJSONString(wxSettleInfo));
////
////			wxPaySubOrdersList.add(wxPaySubOrders);
////
////			orderRequestChildren.setSubOrders(JSON.toJSONString(wxPaySubOrdersList));
//
//
////			orderRequestChildren.setOutTradeNo(order.getOrderSn());
////			orderRequestChildren.setOpenid(openid);
////			orderRequestChildren.setBody(CommConsts.DEFAULT_ORDER_FIX + order.getOrderSn());
////			orderRequestChildren.setNotifyUrl("https://www.gzwakjaqjy.cn/wx/order/dtsNotify");
////			orderRequestChildren.setProfitSharing("Y");//是否需要分账标识
//
////			orderRequestChildren.setAttach(sb.toString());//该值放优惠券id数组
//			// 元转成分
//			int fee = 0;
//			BigDecimal actualPrice = order.getActualPrice();
//			fee = actualPrice.multiply(new BigDecimal(100)).intValue();
//
//			orderRequestChildren.setTotalFee(fee);
//			orderRequestChildren.setSpbillCreateIp(IpUtil.getIpAddr(request));
//
//			result = wxPayService.createOrder(orderRequestChildren);
//
//			// 缓存prepayID用于后续模版通知
//			String prepayId = result.getPackageValue();
//			prepayId = prepayId.replace("prepay_id=", "");
//			DtsUserFormid userFormid = new DtsUserFormid();
//			userFormid.setOpenid(user.getWeixinOpenid());
//			userFormid.setFormid(prepayId);
//			userFormid.setIsprepay(true);
//			userFormid.setUseamount(3);
//			userFormid.setExpireTime(LocalDateTime.now().plusDays(7));
//			formIdService.addUserFormid(userFormid);
//
//		} catch (Exception e) {
//			logger.error("付款订单的预支付会话标识失败：{}", ORDER_PAY_FAIL.desc());
//			e.printStackTrace();
//			return WxResponseUtil.fail(ORDER_PAY_FAIL);
//		}
//		if (orderService.updateWithOptimisticLocker(order) == 0) {
//			logger.error("付款订单的预支付会话标识失败：{}", "更新订单信息失败");
//			return ResponseUtil.updatedDateExpired();
//		}
//
//		logger.info("【请求结束】购物车商品删除成功:清理的productIds:{}", JSONObject.toJSONString(result));
//		return ResponseUtil.ok(result);
//	}

	/**
	 * 微信付款成功或失败回调接口
	 * <p>
	 * 1. 检测当前订单是否是付款状态; 2. 设置订单付款成功状态相关信息; 3. 响应微信商户平台.
	 *
	 * @param request
	 *            请求内容
	 * @param response
	 *            响应内容
	 * @return 操作结果
	 */
	@Transactional(rollbackFor = Exception.class)
	public Object dtsPayNotify(HttpServletRequest request, HttpServletResponse response) {
		String xmlResult = null;
		try {
			xmlResult = IOUtils.toString(request.getInputStream(), request.getCharacterEncoding());
		} catch (IOException e) {
			logger.error("微信付款成功或失败回调失败：{}", "获取回调消息内容错误!");
			e.printStackTrace();
			return WxPayNotifyResponse.fail(e.getMessage());
		}

		WxPayOrderNotifyResult result = null;
		try {
			result = wxPayService.parseOrderNotifyResult(xmlResult);
		} catch (WxPayException e) {
			logger.error("微信付款成功或失败回调失败：{}", "格式化消息内容错误!");
			e.printStackTrace();
			return WxPayNotifyResponse.fail(e.getMessage());
		}
		if(StringUtils.isEmpty(result)){
			return WxPayNotifyResponse.fail("微信付款回调失败");
		}
		logger.info("处理腾讯支付平台的订单支付：{}", JSONObject.toJSONString(result));

		String orderSn = result.getOutTradeNo();
		String payId = result.getTransactionId();

		// 分转化成元
		logger.info("分转化成元");
		String totalFee = BaseWxPayResult.fenToYuan(result.getTotalFee());
		DtsOrder order = orderService.findBySn(orderSn);
		if (order == null) {
			logger.error("微信付款成功或失败回调失败：{}", "订单不存在 sn=" + orderSn);
			return WxPayNotifyResponse.fail("订单不存在 sn=" + orderSn);
		}

		// 检查这个订单是否已经处理过
		logger.info("检查这个订单是否已经处理过");
		if (OrderUtil.isPayStatus(order) && order.getPayId() != null) {
			logger.warn("警告：微信付款成功或失败回调：{}", "订单已经处理成功!");
			return WxPayNotifyResponse.success("订单已经处理成功!");
		}

		// 检查支付订单金额
		logger.info("检查支付订单金额");
		if (!totalFee.equals(order.getActualPrice().toString())) {
			logger.error("微信付款成功或失败回调失败：{}", order.getOrderSn() + " : 支付金额不符合 totalFee=" + totalFee);
			return WxPayNotifyResponse.fail(order.getOrderSn() + " : 支付金额不符合 totalFee=" + totalFee);
		}

		// 支付成功，有团购信息，更新团购信息
		logger.info("支付成功，有团购信息，更新团购信息");
		DtsGroupon groupon = grouponService.queryByOrderId(order.getId());
		DtsGrouponRules grouponRules = null;
		if (!StringUtils.isEmpty(groupon)) {
			grouponRules = grouponRulesService.queryById(groupon.getRulesId());

			// 仅当发起者才创建分享图片
			if (groupon.getGrouponId() == 0) {
				BigDecimal actualPrice = new BigDecimal(order.getActualPrice().toString());
				String url = qCodeService.createGrouponShareImage(grouponRules.getGoodsName(), grouponRules.getPicUrl(),
						groupon,actualPrice.add(grouponRules.getDiscount()),actualPrice);
				groupon.setShareUrl(url);
			}
			groupon.setPayed(true);
			if (grouponService.updateById(groupon) == 0) {
				logger.error("微信付款成功或失败回调失败：{}", "更新团购数据已失败!");
				return WxPayNotifyResponse.fail("更新团购数据已失败!");
			}
		}

		/**
		 * modify by CHENBO 2019-08-09 对于多店铺模式，支付成功后，如果这个订单包含多个商铺的商品，则考虑拆分订单
		 * 1.原订单删除，需要按店铺拆成多个单，每个订单的订单序列码用 orderSn_1,orderSn_2,orderSn_3... 2.调整原订单商品表
		 * dts_order_goods 中的订单编号 3.调整团购的订单编号，注意，不管是否为多商铺模式，每个团购商品只会归属于一个订单或一个子订单
		 * 4.调整用户优惠券对应的订单，用户优惠券对应的订单可能会有多个，因为多店铺模式，每个大订单可能只有一个优惠券，会按实际收款金额比例进行平摊
		 */
		String attach = result.getAttach();
		logger.info("商家优惠券Id数组，商家数据包内容:"+attach);
		List<String> couponIdList = new ArrayList<>();
		/**
		 * TODO OtherUpdate
		 * 马辉 2020-7-31
		 * 这里couponIdList为空会报错  设置成list
		 */
		if(!StringUtils.isEmpty(attach) && attach!=null ){
			Arrays.asList(attach.split(",")).forEach(e-> couponIdList.add(e+""));//商家优惠券Id数组
		}

		List<DtsOrderGoods> orderGoodsList = orderGoodsService.queryByOid(order.getId());
		List<BrandOrderGoods> brandOrderGoodsList = divideMultiBrandOrderGoods(orderGoodsList);

		// 邮件通知的订单id
		String orderIds = "";
		List<DtsOrder> childOrderList=new ArrayList<>();
		if (SystemConfig.isMultiOrderModel() && brandOrderGoodsList.get(0).getOrderGoodsList().size() > 1) {// 如果是多店铺模式，且这个订单确实含有多个店铺商品
			logger.info("需要进行拆单：主订单编号：【" + order.getId() + "】 支付编号【" + payId + "】");
			List<DtsOrder> dtsOrders = new ArrayList<DtsOrder>();

			/**
			 * 记录一个临时的总订单支付金额 包含团购减免，即减免团购后的商品总价，多店铺需将所有商品相加
			 * 用于核对拆单后的总价与原来订单是否一致，可用于日志警告。理论上拆单逻辑正确，价格应该一致
			 */
			BigDecimal checkActualPrice = new BigDecimal(0.00);
			for (int i = 0; i < brandOrderGoodsList.size(); i++) {
				BrandOrderGoods bog = brandOrderGoodsList.get(i);
				List<DtsOrderGoods> bandOrderGoodsList = bog.getOrderGoodsList();
				BigDecimal bandFreightPrice = new BigDecimal(0.00);//同一家店铺配送费用
				for (int j = 0; j< bandOrderGoodsList.size(); j++) {// 循环店铺各自的购物商品
					BigDecimal bandGoodsTotalPrice = new BigDecimal(0.00);//同一家店铺商品总费用
					DtsOrder childOrder = new DtsOrder();
					DtsOrderGoods orderGoods = bandOrderGoodsList.get(j);
					// 只有当团购规格商品ID符合才进行团购优惠
					if (grouponRules != null && grouponRules.getGoodsId().equals(orderGoods.getGoodsId())) {
						BigDecimal grouponPrice = grouponRules.getDiscount();
						childOrder.setGrouponPrice(grouponPrice);
						bandGoodsTotalPrice = bandGoodsTotalPrice.add(orderGoods.getPrice().subtract(grouponPrice)
								.multiply(new BigDecimal(orderGoods.getNumber())));
					} else {
						bandGoodsTotalPrice = bandGoodsTotalPrice
								.add(orderGoods.getPrice().multiply(new BigDecimal(orderGoods.getNumber())));
					}
					/**
					 * 目前商家优惠券统一是店铺通用优惠券，后期改成优惠券适用部分商品，这里需要做  查询该商品是否有可以使用的优惠券处理
					 */
//				}
					// 每个店铺都单独计算运费，满xxx则免运费，否则x元； (不算运费)
//				if (bandGoodsTotalPrice.compareTo(SystemConfig.getFreightLimit()) < 0) {
//					bandFreightPrice = SystemConfig.getFreight();
//				}
					childOrder.setGoodsPrice(bandGoodsTotalPrice);//设置k 商品总费用(同一家店铺)
					childOrder.setFreightPrice(bandFreightPrice);//设置配送费用

					/**
					 * 核算实际价格和设置子订单的其他属性 按店铺子订单（商品总价与配送费总和）占大订单的 （商品总价与配送费总和） 的比例 分摊其他共用费用
					 */
					childOrder = copyfixedOrderAttr(order, childOrder);//将不变的属性添加到子订单
					childOrder.setPayId(payId);
					childOrder.setPayTime(order.getPayTime());
					childOrder.setOrderStatus(OrderUtil.STATUS_PAY);
					childOrder.setOrderSn(order.getOrderSn() + "_" + j);
					childOrder.setUpdateTime(LocalDateTime.now());
					/**
					 * TODO OtherUpdate
					 * 马辉 2020-7-31
					 * 这里增加shopId  商家端看不到订单信息
					 */
					childOrder.setShopId(bog.getShopId());
					BigDecimal calcOldOrderPrice = order.getGoodsPrice().add(order.getFreightPrice());//原来订单的商品总费用+店铺配送费用
					BigDecimal calcChildOrderPrice = childOrder.getGoodsPrice().add(childOrder.getFreightPrice());//当前店铺的商品总费用+店铺配送费用

					/**
					 * 目前是当前商家通用优惠券的使用，后期会将优惠券改成使用部分商品，当前逻辑应当注释掉，使用往上数第二个注释的代码
					 */
					Integer userId = order.getUserId();
					Integer shopId = bog.getShopId();
					BigDecimal couponPrice = new BigDecimal(0.00);//优惠金额
					for (String s : couponIdList) {
						int couponId = Integer.parseInt(s);
						List<DtsCouponUser> dtsCouponUserList = couponUserService.selectCouponUserByStatus(userId, shopId, couponId, orderSn, CouponUserConstant.STATUS_USED);
						if (dtsCouponUserList.size() > 0) {
							DtsCoupon dtsCoupon = couponService.findById(couponId);
							Short type = dtsCoupon.getType();
							if (type.equals(CouponUserConstant.STATUS_USABLE)) {//满减
								couponPrice = dtsCoupon.getDiscount();
							} else if (type.equals(CouponUserConstant.STATUS_OUT)) {//折扣券
								BigDecimal bigDecimal = new BigDecimal(Double.parseDouble(dtsCoupon.getPercentage()));
								couponPrice = calcChildOrderPrice.subtract(bigDecimal.multiply(calcChildOrderPrice).setScale(2, BigDecimal.ROUND_UP));//保留两位小数，剩下的直接舍弃
							}
						}
					}
					BigDecimal rate = calcChildOrderPrice.divide(calcOldOrderPrice, 8, BigDecimal.ROUND_UP);
//				BigDecimal couponPrice = order.getCouponPrice().multiply(rate).setScale(2, BigDecimal.ROUND_UP);
					BigDecimal integralPrice = order.getIntegralPrice().multiply(rate).setScale(2, BigDecimal.ROUND_UP);
					/**
					 * TODO OtherUpdate
					 * 马辉 2020-7-31
					 * 这里为空会报错 暂时注释代理结算金额
					 */
//				BigDecimal settlementMoney = order.getSettlementMoney().multiply(rate).setScale(2,BigDecimal.ROUND_DOWN);

					BigDecimal orderPrice = bandGoodsTotalPrice.add(bandFreightPrice).subtract(couponPrice);
					BigDecimal actualPrice = orderPrice.subtract(integralPrice);

					childOrder.setCouponPrice(couponPrice);
					childOrder.setIntegralPrice(integralPrice);
//				childOrder.setSettlementMoney(settlementMoney);
					childOrder.setOrderPrice(orderPrice);
					childOrder.setActualPrice(actualPrice);

					checkActualPrice = checkActualPrice.add(actualPrice);
					dtsOrders.add(childOrder);
					// 添加订单表项
					logger.info("拆单后，子订单 订单货品数据：" + childOrder.toString());
					if (childOrder.getGrouponPrice() == null) {
						childOrder.setGrouponPrice(new BigDecimal(0.00));
					}
					orderService.add(childOrder);
					Integer childOrderId = childOrder.getId();
					orderIds = orderIds + "," + childOrderId.intValue();
					for (DtsOrderGoods orderGoodsTmp : bandOrderGoodsList) {// 循环更新店铺各自的购物商品
						orderGoodsTmp.setOrderId(childOrderId);
						orderGoodsService.updateById(orderGoodsTmp);
					}
					childOrderList.add(childOrder);
				}
			}

			// 逻辑删除原订单
			order.setPayId(payId);
			order.setPayTime(LocalDateTime.now());
			order.setOrderStatus(OrderUtil.STATUS_PAY);
			order.setDeleted(true);
			orderService.updateWithOptimisticLocker(order);


			BigDecimal payTotalFee = new BigDecimal(totalFee);
			// 验证误差范围
			BigDecimal errorPrice = checkActualPrice.subtract(payTotalFee).abs();
			if (payTotalFee.compareTo(checkActualPrice) < 0 || errorPrice.divide(payTotalFee, 6, BigDecimal.ROUND_HALF_UP).compareTo(new BigDecimal("0.005")) > 0) {
				logger.warn("拆单过程存在误差已超出千分之五，请联系技术人员核查比对：订单编号：【" + orderSn + "】");
			}
		} else {
//			orderIds = orderIds + "," + order.getId().intValue();
			order.setPayId(payId);
			order.setPayTime(LocalDateTime.now());
			order.setOrderStatus(OrderUtil.STATUS_PAY);
			childOrderList.add(order);
			if (orderService.updateWithOptimisticLocker(order) == 0) {
				// 这里可能存在这样一个问题，用户支付和系统自动取消订单发生在同时
				// 如果数据库首先因为系统自动取消订单而更新了订单状态；
				// 此时用户支付完成回调这里也要更新数据库，而由于乐观锁机制这里的更新会失败
				// 因此，这里会重新读取数据库检查状态是否是订单自动取消，如果是则更新成支付状态。
				order = orderService.findBySn(orderSn);
				int updated = 0;
				if (OrderUtil.isAutoCancelStatus(order)) {
					order.setPayId(payId);
					order.setPayTime(LocalDateTime.now());
					order.setOrderStatus(OrderUtil.STATUS_PAY);
					updated = orderService.updateWithOptimisticLocker(order);
				}

				// 如果updated是0，那么数据库更新失败
				if (updated == 0) {
					return WxPayNotifyResponse.fail("更新数据已失效");
				}
			}
		}
		/**
		 * 根据订单发送短信通知
		 */
		for (DtsOrder dtsOrder : childOrderList) {
			DtsShop orderShopInfo = dtsShopDao.getShopColumnInfo(dtsOrder.getShopId());//商铺信息
			DtsUser dtsUser = userService.findById(dtsOrder.getUserId());//用户信息
			List<DtsOrderGoods> dtsOrderGoods = orderGoodsService.queryByOid(dtsOrder.getId());//订单货品信息
			StringBuffer sb = new StringBuffer();//货品信息
			sb.append("  ").append(dtsUser.getNickname()).append("  已在魅力青岩平台购买产品,").append("   订单号:").append(dtsOrder.getOrderSn());
			for(DtsOrderGoods dtsOrderGoodsTmp:dtsOrderGoods){
				sb.append("   产品名称:").append(dtsOrderGoodsTmp.getGoodsName()).append("   产品数量:").append(dtsOrderGoodsTmp.getNumber());
			}
			logger.info("商铺信息->商家id：{},商家名称：{}",orderShopInfo.getId(),orderShopInfo.getShopName());
			logger.info("发送短信状态:"+orderShopInfo.getReceiveSms());
			if(orderShopInfo.getReceiveSms()){
//				String shopNotify = String.format("订单编号:%s",dtsOrder.getOrderSn());
				String shopNotify = sb.toString(); //短信内容
				String shopContactPhone = orderShopInfo.getContactPhone();
				try {
					String res = httpCheckCodeRequest.toSendShopOrderNotify(shopContactPhone,shopNotify);
					if(res == null){
						logger.error(String.format("方法:%s>操作:%s>数据:%s>信息:%s","微信支付成功回调","发送商户订单通知失败","订单号:"+dtsOrder.getOrderSn(),res+""));
					}
					else{
						logger.info(String.format("方法:%s>操作:%s>数据:%s>信息:%s","微信支付成功回调","发送商户订单通知","订单号:"+dtsOrder.getOrderSn(),res+""));
					}
				} catch (UnsupportedEncodingException e) {
					logger.error(String.format("方法:%s>操作:%s>数据:%s>信息:%s","微信支付成功回调","发送商户订单通知失败","订单号:"+dtsOrder.getOrderSn(),e.getMessage()));
					e.printStackTrace();
				}

			}
		}
		// TODO 发送邮件和短信通知，这里采用异步发送
		// 订单支付成功以后，会发送短信给用户，以及发送邮件给管理员
		// notifyService.notifyMail("新订单通知", order.toString());
//		notifyService.notifySslMail("新订单通知", OrderUtil.orderHtmlText(order, orderIds, orderGoodsList));
		// 这里微信的短信平台对参数长度有限制，所以将订单号只截取后6位，暂时屏蔽，因为已有微信模板提醒
		// notifyService.notifySmsTemplateSync(order.getMobile(),
		// NotifyType.PAY_SUCCEED, new String[]{orderSn.substring(8, 14)});
		// 请依据自己的模版消息配置更改参数
//		String[] parms = new String[] { order.getOrderSn(), order.getOrderPrice().toString(),
//				DateTimeUtil.getDateTimeDisplayString(order.getAddTime()), order.getConsignee(), order.getMobile(),
//				order.getAddress() };

		// notifyService.notifyWxTemplate(result.getOpenid(), NotifyType.PAY_SUCCEED,
		// parms, "pages/index/index?orderId=" + order.getId());
//		notifyService.notifyWxTemplate(result.getOpenid(), NotifyType.PAY_SUCCEED, parms, "/pages/ucenter/order/order");

		logger.info("【请求结束】微信付款成功或失败回调:响应结果:{}", "处理成功!");
		return WxPayNotifyResponse.success("处理成功!");
	}

	/**
	 * 将不变的订单属性复制到子订单
	 *
	 * @param order
	 * @param childOrder
	 * @return
	 */
	private DtsOrder copyfixedOrderAttr(DtsOrder order, DtsOrder childOrder) {
		if (childOrder != null && order != null) {
			childOrder.setAddress(order.getAddress());
			childOrder.setAddTime(order.getAddTime());
			childOrder.setConsignee(order.getConsignee());
			childOrder.setMessage(order.getMessage());
			childOrder.setMobile(order.getMobile());
			childOrder.setUserId(order.getUserId());
			childOrder.setFreightType(order.getFreightType());
		}
		return childOrder;
	}

	/**
	 * 将订单中的商品按入驻店铺分离归类
	 *
	 * @param orderGoodsList
	 * @return
	 */
	private List<BrandOrderGoods> divideMultiBrandOrderGoods(List<DtsOrderGoods> orderGoodsList) {
		List<BrandOrderGoods> brandOrderGoodsList = new ArrayList<BrandOrderGoods>();
		for (int i = 0; i < orderGoodsList.size(); i++) {
			DtsOrderGoods dog = orderGoodsList.get(i);
			Integer shopId = dog.getShopId();
			boolean hasExsist = false;
			for (int k = 0; k < brandOrderGoodsList.size(); k++) {
				if (brandOrderGoodsList.get(k).getShopId().intValue() == dog.getShopId().intValue()) {
					brandOrderGoodsList.get(k).getOrderGoodsList().add(dog);
					hasExsist = true;
					break;
				}
			}
			if (!hasExsist) { // 还尚未加入，则需要查询品牌入驻商铺
				BrandOrderGoods bog = new BrandOrderGoods();
				bog.setShopId(shopId);
				List<DtsOrderGoods> childOrderGoodslist = new ArrayList<DtsOrderGoods>();
				childOrderGoodslist.add(dog);
				bog.setOrderGoodsList(childOrderGoodslist);
				brandOrderGoodsList.add(bog);
			}
		}

		return brandOrderGoodsList;
	}

	/**
	 * 订单申请退款
	 * <p>
	 * 1. 检测当前订单是否能够退款； 2. 设置订单申请退款状态。
	 *
	 * @param userId
	 *            用户ID
	 * @param body
	 *            订单信息，{ orderId：xxx }
	 * @return 订单退款操作结果
	 */
	@Transactional(rollbackFor = Exception.class)
	public Object refund(Integer userId, String body) {
		if (userId == null) {
			logger.error("订单申请退款失败：用户未登录!");
			return ResponseUtil.unlogin();
		}
		Integer orderId = JacksonUtil.parseInteger(body, "orderId");
		String refundReason = JacksonUtil.parseString(body,"refundReason");
		String picUrls = JacksonUtil.parseString(body,"picUrls");
		if (orderId == null) {
			return ResponseUtil.badArgument();
		}

		DtsOrder order = orderService.findById(orderId);
		if (order == null) {
			return ResponseUtil.badArgument();
		}
		if (!order.getUserId().equals(userId)) {
			return ResponseUtil.badArgumentValue();
		}

		OrderHandleOption handleOption = OrderUtil.build(order);
		if (!handleOption.isRefund()) {
			logger.error("订单申请退款失败：{}", ORDER_INVALID_OPERATION.desc());
			return WxResponseUtil.fail(ORDER_INVALID_OPERATION);
		}

		// 设置订单申请退款状态
		order.setOrderStatus(OrderUtil.STATUS_REFUND);
		if (orderService.updateWithOptimisticLocker(order) == 0) {
			logger.error("订单申请退款失败：{}", "更新订单信息失败");
			return ResponseUtil.updatedDateExpired();
		}
		//新建 订单申请退款跟踪 信息
		DtsRefundTrace dtsRefundTrace = new DtsRefundTrace();
		dtsRefundTrace.setOrderId(order.getId());
		dtsRefundTrace.setOrderSn(order.getOrderSn());
		dtsRefundTrace.setStatus(0);//退款申请
		dtsRefundTrace.setRefundReason(refundReason);
		if(!StringUtils.isEmpty(picUrls)){
			dtsRefundTrace.setHasPicture(true);
			dtsRefundTrace.setPicUrls(picUrls);
		}
		if(orderService.createRefundTrace(dtsRefundTrace) == 0){
			logger.error("订单申请退款失败:{}","新增退款跟踪表信息失败");
			return WxResponseUtil.fail(ORDER_INVALID_OPERATION);
		}
		// TODO 发送邮件和短信通知，这里采用异步发送
		// 有用户申请退款，邮件通知运营人员
//		notifyService.notifyMail("退款申请", order.toString());
//		notifyService.notifySslMail("退款申请", OrderUtil.orderHtmlText(order, order.getId().intValue() + "", null));

		// 请依据自己的模版消息配置更改参数
		/*
		 * String[] parms = new String[]{ order.getOrderSn(),
		 * order.getOrderPrice().toString(),
		 * DateTimeUtil.getDateTimeDisplayString(order.getAddTime()),
		 * order.getConsignee(), order.getMobile(), order.getAddress() };
		 * notifyService.notifyWxTemplate("oZQrt0N4e5Ps_R-NhtMzsei93-58",
		 * NotifyType.APPLYREFUND, parms, "pages/index/index?orderId=" + order.getId());
		 */
		/**
		 * 根据订单 填写退款信息
		 */
		DtsShop orderShopInfo = dtsShopDao.getShopColumnInfo(order.getShopId());//商铺信息
		DtsUser dtsUser = userService.findById(order.getUserId());//用户信息
		List<DtsOrderGoods> dtsOrderGoods = orderGoodsService.queryByOid(order.getId());//订单货品信息
		StringBuffer sb = new StringBuffer();//货品信息
		sb.append("  ").append(dtsUser.getNickname()).append("  已在魅力青岩平台申请退款，产品:").append("   订单号:").append(order.getOrderSn());
		for(DtsOrderGoods dtsOrderGoodsTmp:dtsOrderGoods){
			sb.append("   产品名称:").append(dtsOrderGoodsTmp.getGoodsName()).append("   产品数量:").append(dtsOrderGoodsTmp.getNumber());
		}
		logger.info("商铺信息->商家id：{},商家名称：{}",orderShopInfo.getId(),orderShopInfo.getShopName());
		logger.info("发送短信状态:"+orderShopInfo.getReceiveSms());
		if(StringUtils.isEmpty(orderShopInfo.getReceiveSms())){
			logger.info("【请求结束】订单申请退款成功！不发送短信");
			return ResponseUtil.ok();
		}
		if(orderShopInfo.getReceiveSms()){
//				String shopNotify = String.format("订单编号:%s",dtsOrder.getOrderSn());
			String shopNotify = sb.toString(); //短信内容
			String shopContactPhone = orderShopInfo.getContactPhone();
			try {
				String res = httpCheckCodeRequest.toRefund(shopContactPhone,shopNotify);
				if(res == null){
					logger.error(String.format("方法:%s>操作:%s>数据:%s>信息:%s","微信退款成功回调","发送商户订单通知失败","订单号:"+order.getOrderSn(),res+""));
				}
				else{
					logger.info(String.format("方法:%s>操作:%s>数据:%s>信息:%s","微信退款成功回调","发送商户订单通知","订单号:"+order.getOrderSn(),res+""));
				}
			} catch (UnsupportedEncodingException e) {
				logger.error(String.format("方法:%s>操作:%s>数据:%s>信息:%s","微信退款成功回调","发送商户订单通知失败","订单号:"+order.getOrderSn(),e.getMessage()));
				e.printStackTrace();
			}

		}

		logger.info("【请求结束】订单申请退款成功！");
		return ResponseUtil.ok();
	}

	/**
	 * 确认收货
	 * <p>
	 * 1. 检测当前订单是否能够确认收货； 2. 设置订单确认收货状态。
	 *
	 * @param userId
	 *            用户ID
	 * @param body
	 *            订单信息，{ orderId：xxx }
	 * @return 订单操作结果
	 */
	public Object confirm(Integer userId, String body) {
		if (userId == null) {
			logger.error("订单确认收货失败：用户未登录!");
			return ResponseUtil.unlogin();
		}
		Integer orderId = JacksonUtil.parseInteger(body, "orderId");
		if (orderId == null) {
			return ResponseUtil.badArgument();
		}

		DtsOrder order = orderService.findById(orderId);
		if (order == null) {
			return ResponseUtil.badArgument();
		}
		if (!order.getUserId().equals(userId)) {
			return ResponseUtil.badArgumentValue();
		}

		OrderHandleOption handleOption = OrderUtil.build(order);
		if (!handleOption.isConfirm()) {
			logger.error("订单确认收货失败：{}", ORDER_CONFIRM_OPERATION.desc());
			return WxResponseUtil.fail(ORDER_CONFIRM_OPERATION);
		}

		Short comments = orderGoodsService.getComments(orderId);
		order.setComments(comments);

		order.setOrderStatus(OrderUtil.STATUS_CONFIRM);
		order.setConfirmTime(LocalDateTime.now());
		if (orderService.updateWithOptimisticLocker(order) == 0) {
			logger.error("订单确认收货失败：{}", "更新订单信息失败");
			return ResponseUtil.updatedDateExpired();
		}

		logger.info("【请求结束】订单确认收货成功！");
		return ResponseUtil.ok();
	}

	/**
	 * 删除订单
	 * <p>
	 * 1. 检测当前订单是否可以删除； 2. 删除订单。
	 *
	 * @param userId
	 *            用户ID
	 * @param body
	 *            订单信息，{ orderId：xxx }
	 * @return 订单操作结果
	 */
	public Object delete(Integer userId, String body) {
		if (userId == null) {
			logger.error("删除订单失败：用户未登录!");
			return ResponseUtil.unlogin();
		}
		Integer orderId = JacksonUtil.parseInteger(body, "orderId");
		if (orderId == null) {
			return ResponseUtil.badArgument();
		}

		DtsOrder order = orderService.findById(orderId);
		if (order == null) {
			return ResponseUtil.badArgument();
		}
		if (!order.getUserId().equals(userId)) {
			return ResponseUtil.badArgumentValue();
		}

		OrderHandleOption handleOption = OrderUtil.build(order);
		if (!handleOption.isDelete()) {
			logger.error("删除订单失败：{}", ORDER_DEL_OPERATION.desc());
			return WxResponseUtil.fail(ORDER_DEL_OPERATION);
		}
		// 订单order_status没有字段用于标识删除
		// 而是存在专门的delete字段表示是否删除
		orderService.deleteById(orderId);

		logger.info("【请求结束】删除订单成功！");
		return ResponseUtil.ok();
	}

	/**
	 * 待评价订单商品信息
	 *
	 * @param userId
	 *            用户ID
	 * @param orderId
	 *            订单ID
	 * @param goodsId
	 *            商品ID
	 * @return 待评价订单商品信息
	 */
	public Object goods(Integer userId, Integer orderId, Integer goodsId) {
		if (userId == null) {
			logger.error("获取待评价订单商品订单失败：用户未登录!");
			return ResponseUtil.unlogin();
		}

		List<DtsOrderGoods> orderGoodsList = orderGoodsService.findByOidAndGid(orderId, goodsId);
		int size = orderGoodsList.size();

		Assert.state(size < 2, "存在多个符合条件的订单商品");

		if (size == 0) {
			return ResponseUtil.badArgumentValue();
		}

		DtsOrderGoods orderGoods = orderGoodsList.get(0);

		logger.info("【请求结束】获取待评价订单商品订单成功！");
		return ResponseUtil.ok(orderGoods);
	}

	/**
	 * 评价订单商品
	 * <p>
	 * 确认商品收货或者系统自动确认商品收货后7天内可以评价，过期不能评价。
	 *
	 * @param userId
	 *            用户ID
	 * @param body
	 *            订单信息，{ orderId：xxx }
	 * @return 订单操作结果
	 */
	public Object comment(Integer userId, String body) {
		if (userId == null) {
			logger.error("评价订单商品失败：用户未登录!");
			return ResponseUtil.unlogin();
		}

		Integer orderGoodsId = JacksonUtil.parseInteger(body, "orderGoodsId");
		if (orderGoodsId == null) {
			return ResponseUtil.badArgument();
		}
		DtsOrderGoods orderGoods = orderGoodsService.findById(orderGoodsId);
		if (orderGoods == null) {
			return ResponseUtil.badArgumentValue();
		}
		Integer orderId = orderGoods.getOrderId();
		DtsOrder order = orderService.findById(orderId);
		if (order == null) {
			return ResponseUtil.badArgumentValue();
		}

		if (!OrderUtil.isConfirmStatus(order) && !OrderUtil.isAutoConfirmStatus(order)) {
			logger.error("评价订单商品失败：{}", ORDER_NOT_COMMENT.desc());
			return WxResponseUtil.fail(ORDER_NOT_COMMENT);
		}
		if (!order.getUserId().equals(userId)) {
			logger.error("评价订单商品失败：{}", ORDER_INVALID.desc());
			return WxResponseUtil.fail(ORDER_INVALID);
		}
		Integer commentId = orderGoods.getComment();
		if (commentId == -1) {
			logger.error("评价订单商品失败：{}", ORDER_COMMENT_EXPIRED.desc());
			return WxResponseUtil.fail(ORDER_COMMENT_EXPIRED);
		}
		if (commentId != 0) {
			logger.error("评价订单商品失败：{}", ORDER_COMMENTED.desc());
			return WxResponseUtil.fail(ORDER_COMMENTED);
		}

		String content = JacksonUtil.parseString(body, "content");
		Integer star = JacksonUtil.parseInteger(body, "star");
		if (star == null || star < 0 || star > 5) {
			return ResponseUtil.badArgumentValue();
		}
		Boolean hasPicture = JacksonUtil.parseBoolean(body, "hasPicture");
		List<String> picUrls = JacksonUtil.parseStringList(body, "picUrls");
		if (hasPicture == null || !hasPicture) {
			picUrls = new ArrayList<>(0);
		}

		// 1. 创建评价
		DtsComment comment = new DtsComment();
		comment.setUserId(userId);
		comment.setType((byte) 0);
		comment.setValueId(orderGoods.getGoodsId());
		comment.setStar(star.shortValue());
		comment.setContent(content);
		comment.setHasPicture(hasPicture);
		comment.setPicUrls(picUrls.toArray(new String[] {}));
		commentService.save(comment);

		// 2. 更新订单商品的评价列表
		orderGoods.setComment(comment.getId());
		orderGoodsService.updateById(orderGoods);

		// 3. 更新订单中未评价的订单商品可评价数量
		Short commentCount = order.getComments();
		if (commentCount > 0) {
			commentCount--;
		}
		order.setComments(commentCount);
		orderService.updateWithOptimisticLocker(order);

		logger.info("【请求结束】评价订单商品成功！");
		return ResponseUtil.ok();
	}

	/**
	 * 推广订单列表
	 *
	 * @param userId
	 *            用户代理用户ID
	 * @param showType
	 *            订单信息： 0，全部订单； 1，有效订单； 2，失效订单； 3，结算订单； 4，待结算订单。
	 * @param page
	 *            分页页数
	 * @param size
	 *            分页大小
	 * @return 推广订单列表
	 */
	public Object settleOrderList(Integer userId, Integer showType, Integer page, Integer size) {
		if (userId == null) {
			logger.error("获取推广订单列表失败：用户未登录!");
			return ResponseUtil.unlogin();
		}
		List<Short> orderStatus = OrderUtil.settleOrderStatus(showType);
		List<Short> settlementStatus = OrderUtil.settlementStatus(showType);

		List<DtsOrder> orderList = accountService.querySettlementOrder(userId, orderStatus, settlementStatus, page,
				size);
		long count = PageInfo.of(orderList).getTotal();
		int totalPages = (int) Math.ceil((double) count / size);

		List<Map<String, Object>> orderVoList = new ArrayList<>(orderList.size());
		for (DtsOrder order : orderList) {
			Map<String, Object> orderVo = new HashMap<>();
			orderVo.put("id", order.getId());
			orderVo.put("orderSn", order.getOrderSn());
			orderVo.put("actualPrice", order.getActualPrice());
			orderVo.put("orderStatusText", OrderUtil.orderStatusText(order));
			orderVo.put("handleOption", OrderUtil.build(order));

			List<DtsOrderGoods> orderGoodsList = orderGoodsService.queryByOid(order.getId());
			List<Map<String, Object>> orderGoodsVoList = new ArrayList<>(orderGoodsList.size());
			for (DtsOrderGoods orderGoods : orderGoodsList) {
				Map<String, Object> orderGoodsVo = new HashMap<>();
				orderGoodsVo.put("id", orderGoods.getId());
				orderGoodsVo.put("goodsName", orderGoods.getGoodsName());
				orderGoodsVo.put("number", orderGoods.getNumber());
				orderGoodsVo.put("picUrl", orderGoods.getPicUrl());
				orderGoodsVoList.add(orderGoodsVo);
			}
			orderVo.put("goodsList", orderGoodsVoList);

			orderVoList.add(orderVo);
		}

		Map<String, Object> result = new HashMap<>();
		result.put("count", count);
		result.put("data", orderVoList);
		result.put("totalPages", totalPages);

		logger.info("【请求结束】获取推广订单列表成功！,推广订单数：{}", orderVoList.size());
		return ResponseUtil.ok(result);
	}

	/**
	 * 分页查询取款结算记录
	 *
	 * @param userId
	 * @param page
	 * @param size
	 * @return
	 */
	public Object extractList(Integer userId, Integer page, Integer size) {
		if (userId == null) {
			logger.error("分页查询取款结算记录失败：用户未登录!");
			return ResponseUtil.unlogin();
		}

		List<DtsAccountTrace> accountTraceList = accountService.queryAccountTraceList(userId, page, size);
		long count = PageInfo.of(accountTraceList).getTotal();
		int totalPages = (int) Math.ceil((double) count / size);

		Map<String, Object> result = new HashMap<>();
		result.put("count", count);
		result.put("accountTraceList", accountTraceList);
		result.put("totalPages", totalPages);

		logger.info("【请求结束】获取推广订单列表成功！,推广订单数：{}", count);
		return ResponseUtil.ok(result);
	}

	/**
	 * 订单物流跟踪
	 *
	 * @param userId
	 * @param orderId
	 * @return
	 */
	public Object expressTrace(Integer userId, @NotNull Integer orderId) {
		if (userId == null) {
			logger.error("订单物流跟踪失败：用户未登录!");
			return ResponseUtil.unlogin();
		}

		// 订单信息
		DtsOrder order = orderService.findById(orderId);
		if (order == null) {
			logger.error("订单物流跟踪失败：{}", ORDER_UNKNOWN.desc());
			return WxResponseUtil.fail(ORDER_UNKNOWN);
		}
		if (!order.getUserId().equals(userId)) {
			logger.error("订单物流跟踪失败：{}", ORDER_INVALID.desc());
			return WxResponseUtil.fail(ORDER_INVALID);
		}

		Map<String, Object> result = new HashMap<>();
		DateTimeFormatter dateSdf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
		DateTimeFormatter timeSdf = DateTimeFormatter.ofPattern("HH:mm");

		result.put("shipDate", dateSdf.format(order.getShipTime()));
		result.put("shipTime", timeSdf.format(order.getShipTime()));
		result.put("shipperCode", order.getShipSn());
		result.put("address", order.getAddress());

		result.put("shipperName",order.getShipChannel());//临时写法  目前未对接物流快递
		// "YTO", "800669400640887922"
//		if (order.getOrderStatus().equals(OrderUtil.STATUS_SHIP)) {
//			ExpressInfo ei = expressService.getExpressInfo(order.getShipChannel(), order.getShipSn());
//			if (ei != null) {
//				result.put("state", ei.getState());
//				result.put("shipperName", ei.getShipperName());
//				List<Traces> eiTrace = ei.getTraces();
//				List<Map<String, Object>> traces = new ArrayList<Map<String, Object>>();
//				for (Traces trace : eiTrace) {
//					Map<String, Object> traceMap = new HashMap<String, Object>();
//					traceMap.put("date", trace.getAcceptTime().substring(0, 10));
//					traceMap.put("time", trace.getAcceptTime().substring(11, 16));
//					traceMap.put("acceptTime", trace.getAcceptTime());
//					traceMap.put("acceptStation", trace.getAcceptStation());
//					traces.add(traceMap);
//				}
//				result.put("traces", traces);
//			}
//		}
		if(order.getOrderStatus().equals(OrderUtil.STATUS_SHIP)){//订单状态为发货状态
			DtsLogisticsTracking dtsLogisticsTracking = new DtsLogisticsTracking();
			dtsLogisticsTracking.setMobile(order.getMobile());//收货人手机号
			dtsLogisticsTracking.setNumber(order.getShipSn());//快递单号
			dtsLogisticsTracking.setType("auto");
			String request = orderService.orderTracking(dtsLogisticsTracking);
			result.put("result",request);
		}

		logger.info("【请求结束】订单物流跟踪成功！");
		return ResponseUtil.ok(result);

	}

	public Object destructionOrder(String body) {
		String orderSn = JacksonUtil.parseString(body, "orderSn");
		Integer shopId = JacksonUtil.parseInteger(body,"shopId");
		if(StringUtils.isEmpty(orderSn)){
			return ResponseUtil.fail(500,"请选择需要核销的订单");
		}

		DtsOrder dtsOrder = orderService.findBySn(orderSn);
		if(StringUtils.isEmpty(dtsOrder)){
			return ResponseUtil.fail(500,"该订单不存在,请核实");
		}
		if(!dtsOrder.getShopId().equals(shopId)){
			return ResponseUtil.fail(500,"该订单不属于该商家,请核实");
		}
		List<DtsOrderGoods> dtsOrderGoodsList = orderGoodsService.queryByOid(dtsOrder.getId());
		DtsGoods dtsGoods = dtsGoodsService.findById(dtsOrderGoodsList.get(0).getGoodsId());
		if(!dtsGoods.getIsShopConsumption()){
			return ResponseUtil.fail(500,"非到店消费产品,请核实");
		}
		if(!dtsOrder.getOrderStatus().equals(OrderUtil.STATUS_PAY)){
			return ResponseUtil.fail(500,"无法核销,请支付后再核销该订单");
		}
		dtsOrder.setOrderStatus(OrderUtil.STATUS_CONFIRM);//已收货
		if(orderService.updateWithOptimisticLocker(dtsOrder) == 1){
			return ResponseUtil.ok();
		}
		return ResponseUtil.fail(500,"核销失败,请联系管理员");
	}
}