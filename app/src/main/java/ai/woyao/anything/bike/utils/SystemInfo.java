package ai.woyao.anything.bike.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.telephony.TelephonyManager;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Locale;

import ai.woyao.anything.bike.net.encrypt.SDKPswCoder;

/**
 * 设备参数信息
 *
 * @author jen
 */
public class SystemInfo {
    /**
     * mac地址的缓存文件名
     */
    private static final String sMacCacheFileName = "50b8f7b920efe75c986a4a4e8ccee4af";
    private static final String sMacPSW = "B9gfhupt";
    /**
     * mac地址(去除了冒号)
     */
    private static String sMac;

    /**
     * Android ID
     */
    private static String sAndroidId;

    /**
     * 运营商名称 carrierName
     */
    private static String sOperatorName;

    /**
     * imei
     */
    private static String sImei;
    /**
     * imsi
     */
    private static String sImsi;

    /**
     * 初始化AndroidID
     *
     * @param context
     * @return
     */
    public static String initAndroidId(Context context) {
        try {
            String andId = android.provider.Settings.Secure.getString(
                    context.getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID);

            if (andId != null) {
                andId = andId.trim();
                andId = andId.toLowerCase();
                return andId;
            }
        } catch (Throwable ignored) {
        }
        return "";
    }

    /**
     * 获取Android Id
     *
     * @param context
     * @return
     */
    public static String getAndroidId(Context context) {
        try {
            if (sAndroidId == null) {
                sAndroidId = initAndroidId(context);
            }

            if (sAndroidId != null) {
                return sAndroidId;
            }
        } catch (Throwable ignored) {
        }
        return "";
    }

