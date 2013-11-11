package se.inera.axel.shs.camel.component;

import org.apache.commons.httpclient.HttpClient;
import se.inera.axel.shs.exception.ShsException;
import se.inera.axel.shs.mime.ShsMessage;

import java.util.Map;

public class ShsClient {

    String rsUrl = null;
    String dsUrl = null;

    HttpClient httpClient;

    public ShsClient() {
        this.httpClient = new HttpClient();
    }


    public SendResponse send(ShsMessage shsMessage) throws ShsException {

    }


    public String getRsUrl() {
        return rsUrl;
    }

    public void setRsUrl(String rsUrl) {
        this.rsUrl = rsUrl;
    }

    public String getDsUrl() {
        return dsUrl;
    }

    public void setDsUrl(String dsUrl) {
        this.dsUrl = dsUrl;
    }


    public HttpClient getHttpClient() {
        return httpClient;
    }

    public void setHttpClient(HttpClient httpClient) {
        this.httpClient = httpClient;
    }

    class SendResponse {
        Map<String, String> headers;
        Object body;

        public SendResponse withHeaders(Map<String, String> headers) {
            this.headers = headers;
            return this;
        }

        public SendResponse withBody(Object body) {
            this.body = body;
            return this;
        }
    }
}
