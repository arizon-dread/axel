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
package se.inera.axel.shs.client;

import se.inera.axel.shs.xml.label.Status;

import java.util.*;

public class MessageListConditions {
    Date since;
    Boolean noAck = false;
    Status status = Status.PRODUCTION;
    String originator;
    String endRecipient;
    String corrId;
    String contentId;
    Integer maxHits;
    List<String> productIds = new ArrayList<String>();
    String metaName;
    String metaValue;
    String sortAttribute;
    SortOrder sortOrder = SortOrder.ASCENDING;
    String arrivalOrder = "ascending";

    public Date getSince() {
        return since;
    }

    public void setSince(Date since) {
        this.since = since;
    }

    public Boolean getNoAck() {
        return noAck;
    }

    public void setNoAck(Boolean noAck) {
        this.noAck = noAck;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public String getOriginator() {
        return originator;
    }

    public void setOriginator(String originator) {
        this.originator = originator;
    }

    public String getEndRecipient() {
        return endRecipient;
    }

    public void setEndRecipient(String endRecipient) {
        this.endRecipient = endRecipient;
    }

    public String getCorrId() {
        return corrId;
    }

    public void setCorrId(String corrId) {
        this.corrId = corrId;
    }

    public String getContentId() {
        return contentId;
    }

    public void setContentId(String contentId) {
        this.contentId = contentId;
    }

    public Integer getMaxHits() {
        return maxHits;
    }

    public void setMaxHits(Integer maxHits) {
        this.maxHits = maxHits;
    }

    public List<String> getProductIds() {
        return productIds;
    }

    public void setProductIds(List<String> productIds) {
        this.productIds = productIds;
    }

    public String getSortAttribute() {
        return sortAttribute;
    }

    public void setSortAttribute(String sortAttribute) {
        this.sortAttribute = sortAttribute;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public void setSortOrder(String sortOrder) {
        this.sortOrder = SortOrder.fromString(sortOrder);
    }

    public String getArrivalOrder() {
        return arrivalOrder;
    }

    public void setArrivalOrder(String arrivalOrder) {
        if (arrivalOrder != null && !("descending".equalsIgnoreCase(arrivalOrder) || "ascending".equalsIgnoreCase(arrivalOrder))) {
            throw new IllegalArgumentException(String.format("Invalid arrival order value '%s', must be either descending or ascending", arrivalOrder));
        }
        this.arrivalOrder = arrivalOrder;
    }

    public String getMetaName() {
        return metaName;
    }

    public void setMetaName(String metaName) {
        this.metaName = metaName;
    }

    public String getMetaValue() {
        return metaValue;
    }

    public void setMetaValue(String metaValue) {
        this.metaValue = metaValue;
    }

    @Override
    public String toString() {
        return "Filter{" +
                "since=" + since +
                ", noAck=" + noAck +
                ", status=" + status +
                ", originator='" + originator + '\'' +
                ", endRecipient='" + endRecipient + '\'' +
                ", corrId='" + corrId + '\'' +
                ", contentId='" + contentId + '\'' +
                ", maxHits=" + maxHits +
                ", productIds=" + productIds +
                ", metaName='" + metaName + '\'' +
                ", metaValue='" + metaValue + '\'' +
                ", sortAttribute='" + sortAttribute + '\'' +
                ", sortOrder='" + sortOrder + '\'' +
                ", arrivalOrder='" + arrivalOrder + '\'' +
                '}';
    }

    public enum SortOrder {
        DESCENDING, ASCENDING;

        private static List<String> DESCENDING_VALUES = Arrays.asList("desc", "descending");
        private static List<String> ASCENDING_VALUES = Arrays.asList("asc", "ascending");

        /**
         * Returns the {@link SortOrder} enum for the given {@link String} value.
         *
         * @param value valid values are ascending, asc, descending, desc. The values are case insensitive.
         *              <code>null</code> is valid.
         * @throws IllegalArgumentException if the given value cannot be parsed into an enum value.
         * @return the sort order, if the value is null ASCENDING is returned.
         */
        public static SortOrder fromString(String value) {
            if (value == null) {
                return ASCENDING;
            }

            String lowerCaseValue = value.toLowerCase(Locale.US);

            if (DESCENDING_VALUES.contains(lowerCaseValue)) {
                return DESCENDING;
            } else if (ASCENDING_VALUES.contains(lowerCaseValue)) {
                return ASCENDING;
            } else {
                throw new IllegalArgumentException(String.format(
                        "Invalid sort order value '%s'! Has to be either 'desc', 'descending', 'asc', 'ascending' or null (case insensitive).", value));
            }
        }
    }
}
