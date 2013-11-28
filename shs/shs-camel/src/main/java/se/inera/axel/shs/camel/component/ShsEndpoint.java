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

import org.apache.camel.Consumer;
import org.apache.camel.Processor;
import org.apache.camel.Producer;
import org.apache.camel.impl.ScheduledPollEndpoint;
import org.apache.camel.spi.ExceptionHandler;
import org.apache.camel.util.ObjectHelper;
import se.inera.axel.shs.client.MessageListConditions;
import se.inera.axel.shs.client.ShsClient;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents an SHS endpoint.
 */
public class ShsEndpoint extends ScheduledPollEndpoint {

    String to;
    String from;
    ShsClient client;
    Map<String, Object> parameters;

    public ShsEndpoint(String uri, ShsComponent component, ShsClient client, Map<String, Object> parameters)
            throws Exception
    {
        super(uri, component);
        this.client = client;
        this.parameters = parameters;

        component.setProperties(this, parameters);


    }

    @Override
    public boolean isLenientProperties() {
        return true;
    }

    @Override
    public ShsComponent getComponent() {
        return (ShsComponent)super.getComponent();
    }


    public Producer createProducer() throws Exception {

        ObjectHelper.notNull(getClient(), "client");
        ObjectHelper.notEmpty(getClient().getRsUrl(), "rsUrl");

        return new ShsProducer(this);
    }

    public Consumer createConsumer(Processor processor) throws Exception {

        ObjectHelper.notNull(getClient(), "client");
        ObjectHelper.notEmpty(getClient().getDsUrl(), "'dsUrl'");
        ObjectHelper.notEmpty(getTo(), "'to'");

        /* first copy parameter map since we don't want to consume it for each consumer */
        Map<String, Object> parameters = new HashMap();
        parameters.putAll(this.parameters);


        /* configure exception handler */
        ExceptionHandler exceptionHandler =
                getComponent().getAndRemoveParameter(parameters, "exceptionHandler", ExceptionHandler.class);

        if (exceptionHandler == null) {
            exceptionHandler = new DefaultShsExceptionHandler(getClient());
        }
        getComponent().setProperties(exceptionHandler, parameters);


        /* configure message list criterias */
        MessageListConditions conditions =
                getComponent().resolveAndRemoveReferenceParameter(parameters, "conditions", MessageListConditions.class);
        if (conditions == null) {
            conditions = new MessageListConditions();
        } else {
            conditions = conditions.copy();
        }

        getComponent().setProperties(conditions, parameters);


        ShsPollingConsumer shsPollingConsumer = new ShsPollingConsumer(this, processor);
        configureConsumer(shsPollingConsumer);
        shsPollingConsumer.setExceptionHandler(exceptionHandler);
        shsPollingConsumer.setConditions(conditions);

        return shsPollingConsumer;
    }

    public boolean isSingleton() {
        return true;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public ShsClient getClient() {
        return client;
    }

    public void setClient(ShsClient client) {
        this.client = client;
    }
}
