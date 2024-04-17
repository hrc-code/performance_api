package com.example.workflow.utils;

import java.lang.reflect.Field;

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

}
