package com.thm.app_server.utils;

import java.util.Base64;

public class ImageUtils {
    public static byte[] convertBase64StringToByteArray(String src) {
        return Base64.getDecoder().decode(src.split(",")[1]);
    }
}
