package ai.woyao.anything.bike.net.encrypt;

import java.lang.reflect.Type;

import ai.woyao.anything.bike.net.bean.response.DataResponse;
import ai.woyao.anything.bike.utils.DebugLog;
import ai.woyao.anything.bike.utils.GsonSingleton;

/**
 * Created by LiuCongshan on 17-6-29.
 * 解析服务器返回结果
 */

public class ResponseDecoder {
    private final static int DATA_LENGTH = 8;

    public static <T> DataResponse<T> getData(String response, Type type) {
        int index = response.indexOf("{");
        if (index == -1) return null;

        response = response.substring(index);
        DebugLog.e("response : " + response);
        return GsonSingleton.get().fromJson(response, type);
    }

    public static <T> DataResponse<T> getData(String response, Type type, String uuid) {
        int uuidStart = response.indexOf(uuid);
        if (uuidStart == -1) return null;

        try {
            int dataStart = uuidStart + uuid.length() + DATA_LENGTH;
            String strDataLength = response.substring(dataStart - DATA_LENGTH, dataStart);
            int dataLength = Integer.parseInt(strDataLength, 16);
            response = response.substring(dataStart, dataStart + dataLength);
            DebugLog.e("response : " + response);
            return GsonSingleton.get().fromJson(response, type);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Throwable error(DataResponse response) {
        if (response != null) {
            return new Throwable(response.error);
        } else {
            return new Throwable("DataResponse NULL!!!");
        }
    }
}
