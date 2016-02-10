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
package com.raytheon.uf.edex.hazards.servicebackup;

import java.io.File;
import java.util.List;

import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Process Hazard Services Import and Export of Localization data.
 * 
 * This method calls the
 * AWIPS_HOME/edex/scripts/HazardServices/ServiceBackup/scripts
 * /hs_export_configuration to export a Site's Localization Data to the Central
 * Registry X400 directory using the send_msg command. Imports are handled by
 * copying from the mounted, accessible X400 Central Registry directories. This
 * mechanism is also used by the GISSuite Localization import/export process.
 * Import of site configuration data is processed through:
 * AWIPS_HOME/edex/scripts/HazardServices/ServiceBackup/scripts
 * /hs_process_configuration.
 * <p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 14, 2015 3473       Chris.Cody  Initial Implementation; Implement Hazard Services 
 *                                     Import/Export through Central Registry server.
 * 
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */
public class HazardSiteDataProcessor {

    /** The logger */
    private static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(HazardSiteDataProcessor.class);

    private final String svcbuFileNamePrefix = "hs_svc_bkup_";

    private final String sep = File.separator;

    private final String tarSuffix = ".tar";

    public HazardSiteDataProcessor() {
    }

    /**
     * Export Hazard Services Localization data for a given Site Id.
     * 
     * @param siteId
     *            Hazard Services Site Id
     * @return Error messages or null
     */
    public String exportApplicationSiteData(String siteId) {
        StringBuilder resultSB = new StringBuilder();

        try {
            SvcBackupUtil.execute("hs_export_configuration",
                    siteId.toLowerCase());
        } catch (Exception ex) {
            resultSB.append("Error executing hs_export_configuration for configured site: ");
            resultSB.append(siteId);
            statusHandler.error(resultSB.toString(), ex);
        }

        return (resultSB.toString());
    }

    /**
     * Import (to the Request Server Localization directories); Hazard Services
     * Localization data for a list of Backup Site Site Id values. If one import
     * fails; the rest of the imports will be attempted.
     * 
     * @param siteBackupBaseDir
     *            Accessible Site Backup base directory path to import
     * @param backupSiteIdList
     *            List of Hazard Services Site Id data to import
     * @return Error messages or null
     */
    public String retrieveApplicationSiteData(String siteBackupBaseDir,
            List<String> backupSiteIdList) {

        StringBuilder resultSB = new StringBuilder();

        if (siteBackupBaseDir != null) {
            // Get Configured Central Registry X.400 Base Path
            // Ensure it is readable.
            File siteBackupDirectory = new File(siteBackupBaseDir);
            if ((siteBackupDirectory.exists() == false)
                    || (siteBackupDirectory.canRead() == false)) {
                resultSB.append("Unable to request Hazards Services SITE configuration to run as a Backup Site. Configured Backup Base Directory (siteBackupBaseDir Property): "
                        + siteBackupBaseDir
                        + " is inaccessible or does not exist");
                statusHandler.error(resultSB.toString());
                return (resultSB.toString());
            } else if (siteBackupDirectory.isDirectory() == false) {
                resultSB.append("Unable to request Hazard Services SITE configuration to run as a Backup Site. Configured Central Server X.400 Directory (siteBackupBaseDir Property): "
                        + siteBackupBaseDir + " is not a directory.");
                statusHandler.error(resultSB.toString());
                return (resultSB.toString());
            }
        } else {
            resultSB.append("Unable to request Hazard Services SITE configuration to run as a Backup Site. Configured Central Server X.400 Directory (siteBackupBaseDir Property) is null");
            statusHandler.error(resultSB.toString());
            return (resultSB.toString());
        }

        if (resultSB.length() == 0) {
            for (String siteId : backupSiteIdList) {
                try {
                    String lowerCaseSiteId = siteId.toLowerCase();
                    statusHandler
                            .info("Requesting Hazard Services SITE configuration from Central Registry: "
                                    + siteBackupBaseDir
                                    + " (getting SITE backup data) for configured site: "
                                    + siteId);

                    String backupSiteFileName = getBackupSiteFileName(
                            siteBackupBaseDir, lowerCaseSiteId);
                    SvcBackupUtil.execute("hs_process_configuration",
                            backupSiteFileName, lowerCaseSiteId);
                    statusHandler.info("Hazard Services Site Import complete.");
                } catch (Exception ex) {
                    resultSB.append("Error executing hs_process_configuration for configured site: ");
                    resultSB.append(siteId);
                    resultSB.append("\n");
                    statusHandler.error(resultSB.toString(), ex);
                }
            }
        }

        return (resultSB.toString());
    }

    /**
     * Build (default) Hazard Services Localization file name.
     * 
     * @param siteBackupBaseDir
     *            Directory Path Name
     * @param lowerCaseSiteId
     *            Site ID (in lower case)
     * @return Directory Path and File Name
     */
    private String getBackupSiteFileName(String siteBackupBaseDir,
            String lowerCaseSiteId) {

        StringBuilder pathAndFileNameSB = new StringBuilder();
        pathAndFileNameSB.append(siteBackupBaseDir);
        if (siteBackupBaseDir.endsWith(sep) == false) {
            pathAndFileNameSB.append(sep);
        }
        pathAndFileNameSB.append(svcbuFileNamePrefix);
        pathAndFileNameSB.append(lowerCaseSiteId);
        pathAndFileNameSB.append(tarSuffix);

        return pathAndFileNameSB.toString();
    }
}
