package se.inera.axel.shs.camel;

import org.apache.camel.Exchange;
import se.inera.axel.shs.mime.DataPart;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.Content;
import se.inera.axel.shs.xml.label.Data;

public abstract class ShsMessageBinding {
    ShsDataPartBinding dataPartBinding = new ShsDataPartBinding();
    ShsLabelBinding labelBinding = new ShsLabelBinding();

    public abstract ShsMessage toShsMessage(Exchange exchange) throws Exception;

    public abstract void fromShsMessage(ShsMessage shsMessage, Exchange exchange) throws Exception;

    protected void updateLabelContent(ShsMessage shsMessage) {
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
