package org.itoapp.strict.network;

import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class NetworkHelper {

    private static final String LOG_TAG = "InfectedUUIDRepository";
    private static final String BASE_URL = "https://tcn.ito-app.org/tcnreport";

    private static final int SIGNATURELENGTH = 64;
    private static final int BASELENGTH = 70;

    public static List<byte[]> refreshInfectedUUIDs() {
        List<byte[]> reports = new LinkedList<>();
        HttpURLConnection urlConnection = null;
        try {
            //TODO use a more sophisticated library
            URL url = new URL(BASE_URL);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.addRequestProperty("Accept", "application/octet-stream");
            InputStream in = urlConnection.getInputStream();
            byte[] base = new byte[BASELENGTH];
            byte[] memo;
            int readBytes;
            while ((readBytes = in.read(base, 0, BASELENGTH)) == BASELENGTH) {
                int memolength = (int) base[BASELENGTH - 1] & 0xFF;
                memo = new byte[memolength];
                if (in.read(memo, 0, memolength) < memolength) {
                    throw new RuntimeException("Parsing from Server failed");
                }
                byte[] signature = new byte[SIGNATURELENGTH];
                if (in.read(signature, 0, SIGNATURELENGTH) < SIGNATURELENGTH) {
                    throw new RuntimeException("Parsing from Server failed");
                }
                // use PushbackInputstream and get rid of BB?
                ByteBuffer report = ByteBuffer.allocate(BASELENGTH + memolength + SIGNATURELENGTH);
                report.put(base);
                report.put(memo);
                report.put(signature);
                reports.add(report.array());
            }
            if (readBytes > 0)
                throw new RuntimeException("Parsing from Server failed");
        } catch (MalformedURLException e) {
            Log.wtf(LOG_TAG, "Malformed URL?!", e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
        return reports;
    }


    public static void publishReport(byte[] report) throws IOException {
        HttpURLConnection urlConnection = null;
        try {
            URL url = new URL(BASE_URL);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoOutput(true);
            urlConnection.addRequestProperty("Content-Type", "application/octet-stream");
            OutputStream outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
            outputStream.write(report);
            outputStream.close();

            InputStream inputStream = urlConnection.getInputStream();
            inputStream.read();
            inputStream.close();
        } catch (MalformedURLException e) {
            Log.wtf(LOG_TAG, "Malformed URL?!", e);
            throw new RuntimeException(e);
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
    }
}
