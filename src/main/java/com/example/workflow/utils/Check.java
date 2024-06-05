package com.example.workflow.utils;

import java.lang.reflect.Field;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Check {
    /**
     * @param object 对象
     * @param attributeName 对象中某属性名称
     * @return
     */
    public static boolean hasAttribute(Object object, String attributeName) {
        Class<?> clazz = object.getClass();
        Field[] fields = clazz.getDeclaredFields();

        for (Field field : fields) {
            if (field.getName().equals(attributeName)) {
                return true;
            }
        }

        return false;
    }


    /** 判断对象是否为空*/
    public static boolean noNull(Object... list) {
        for (Object object : list) {
            if (object == null) {
                return false;
            }
        }
        return true;
    }


    /**
     * 检查密码强度
     * @param password 待检查的密码字符串
     * @return 如果密码强度满足要求返回true，否则返回false
     */
    public static boolean isStrongPassword(String password) {
        // 定义密码强度的正则表达式
        String regex = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*]).{6,}$";

        // 编译正则表达式
        Pattern pattern = Pattern.compile(regex);

        // 使用正则表达式对密码进行匹配
        Matcher matcher = pattern.matcher(password);

        // 如果匹配成功，说明密码强度满足要求
        return matcher.matches();
    }

}
