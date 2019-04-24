package io.github.akueisara.currencyconversion.utils;

import android.content.Context;
import android.content.res.Resources;
import android.util.DisplayMetrics;

/**
 * Created by Kuei on 2019-04-24.
 */
public class Utils {

    public static int calculateNoOfColumns(Context context, int taskSize, int itemWidth) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        float dpWidth = displayMetrics.widthPixels / displayMetrics.density;
        int noOfColumns = (int) (dpWidth / pxToDp(itemWidth));
        int columns = taskSize;
        if(taskSize > noOfColumns) {
            columns = noOfColumns;
        }
        if(columns == 0) {
            columns = 1;
        }
        return columns;
    }

    public static int dpToPx(int dp) {
        return (int) (dp * Resources.getSystem().getDisplayMetrics().density);
    }

    public static int pxToDp(int px) {
        return (int) (px / Resources.getSystem().getDisplayMetrics().density);
    }
}
