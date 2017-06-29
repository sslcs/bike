package ai.woyao.anything.bike.net;


import android.net.Uri;
import android.text.TextUtils;
import android.util.Base64;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import ai.woyao.anything.bike.AppInstance;
import ai.woyao.anything.bike.constants.NativeParams;
import ai.woyao.anything.bike.net.encrypt.Aes;
import ai.woyao.anything.bike.utils.DebugLog;
import ai.woyao.anything.bike.utils.GsonSingleton;

/**
 * Created by LiuCongshan on 17-6-22.
 * 网络请求参数
 */

public class NetParams {
    private final static String UTF8 = "UTF-8";
    private final static byte[] PSW = "mwxNw1VIvx7Uz4IT".getBytes();

    /**
     * 加密参数，并将向量置于密文前头。
     *
     * @param params 参数
     * @return 加密后数组
     * @throws Throwable 异常
     */
    private static byte[] encrypt(Map<String, String> params) throws Throwable {
        if (params == null) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (sb.length() > 0) {
                sb.append('&');
            }
            sb.append(key).append('=').append(Uri.encode(value));
        }

        byte[] buff = sb.toString().getBytes(UTF8);
        byte[] iv = new byte[16];
        Random rd = new Random();
        rd.nextBytes(iv);
        byte[] data = Aes.encrypt(buff, PSW, iv);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        stream.write(iv, 0, iv.length);
        stream.write(data, 0, data.length);

        byte[] out = stream.toByteArray();
        stream.close();

        return Base64.encodeToString(out, Base64.DEFAULT).getBytes(UTF8);
    }

    /**
     * 获得默认公共参数
     *
     * @return 公共参数
     */
    private static HashMap<String, String> getDefaultParams() {
        HashMap<String, String> params = new HashMap<>();
        params.put(NativeParams.PARAMS_TIME, String.valueOf(System.currentTimeMillis() / 1000));
        params.put(NativeParams.PARAMS_APP_VERSION, AppInstance.getInstance().getAppVersion());
        params.put(NativeParams.PARAMS_APP_VERSION_NUM, AppInstance.getInstance().getAppVersionNum());
        params.put(NativeParams.PARAMS_IMEI, AppInstance.getInstance().getImei());
        params.put(NativeParams.PARAMS_IMSI, AppInstance.getInstance().getImsi());
        params.put(NativeParams.PARAMS_MAC, AppInstance.getInstance().getMac());
        params.put(NativeParams.PARAMS_ANDROID_ID, AppInstance.getInstance().getAndroidId());
        params.put(NativeParams.PARAMS_PLATFORM, AppInstance.getInstance().getPlatform());
        params.put(NativeParams.PARAMS_SYS_VER, AppInstance.getInstance().getSystemVersion());
        params.put(NativeParams.PARAMS_MODEL, AppInstance.getInstance().getModel());
        params.put(NativeParams.PARAMS_NET_INFO, AppInstance.getInstance().getNetInfo());
        params.put(NativeParams.PARAMS_NET_TYPE, AppInstance.getInstance().getNetType());
        params.put(NativeParams.PARAMS_CHANNEL, AppInstance.getInstance().getChannel());
        return params;
    }

    public static class Builder {
        private ArrayList<ActionObject> actions;

        public Builder() {
            actions = new ArrayList<>();
        }

        public <T> Builder add(String uuid, String action, T params) {
            actions.add(new ActionObject<>(uuid, action, params));
            return this;
        }

        public byte[] build() {
            String json = GsonSingleton.get().toJson(actions);
            DebugLog.e("params : " + json);
            if (TextUtils.isEmpty(json)) return null;

            try {
                byte[] out = json.getBytes(UTF8);
                String actionParams = Base64.encodeToString(out, Base64.DEFAULT);

                HashMap<String, String> map = getDefaultParams();
                map.put(NativeParams.PARAMS_ACTIONS, actionParams);

                return encrypt(map);
            } catch (Throwable e) {
                DebugLog.e(e);
            }
            return null;
        }

        class ActionObject<T> {
            String uuid;

            String action;

            T params;

            ActionObject(String uuid, String action, T params) {
                this.uuid = uuid;
                this.action = action;
                this.params = params;
            }
        }
    }
}
