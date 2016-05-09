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
import se.inera.axel.shs.xml.label.SequenceType;
import se.inera.axel.shs.xml.label.TransferType;

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

        if (getEndpoint().getTo() != null) {
            exchange.getIn().setHeader(ShsHeaders.TO, getEndpoint().getTo());
        }
        if (getEndpoint().getOriginator() != null) {
            exchange.getIn().setHeader(ShsHeaders.ORIGINATOR, getEndpoint().getOriginator());
        }

        if (getEndpoint().getEndrecipient() != null) {
            exchange.getIn().setHeader(ShsHeaders.ENDRECIPIENT, getEndpoint().getEndrecipient());
        }

        if (getEndpoint().getProducttype() != null) {
            exchange.getIn().setHeader(ShsHeaders.PRODUCT_ID, getEndpoint().getProducttype());
        }

        if (getEndpoint().getClient() != null) {
            exchange.getIn().setHeader(ShsHeaders.FROM, getEndpoint().getClient().getShsAddress());
        }

        ShsMessage shsMessage = getEndpoint().getShsMessageBinding().toShsMessage(exchange);
        getEndpoint().getShsLabelValidator().validate(shsMessage);

        if ("send".equals(getEndpoint().getCommand())) {
            doAsyncSend(exchange, shsMessage);
        } else if ("request".equals(getEndpoint().getCommand())) {
            doSyncSend(exchange, shsMessage);
        } else {
            throw new IllegalMessageStructureException("Unknown command: " + getEndpoint().getCommand());
        }

	}

    private void doAsyncSend(final Exchange exchange, ShsMessage shsMessage) throws Exception {
        ShsClient shsClient = getShsClient();
        shsMessage.getLabel().setTransferType(TransferType.ASYNCH);

        String txId = shsClient.send(shsMessage);
        exchange.getIn().setBody(txId);
        exchange.getIn().setHeader(ShsHeaders.X_SHS_TXID, txId);
    }

    private void doSyncSend(final Exchange exchange, ShsMessage shsMessage) throws Exception {
        ShsClient shsClient = getShsClient();
        shsMessage.getLabel().setTransferType(TransferType.SYNCH);
        SequenceType sequenceType = exchange.getIn().getHeader(ShsHeaders.SEQUENCETYPE, SequenceType.REQUEST, SequenceType.class);
        shsMessage.getLabel().setSequenceType(sequenceType);

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
