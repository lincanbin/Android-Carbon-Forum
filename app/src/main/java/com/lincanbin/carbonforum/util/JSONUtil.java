package com.lincanbin.carbonforum.util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * Created by 灿斌 on 10/10/2015.
 */
public class JSONUtil {
    // JSON字符串转List
    public static List<Map<String, Object>> jsonObject2List(JSONObject jsonObject, String keyName) {

        List<Map<String, Object>> list = new ArrayList<>();
        if(null != jsonObject){
            try {
                JSONArray jsonArray = jsonObject.getJSONArray(keyName);

                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject2 = jsonArray.getJSONObject(i);
                    Map<String, Object> map = new HashMap<>();
                    Iterator<String> iterator = jsonObject2.keys();
                    while (iterator.hasNext()) {
                        String key = iterator.next();
                        Object value = jsonObject2.get(key);
                        map.put(key, value);
                    }
                    list.add(map);
                }
                return list;
            } catch (JSONException e) {
                e.printStackTrace();
                return null;
            }
        }else{
            return null;
        }
    }
    public static JSONObject jsonString2Object(String jsonString){
        try {
            JSONTokener jsonParser = new JSONTokener(jsonString);
            return (JSONObject) jsonParser.nextValue();
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
    }
}
