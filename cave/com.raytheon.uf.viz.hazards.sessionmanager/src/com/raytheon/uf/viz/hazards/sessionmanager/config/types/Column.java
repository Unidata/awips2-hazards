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
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class Column {
    private Integer width;

    // TODO enum
    private String type;

    private String fieldName;

    private String hintTextFieldName;

    // TODO enum
    private String sortDir;

    private String displayEmptyAs;

    public Column() {

    }

    public Column(String type, String fieldName, String sortDir) {
        this(null, type, fieldName, null, sortDir);
    }

    public Column(Integer width, String type, String fieldName, String sortDir) {
        this(width, type, fieldName, null, sortDir);
    }

    public Column(String type, String fieldName, String hintTextFieldName,
            String sortDir) {
        this(null, type, fieldName, hintTextFieldName, sortDir);
    }

    public Column(Integer width, String type, String fieldName,
            String hintTextFieldName, String sortDir) {
        this.width = width;
        this.type = type;
        this.fieldName = fieldName;
        this.hintTextFieldName = hintTextFieldName;
        this.sortDir = sortDir;
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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((fieldName == null) ? 0 : fieldName.hashCode());
        result = prime
                * result
                + ((hintTextFieldName == null) ? 0 : hintTextFieldName
                        .hashCode());
        result = prime * result + ((sortDir == null) ? 0 : sortDir.hashCode());
        result = prime * result + ((type == null) ? 0 : type.hashCode());
        result = prime * result + ((width == null) ? 0 : width.hashCode());
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
        Column other = (Column) obj;
        if (fieldName == null) {
            if (other.fieldName != null) {
                return false;
            }
        } else if (!fieldName.equals(other.fieldName)) {
            return false;
        }
        if (hintTextFieldName == null) {
            if (other.hintTextFieldName != null) {
                return false;
            }
        } else if (!hintTextFieldName.equals(other.hintTextFieldName)) {
            return false;
        }
        if (sortDir == null) {
            if (other.sortDir != null) {
                return false;
            }
        } else if (!sortDir.equals(other.sortDir)) {
            return false;
        }
        if (type == null) {
            if (other.type != null) {
                return false;
            }
        } else if (!type.equals(other.type)) {
            return false;
        }
        if (width == null) {
            if (other.width != null) {
                return false;
            }
        } else if (!width.equals(other.width)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * @return the displayEmptyAs
     */
    public String getDisplayEmptyAs() {
        return displayEmptyAs;
    }

    /**
     * @param displayEmptyAs the displayEmptyAs to set
     */
    public void setDisplayEmptyAs(String displayEmptyAs) {
        this.displayEmptyAs = displayEmptyAs;
    }

}