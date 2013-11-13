package se.inera.axel.shs.client;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.util.HttpURLConnection;
import se.inera.axel.shs.exception.MissingDeliveryExecutionException;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.processor.ShsMessageMarshaller;

import java.io.IOException;
import java.io.InputStream;

public class ShsClient {

    String rsUrl = null;
    String dsUrl = null;

    HttpClient httpClient;

    public ShsClient() {
        this.httpClient = new HttpClient();
    }


    public String send(ShsMessage shsMessage) throws IOException {

        HttpClient httpClient = getHttpClient();

        PostMethod postMethod = new PostMethod(getRsUrl());

        postMethod.setRequestEntity(new ShsMessageRequestEntity(shsMessage));

        try {
            int statusCode = httpClient.executeMethod(postMethod);
            switch (statusCode) {
                case HttpURLConnection.HTTP_ACCEPTED:
                case HttpURLConnection.HTTP_OK:

                    Header header = postMethod.getResponseHeader(ShsHeaders.X_SHS_TXID);
                    String txId;

                    if (header == null) {
                        return null;
                    } else {
                        txId = header.getValue();
                    }

                    return txId;

                default:

                    String message = String.format("HTTP status code %d (%s): %s ",
                            postMethod.getStatusCode(), postMethod.getStatusText(),
                            postMethod.getResponseBodyAsString());

                    MissingDeliveryExecutionException e = new MissingDeliveryExecutionException(message);
                    e.setContentId(shsMessage.getLabel().getContent().getContentId());
                    e.setCorrId(shsMessage.getLabel().getCorrId());

                    throw e;
            }
        } finally {
            postMethod.releaseConnection();
        }
    }

    public ShsMessage request(ShsMessage shsMessage) throws IOException {

        HttpClient httpClient = getHttpClient();

        PostMethod postMethod = new PostMethod(getRsUrl());

        postMethod.setRequestEntity(new ShsMessageRequestEntity(shsMessage));

        try {
            int statusCode = httpClient.executeMethod(postMethod);
            switch (statusCode) {
                case HttpURLConnection.HTTP_ACCEPTED:
                case HttpURLConnection.HTTP_OK:

                    InputStream responseBody = postMethod.getResponseBodyAsStream();
                    final ShsMessageMarshaller marshaller = new ShsMessageMarshaller();

                    ShsMessage shsReply = marshaller.unmarshal(responseBody);

                    return shsReply;

                case HttpURLConnection.HTTP_NO_CONTENT:
                    postMethod.getResponseBodyAsString();
                    return null;

                default:

                    String message = String.format("HTTP status code %d (%s): %s",
                            postMethod.getStatusCode(), postMethod.getStatusText(),
                            postMethod.getResponseBodyAsString());
                    MissingDeliveryExecutionException e = new MissingDeliveryExecutionException(message);
                    e.setContentId(shsMessage.getLabel().getContent().getContentId());
                    e.setCorrId(shsMessage.getLabel().getCorrId());

                    throw e;
            }
        } finally {
            postMethod.releaseConnection();
        }
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

}
