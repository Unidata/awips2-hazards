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
package com.raytheon.uf.common.dataplugin.hazards.interoperability.registry;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * Holds an entry in the hazard conflict dictionary
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 29, 2015 6895      Ben.Phillippe Refactored Hazard Service data access
 * Aug 4, 2015  6895     Ben.Phillippe Finished HS data access refactor
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@XmlRootElement(name = "HazardConflictDictEntry")
@XmlAccessorType(XmlAccessType.FIELD)
public class HazardConflictDictEntry {

    /** The value */
    @XmlElement
    private List<String> value = new ArrayList<String>();

    /**
     * Creates a new HazardConflictDictEntry
     */
    public HazardConflictDictEntry() {

    }

    /**
     * Creats a new entry with the given value list
     * 
     * @param value
     */
    public HazardConflictDictEntry(List<String> value) {
        this.value = value;
    }

    /**
     * @return the value
     */
    public List<String> getValue() {
        return value;
    }

    /**
     * @param value
     *            the value to set
     */
    public void setValue(List<String> value) {
        this.value = value;
    }

    public String toString() {
        return value.toString();
    }
}
