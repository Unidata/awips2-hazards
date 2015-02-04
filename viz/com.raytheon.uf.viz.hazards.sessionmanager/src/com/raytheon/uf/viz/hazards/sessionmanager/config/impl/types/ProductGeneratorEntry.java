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

/**
 * 
 * Entry in the product generation table.
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
public class ProductGeneratorEntry {

    private String[][] allowedHazards;

    private boolean reservedNameNotYetImplemented;

    private String[] previewFormatters;

    private String[] issueFormatters;
    
    private boolean autoSelect = true;

    public String[][] getAllowedHazards() {
        return allowedHazards;
    }

    public void setAllowedHazards(String[][] allowedHazards) {
        this.allowedHazards = allowedHazards;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * @return the reservedNameNotYetImplemented
     */
    public boolean isReservedNameNotYetImplemented() {
        return reservedNameNotYetImplemented;
    }

    /**
     * @param reservedNameNotYetImplemented
     *            the reservedNameNotYetImplemented to set
     */
    public void setReservedNameNotYetImplemented(
            boolean reservedNameNotYetImplemented) {
        this.reservedNameNotYetImplemented = reservedNameNotYetImplemented;
    }

    /**
     * @return the previewFormatters
     */
    public String[] getPreviewFormatters() {
        return previewFormatters;
    }

    /**
     * @param previewFormatters
     *            the previewFormatters to set
     */
    public void setPreviewFormatters(String[] previewFormatters) {
        this.previewFormatters = previewFormatters;
    }

    /**
     * @return the issueFormatters
     */
    public String[] getIssueFormatters() {
        return issueFormatters;
    }

    /**
     * @param issueFormatters
     *            the issueFormatters to set
     */
    public void setIssueFormatters(String[] issueFormatters) {
        this.issueFormatters = issueFormatters;
    }

    public void setAutoSelect(
            boolean autoSelect) {
        this.autoSelect = autoSelect;
    }

    public boolean getAutoSelect() {
		return autoSelect;
	}
}