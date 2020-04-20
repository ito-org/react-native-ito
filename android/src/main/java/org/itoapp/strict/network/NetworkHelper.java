package org.itoapp.strict.network;

import android.util.Log;

import org.itoapp.strict.database.ItoDBHelper;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import static org.itoapp.strict.Constants.UUID_LENGTH;
import static org.itoapp.strict.Helper.encodeHexString;

public class NetworkHelper {

    private static final String LOG_TAG = "InfectedUUIDRepository";
    private static final String BASE_URL = "https://api.ito-app.org/tcnreport";

    public static void refreshInfectedUUIDs(ItoDBHelper dbHelper) {
        byte[] lastInfectedUUID = dbHelper.selectRandomLastUUID();
        HttpURLConnection urlConnection = null;
        try {
            //TODO use a more sophisticated library
            String appendix = lastInfectedUUID == null? "": "?uuid=" + encodeHexString(lastInfectedUUID);
            URL url = new URL(BASE_URL);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.addRequestProperty("Accept", "application/octet-stream");
            InputStream inputStream = new BufferedInputStream(urlConnection.getInputStream());
            byte[] uuidBytes = new byte[UUID_LENGTH];
            while (inputStream.read(uuidBytes) == uuidBytes.length) {
                dbHelper.insertInfected(uuidBytes);
            }
            dbHelper.updateLatestFetchTime();
        } catch (MalformedURLException e) {
            Log.wtf(LOG_TAG, "Malformed URL?!", e);
            throw new RuntimeException(e);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null)
                urlConnection.disconnect();
        }
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
