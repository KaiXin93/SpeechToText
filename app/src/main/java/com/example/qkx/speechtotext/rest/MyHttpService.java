package com.example.qkx.speechtotext.rest;

import com.example.qkx.speechtotext.Constants;
import com.example.qkx.speechtotext.model.ResultBean;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by qkx on 16/4/8.
 */
public interface MyHttpService {
    @GET(Constants.baseUrl)
    Call<ResultBean> query(@Query("q") String q, @Query("from") String from, @Query("to") String to,
                       @Query("appid") String appid, @Query("salt") int salt, @Query("sign") String sign);
}
