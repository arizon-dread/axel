package se.inera.axel.shs.client;

import org.apache.commons.httpclient.Header;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.httpclient.util.HttpURLConnection;
import se.inera.axel.shs.exception.MissingDeliveryExecutionException;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.processor.ShsMessageListMarshaller;
import se.inera.axel.shs.processor.ShsMessageMarshaller;
import se.inera.axel.shs.xml.UrnAddress;
import se.inera.axel.shs.xml.message.ShsMessageList;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

public class DefaultShsClient implements ShsClient {

    String rsUrl = null;
    String dsUrl = null;
    String shsAddress = null;

    HttpClient httpClient;
    String keyStore;
    String trustStore;
    String keyStorePassword;
    String trustStorePassword;

    public DefaultShsClient() {

    }


    @Override
    public String send(ShsMessage shsMessage) throws IOException {

        HttpClient httpClient = getHttpClient();

        PostMethod postMethod = new PostMethod(getRsUrl());

        postMethod.setRequestEntity(new ShsMessageRequestEntity(shsMessage));

        try {
            int statusCode = httpClient.executeMethod(postMethod);
            switch (statusCode) {
                case HttpURLConnection.HTTP_ACCEPTED:
                case HttpURLConnection.HTTP_OK:

                    // should return nothing or an txId for example.
                    String body = postMethod.getResponseBodyAsString(1024);

                    Header header = postMethod.getResponseHeader(ShsHeaders.X_SHS_TXID);

                    String txId;

                    if (header == null) {
                        return body;
                    } else {
                        return header.getValue();
                    }

                default:

                    String message = String.format("HTTP status code %d (%s): %s ",
                            postMethod.getStatusCode(), postMethod.getStatusText(),
                            postMethod.getResponseBodyAsString(1024*1024));

                    MissingDeliveryExecutionException e = new MissingDeliveryExecutionException(message);
                    e.setContentId(shsMessage.getLabel().getContent().getContentId());
                    e.setCorrId(shsMessage.getLabel().getCorrId());

                    throw e;
            }
        } finally {
            postMethod.releaseConnection();
        }
    }

    @Override
    public ShsMessage request(ShsMessage shsMessage) throws IOException {

        HttpClient httpClient = getHttpClient();

        PostMethod postMethod = new PostMethod(getRsUrl());

        postMethod.setRequestEntity(new ShsMessageRequestEntity(shsMessage));

        try {
            int statusCode = httpClient.executeMethod(postMethod);
            switch (statusCode) {
                case HttpURLConnection.HTTP_ACCEPTED:
                case HttpURLConnection.HTTP_OK:

                    ShsMessageMarshaller marshaller = new ShsMessageMarshaller();

                    try (InputStream responseStream = postMethod.getResponseBodyAsStream()) {
                        return marshaller.unmarshal(responseStream);
                    }

                case HttpURLConnection.HTTP_NO_CONTENT:
                    postMethod.getResponseBodyAsString(1024);
                    return null;

                default:

                    String message = String.format("HTTP status code %d (%s): %s",
                            postMethod.getStatusCode(), postMethod.getStatusText(),
                            postMethod.getResponseBodyAsString(1024*1024));
                    MissingDeliveryExecutionException e = new MissingDeliveryExecutionException(message);
                    e.setContentId(shsMessage.getLabel().getContent().getContentId());
                    e.setCorrId(shsMessage.getLabel().getCorrId());

                    throw e;
            }
        } finally {
            postMethod.releaseConnection();
        }
    }

