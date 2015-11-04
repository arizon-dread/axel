package se.inera.axel.shs.camel;

import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.xml.label.*;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ShsLabelBinding {
    public ShsLabel toLabel(Map<String, Object> headers) throws Exception {

        ShsLabel label = new ShsLabel();

        label.setSubject((String)headers.get(ShsHeaders.SUBJECT));
        label.setTo(convertStringToTo((String)headers.get(ShsHeaders.TO)));

        From from = convertStringToFrom((String)headers.get(ShsHeaders.FROM));
        if (from != null) {
            label.getOriginatorOrFrom().add(from);
        }

        Originator originator = convertStringToOriginator((String)headers.get(ShsHeaders.ORIGINATOR));
        if (originator != null) {
            label.getOriginatorOrFrom().add(originator);
        }

        EndRecipient endRecipient = convertStringToEndRecipient((String)headers.get(ShsHeaders.ENDRECIPIENT));
        label.setEndRecipient(endRecipient);
        label.setProduct(convertStringToProduct((String)headers.get(ShsHeaders.PRODUCT_ID)));
        label.setTxId((String)headers.get(ShsHeaders.TXID));
        if (label.getTxId() == null) {
            label.setTxId(UUID.randomUUID().toString());
        }

        label.setCorrId((String)headers.get(ShsHeaders.CORRID));
        if (label.getCorrId() == null) {
            label.setCorrId(label.getTxId());
        }
        label.setDatetime((Date)headers.get(ShsHeaders.DATETIME));
        if (label.getDatetime() == null) {
            label.setDatetime( new Date());
        }

        label.setStatus(convertToStatus(headers.get(ShsHeaders.STATUS)));
        if (label.getStatus() == null) {
            label.setStatus(Status.PRODUCTION);
        }

        label.setTransferType(convertToTransferType(headers.get(ShsHeaders.TRANSFERTYPE)));
        if (label.getTransferType() == null) {
            label.setTransferType(TransferType.ASYNCH);
        }


        label.setSequenceType(convertToSequenceType(headers.get(ShsHeaders.SEQUENCETYPE)));
        if (label.getSequenceType() == null) {
            if (label.getTransferType() == TransferType.ASYNCH) {
                label.setSequenceType(SequenceType.EVENT);
            } else {
                label.setSequenceType(SequenceType.REQUEST);
            }
        }


        label.setMessageType(convertToMessageType(headers.get(ShsHeaders.MESSAGETYPE)));
        if (label.getMessageType() == null) {
            label.setMessageType(MessageType.SIMPLE);
        }

        Content content = new Content();
        content.setContentId((String)headers.get(ShsHeaders.CONTENT_ID));
        if (content.getContentId() == null) {
            content.setContentId(UUID.randomUUID().toString());
        }
        content.setComment((String)headers.get(ShsHeaders.CONTENT_COMMENT));

        label.setContent(content);

        addMetaToLabel(label, (Map)headers.get(ShsHeaders.META));

        return label;
        //       in.removeHeaders("ShsLabel*");

    }


    public void fromLabel(ShsLabel label, Map<String, Object> headers) throws Exception {

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

        //exchange.removeProperty(ShsHeaders.LABEL);


    }


    private static Status convertToStatus(Object status) {
        if (status == null)
            return null;
        else if (status instanceof String)
            return Status.valueOf(((String) status).toUpperCase());
        else if (status instanceof Status)
            return (Status)status;
        else throw new IllegalArgumentException("Cannot convert status: " + status);
    }

    private static TransferType convertToTransferType(Object transferType) {
        if (transferType == null)
            return null;
        else if (transferType instanceof String)
            return TransferType.valueOf(((String) transferType).toUpperCase());
        else if (transferType instanceof TransferType)
            return (TransferType) transferType;
        else throw new IllegalArgumentException("Cannot convert transferType: " + transferType);
    }

    private static SequenceType convertToSequenceType(Object sequenceType) {
        if (sequenceType == null)
            return null;
        else if (sequenceType instanceof String)
            return SequenceType.valueOf(((String) sequenceType).toUpperCase());
        else if (sequenceType instanceof SequenceType)
            return (SequenceType) sequenceType;
        else throw new IllegalArgumentException("Cannot convert sequenceType: " + sequenceType);
    }

    private static MessageType convertToMessageType(Object messageType) {
        if (messageType == null)
            return null;
        else if (messageType instanceof String)
            return MessageType.valueOf(((String) messageType).toUpperCase());
        else if (messageType instanceof MessageType)
            return (MessageType) messageType;
        else throw new IllegalArgumentException("Cannot convert messageType: " + messageType);
    }

    private Map<String, String> createMetaMap(ShsLabel label) {
        Map<String, String> metaMap = null;

        if (!label.getMeta().isEmpty()) {
            metaMap = new HashMap<String, String>();

            for (Meta meta : label.getMeta()) {
                metaMap.put(meta.getName(), meta.getValue());
            }
        }

        return metaMap;
    }

    private void addMetaToLabel(ShsLabel label, Map<String, String> metaMap) {
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
