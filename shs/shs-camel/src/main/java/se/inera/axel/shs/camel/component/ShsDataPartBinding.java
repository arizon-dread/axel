package se.inera.axel.shs.camel.component;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import org.apache.camel.impl.DefaultMessage;
import org.apache.commons.lang.StringUtils;
import se.inera.axel.shs.exception.IllegalDatapartContentException;
import se.inera.axel.shs.mime.DataPart;
import se.inera.axel.shs.processor.InputStreamDataSource;
import se.inera.axel.shs.processor.ShsHeaders;

import javax.activation.DataHandler;
import java.io.File;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShsDataPartBinding {

    public DataPart toDataPart(Message in) throws Exception {

        Object body = in.getBody();

        if (body instanceof DataPart) {
            return (DataPart)body;
        }

        DataPart dataPart = new DataPart();

        dataPart.setDataPartType(in.getHeader(ShsHeaders.DATAPART_TYPE, String.class));
        if (dataPart.getDataPartType() == null) {
            throw new IllegalDatapartContentException("Header '" + ShsHeaders.DATAPART_TYPE + "' must be specified");
        }

        String contentType = in.getHeader(ShsHeaders.DATAPART_CONTENTTYPE, String.class);
        if (contentType == null) {
            contentType = in.getHeader(Exchange.CONTENT_TYPE, String.class);
        }


        if (contentType != null) {
            if (!contentType.contains("charset")) {
                String charset = in.getHeader(Exchange.CHARSET_NAME, String.class);
                if (charset != null) {
                    contentType += ";charset=" + charset;
                }
            }
        }

        dataPart.setContentType(contentType);

        String fileName = in.getHeader(ShsHeaders.DATAPART_FILENAME, String.class);
        if (fileName == null)
            fileName = in.getHeader(Exchange.FILE_NAME_ONLY, String.class);

        if (fileName == null) {
            if (contentType != null) {
                Pattern pattern = Pattern.compile(".+;[ ]*name=(.+?)([ ]*;.+)*");
                Matcher matcher = pattern.matcher(contentType);
                if (matcher.matches()) {
                    fileName = matcher.group(1);
                }
            }
        }

        if (fileName == null) {
            if (body instanceof File) {
                fileName = ((File) body).getName();
            }
        }
        dataPart.setFileName(fileName);

        Long contentLength = in.getHeader(ShsHeaders.DATAPART_CONTENTLENGTH, Long.class);
        if (contentLength == null) {
            contentLength = in.getHeader(Exchange.CONTENT_LENGTH, Long.class);
        }
        if (contentLength == null) {
            if (body instanceof File) {
                contentLength = ((File) body).length();
            }
        }

        if (contentLength == null) {
            contentLength = 0L;
        }


        dataPart.setContentLength(contentLength.longValue());
        dataPart.setDataHandler(
                new DataHandler(
                        new InputStreamDataSource(
                                in.getMandatoryBody(InputStream.class),
                                dataPart.getContentType(), dataPart.getFileName())));

        String transferEncoding = in.getHeader(ShsHeaders.DATAPART_TRANSFERENCODING, "binary", String.class);

        if ("binary".equalsIgnoreCase(transferEncoding) == false
                && "base64".equalsIgnoreCase(transferEncoding) == false)
        {
            throw new IllegalDatapartContentException("transfer encoding not supported: " + transferEncoding);
        }

        dataPart.setTransferEncoding(transferEncoding);
        return dataPart;

    }

    public Message fromDataPart(DataPart dataPart) throws Exception {

        Message out = new DefaultMessage();
        Map<String, Object> headers = out.getHeaders();

        headers.put(ShsHeaders.DATAPART_CONTENTLENGTH, dataPart.getContentLength());
        headers.put(ShsHeaders.DATAPART_CONTENTTYPE, dataPart.getContentType());
        headers.put(ShsHeaders.DATAPART_TRANSFERENCODING, dataPart.getTransferEncoding());
        headers.put(ShsHeaders.DATAPART_TYPE, dataPart.getDataPartType());
        if (StringUtils.isNotBlank(dataPart.getFileName())) {
            headers.put(ShsHeaders.DATAPART_FILENAME, dataPart.getFileName());
        }

        if (dataPart.getContentType() != null) {
            Pattern pattern = Pattern.compile(".+;[ ]*charset=(.+?)([ ]*;.+)*");
            Matcher matcher = pattern.matcher(dataPart.getContentType());
            if (matcher.matches()) {
                String charset = matcher.group(1);
                headers.put(Exchange.CHARSET_NAME, charset);
            }
        }

        out.setBody(dataPart.getDataHandler().getInputStream());
        return out;
    }

}
