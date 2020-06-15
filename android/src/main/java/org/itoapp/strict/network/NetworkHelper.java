package org.itoapp.strict.network;

import android.util.Log;

import org.itoapp.psic.Client;
import org.itoapp.psic.GsonSingleton;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetworkHelper {

    public static final String BASE_URL = "https://psi.ito-app.org";
    private static final String LOG_TAG = "NetworkHelper";

    public static int getNumberOfInfectedContacts(Set<byte[]> contacts) throws IOException {
        Client psicClient = new Client();
        OkHttpClient httpClient = new OkHttpClient();
        Request setupRequest = new Request.Builder().url(BASE_URL + "/setup").get().build();

        // required because the variable has to be effective final
        final String[] setupMessage = new String[1];
        final IOException[] setupRequestException = new IOException[1];

        CountDownLatch requestLatch = new CountDownLatch(1);

        // Query the setup message asynchronously
        httpClient.newCall(setupRequest).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                setupRequestException[0] = e;
                requestLatch.countDown();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                setupMessage[0] = response.body().string();
                response.close();
                requestLatch.countDown();
            }
        });

        String requestString = psicClient.createRequest(contacts);

        RequestBody requestBody = RequestBody.create(MediaType.get("application/json"), requestString);
        Request psicRequest = new Request.Builder().url(BASE_URL + "/request").post(requestBody).build();

        Response psicResponse = httpClient.newCall(psicRequest).execute();
        String psicResponseText = psicResponse.body().string();
        psicResponse.close();

        try {
            requestLatch.await(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            Log.e(LOG_TAG, "Got interrupted while waiting for response!", e);
            throw new RuntimeException(e);
        }

        if (setupRequestException[0] != null)
            throw setupRequestException[0];

        return psicClient.calculateIntersectionCardinality(setupMessage[0], psicResponseText);
    }


    public static void submitReports(Set<byte[]> reports) throws IOException {
        OkHttpClient client = new OkHttpClient();

        String reportsJson = GsonSingleton.GSON.toJson(reports);
        RequestBody requestBody = RequestBody.create(MediaType.get("application/json"), reportsJson);

        Request request = new Request.Builder().url(BASE_URL + "/publish").post(requestBody).build();

        Response response = client.newCall(request).execute();

        if(!response.isSuccessful()) {
            throw new IOException("Could not upload the tcn reports. " + response.message());
        }
        response.close();
    }
}
