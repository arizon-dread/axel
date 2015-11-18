/**
 * Copyright (C) 2013 Inera AB (http://www.inera.se)
 *
 * This file is part of Inera Axel (http://code.google.com/p/inera-axel).
 *
 * Inera Axel is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Inera Axel is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */
package se.inera.axel.shs.camel.component;

import org.apache.camel.EndpointInject;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.builder.PredicateBuilder;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.testng.CamelTestSupport;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;
import se.inera.axel.shs.client.DefaultShsClient;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.processor.ShsMessageMarshaller;
import se.inera.axel.shs.xml.label.SequenceType;
import se.inera.axel.shs.xml.label.Status;
import se.inera.axel.shs.xml.label.TransferType;
import se.inera.axel.shs.xml.message.Message;
import se.inera.axel.shs.xml.message.ShsMessageList;

import java.util.HashMap;

import static com.natpryce.makeiteasy.MakeItEasy.*;
import static se.inera.axel.shs.mime.ShsMessageMaker.ShsMessage;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.*;
import static se.inera.axel.shs.xml.label.ShsLabelMaker.ShsLabelInstantiator.*;

@ContextConfiguration
public class ShsPollingConsumerIT extends CamelTestSupport {

	@EndpointInject(uri = "mock:result")
	MockEndpoint resultEndpoint;

    int port = PortFinder.findFreePort();

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry reg = super.createRegistry();


        DefaultShsClient client = new DefaultShsClient();
        client.setRsUrl("http://localhost:" + port + "/shs/rs");
        client.setDsUrl("http://localhost:" + port + "/shs/ds");

        reg.bind("client", client);

        return reg;
    }

    @Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {

				from("shs:client?to=0000000000.junit&producttype=00000000-0000-0000-0000-000000000000")
                .setHeader(Exchange.FILE_NAME, header(ShsHeaders.TXID))
                .to("mock:result");


                final HashMap<String, se.inera.axel.shs.mime.ShsMessage> messages = new HashMap();

                se.inera.axel.shs.mime.ShsMessage shs1 = make(an(ShsMessage,
                    with(ShsMessage.label, an(ShsLabel,
                        with(txId, "88B06FFC-860D-430F-B812-A0DA2EB40E9F"),
                        with(status, Status.TEST),
                        with(to, a(To, with(To.value, "0000000000.jmeter"))),
                        with(product, a(Product, with(Product.value, "00000000-0000-0000-0000-000000000000"))),
                        with(sequenceType, SequenceType.EVENT),
                        with(transferType, TransferType.ASYNCH)))));


                messages.put(shs1.getLabel().getTxId(), shs1);

                /* mocking shs server */
                from("jetty:http://localhost:" + port + "/shs/ds/urn:X-shs:0000000000.junit")
                    .process(new Processor() {
                        @Override
                        public void process(Exchange exchange) throws Exception {
                            ShsMessageList list = new ShsMessageList();
                            for (se.inera.axel.shs.mime.ShsMessage shs : messages.values()) {
                                if (shs.getLabel().getProduct().getValue().equals(exchange.getIn().getHeader("producttype"))) {
                                    ShsMessageMarshaller msh = new ShsMessageMarshaller();
                                    Message m = new Message();
                                    m.setFrom(shs.getLabel().getFrom().getValue());
                                    m.setTo(shs.getLabel().getTo().getValue());
                                    m.setProduct(shs.getLabel().getProduct().getValue());
                                    m.setTxId(shs.getLabel().getTxId());
                                    m.setStatus(shs.getLabel().getStatus());
                                    m.setContentId(shs.getLabel().getContent().getContentId());
                                    m.setCorrId(shs.getLabel().getCorrId());

                                    list.getMessage().add(m);
                                }
                            }
                            exchange.getIn().setBody(list);
                        }
                    })
                    .convertBodyTo(String.class);

                from("jetty:http://localhost:" + port + "/shs/ds/{shsTo}/{txId}")
                        .choice()
                        .when(header(Exchange.HTTP_METHOD).isEqualTo("GET"))
                        .process(new Processor() {
                            @Override
                            public void process(Exchange exchange) throws Exception {
                                se.inera.axel.shs.mime.ShsMessage shs = messages.get("88B06FFC-860D-430F-B812-A0DA2EB40E9F");
                                if (shs == null) {
                                    throw new RuntimeException("message not found");
                                }
                                exchange.getIn().setBody(shs);
                            }
                        })
                        .when(PredicateBuilder.and(
                                header(Exchange.HTTP_METHOD).isEqualTo("POST"),
                                header("action").isEqualTo("ack")))
                        .process(new Processor() {
                            @Override
                            public void process(Exchange exchange) throws Exception {
                                messages.remove("88B06FFC-860D-430F-B812-A0DA2EB40E9F");
                            }
                    })
                    .otherwise().throwException(new IllegalArgumentException("Method not allowed"));
            }
        };
	}
	
	@DirtiesContext
	@Test(enabled = true)
	public void testShouldThrowException() throws Exception {
        resultEndpoint.expectedMessageCount(1);
        resultEndpoint.expectedBodiesReceived("Message body");
        resultEndpoint.expectedHeaderReceived(ShsHeaders.SEQUENCETYPE, SequenceType.EVENT);
        resultEndpoint.expectedHeaderReceived(ShsHeaders.PRODUCT_ID, "00000000-0000-0000-0000-000000000000");
        resultEndpoint.expectedHeaderReceived(ShsHeaders.STATUS, Status.TEST);
        resultEndpoint.expectedHeaderReceived(ShsHeaders.TXID, "88B06FFC-860D-430F-B812-A0DA2EB40E9F");
        resultEndpoint.expectedHeaderReceived(ShsHeaders.DATAPART_FILENAME, "testfile.txt");
        resultEndpoint.expectedHeaderReceived(Exchange.FILE_NAME, "88B06FFC-860D-430F-B812-A0DA2EB40E9F");
        resultEndpoint.assertIsSatisfied(4000);
    }


}
