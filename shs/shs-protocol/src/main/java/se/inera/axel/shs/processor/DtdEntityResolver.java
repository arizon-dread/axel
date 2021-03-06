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
package se.inera.axel.shs.processor;

import org.apache.commons.io.IOUtils;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class DtdEntityResolver implements EntityResolver {
	private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(DtdEntityResolver.class);
	
	private static final String DTD_LOCATION ="/dtd/";

    static Map<String, String> entities = new ConcurrentHashMap<>();

	public DtdEntityResolver() {
	}

	public InputSource resolveEntity(String publicId, String systemId)
			throws SAXException, IOException {
		
		log.trace("resolveEntity({}, {})", publicId, systemId);
		
        try {          

            String entity = entities.get(systemId);

            if (entity == null) {
                InputStream in = getClass().getResourceAsStream(DTD_LOCATION + getFilename(systemId));

                if (in == null) {
                    return null;
                }

                entity = IOUtils.toString(in);

                entities.put(systemId, entity);
            }

            InputSource is = new InputSource(new StringReader(entity));
            is.setEncoding("ISO-8859-1");

            return is;

        } catch (Exception e) {
        	log.debug("Failed to resolve entity returning null to let the parser open a regular URI connection");
        }
        
        return null;
	}
	
	private String getFilename(String systemId) {
		int index = systemId.lastIndexOf("/");
		
		if (index == -1) {
			return systemId;
		} else {
			return systemId.substring(index +1);
		}
	}
}
