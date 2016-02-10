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

import java.util.List;

import com.raytheon.uf.common.serialization.annotations.DynamicSerialize;
import com.raytheon.uf.common.serialization.annotations.DynamicSerializeElement;
import com.raytheon.uf.common.serialization.comm.IServerRequest;

/**
 * Cave Request message to request server. This message requests that the
 * Request server exports this Site's Localization Data to the Central Registry
 * or to request Requests that Localization data to be pulled from the Central
 * Registry so that this site can operate as a backup for another Hazard
 * Services Site. This message object supports both Import and Export requests.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 14, 2015 3473       Chris.Cody  Initial creation
 * 
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */

@DynamicSerialize
public class HazardSiteDataRequest implements IServerRequest {
    @DynamicSerializeElement
    private HazardSiteDataRequestType type;

    @DynamicSerializeElement
    private String siteId;

    @DynamicSerializeElement
    private String siteBackupBaseDir;

    @DynamicSerializeElement
    private List<String> backupSiteIdList;

    @DynamicSerializeElement
    private Boolean practice;

    public static enum HazardSiteDataRequestType {
        IMPORT, EXPORT;
    }

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
     */
    public HazardSiteDataRequest(String siteId, Boolean practice) {
        this.type = HazardSiteDataRequestType.EXPORT;
        this.siteId = siteId;
        this.siteBackupBaseDir = null;
        this.backupSiteIdList = null;
        this.setPractice(practice);
    }

    /**
     * Constructor for Import Localization Request
     * 
     * @param siteBackupBaseDir
     *            Site to Export to Backup repository
     */
    public HazardSiteDataRequest(String siteBackupBaseDir,
            List<String> backupSiteIdList, Boolean practice) {
        this.type = HazardSiteDataRequestType.IMPORT;
        this.siteId = null;
        this.siteBackupBaseDir = siteBackupBaseDir;
        this.backupSiteIdList = backupSiteIdList;
        this.setPractice(practice);
    }

    /**
     * Get Message Type.
     * 
     * @return Message Type
     */
    public HazardSiteDataRequestType getType() {
        return this.type;
    }

    /**
     * Set Message Type.
     * 
     * @param type
     *            The Message Type
     */
    public void setType(HazardSiteDataRequestType type) {
        this.type = type;
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
     * This will change the type to HazardSiteDataRequestType.EXPORT and null
     * the backupSiteIdList attribute.
     * 
     * @param siteId
     *            Export Site Id
     */
    public void setSiteId(String siteId) {
        this.siteId = siteId;
        if (siteId != null) {
            this.type = HazardSiteDataRequestType.EXPORT;
            this.backupSiteIdList = null;
        }
    }

    /**
     * Get Site Backup Directory for Import.
     * 
     * The siteBackupBaseDir is an accessible directory (or directory path and
     * file name) where the Localization tar files are located. This is only
     * used for Import.
     * <p>
     * Dev Note: The path and directory must be accessible by the Request
     * Server.
     * 
     * @return Import Site Id List
     */
    public String getSiteBackupBaseDir() {
        return this.siteBackupBaseDir;
    }

    /**
     * Set Site Backup Directory for Import.
     * 
     * The siteBackupBaseDir is an accessible directory (or directory path and
     * file name) where the Localization tar files are located. This is only
     * used for Import.
     * <p>
     * Dev Note: The path and directory must be accessible by the Request
     * Server.
     * 
     * @return Import Site Id List
     */
    public void setSiteBackupBaseDir(String siteBackupBaseDir) {
        this.siteBackupBaseDir = siteBackupBaseDir;
    }

    /**
     * Get Import Site Id List.
     * 
     * @return Import Site Id List
     */
    public List<String> getBackupSiteIdList() {
        return this.backupSiteIdList;
    }

    /**
     * Set Import Site Id List.
     * 
     * This will change the type to HazardSiteDataRequestType.IMPORT and null
     * the siteId attribute.
     * 
     * @param backupSiteIdList
     *            Import Site Id List
     */
    public void setBackupSiteIdList(List<String> backupSiteIdList) {
        this.backupSiteIdList = backupSiteIdList;
        if ((backupSiteIdList != null) && (backupSiteIdList.isEmpty() == false)) {
            this.type = HazardSiteDataRequestType.IMPORT;
            this.siteId = null;
        }
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
