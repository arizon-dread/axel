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
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.testng.CamelTestSupport;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;
import se.inera.axel.shs.camel.ShsMessageDataFormat;
import se.inera.axel.shs.client.DefaultShsClient;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.xml.label.SequenceType;
import se.inera.axel.shs.xml.label.ShsLabel;
import se.inera.axel.shs.xml.label.TransferType;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@ContextConfiguration
public class ShsComponentIT extends CamelTestSupport {
	@EndpointInject(uri = "mock:result")
	MockEndpoint resultEndpoint;

    @EndpointInject(uri = "mock:shsSink")
    MockEndpoint shsSink;

    int port = PortFinder.findFreePort();

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry reg = super.createRegistry();


        DefaultShsClient client = new DefaultShsClient();
        client.setRsUrl("http://localhost:" + port + "/shs/rs");
        client.setDsUrl("http://localhost:" + port + "/shs/ds");
        client.setShsAddress("0000000000.junit");

        reg.bind("testAxel", client);

        reg.bind("shsDataFormat", new ShsMessageDataFormat());

        return reg;
    }

	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("direct:async1")
				.setHeader(ShsHeaders.TO, constant("0000000000.junit"))
                .setHeader(ShsHeaders.DATAPART_CONTENTTYPE, constant("text/xml"))
                .setHeader(ShsHeaders.DATAPART_FILENAME, constant("MyXmlFile.xml"))
                .setHeader(ShsHeaders.DATAPART_TYPE, constant("xml"))
				.to("shs:send:00000000-0000-0000-0000-000000000000")
                .to(resultEndpoint);

                from("direct:sync1")
                .setHeader(ShsHeaders.DATAPART_CONTENTTYPE, constant("text/xml"))
                .setHeader(ShsHeaders.DATAPART_FILENAME, constant("MyXmlFile.xml"))
                .setHeader(ShsHeaders.DATAPART_TYPE, constant("xml"))
                .to("shs:request:00000000-0000-0000-0000-000000000000?to=0000000000.junit")
                .to(resultEndpoint);

                /* mocking shs server */
                from("jetty:http://localhost:" + port + "/shs/rs")
                .bean(DefaultShsMessageBinding.class)
                .choice()
                .when(header(ShsHeaders.TRANSFERTYPE).isEqualTo(TransferType.ASYNCH))
                    .transform(header(ShsHeaders.TXID))
                .otherwise()
                    .setHeader(ShsHeaders.SEQUENCETYPE, constant(SequenceType.REPLY))
                    .transform(constant("SVAR"))
                    .bean(DefaultShsMessageBinding.class, "toShsMessage")
                .end();

			}
		};
	}

	@DirtiesContext
	@Test(enabled = true)
	public void sendingAsynchMessageShouldReturnTxId() throws Exception {

        resultEndpoint.expectedMessageCount(1);

        Map<String, Object> headers = new HashMap<>();
        template.sendBodyAndHeaders("direct:async1", "BODY", headers);

        resultEndpoint.assertIsSatisfied(1000);
        Exchange exchange = resultEndpoint.getReceivedExchanges().get(0);
        String txId = exchange.getIn().getMandatoryBody(String.class);

        UUID.fromString(txId);
    }

    @DirtiesContext
    @Test(enabled = true)
    public void sendingSynchMessageShouldReturnResponseMessage() throws Exception {

        resultEndpoint.expectedMessageCount(1);

        Map<String, Object> headers = new HashMap<>();
        template.sendBodyAndHeaders("direct:sync1", "BODY", headers);

        resultEndpoint.assertIsSatisfied(1000);
        Exchange exchange = resultEndpoint.getReceivedExchanges().get(0);
        String response = exchange.getIn().getMandatoryBody(String.class);
        assertEquals(response, "SVAR");

        ShsLabel label = exchange.getProperty(ShsHeaders.LABEL, ShsLabel.class);
        assertNotNull(label);
        assertEquals(label.getSequenceType(), SequenceType.REPLY);

    }

}
