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
package com.raytheon.uf.common.hazards.productgen;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.persistence.Embeddable;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

import gov.noaa.gsd.common.utilities.collect.IParameterInfo;

/**
 * Basic information about the keys set in the python dictionary.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 1, 2014            jsanchez     Initial creation
 * Apr 10, 2014  2336     Chris.Golden Added implementation of IParameterInfo.
 * Apr 23, 2014  3519     jsanchez     Made this class serializable and added the required field.
 * Jul 28, 2015  9687     Robert.Blum  Added displayLabel field.
 * Aug 03, 2015  8836     Chris.Cody   Changes for a configurable Event Id
 * Aug 31, 2015  9617     Chris.Golden Decoupled from the megawidget framework.
 * Feb 23, 2017  29170    Robert.Blum  Product Editor refactor.
 * Mar 30, 2017  32569    Robert.Blum  Added segmentDivider field.
 * Jun 05, 2017  29996    Robert.Blum  Removed all Product Editor configurable fields, this
 *                                     is strictly used for storing to ProductText table.
 * Jun 12, 2017  35022    Kevin.Bisanz Remove productID, add mode.
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */
@Embeddable
@DynamicSerialize
public class KeyInfo implements IParameterInfo, Serializable {

    @DynamicSerializeElement
    private String name;

    @DynamicSerializeElement
    private String productCategory;

    @DynamicSerializeElement
    private String mode;

    @DynamicSerializeElement
    private List<String> eventIDs;

    @DynamicSerializeElement
    private String segment;

    private int index;

    public KeyInfo() {

    }

    public KeyInfo(KeyInfo keyInfo) {
        this.name = keyInfo.getName();
        this.productCategory = keyInfo.getProductCategory();
        this.mode = keyInfo.getMode();
        this.eventIDs = keyInfo.getEventIDs();
        this.segment = keyInfo.getSegment();
        this.index = keyInfo.getIndex();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public List<String> getEventIDs() {
        return eventIDs;
    }

    public void setEventIDs(List<String> eventIDs) {
        this.eventIDs = eventIDs;
    }

    public String getSegment() {
        return segment;
    }

    public void setSegment(String segment) {
        this.segment = segment;
    }

    @Override
    public String getKey() {
        return toString();
    }

    @Override
    public String getLabel() {
        return name;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((eventIDs == null) ? 0 : eventIDs.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result
                + ((productCategory == null) ? 0 : productCategory.hashCode());
        result = prime * result + ((mode == null) ? 0 : mode.hashCode());
        result = prime * result + ((segment == null) ? 0 : segment.hashCode());
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
        if (obj instanceof KeyInfo == false) {
            return false;
        }
        KeyInfo other = (KeyInfo) obj;
        if (eventIDs == null) {
            if (other.eventIDs != null) {
                return false;
            }
        } else if (!eventIDs.equals(other.eventIDs)) {
            return false;
        }
        if (index != other.index) {
            return false;
        }
        if (name == null) {
            if (other.name != null) {
                return false;
            }
        } else if (!name.equals(other.name)) {
            return false;
        }
        if (productCategory == null) {
            if (other.productCategory != null) {
                return false;
            }
        } else if (!productCategory.equals(other.productCategory)) {
            return false;
        }
        if (mode == null) {
            if (other.mode != null)
                return false;
        } else if (!mode.equals(other.mode)) {
            return false;
        }
        if (segment == null) {
            if (other.segment != null) {
                return false;
            }
        } else if (!segment.equals(other.segment)) {
            return false;
        }
        return true;
    }

    public static KeyInfo createBasicKeyInfo(String name) {
        KeyInfo info = new KeyInfo();
        info.setName(name);
        info.setEventIDs(new ArrayList<String>());
        return info;
    }

    @Deprecated
    public static KeyInfo getElements(String label, Set<KeyInfo> keySet) {
        for (KeyInfo key : keySet) {
            if (key.getLabel().equals(label)) {
                return key;
            }
        }
        return null;
    }

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }
}
