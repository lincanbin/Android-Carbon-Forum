package com.lincanbin.carbonforum.config;
public class ApiAddress
{
	public static final String DOMAIN_NAME = "http://192.168.137.1:8087"; // PC版域名
	public static final String MOBILE_DOMAIN_NAME = "http://192.168.137.1:8087"; // 移动版域名
	public static final String BASIC_API_URL = "http://192.168.137.1:8087"; // API地址前缀

	//中等头像地址
	public static final String MIDDLE_AVATAR_URL = DOMAIN_NAME + "/upload/avatar/middle/";

	//首页帖子列表API地址
	public static final String HOME_URL = BASIC_API_URL + "/page/";

}
