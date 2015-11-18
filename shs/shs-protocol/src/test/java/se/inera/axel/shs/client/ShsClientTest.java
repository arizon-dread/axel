package se.inera.axel.shs.client;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import se.inera.axel.shs.exception.MissingDeliveryExecutionException;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ResponseMessageBuilder;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.processor.ShsMessageListMarshaller;
import se.inera.axel.shs.processor.ShsMessageMarshaller;
import se.inera.axel.shs.xml.UrnAddress;
import se.inera.axel.shs.xml.label.SequenceType;
import se.inera.axel.shs.xml.label.ShsLabel;
import se.inera.axel.shs.xml.label.TransferType;
import se.inera.axel.shs.xml.message.Data;
import se.inera.axel.shs.xml.message.Message;
import se.inera.axel.shs.xml.message.ShsMessageList;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertNotNull;
import static se.inera.axel.shs.mime.ShsMessageMaker.ShsMessage;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabel;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.*;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.To;

public class ShsClientTest {

    DefaultShsClient shsClient;
    Server server;
    ShsMessageMarshaller messageMarshaller = new ShsMessageMarshaller();
    ShsMessageListMarshaller shsMessageListMarshaller = new ShsMessageListMarshaller();
    ServletContextHandler servletContextHandler;
    URL shsTextMessageMime = getClass().getResource("/shsTextMessage.mime");

    @BeforeMethod
    public void setup() throws Exception {

        server = new Server(0);
        servletContextHandler = new ServletContextHandler();
        server.setHandler(servletContextHandler);
        server.start();
        while (!server.isStarted()) {
            Thread.sleep(100);
        }

        shsClient = new DefaultShsClient();
        shsClient.setRsUrl("http://localhost:" + server.getConnectors()[0].getLocalPort() + "/shs/rs");
        shsClient.setDsUrl("http://localhost:" + server.getConnectors()[0].getLocalPort() + "/shs/ds");

    }


    @AfterMethod
    public void teardown() throws Exception {
        server.stop();
        server = null;
        servletContextHandler = null;
        shsClient = null;
    }


    private void addServlet(String pathSpec, HttpServlet servlet) {
        servletContextHandler.addServlet(new ServletHolder(servlet), pathSpec);
    }

    @Test
    public void sendAsynchMessageShouldWork() throws Exception {

        final se.inera.axel.shs.mime.ShsMessage shsMessage = make(a(ShsMessage));


        addServlet("/shs/rs", new HttpServlet() {
            @Override
            protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

                assertEquals(req.getContentType(), ShsHeaders.SHS_CONTENT_TYPE);
                se.inera.axel.shs.mime.ShsMessage msg = messageMarshaller.unmarshal(req.getInputStream());

                assertNotNull(msg);
                assertNotNull(msg.getLabel());

                assertEquals(msg.getLabel().getTransferType(), TransferType.ASYNCH);

                resp.setHeader(ShsHeaders.X_SHS_TXID, msg.getLabel().getTxId());
                resp.setStatus(HttpServletResponse.SC_ACCEPTED);
                resp.getWriter().println(msg.getLabel().getTxId());

            }
        });

