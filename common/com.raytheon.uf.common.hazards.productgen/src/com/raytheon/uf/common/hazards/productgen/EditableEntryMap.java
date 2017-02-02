package com.raytheon.uf.common.hazards.productgen;

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

import java.io.Serializable;
import java.util.LinkedHashMap;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * Stores the map of the editable fields for a given format.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 07, 2015 6979       Robert.Blum  Initial creation.
 * Feb 01, 2017 15556      Chris.Golden Added copy constructor.
 * </pre>
 * 
 * @author Robert.Blum
 * @version 1.0
 */

@DynamicSerialize
public class EditableEntryMap implements Serializable {

    private static final long serialVersionUID = 1L;

    @DynamicSerializeElement
    private String format;

    @DynamicSerializeElement
    private LinkedHashMap<KeyInfo, Serializable> editableEntries;

    public EditableEntryMap() {

    }

    public EditableEntryMap(String format,
            LinkedHashMap<KeyInfo, Serializable> editableEntries) {
        this.format = format;
        this.editableEntries = editableEntries;
    }

    public EditableEntryMap(EditableEntryMap other) {
        this.format = other.format;
        this.editableEntries = (other.editableEntries == null ? null
                : new LinkedHashMap<>(other.editableEntries));
    }

    public LinkedHashMap<KeyInfo, Serializable> getEditableEntries() {
        return editableEntries;
    }

    public void setEditableEntries(
            LinkedHashMap<KeyInfo, Serializable> editableEntries) {
        this.editableEntries = editableEntries;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}