package com.lincanbin.carbonforum.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;
import android.webkit.MimeTypeMap;

import com.lincanbin.carbonforum.LoginActivity;
import com.lincanbin.carbonforum.application.CarbonForumApplication;
import com.lincanbin.carbonforum.config.APIAddress;

import org.json.JSONObject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class HttpUtil {
    protected Context context;
    private long connectTimeout = 130;
    private long writeTimeout = 130;
    private long readTimeout = 130;
    // private String proxyHost = null;
    // private Integer proxyPort = null;

    private OkHttpClient client;

    public HttpUtil(Context context) {
        this.context = context;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(connectTimeout, TimeUnit.SECONDS)
                .writeTimeout(writeTimeout, TimeUnit.SECONDS)
                .readTimeout(readTimeout, TimeUnit.SECONDS)
                .build();
    }

    // 访问服务器，返回json对象
    public JSONObject request(String method, String url, Map<String, String> parameterMap, Boolean enableSession, Boolean loginRequired) {
        try {
            Log.d(method + " URL : ", url);

            if (parameterMap == null) {
                parameterMap = new HashMap<>();
            }
            Request.Builder builder = new Request.Builder();
            String requestString = buildRequestString(parameterMap, loginRequired);
            Log.d(method + " parameter", requestString);
            if (method.equalsIgnoreCase("GET")) {
                builder.url(url + "?" + requestString);
            } else {
                RequestBody requestBody = buildRequestBody(parameterMap, loginRequired);
                builder.url(url).method(method, requestBody);
            }

            String cookie = getCookie();
            if (enableSession && cookie != null) {
                builder.addHeader("Cookie", cookie);
            }

            Request request = builder.build();
            Response response = this.client.newCall(request).execute();
            String getResult = response.body().string();

            switch (response.code()) {
                case 200:
                case 301:
                case 302:
                case 404:
                    break;
                case 403:
                    Log.d("Configuration error", "API_KEY or API_SECRET or system time error.");
                    return null;
                case 401:
                    this.context.getSharedPreferences("UserInfo", Activity.MODE_PRIVATE).edit().clear().apply();
                    Intent intent = new Intent(context, LoginActivity.class);
                    this.context.startActivity(intent);
                    break;
                case 500:
                    Log.d(method + " Result", "Code 500");
                    return null;
                default:
                    throw new Exception("HTTP Request is not success, Response code is " + response.code());
            }
            if (enableSession) {
                String CookieValue = response.header("Cookie", "");
                Boolean saveCookieResult = saveCookie(CookieValue);
                if (saveCookieResult) {
                    Log.d(" Save Cookie success", CookieValue);
                } else {
                    Log.d(" Save Cookie failed", CookieValue);
                }
            }
            Log.d(method + " URL : ", url);
            Log.d(method + " Result", getResult);
            return JSONUtil.jsonString2Object(getResult);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    //获取之前保存的Cookie
    private String getCookie() {
        SharedPreferences mySharedPreferences = this.context.getSharedPreferences("Session",
                Activity.MODE_PRIVATE);
        try {
            return mySharedPreferences.getString("Cookie", "");
        } catch (ClassCastException e) {
            e.printStackTrace();
            return null;
        }
    }

    //保存Cookie
    private Boolean saveCookie(String cookie) {
        //将Cookie保存起来
        SharedPreferences mySharedPreferences = this.context.getSharedPreferences("Session",
                Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = mySharedPreferences.edit();
        editor.putString("Cookie", cookie);
        editor.apply();
        return true;
    }

    private static Map<String, String> buildRequestMap(Map<String, String> parameterMap, Boolean loginRequired) {
        String currentTimeStamp = String.valueOf(System.currentTimeMillis() / 1000);

        parameterMap.put("SKey", APIAddress.API_KEY);
        parameterMap.put("STime", currentTimeStamp);
        parameterMap.put("SValue", MD5Util.md5(APIAddress.API_KEY + APIAddress.API_SECRET + currentTimeStamp));
        if (loginRequired && CarbonForumApplication.isLoggedIn()) {
            parameterMap.put(
                    "AuthUserID",
                    CarbonForumApplication.userInfo.getString("UserID", "")
            );
            parameterMap.put(
                    "AuthUserExpirationTime",
                    CarbonForumApplication.userInfo.getString("UserExpirationTime", "")
            );
            parameterMap.put(
                    "AuthUserCode",
                    CarbonForumApplication.userInfo.getString("UserCode", "")
            );
        }
        return parameterMap;
    }

    private static RequestBody buildRequestBody(Map<String, String> parameterMap, Boolean loginRequired) {
        parameterMap = buildRequestMap(parameterMap, loginRequired);
        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM);
        if (parameterMap != null) {
            Iterator iterator = parameterMap.keySet().iterator();
            String key;
            String value;
            while (iterator.hasNext()) {
                key = (String) iterator.next();
                if (parameterMap.get(key) == null) {
                    value = "";
                } else {
                    value = parameterMap.get(key);
                }
                File file = new File(value);
                if (file.exists()) {
                    builder.addFormDataPart(key, file.getName(), RequestBody.create(getMediaType(file), file));
                    builder.addFormDataPart(key, value);
                } else {
                    builder.addFormDataPart(key, value);
                }
            }
        }
        return builder.build();
    }

    private static MediaType getMediaType(File file) {
        String url = file.getAbsolutePath();
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return MediaType.parse(type);
    }

    private static String buildRequestString(Map<String, String> parameterMap, Boolean loginRequired) {
        parameterMap = buildRequestMap(parameterMap, loginRequired);
        StringBuilder parameterBuffer = new StringBuilder();
        if (parameterMap != null) {
            Iterator iterator = parameterMap.keySet().iterator();
            String key;
            String value;
            while (iterator.hasNext()) {
                key = (String) iterator.next();
                if (parameterMap.get(key) == null) {
                    value = "";
                } else {
                    try {
                        value = URLEncoder.encode(parameterMap.get(key), "UTF-8");
                    } catch (UnsupportedEncodingException e) {
                        value = parameterMap.get(key);
                        e.printStackTrace();
                    }

                }

                parameterBuffer.append(key.contains("#") ? key.substring(0, key.indexOf("#")) : key).append("=").append(value);
                if (iterator.hasNext()) {
                    parameterBuffer.append("&");
                }
            }
        }
        return parameterBuffer.toString();
    }
}