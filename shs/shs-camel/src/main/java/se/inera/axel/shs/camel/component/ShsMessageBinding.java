package se.inera.axel.shs.camel.component;

import org.apache.camel.Exchange;
import se.inera.axel.shs.mime.ShsMessage;

public interface ShsMessageBinding {
    ShsMessage toShsMessage(Exchange exchange) throws Exception;
    void fromShsMessage(ShsMessage shsMessage, Exchange exchange) throws Exception;
}
