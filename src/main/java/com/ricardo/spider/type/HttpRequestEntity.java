package com.ricardo.spider.type;

import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * @author ricardo
 * @date 2020/7/20 12:00:46
 * @desc 爬虫请求信息 , 请求路径 ,请求头 ,请求体
 */

@Getter
@Setter
public class HttpRequestEntity {

    private String url;

    //请求头
    private Map<String, String> headParams = new HashMap<>();

    //请求体
    private Map<String, String> bodyParams = new HashMap<>();

    //json格式请求
    private String json;

}
