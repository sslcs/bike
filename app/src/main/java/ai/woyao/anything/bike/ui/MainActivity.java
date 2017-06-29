package ai.woyao.anything.bike.ui;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.google.gson.reflect.TypeToken;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.lang.reflect.Type;

import ai.woyao.anything.bike.R;
import ai.woyao.anything.bike.constants.Action;
import ai.woyao.anything.bike.net.Api;
import ai.woyao.anything.bike.net.NetCallback;
import ai.woyao.anything.bike.net.NetParams;
import ai.woyao.anything.bike.net.bean.params.ParamsQr;
import ai.woyao.anything.bike.net.bean.response.DataPassword;
import ai.woyao.anything.bike.net.bean.response.DataResponse;
import ai.woyao.anything.bike.net.encrypt.ResponseDecoder;
import ai.woyao.anything.bike.utils.DebugLog;

public class MainActivity extends TaskActivity {
    private final int REQUEST_CODE_PERMISSION_LOCATION = 100;
    private final int REQUEST_CODE_PERMISSION_CAMERA = 101;
    private final int REQUEST_CODE_GPS_SETTING = 103;

    private AMapLocationClient mLocationClient;
    private AMapLocation mLocation;
    private String bikeId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        listenLocation();
    }


    private void listenLocation() {
        if (!hasPermission(Manifest.permission.ACCESS_FINE_LOCATION) && !hasPermission(Manifest.permission.ACCESS_COARSE_LOCATION)) {
            String[] permissions = new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION};
            ActivityCompat.requestPermissions(this, permissions, REQUEST_CODE_PERMISSION_LOCATION);
            return;
        }

        isGpsOpened();

        mLocationClient = new AMapLocationClient(this);
        //设置定位监听
        mLocationClient.setLocationListener(new AMapLocationListener() {
            @Override
            public void onLocationChanged(AMapLocation location) {
                if (location == null) return;
                if (location.getErrorCode() == 0) {
                    //定位成功回调信息，设置相关消息
                    DebugLog.e("getLatitude : " + location.getLatitude() + " getLongitude : " + location.getLongitude());
                    mLocation = location;
                    if (bikeId != null) {
                        unlockBike();
                    }
                } else {
                    //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                    DebugLog.e("location Error, ErrCode:"
                            + location.getErrorCode() + ", errInfo:"
                            + location.getErrorInfo());
                }
            }
        });

        //声明mLocationOption对象
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        //设置定位模式为高精度模式，Battery_Saving为低功耗模式，Device_Sensors是仅设备模式
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        //设置定位间隔,单位毫秒,默认为2000ms
        mLocationOption.setInterval(20000);
        //设置定位参数
        mLocationClient.setLocationOption(mLocationOption);
        // 此方法为每隔固定时间会发起一次定位请求，为了减少电量消耗或网络流量消耗，
        // 注意设置合适的定位时间的间隔（最小间隔支持为1000ms），并且在合适时间调用stopLocation()方法来取消定位请求
        // 在定位结束后，在合适的生命周期调用onDestroy()方法
        // 在单次定位情况下，定位无论成功与否，都无需调用stopLocation()方法移除请求，定位sdk内部会移除
        //启动定位
        mLocationClient.startLocation();
    }

    private boolean isGpsOpened() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(getString(R.string.tip_open_gps));
            builder.setCancelable(false);
            builder.setPositiveButton(R.string.ok,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface arg0, int arg1) {
                            // 转到手机设置界面，用户设置GPS
                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            startActivityForResult(intent, REQUEST_CODE_GPS_SETTING);
                        }
                    });
            builder.show();
            return false;
        }
        return true;
    }

    public void onClickNew(View view) {
        if (!hasPermission(Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, REQUEST_CODE_PERMISSION_CAMERA);
            return;
        }

        IntentIntegrator integrator = new IntentIntegrator(MainActivity.this);
        integrator.initiateScan();
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_CODE_GPS_SETTING == requestCode) {
            listenLocation();
            return;
        }

        IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (scanResult != null) {
            String result = scanResult.getContents();
            if (TextUtils.isEmpty(result)) return;

            DebugLog.e("result : " + result);
            bikeId = result;
            unlockBike();
        }
    }

    private void unlockBike() {
        if (mLocation == null) {
            if (isGpsOpened()) {
                Toast.makeText(this, R.string.tip_locating, Toast.LENGTH_LONG).show();
            }
            return;
        }

        String action = Action.ACTION_UNLOCK_BIKE;
        ParamsQr paramsQr = new ParamsQr();
        paramsQr.bike_id = bikeId;
        paramsQr.lat = mLocation.getLatitude();
        paramsQr.lng = mLocation.getLongitude();

        NetParams.Builder builder = new NetParams.Builder();
        builder.add(action, action, paramsQr);
        byte[] params = null;
        try {
            params = builder.build();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }

        NetCallback callback = new NetCallback() {
            @Override
            public void onSuccess(String response) {
                DebugLog.e("onSuccess");
                bikeId = null;

                Type type = new TypeToken<DataResponse<DataPassword>>() {}.getType();
                DataResponse<DataPassword> dataResponse = ResponseDecoder.getData(response, type);
                if (dataResponse != null && dataResponse.isSuccess()) {
                    showPassword(dataResponse.data.password);
                } else {
                    onFail(ResponseDecoder.error(dataResponse));
                }
            }

            @Override
            public void onFail(Throwable e) {
                bikeId = null;
                DebugLog.e(e);
                Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
            }
        };
        addTask(Api.request(params, callback));
    }

    private void showPassword(String password) {
        startActivity(PasswordActivity.getIntent(this, password));
    }

    private boolean hasPermission(String permission) {
        return ActivityCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION_CAMERA) {
            onClickNew(null);
        } else if (requestCode == REQUEST_CODE_PERMISSION_LOCATION) {
            listenLocation();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mLocationClient.startLocation();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mLocationClient.stopLocation();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLocationClient.onDestroy();
    }
}
