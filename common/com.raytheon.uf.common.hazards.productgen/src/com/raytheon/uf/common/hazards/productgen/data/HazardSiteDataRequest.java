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
package com.raytheon.uf.common.hazards.productgen.data;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * Cave Request message to request server. This message requests that the
 * Request server exports this Site's Localization Data.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 14, 2015  3473      Chris.Cody  Initial creation
 * Nov 23, 2015  3473      Robert.Blum Changed to only be used for exports.
 * Dec 15, 2016 22119      Kevin.Bisanz Added flags to export config, ProductText,
 *                                     and ProductData individually.
 * 
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */

@DynamicSerialize
public class HazardSiteDataRequest implements IServerRequest {

    @DynamicSerializeElement
    private String siteId;

    @DynamicSerializeElement
    private Boolean practice;

    @DynamicSerializeElement
    private boolean exportConfig;

    @DynamicSerializeElement
    private boolean exportProductText;

    @DynamicSerializeElement
    private boolean exportProductData;

    /**
     * Default constructor.
     */
    public HazardSiteDataRequest() {
    }

    /**
     * Constructor for Export Localization Request
     * 
     * @param siteId
     *            Site to Export to Central Registry Server
     * @param practice
     * @param exportConfig
     *            Flag to export config info
     * @param exportProductText
     *            Flag to export ProductText info
     * @param exportProductData
     *            Flag to export ProductData info
     */
    public HazardSiteDataRequest(String siteId, Boolean practice,
            boolean exportConfig, boolean exportProductText,
            boolean exportProductData) {
        this.siteId = siteId;
        this.setPractice(practice);
        this.exportConfig = exportConfig;
        this.exportProductText = exportProductText;
        this.exportProductData = exportProductData;
    }

    public void setExportConfig(boolean exportConfig) {
        this.exportConfig = exportConfig;
    }

    public boolean isExportConfig() {
        return exportConfig;
    }

    public void setExportProductText(boolean exportProductText) {
        this.exportProductText = exportProductText;
    }

    public boolean isExportProductText() {
        return exportProductText;
    }

    public void setExportProductData(boolean exportProductData) {
        this.exportProductData = exportProductData;
    }

    public boolean isExportProductData() {
        return exportProductData;
    }

    /**
     * Get Export Site Id.
     * 
     * @return Export Site Id
     */
    public String getSiteId() {
        return this.siteId;
    }

    /**
     * Set Export Site Id.
     * 
     * @param siteId
     *            Export Site Id
     */
    public void setSiteId(String siteId) {
        this.siteId = siteId;
    }

    /**
     * Mode of Hazard Service Operation.
     * 
     * @return Hazard Services Mode
     */
    public Boolean getPractice() {
        return this.practice;
    }

    /**
     * Mode of Hazard Service Operation.
     * 
     * @param practice
     *            Hazard Services Mode
     */
    public void setPractice(Boolean practice) {
        if (practice == null) {
            practice = Boolean.TRUE;
        }
        this.practice = practice;
    }

}
