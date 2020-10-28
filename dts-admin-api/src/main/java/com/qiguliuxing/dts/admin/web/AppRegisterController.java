/**
 * Copyright (c) 2016-2019 人人开源 All rights reserved.
 *
 * https://www.renren.io
 *
 * 版权所有，侵权必究！
 */

package com.qiguliuxing.dts.admin.web;


import com.qiguliuxing.dts.admin.form.RegForm;
import com.qiguliuxing.dts.admin.form.RegisterForm;
import com.qiguliuxing.dts.admin.service.UserService;
import com.qiguliuxing.dts.admin.util.R;
import com.qiguliuxing.dts.core.util.HttpCheckCodeRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

/**
 * 注册
 *
 * @author Mark software8888@163.com
 */
@RestController
@RequestMapping("/admin/app")
public class AppRegisterController {
    @Autowired
    private UserService userService;

    @Autowired
    private HttpCheckCodeRequest httpCheckCodeRequest;

    @PostMapping("register")
//    @ApiOperation("注册")
    public R register(@RequestBody RegisterForm form){
        //表单校验
//        ValidatorUtils.validateEntity(form);
//        DtsAdminEntity user = new DtsAdminEntity();
//        user.setTel(form.getMobile());
//        user.setUsername(form.getMobile());
//        user.setPassword(DigestUtils.sha256Hex(form.getPassword()));
//        user.setAddTime(java.time.LocalDateTime.now());
//        userService.save(user);
//        return R.ok();
        return R.ok();
    }
    @PostMapping("reg")
    public R reg(@RequestBody RegForm regForm){
            if(userService.reg(regForm)){
                return  R.ok("注册成功!");
            }
            return R.error("验证失败!");
    }

    @GetMapping("getRegCode")
    public R getRegCode(String tell){
        try {
               return R.ok(userService.getRegCode(tell));
        } catch (UnsupportedEncodingException e) {
            return  R.error("获取验证码异常!");
        }
    }

}
