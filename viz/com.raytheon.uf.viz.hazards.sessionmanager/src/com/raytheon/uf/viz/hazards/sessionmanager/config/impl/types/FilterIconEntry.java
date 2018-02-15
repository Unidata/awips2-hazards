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

/**
 * Filter Icon Configuration for a given product suite.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 11, 2016   16374    mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class FilterIconEntry {

    private String label;

    private String normalIcon;

    private String coloredIcon;

    private String[] hazardTypes;

    private String[] status;

    /**
     * @return the label
     */
    public String getLabel() {
        return label;
    }

    /**
     * @param label
     *            the label to set
     */
    public void setLabel(String label) {
        this.label = label;
    }

    /**
     * @return the normalIcon
     */
    public String getNormalIcon() {
        return normalIcon;
    }

    /**
     * @param normalIcon
     *            the normalIcon to set
     */
    public void setNormalIcon(String normalIcon) {
        this.normalIcon = normalIcon;
    }

    /**
     * @return the colordIcon
     */
    public String getColoredIcon() {
        return coloredIcon;
    }

    /**
     * @param colordIcon
     *            the colordIcon to set
     */
    public void setColoredIcon(String colorIcon) {
        this.coloredIcon = colorIcon;
    }

    /**
     * @return the hazardTypes
     */
    public String[] getHazardTypes() {
        return hazardTypes;
    }

    /**
     * @param hazardTypes
     *            the hazardTypes to set
     */
    public void setHazardTypes(String[] hazardTypes) {
        this.hazardTypes = hazardTypes;
    }

    /**
     * @return the status
     */
    public String[] getStatus() {
        return status;
    }

    /**
     * @param status
     *            the status to set
     */
    public void setStatus(String[] status) {
        this.status = status;
    }
}
