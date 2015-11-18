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
import org.apache.camel.spi.UriEndpoint;
import org.apache.camel.spi.UriParam;
import org.apache.camel.util.ObjectHelper;
import se.inera.axel.shs.client.MessageListConditions;
import se.inera.axel.shs.client.ShsClient;
import se.inera.axel.shs.processor.LabelValidator;
import se.inera.axel.shs.processor.SimpleLabelValidator;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an SHS endpoint.
 */
@UriEndpoint(scheme = "shs", syntax = "shs:command:producttype", title = "SHS", consumerClass = ShsPollingConsumer.class )
public class ShsEndpoint extends ScheduledPollEndpoint {

    @UriParam(enums = "send,request,fetch")
    String command;

    @UriParam
    String producttype;

    @UriParam(enums = "test,production", defaultValue = "production")
    String status;

    @UriParam
    String originator;

    @UriParam
    String endrecipient;

    @UriParam(label = "consumer")
    Integer maxhits;

    @Resource(type = ShsClient.class)
    @UriParam
    ShsClient client;

    @UriParam()
    ShsMessageBinding shsMessageBinding = new DefaultShsMessageBinding();

    @UriParam(label = "producer", description = "Shs address (org nr) of receiver")
    String to;

    @UriParam(label = "producer")
    LabelValidator shsLabelValidator = new SimpleLabelValidator();

    Map<String, Object> parameters;

    public ShsEndpoint(String uri, ShsComponent component, Map<String, Object> parameters)
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

        return new ShsProducer(this);
    }

    public Consumer createConsumer(Processor processor) throws Exception {

        ObjectHelper.notNull(getClient(), "client");
        ObjectHelper.notEmpty(getCommand(), "command");

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
        MessageListConditions conditions = new MessageListConditions();
        conditions.setEndrecipient(endrecipient);
        conditions.setOriginator(originator);
        conditions.setStatus(status);
        conditions.setMaxhits(maxhits);
        conditions.setProducttype(producttype);

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

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public String getProducttype() {
        return producttype;
    }

    public void setProducttype(String producttype) {
        this.producttype = producttype;
    }

    public String getOriginator() {
        return originator;
    }

    public void setOriginator(String originator) {
        this.originator = originator;
    }

    public String getEndrecipient() {
        return endrecipient;
    }

    public void setEndrecipient(String endrecipient) {
        this.endrecipient = endrecipient;
    }

    public Integer getMaxhits() {
        return maxhits;
    }

    public void setMaxhits(Integer maxhits) {
        this.maxhits = maxhits;
    }

    public ShsClient getClient() {
        return client;
    }

    public void setClient(ShsClient client) {
        this.client = client;
    }

    public ShsMessageBinding getShsMessageBinding() {
        return shsMessageBinding;
    }

    public void setShsMessageBinding(ShsMessageBinding shsMessageBinding) {
        this.shsMessageBinding = shsMessageBinding;
    }

    public LabelValidator getShsLabelValidator() {
        return shsLabelValidator;
    }

    public void setShsLabelValidator(LabelValidator shsLabelValidator) {
        this.shsLabelValidator = shsLabelValidator;
    }
}
