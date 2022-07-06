package com.nowcoder.community.util;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.UUID;
import java.util.Map;

public class CommunityUtil {
    //  封装一些方法

    //  生成随机字符串
    public static String generateUUID() {
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    //  MD5加密
    //  hello -> abc123def456   只能加密，不能解密；且每次hello都是abc123def456
    //  hello + 3e4a8 -> abc123def456abc    3e4a8为了提高安全性，随机字符串
    public static String md5(String key) {   //key: hello + 3e4a8 ;返回abc123def456abc加密结果
        if (StringUtils.isBlank(key)) {
            return null;
        }
        return DigestUtils.md5DigestAsHex(key.getBytes());
    }

    /**
     * @param code 编码
     * @param msg  提示信息
     * @param map  业务数据
     * @return 以上参数封装成的JSON对象，再转换成JSON字符串
     */
    public static String getJSONString(int code, String msg, Map<String, Object> map) {
        JSONObject json = new JSONObject();
        json.put("code", code);
        json.put("msg", msg);
        if (map != null) {
            for (String key : map.keySet()) {     //遍历Map集合
                json.put(key, map.get(key));
            }
        }
        return json.toJSONString();
    }

    //函数重载，因为可能参数传入数量不同
    public static String getJSONString(int code, String msg) {
        return getJSONString(code, msg, null);
    }

    public static String getJSONString(int code) {
        return getJSONString(code, null, null);
    }

    //测试JSON
    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("name", "张三");
        map.put("age", 25);
        System.out.printf(getJSONString(0, "ok", map));
    }
}
