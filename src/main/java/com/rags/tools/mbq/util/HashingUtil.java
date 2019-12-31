package com.rags.tools.mbq.util;

import com.rags.tools.mbq.exception.MBQException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HashingUtil {
    private static final String SHA_256 = "SHA-256";

    public static String hashSHA256(String message) {
        if (message == null) {
            throw new MBQException("For id calculation, message can't be null");
        }
        try {
            return bytesToHex(MessageDigest.getInstance(SHA_256).digest(message.getBytes()));
        } catch (NoSuchAlgorithmException e) {
            throw new MBQException("ID calculation failed", e);
        }

    }

    private static String bytesToHex(byte[] digest) {
        StringBuilder builder = new StringBuilder();
        for (byte hash : digest) {
            String hex = Integer.toHexString(0xff & hash);
            if (hex.length() == 1) {
                builder.append('0');
            }
            builder.append(hex);
        }
        return builder.toString();
    }
}
