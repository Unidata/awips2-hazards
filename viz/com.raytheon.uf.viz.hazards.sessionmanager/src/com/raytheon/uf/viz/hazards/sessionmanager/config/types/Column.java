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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * 
 * Object defining a column in the temporal display.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 17, 2013 1257       bsteffen    Initial creation
 * Feb 19, 2014 2915       bkowal      Remove unused constructors.
 * Dec 05, 2014 4124       Chris.Golden Added copy constructor, and
 *                                      new sort priority parameter.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@XmlType(name = "HazardServicesSettingsColumn")
@XmlAccessorType(XmlAccessType.FIELD)
public class Column {
    private Integer width;

    // TODO enum
    private String type;

    private String fieldName;

    private String hintTextFieldName;

    // TODO enum
    private String sortDir;

    /**
     * Sort priority; if <code>0</code>, this column is not being used for
     * sorting.
     */
    private int sortPriority;

    private String displayEmptyAs;

    public Column() {

    }

    public Column(String type, String fieldName, String sortDir) {
        this(null, type, fieldName, null, sortDir);
    }

    public Column(Integer width, String type, String fieldName,
            String hintTextFieldName, String sortDir) {
        this.width = width;
        this.type = type;
        this.fieldName = fieldName;
        this.hintTextFieldName = hintTextFieldName;
        this.sortDir = sortDir;
    }

    public Column(Column other) {
        this.width = other.width;
        this.type = other.type;
        this.fieldName = other.fieldName;
        this.hintTextFieldName = other.hintTextFieldName;
        this.sortPriority = other.sortPriority;
        this.sortDir = other.sortDir;
        this.displayEmptyAs = other.displayEmptyAs;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public int getSortPriority() {
        return sortPriority;
    }

    public void setSortPriority(int sortPriority) {
        this.sortPriority = sortPriority;
    }

    public String getSortDir() {
        return sortDir;
    }

    public void setSortDir(String sortDir) {
        this.sortDir = sortDir;
    }

    public String getHintTextFieldName() {
        return hintTextFieldName;
    }

    public void setHintTextFieldName(String hintTextFieldName) {
        this.hintTextFieldName = hintTextFieldName;
    }

    /**
     * @return the displayEmptyAs
     */
    public String getDisplayEmptyAs() {
        return displayEmptyAs;
    }

    /**
     * @param displayEmptyAs
     *            the displayEmptyAs to set
     */
    public void setDisplayEmptyAs(String displayEmptyAs) {
        this.displayEmptyAs = displayEmptyAs;
    }

    @Override
    public int hashCode() {
        return HashCodeBuilder.reflectionHashCode(this);
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}