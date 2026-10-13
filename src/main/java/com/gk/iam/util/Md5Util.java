package com.gk.iam.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Md5Util {

    private static final char[] HEX_CHARS = "0123456789abcdef".toCharArray();

    /**
     * 计算字符串的MD5 32位小写十六进制
     */
    public static String encode(String rawText) {
        if (rawText == null) {
            return null;
        }
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] bytes = md.digest(rawText.getBytes(StandardCharsets.UTF_8));
            char[] chars = new char[bytes.length * 2];
            for (int i = 0; i < bytes.length; i++) {
                int b = bytes[i] & 0xFF;
                chars[i * 2] = HEX_CHARS[b >>> 4];
                chars[i * 2 + 1] = HEX_CHARS[b & 0x0F];
            }
            return new String(chars);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5算法不可用", e);
        }
    }

    /**
     * 校验明文与密文是否匹配
     */
    public static boolean matches(String rawPassword, String encodedPassword) {
        if (rawPassword == null || encodedPassword == null) {
            return false;
        }
        return encode(rawPassword).equalsIgnoreCase(encodedPassword);
    }
}

