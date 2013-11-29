package se.inera.axel.shs.camel;

import org.apache.camel.Exchange;
import org.apache.commons.lang.StringUtils;
import se.inera.axel.shs.exception.IllegalDatapartContentException;
import se.inera.axel.shs.mime.DataPart;
import se.inera.axel.shs.processor.InputStreamDataSource;
import se.inera.axel.shs.processor.ShsHeaders;

import javax.activation.DataHandler;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ShsDataPartBinding {

    public DataPart toDataPart(InputStream body, Map<String, Object> headers) throws Exception {

        DataPart dataPart = new DataPart();

        dataPart.setDataPartType((String)headers.get(ShsHeaders.DATAPART_TYPE));
        if (dataPart.getDataPartType() == null) {
            throw new IllegalDatapartContentException("Header '" + ShsHeaders.DATAPART_TYPE + "' must be specified");
        }

        String contentType = (String)headers.get(ShsHeaders.DATAPART_CONTENTTYPE);
        if (contentType == null) {
            contentType = (String)headers.get(Exchange.CONTENT_TYPE);
        }


        if (contentType != null) {
            if (!contentType.contains("charset")) {
                String charset = (String)headers.get(Exchange.CHARSET_NAME);
                if (charset != null) {
                    contentType += ";charset=" + charset;
                }
            }
        }

        dataPart.setContentType(contentType);

        String fileName = (String)headers.get(ShsHeaders.DATAPART_FILENAME);
        if (fileName == null)
            fileName = (String)headers.get(Exchange.FILE_NAME_ONLY);

        if (fileName == null) {
            if (contentType != null) {
                Pattern pattern = Pattern.compile(".+;[ ]*name=(.+?)([ ]*;.+)*");
                Matcher matcher = pattern.matcher(contentType);
                if (matcher.matches()) {
                    fileName = matcher.group(1);
                }
            }
        }

        dataPart.setFileName(fileName);

        Long contentLength = (Long)headers.get(ShsHeaders.DATAPART_CONTENTLENGTH);
        if (contentLength == null) {
            contentLength = (Long)headers.get(Exchange.CONTENT_LENGTH);
        }

        if (contentLength == null) {
            contentLength = 0L;
        }


        dataPart.setContentLength(contentLength.longValue());
        dataPart.setDataHandler(
                new DataHandler(
                        new InputStreamDataSource(body,
                                dataPart.getContentType(), dataPart.getFileName())));

        String transferEncoding = (String)headers.get(ShsHeaders.DATAPART_TRANSFERENCODING);
        if (transferEncoding == null) {
            transferEncoding = "binary";
        }

        if ("binary".equalsIgnoreCase(transferEncoding) == false
                && "base64".equalsIgnoreCase(transferEncoding) == false)
        {
            throw new IllegalDatapartContentException("transfer encoding not supported: " + transferEncoding);
        }

        dataPart.setTransferEncoding(transferEncoding);

        //headers.remove("ShsDataPart*");

        return dataPart;

    }

    public InputStream fromDataPart(DataPart dataPart, Map<String, Object> headers) throws Exception {

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

        return dataPart.getDataHandler().getInputStream();
    }

}
