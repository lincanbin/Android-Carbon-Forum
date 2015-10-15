package com.lincanbin.carbonforum.config;
public class APIAddress
{
/*
	//Debug
	public static final String DOMAIN_NAME = "http://192.168.137.1"; // PC版域名
	public static final String MOBILE_DOMAIN_NAME = "http://192.168.137.1"; // 移动版域名
	public static final String BASIC_API_URL = "http://192.168.137.1"; // API地址前缀
*/

	//Real
	public static final String DOMAIN_NAME = "https://www.94cb.com"; // PC版域名
	public static final String MOBILE_DOMAIN_NAME = "https://m.94cb.com"; // 移动版域名
	public static final String BASIC_API_URL = "https://api.94cb.com"; // API地址前缀

	//中等头像地址
	public static String MIDDLE_AVATAR_URL(String userID, String avatarSize){
        return DOMAIN_NAME + "/upload/avatar/"+ avatarSize +"/" + userID +".png";
    }

	//首页帖子列表API地址
	public static String HOME_URL(int targetPage){
		return BASIC_API_URL + "/page/"+ targetPage;
	}

    public static String TOPIC_URL(int topicID, int targetPage){
        return BASIC_API_URL + "/t/" + topicID + "-" + targetPage;
    }

	//验证码
	public static final String VERIFICATION_CODE = BASIC_API_URL + "/seccode.php";

	//登陆
	public static final String LOGIN_URL = BASIC_API_URL + "/login";

}
