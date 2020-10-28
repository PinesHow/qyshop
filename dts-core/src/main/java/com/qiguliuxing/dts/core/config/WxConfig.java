package com.qiguliuxing.dts.core.config;

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
public class
WxConfig {
	@Autowired
	private WxProperties properties;

	@Bean
	public WxMaConfig wxMaConfig() {
		WxMaInMemoryConfig config = new WxMaInMemoryConfig();
//		config.setAppid(properties.getAppId());//久方式
		config.setAppid(properties.getSubAppId());//新方式
		config.setSecret(properties.getAppSecret());
		return config;
	}

	@Bean
	public WxMaService wxMaService(WxMaConfig wxMaConfig) {
		WxMaService service = new WxMaServiceImpl();
		service.setWxMaConfig(wxMaConfig);
		return service;
	}

	@Bean
	public WxPayConfigChildren wxPayConfigChildren() {
//		WxPayConfig payConfig = new WxPayConfig();
		WxPayConfigChildren payConfig = new WxPayConfigChildren();
		payConfig.setAppId(properties.getAppId());//服务商appid
		payConfig.setMchId(properties.getMchId());
		payConfig.setMchKey(properties.getMchKey());
		payConfig.setNotifyUrl(properties.getNotifyUrl());
		payConfig.setKeyPath(properties.getKeyPath());
		payConfig.setSubAppId(properties.getSubAppId());//公众号appid
//		payConfig.setCombineAppid(properties.getAppId());//合单发起方的appid
//		payConfig.setCombineMchid(properties.getMchId());//合单发起方商户号
		payConfig.setTradeType("JSAPI");
//		payConfig.setPrivateKey(properties.getPrivateKey());
//		payConfig.setSerialNo(properties.getSerialNo());
		payConfig.setSignType("MD5");//签名方式 WECHATPAY2-SHA256-RSA2048
		return payConfig;
	}

	@Bean
	public WxPayConfig wxPayConfig() {
		WxPayConfig payConfig = new WxPayConfig();
//		WxPayConfigChildren payConfig = new WxPayConfigChildren();
		payConfig.setAppId(properties.getAppId());//服务商appid
		payConfig.setMchId(properties.getMchId());
		payConfig.setMchKey(properties.getMchKey());
		payConfig.setNotifyUrl(properties.getNotifyUrl());
		payConfig.setKeyPath(properties.getKeyPath());
		payConfig.setSubAppId(properties.getSubAppId());//公众号appid
//		payConfig.setCombineAppid(properties.getAppId());//合单发起方的appid
//		payConfig.setCombineMchid(properties.getMchId());//合单发起方商户号
		payConfig.setTradeType("JSAPI");
//		payConfig.setPrivateKey(properties.getPrivateKey());
//		payConfig.setSerialNo(properties.getSerialNo());
//		payConfig.setSignType("HMAC-SHA256");//签名方式 WECHATPAY2-SHA256-RSA2048
		payConfig.setSignType("MD5");//签名方式 WECHATPAY2-SHA256-RSA2048
		return payConfig;
	}

	@Bean
	public WxPayService wxPayService(WxPayConfig wxPayConfig) {
		WxPayService wxPayService = new WxPayServiceImpl();
//		WxPayServiceApacheHttpImplChildren wxPayService = new WxPayServiceApacheHttpImplChildren();
//		wxPayService.setChildrenConfig(wxPayConfigChildren);
		wxPayService.setConfig(wxPayConfig);
		return wxPayService;
	}

}