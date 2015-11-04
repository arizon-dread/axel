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
package se.inera.axel.shs.camel;

import java.util.HashMap;
import java.util.Map;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;

import se.inera.axel.shs.processor.ShsHeaders;
import se.inera.axel.shs.xml.label.From;
import se.inera.axel.shs.xml.label.Meta;
import se.inera.axel.shs.xml.label.Originator;
import se.inera.axel.shs.xml.label.ShsLabel;

/**
 * Converts an {@link ShsLabel} on the Camel Exchange property named {@value se.inera.axel.shs.processor.ShsHeaders#LABEL}
 * into Camel Headers defined by {@link se.inera.axel.shs.processor.ShsHeaders}.
 * <p>
 * The label object in the camel property is removed.
 */
public class ShsLabelToCamelHeadersProcessor implements Processor {
	ShsLabelBinding labelBinding = new ShsLabelBinding();

	@Override
	public void process(Exchange exchange) throws Exception {
		ShsLabel label = exchange.getProperty(ShsHeaders.LABEL, ShsLabel.class);
		exchange.removeProperty(ShsHeaders.LABEL);
		exchange.getIn().setHeaders(labelBinding.fromLabel(label));
	}


}
