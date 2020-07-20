package com.ricardo.spider.util;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpEntityEnclosingRequestBase;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.cookie.Cookie;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import com.ricardo.spider.type.HttpRequestEntity;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author ricardo
 * @date 2020/7/20 11:57:05
 * @desc
 */


@Slf4j
public class HttpRequestUtils {

    public static final int SLEEP_TIME = 30000;
    public static final int HTTP_TIME_OUT = 300000;
    public static final String ENCODE_UTF8 = "UTF-8";
    public static final String LOG_INFO = "===== >> doHttpResponse() Exception is : {}";
    public static final String RES_STR = "response";
    public static final String STS_CODE = "statusCode";
    public static final String LOCATION_STR = "Location";
    public static final String COOKIES_STR = "cookies";

    private static RequestConfig cfg;

//    public static Map<String, Object> post(HttpRequestEntity entity) throws Exception {
//        //结果集
//        Map<String, Object> resultMap = new HashMap<>();
//
//        CookieStore cookieStore = new BasicCookieStore();
//        CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
//
//        HttpPost httpPost = new HttpPost(entity.getUrl());
//
//        //设置请求和传输超时时间
//        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(180000).setConnectTimeout(180000).build();
//        httpPost.setConfig(requestConfig);
//
//        // 设置请求头
//        for (Map.Entry<String, String> entry : entity.getHeadParams().entrySet()) {
//            httpPost.setHeader(entry.getKey(), entry.getValue());
//        }
//
//        // 设置请求体
//        if (!StringUtils.isBlank(entity.getJson())) {
//            StringEntity requestEntity = new StringEntity(entity.getJson(), "UTF-8");
//            requestEntity.setContentEncoding("UTF-8");
//
//            httpPost.setEntity(requestEntity);
//        } else {
//            List<NameValuePair> list = new ArrayList<>();
//            for (Map.Entry<String, String> entry : entity.getBodyParams().entrySet()) {
//                list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
//            }
//
//            if (list.size() > 0) {
//                UrlEncodedFormEntity fromEntity = new UrlEncodedFormEntity(list, ENCODE_UTF8);
//                httpPost.setEntity(fromEntity);
//            }
//        }
//
//        HttpResponse response = httpClient.execute(httpPost);
//
//        if (response != null) {
//            List<Cookie> cookies = cookieStore.getCookies();
//
//            for (Cookie cookie : cookies) {
//                resultMap.put(cookie.getName(), cookie.getValue());
//            }
//
//            resultMap.put("response", response.getEntity());
//        }
//
//        return resultMap;
//    }


    public static Map<String, Object> get(HttpRequestEntity entity) {
        return get(entity, null);
    }

    @SneakyThrows
    public static Map<String, Object> post(HttpRequestEntity entity) {
        String clazz = "org.apache.http.client.methods.HttpPost";
        return request(entity, null, clazz);
    }

    @SneakyThrows
    public static Map<String, Object> get(HttpRequestEntity entity, List<Cookie> cookieList) {
        String clazz = "org.apache.http.client.methods.HttpGet";
        return request(entity, cookieList, clazz);
    }

    @SneakyThrows
    private static Map<String, Object> request(HttpRequestEntity entity, List<Cookie> cookieList, String clazz) {
        //结果集
        Map<String, Object> resultMap = new HashMap<>();

        CookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
        Constructor<?> constructor = Class.forName(clazz).getConstructor(String.class);
        HttpEntityEnclosingRequestBase req = (HttpEntityEnclosingRequestBase) constructor.newInstance(entity.getUrl());

        //设置请求和传输超时时间
        RequestConfig requestConfig = RequestConfig.custom().setSocketTimeout(180000).setConnectTimeout(180000).build();
        req.setConfig(requestConfig);

        // 设置请求头
        for (Map.Entry<String, String> entry : entity.getHeadParams().entrySet()) {
            req.setHeader(entry.getKey(), entry.getValue());
        }

        // 设置请求体
        if (!StringUtils.isBlank(entity.getJson())) {
            StringEntity requestEntity = new StringEntity(entity.getJson(), "UTF-8");
            requestEntity.setContentEncoding("UTF-8");
            req.setEntity(requestEntity);
        } else {
            List<NameValuePair> list = new ArrayList<>();
            for (Map.Entry<String, String> entry : entity.getBodyParams().entrySet()) {
                list.add(new BasicNameValuePair(entry.getKey(), entry.getValue()));
            }
            if (list.size() > 0) {
                UrlEncodedFormEntity fromEntity = new UrlEncodedFormEntity(list, ENCODE_UTF8);
                req.setEntity(fromEntity);
            }
        }

        if (cookieList != null) {
            for (Cookie cookie : cookieList) {
                cookieStore.addCookie(cookie);
            }
        }

        HttpResponse response = httpClient.execute(req);

        if (response != null) {
            List<Cookie> cookies = cookieStore.getCookies();

            for (Cookie cookie : cookies) {
                resultMap.put(cookie.getName(), cookie.getValue());
            }

            resultMap.put("response", response.getEntity());
        }

        return resultMap;
    }

    public static Map<String, Object> get2(HttpRequestEntity entity) {
        CookieStore cookieStore = new BasicCookieStore();
        CloseableHttpClient httpClient = HttpClients.custom().setDefaultCookieStore(cookieStore).build();
        HttpGet httpGet = new HttpGet(entity.getUrl());
        httpGet.setConfig(cfg());
        for (Map.Entry<String, String> entry : entity.getHeadParams().entrySet()) {
            httpGet.addHeader(entry.getKey(), entry.getValue());
        }
        HttpResponse response = doHttpResponse(httpClient, httpGet);
        return resultMap(response, cookieStore);
    }

    public static Map<String, Object> resultMap(HttpResponse response, CookieStore cookieStore) {
        Map<String, Object> resultMap = new HashMap<>();
        if (response != null) {
            List<Cookie> cookies = cookieStore.getCookies();
            for (Cookie cookie : cookies) {
                resultMap.put(cookie.getName(), cookie.getValue());
            }
            resultMap.put(COOKIES_STR, cookies);
            resultMap.put(RES_STR, response.getEntity());
            resultMap.put(STS_CODE, response.getStatusLine().getStatusCode());
            if (response.getLastHeader(LOCATION_STR) != null) {
                resultMap.put(LOCATION_STR, response.getLastHeader(LOCATION_STR).getValue());
            }
        }
        return resultMap;
    }

    @SneakyThrows
    public static HttpResponse doHttpResponse(CloseableHttpClient httpClient, HttpGet httpGet) {
        HttpResponse response;
        try {
            response = httpClient.execute(httpGet);
        } catch (Exception e) {
            log.error(LOG_INFO, e.getLocalizedMessage());
            log.error("===== >> will sleep : {} ms", SLEEP_TIME);
            TimeUnit.SECONDS.sleep(SLEEP_TIME);
            response = doHttpResponse(httpClient, httpGet);
        }
        return response;
    }

    public static RequestConfig cfg() {
        if (cfg == null) {
            cfg = RequestConfig.custom()
                    .setRedirectsEnabled(true)
                    .setSocketTimeout(HTTP_TIME_OUT)
                    .setConnectTimeout(HTTP_TIME_OUT)
                    .setAuthenticationEnabled(true)
                    .setConnectionRequestTimeout(HTTP_TIME_OUT)
                    .build();
        }
        return cfg;
    }

}
