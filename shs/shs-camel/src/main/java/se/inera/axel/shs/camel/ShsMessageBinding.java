package se.inera.axel.shs.camel;

import org.apache.camel.Exchange;
import org.apache.camel.Message;
import se.inera.axel.shs.exception.IllegalDatapartContentException;
import se.inera.axel.shs.exception.IllegalMessageStructureException;
import se.inera.axel.shs.mime.DataPart;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.Content;
import se.inera.axel.shs.xml.label.Data;
import se.inera.axel.shs.xml.label.ShsLabel;

import java.util.ArrayList;
import java.util.List;

public abstract class ShsMessageBinding {
    ShsDataPartBinding dataPartBinding = new ShsDataPartBinding();
    ShsLabelBinding labelBinding = new ShsLabelBinding();


    /**
     * Creates an shs message from the camel exchange using {@link ShsLabelBinding#toLabel(Message)} and
     * {@link #extractDataParts(org.apache.camel.Exchange)}.
     * <p/>
     *
     * To make a custom shs message binding, consider overriding {@link #extractDataParts(org.apache.camel.Exchange)
     *
     * @param exchange
     * @return
     * @throws Exception
     */
    public ShsMessage toShsMessage(Exchange exchange) throws Exception  {


        ShsMessage shsMessage = null;
        Object body = exchange.getIn().getBody();
        /* if the body already IS a n shs message, do nothing. */
        if (body instanceof ShsMessage) {
            return (ShsMessage)body;
        } else {
            shsMessage = new ShsMessage();
        }

        shsMessage.setLabel(labelBinding.toLabel(exchange.getIn()));

        shsMessage.setDataParts(extractDataParts(exchange));

        updateLabelContent(shsMessage);

        return shsMessage;
    }


    /**
     * Convert an shs message to a camel exchange. <p/>
     * If the shs message contains one data part, it is converted with {@link ShsDataPartBinding#fromDataPart(DataPart)},
     * otherwise the body is set to the list of data parts contained in the shs message.
     * @param shsMessage
     * @param exchange
     * @throws Exception
     */
    public void fromShsMessage(ShsMessage shsMessage, Exchange exchange) throws Exception  {
        if (shsMessage.getDataParts() == null || shsMessage.getDataParts().isEmpty())
            throw new IllegalMessageStructureException("Message contains no data parts");

        Message in = exchange.getIn();

        Message outLabel = labelBinding.fromLabel(shsMessage.getLabel());
        in.getHeaders().putAll(outLabel.getHeaders());

        if (shsMessage.getDataParts().size() == 1) {
            DataPart dataPart = shsMessage.getDataParts().get(0);
            Message outDp = dataPartBinding.fromDataPart(dataPart);
            in.getHeaders().putAll(outDp.getHeaders());
            in.setBody(outDp.getBody());
        } else {
            in.setBody(shsMessage.getDataParts());
            // TODO convert every data part into a separate exchange and put the list of exchanges on the body?
            // See GroupedExchanges in http://camel.apache.org/aggregator2.html
        }

    }



    /**
     * If the camel message body already is a data part or a list of data parts, use them, otherwise try convertering
     * the exchange to a data part with . <p/>
     * TODO use product type file to automatically validate, convert and package the data parts.
     *
     * @param exchange
     * @return
     */
    protected List<DataPart> extractDataParts(Exchange exchange) throws Exception {
        List<DataPart> dataParts = new ArrayList();

        Object body = exchange.getIn().getBody();

        if (body instanceof List) {

            for (Object bodyItem : (List)body) {
                if (bodyItem instanceof DataPart) {
                    dataParts.add((DataPart)bodyItem);
                } else if (bodyItem instanceof Exchange) {

                } else {
                    throw new IllegalDatapartContentException("Unsupported list content: " + bodyItem);
                }
            }

        } else if (body instanceof DataPart) {
            dataParts.add((DataPart)body);
        } else {
            dataParts.add(dataPartBinding.toDataPart(exchange.getIn()));
        }

        return dataParts;
    }

    /**
     * Updates the "content"-tag of the label, based on the contents of the shs message.
     *
     * @param shsMessage
     */
    protected void updateLabelContent(ShsMessage shsMessage) {
        if (shsMessage.getDataParts() == null || shsMessage.getDataParts().isEmpty()) {
            throw new IllegalMessageStructureException("No data parts found on message");
        }

        if (shsMessage.getLabel() == null) {
            throw new IllegalMessageStructureException("No label found on message");
        }


        Content content = shsMessage.getLabel().getContent();
        content.getDataOrCompound().clear();
        for (DataPart dp : shsMessage.getDataParts()) {
            Data data = new Data();
            data.setDatapartType(dp.getDataPartType());
            data.setFilename(dp.getFileName());
            if (dp.getContentLength() > 0)
                data.setNoOfBytes("" + dp.getContentLength());
            content.getDataOrCompound().add(data);
        }
    }
}