    @Override
    public ShsMessageList list(MessageListConditions conditions) throws IOException {
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

                    ShsMessageListMarshaller marshaller = new ShsMessageListMarshaller();

                    try (InputStream responseStream = getMethod.getResponseBodyAsStream()) {
                        return marshaller.unmarshal(responseStream);
                    }

                default:
                    String message = String.format("HTTP status code %d (%s): %s",
                            getMethod.getStatusCode(), getMethod.getStatusText(),
                            getMethod.getResponseBodyAsString(1024*1024));

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
        if (conditions.getProducttype() != null)
            queryParams.add(new NameValuePair("producttype", conditions.getProducttype()));

        if (conditions.getSince() != null)
            queryParams.add(new NameValuePair("since", conditions.getSince()));

        if (conditions.getFilter() != null)
            queryParams.add(new NameValuePair("filter", conditions.getFilter()));

        if (conditions.getStatus() != null)
            queryParams.add(new NameValuePair("status", conditions.getStatus()));

        if (conditions.getOriginator() != null)
            queryParams.add(new NameValuePair("originator", conditions.getOriginator()));

        if (conditions.getEndrecipient() != null)
            queryParams.add(new NameValuePair("endrecipient", conditions.getEndrecipient()));

        if (conditions.getContentid() != null)
            queryParams.add(new NameValuePair("contentid", conditions.getContentid()));

        if (conditions.getCorrid() != null)
            queryParams.add(new NameValuePair("corrid", conditions.getCorrid()));

        if (conditions.getMaxhits() != null)
            queryParams.add(new NameValuePair("maxhits", "" + conditions.getMaxhits()));

        if (conditions.getMetaname() != null)
            queryParams.add(new NameValuePair("metaname", conditions.getMetaname()));

        if (conditions.getMetavalue() != null)
            queryParams.add(new NameValuePair("metavalue", conditions.getMetavalue()));

        if (conditions.getSortattribute() != null)
            queryParams.add(new NameValuePair("sortattribute", conditions.getSortattribute()));

        if (conditions.getSortorder() != null)
            queryParams.add(new NameValuePair("sortorder", conditions.getSortorder()));

        if (conditions.getArrivalorder() != null)
            queryParams.add(new NameValuePair("arrivalorder", conditions.getArrivalorder()));


        return queryParams.toArray(new NameValuePair[0]);
    }

    @Override
    public ShsMessage fetch(String txId) throws IOException {

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

                    ShsMessageMarshaller marshaller = new ShsMessageMarshaller();
                    try (InputStream responseStream = getMethod.getResponseBodyAsStream()) {
                        return marshaller.unmarshal(responseStream);
                    }

                default:
                    String message = String.format("HTTP status code %d (%s): %s",
                            getMethod.getStatusCode(), getMethod.getStatusText(),
                            getMethod.getResponseBodyAsString(1024*1024));

                    throw new HttpException(message);
            }
        } finally {
            getMethod.releaseConnection();
        }
    }

    @Override
    public void ack(String txId) throws IOException {

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

                    // should return nothing
                    postMethod.getResponseBody(1024);
                    break;

                default:
                    String message = String.format("HTTP status code %d (%s): %s",
                            postMethod.getStatusCode(), postMethod.getStatusText(),
                            postMethod.getResponseBodyAsString(1024*1024));

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

    @Override
    public String getShsAddress() {
        return shsAddress;
    }

    public void setShsAddress(String shsAddress) {
        this.shsAddress = shsAddress;
    }

    public String getKeyStore() {
        return keyStore;
    }

    public void setKeyStore(String keyStore) {
        this.keyStore = keyStore;
    }

    public String getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    public String getKeyStorePassword() {
        return keyStorePassword;
    }

    public void setKeyStorePassword(String keyStorePassword) {
        this.keyStorePassword = keyStorePassword;
    }

    public String getTrustStorePassword() {
        return trustStorePassword;
    }

    public void setTrustStorePassword(String trustStorePassword) {
        this.trustStorePassword = trustStorePassword;
    }

    protected HttpClient getHttpClient() {
        if (this.httpClient == null) {
            ProtocolSocketFactory sslProtocolFactory = getSSLProtocolFactory();
            if (sslProtocolFactory != null) {
                Protocol https = new Protocol("https", sslProtocolFactory, 443);
                Protocol.registerProtocol("https", https);
            }

            this.httpClient = new HttpClient();
            MultiThreadedHttpConnectionManager connectionManager =
                    new MultiThreadedHttpConnectionManager();
            httpClient.setHttpConnectionManager(connectionManager);
        }
        return httpClient;
    }

    protected ProtocolSocketFactory getSSLProtocolFactory() {
        if (getKeyStore() == null && getTrustStore() == null)
            return null;

        try {
            URL keystoreUrl = null;
            if (getKeyStore() != null) {
                keystoreUrl = new URL(getKeyStore());
            }
            URL truststoreUrl = null;
            if (getTrustStore() != null) {
                truststoreUrl = new URL(getTrustStore());
            }

            return new AuthSSLProtocolSocketFactory(
                    keystoreUrl, getKeyStorePassword(),
                    truststoreUrl, getTrustStorePassword());
        } catch (MalformedURLException e) {
            throw new AuthSSLInitializationError("Malformed URL", e);
        }
    }

}
