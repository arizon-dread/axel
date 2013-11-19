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

import org.apache.camel.impl.DefaultComponent;
import org.apache.camel.util.EndpointHelper;
import se.inera.axel.shs.client.MessageListConditions;
import se.inera.axel.shs.client.ShsClient;

import java.util.Map;

/**
 * Represents the component that manages {@link ShsEndpoint}.
 */
public class ShsComponent extends DefaultComponent {

    @Override
    protected ShsEndpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
        ShsClient shsClient = EndpointHelper.resolveReferenceParameter(
                getCamelContext(), remaining, ShsClient.class, true);

        setProperties(shsClient, parameters);

        ShsEndpoint endpoint = new ShsEndpoint(uri, this, shsClient);
        setProperties(endpoint, parameters);

        ShsExceptionHandler exceptionHandler =
                getAndRemoveParameter(parameters, "exceptionHandler", ShsExceptionHandler.class);
        if (exceptionHandler == null) {
        	exceptionHandler = new DefaultShsExceptionHandler();
        }
        setProperties(exceptionHandler, parameters);
        
        endpoint.setExceptionHandler(exceptionHandler);

        MessageListConditions conditions =
                resolveAndRemoveReferenceParameter(parameters, "conditions", MessageListConditions.class);
        if (conditions == null) {
            conditions = new MessageListConditions();
        } else {
            conditions = conditions.copy();
        }

        endpoint.setConditions(conditions);
//        Map<String, Object> filterProperties = IntrospectionSupport.extractProperties(parameters, "filter.");
        setProperties(conditions, parameters);


        return endpoint;
    }

}
