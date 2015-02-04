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
package com.raytheon.uf.viz.hazards.sessionmanager.config.types;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 * Object defining a tool.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 17, 2013            bsteffen     Initial creation
 * Dec 05, 2014    4124    Chris.Golden Added copy constructor.
 * Jan 29, 2015 4375       Dan Schaffer Console initiation of RVS product generation
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@XmlType(name = "HazardServicesSettingsTools")
@XmlAccessorType(XmlAccessType.FIELD)
public class Tool {
    private String toolName;

    private String displayName;

    @XmlJavaTypeAdapter(ToolTypeAdapter.class)
    private ToolType toolType;

    public Tool() {

        /*
         * No action.
         */
    }

    public Tool(Tool other) {
        this.toolName = other.toolName;
        this.displayName = other.displayName;
        this.toolType = other.toolType;
    }

    public String getToolName() {
        return toolName;
    }

    public void setToolName(String toolName) {
        this.toolName = toolName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    /**
     * @return the toolType
     */
    public ToolType getToolType() {
        return toolType;
    }

    /**
     * @param toolType
     *            the toolType to set
     */
    public void setToolType(ToolType toolType) {
        this.toolType = toolType;
    }

}