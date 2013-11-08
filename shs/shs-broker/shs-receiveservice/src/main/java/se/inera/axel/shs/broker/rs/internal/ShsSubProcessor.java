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
package se.inera.axel.shs.broker.rs.internal;

import org.apache.camel.*;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.axel.shs.exception.MissingDeliveryExecutionException;
import se.inera.axel.shs.exception.OtherErrorException;
import se.inera.axel.shs.exception.ShsException;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ResponseMessageBuilder;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.xml.label.ShsLabel;

import java.io.IOException;
import java.util.Map;

/**
 * A processor that sends a SHS message to another camel uri, in a new exchange.
 * Response, exceptions and certain headers are forwarded.
 * It's intended to work like an advanced form of try/catch.
 */
public class ShsSubProcessor implements Processor {
    private static final transient Logger log = LoggerFactory.getLogger(ShsSubProcessor.class);

    ExceptionHandler exceptionHandler = new ExceptionHandler();
    Expression destinationUriExpression;

    public ShsSubProcessor(Expression destinationUriExpression) {
        this.destinationUriExpression = destinationUriExpression;
    }

    public void process(final Exchange inExchange) throws Exception {
        ProducerTemplate producerTemplate = getProducerTemplate(inExchange);

    	Exchange returnedExchange = producerTemplate.send(getDestinationUri(inExchange), ExchangePattern.InOut, new Processor() {

			@Override
			public void process(Exchange exchange) throws Exception {
				Object body = inExchange.getIn().getBody();

				exchange.getIn().setBody(body);
			}	
		});

		Object body = getBody(returnedExchange);
		log.debug("Returned body {}", body);
		inExchange.getIn().setBody(body);

        Map<String, Object> headers = returnedExchange.getOut().getHeaders();
        for (String key: headers.keySet()) {
            if (key.startsWith("x-shs")) {
                inExchange.getIn().setHeader(key, headers.get(key));
            }
        }

		if (isException(returnedExchange)) {
			handleException(inExchange, returnedExchange);
		}
	}

	private void handleException(final Exchange inExchange,
			Exchange returnedExchange) {
        exceptionHandler.handleException(inExchange, returnedExchange);
	}

	private boolean isException(Exchange returnedExchange) {
		return exceptionHandler.isException(returnedExchange);
	}

	private String getDestinationUri(Exchange exchange) {
		return destinationUriExpression.evaluate(exchange, String.class);
	}

    private ProducerTemplate getProducerTemplate(Exchange exchange) {
        return exchange.getContext().createProducerTemplate();
    }

	private Object getBody(Exchange returnedExchange) {
		if (returnedExchange.hasOut()) {
			return returnedExchange.getOut().getBody();
		} else {
			return returnedExchange.getIn().getBody();
		}
	}
}

class ExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(ExceptionHandler.class);

    private boolean isReturnError = false;

    ResponseMessageBuilder responseMessageBuilder = new ResponseMessageBuilder();

    public boolean isReturnError() {
        return isReturnError;
    }

    public void setReturnError(boolean isReturnError) {
        this.isReturnError = isReturnError;
    }

    public void handleException(final Exchange inExchange, final Exchange returnedExchange) {

        ShsLabel label = null;
        ShsMessage shsMessage = inExchange.getContext().getTypeConverter().tryConvertTo(ShsMessage.class, inExchange, inExchange.getIn().getBody());

        if (shsMessage != null)
            label = shsMessage.getLabel();

        if (label == null)
            label = inExchange.getProperty(ShsHeaders.LABEL, ShsLabel.class);

        if (hasException(returnedExchange)) {
            createResponse(inExchange, createOrEnrichShsException(returnedExchange, label));
        }
    }

    private ShsException createOrEnrichShsException(Exchange returnedExchange, ShsLabel label) {

        ShsException shsException = returnedExchange.getException(ShsException.class);

        if (shsException == null) {
            IOException ioException = returnedExchange.getException(IOException.class);

            if (ioException != null) {
                shsException = new MissingDeliveryExecutionException(ioException);
            }
        }

        if (shsException == null) {
            Exception exception = returnedExchange.getException(Exception.class);
            shsException = new OtherErrorException(exception);
        }

        if (label != null) {
            if (StringUtils.isBlank(shsException.getContentId()) && label.getContent() != null) {
                shsException.setContentId(label.getContent().getContentId());
            }

            if (StringUtils.isBlank(shsException.getCorrId())) {
                shsException.setCorrId(label.getCorrId());
            }
        }

        return shsException;
    }

    private void createResponse(final Exchange inExchange,
                                ShsException shsException) {
        if (isReturnError()) {
            inExchange.getIn().setBody(responseMessageBuilder.buildErrorMessage(inExchange.getIn().getBody(ShsMessage.class), shsException));
        } else {
            inExchange.setException(shsException);
        }
    }

    public boolean isException(Exchange returnedExchange) {
        if (hasException(returnedExchange))
            return true;

        if (!isShsMessage(returnedExchange))
            return true;

        return false;
    }

    private boolean isShsMessage(Exchange returnedExchange) {
        return getBody(returnedExchange) instanceof ShsMessage;
    }

    private boolean hasException(Exchange returnedExchange) {
        return returnedExchange.getException() != null;
    }

    private Object getBody(Exchange exchange) {
        if (exchange.hasOut()) {
            return exchange.getOut().getBody();
        } else {
            return exchange.getIn().getBody();
        }
    }
}
