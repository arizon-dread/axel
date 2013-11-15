package se.inera.axel.shs.client;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.util.HttpURLConnection;
import org.apache.commons.lang.StringUtils;
import se.inera.axel.shs.exception.MissingDeliveryExecutionException;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.processor.ShsMessageListMarshaller;
import se.inera.axel.shs.processor.ShsMessageMarshaller;
import se.inera.axel.shs.xml.UrnAddress;
import se.inera.axel.shs.xml.UrnProduct;
import se.inera.axel.shs.xml.message.Message;
import se.inera.axel.shs.xml.message.ShsMessageList;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

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

    public ShsMessageList list(String shsAddress, MessageListConditions conditions) throws IOException {
        HttpClient httpClient = getHttpClient();

        if (shsAddress == null) {
            throw new IllegalArgumentException("shsAddress must be specified");
        }

        GetMethod getMethod = new GetMethod(getDsUrl() + "/" + UrnAddress.valueOf(shsAddress).toUrnForm());

        getMethod.setQueryString(createQueryString(conditions));

        try {
            int statusCode = httpClient.executeMethod(getMethod);
            switch (statusCode) {
                case HttpURLConnection.HTTP_OK:

                    ShsMessageListMarshaller m = new ShsMessageListMarshaller();
                    return m.unmarshal(getMethod.getResponseBodyAsString());

                default:
                    String message = String.format("HTTP status code %d (%s): %s",
                            getMethod.getStatusCode(), getMethod.getStatusText(),
                            getMethod.getResponseBodyAsString());

                    throw new HttpException(message);
            }
        } finally {
            getMethod.releaseConnection();
        }
    }

    private NameValuePair[] createQueryString(MessageListConditions conditions) {

        if (conditions == null) {
            return new NameValuePair[0];
        }

        List<NameValuePair> queryParams = new ArrayList();
        StringBuffer ps = new StringBuffer();
        for (String productType : conditions.getProductIds()) {
            ps.append(UrnProduct.valueOf(productType).toUrnForm()).append(",");
        }

        if (ps.length() > 0) {
            String p = ps.toString();
            NameValuePair param = new NameValuePair("producttype", p.substring(0, ps.length() - 1));
            queryParams.add(param);
        }


        return queryParams.toArray(new NameValuePair[0]);
    }

    public ShsMessage fetch(String shsAddress, String txId) throws IOException {

        HttpClient httpClient = getHttpClient();

        if (shsAddress == null) {
            throw new IllegalArgumentException("shsAddress must be specified");
        }

        if (txId == null) {
            throw new IllegalArgumentException("txId must be specified");
        }

        GetMethod getMethod = new GetMethod(getDsUrl()
                + "/" + UrnAddress.valueOf(shsAddress).toUrnForm()
                + "/" + txId);

        try {
            int statusCode = httpClient.executeMethod(getMethod);
            switch (statusCode) {
                case HttpURLConnection.HTTP_OK:

                    ShsMessageMarshaller m = new ShsMessageMarshaller();
                    return m.unmarshal(getMethod.getResponseBodyAsStream());

                default:
                    String message = String.format("HTTP status code %d (%s): %s",
                            getMethod.getStatusCode(), getMethod.getStatusText(),
                            getMethod.getResponseBodyAsString());

                    throw new HttpException(message);
            }
        } finally {
            getMethod.releaseConnection();
        }
    }

    public void ack(String shsAddress, String txId) throws IOException {

        HttpClient httpClient = getHttpClient();

        if (shsAddress == null) {
            throw new IllegalArgumentException("shsAddress must be specified");
        }

        if (txId == null) {
            throw new IllegalArgumentException("txId must be specified");
        }

        PostMethod postMethod = new PostMethod(getDsUrl()
                + "/" + UrnAddress.valueOf(shsAddress).toUrnForm()
                + "/" + txId);

        postMethod.setQueryString("action=ack");

        try {
            int statusCode = httpClient.executeMethod(postMethod);
            switch (statusCode) {
                case HttpURLConnection.HTTP_OK:

                    postMethod.getResponseBody();
                    break;

                default:
                    String message = String.format("HTTP status code %d (%s): %s",
                            postMethod.getStatusCode(), postMethod.getStatusText(),
                            postMethod.getResponseBodyAsString());

                    throw new HttpException(message);
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
