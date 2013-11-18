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

import org.apache.camel.AsyncCallback;
import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultComponent;
import org.apache.camel.impl.ScheduledBatchPollingConsumer;
import se.inera.axel.shs.camel.DefaultShsMessageToCamelProcessor;
import se.inera.axel.shs.client.ShsClient;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.message.Message;
import se.inera.axel.shs.xml.message.ShsMessageList;

import java.util.LinkedList;
import java.util.Map;
import java.util.Queue;

/**
 * Camel SHS Message producer.
 */
public class ShsConsumer extends ScheduledBatchPollingConsumer {


    public ShsConsumer(ShsEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
    }

    @Override
    public ShsEndpoint getEndpoint() {
        return (ShsEndpoint)super.getEndpoint();
    }

    @Override
    public int processBatch(Queue<Object> exchanges) throws Exception {

        int total = exchanges.size();
        int answer = total;

        // limit if needed
        if (maxMessagesPerPoll > 0 && total > maxMessagesPerPoll) {
            log.debug("Limiting to maximum messages to poll {} as there was {} messages in this poll.", maxMessagesPerPoll, total);
            total = maxMessagesPerPoll;
        }

        for (int index = 0; index < total && isBatchAllowed(); index++) {
            // only loop if we are started (allowed to run)
            // use poll to remove the head so it does not consume memory even after we have processed it

            Exchange exchange = (Exchange)exchanges.poll();

            // add current index and total as properties
            exchange.setProperty(Exchange.BATCH_INDEX, index);
            exchange.setProperty(Exchange.BATCH_SIZE, total);
            exchange.setProperty(Exchange.BATCH_COMPLETE, index == total - 1);

            // update pending number of exchanges
            pendingExchanges = total - index - 1;

            // process the current exchange
            boolean started = processExchange(exchange);

            // if we did not start process the file then decrement the counter
            if (!started) {
                answer--;
            }
        }

        return answer;
    }

    protected boolean processExchange(final Exchange exchange) throws Exception {

        // TODO add task executor

        final Message message = exchange.getProperty(Message.class.getCanonicalName(), Message.class);


        try {
            ShsMessage shsMessage = getShsClient().fetch(getEndpoint().getTo(), message.getTxId());
            exchange.getIn().setBody(shsMessage);

            // binding to convert from shs message to camel exchange.
            new DefaultShsMessageToCamelProcessor().process(exchange);

        } catch (Exception e) {
            // TODO log exception
            return false;
        }

        // send message to next processor in the route
        getAsyncProcessor().process(exchange, new AsyncCallback() {
            @Override
            public void done(boolean doneSync) {

                // TODO add shs error message creation
                if (exchange.getException() != null) {
                    getExceptionHandler().handleException("Error processing exchange", exchange, exchange.getException());
                } else {
                    try {
                        getShsClient().ack(getEndpoint().getTo(), message.getTxId());
                    } catch (Exception e) {
                        // TODO add logging and handling
                        e.printStackTrace();
                    }
                }

            }
        });

        return true;
    }

    @Override
    protected int poll() throws Exception {

        ShsMessageList queryResult;

        try {
            queryResult = getShsClient().list(getEndpoint().getTo(), getEndpoint().getFilter());
            LinkedList exchanges = new LinkedList();

            for (Message message : queryResult.getMessage()) {
                Exchange exchange = getEndpoint().createExchange();
                exchange.setProperty(Message.class.getCanonicalName(), message);
                exchanges.add(exchange);
            }

            // consume files one by one
            int total = exchanges.size();
            if (total > 0) {
                log.debug("Total {} messages to consume", total);
            }

            return processBatch(exchanges);
        } finally {
            // log exception if an exception occurred and was not handled
        }
    }


    public ShsClient getShsClient() {
        return getEndpoint().getClient();
    }

}
