package ai.woyao.anything.bike.net.retrofit;

import android.text.TextUtils;
import android.util.Base64;

import ai.woyao.anything.bike.net.bean.response.ServerResponse;
import rx.Subscriber;

public abstract class AppCallback extends Subscriber<ServerResponse> {
    @Override
    public final void onError(Throwable e) {
        preprocessor();
        onFail(e);
    }

    @Override
    public final void onNext(ServerResponse response) {
        if (response == null || TextUtils.isEmpty(response.data)) {
            onError(new Throwable("response null!!!"));
        } else if (!response.isSuccess()) {
            onError(new Throwable(response.error));
        } else {
            String encoded = response.data;
            byte[] decode = Base64.decode(encoded, Base64.DEFAULT);
            String data = new String(decode);

            preprocessor();
            onSuccess(data);
        }
    }

    @Override
    public final void onCompleted() {}

    public abstract void preprocessor();

    public abstract void onSuccess(String response);

    public abstract void onFail(Throwable e);
}
