package com.qiguliuxing.dts.admin.service;

import com.alibaba.fastjson.JSONObject;
import com.github.binarywang.wxpay.bean.notify.WxPayNotifyResponse;
import com.github.binarywang.wxpay.bean.notify.WxPayRefundNotifyResult;
import com.github.binarywang.wxpay.bean.request.WxPayRefundRequest;
import com.github.binarywang.wxpay.bean.result.WxPayRefundResult;
import com.github.binarywang.wxpay.exception.WxPayException;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.pagehelper.PageInfo;
import com.qiguliuxing.dts.admin.util.AdminResponseUtil;
import com.qiguliuxing.dts.admin.util.OrderTrackingUtils;
import com.qiguliuxing.dts.core.notify.NotifyService;
import com.qiguliuxing.dts.core.notify.NotifyType;
import com.qiguliuxing.dts.core.util.JacksonUtil;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.db.dao.DtsShopDao;
import com.qiguliuxing.dts.db.domain.*;
import com.qiguliuxing.dts.db.service.*;
import com.qiguliuxing.dts.db.util.OrderUtil;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.qiguliuxing.dts.admin.util.AdminResponseCode.*;

@Service
public class AdminOrderService {
	private static final Logger logger = LoggerFactory.getLogger(AdminOrderService.class);

	@Value("${orderTracking.url}")
	private String orderTrackingUrl;
	@Value("${orderTracking.secretId}")
	private String secretId;
	@Value("${orderTracking.secretKey}")
	private String secretKey;

	private String source = "market";

	@Autowired
	private DtsOrderGoodsService orderGoodsService;
	@Autowired
	private DtsOrderService orderService;
	@Autowired
	private DtsGoodsProductService productService;
	@Autowired
	private DtsUserService userService;
	@Autowired
	private DtsCommentService commentService;
	@Autowired
	private WxPayService wxPayService;
	@Autowired
	private DtsGoodsService dtsGoodsService;
	@Autowired
	private DtsShopDao dtsShopDao;
//	@Autowired
//	private HttpCheckCodeRequest httpCheckCodeRequest;

	@Autowired
	private NotifyService notifyService;

