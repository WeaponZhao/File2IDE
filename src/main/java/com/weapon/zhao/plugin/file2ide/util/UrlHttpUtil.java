package com.weapon.zhao.plugin.file2ide.util;

import com.intellij.openapi.diagnostic.Logger;

import java.io.*;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;

/**
 * @author WeaponZhao
 * @since 2021/3/8 16:56
 */
public class UrlHttpUtil {

    private static final Logger LOG = Logger.getInstance(UrlHttpUtil.class);

    private UrlHttpUtil() {}

    public static HttpURLConnection getConnection(String httpUrl) throws IOException {
        URL url = new URL(httpUrl);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("Content-Type", "application/octet-stream");
        connection.setDoOutput(true);
        connection.setDoInput(true);
        connection.setRequestProperty("Connection", "Keep-Alive");
        connection.connect();
        return connection;

    }

    public static void downloadRes(String httpUrl, String dest) {
        HttpURLConnection connection;
        try {
            connection = getConnection(httpUrl);
        } catch (IOException e) {
            LOG.warn(e.getMessage(), e);
            return;
        }
        try (InputStream is = connection.getInputStream();
             BufferedInputStream bis = new BufferedInputStream(is);
             FileOutputStream fos = new FileOutputStream(dest);
             BufferedOutputStream bos = new BufferedOutputStream(fos))
        {
            int contentLength = connection.getContentLength();
            if (contentLength > 32) {
                int b;
                int has = 0;
                DecimalFormat df = (DecimalFormat) NumberFormat.getInstance();
                df.setMaximumFractionDigits(2);
                df.setRoundingMode(RoundingMode.HALF_UP);
                byte[] byArr = new byte[1024];
                while ((b = bis.read(byArr)) != -1) {
                    bos.write(byArr, 0, b);
                    has += b;
                    LOG.info(df.format(has / contentLength * 100L) + "%");
                }
            }
        } catch (IOException e) {
            LOG.warn(e.getMessage(), e);
        }
    }
}
