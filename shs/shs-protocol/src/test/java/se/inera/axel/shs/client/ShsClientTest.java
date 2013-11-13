package se.inera.axel.shs.client;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import se.inera.axel.shs.exception.MissingDeliveryExecutionException;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ResponseMessageBuilder;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.processor.ShsMessageMarshaller;
import se.inera.axel.shs.xml.label.SequenceType;
import se.inera.axel.shs.xml.label.TransferType;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static org.testng.Assert.*;
import static org.testng.AssertJUnit.assertNotNull;
import static se.inera.axel.shs.mime.ShsMessageMaker.ShsMessage;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabel;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.*;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.To;

public class ShsClientTest {

    ShsClient shsClient;
    Server server;
    ShsMessageMarshaller messageMarshaller = new ShsMessageMarshaller();
    ServletContextHandler servletContextHandler = new ServletContextHandler();

    @BeforeClass
    public void setup() throws Exception {

        server = new Server(0);
        server.setHandler(servletContextHandler);
        server.start();
        while (!server.isStarted()) {
            Thread.sleep(100);
        }

        shsClient = new ShsClient();
        shsClient.setRsUrl("http://localhost:" + server.getConnectors()[0].getLocalPort() + "/shs/rs");
        shsClient.setDsUrl("http://localhost:" + server.getConnectors()[0].getLocalPort() + "/shs/ds");

    }

    @AfterMethod
    public void clearServlets() {
        servletContextHandler.getServletHandler().setServlets(null);
    }

    @AfterClass
    public void teardown() throws Exception {
        server.stop();
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

}

