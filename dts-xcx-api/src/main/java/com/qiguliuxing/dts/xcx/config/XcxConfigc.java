package com.qiguliuxing.dts.xcx.config;

import cn.binarywang.wx.miniapp.api.WxMaService;
import cn.binarywang.wx.miniapp.api.impl.WxMaServiceImpl;
import cn.binarywang.wx.miniapp.config.WxMaConfig;
import cn.binarywang.wx.miniapp.config.WxMaInMemoryConfig;
import com.github.binarywang.wxpay.config.WxPayConfig;
import com.github.binarywang.wxpay.service.WxPayService;
import com.github.binarywang.wxpay.service.impl.WxPayServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XcxConfigc {
	@Autowired
	private XcxProperties properties;

	@Bean(name = "xcxMaConfig")
	public WxMaConfig xcxMaConfig() {
		WxMaInMemoryConfig config = new WxMaInMemoryConfig();
		config.setAppid(properties.getAppId());
		config.setSecret(properties.getAppSecret());
		return config;
	}

	@Bean(name = "xcxMaService")
	public WxMaService xcxMaService(WxMaConfig xcxMaConfig) {
		WxMaService service = new WxMaServiceImpl();
		service.setWxMaConfig(xcxMaConfig);
		return service;
	}

	@Bean(name = "xcxPayConfig")
	public WxPayConfig wxPayConfig() {
		WxPayConfig payConfig = new WxPayConfig();
		payConfig.setAppId(properties.getAppId());
		payConfig.setMchId(properties.getMchId());
		payConfig.setMchKey(properties.getMchKey());
		payConfig.setNotifyUrl(properties.getNotifyUrl());
		payConfig.setKeyPath(properties.getKeyPath());
		payConfig.setTradeType("JSAPI");
		payConfig.setSignType("MD5");
		return payConfig;
	}

	@Bean(name = "xcxPayService")
	public WxPayService wxPayService(WxPayConfig xcxPayConfig) {
		WxPayService wxPayService = new WxPayServiceImpl();
		wxPayService.setConfig(xcxPayConfig);
		return wxPayService;
	}
}