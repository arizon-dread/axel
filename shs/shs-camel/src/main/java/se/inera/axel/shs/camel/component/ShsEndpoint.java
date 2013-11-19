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
import org.apache.camel.util.ObjectHelper;
import se.inera.axel.shs.client.MessageListConditions;
import se.inera.axel.shs.client.ShsClient;

/**
 * Represents an SHS endpoint.
 */
public class ShsEndpoint extends ScheduledPollEndpoint {
	private ShsExceptionHandler exceptionHandler;

    String to;
    String from;
    MessageListConditions conditions;
    ShsClient client;

    public ShsEndpoint(String uri, ShsComponent component, ShsClient client) {
        super(uri, component);
        this.client = client;
    }

    @Override
    public boolean isLenientProperties() {
        return false;
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

        ShsConsumer shsConsumer = new ShsConsumer(this, processor);
        configureConsumer(shsConsumer);

        return shsConsumer;
    }

    public boolean isSingleton() {
        return true;
    }

	public ShsExceptionHandler getExceptionHandler() {
		return exceptionHandler;
	}

	public void setExceptionHandler(ShsExceptionHandler exceptionHandler) {
		this.exceptionHandler = exceptionHandler;
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

    public MessageListConditions getConditions() {
        return conditions;
    }

    public void setConditions(MessageListConditions conditions) {
        this.conditions = conditions;
    }

    public ShsClient getClient() {
        return client;
    }

    public void setClient(ShsClient client) {
        this.client = client;
    }
}
