package ai.woyao.anything.bike.net;

import ai.woyao.anything.bike.net.retrofit.RetrofitSender;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class Api {
    public static NetTask request(byte[] params, NetCallback callback) {
        RequestBody body = RequestBody.create(MediaType.parse("application/octet-stream"), params);
        Subscription task = RetrofitSender.getInstance()
                .post(body)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(callback);
        return new NetTask(task);
    }
}
