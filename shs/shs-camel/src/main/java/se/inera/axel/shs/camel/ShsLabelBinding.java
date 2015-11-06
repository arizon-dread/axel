package se.inera.axel.shs.camel;

import org.apache.camel.Message;
import org.apache.camel.impl.DefaultMessage;
import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.xml.label.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShsLabelBinding {

    public ShsLabel toLabel(Message in) throws Exception {

        ShsLabel label = new ShsLabel();

        label.setSubject(in.getHeader(ShsHeaders.SUBJECT, String.class));
        label.setTo(convertStringToTo(in.getHeader(ShsHeaders.TO, String.class)));

        From from = convertStringToFrom(in.getHeader(ShsHeaders.FROM, String.class));
        if (from != null) {
            label.getOriginatorOrFrom().add(from);
        }

        Originator originator = convertStringToOriginator(in.getHeader(ShsHeaders.ORIGINATOR, String.class));
        if (originator != null) {
            label.getOriginatorOrFrom().add(originator);
        }

        EndRecipient endRecipient = convertStringToEndRecipient(in.getHeader(ShsHeaders.ENDRECIPIENT, String.class));
        label.setEndRecipient(endRecipient);
        label.setProduct(convertStringToProduct(in.getHeader(ShsHeaders.PRODUCT_ID, String.class)));
        label.setTxId(in.getHeader(ShsHeaders.TXID, UUID.randomUUID(), String.class));
        label.setCorrId(in.getHeader(ShsHeaders.CORRID, label.getTxId(), String.class));
        label.setDatetime(in.getHeader(ShsHeaders.DATETIME, new Date(), Date.class));

        label.setStatus(in.getHeader(ShsHeaders.STATUS, Status.PRODUCTION, Status.class));

        label.setSequenceType(in.getHeader(ShsHeaders.SEQUENCETYPE, SequenceType.EVENT, SequenceType.class));
        label.setTransferType(in.getHeader(ShsHeaders.TRANSFERTYPE, TransferType.ASYNCH, TransferType.class));

        label.setMessageType(in.getHeader(ShsHeaders.MESSAGETYPE, MessageType.SIMPLE, MessageType.class));

        Content content = new Content();
        content.setContentId(in.getHeader(ShsHeaders.CONTENT_ID, UUID.randomUUID(), String.class));
        content.setComment(in.getHeader(ShsHeaders.CONTENT_COMMENT, String.class));

        label.setContent(content);

        addMetaToLabel(label, in.getHeader(ShsHeaders.META, Map.class));
        return label;
    }


    public Message fromLabel(ShsLabel label) throws Exception {
        Message out = new DefaultMessage();
        Map<String, Object> headers = out.getHeaders();

        From from = null;
        Originator originator = null;
        if (!label.getOriginatorOrFrom().isEmpty()) {
            if (label.getOriginatorOrFrom().get(0) instanceof From)
                from = (From)label.getOriginatorOrFrom().get(0);
            else if (label.getOriginatorOrFrom().get(0) instanceof Originator)
                originator = (Originator)label.getOriginatorOrFrom().get(0);
        }

        if (from != null)
            headers.put(ShsHeaders.FROM, from.getValue());
        if (originator != null)
            headers.put(ShsHeaders.ORIGINATOR, originator.getValue());

        headers.put(ShsHeaders.CORRID, label.getCorrId());
        headers.put(ShsHeaders.CONTENT_ID, label.getContent().getContentId());
        headers.put(ShsHeaders.CONTENT_COMMENT, label.getContent().getComment());
        headers.put(ShsHeaders.TXID, label.getTxId());
        headers.put(ShsHeaders.DATETIME, label.getDatetime());
        if (label.getEndRecipient() != null)
            headers.put(ShsHeaders.ENDRECIPIENT, label.getEndRecipient().getValue());

        headers.put(ShsHeaders.MESSAGETYPE, "" + label.getMessageType());
        if (label.getProduct() != null)
            headers.put(ShsHeaders.PRODUCT_ID, label.getProduct().getValue());
        headers.put(ShsHeaders.SEQUENCETYPE, "" + label.getSequenceType());
        headers.put(ShsHeaders.STATUS, "" + label.getStatus());
        headers.put(ShsHeaders.SUBJECT, label.getSubject());
        headers.put(ShsHeaders.TRANSFERTYPE, "" + label.getTransferType());
        if (label.getTo() != null)
            headers.put(ShsHeaders.TO, label.getTo().getValue());

        Map<String, String> metaMap = createMetaMap(label);

        if (metaMap != null)
            headers.put(ShsHeaders.META, metaMap);

        return out;
    }

    private static Map<String, String> createMetaMap(ShsLabel label) {
        Map<String, String> metaMap = null;

        if (!label.getMeta().isEmpty()) {
            metaMap = new HashMap<String, String>();

            for (Meta meta : label.getMeta()) {
                metaMap.put(meta.getName(), meta.getValue());
            }
        }

        return metaMap;
    }

    private static void addMetaToLabel(ShsLabel label, Map<String, String> metaMap) {
        label.getMeta().clear();

        if (metaMap != null) {
            for (Map.Entry<String, String> metaEntry : metaMap.entrySet()) {
                label.getMeta().add(convertToMeta(metaEntry));
            }
        }
    }

    private static Meta convertToMeta(Map.Entry<String, String> metaEntry) {
        Meta meta = new Meta();
        meta.setName(metaEntry.getKey());
        meta.setValue(metaEntry.getValue());
        return meta;
    }

    private static Product convertStringToProduct(String s) {
        if (s == null)
            return null;
        Product product = new Product();
        product.setValue(s);
        return product;
    }

    private static From convertStringToFrom(String s) {
        if (s == null)
            return null;
        From from = new From();
        from.setValue(s);
        return from;
    }

    private static To convertStringToTo(String s) {
        if (s == null)
            return null;
        To to = new To();
        to.setValue(s);
        return to;
    }

    private static Originator convertStringToOriginator(String s) {
        if (s == null)
            return null;
        Originator originator = new Originator();
        originator.setValue(s);
        return originator;
    }

    private static EndRecipient convertStringToEndRecipient(String s) {
        if (s == null)
            return null;
        EndRecipient endRecipient = new EndRecipient();
        endRecipient.setvalue(s);
        return endRecipient;
    }


}
