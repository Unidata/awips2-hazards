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
package com.raytheon.uf.common.dataplugin.events.hazards.request;

import java.util.Arrays;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;

/**
 * 
 * Query parameter used by HazardEventQueryRequest
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 29, 2015 6895      Ben.Phillippe Refactored Hazard Service data access
 * Aug 20, 2015 6895      Ben.Phillippe Routing registry requests through
 *                                      request server
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
@XmlRootElement(name = "HazardQueryParameter")
@XmlAccessorType(XmlAccessType.NONE)
@DynamicSerialize
public class HazardQueryParameter {

    /** The equals operator */
    private static final String EQUALS = "=";

    /** The name of the parameter */
    @XmlElement
    @DynamicSerializeElement
    private String key;

    /** The operand to use. Equals (=) is the default operand */
    @XmlElement
    @DynamicSerializeElement
    private String operand = EQUALS;

    /** The value(s) */
    @XmlElement
    @DynamicSerializeElement
    private Object[] values;

    /**
     * Creates an empty HazardQueryParameter
     */
    public HazardQueryParameter() {
    }

    /**
     * Creates a query parameter with the given values
     * 
     * @param key
     *            The key
     * @param operand
     *            The operand
     * @param values
     *            The values
     */
    public HazardQueryParameter(String key, String operand, Object[] values) {
        super();
        this.key = key;
        this.operand = operand;
        this.values = values;
    }

    /**
     * Creates a query parameter assuming the equals operator is to be used
     * 
     * @param key
     * @param values
     */
    public HazardQueryParameter(String key, Object[] values) {
        this(key, EQUALS, values);
    }

    /**
     * Creates a query parameter for a single value
     * 
     * @param key
     *            The key
     * @param operand
     *            The operand
     * @param value
     *            The operand
     */
    public HazardQueryParameter(String key, String operand, Object value) {
        this(key, operand, new Object[] { value });
    }

    /**
     * Creates a query parameter for a single value. Assumes the equals (=)
     * operator is to be used.
     * 
     * @param key
     *            The key
     * @param value
     *            The value
     */
    public HazardQueryParameter(String key, Object value) {
        this(key, "=", new Object[] { value });
    }

    /**
     * @return the key
     */
    public String getKey() {
        return key;
    }

    /**
     * @param key
     *            the key to set
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * @return the operand
     */
    public String getOperand() {
        return operand;
    }

    /**
     * @param operand
     *            the operand to set
     */
    public void setOperand(String operand) {
        this.operand = operand;
    }

    /**
     * @return the values
     */
    public Object[] getValues() {
        return values;
    }

    /**
     * @param values
     *            the values to set
     */
    public void setValues(Object[] values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return key + " " + operand + " " + Arrays.toString(values);
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((key == null) ? 0 : key.hashCode());
        result = prime * result + ((operand == null) ? 0 : operand.hashCode());
        result = prime * result + Arrays.hashCode(values);
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
        HazardQueryParameter other = (HazardQueryParameter) obj;
        if (key == null) {
            if (other.key != null) {
                return false;
            }
        } else if (!key.equals(other.key)) {
            return false;
        }
        if (operand == null) {
            if (other.operand != null) {
                return false;
            }
        } else if (!operand.equals(other.operand)) {
            return false;
        }
        if (!Arrays.equals(values, other.values)) {
            return false;
        }
        return true;
    }

}
