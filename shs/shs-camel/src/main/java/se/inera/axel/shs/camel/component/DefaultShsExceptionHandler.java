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
import org.apache.camel.spi.ExceptionHandler;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.axel.shs.client.ShsClient;
import se.inera.axel.shs.exception.MissingDeliveryExecutionException;
import se.inera.axel.shs.exception.OtherErrorException;
import se.inera.axel.shs.exception.ShsException;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ResponseMessageBuilder;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.xml.label.ShsLabel;

import java.io.IOException;

/**
 * This exception handler converts an exception (on an exchange) to an {@link ShsException} and sends it
 * back to the sender of the original message using the provided {@link ShsClient}.
 *
 * @author Björn Bength
 */
public class DefaultShsExceptionHandler implements ExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(DefaultShsExceptionHandler.class);

    ResponseMessageBuilder responseMessageBuilder = new ResponseMessageBuilder();
    ShsClient client;

    public DefaultShsExceptionHandler(ShsClient client) {
        this.client = client;
    }

    @Override
    public void handleException(Throwable exception) {
        handleException("Exception occurred during process of message", exception);
    }

    @Override
    public void handleException(String message, Throwable exception) {
        log.error(message, exception);
    }

    @Override
    public void handleException(String message, Exchange exchange, Throwable exception) {

        log.error(message, exception);

        ShsLabel label = exchange.getProperty(ShsHeaders.LABEL, ShsLabel.class);
        if (label == null) {
            log.warn("Original SHS Label was not found on camel exchange, cannot continue");
            return;
        }

        log.debug("Creating an shs error message from original message with corrId=" + label.getCorrId() +
                " to send back to the original sender (" + label.getFrom().getValue() + ")");

        ShsException shsException = createOrEnrichShsException(exchange, label);

        ShsMessage errorMessage = responseMessageBuilder.buildErrorMessage(label, shsException);
        try {
            client.send(errorMessage);
        } catch (Exception e) {
            ShsLabel errorLabel = errorMessage.getLabel();

            log.error("Error sending shs error message with txId=" + errorLabel.getTxId() +
                    " back to the original sender (" + errorLabel.getTo().getValue() + ")" +
                    " regarding message with corrId=" + label.getCorrId(), e);
        }
    }

    private ShsException createOrEnrichShsException(Exchange exchange, ShsLabel label) {

        ShsException shsException = exchange.getException(ShsException.class);

        if (shsException == null) {
            IOException ioException = exchange.getException(IOException.class);
            if (ioException != null) {
                shsException = new MissingDeliveryExecutionException(ioException);
            }
        }

        if (shsException == null) {
            Exception exception = exchange.getException(Exception.class);
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

}
