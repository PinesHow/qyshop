package com.qiguliuxing.dts.admin.web;

import com.qiguliuxing.dts.admin.annotation.RequiresPermissionsDesc;
import com.qiguliuxing.dts.admin.service.AdminOrderService;
import com.qiguliuxing.dts.core.util.ResponseUtil;
import com.qiguliuxing.dts.core.validator.Order;
import com.qiguliuxing.dts.core.validator.Sort;
import com.qiguliuxing.dts.db.domain.DtsAdmin;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.constraints.NotNull;
import java.util.List;

@RestController
@RequestMapping("/admin/order")
@Validated
public class AdminOrderController {
	private static final Logger logger = LoggerFactory.getLogger(AdminOrderController.class);

	@Autowired
	private AdminOrderService adminOrderService;

	/**
	 * 查询订单
	 *
	 * @param userId
	 * @param orderSn
	 * @param orderStatusArray
	 * @param page
	 * @param limit
	 * @param sort
	 * @param order
	 * @return
	 */
	@RequiresPermissions("admin:order:list")
	@RequiresPermissionsDesc(menu = { "商品管理", "订单管理" }, button = "查询")
	@GetMapping("/list")
	public Object list(Integer userId, String orderSn, @RequestParam(required = false) List<Short> orderStatusArray,
			@RequestParam(defaultValue = "1") Integer page, @RequestParam(defaultValue = "10") Integer limit,
			@Sort @RequestParam(defaultValue = "add_time") String sort,
			@Order @RequestParam(defaultValue = "desc") String order) {
		logger.info("【请求开始】商品管理->订单管理->查询,请求参数:userId:{},orderSn:{},page:{}", userId, orderSn, page);
		DtsAdmin dtsAdmin = (DtsAdmin) SecurityUtils.getSubject().getPrincipal();
		Integer shopId = dtsAdmin.getShopId();
		return adminOrderService.list(userId, orderSn, orderStatusArray, page, limit, sort, order,shopId);
	}

	/**
	 * 订单详情
	 *
	 * @param id
	 * @return
	 */
	@RequiresPermissions("admin:order:read")
	@RequiresPermissionsDesc(menu = { "商品管理", "订单管理" }, button = "详情")
	@GetMapping("/detail")
	public Object detail(@NotNull Integer id) {
		logger.info("【请求开始】商品管理->订单管理->详情,请求参数:id:{}", id);

		return adminOrderService.detail(id);
	}

	/**
	 * 订单退款
	 *
	 * @param body 订单信息，{ orderId：xxx }
	 * @return 订单退款操作结果
	 */
	@RequiresPermissions("admin:order:refund")
	@RequiresPermissionsDesc(menu = { "商品管理", "订单管理" }, button = "订单退款")
	@PostMapping("/refund")
	public Object refund(@RequestBody String body) {
		logger.info("【请求开始】商品管理->订单管理->订单退款,请求参数,body:{}", body);

		return adminOrderService.refund(body);
	}

	/**
	 * 订单退款成功或失败回调接口
	 *
	 * @param request
	 *            请求内容
	 * @param response
	 *            响应内容
	 * @return 操作结果
	 */
	@PostMapping("/dtsNotify")
	public Object dtsNotify(HttpServletRequest request, HttpServletResponse response) {
		logger.info("【请求开始】订单退款成功或失败回调...");
		return adminOrderService.dtsNotify(request,response);
	}

	/**
	 * 发货
	 *
	 * @param body 订单信息，{ orderId：xxx, shipSn: xxx, shipChannel: xxx }
	 * @return 订单操作结果
	 */
	@RequiresPermissions("admin:order:ship")
	@RequiresPermissionsDesc(menu = { "商品管理", "订单管理" }, button = "订单发货")
	@PostMapping("/ship")
	public Object ship(@RequestBody String body) {
		logger.info("【请求开始】商品管理->订单管理->订单发货,请求参数,body:{}", body);

		return adminOrderService.ship(body);
	}

	/**
	 * 回复订单商品
	 *
	 * @param body 订单信息，{ orderId：xxx }
	 * @return 订单操作结果
	 */
	@RequiresPermissions("admin:order:reply")
	@RequiresPermissionsDesc(menu = { "商品管理", "订单管理" }, button = "订单商品回复")
	@PostMapping("/reply")
	public Object reply(@RequestBody String body) {
		logger.info("【请求开始】商品管理->订单管理->订单商品回复,请求参数,body:{}", body);

		return adminOrderService.reply(body);
	}

	/**
	 * 订单跟踪
	 * @param orderId
	 * @return
	 */
	@RequiresPermissions("admin:order:tracking")
	@RequiresPermissionsDesc(menu = { "商品管理", "订单管理" }, button = "订单跟踪")
	@GetMapping("/tracking")
	public Object orderTracking(@RequestParam("id") Integer orderId){
		logger.info("【请求开始】商品管理->订单管理->订单跟踪,请求参数,dtsLogisticsTracking:{}", orderId);
		if(StringUtils.isEmpty(orderId)){
			return ResponseUtil.fail(500,"请选择需要查询的订单");
		}

		return adminOrderService.orderTracking(orderId);
	}

}
