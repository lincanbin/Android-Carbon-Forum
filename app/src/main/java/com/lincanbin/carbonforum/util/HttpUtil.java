package com.lincanbin.carbonforum.util;

import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class HttpUtil {
    private static String charset = "utf-8";
    private Integer connectTimeout = null;
    private Integer socketTimeout = null;
    private String proxyHost = null;
    private Integer proxyPort = null;

    // post方法访问服务器，返回json字符串
    public static String getRequest(String url) {
        try {
            URL localURL = new URL(url);

            URLConnection connection = localURL.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) connection;

            httpURLConnection.setRequestProperty("Accept-Charset", charset);
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            InputStream inputStream = null;
            InputStreamReader inputStreamReader = null;
            BufferedReader reader = null;
            StringBuilder resultBuffer = new StringBuilder();
            String tempLine = null;

            if (httpURLConnection.getResponseCode() >= 400) {
                throw new Exception("HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
            }

            try {
                inputStream = httpURLConnection.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                reader = new BufferedReader(inputStreamReader);

                while ((tempLine = reader.readLine()) != null) {
                    resultBuffer.append(tempLine);
                }

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
            return resultBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 字符串转成集合数据
    public static void resultString2List(List<Map<String, Object>> list, String str, String title) {

        try {
            JSONObject jsonObject = new JSONObject(str);
            JSONArray jsonArray = jsonObject.getJSONArray(title);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                Map<String, Object> map = new HashMap<String, Object>();
                Iterator<String> iterator = jsonObject2.keys();
                while (iterator.hasNext()) {
                    String key = iterator.next();
                    Object value = jsonObject2.get(key);
                    map.put(key, value);
                }

                list.add(map);

            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // post方法访问服务器，返回集合数据
    public static List<Map<String, Object>> getRequest2List(String str, String title) {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if(null != str){
            resultString2List(list, str, title);
        }
        return list;

    }

    // get方法访问服务器，返回json字符串
    public static String postRequest(String url, Map<String, String> parameterMap) throws Exception {
        try{
           /* Translate parameter map to parameter date string */
            StringBuilder parameterBuffer = new StringBuilder();
            if (parameterMap != null) {
                Iterator iterator = parameterMap.keySet().iterator();
                String key = null;
                String value = null;
                while (iterator.hasNext()) {
                    key = (String) iterator.next();
                    if (parameterMap.get(key) != null) {
                        value = parameterMap.get(key);
                    } else {
                        value = "";
                    }

                    parameterBuffer.append(key).append("=").append(value);
                    if (iterator.hasNext()) {
                        parameterBuffer.append("&");
                    }
                }
            }

            Log.v("POST parameter : ", parameterBuffer.toString());

            URL localURL = new URL(url);

            URLConnection connection = localURL.openConnection();
            HttpURLConnection httpURLConnection = (HttpURLConnection) connection;

            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setRequestProperty("Accept-Charset", charset);
            httpURLConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            httpURLConnection.setRequestProperty("Content-Length", String.valueOf(parameterBuffer.length()));

            OutputStream outputStream = null;
            OutputStreamWriter outputStreamWriter = null;
            InputStream inputStream = null;
            InputStreamReader inputStreamReader = null;
            BufferedReader reader = null;
            StringBuilder resultBuffer = new StringBuilder();
            String tempLine = null;

            try {
                outputStream = httpURLConnection.getOutputStream();
                outputStreamWriter = new OutputStreamWriter(outputStream);

                outputStreamWriter.write(parameterBuffer.toString());
                outputStreamWriter.flush();

                if (httpURLConnection.getResponseCode() >= 400) {
                    throw new Exception("HTTP Request is not success, Response code is " + httpURLConnection.getResponseCode());
                }

                inputStream = httpURLConnection.getInputStream();
                inputStreamReader = new InputStreamReader(inputStream);
                reader = new BufferedReader(inputStreamReader);

                while ((tempLine = reader.readLine()) != null) {
                    resultBuffer.append(tempLine);
                }

            } finally {

                if (outputStreamWriter != null) {
                    outputStreamWriter.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }

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
            return resultBuffer.toString();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
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