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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 
 * Generated product created by the ProductGenerator.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 19, 2012            jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */
public class GeneratedProduct implements IGeneratedProduct {

    private String productID;

    /** Resulting products generated */
    private Map<String, List<Object>> entries = new HashMap<String, List<Object>>();

    /** Errors thrown executing python product classes */
    private String errors;

    public GeneratedProduct(String productID) {
        this.productID = productID;
    }

    @Override
    public String getProductID() {
        return productID;
    }

    @Override
    public Map<String, List<Object>> getEntries() {
        return entries;
    }

    public void setEntries(Map<String, List<Object>> entries) {
        this.entries = entries;
    }

    public void addEntry(String key, List<Object> entry) {
        this.entries.put(key, entry);
    }

    public String getErrors() {
        return errors;
    }

    public void setErrors(String errors) {
        this.errors = errors;
    }

    @Override
    public List<Object> getEntry(String format) {
        if (entries != null) {
            return entries.get(format);
        }

        return null;
    }

}
