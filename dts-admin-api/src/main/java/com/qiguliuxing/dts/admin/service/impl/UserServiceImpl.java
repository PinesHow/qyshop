/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package com.qiguliuxing.dts.admin.service.impl;


import com.qiguliuxing.dts.admin.form.LoginForm;
import com.qiguliuxing.dts.admin.form.RegForm;
import com.qiguliuxing.dts.admin.service.UserService;
import com.qiguliuxing.dts.admin.util.MD5Utils;
import com.qiguliuxing.dts.core.util.HttpCheckCodeRequest;
import com.qiguliuxing.dts.core.util.bcrypt.BCryptPasswordEncoder;
import com.qiguliuxing.dts.db.dao.DtsAdminDao;
import com.qiguliuxing.dts.db.dao.DtsShopApplyDao;
import com.qiguliuxing.dts.db.dao.DtsShopDao;
import com.qiguliuxing.dts.db.domain.DtsAdminEntity;
import com.qiguliuxing.dts.db.domain.DtsShopApplyEntity;
import com.qiguliuxing.dts.db.domain.DtsShopEntity;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.Assert;

import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Date;


@Service("userService")
public class UserServiceImpl implements UserService {

	@Autowired
	HttpCheckCodeRequest httpCheckCodeRequest;

	@Autowired
    DtsAdminDao dtsAdminDao;

	@Autowired
    DtsShopDao dtsShopDao;

	@Autowired
    DtsShopApplyDao dtsShopApplyDao;


	@Override
	public DtsAdminEntity queryByMobile(String mobile) {
		return dtsAdminDao.selectOne( mobile);
	}

	@Override
	public long login(LoginForm form) throws Exception {
        DtsAdminEntity user = queryByMobile(form.getMobile());
		Assert.isNull(user, "手机号或密码错误");
		//密码错误
		if(!user.getPassword().equals(DigestUtils.sha256Hex(form.getPassword()))){
			throw new Exception("手机号或密码错误");
		}
		return user.getId();
	}

	@Override
	@Transactional
	public boolean reg(RegForm regForm) {
		if(httpCheckCodeRequest.checkCode(regForm.getPhoneNum(),"reg",regForm.getCode())){
			DtsShopEntity shopEntity=new DtsShopEntity();
			shopEntity.setShopName(regForm.getShopName());
			shopEntity.setContactPhone(regForm.getContactPhone());
			shopEntity.setPhoneNum(regForm.getPhoneNum());
			shopEntity.setAddress(regForm.getAddress());
			shopEntity.setLegalPerson(regForm.getLegalPerson());
			shopEntity.setShopType(regForm.getShopTypeId());
			dtsShopDao.insert(shopEntity);
			int shopId=dtsShopDao.selectByTel(regForm.getPhoneNum()).getId();
            DtsAdminEntity adminEntity=new DtsAdminEntity();
			adminEntity.setUsername(regForm.getPhoneNum());
			adminEntity.setTel(regForm.getPhoneNum());
			adminEntity.setShopId(shopId);
			adminEntity.setAddTime(LocalDateTime.now());
			adminEntity.setUpdateTime(LocalDateTime.now());
			adminEntity.setRoleIds("[5]");
			String uuid= MD5Utils.string2Md5(regForm.getPhoneNum()+new Date());
			adminEntity.setQrCode(uuid);
			String smsCode=HttpCheckCodeRequest.smsCode();
			BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
			String encodedPassword = encoder.encode(smsCode);
//			String psdMd5=MD5Utils.string2Md5(smsCode);
			adminEntity.setPassword(encodedPassword);
			dtsAdminDao.insert(adminEntity);
			DtsShopApplyEntity dtsShopApplyEntity=new DtsShopApplyEntity();
			dtsShopApplyEntity.setCheckStatusId(1);
			dtsShopApplyEntity.setShopId(shopId);
			dtsShopApplyDao.insert(dtsShopApplyEntity);
			try {
				httpCheckCodeRequest.toSendRegister(regForm.getPhoneNum(),smsCode);
			} catch (UnsupportedEncodingException e) {
				//手动回滚
				TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();
				return false;
			}
			return true;
		}
		return false;
	}

	@Override
	public String getRegCode(String tell) throws UnsupportedEncodingException {
		DtsShopEntity shopEntity=dtsShopDao.selectByTel(tell);
		if(shopEntity!=null){
			return "该手机号已经注册!";
		}
		return httpCheckCodeRequest.getTellCheckCode(tell,1,"reg");
	}


}
