package com.example.workflow.common;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class R<T> {
    private Integer code;
    private Boolean status;
    private String message;
    private T data;
    private Map map=new HashMap();

    private static <T> R<T> response(Integer code, Boolean states, String message, T data) {
        R<T> r=new R<>();
        r.setCode(code);
        r.setStatus(states);
        r.setMessage(message);
        r.setData(data);
        return r;
    }
    private static <T> R<T> response(Integer code, Boolean states, String message) {
        R<T> r=new R<>();
        r.setCode(code);
        r.setStatus(states);
        r.setMessage(message);
        return r;
    }

    public static <T> R<T> success(T data){
        return response(HttpStatusEnum.SUCCESS.getCode(), true, HttpStatusEnum.SUCCESS.getMessage(), data);
    }
    public static <T> R<T> success(){
        return response(HttpStatusEnum.SUCCESS.getCode(), true, HttpStatusEnum.SUCCESS.getMessage());
    }
    public static <T> R<T> error(String msg){
        R r=new R();
        r.message=msg;
        r.code=100;
        return r;
    }
    public static <T> R<T> error(int code, String msg){
        R<T> r = new R<>();
        r.setCode(code);
        r.setMessage(msg);
        return r;
    }
    public static <T> R<T> error(String msg,T data){
        R<T> r=new R();
        r.message=msg;
        r.code=0;
        r.data = data;
        return r;
    }
    public R<T> add(String key,Object value){
        this.map.put(key,value);
        return this;
    }
}
