package com.lincanbin.carbonforum.config;
public class APIAddress
{
	public static final String WEBSITE_PATH = ""; //网站路径，不以"/"结尾，默认留空

        //Debug
        public static final String DOMAIN_NAME = "http://192.168.191.1" + WEBSITE_PATH; // PC版域名
        public static final String MOBILE_DOMAIN_NAME = "http://192.168.191.1" + WEBSITE_PATH; // 移动版域名
        public static final String BASIC_API_URL = "http://192.168.191.1" + WEBSITE_PATH; // API地址前缀

	/*
        //Real
        public static final String DOMAIN_NAME = "https://www.94cb.com" + WEBSITE_PATH; // PC版域名
        public static final String MOBILE_DOMAIN_NAME = "https://m.94cb.com" + WEBSITE_PATH; // 移动版域名
        public static final String BASIC_API_URL = "https://api.94cb.com" + WEBSITE_PATH; // API地址前缀
     */
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

	//获取消息提醒
	public static final String NOTIFICATIONS_URL = BASIC_API_URL + "/notifications";

	//推送接口，维护一个长连接
	public static final String PUSH_SERVICE_URL = BASIC_API_URL + "/json/get_notifications";

	//回复接口
	public static final String REPLY_URL = BASIC_API_URL + "/reply";
}
