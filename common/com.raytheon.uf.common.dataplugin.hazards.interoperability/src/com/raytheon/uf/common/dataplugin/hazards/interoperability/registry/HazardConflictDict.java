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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * 
 * Container object holding the hazard conflict dictionary
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
@XmlRootElement(name = "HazardConflictDict")
@XmlAccessorType(XmlAccessType.FIELD)
public class HazardConflictDict {

    /** The conflict map */
    private Map<String, HazardConflictDictEntry> conflictDict = new HashMap<String, HazardConflictDictEntry>();

    /**
     * Gets a value from the conflict map
     * 
     * @param key
     *            The key to get
     * @return The value associated with the key
     */
    public List<String> get(String key) {
        HazardConflictDictEntry entry = conflictDict.get(key);
        if (entry == null) {
            return new ArrayList<String>();
        }
        return entry.getValue();
    }

    /**
     * Inserts a value into the conflict map
     * @param key The key
     * @param value The value
     */
    public void put(String key, List<String> value) {
        conflictDict.put(key, new HazardConflictDictEntry(value));
    }

    /**
     * @return the conflictDict
     */
    public Map<String, HazardConflictDictEntry> getConflictDict() {
        return conflictDict;
    }

    /**
     * @param conflictDict
     *            the conflictDict to set
     */
    public void setConflictDict(
            Map<String, HazardConflictDictEntry> conflictDict) {
        this.conflictDict = conflictDict;
    }

    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (Entry<String, HazardConflictDictEntry> entry : conflictDict
                .entrySet()) {
            builder.append("Key: " + entry.getKey()).append(" Value: ")
                    .append(entry.getValue()).append("\n");
        }
        return builder.toString();
    }
}
