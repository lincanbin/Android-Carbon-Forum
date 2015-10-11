package com.lincanbin.carbonforum.config;
public class APIAddress
{
	/*
	//Debug
	public static final String DOMAIN_NAME = "http://192.168.137.1:8087"; // PC版域名
	public static final String MOBILE_DOMAIN_NAME = "http://192.168.137.1:8087"; // 移动版域名
	public static final String BASIC_API_URL = "http://192.168.137.1:8087"; // API地址前缀
	*/

	//Real
	public static final String DOMAIN_NAME = "https://www.94cb.com"; // PC版域名
	public static final String MOBILE_DOMAIN_NAME = "https://m.94cb.com"; // 移动版域名
	public static final String BASIC_API_URL = "https://api.94cb.com"; // API地址前缀

	//中等头像地址
	public static final String MIDDLE_AVATAR_URL = DOMAIN_NAME + "/upload/avatar/middle/";

	//首页帖子列表API地址
	public static final String HOME_URL = BASIC_API_URL + "/page/";

	//验证码
	public static final String VERIFICATION_CODE = BASIC_API_URL + "/seccode.php";

	//登陆
	public static final String LOGIN_URL = BASIC_API_URL + "/login";

}
