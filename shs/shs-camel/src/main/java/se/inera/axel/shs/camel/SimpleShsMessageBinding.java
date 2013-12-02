package se.inera.axel.shs.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import se.inera.axel.shs.exception.IllegalMessageStructureException;
import se.inera.axel.shs.mime.DataPart;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.ShsLabel;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class SimpleShsMessageBinding extends ShsMessageBinding {


    /**
     * Converts the camel message body to a data part using the {@link ShsDataPartBinding} of the super class.
     *
     * @param exchange
     * @return
     * @throws Exception
     */
    @Override
    protected List<DataPart> extractDataParts(Exchange exchange) throws Exception {
        List<DataPart> dataParts = super.extractDataParts(exchange);

        if (!dataParts.isEmpty()) {
            return dataParts;
        }

        dataParts = new ArrayList();
        DataPart dataPart = dataPartBinding.toDataPart(
                exchange.getIn().getMandatoryBody(InputStream.class),
                exchange.getIn().getHeaders());

        if (dataPart == null) {
            throw new RuntimeException("Can't assemble data part from camel exchange");
        } else {
            dataParts.add(dataPart);
        }

        return dataParts;
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
