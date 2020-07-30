package com.xuezhy.mapdrawdemo.bean;

public class LocationEvent extends BaseEvent{

    public LocationEvent(int code) {
        super(code);
    }

    public LocationEvent(int code, Object data) {
        super(code, data);
    }
}
