package com.example.qkx.speechtotext.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by qkx on 16/7/13.
 */
public class MD5Util {
    public static String getMD5(String str) {
        try {
            MessageDigest md5 = MessageDigest.getInstance("MD5");
            byte[] bytes = md5.digest(str.getBytes());
//            System.out.println(bytes.length);
            StringBuffer temp = new StringBuffer();
            for (byte b : bytes) {
                int bt = b & 0xff;
                if (bt < 16) {
                    temp.append(0);
                }
                temp.append(Integer.toHexString(bt));
            }
//            System.out.println(temp.toString());
            return temp.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }
}