        String txId = shsClient.send(shsMessage);
        assertEquals(txId, shsMessage.getLabel().getTxId());

    }

    @Test(expectedExceptions = MissingDeliveryExecutionException.class)
    public void sendAsynchMessageWithWrongUrlShouldThrow() throws Exception {

        final se.inera.axel.shs.mime.ShsMessage shsMessage = make(a(ShsMessage));

        try {
            String txId = shsClient.send(shsMessage);
        } catch (MissingDeliveryExecutionException e) {
            assertEquals(e.getCorrId(), shsMessage.getLabel().getCorrId());
            assertEquals(e.getContentId(), shsMessage.getLabel().getContent().getContentId());
            assertTrue(e.getMessage().contains("404"));

            throw e;
        }

    }


    @Test
    public void sendSynchRequestMessageShouldWork() throws Exception {

        se.inera.axel.shs.mime.ShsMessage request = make(a(ShsMessage,
                with(ShsMessage.label, a(ShsLabel,
                        with(to, a(To, with(To.value, "0000000000"))),
                        with(sequenceType, SequenceType.REQUEST),
                        with(transferType, TransferType.SYNCH)))));

        addServlet("/shs/rs", new HttpServlet() {
            @Override
            protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

                assertEquals(req.getContentType(), ShsHeaders.SHS_CONTENT_TYPE);
                se.inera.axel.shs.mime.ShsMessage msg = messageMarshaller.unmarshal(req.getInputStream());

                assertNotNull(msg);
                assertNotNull(msg.getLabel());

                assertEquals(msg.getLabel().getTransferType(), TransferType.SYNCH);

                resp.setHeader(ShsHeaders.X_SHS_TXID, msg.getLabel().getTxId());
                resp.setContentType(ShsHeaders.SHS_CONTENT_TYPE);
                resp.setStatus(HttpServletResponse.SC_OK);

                ResponseMessageBuilder builder = new ResponseMessageBuilder();
                se.inera.axel.shs.mime.ShsMessage response = builder.buildConfirmMessage(msg);

                messageMarshaller.marshal(response, resp.getOutputStream());

            }
        });

        ShsMessage response = shsClient.request(request);

        assertNotNull(response);
        assertEquals(response.getLabel().getCorrId(), request.getLabel().getCorrId());
        assertEquals(response.getLabel().getProduct().getValue(), "confirm");
        assertEquals(response.getLabel().getSequenceType(), SequenceType.ADM);

    }

    @Test
    public void sendSynchRequestWithNoResponseShouldWork() throws Exception {

        se.inera.axel.shs.mime.ShsMessage request = make(a(ShsMessage,
                with(ShsMessage.label, a(ShsLabel,
                        with(to, a(To, with(To.value, "0000000000"))),
                        with(sequenceType, SequenceType.REQUEST),
                        with(transferType, TransferType.SYNCH)))));

        addServlet("/shs/rs", new HttpServlet() {
            @Override
            protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

                assertEquals(req.getContentType(), ShsHeaders.SHS_CONTENT_TYPE);
                se.inera.axel.shs.mime.ShsMessage msg = messageMarshaller.unmarshal(req.getInputStream());

                assertNotNull(msg);
                assertNotNull(msg.getLabel());

                assertEquals(msg.getLabel().getTransferType(), TransferType.SYNCH);

                resp.setHeader(ShsHeaders.X_SHS_TXID, msg.getLabel().getTxId());
                resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
                resp.flushBuffer();

            }
        });

        ShsMessage response = shsClient.request(request);

        assertNull(response);

    }

    @Test
    public void listMessagesWithNoParamsShouldReturnList() throws Exception {

        addServlet("/shs/ds/*", new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
                InputStream stream = shsTextMessageMime.openStream();
                ShsLabel label = messageMarshaller.parseLabel(stream);
                Assert.assertNotNull(label);

                ShsMessageList list = new ShsMessageList();
                list.getMessage().add(createMessage(label));

                assertEquals(req.getPathInfo(), "/" + UrnAddress.valueOf("0000000000"));

                resp.setContentType("application/xml");
                resp.setStatus(HttpServletResponse.SC_OK);

                shsMessageListMarshaller.marshal(list, resp.getOutputStream());

                resp.flushBuffer();

            };
        });

        ShsMessageList response = shsClient.list("0000000000", null);

        assertNotNull(response);
        assertFalse(response.getMessage().isEmpty());

    }

    @Test
    public void fetchExistingMessagesShouldReturnList() throws Exception {
        final String txId = "4c9fd3e8-b4c4-49aa-926a-52a68864a7b8";
        final String address = "0000000000";

        addServlet("/shs/ds/*", new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

                assertEquals(req.getPathInfo(),
                        "/" + UrnAddress.valueOf(address) +  "/" + txId);

                InputStream stream = shsTextMessageMime.openStream();
                ShsMessage msg = messageMarshaller.unmarshal(stream);

                Assert.assertNotNull(msg);

                resp.setContentType(ShsHeaders.SHS_CONTENT_TYPE);
                resp.setStatus(HttpServletResponse.SC_OK);

                messageMarshaller.marshal(msg, resp.getOutputStream());

                resp.flushBuffer();

            };
        });


        ShsMessage response = shsClient.fetch(address, txId);

        assertNotNull(response);
        assertEquals(response.getLabel().getTxId(), txId);

    }

    @Test
    public void ackExistingMessagesShouldWork() throws Exception {
        final String txId = "4c9fd3e8-b4c4-49aa-926a-52a68864a7b8";
        final String address = "0000000000";

        addServlet("/shs/ds/*", new HttpServlet() {
            @Override
            protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

                fail("ack should not use GET");
            };

            @Override
            protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

                assertEquals(req.getPathInfo(),
                        "/" + UrnAddress.valueOf(address) +  "/" + txId);

                assertEquals(req.getQueryString(), "action=ack");

                resp.setStatus(HttpServletResponse.SC_OK);
                resp.flushBuffer();

            };

        });


        shsClient.ack(address, txId);

    }

    private Message createMessage(ShsLabel label) {
        Message message =
                new Message();

        if (label.getProduct() != null)
            message.setProduct(label.getProduct().getValue());

        if (label.getContent() != null)
            message.setContentId(label.getContent().getContentId());
        message.setCorrId(label.getCorrId());

        if (label.getEndRecipient() != null)
            message.setEndRecipient(label.getEndRecipient().getValue());

        if (label.getFrom() != null)
            message.setFrom(label.getFrom().getValue());

        if (label.getOriginator() != null)
            message.setOriginator(label.getOriginator().getValue());

        message.setSequenceType(label.getSequenceType());
        // message.setSize();
        message.setStatus(label.getStatus());
        message.setSubject(label.getSubject());
        message.setTimestamp(label.getDatetime());

        if (label.getTo() != null)
            message.setTo(label.getTo().getValue());

        message.setTxId(label.getTxId());

        for (Object object : label.getContent().getDataOrCompound()) {
            if (object instanceof se.inera.axel.shs.xml.label.Data) {
                se.inera.axel.shs.xml.label.Data labelData = (se.inera.axel.shs.xml.label.Data)object;
                Data data = new Data();
                data.setDatapartType(labelData.getDatapartType());
                data.setFilename(labelData.getFilename());
                data.setNoOfBytes(labelData.getNoOfBytes());
                data.setNoOfRecords(labelData.getNoOfRecords());
                message.getData().add(data);
            }
        }



        return message;
    }


}

