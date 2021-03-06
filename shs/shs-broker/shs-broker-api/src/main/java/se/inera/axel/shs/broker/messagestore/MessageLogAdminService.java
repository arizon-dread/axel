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
package se.inera.axel.shs.broker.messagestore;

import java.io.Serializable;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;

import se.inera.axel.shs.xml.label.ShsLabel;

@Path("/")
public interface MessageLogAdminService {
    @GET
    @Path("/entry/{txId}")
    /**
     * Finds ShsMessageEntry with txId and returns ShsLabel.
     * 
     * @param txId
     * @return
     */
    ShsLabel findEntryById(@PathParam("txId") String txId);

    @DELETE
    @Path("/entry/{txId}")
    /**
     * Deletes ShsMessageEntry with txId.
     *  
     * @param txId
     */
    public void deleteEntry(@PathParam("txId") String txId);

    @GET
    @Path("/message/{txId}")
    /**
     * Finds ShsMessage with txId. 
     * 
     * @param txId
     * @return
     */
    ShsLabel findFileById(@PathParam("txId") String txId);

    @DELETE
    @Path("/message/{txId}")
    /**
     * Deletes ShsMessage with txId.
     * 
     * @param txId
     */
    public void deleteFile(@PathParam("txId") String txId);


    /**
	 * Finds all message entries that have the same correlation id as the given entry.
	 * The resulting list does not contain the given entry.
	 *
	 * @param entry
	 * 
	 * @return 
	 */
	Iterable<? extends ShsMessageEntry> findRelatedEntries(ShsMessageEntry entry, int maxRelatedEntries);

    Iterable<ShsMessageEntry> findMessages(Filter filter);
    int countMessages(Filter filter);

    ShsMessageEntry findById(String messageId);

    class Filter implements Serializable {

        int skip;
        int limit;

        String from;
        String to;
        String corrId;
        String product;
        MessageState state;
        String filename;
        Boolean acknowledged;
        Boolean archived;
        String txId;

        public String getFrom() {
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getCorrId() {
            return corrId;
        }

        public void setCorrId(String corrId) {
            this.corrId = corrId;
        }

        public String getProduct() {
            return product;
        }

        public void setProduct(String product) {
            this.product = product;
        }

        public MessageState getState() {
            return state;
        }

        public void setState(MessageState state) {
            this.state = state;
        }

        public String getFilename() {
            return filename;
        }

        public void setFilename(String filename) {
            this.filename = filename;
        }

        public Boolean getAcknowledged() {
            return acknowledged;
        }

        public void setAcknowledged(Boolean acknowledged) {
            this.acknowledged = acknowledged;
        }
        
        public Boolean getArchived() {
        	return archived;
        }
        
        public void setArchived(Boolean archived) {
        	this.archived = archived;
        }

        public String getTxId() {
            return txId;
        }

        public void setTxId(String txId) {
            this.txId = txId;
        }

        public int getSkip() {
            return skip;
        }

        public void setSkip(int skip) {
            this.skip = skip;
        }

        public int getLimit() {
            return limit;
        }

        public void setLimit(int limit) {
            this.limit = limit;
        }

        @Override
        public String toString() {
            return "Filter{" +
                    "skip=" + skip +
                    ", limit=" + limit +
                    ", from='" + from + '\'' +
                    ", to='" + to + '\'' +
                    ", corrId='" + corrId + '\'' +
                    ", product='" + product + '\'' +
                    ", state='" + state + '\'' +
                    ", filename='" + filename + '\'' +
                    ", acknowledged=" + acknowledged +
                    ", archived=" + archived +
                    ", txId='" + txId + '\'' +
                    '}';
        }
    }
}
