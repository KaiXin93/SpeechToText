package com.example.qkx.speechtotext.rest;

import com.example.qkx.speechtotext.BusManager;
import com.example.qkx.speechtotext.Constants;
import com.example.qkx.speechtotext.model.ResultBean;
import com.example.qkx.speechtotext.utils.MD5Util;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by qkx on 16/4/8.
 */
public class RestSource {

    private static volatile RestSource mInstance;
    private final MyHttpService mAPI;

    private RestSource() {
        /*OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new Interceptor() {
            @Override
            public okhttp3.Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                //日志
                Request request = original.newBuilder()
                        .method(original.method(), original.body())
                        .build();
                okhttp3.Response reponse = chain.proceed(request);
                return reponse;
            }
        }).build();*/
        //默认client不打印日志
        //添加拦截器打印
        OkHttpClient client = new OkHttpClient.Builder().addInterceptor(new HttpLoggingInterceptor()
                .setLevel(HttpLoggingInterceptor.Level.BODY))
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.baseUrl)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build();
//        retrofit.
        mAPI = retrofit.create(MyHttpService.class);
    }

    public static RestSource getInstance() {
        if (mInstance == null) {
            synchronized (RestSource.class) {
                if (mInstance == null) {
                    mInstance = new RestSource();
                }
            }
        }
        return mInstance;
    }

    public void query(String q) {
        String salt = "8888";
        String str = Constants.appId + q + salt + Constants.secret;
        String sign = MD5Util.getMD5(str);
        query(q, "zh", "en", Constants.appId, Integer.valueOf(salt), sign);
    }

    public void query(String q, String from, String to, String appid, int salt, String sign) {
        Call<ResultBean> call = mAPI.query(q, from, to, appid, salt, sign);

        call.enqueue(new Callback<ResultBean>() {
            @Override
            public void onResponse(Call<ResultBean> call, Response<ResultBean> response) {
                ResultBean resultBean = response.body();
                if (null != resultBean) {
                    BusManager.getUiBus().post(resultBean);
                }
            }

            @Override
            public void onFailure(Call<ResultBean> call, Throwable t) {
                System.out.println("Failed!");
                t.printStackTrace();
            }

//            @Override
//            public void onResponse(Call<WelfareBean> call, Response<WelfareBean> response) {
//                WelfareBean bean = response.body();
//                if (bean != null) {
//                    BusManager.getUiBus().post(bean);
//                }
//            }
//
//            @Override
//            public void onFailure(Call<WelfareBean> call, Throwable t) {
//
//            }
        });
    }


}
