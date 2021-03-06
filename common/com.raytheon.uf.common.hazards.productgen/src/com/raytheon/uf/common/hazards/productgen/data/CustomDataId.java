/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.common.hazards.productgen.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import com.raytheon.uf.common.serialization.ISerializableObject;
import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * The class that represents a unique row in the table. Can be multiple keys, so
 * this is embedding a {@link UserTextId} such that there can be multiple of
 * those.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Apr 11, 2014            jsanchez     Initial creation.
 * Aug 13, 2015 8836       Chris.Cody   Changes for a configurable Event Id
 * Sep 11, 2015 10203      Robert.Blum  Added issueTime to the productdata primary key.
 * Nov 07, 2016 22119      Kevin.Bisanz Add officeID.
 * Feb 01, 2017 15556      Chris.Golden Added copy constructor.
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */
@Embeddable
@DynamicSerialize
public class CustomDataId implements ISerializableObject, Serializable {

    @Column
    @DynamicSerializeElement
    private String mode;

    @Column
    @DynamicSerializeElement
    private String productGeneratorName;

    @Column
    @DynamicSerializeElement
    private ArrayList<String> eventIDs;

    @Column
    @DynamicSerializeElement
    private String officeID;

    @Column
    @DynamicSerializeElement
    private Date issueTime;

    public CustomDataId() {

    }

    public CustomDataId(String mode, String productGeneratorName,
            ArrayList<String> eventIDs, String officeID, Date issueTime) {
        this.mode = mode;
        this.productGeneratorName = productGeneratorName;
        this.eventIDs = eventIDs;
        this.officeID = officeID;
        this.issueTime = issueTime;
    }

    public CustomDataId(CustomDataId other) {
        this.mode = other.mode;
        this.productGeneratorName = other.productGeneratorName;
        this.eventIDs = (other.eventIDs == null ? null : new ArrayList<>(
                other.eventIDs));
        this.officeID = other.officeID;
        this.issueTime = (other.issueTime == null ? null : new Date(
                other.issueTime.getTime()));
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getProductGeneratorName() {
        return productGeneratorName;
    }

    public void setProductGeneratorName(String productGeneratorName) {
        this.productGeneratorName = productGeneratorName;
    }

    public ArrayList<String> getEventIDs() {
        return eventIDs;
    }

    public void setEventIDs(ArrayList<String> eventIDs) {
        this.eventIDs = eventIDs;
    }

    public String getOfficeID() {
        return this.officeID;
    }

    public void setOfficeID(String officeID) {
        this.officeID = officeID;
    }

    public Date getIssueTime() {
        return issueTime;
    }

    public void setIssueTime(Date issueTime) {
        this.issueTime = issueTime;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((eventIDs == null) ? 0 : eventIDs.hashCode());
        result = prime * result
                + ((officeID == null) ? 0 : officeID.hashCode());
        result = prime * result + ((mode == null) ? 0 : mode.hashCode());
        result = prime * result + ((productGeneratorName == null) ? 0
                : productGeneratorName.hashCode());
        result = prime * result
                + ((issueTime == null) ? 0 : issueTime.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        CustomDataId other = (CustomDataId) obj;
        if (eventIDs == null) {
            if (other.eventIDs != null) {
                return false;
            }
        } else if (!eventIDs.equals(other.eventIDs)) {
            return false;
        }
        if (mode == null) {
            if (other.mode != null) {
                return false;
            }
        } else if (!mode.equals(other.mode)) {
            return false;
        }
        if (productGeneratorName == null) {
            if (other.productGeneratorName != null) {
                return false;
            }
        } else if (!productGeneratorName.equals(other.productGeneratorName)) {
            return false;
        }
        if (issueTime == null) {
            if (other.issueTime != null) {
                return false;
            }
        } else if (!issueTime.equals(other.issueTime)) {
            return false;
        }
        if (officeID == null) {
            if (other.officeID != null)
                return false;
        } else if (!officeID.equals(other.officeID)) {
            return false;
        }
        return true;
    }

}
