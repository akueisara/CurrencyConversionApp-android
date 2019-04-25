package io.github.akueisara.currencyconversion.utils;

import android.content.Context;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import org.json.JSONObject;

import java.io.IOException;
import java.net.SocketTimeoutException;

import retrofit2.HttpException;
import retrofit2.Response;

/**
 * Created by Kuei on 2019-04-24.
 */
public class ErrorUtils {

    public static void parseError(Context context, Throwable e) {
        if (e instanceof HttpException) {
            Response<?> response = ((HttpException)e).response();
            Logger.e(e, "HttpException: Code - %s, Message - %s",response.raw().code(), response.raw().message());
        } else if (e instanceof SocketTimeoutException) {
            Logger.e(e, "SocketTimeoutException: %s", e.getLocalizedMessage());
        } else if (e instanceof IOException) {
            Logger.e(e, "IOException: %s", e.getLocalizedMessage());
            Toast.makeText(context, "Network connection is not stable. Please try it again later.", Toast.LENGTH_LONG).show();
        } else {
            Logger.e(e, "UnknownException: %s", e.getLocalizedMessage());
        }
    }
}
