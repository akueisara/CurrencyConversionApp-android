package io.github.akueisara.currencyconversion.utils;

import android.content.Context;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;

import okhttp3.ResponseBody;
import retrofit2.HttpException;
import retrofit2.Response;
import timber.log.Timber;

/**
 * Created by Kuei on 2019-04-24.
 */
public class ErrorUtils {

    public static void parseError(Context context, Throwable e) {
        if (e instanceof HttpException) {
            Response<?> response = ((HttpException)e).response();
            Timber.d(e, "HttpException: Code - %s, Message - %s",response.raw().code(), response.raw().message());
        } else if (e instanceof SocketTimeoutException) {
            Timber.d(e, "SocketTimeoutException: %s", e.getLocalizedMessage());
        } else if (e instanceof IOException) {
            Timber.d(e, "IOException: %s", e.getLocalizedMessage());
        } else {
            Timber.d(e, "UnknownException: %s", e.getLocalizedMessage());
        }
    }

    private static String getErrorMessage(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            return jsonObject.getString("message");
        } catch (Exception e) {
            return e.getMessage();
        }
    }
}
