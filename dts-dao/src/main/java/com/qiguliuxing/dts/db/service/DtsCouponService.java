package com.qiguliuxing.dts.db.service;

import com.alibaba.druid.util.StringUtils;
import com.github.pagehelper.PageHelper;
import com.qiguliuxing.dts.db.dao.DtsCouponMapper;
import com.qiguliuxing.dts.db.dao.DtsCouponUserMapper;
import com.qiguliuxing.dts.db.domain.DtsCoupon;
import com.qiguliuxing.dts.db.domain.DtsCoupon.Column;
import com.qiguliuxing.dts.db.domain.DtsCouponExample;
import com.qiguliuxing.dts.db.domain.DtsCouponUser;
import com.qiguliuxing.dts.db.domain.DtsCouponUserExample;
import com.qiguliuxing.dts.db.util.CouponConstant;
import com.qiguliuxing.dts.db.util.CouponUserConstant;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class DtsCouponService {
	@Resource
	private DtsCouponMapper couponMapper;
	@Resource
	private DtsCouponUserMapper couponUserMapper;

	private Set couponUserLock = new HashSet();

	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

	private Column[] result = new Column[] { Column.id, Column.name, Column.desc, Column.tag, Column.days,Column.type,
			Column.startTime, Column.endTime, Column.discount, Column.min,Column.dtsType,Column.shopId,Column.percentage,Column.url };

	/**
	 * 查询
	 *
	 * @param offset
	 * @param limit
	 * @param sort
	 * @param order
	 * @return
	 */
	public List<DtsCoupon> queryList(int offset, int limit, String sort, String order) {
		return queryList(DtsCouponExample.newAndCreateCriteria(), offset, limit, sort, order);
	}

	/**
	 * 查询
	 *
	 * @param criteria
	 *            可扩展的条件
	 * @param offset
	 * @param limit
	 * @param sort
	 * @param order
	 * @return
	 */
	public List<DtsCoupon> queryList(DtsCouponExample.Criteria criteria, int offset, int limit, String sort,
			String order) {
		criteria.andStatusEqualTo(CouponConstant.STATUS_NORMAL)
				.andDeletedEqualTo(false);
		criteria.example().setOrderByClause(sort + " " + order);
		PageHelper.startPage(offset, limit);
		List<DtsCoupon> dtsCoupons = couponMapper.selectByExampleSelective(criteria.example(), result);
		//临时写法，返回商铺名称
//		List<DtsCoupon> dtsCouponList = new ArrayList<>();
//		for(DtsCoupon dtsCoupon:dtsCoupons){
//			Integer shopId = dtsCoupon.getShopId();
//			DtsShop dtsShop = dtsShopMapper.selectShopById(shopId);
//			dtsCoupon.setShopName(dtsShop.getShopName());
//			dtsCouponList.add(dtsCoupon);
//		}
		return dtsCoupons;
	}

	public int queryTotal() {
		List<Short> typeList = new ArrayList<>();
		typeList.add(CouponConstant.TYPE_COMMON);
		typeList.add(CouponConstant.TYPE_COMMON2);
		DtsCouponExample example = new DtsCouponExample();
		example.or().andTypeIn(typeList).andStatusEqualTo(CouponConstant.STATUS_NORMAL)
				.andDeletedEqualTo(false);
		return (int) couponMapper.countByExample(example);
	}

	public List<DtsCoupon> queryAvailableList(Integer userId, int offset, int limit) {
		assert userId != null;
		// 过滤掉登录账号已经领取过的coupon
		synchronized (this){
			boolean flag = couponUserLock.contains(userId);
			if(flag){
				return null;
			}else{
				couponUserLock.add(userId);
			}
		}
		try {
			DtsCouponExample.Criteria c = DtsCouponExample.newAndCreateCriteria();
			List<DtsCouponUser> used = couponUserMapper.selectByExample(DtsCouponUserExample.newAndCreateCriteria().andUserIdEqualTo(userId).example());
			Map<Integer, List<DtsCouponUser>> collect = used.stream().collect(Collectors.groupingBy(t -> t.getCouponId()));
			if (used != null && !used.isEmpty()) {
				List<Integer> couponIds = new ArrayList<>();
				for(DtsCouponUser dtsCouponUser:used){
//					LocalDate endTime = dtsCouponUser.getEndTime();//优惠券过期时间
////					LocalDateTime addTime = dtsCouponUser.getAddTime();//领取时间
////					boolean flag = endTime.isAfter(LocalDate.now());//过期时间和当前服务器时间对比
////					DtsCoupon dtsCoupon = couponMapper.selectByPrimaryKey(dtsCouponUser.getCouponId());//当前优惠券
////					Short couponLimit = dtsCoupon.getLimit();
////					if(flag){//不进该判断就可以再领券 (表示过期时间在当前时间之后)
////						couponIds.add(dtsCouponUser.getCouponId());
////					}else{//进该判断，还需再判断限领张数
////						Short couponUserListSize = (short)collect.get(dtsCouponUser.getCouponId()).size();
////						if(!couponIds.equals(CouponUserConstant.STATUS_USABLE)){//非无限领取
////							if(couponUserListSize >= couponLimit){
////								couponIds.add(dtsCouponUser.getCouponId());
////							}
////						}else{//无限领取
////
////						}
////
////					}
					Integer couponId = dtsCouponUser.getCouponId();
					DtsCoupon dtsCoupon = couponMapper.selectByPrimaryKey(couponId);//当前优惠券
					Short couponLimit = dtsCoupon.getLimit();//优惠券限领
					List<DtsCouponUser> dtsCouponUserList = collect.get(couponId);
					if(couponLimit.equals(CouponUserConstant.STATUS_USABLE)){//无限领
						for(DtsCouponUser dtsCouponUserTmp:dtsCouponUserList){
							LocalDate addTime = dtsCouponUserTmp.getAddTime().toLocalDate();//优惠券领取时间
							LocalDate endTimeDate = dtsCouponUser.getEndTime();
							LocalDate nowTime = LocalDateTime.now().toLocalDate();//当前服务器 日期
							if(nowTime.equals(addTime)){//当前服务器日期 在 优惠券领取日期是同一天  今天已领取过  不可以领
								couponIds.add(couponId);
							}else{//未领取过
								LocalDate nowTimeDate = LocalDate.now();
								if(nowTimeDate.isBefore(endTimeDate)){//当前服务器日期 在 优惠券过期日期之前  未过期
									Short status = dtsCouponUser.getStatus();//用户拥有该优惠券的状态
									if(status.equals(CouponUserConstant.STATUS_USABLE)){//当前拥有的该优惠券未使用  不可以领
										couponIds.add(couponId);
									}
								}
							}
						}
					}else{//限领
						Short couponUserLimit = (short) dtsCouponUserList.size();//用户拥有当前优惠券数量
						LocalDate nowTime = LocalDateTime.now().toLocalDate();//当前服务器日期
						for(DtsCouponUser dtsCouponUserTmp:dtsCouponUserList){
							LocalDate endTime = dtsCouponUserTmp.getEndTime();//优惠券过期时间
							LocalDate addTime = dtsCouponUserTmp.getAddTime().toLocalDate();//优惠券领取日期
							if(nowTime.equals(addTime)){//当前服务器时间 在 领取时间是同一天  今天已领取过   不可以领
								couponIds.add(couponId);
							}else{//非当前领取情况  看优惠券是否过期 如果过期可以领 未过期再判断是否使用 如果使用可以领  未使用不可以领
								if(nowTime.isBefore(endTime)){//当前时间在过期时间之前  未过期
									if(dtsCouponUserTmp.getStatus().equals(CouponUserConstant.STATUS_USABLE)){
										couponIds.add(couponId);
									}
								}
							}
						}
						if((couponLimit <= couponUserLimit) && !couponIds.contains(couponId)){//用户拥有当前优惠券数量大于等于 限领张数 不可领取该优惠券
							couponIds.add(couponId);
						}
					}
				}
				if(couponIds.size() > 0){
					c.andIdNotIn(couponIds);
				}
//			c.andIdNotIn(used.stream().map(DtsCouponUser::getCouponId).collect(Collectors.toList()));
			}
			return queryList(c, offset, limit, "add_time", "desc");
		}catch (Exception e){
			e.printStackTrace();
			return null;
		}finally {
			couponUserLock.remove(userId);
		}

	}

	public List<DtsCoupon> queryList(int offset, int limit) {
		return queryList(offset, limit, "add_time", "desc");
	}

	public DtsCoupon findById(Integer id) {
		return couponMapper.selectByPrimaryKey(id);
	}

	public DtsCoupon findByCode(String code) {
		DtsCouponExample example = new DtsCouponExample();
		example.or().andCodeEqualTo(code).andTypeEqualTo(CouponConstant.TYPE_CODE)
				.andStatusEqualTo(CouponConstant.STATUS_NORMAL).andDeletedEqualTo(false);
		List<DtsCoupon> couponList = couponMapper.selectByExample(example);
		if (couponList.size() > 1) {
			throw new RuntimeException("");
		} else if (couponList.size() == 0) {
			return null;
		} else {
			return couponList.get(0);
		}
	}

	/**
	 * 查询新用户注册优惠券
	 *
	 * @return
	 */
	public List<DtsCoupon> queryRegister() {
		DtsCouponExample example = new DtsCouponExample();
		example.or().andTypeEqualTo(CouponConstant.TYPE_REGISTER).andStatusEqualTo(CouponConstant.STATUS_NORMAL)
				.andDeletedEqualTo(false);
		return couponMapper.selectByExample(example);
	}

	public List<DtsCoupon> querySelective(String name,Integer shopId, Short type, Short status, Integer page, Integer limit,
			String sort, String order) {
		DtsCouponExample example = new DtsCouponExample();
		DtsCouponExample.Criteria criteria = example.createCriteria();

		if (!StringUtils.isEmpty(name)) {
			criteria.andNameLike("%" + name + "%");
		}
		if (type != null) {
			criteria.andTypeEqualTo(type);
		}
		if(shopId != null){
			criteria.andShopIdEqualTo(shopId);
		}
		if (status != null) {
			criteria.andStatusEqualTo(status);
		}
		criteria.andDeletedEqualTo(false);

		if (!StringUtils.isEmpty(sort) && !StringUtils.isEmpty(order)) {
			example.setOrderByClause(sort + " " + order);
		}

		PageHelper.startPage(page, limit);
		return couponMapper.selectByExample(example);
	}

	public void add(DtsCoupon coupon) {
		coupon.setAddTime(LocalDateTime.now());
		coupon.setUpdateTime(LocalDateTime.now());
		couponMapper.insertSelective(coupon);
	}

	public int updateById(DtsCoupon coupon) {
		coupon.setUpdateTime(LocalDateTime.now());
		return couponMapper.updateByPrimaryKeySelective(coupon);
	}

	public void deleteById(Integer id) {
		couponMapper.logicalDeleteByPrimaryKey(id);
	}

	private String getRandomNum(Integer num) {
		String base = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
		base += "0123456789";

		Random random = new Random();
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < num; i++) {
			int number = random.nextInt(base.length());
			sb.append(base.charAt(number));
		}
		return sb.toString();
	}

	/**
	 * 生成优惠码
	 *
	 * @return 可使用优惠码
	 */
	public String generateCode() {
		String code = getRandomNum(8);
		while (findByCode(code) != null) {
			code = getRandomNum(8);
		}
		return code;
	}

	/**
	 * 查询过期的优惠券: 注意：如果timeType=0, 即基于领取时间有效期的优惠券，则优惠券不会过期
	 *
	 * @return
	 */
	public List<DtsCoupon> queryExpired() {
		DtsCouponExample example = new DtsCouponExample();
		example.or().andStatusEqualTo(CouponConstant.STATUS_NORMAL).andTimeTypeEqualTo(CouponConstant.TIME_TYPE_TIME)
				.andEndTimeLessThan(LocalDate.now()).andDeletedEqualTo(false);
		return couponMapper.selectByExample(example);
	}

	public List<DtsCoupon> queryExpiredTwo() {
		DtsCouponExample example = new DtsCouponExample();
		example.or().andStatusEqualTo(CouponConstant.STATUS_NORMAL).andTimeTypeEqualTo(CouponConstant.TIME_TYPE_TIME)
				.andEndTimeEqualTo(LocalDate.now()).andDeletedEqualTo(false);
		return couponMapper.selectByExample(example);
	}

	/**
	 * 查询用户的优惠券
	 * @param userId
	 * @return
	 */
	public int queryUserCouponCnt(Integer userId) {
		DtsCouponUserExample example = new DtsCouponUserExample();
		DtsCouponUserExample.Criteria criteria = example.createCriteria();
		criteria.andUserIdEqualTo(userId);
		criteria.andDeletedEqualTo(false);
		return (int) couponUserMapper.countByExample(example);
	}

	public void updateCouponStatus(Integer id, LocalDateTime now) {
		couponUserMapper.updateCouponStatusById(id,now);
	}

}
