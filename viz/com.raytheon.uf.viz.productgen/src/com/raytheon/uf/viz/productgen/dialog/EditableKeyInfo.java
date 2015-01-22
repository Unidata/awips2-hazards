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
package com.raytheon.uf.viz.productgen.dialog;

import java.io.Serializable;
import java.util.List;

import com.raytheon.uf.common.hazards.productgen.KeyInfo;

/**
 * 
 * This class holds the details pertaining to an editable piece of data produced
 * by the product generator
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * 01/15/2015   5109       bphillip     Initial creation
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
class EditableKeyInfo {

    /**
     * Path to the editable key info object in the data map contained in the
     * generated product
     */
    private List<KeyInfo> path;

    /** Denotes if this data item has been modified */
    private boolean modified = false;

    /** Denotes if this data item is displayable in the product editor */
    private boolean displayable = false;

    /** The current value associated with this key info */
    private Serializable value;

    /** The original value of this key */
    private Serializable originalValue;

    /** The previous value of this key prior to modifications */
    private Serializable previousValue;

    /**
     * @return the path
     */
    public List<KeyInfo> getPath() {
        return path;
    }

    /**
     * @param path the path to set
     */
    public void setPath(List<KeyInfo> path) {
        this.path = path;
    }

    /**
     * @return the modified
     */
    public boolean isModified() {
        return modified;
    }

    /**
     * @param modified the modified to set
     */
    public void setModified(boolean modified) {
        this.modified = modified;
    }

    /**
     * @return the displayable
     */
    public boolean isDisplayable() {
        return displayable;
    }

    /**
     * @param displayable the displayable to set
     */
    public void setDisplayable(boolean displayable) {
        this.displayable = displayable;
    }

    /**
     * @return the value
     */
    public Serializable getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(Serializable value) {
        this.value = value;
    }

    /**
     * @return the originalValue
     */
    public Serializable getOriginalValue() {
        return originalValue;
    }

    /**
     * @param originalValue the originalValue to set
     */
    public void setOriginalValue(Serializable originalValue) {
        this.originalValue = originalValue;
    }

    /**
     * @return the previousValue
     */
    public Serializable getPreviousValue() {
        return previousValue;
    }

    /**
     * @param previousValue the previousValue to set
     */
    public void setPreviousValue(Serializable previousValue) {
        this.previousValue = previousValue;
    }
    
    
    
    
}
