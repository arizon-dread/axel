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

import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.Processor;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.component.http.HttpProducer;
import org.apache.camel.impl.DefaultProducer;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.axel.shs.camel.ShsMessageRequestEntity;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ShsHeaders;

import java.io.InputStream;
import java.util.Map;

/**
 * The HelloWorld producer.
 */
public class ShsProducer extends DefaultProducer {
    private static final transient Logger log = LoggerFactory.getLogger(ShsProducer.class);
    private ShsEndpoint endpoint;

    public ShsProducer(ShsEndpoint endpoint) {
        super(endpoint);
        this.endpoint = endpoint;
    }

    @Override
    public void process(final Exchange exchange) throws Exception {
        HttpClient httpClient = new HttpClient();

        PostMethod postMethod = new PostMethod(getDestinationUri(exchange));

        postMethod.setRequestEntity(new ShsMessageRequestEntity(exchange.getIn().getBody(ShsMessage.class)));

        int statusCode = httpClient.executeMethod(postMethod);
        switch (statusCode) {
            case 202:
            case 200:

        }
	}



	private String getDestinationUri(Exchange exchange) {
		String destinationUri = exchange.getIn().getHeader(ShsHeaders.DESTINATION_URI, String.class);
		
		if (StringUtils.isBlank(destinationUri)) {
			destinationUri = endpoint.getDestinationUri(); 
		}
		
		return destinationUri;
	}


}
