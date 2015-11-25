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
import org.apache.camel.spi.*;
import org.apache.camel.util.ObjectHelper;
import se.inera.axel.shs.client.MessageListConditions;
import se.inera.axel.shs.client.ShsClient;
import se.inera.axel.shs.processor.LabelValidator;
import se.inera.axel.shs.processor.SimpleLabelValidator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

/**
 * Represents an SHS endpoint.
 */
@UriEndpoint(scheme = "shs", syntax = "shs:command", title = "SHS", consumerClass = ShsPollingConsumer.class )
public class ShsEndpoint extends ScheduledPollEndpoint {

    @UriPath(enums = "send,request,fetch") @Metadata(required = "true")
    String command;

    @UriParam
    String producttype;

    @UriParam(name = "status", enums = "test,production", defaultValue = "production")
    String labelStatus;

    @UriParam
    String originator;

    @UriParam
    String endrecipient;

    @UriParam(label = "consumer")
    Integer maxhits;

    @UriParam
    ShsClient client;

    @UriParam()
    ShsMessageBinding shsMessageBinding = new DefaultShsMessageBinding();

    @UriParam(label = "producer", description = "Shs address (org nr) of receiver")
    String to;

    @UriParam(label = "producer")
    LabelValidator shsLabelValidator = new SimpleLabelValidator();

    @UriParam
    ExceptionHandler exceptionHandler = null;

    public ShsEndpoint(String uri, ShsComponent component, Map<String, Object> parameters)
            throws Exception
    {
        super(uri, component);

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
        ObjectHelper.notEmpty(getCommand(), "command");
        ObjectHelper.notNull(getExceptionHandler(), "exceptionHandler");

        String[] validCommands = { "send", "request" };
        if (!ObjectHelper.contains(validCommands, getCommand()))
            throw new IllegalArgumentException("Unknown command:" + getCommand());

        return new ShsProducer(this);
    }

    public Consumer createConsumer(Processor processor) throws Exception {

        ObjectHelper.notNull(getClient(), "client");
        ObjectHelper.notEmpty(getCommand(), "command");

        ObjectHelper.notNull(getExceptionHandler(), "exceptionHandler");

        String[] validCommands = { "fetch" };
        if (!ObjectHelper.contains(validCommands, getCommand()))
            throw new IllegalArgumentException("Unknown command:" + getCommand());

        /* configure message list criterias */
        MessageListConditions conditions = new MessageListConditions();
        conditions.setEndrecipient(getEndrecipient());
        conditions.setOriginator(getOriginator());
        conditions.setStatus(getLabelStatus());
        conditions.setMaxhits(getMaxhits());
        conditions.setProducttype(getProducttype());

        getComponent().setProperties(conditions, getConsumerProperties());

        ShsPollingConsumer shsPollingConsumer = new ShsPollingConsumer(this, processor);
        configureConsumer(shsPollingConsumer);
        shsPollingConsumer.setExceptionHandler(getExceptionHandler());
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

    public String getLabelStatus() {
        return labelStatus;
    }

    public void setLabelStatus(String labelStatus) {
        this.labelStatus = labelStatus;
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

    public ExceptionHandler getExceptionHandler() {
        return exceptionHandler;
    }

    public void setExceptionHandler(ExceptionHandler exceptionHandler) {
        this.exceptionHandler = exceptionHandler;
    }

    public LabelValidator getShsLabelValidator() {
        return shsLabelValidator;
    }

    public void setShsLabelValidator(LabelValidator shsLabelValidator) {
        this.shsLabelValidator = shsLabelValidator;
    }
}
