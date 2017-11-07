package com.arcao.geocaching.api.downloader;

import com.arcao.geocaching.api.exception.InvalidResponseException;
import com.arcao.geocaching.api.exception.NetworkException;
import com.arcao.geocaching.api.parser.JsonReader;
import com.arcao.geocaching4locus.BuildConfig;

import java.io.Reader;
import java.net.URL;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import timber.log.Timber;

public class OkHttpClientJsonDownloader implements JsonDownloader {
    private static final MediaType MEDIA_TYPE_JSON = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client;

    public OkHttpClientJsonDownloader(OkHttpClient client) {
        this.client = client;
    }

    @Override
    public JsonReader get(URL url) throws NetworkException, InvalidResponseException {
        Reader reader;

        try {
            Request request = new Request.Builder()
                    .url(url)
                    .addHeader("User-Agent", "Geocaching4Locus/" + BuildConfig.VERSION_NAME)
                    .addHeader("Accept-Language", "en-US")
                    .addHeader("Accept-Encoding", "gzip, deflate")
                    .addHeader("Accept", "application/json")
                    .build();

            Response response = client.newCall(request).execute();
            ResponseBody body = response.body();

            if (!response.isSuccessful()) {
                // read error response
                throw new InvalidResponseException(response.code() + " " + response.message() + "\n\n" + body.string());
            }

            reader = body.charStream();
            return new JsonReader(reader);
        } catch (InvalidResponseException e) {
            Timber.e(e, e.getMessage());
            throw e;
        } catch (Throwable e) {
            Timber.e(e, e.getMessage());
            throw new NetworkException("Error while downloading data (" + e.getClass().getSimpleName() + ")", e);
        }
    }

    @Override
    public JsonReader post(URL url, byte[] postData) throws NetworkException, InvalidResponseException {
        Reader reader;

        try {
            Request request = new Request.Builder()
                    .url(url)
                    .post(RequestBody.create(MEDIA_TYPE_JSON, postData))
                    .addHeader("User-Agent", "Geocaching4Locus/" + BuildConfig.VERSION_NAME)
                    .addHeader("Accept", "application/json")
                    .addHeader("Accept-Language", "en-US")
                    .addHeader("Accept-Encoding", "gzip, deflate")
                    .build();

            Response response = client.newCall(request).execute();
            ResponseBody body = response.body();

            if (!response.isSuccessful()) {
                // read error response
                throw new InvalidResponseException(response.code() + " " + response.message() + "\n\n" + body.string());
            }

            reader = body.charStream();
            return new JsonReader(reader);
        } catch (InvalidResponseException e) {
            Timber.e(e, e.getMessage());
            throw e;
        } catch (Throwable e) {
            Timber.e(e, e.getMessage());
            throw new NetworkException("Error while downloading data (" + e.getClass().getSimpleName() + "): " + e.getMessage(), e);
        }
    }
}
