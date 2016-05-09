package se.inera.axel.shs.processor;

import se.inera.axel.shs.exception.ShsException;
import se.inera.axel.shs.mime.ShsMessage;
import se.inera.axel.shs.xml.label.ShsLabel;

public interface LabelValidator {
    void validate(ShsMessage message)  throws ShsException;

    void validate(ShsLabel label) throws ShsException;
}
