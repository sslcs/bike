package ai.woyao.anything.bike.net;

import ai.woyao.anything.bike.net.retrofit.AppCallback;

public abstract class NetCallback extends AppCallback {
    /**
     * 请求成功时回调接口
     *
     * @param response 返回结果
     */
    public abstract void onSuccess(String response);

    /**
     * 请求失败时回调接口
     *
     * @param e 错误信息
     */
    public abstract void onFail(Throwable e);

    /**
     * 结果返回时的预处理
     */
    public void preprocessor() {}
}
