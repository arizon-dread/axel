package se.inera.axel.shs.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import se.inera.axel.shs.mime.DataPart;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.ShsLabel;

import java.io.InputStream;


public class SimpleShsMessageBinding extends ShsMessageBinding {

    @Override
    public ShsMessage toShsMessage(Exchange exchange) throws Exception  {

        ShsMessage shsMessage = exchange.getIn().getBody(ShsMessage.class);
        /* if the body already IS a n shs message, do nothing. */
        if (shsMessage != null) {
            return shsMessage;
        } else {
            shsMessage = new ShsMessage();
        }

        /* create label from headers on the message */
        shsMessage.setLabel(labelBinding.toLabel(exchange.getIn().getHeaders()));

        if (shsMessage.getLabel() == null) {
            throw new RuntimeException("Can't assemble shs message, no label found");
        }


        /* create a data part from the body of the camel message */
        DataPart dataPart = exchange.getIn().getBody(DataPart.class);

        /* if the body already IS a data part, do nothing. */
        if (dataPart == null) {
            dataPart = dataPartBinding.toDataPart(
                    exchange.getIn().getMandatoryBody(InputStream.class),
                    exchange.getIn().getHeaders());

            if (dataPart == null) {
                throw new RuntimeException("Can't assemble data part from camel exchange");
            }
        }

        /* this binding only binds the body to the first data part. */
        shsMessage.addDataPart(dataPart);

        updateLabelContent(shsMessage);

        return shsMessage;
    }


    @Override
    public void fromShsMessage(ShsMessage shsMessage, Exchange exchange) throws Exception  {
        if (shsMessage.getDataParts() == null || shsMessage.getDataParts().isEmpty())
            throw new RuntimeException("Shs Message contains no data parts");

        Message in = exchange.getIn();

        /* set headers on the camel message from the label */
        ShsLabel label = shsMessage.getLabel();
        labelBinding.fromLabel(label, in.getHeaders());


        /* this binding only binds the first data part to the camel message */
        DataPart dataPart = shsMessage.getDataParts().get(0);
        in.setBody(dataPartBinding.fromDataPart(dataPart, in.getHeaders()));

    }

}
