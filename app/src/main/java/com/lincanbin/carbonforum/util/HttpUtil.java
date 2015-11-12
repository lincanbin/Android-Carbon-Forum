package com.lincanbin.carbonforum.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.lincanbin.carbonforum.LoginActivity;
import com.lincanbin.carbonforum.application.CarbonForumApplication;
import com.lincanbin.carbonforum.config.APIAddress;

import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.Iterator;
import java.util.Map;

public class HttpUtil {
    private static String charset = "utf-8";
    private Integer connectTimeout = null;
    private Integer socketTimeout = null;
    private String proxyHost = null;
    private Integer proxyPort = null;

    // get方法访问服务器，返回json对象
    public static JSONObject getRequest(Context context, String url, Map<String, String> parameterMap, Boolean enableSession, Boolean loginRequired) {
        try {
            Log.d("GET URL : ", url);
            String parameterString = buildParameterString(parameterMap, loginRequired);
            Log.d("GET parameter", parameterString);

            URL localURL = new URL(url + "?" + parameterString);

            URLConnection connection = localURL.openConnection();

            HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
            httpURLConnection.setRequestProperty("Accept-Charset", charset);
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            String cookie = getCookie(context);
            if(enableSession && cookie != null){
                httpURLConnection.setRequestProperty("Cookie", cookie);
            }
            InputStream inputStream = null;
            InputStreamReader inputStreamReader = null;
            BufferedReader reader = null;
            StringBuilder resultBuffer = new StringBuilder();
            String tempLine = null;

            switch (httpURLConnection.getResponseCode()){
                case 200:
                case 301:
                case 302:
                case 404:
                    break;
                case 403:
                    Log.d("Configuration error", "API_KEY or API_SECRET or system time error.");
                    return null;
                case 401:
                    context.getSharedPreferences("UserInfo",Activity.MODE_PRIVATE).edit().clear().apply();
                    Intent intent = new Intent(context, LoginActivity.class);
                    context.startActivity(intent);
                    break;
                case 500:
                    Log.d("Get Result","Code 500");
                    return null;
                default:
                    throw new Exception("HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
            }
            if(enableSession) {
                saveCookie(context, httpURLConnection);
            }
            try {
                inputStream = httpURLConnection.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                reader = new BufferedReader(inputStreamReader);

                while ((tempLine = reader.readLine()) != null) {
                    resultBuffer.append(tempLine);
                }

                String getResult = resultBuffer.toString();
                Log.d("Get URL : ", url);
                Log.d("Get Result",getResult);
                return JSONUtil.jsonString2Object(getResult);
            }  catch (Exception e) {
                e.printStackTrace();
                return null;
            } finally {
                if (reader != null) {
                    reader.close();
                }

                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }

                if (inputStream != null) {
                    inputStream.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // post方法访问服务器，返回json对象
    public static JSONObject postRequest(Context context, String url, Map<String, String> parameterMap, Boolean enableSession, Boolean loginRequired) {
        try{
            Log.d("POST URL : ", url);
            String parameterString = buildParameterString(parameterMap, loginRequired);
            Log.d("POST parameter", parameterString);

            final URL localURL = new URL(url);

            URLConnection connection = localURL.openConnection();

            HttpURLConnection httpURLConnection = (HttpURLConnection) connection;
            /*
            // http://developer.android.com/training/articles/security-ssl.html
            // Create an HostnameVerifier that hardwires the expected hostname.
            // Note that is different than the URL's hostname:
            // example.com versus example.org
            HostnameVerifier hostnameVerifier = new HostnameVerifier() {
                @Override
                public boolean verify(String hostname, SSLSession session) {
                    HostnameVerifier hv =
                            HttpsURLConnection.getDefaultHostnameVerifier();
                    return hv.verify(localURL.getHost(), session);
                }
            };
            httpURLConnection.setHostnameVerifier(hostnameVerifier);
            */
            httpURLConnection.setConnectTimeout(15000);
            if(url.equals(APIAddress.PUSH_SERVICE_URL)) {
                httpURLConnection.setReadTimeout(360000);
            }else{
                httpURLConnection.setReadTimeout(25000);
            }
            // 设置是否向httpUrlConnection输出，因为这个是post请求，参数要放在
            // http正文内，因此需要设为true, 默认情况下是false;
            httpURLConnection.setDoOutput(true);
            // 设置是否从httpUrlConnection读入，默认情况下是true;
            httpURLConnection.setDoInput(true);
            httpURLConnection.setInstanceFollowRedirects(true);//允许重定向
            // Post 请求不能使用缓存
            httpURLConnection.setUseCaches(false);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Accept-Charset", charset);
            /*
            if (Build.VERSION.SDK_INT < 21) {
                // http://stackoverflow.com/questions/17638398/androids-httpurlconnection-throws-eofexception-on-head-requests
                // Fixed known bug in Android's class implementation
                httpURLConnection.setRequestProperty("Accept-Encoding", "");
            }
            */
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(parameterString.length()));
            String cookie = getCookie(context);
            if(enableSession && cookie != null){
                httpURLConnection.setRequestProperty("Cookie", cookie);
            }
            /*
            if (Build.VERSION.SDK_INT < 21) {
                //http://stackoverflow.com/questions/15411213/android-httpsurlconnection-eofexception
                // Fixed bug with recycled url connections in versions of android.
                httpURLConnection.setRequestProperty("Connection", "close");
            }
            */
            String tempLine = null;
            OutputStream outputStream = httpURLConnection.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);//现在通过输出流对象构建对象输出流对象，以实现输出可序列化的对象。
            outputStreamWriter.write(parameterString);// 向对象输出流写出数据，这些数据将存到内存缓冲区中
            outputStreamWriter.flush();// 刷新对象输出流，将任何字节都写入潜在的流中（些处为ObjectOutputStream）
            outputStreamWriter.close();
            outputStream.close();

            switch (httpURLConnection.getResponseCode()){
                case HttpURLConnection.HTTP_OK:
                case 301:
                case 302:
                case 404:
                    break;
                case 403:
                    Log.d("Configuration error", "API_KEY or API_SECRET or system time error.");
                    return null;
                case 401:
                    Log.d("Post Result","Code 401");
                    CarbonForumApplication.userInfo.edit().clear().apply();
                    Intent intent = new Intent(context, LoginActivity.class);
                    context.startActivity(intent);
                    break;
                case 500:
                    Log.d("Post Result","Code 500");
                    return null;
                default:
                    throw new Exception("HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
            }
            if(enableSession) {
                saveCookie(context, httpURLConnection);
            }
            InputStream inputStream = httpURLConnection.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            StringBuilder resultBuffer = new StringBuilder();

            while ((tempLine = reader.readLine()) != null) {
                resultBuffer.append(tempLine);
            }
            reader.close();
            inputStreamReader.close();
            inputStream.close();

            //httpURLConnection.disconnect();//断开连接
            String postResult = resultBuffer.toString();
            Log.d("Post Result",postResult);
            JSONTokener jsonParser = new JSONTokener(postResult);
            return (JSONObject) jsonParser.nextValue();

        } catch (Exception e) {
            Log.d("Post Error", "No Network");
            e.printStackTrace();
            return null;
        }
    }

    //获取之前保存的Cookie
    public static String getCookie(Context context){
        SharedPreferences mySharedPreferences= context.getSharedPreferences("Session",
                Activity.MODE_PRIVATE);
        try{
            return  mySharedPreferences.getString("Cookie", "");
        } catch (ClassCastException e) {
            e.printStackTrace();
            return null;
        }
    }

    //保存Cookie
    public static Boolean saveCookie(Context context, URLConnection connection){
        //获取Cookie
        String headerName=null;
        for (int i=1; (headerName = connection.getHeaderFieldKey(i))!=null; i++) {
            if (headerName.equals("Set-Cookie")) {
                String cookie = connection.getHeaderField(i);
                //将Cookie保存起来
                SharedPreferences mySharedPreferences = context.getSharedPreferences("Session",
                        Activity.MODE_PRIVATE);
                SharedPreferences.Editor editor = mySharedPreferences.edit();
                editor.putString("Cookie", cookie);
                editor.apply();
                return true;
            }
        }
        return false;
    }

    public static String buildParameterString(Map<String, String> parameterMap, Boolean loginRequired){
        /* Translate parameter map to parameter date string */
        StringBuilder parameterBuffer = new StringBuilder();
        String currentTimeStamp = String.valueOf(System.currentTimeMillis() / 1000);
        parameterBuffer
                .append("SKey").append("=")
                .append(APIAddress.API_KEY)
                .append("&")
                .append("STime").append("=")
                .append(currentTimeStamp)
                .append("&")
                .append("SValue").append("=")
                .append(MD5Util.md5(APIAddress.API_KEY + APIAddress.API_SECRET + currentTimeStamp));

        if(loginRequired && CarbonForumApplication.isLoggedIn()){
            parameterBuffer
                    .append("&")
                    .append("AuthUserID").append("=")
                    .append(CarbonForumApplication.userInfo.getString("UserID", ""))
                    .append("&")
                    .append("AuthUserExpirationTime").append("=")
                    .append(CarbonForumApplication.userInfo.getString("UserExpirationTime", ""))
                    .append("&")
                    .append("AuthUserCode").append("=")
                    .append(CarbonForumApplication.userInfo.getString("UserCode", ""));
        }
        if (parameterMap != null) {
            parameterBuffer.append("&");
            Iterator iterator = parameterMap.keySet().iterator();
            String key = null;
            String value = null;
            while (iterator.hasNext()) {
                key = (String) iterator.next();
                if (parameterMap.get(key) != null) {
                    try {
                        value = URLEncoder.encode(parameterMap.get(key), "UTF-8");
                    }catch(UnsupportedEncodingException e){
                        value = parameterMap.get(key);
                        e.printStackTrace();
                    }
                } else {
                    value = "";
                }
                parameterBuffer.append(key.contains("#") ? key.substring(0, key.indexOf("#")) : key).append("=").append(value);
                if (iterator.hasNext()) {
                    parameterBuffer.append("&");
                }
            }
        }
        return parameterBuffer.toString();
    }

    private URLConnection openConnection(URL localURL) throws IOException {
        URLConnection connection;
        if (proxyHost != null && proxyPort != null) {
            Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyHost, proxyPort));
            connection = localURL.openConnection(proxy);
        } else {
            connection = localURL.openConnection();
        }
        return connection;
    }

    private void renderRequest(URLConnection connection) {

        if (connectTimeout != null) {
            connection.setConnectTimeout(connectTimeout);
        }

        if (socketTimeout != null) {
            connection.setReadTimeout(socketTimeout);
        }

    }

    /*
     * Getter & Setter
     */
    public Integer getConnectTimeout() {
        return connectTimeout;
    }

    public void setConnectTimeout(Integer connectTimeout) {
        this.connectTimeout = connectTimeout;
    }

    public Integer getSocketTimeout() {
        return socketTimeout;
    }

    public void setSocketTimeout(Integer socketTimeout) {
        this.socketTimeout = socketTimeout;
    }

    public String getProxyHost() {
        return proxyHost;
    }

    public void setProxyHost(String proxyHost) {
        this.proxyHost = proxyHost;
    }

    public Integer getProxyPort() {
        return proxyPort;
    }

    public void setProxyPort(Integer proxyPort) {
        this.proxyPort = proxyPort;
    }

    public String getCharset() {
        return charset;
    }

    public void setCharset(String charset) {
        HttpUtil.charset = charset;
    }
}