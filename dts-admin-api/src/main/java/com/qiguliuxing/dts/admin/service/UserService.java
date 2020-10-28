/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package com.qiguliuxing.dts.admin.service;


import com.qiguliuxing.dts.admin.form.LoginForm;
import com.qiguliuxing.dts.admin.form.RegForm;
import com.qiguliuxing.dts.db.domain.DtsAdminEntity;

import java.io.UnsupportedEncodingException;

/**
 * 用户
 *
 * @author Mark software8888@163.com
 */
public interface UserService {

	DtsAdminEntity queryByMobile(String mobile);

	/**
	 * 用户登录
	 * @param form    登录表单
	 * @return        返回用户ID
	 */
	long login(LoginForm form) throws Exception;

	boolean reg(RegForm regForm);

	String getRegCode(String tell) throws UnsupportedEncodingException;
}
