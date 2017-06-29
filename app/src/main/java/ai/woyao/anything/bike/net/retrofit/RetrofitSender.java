package ai.woyao.anything.bike.net.retrofit;

import java.util.concurrent.TimeUnit;

import ai.woyao.anything.bike.BuildConfig;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitSender {
    private AppServer server;

    //构造方法私有
    private RetrofitSender() {
        //手动创建一个OkHttpClient并设置超时时间
        OkHttpClient.Builder builder = new OkHttpClient.Builder();
        builder.connectTimeout(30, TimeUnit.SECONDS);
        builder.readTimeout(31, TimeUnit.SECONDS);

        if (BuildConfig.DEBUG) {
            // Log信息拦截器
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            //设置 Debug Log 模式
            builder.addInterceptor(loggingInterceptor);
        }

        Retrofit retrofit = new Retrofit.Builder()
                .client(builder.build())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .baseUrl("http://api.woyao.ai/")
                .build();
        server = retrofit.create(AppServer.class);
    }

    //获取单例
    public static AppServer getInstance() {
        return SingletonHolder.INSTANCE.server;
    }

    private static class SingletonHolder {
        private static final RetrofitSender INSTANCE = new RetrofitSender();
    }
}
