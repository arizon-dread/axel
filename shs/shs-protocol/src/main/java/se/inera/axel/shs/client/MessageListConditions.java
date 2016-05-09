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

public class MessageListConditions implements Cloneable {

    String since;
    String filter;
    String status;
    String originator;
    String endrecipient;
    String corrid;
    String contentid;
    Integer maxhits;
    String producttype;
    String metaname;
    String metavalue;
    String sortattribute;
    String sortorder;
    String arrivalorder = "ascending";

    public String getSince() {
        return since;
    }

    public void setSince(String since) {
        this.since = since;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getOriginator() {
        return originator;
    }

    public void setOriginator(String originator) {
        this.originator = originator;
    }

    public String getEndrecipient() {
        return endrecipient;
    }

    public void setEndrecipient(String endrecipient) {
        this.endrecipient = endrecipient;
    }

    public String getCorrid() {
        return corrid;
    }

    public void setCorrid(String corrid) {
        this.corrid = corrid;
    }

    public String getContentid() {
        return contentid;
    }

    public void setContentid(String contentid) {
        this.contentid = contentid;
    }

    public Integer getMaxhits() {
        return maxhits;
    }

    public void setMaxhits(Integer maxhits) {
        this.maxhits = maxhits;
    }

    public String getProducttype() {
        return producttype;
    }

    public void setProducttype(String producttype) {
        this.producttype = producttype;
    }

    public String getMetaname() {
        return metaname;
    }

    public void setMetaname(String metaname) {
        this.metaname = metaname;
    }

    public String getMetavalue() {
        return metavalue;
    }

    public void setMetavalue(String metavalue) {
        this.metavalue = metavalue;
    }

    public String getSortattribute() {
        return sortattribute;
    }

    public void setSortattribute(String sortattribute) {
        this.sortattribute = sortattribute;
    }

    public String getSortorder() {
        return sortorder;
    }

    public void setSortorder(String sortorder) {
        this.sortorder = sortorder;
    }

    public String getArrivalorder() {
        return arrivalorder;
    }

    public void setArrivalorder(String arrivalorder) {
        this.arrivalorder = arrivalorder;
    }

    @Override
    public String toString() {
        return "MessageListConditions{" +
                "since='" + since + '\'' +
                ", filter='" + filter + '\'' +
                ", status='" + status + '\'' +
                ", originator='" + originator + '\'' +
                ", endrecipient='" + endrecipient + '\'' +
                ", corrid='" + corrid + '\'' +
                ", contentid='" + contentid + '\'' +
                ", maxhits=" + maxhits +
                ", producttype='" + producttype + '\'' +
                ", metaname='" + metaname + '\'' +
                ", metavalue='" + metavalue + '\'' +
                ", sortAttribute='" + sortattribute + '\'' +
                ", sortorder='" + sortorder + '\'' +
                ", arrivalorder='" + arrivalorder + '\'' +
                '}';
    }

    public MessageListConditions copy() {
        try {
            return (MessageListConditions)this.clone();
        } catch (Exception e) {
            return null;
        }
    }
}
