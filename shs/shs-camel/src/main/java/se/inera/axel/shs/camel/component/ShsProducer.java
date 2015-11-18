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
import org.apache.camel.impl.DefaultProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.inera.axel.shs.client.ShsClient;
import se.inera.axel.shs.exception.IllegalMessageStructureException;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ShsHeaders;

/**
 * Camel SHS Message producer.
 */
public class ShsProducer extends DefaultProducer {
    private static final transient Logger log = LoggerFactory.getLogger(ShsProducer.class);

    public ShsProducer(ShsEndpoint endpoint) {
        super(endpoint);
    }

    @Override
    public void process(final Exchange exchange) throws Exception {

        ShsMessage shsMessage = getEndpoint().getShsMessageBinding().toShsMessage(exchange);

        if (shsMessage == null || shsMessage.getLabel() == null) {
            throw new IllegalMessageStructureException("Camel exchange can not be evaluated as an ShsMessage");
        }

        getEndpoint().getShsLabelValidator().validate(shsMessage.getLabel());

        switch (shsMessage.getLabel().getTransferType()) {
            case ASYNCH:
                doAsynchSend(exchange, shsMessage);
                break;
            case SYNCH:
                doSynchSend(exchange, shsMessage);
                break;
            default:
                throw new IllegalMessageStructureException("TransferType must be specified on message");
        }

	}

    private void doAsynchSend(final Exchange exchange, ShsMessage shsMessage) throws Exception {
        ShsClient shsClient = getShsClient();

        String txId = shsClient.send(shsMessage);
        exchange.getIn().setBody(txId);
        exchange.getIn().setHeader(ShsHeaders.X_SHS_TXID, txId);
    }

    private void doSynchSend(final Exchange exchange, ShsMessage shsMessage) throws Exception {
        ShsClient shsClient = getShsClient();

        ShsMessage response = shsClient.request(shsMessage);
        getEndpoint().getShsMessageBinding().fromShsMessage(response, exchange);
    }


    @Override
    public ShsEndpoint getEndpoint() {
        return (ShsEndpoint)super.getEndpoint();
    }

    public ShsClient getShsClient() {
        return getEndpoint().getClient();
    }

}
