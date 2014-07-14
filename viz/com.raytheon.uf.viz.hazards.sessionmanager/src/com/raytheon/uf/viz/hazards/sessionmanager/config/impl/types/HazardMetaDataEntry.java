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
package com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.codehaus.jackson.JsonNode;

/**
 * 
 * Entry in the HazardMetadataMap
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 10, 2013 1257       bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class HazardMetaDataEntry {
    private String[][] hazardTypes;

    /*
     * These are megawidgets objects so they do not match any definable schema
     * and cannot be directly represented as Java objects.
     */
    private JsonNode metaData;

    public String[][] getHazardTypes() {
        return hazardTypes;
    }

    public void setHazardTypes(String[][] hazardTypes) {
        this.hazardTypes = hazardTypes;
    }

    public JsonNode getMetaData() {
        return metaData;
    }

    public void setMetaData(JsonNode metaData) {
        this.metaData = metaData;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}