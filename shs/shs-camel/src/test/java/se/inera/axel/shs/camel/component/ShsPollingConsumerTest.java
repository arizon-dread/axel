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
import org.apache.camel.testng.CamelTestSupport;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.testng.annotations.Test;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.xml.label.TransferType;

import java.util.HashMap;
import java.util.Map;

import static se.inera.axel.shs.mime.ShsMessageTestObjectMother.*;

@ContextConfiguration
public class ShsPollingConsumerTest extends CamelTestSupport {
//	@Produce(uri = "direct:start")
//	ProducerTemplate producer;
//
	@EndpointInject(uri = "mock:result")
	MockEndpoint resultEndpoint;
	
	@Override
	protected RouteBuilder createRouteBuilder() throws Exception {
		return new RouteBuilder() {
			
			@Override
			public void configure() throws Exception {
				
				from("shs:http://localhost:8585/shs/ds")
                .to("file:/tmp/out")
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
