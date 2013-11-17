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
import org.apache.camel.impl.DefaultComponent;
import org.apache.camel.impl.DefaultEndpoint;
import org.apache.camel.impl.ScheduledPollEndpoint;

import java.util.Map;

/**
 * Represents an SHS endpoint.
 */
public class ShsEndpoint extends ScheduledPollEndpoint {
	private ShsExceptionHandler exceptionHandler;
	private String destinationUri;
    String remaining;
    Map<String, Object> parameters;

    public ShsEndpoint(String uri, ShsComponent component, String remaining, Map<String, Object> parameters) {
        super(uri, component);
        this.remaining = remaining;
        this.parameters = parameters;

    }

    public Producer createProducer() throws Exception {
        return new ShsProducer(this, remaining);
    }

    public Consumer createConsumer(Processor processor) throws Exception {
        ShsConsumer shsConsumer = new ShsConsumer(this, processor, remaining);
        configureConsumer(shsConsumer);
        shsConsumer.setSchedulerProperties(parameters);

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
}
