package ai.woyao.anything.bike.net.encrypt;

import java.io.ByteArrayOutputStream;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SDKPswCoder {
    private static final String LowerCaseHex = "0123456789abcdef";

    private static byte hexCharToByte(char c) {
        try {
            c = Character.toLowerCase(c);
        } catch (Throwable e) {
            // handle exception
        }
        return (byte) LowerCaseHex.indexOf(c);
    }

    /**
     * 加密
     *
     * @param toEncode 原文
     * @param key      密钥
     * @return string
     */
    public static String encode(String toEncode, String key) {
        if (toEncode == null) {
            return "";
        }

        try {
            toEncode = toEncode.trim();
            if (toEncode.length() == 0) {
                return "";
            }
        } catch (Throwable e) {
            // handle exception
        }

        if (key == null) {
            return "";
        }

        if (key.length() == 0) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        try {
            key = md5(key);
            key = md5(key.substring(12)) + md5(key.substring(0, 20));
            int index = 0;

            byte[] key_buffer = key.getBytes("UTF-8");
            byte[] buffer = toEncode.getBytes("UTF-8");
            int key_len = key_buffer.length;
            int len = buffer.length;

            for (int i = 0; i < len; i++) {
                int a = buffer[i];
                int b = key_buffer[index];

                int c = ((a ^ b) & 0xff);

                String temp = Integer.toHexString(c);

                if (temp == null) {
                    sb.append("00");
                } else {
                    temp = temp.trim();

                    if (temp.length() == 1) {
                        sb.append("0");
                        sb.append(temp);
                    } else {
                        if (temp.length() == 0) {
                            sb.append("00");
                        } else {
                            sb.append(temp);
                        }
                    }
                }

                index = (++index) % key_len;
            }

        } catch (Throwable e) {
            // handle Throwable
        }

        return sb.toString();
    }

    public static String decode(String toDecode, String key) {
        if (toDecode == null) {
            return null;
        }

        try {
            toDecode = toDecode.trim();
            if (toDecode.length() == 0) {
                return null;
            }
        } catch (Throwable e) {
            // handle exception
        }

        if (key == null) {
            return null;
        }

        if (key.length() == 0) {
            return null;
        }

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        try {
            key = md5(key);
            key = md5(key.substring(12)) + md5(key.substring(0, 20));

            int index = 0;

            byte[] key_buffer = key.getBytes("UTF-8");

            int key_len = key_buffer.length;
            int len = toDecode.length();

            for (int i = 0; i < len; i += 2) {
                char c0 = toDecode.charAt(i);
                char c1 = toDecode.charAt(i + 1);

                byte b0 = hexCharToByte(c0);
                byte b1 = hexCharToByte(c1);

                byte b = (byte) ((b0 << 4) | b1);
                b = (byte) (b ^ key_buffer[index]);

                stream.write(b);

                index = (++index) % key_len;
            }

            return new String(stream.toByteArray(), "UTF-8");

        } catch (Throwable e) {
        }
        return null;
    }

    private static String md5(String content) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] buff = content.getBytes();
            md5.update(buff, 0, buff.length);
            String result = String.format("%032x", new BigInteger(1, md5.digest()));
            return result;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return "";
    }

}
