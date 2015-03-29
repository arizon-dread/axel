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
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.JndiRegistry;
import org.apache.camel.testng.CamelTestSupport;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;
import se.inera.axel.shs.client.ShsClient;
import se.inera.axel.shs.processor.ShsHeaders;

@ContextConfiguration
public class ShsPollingConsumerIT extends CamelTestSupport {
//	@Produce(uri = "direct:start")
//	ProducerTemplate producer;
//
	@EndpointInject(uri = "mock:result")
	MockEndpoint resultEndpoint;

    //ShsClient client = new ShsClient();

    @Override
    protected JndiRegistry createRegistry() throws Exception {
        JndiRegistry reg = super.createRegistry();


        ShsClient client = new ShsClient();
        client.setRsUrl("http://localhost:8585/shs/rs");
        client.setDsUrl("http://localhost:8585/shs/ds");

        reg.bind("client", client);

        return reg;
    }

    @Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {

				from("shs:client?to=0000000000.jmeter&status=test&producttype=00000000-0000-0000-0000-000000000000")
                .to("log:se.inera.axel.ShsPollingConsumerIT?showAll=true")
                .process(new Processor() {
                    @Override
                    public void process(Exchange exchange) throws Exception {
                     //   throw new RuntimeException("nu vart det error in batch index " + exchange.getProperty(Exchange.BATCH_INDEX));
                    }
                })
                .setHeader(Exchange.FILE_NAME, header(ShsHeaders.TXID))
                .to("file:/tmp/out")
                .to("mock:result");


                from("shs:client?to=0000000000.jmeter&status=test&producttype=confirm")
                                .to("log:se.inera.axel.ShsPollingConsumerIT?showAll=true")
                                .to("file:/tmp/out/confirms")
                				.to("mock:result");

                from("shs:client?to=0000000000.jmeter&status=test&producttype=error")
                        .to("log:se.inera.axel.ShsPollingConsumerIT?showAll=true")
                        .to("file:/tmp/out/errors")
                        .to("mock:result");

            }
		};


	}
	
	@DirtiesContext
	@Test(enabled = true)
	public void testShouldThrowException() throws Exception {

//        resultEndpoint.assertIsSatisfied(1000);
//        Exchange exchange = resultEndpoint.getExchanges().get(0);
        Thread.sleep(100000);
    }
	

}
