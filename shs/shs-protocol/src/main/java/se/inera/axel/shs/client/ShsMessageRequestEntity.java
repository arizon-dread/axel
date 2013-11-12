package se.inera.axel.shs.client;

import org.apache.commons.httpclient.methods.RequestEntity;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.processor.ShsMessageMarshaller;

import java.io.IOException;
import java.io.OutputStream;

/**
 * A RequestEntity that represents an ShsMessage.
 *
 * @author Jan Hallonstén, jan.hallonsten@r2m.se
 */
public class ShsMessageRequestEntity implements RequestEntity {
    private final ShsMessage shsMessage;
    private final ShsMessageMarshaller marshaller = new ShsMessageMarshaller();

    public ShsMessageRequestEntity(ShsMessage message) {
        this.shsMessage = message;
    }

    @Override
    public boolean isRepeatable() {
        return true;
    }

    @Override
    public void writeRequest(OutputStream out) throws IOException {
        marshaller.marshal(shsMessage, out);
    }

    @Override
    public long getContentLength() {
        return -1;
    }

    @Override
    public String getContentType() {
        return ShsHeaders.SHS_CONTENT_TYPE;
    }
}