    /**
     * 初始化imei 2012-11-15
     *
     * @param context
     * @return
     */
    public static String initImei(Context context) {
        String imei = null;
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            if (telephonyManager != null) {
                // IMEI
                imei = telephonyManager.getDeviceId();
                if (imei != null) {
                    imei = imei.trim();

                    if (imei.contains(" ")) {
                        imei = imei.replace(" ", "");
                    }

                    if (imei.contains("-")) {
                        imei = imei.replace("-", "");
                    }

                    if (imei.contains("\n")) {
                        imei = imei.replace("\n", "");
                    }

                    String meid = "MEID:";
                    int meidIndex = imei.indexOf(meid);
                    if (meidIndex > -1) {
                        imei = imei.substring(meidIndex + meid.length());
                    }

                    imei = imei.trim();
                    imei = imei.toLowerCase();

                    if (imei.length() < 10) {
                        imei = null;
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        return imei;
    }

    /**
     * 获取imei地址 2012-11-15
     *
     * @param context
     * @return
     */
    public static String getImei(Context context) {
        try {
            if (sImei == null) {
                sImei = initImei(context);
            }

            if (sImei != null) {
                return sImei;
            }
        } catch (Throwable ignored) {
        }
        return "";
    }

    /**
     * 初始化imsi 2012-11-15
     *
     * @param context
     * @return
     */
    public static String initImsi(Context context) {
        String imsi = null;
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            if (telephonyManager != null) {
                // IMSI
                imsi = telephonyManager.getSubscriberId();
                if (imsi != null) {
                    imsi = imsi.trim();
                    if (imsi.length() < 10) {
                        imsi = null;
                    } else {
                        imsi = imsi.toLowerCase();
                    }
                }
            }
        } catch (Throwable ignored) {
        }

        return imsi;
    }

    /**
     * 获取imsi地址 2012-11-15
     *
     * @param context
     * @return
     */
    public static String getImsi(Context context) {
        try {
            if (sImsi == null) {
                sImsi = initImsi(context);
            }

            if (sImsi != null) {
                return sImsi;
            }

        } catch (Throwable e) {
            // handle Throwable
        }
        return "";
    }

    /**
     * 初始化mac地址 2012-11-15
     *
     * @param context
     * @return
     */
    public static String initMac(Context context) {
        try {
            if (hasPermission(context, Manifest.permission.ACCESS_WIFI_STATE)) {
                WifiManager wifi = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo info = wifi.getConnectionInfo();
                String mac = info.getMacAddress();

                if (mac != null) {
                    mac = mac.trim();
                    if (mac.length() > 0) {
                        mac = mac.replace(":", "");// 去掉:号
                        mac = mac.toLowerCase(Locale.ENGLISH);
                        saveMacToCacheFile(context, mac);
                    }
                    return mac;
                }
            }
        } catch (Throwable ignored) {
        }
        return "";
    }

    /**
     * 从缓存文件中获取mac，这取决于上一次写入缓存的mac数据
     *
     * @param context
     * @return
     */
    public static String initMacFromCacheFile(Context context) {
        FileReader fr = null;
        BufferedReader br = null;
        try {
            File file = context.getFileStreamPath(sMacCacheFileName);
            if (file == null || !file.exists()) {
                return "";
            }
            fr = new FileReader(file);
            br = new BufferedReader(fr);
            String macToDecode = br.readLine();
            if (macToDecode == null) {
                return "";
            }

            return SDKPswCoder.decode(macToDecode, sMacPSW);
        } catch (Throwable ignored) {
        } finally {
            try {
                if (br != null) {
                    br.close();
                }
            } catch (Throwable ignored) {
            }
            try {
                if (fr != null) {
                    fr.close();
                }
            } catch (Throwable ignored) {
            }
        }
        return "";
    }

    /**
     * 将收集到的mac地址加密保存到cache文件中，因为极有可能拿到的mac为空
     *
     * @param context
     */
    public static void saveMacToCacheFile(Context context, String mac) {
        FileWriter fw = null;
        BufferedWriter bw = null;
        try {
            if (mac == null) {
                return;
            }
            mac = mac.trim();
            if (mac.length() <= 0) {
                return;
            }

            String encodedMac = SDKPswCoder.encode(mac, sMacPSW);
            File file = context.getFileStreamPath(sMacCacheFileName);
            fw = new FileWriter(file);
            bw = new BufferedWriter(fw);
            bw.write(encodedMac);
            bw.flush();
        } catch (Throwable ignored) {
        } finally {
            try {
                if (fw != null) {
                    fw.close();
                }
            } catch (Throwable ignored) {
            }
            try {
                if (bw != null) {
                    bw.close();
                }
            } catch (Throwable ignored) {
            }
        }
    }

    /**
     * 获取mac地址 2012-11-15
     *
     * @param context
     * @return
     */
    public static String getMac(Context context) {
        try {
            if (sMac == null || sMac.length() <= 0) {
                sMac = initMac(context);
            }

            if (sMac == null || sMac.length() <= 0) {
                //如果拿到的mac为空，才从缓存文件中拿
                sMac = initMacFromCacheFile(context);
            }

            if (sMac != null) {
                return sMac;
            }
        } catch (Throwable ignored) {
        }
        return "";
    }

    /**
     * 获取设备操作系统 PhoneOS 如  2.3    11-5-23
     *
     * @return
     */
    public static String getDeviceOsRelease() {
        return Build.VERSION.RELEASE;
    }

    /**
     * 获取手机型号 DeviceDetail 11-5-23
     *
     * @return
     */
    public static String getDeviceModel() {
        return Build.MODEL;
    }

    /**
     * 初始化 运营商信息 11-5-23
     *
     * @param context
     */
    public static void initOperatorName(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            if (telephonyManager != null) {
                // 运营商的名称 cn
                String str = telephonyManager.getNetworkOperatorName();
                if (str == null) {
                    sOperatorName = "";
                } else {
                    sOperatorName = str;
                }
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * 获取运营商名字
     *
     * @param context
     * @return
     */
    public static String getOperatorName(Context context) {
        if (sOperatorName == null) {
            initOperatorName(context);
        }

        if (sOperatorName == null) {
            return "";
        }

        return sOperatorName;
    }

    /**
     * 获取手机网络类型 2012-11-15
     *
     * @param context
     * @return
     */
    public static int getNetworkType(Context context) {
        try {
            TelephonyManager telephonyManager = (TelephonyManager) context
                    .getSystemService(Context.TELEPHONY_SERVICE);

            if (telephonyManager != null) {
                return telephonyManager.getNetworkType();
            }
        } catch (Throwable ignored) {
        }
        return TelephonyManager.NETWORK_TYPE_UNKNOWN;
    }

    private static boolean hasPermission(Context context, String permission) {
        return ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED;
    }
}
