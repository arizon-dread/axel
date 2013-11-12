package se.inera.axel.shs.client;

import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.SequenceType;
import se.inera.axel.shs.xml.label.TransferType;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static org.testng.Assert.assertEquals;
import static se.inera.axel.shs.mime.ShsMessageMaker.ShsMessage;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabel;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.*;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.To;

public class ShsClientTest {

    ShsClient shsClient;


    @BeforeClass
    public void setupClient() {
        shsClient = new ShsClient();
        shsClient.setRsUrl("http://localhost:8585/shs/rs");
    }

    @Test
    public void sendAsynchMessageShouldWork() throws Exception {

        final se.inera.axel.shs.mime.ShsMessage shsMessage = make(a(ShsMessage));

        String txId = shsClient.send(shsMessage);
        assertEquals(txId, shsMessage.getLabel().getTxId());

    }


    @Test
    public void sendSynchRequestMessageShouldWork() throws Exception {

        se.inera.axel.shs.mime.ShsMessage request = make(a(ShsMessage,
                with(ShsMessage.label, a(ShsLabel,
                        with(to, a(To, with(To.value, "0000000000"))),
                        with(sequenceType, SequenceType.REQUEST),
                        with(transferType, TransferType.SYNCH)))));

        ShsMessage response = shsClient.request(request);

        assertEquals(response.getLabel().getSequenceType(), SequenceType.REPLY);

    }

}

