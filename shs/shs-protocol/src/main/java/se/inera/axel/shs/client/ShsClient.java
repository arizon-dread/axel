package se.inera.axel.shs.client;

import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.message.ShsMessageList;

import java.io.IOException;

public interface ShsClient {
    String send(ShsMessage shsMessage) throws IOException;

    ShsMessage request(ShsMessage shsMessage) throws IOException;

    ShsMessageList list(MessageListConditions conditions) throws IOException;

    ShsMessage fetch(String txId) throws IOException;

    void ack(String txId) throws IOException;

    String getShsAddress();
}
