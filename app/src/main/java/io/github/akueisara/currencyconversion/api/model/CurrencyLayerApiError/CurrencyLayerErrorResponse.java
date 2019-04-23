package io.github.akueisara.currencyconversion.api.model.CurrencyLayerApiError;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

/**
 * Created by Kuei on 2019-04-23.
 */
public class CurrencyLayerErrorResponse {

    @SerializedName("success")
    @Expose
    private Boolean success;
    @SerializedName("error")
    @Expose
    private ErrorMessage error;

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public ErrorMessage getErrorMessage() {
        return error;
    }

    public void setErrorMessage(ErrorMessage error) {
        this.error = error;
    }
}
