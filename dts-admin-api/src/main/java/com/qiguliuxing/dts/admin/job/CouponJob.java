package com.qiguliuxing.dts.admin.job;

import com.qiguliuxing.dts.db.domain.DtsCoupon;
import com.qiguliuxing.dts.db.domain.DtsCouponUser;
import com.qiguliuxing.dts.db.service.DtsCouponService;
import com.qiguliuxing.dts.db.service.DtsCouponUserService;
import com.qiguliuxing.dts.db.util.CouponConstant;
import com.qiguliuxing.dts.db.util.CouponUserConstant;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 检测优惠券过期情况
 */
@Component
public class CouponJob {
	private final Log logger = LogFactory.getLog(CouponJob.class);

	@Autowired
	private DtsCouponService couponService;
	@Autowired
	private DtsCouponUserService couponUserService;

	/**
	 * 每隔一个小时检查
	 */
	@Scheduled(fixedDelay = 60 * 60 * 1000)
	public void checkCouponExpired() {
		logger.info("系统开启任务检查优惠券是否已经过期");
		//查询指定绝对时间、可正常使用、未删除，使用券截至时间为当前时间之前的优惠券
		List<DtsCoupon> couponList = couponService.queryExpired();
		for (DtsCoupon coupon : couponList) {
			coupon.setStatus(CouponConstant.STATUS_EXPIRED);
			couponService.updateById(coupon);
		}
		//查询可正常使用、未删除、使用券过期时间为当前时间之前的优惠券
		List<DtsCouponUser> couponUserList = couponUserService.queryExpired();
		for (DtsCouponUser couponUser : couponUserList) {
			couponUser.setStatus(CouponUserConstant.STATUS_EXPIRED);
			couponUserService.update(couponUser);
		}
	}

	/**
	 * 每天凌晨12点，是否有当天过期优惠券
	 */
	@Scheduled(cron = "0 0 0 * * ?")
	public void checkCouponExpiredTwo(){
		logger.info("系统开启任务检查当天优惠券是否已经过期");

		//查询指定绝对时间、可正常使用、未删除，使用券截至时间为当天时间的优惠券
		List<DtsCoupon> couponList = couponService.queryExpiredTwo();
		for (DtsCoupon coupon : couponList) {
			coupon.setStatus(CouponConstant.STATUS_EXPIRED);
			couponService.updateById(coupon);
		}
		//查询可正常使用、未删除、使用券过期时间为当天时间的优惠券
		List<DtsCouponUser> couponUserList = couponUserService.queryExpiredTwo();
		for (DtsCouponUser couponUser : couponUserList) {
			couponUser.setStatus(CouponUserConstant.STATUS_EXPIRED);
			couponUserService.update(couponUser);
		}
	}

	public static void main(String[] args) {
		String a = null;
	}

}
