package com.lincanbin.carbonforum.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by 灿斌 on 10/10/2015.
 */
public class JSONUtil {
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

    // JSON字符串转List
    public static List<Map<String, Object>> jsonDecode(String str, String title) {

        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if(null != str){
            resultString2List(list, str, title);
        }
        return list;

    }
}
