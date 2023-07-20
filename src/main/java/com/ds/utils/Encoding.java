package com.ds.utils;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Encoding {
    // encode the plaintext password
    public static String encodePassword(String pwd) throws NoSuchAlgorithmException, UnsupportedEncodingException {
        byte[] digest = null;
        MessageDigest md5 = MessageDigest.getInstance("md5");
        digest = md5.digest(pwd.getBytes("utf-8"));

        String md5Str = new BigInteger(1, digest).toString(16);
        return md5Str;
    }
}
