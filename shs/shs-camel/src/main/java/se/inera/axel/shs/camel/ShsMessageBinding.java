package se.inera.axel.shs.camel;

import org.apache.camel.Exchange;
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
     * Creates an shs message from the camel exchange using {@link #extractLabel(org.apache.camel.Exchange)} and
     * {@link #extractDataParts(org.apache.camel.Exchange)} followed by {@link #updateLabelContent(se.inera.axel.shs.mime.ShsMessage)}.
     * <p/>
     *
     * To make a custom shs message binding, consider overriding {@link #extractDataParts(org.apache.camel.Exchange) first,
     * before overriding this method.
     *
     * @param exchange
     * @return
     * @throws Exception
     */
    public ShsMessage toShsMessage(Exchange exchange) throws Exception  {

        ShsMessage shsMessage = exchange.getIn().getBody(ShsMessage.class);
        /* if the body already IS a n shs message, do nothing. */
        if (shsMessage != null) {
            return shsMessage;
        } else {
            shsMessage = new ShsMessage();
        }

        shsMessage.setLabel(extractLabel(exchange));

        shsMessage.setDataParts(extractDataParts(exchange));

        updateLabelContent(shsMessage);

        return shsMessage;
    }


    public abstract void fromShsMessage(ShsMessage shsMessage, Exchange exchange) throws Exception;


    /**
     * Create label from headers on the message
     *
     * @param exchange
     * @return
     * @throws Exception
     */
    protected ShsLabel extractLabel(Exchange exchange) throws Exception {

        ShsLabel label = labelBinding.toLabel(exchange.getIn().getHeaders());

        if (label == null) {
            throw new RuntimeException("Can't assemble shs message, no label found");
        }

        return label;
    }

    /**
     * Assumes the camel message body already is a data part or a list of data parts. <p/>
     *
     * @param exchange
     * @return
     */
    protected List<DataPart> extractDataParts(Exchange exchange) throws Exception {
        /*  */
        List<DataPart> dataParts = new ArrayList();

        List<Object> bodyList = exchange.getIn().getBody(List.class);
        if (bodyList != null && !bodyList.isEmpty()) {
            for (Object bodyItem : bodyList) {
                if (bodyItem instanceof DataPart) {
                    dataParts.add((DataPart)bodyItem);
                }
            }
            /* if all elements are not data parts, clear and let subclass decide */
            if (dataParts.size() != bodyList.size()) {
                dataParts.clear();
            }
        } else {
            DataPart dataPart = exchange.getIn().getBody(DataPart.class);
            if (dataPart != null) {
                dataParts.add(dataPart);
            }
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
