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
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.ScheduledBatchPollingConsumer;
import se.inera.axel.shs.client.DefaultShsClient;
import se.inera.axel.shs.client.MessageListConditions;
import se.inera.axel.shs.client.ShsClient;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.xml.message.Message;
import se.inera.axel.shs.xml.message.ShsMessageList;

import java.util.LinkedList;
import java.util.Queue;

/**
 * A polling Camel SHS Message consumer that polls an SHS server for new (asynchronous) messages.
 * <p/>
 *
 * This consumer executes this loop:
 * <ol>
 *     <li>{@link DefaultShsClient#list(se.inera.axel.shs.client.MessageListConditions)} with the given criteria (called conditions) to get a list of messages.</li>
 *     <li>For each message do:
 *          <ol>
 *              <li>{@link DefaultShsClient#fetch(String)} a message based on txId</li>
 *              <li>Convert the shs message using a "binding" to a normalized camel message</li>
 *              <li>Send it out in the camel pipeline using a newly created camel exchange.</li>
 *              <li>When exchange completes:
 *                  <ul>
 *                      <li>without errors: Ack the message using {@link DefaultShsClient#ack(String)}</li>
 *                      <li>with errors: Create an shs error message and send it back to the server using {@link DefaultShsClient#send(se.inera.axel.shs.mime.ShsMessage)} </li>
 *                  </ul>
 *               </li>
 *          </ol>
 *     </li>
 * </ol>
 *
 *
 *
 *
 */
public class ShsPollingConsumer extends ScheduledBatchPollingConsumer {
    MessageListConditions conditions;


    public ShsPollingConsumer(ShsEndpoint endpoint, Processor processor) {
        super(endpoint, processor);
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

        /* which shs message we deal with is specified in an exchange property */
        final Message message = exchange.getProperty(Message.class.getCanonicalName(), Message.class);
        if (message == null) {
            log.warn("no shs message registered on exchange property '{}' for fetching and processing",
                    Message.class.getCanonicalName());
            return false;
        }

        /* fetch the message from the server given the txId */

        log.trace("scheduling shs message {} for fetching and processing", message.getTxId());

        ShsMessage shsMessage;
        try {
            shsMessage = getShsClient().fetch(message.getTxId());
        } catch (Exception e) {
            log.error("error fetching message from shs server", e);
            return false;
        }


        /* convert the shs message to a camel normalized message using some 'binding' converter */
        try {
            // binding to convert from shs message to camel exchange.
            getEndpoint().getShsMessageBinding().fromShsMessage(shsMessage, exchange);
            exchange.setProperty(ShsHeaders.LABEL, shsMessage.getLabel());
        } catch (Exception e) {
            log.error("error converting shs message '" + message.getTxId() + "' to camel message", e);
            return false;
        }


        /* send the normalized message to the next processor in the route */
        getAsyncProcessor().process(exchange, new AsyncCallback() {
            @Override
            public void done(boolean doneSync) {

                if (exchange.getException() != null) {
                    getExceptionHandler().handleException("Error processing exchange", exchange, exchange.getException());
                } else {
                    try {
                        getShsClient().ack(message.getTxId());
                    } catch (Exception e) {
                        log.error("error acking shs message " + message.getTxId() + " with server,"
                                + " although message is fetched and processed without errors", e);
                    }
                }
            }
        });

        /* return true if done sync or false if async, right now something in between. */
        return false;
    }

    @Override
    protected int poll() throws Exception {

        ShsMessageList queryResult;
        LinkedList exchanges = new LinkedList();

        queryResult = getShsClient().list(getConditions());

        if (queryResult == null || queryResult.getMessage() == null) {
            log.warn("faulty (empty) response polling shs server");
            return 0;
        }

        for (Message message : queryResult.getMessage()) {
            Exchange exchange = getEndpoint().createExchange();
            exchange.setProperty(Message.class.getCanonicalName(), message);
            exchanges.add(exchange);
        }

        int total = exchanges.size();

        log.debug("Total {} messages to consume from {}", total, getEndpoint().getEndpointUri());

        if (total == 0) {
            return 0;
        }

        return processBatch(exchanges);
    }


    @Override
    public ShsEndpoint getEndpoint() {
        return (ShsEndpoint)super.getEndpoint();
    }

    public ShsClient getShsClient() {
        return getEndpoint().getClient();
    }


    public MessageListConditions getConditions() {
        return conditions;
    }

    public void setConditions(MessageListConditions conditions) {
        this.conditions = conditions;
    }

}
