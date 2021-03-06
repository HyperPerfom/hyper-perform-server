package me.hyperperform.forecasting.request;

/**
 * A POJO for the request data coming from the caller, for the positions within an integration to be updated in the json string.
 *
 * @author  CodusMaximus
 * @version 1.0
 * @since   2016/09/30
 */

public class UpdateIntegrationRequest
{
    private String data;

    public UpdateIntegrationRequest() {
    }

    public UpdateIntegrationRequest(String data) {
        this.data = data;
    }

    public String getData()
    {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
