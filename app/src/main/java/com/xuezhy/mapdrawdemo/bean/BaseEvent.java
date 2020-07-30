package com.xuezhy.mapdrawdemo.bean;

/**
 * 类功能描述:
 * 作者:        zhongyangxue
 * 创建时间:     2019-12-31 15:34
 * 邮箱         1366411749@qq.com
 * 版本:        1.0
 */
public class BaseEvent<T> {
    private int code;
    private T data;

    public BaseEvent(int code) {
        this.code = code;
    }

    public BaseEvent(int code, T data) {
        this.code = code;
        this.data = data;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}