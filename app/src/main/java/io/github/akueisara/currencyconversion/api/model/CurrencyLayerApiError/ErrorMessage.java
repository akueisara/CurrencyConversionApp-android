package io.github.akueisara.currencyconversion.api.model.CurrencyLayerApiError;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Kuei on 2019-04-23.
 */
public class ErrorMessage {

    @SerializedName("code")
    @Expose
    private Integer code;
    @SerializedName("info")
    @Expose
    private String info;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    @Override
    public String toString() {
        return "ErrorMessage{" + "code=" + code + ", info='" + info + '\'' + '}';
    }
}