	public Object list(Integer userId, String orderSn, List<Short> orderStatusArray, Integer page, Integer limit,
			String sort, String order,Integer shopId) {
		List<DtsOrder> orderList = orderService.querySelective(userId, orderSn, orderStatusArray, page, limit, sort,
				order,shopId);
		long total = PageInfo.of(orderList).getTotal();
		List<DtsOrderVo> dtsOrderVoList = new ArrayList<>();
		for(DtsOrder dtsOrder:orderList){
			List<DtsOrderGoods> dtsOrderGoodsList = orderGoodsService.queryByOid(dtsOrder.getId());
//			DtsGoods dtsGoods = dtsGoodsService.findById(dtsOrderGoodsList.get(0).getGoodsId());
			DtsGoods dtsGoods = dtsGoodsService.queryById(dtsOrderGoodsList.get(0).getGoodsId());
			DtsOrderVo dtsOrderVo = new DtsOrderVo();
			dtsOrderVo.setDtsOrder(dtsOrder);
			List<Integer> shopTypeList = orderGoodsService.selectShopTypeByOrderId(dtsOrder.getId());
			if(shopTypeList.size() == 0 && shopTypeList.get(0).equals(3)){//订单里只有旅游产品
				dtsOrderVo.setType(1);
			}else if(shopTypeList.size() == 0 && shopTypeList.get(0).equals(2)){//订单里只有住宿产品
				dtsOrderVo.setType(1);
			}else{//混合商品  或者 纯普通商品
				dtsOrderVo.setType(0);
			}
			dtsOrderVo.setOrderStatusText(OrderUtil.orderStatusText(dtsOrder));
			dtsOrderVo.setIsShopConsumption(dtsGoods.getIsShopConsumption());
			dtsOrderVoList.add(dtsOrderVo);
		}


		Map<String, Object> data = new HashMap<>();
		data.put("total", total);
		data.put("items", dtsOrderVoList);

		logger.info("【请求结束】商场管理->订单管理->查询,响应结果:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}

	public Object detail(Integer id) {
		DtsOrder order = orderService.findById(id);
		List<DtsOrderGoods> orderGoods = orderGoodsService.queryByOid(id);
		if(StringUtils.isEmpty(order)){
			return ResponseUtil.fail(500,"该订单不存在");
		}
		UserVo user = userService.findUserVoById(order.getUserId());
		DtsRefundTrace dtsRefundTrace = orderService.findRefundTraceByOrderId(order.getId());
		Map<String, Object> data = new HashMap<>();
		data.put("order", order);
		data.put("orderGoods", orderGoods);
		data.put("user", user);
		if(!StringUtils.isEmpty(dtsRefundTrace)){
			data.put("dtsRefundTrace",dtsRefundTrace);
		}

		logger.info("【请求结束】商场管理->订单管理->详情,响应结果:{}", JSONObject.toJSONString(data));
		return ResponseUtil.ok(data);
	}

	/**
	 * 订单退款
	 * <p>
	 * 1. 检测当前订单是否能够退款; 2. 微信退款操作; 3. 设置订单退款确认状态； 4. 订单商品库存回库。
	 * <p>
	 * TODO 虽然接入了微信退款API，但是从安全角度考虑，建议开发者删除这里微信退款代码，采用以下两步走步骤： 1.
	 * 管理员登录微信官方支付平台点击退款操作进行退款 2. 管理员登录Dts管理后台点击退款操作进行订单状态修改和商品库存回库
	 *
	 * @param body 订单信息，{ orderId：xxx }
	 * @return 订单退款操作结果
	 */
	@Transactional(rollbackFor = Exception.class)
	public Object refund(String body) {
		Integer orderId = JacksonUtil.parseInteger(body, "orderId");
		Integer status = JacksonUtil.parseInteger(body,"status");//审批状态
		String approveMsg = JacksonUtil.parseString(body,"approveMsg");//审批内容
//		String refundMoney = JacksonUtil.parseString(body, "refundMoney");
		if (orderId == null) {
			return ResponseUtil.badArgument();
		}
//		if (StringUtils.isEmpty(refundMoney)) {
//			return ResponseUtil.badArgument();
//		}

		if(StringUtils.isEmpty(status)){
			return ResponseUtil.badArgument();
		}

		DtsOrder order = orderService.findById(orderId);
		if (order == null) {
			return ResponseUtil.badArgument();
		}

//		if (order.getActualPrice().compareTo(new BigDecimal(refundMoney)) != 0) {
//			return ResponseUtil.badArgumentValue();
//		}

		// 如果订单不是退款状态，则不能退款
		if (!order.getOrderStatus().equals(OrderUtil.STATUS_REFUND)) {
			logger.info("商场管理->订单管理->订单退款失败:{}", ORDER_REFUND_FAILED.desc());
			return AdminResponseUtil.fail(ORDER_REFUND_FAILED);
		}

		//更新退款跟踪信息
		DtsRefundTrace dtsRefundTrace = new DtsRefundTrace();
		dtsRefundTrace.setOrderId(orderId);
		dtsRefundTrace.setStatus(status);
		dtsRefundTrace.setApproveMsg(approveMsg);
		orderService.updateRefundTraceByOrderId(dtsRefundTrace);
		//如果是退款拒绝状态,则以下微信退款代码不运行
		if(status == 2){//退款拒绝
			order.setOrderStatus(OrderUtil.STATUS_REFUND_NOT);
			if (orderService.updateWithOptimisticLocker(order) == 0) {
				logger.info("商场管理->订单管理->订单退款失败:{}", "更新数据已失效");
				throw new RuntimeException("更新数据已失效");
			}
			return AdminResponseUtil.ok("退款审批成功");
		}

		// 微信退款
		WxPayRefundRequest wxPayRefundRequest = new WxPayRefundRequest();
		wxPayRefundRequest.setTransactionId(order.getPayId());
//		wxPayRefundRequest.setOutTradeNo(order.getOrderSn());
		wxPayRefundRequest.setOutRefundNo("refund_" + order.getPayId());//退款单号 = refund_ + 微信订单号
		wxPayRefundRequest.setNotifyUrl("https://www.gzwakjaqjy.cn/admin/order/dtsNotify");//设置回调接口地址
		// 元转成分
		Integer orderPrice = order.getOrderPrice().multiply(new BigDecimal(100)).intValue();//订单金额
		Integer actualPrice = order.getActualPrice().multiply(new BigDecimal(100)).intValue();//实付金额
		wxPayRefundRequest.setTotalFee(orderPrice);//订单金额
		wxPayRefundRequest.setRefundFee(actualPrice);//退款金额
		/**
		 * TODO 服务商模式
		 * 服务商模式下   退款设置子商户号
		 */
		DtsShop dtsShop = dtsShopDao.getShopColumnInfo(order.getShopId());
		wxPayRefundRequest.setSubMchId(dtsShop.getSubMchId());//设置子商户号

		  //为了账号安全，暂时屏蔽api退款
	 	WxPayRefundResult wxPayRefundResult = null;

	  try {
		 wxPayRefundResult = wxPayService.refund(wxPayRefundRequest);
		} catch(WxPayException e) {
	  	logger.info("订单退款失败,异常信息:"+e.getReturnMsg());
			e.printStackTrace();
			logger.info("退款失败原因："+e.getReturnMsg());
			throw new RuntimeException("订单退款失败,异常信息:"+e.getReturnMsg());
//			return ResponseUtil.fail(621, "订单退款失败");
		}
		if(!wxPayRefundResult.getReturnCode().equals("SUCCESS")) {
			logger.warn("refund fail: " + wxPayRefundResult.getReturnMsg());
			throw new RuntimeException("订单退款失败");
//			return ResponseUtil.fail(621, "订单退款失败");
		}
		if(!wxPayRefundResult.getResultCode().equals("SUCCESS")) {
			logger.warn("refund fail: " + wxPayRefundResult.getReturnMsg());
			throw new RuntimeException("订单退款失败");
//			return ResponseUtil.fail(621, "订单退款失败");
		}

		// 设置订单取消状态
		order.setOrderStatus(OrderUtil.STATUS_REFUND_CONFIRM);
		if (orderService.updateWithOptimisticLocker(order) == 0) {
			logger.info("商场管理->订单管理->订单退款失败:{}", "更新数据已失效");
			throw new RuntimeException("更新数据已失效");
		}

		// 商品货品数量增加
		List<DtsOrderGoods> orderGoodsList = orderGoodsService.queryByOid(orderId);
		for (DtsOrderGoods orderGoods : orderGoodsList) {
			Integer productId = orderGoods.getProductId();
			Short number = orderGoods.getNumber();
			if (productService.addStock(productId, number) == 0) {
				logger.info("商场管理->订单管理->订单退款失败:{}", "商品货品库存增加失败");
				throw new RuntimeException("商品货品库存增加失败");
			}
		}

		// TODO 发送邮件和短信通知，这里采用异步发送
		// 退款成功通知用户, 例如“您申请的订单退款 [ 单号:{1} ] 已成功，请耐心等待到账。”
		// 注意订单号只发后6位
		// notifyService.notifySmsTemplate(order.getMobile(), NotifyType.REFUND,new String[] { order.getOrderSn().substring(8, 14) });

		logger.info("【请求结束】商场管理->订单管理->订单退款,响应结果:{}", "成功!");
		return ResponseUtil.ok();
	}

	/**
	 * 发货 1. 检测当前订单是否能够发货 2. 设置订单发货状态
	 *
	 * @param body 订单信息，{ orderId：xxx, shipSn: xxx, shipChannel: xxx }
	 * @return 订单操作结果 成功则 { errno: 0, errmsg: '成功' } 失败则 { errno: XXX, errmsg: XXX }
	 */
	public Object ship(String body) {
		Integer orderId = JacksonUtil.parseInteger(body, "orderId");
		String shipSn = JacksonUtil.parseString(body, "shipSn");
		String shipChannel = JacksonUtil.parseString(body, "shipChannel");
		if (orderId == null || shipSn == null || shipChannel == null) {
			return ResponseUtil.badArgument();
		}

		DtsOrder order = orderService.findById(orderId);
		if (order == null) {
			return ResponseUtil.badArgument();
		}

		// 如果订单不是已付款状态，则不能发货
		if (!order.getOrderStatus().equals(OrderUtil.STATUS_PAY)) {
			logger.info("商场管理->订单管理->订单发货失败:{}", ORDER_CONFIRM_NOT_ALLOWED.desc());
			return AdminResponseUtil.fail(ORDER_CONFIRM_NOT_ALLOWED);
		}

		order.setOrderStatus(OrderUtil.STATUS_SHIP);
		order.setShipSn(shipSn);
		order.setShipChannel(shipChannel);
		order.setShipTime(LocalDateTime.now());
		if (orderService.updateWithOptimisticLocker(order) == 0) {
			logger.info("商场管理->订单管理->订单发货失败:{}", "更新数据失败!");
			return ResponseUtil.updatedDateExpired();
		}

		// TODO 发送邮件和短信通知，这里采用异步发送
		// 发货会发送通知短信给用户: *
		// "您的订单已经发货，快递公司 {1}，快递单 {2} ，请注意查收"
		notifyService.notifySmsTemplate(order.getMobile(), NotifyType.SHIP, new String[] { shipChannel, shipSn });

		logger.info("【请求结束】商场管理->订单管理->订单发货,响应结果:{}", "成功!");
		return ResponseUtil.ok();
	}

	/**
	 * 回复订单商品
	 *
	 * @param body 订单信息，{ orderId：xxx }
	 * @return 订单操作结果 成功则 { errno: 0, errmsg: '成功' } 失败则 { errno: XXX, errmsg: XXX }
	 */
	public Object reply(String body) {
		Integer commentId = JacksonUtil.parseInteger(body, "commentId");
		if (commentId == null || commentId == 0) {
			return ResponseUtil.badArgument();
		}
		// 目前只支持回复一次
		if (commentService.findById(commentId) != null) {
			logger.info("商场管理->订单管理->订单商品回复:{}", ORDER_REPLY_EXIST.desc());
			return AdminResponseUtil.fail(ORDER_REPLY_EXIST);
		}
		String content = JacksonUtil.parseString(body, "content");
		if (StringUtils.isEmpty(content)) {
			return ResponseUtil.badArgument();
		}
		// 创建评价回复
		DtsComment comment = new DtsComment();
		comment.setType((byte) 2);
		comment.setValueId(commentId);
		comment.setContent(content);
		comment.setUserId(0); // 评价回复没有用
		comment.setStar((short) 0); // 评价回复没有用
		comment.setHasPicture(false); // 评价回复没有用
		comment.setPicUrls(new String[] {}); // 评价回复没有用
		commentService.save(comment);

		logger.info("【请求结束】商场管理->订单管理->订单商品回复,响应结果:{}", "成功!");
		return ResponseUtil.ok();
	}

	/**
	 * 订单跟踪，查询物流信息
	 * @param orderId
	 * @return
	 */
	public Object orderTracking(Integer orderId) {
		try {
			DtsOrder order = orderService.findById(orderId);
			if(StringUtils.isEmpty(order)){
				return ResponseUtil.fail(500,"该订单不存在");
			}
			DtsLogisticsTracking dtsLogisticsTracking = new DtsLogisticsTracking();
			dtsLogisticsTracking.setMobile(order.getMobile());
			dtsLogisticsTracking.setNumber(order.getShipSn());
			dtsLogisticsTracking.setType("auto");
			String result = OrderTrackingUtils.getResult(dtsLogisticsTracking,secretId,secretKey,source,orderTrackingUrl);
			if(StringUtils.isEmpty(result)){
				return ResponseUtil.fail(500,"查询订单跟踪失败，请重试");
			}
			return result;
		}catch (Exception e){
			return ResponseUtil.fail(500,"查询订单异常，请联系管理员");
		}
	}

	/**
	 * 订单退款成功或失败回调接口
	 * <p>
	 * 1. 检测订单是否为退款中(orderStatus = 202)状态, 2.检验退款跟踪表是否为同意退款状态(status=1) 3. 更改退款跟踪为退款完成状态以及退款时间, 4. 更改订单状态为已退款, 5. 响应微信商户平台
	 *
	 * @param request
	 *            请求内容
	 * @param response
	 *            响应内容
	 * @return 操作结果
	 */
	@Transactional(rollbackFor = Exception.class)
	public Object dtsNotify(HttpServletRequest request, HttpServletResponse response) {
		String xmlResult = null;
		try {
			xmlResult = IOUtils.toString(request.getInputStream(), request.getCharacterEncoding());
		} catch (IOException e) {
			logger.error("订单退款成功或失败：{}", "获取回调消息内容错误!");
			e.printStackTrace();
			return WxPayNotifyResponse.fail("获取回调消息内容错误!");
		}
		WxPayRefundNotifyResult result = null;
		try {
			result = wxPayService.parseRefundNotifyResult(xmlResult);
		} catch (WxPayException e) {
			logger.error("订单退款成功或失败：{}", "格式化消息内容错误!");
			e.printStackTrace();
			return WxPayNotifyResponse.fail("格式化消息内容错误!");
		}
		logger.info("处理腾讯支付平台的订单退款：{}", JSONObject.toJSONString(result));

		WxPayRefundNotifyResult.ReqInfo reqInfo = result.getReqInfo();
		String transactionId = reqInfo.getTransactionId();//商户订单号(微信单号)
		DtsOrder dtsOrder = orderService.findByPayId(transactionId);//订单
		if(!dtsOrder.getOrderStatus().equals(OrderUtil.STATUS_REFUND)){
			logger.info("该订单状态不能退款,退款失败:",dtsOrder.getOrderStatus());
			return WxPayNotifyResponse.fail("该订单状态不能退款,退款失败");
		}

		DtsRefundTrace dtsRefundTrace = orderService.findRefundTraceByOrderId(dtsOrder.getId());//退款跟踪
		if(dtsRefundTrace.getStatus() != 1){
			logger.info("该订单退款跟踪状态有误!{}",dtsRefundTrace.getStatus());
			return WxPayNotifyResponse.fail("该订单退款跟踪状态有误!");
		}

		String resultCode = result.getResultCode();//退款是否成功返回值
		if(resultCode.equals("SUCCESS")){//退款成功
			try {
				dtsOrder.setOrderStatus(OrderUtil.STATUS_REFUND_CONFIRM);//订单状态改为已退款
				dtsOrder.setUpdateTime(LocalDateTime.now());
				orderService.updateWithOptimisticLocker(dtsOrder);

				dtsRefundTrace.setStatus(3);
				dtsRefundTrace.setRefundTime(LocalDateTime.now());
				//更新退款跟踪表 状态信息
				orderService.updateRefundTraceByOrderId(dtsRefundTrace);
				return WxPayNotifyResponse.success("SUCCESS");
			}catch (Exception e){
				logger.info("订单、退款跟踪表状态修改失败:" + e.getMessage());
				return WxPayNotifyResponse.fail("订单、退款跟踪表状态修改失败:" + e.getMessage());
			}
		}
		return WxPayNotifyResponse.fail("退款失败");
	}
}
