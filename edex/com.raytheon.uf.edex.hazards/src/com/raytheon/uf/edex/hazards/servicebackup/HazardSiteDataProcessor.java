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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.configuration.ConfigurationException;

import com.raytheon.uf.common.hazards.productgen.data.ProductData;
import com.raytheon.uf.common.hazards.productgen.data.ProductDataResponse;
import com.raytheon.uf.common.hazards.productgen.data.ProductDataUtil;
import com.raytheon.uf.common.hazards.productgen.editable.ProductText;
import com.raytheon.uf.common.hazards.productgen.editable.ProductTextResponse;
import com.raytheon.uf.common.hazards.productgen.editable.ProductTextUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.util.FileUtil;

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
 * Nov 03, 2016 22119       Kevin.Bisanz Propagate error message during export.
 * Nov 10, 2016 22119       Kevin.Bisanz Changes for product export/import
 * Nov 14, 2016 22119       Kevin.Bisanz Add log file path to export error message.
 * Dec 14, 2016 22119       Kevin.Bisanz Modify options for export system call.
 * Jun 12, 2017 35022       Kevin.Bisanz Remove productID, add mode for ProductText.
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
     * @param exportConfig
     *            Flag to export config info
     * @param exportProductText
     *            Flag to export ProductText info
     * @param exportProductData
     *            Flag to export ProductData info
     * @return Error messages or null
     */
    public String exportApplicationSiteData(String siteId, boolean exportConfig,
            boolean exportProductText, boolean exportProductData) {
        StringBuilder resultSB = new StringBuilder();

        try {
            List<String> args = new ArrayList<>();
            args.add("hs_export_configuration");
            args.add("-s");
            args.add(siteId.toLowerCase());
            if (exportConfig) {
                args.add("-c");
            }
            if (exportProductText) {
                String productTextFilePath = exportProductText(siteId);
                args.add("-t");
                args.add(productTextFilePath);
            }
            if (exportProductData) {
                String productDataFilePath = exportProductData(siteId);
                args.add("-d");
                args.add(productDataFilePath);
            }
            SvcBackupUtil.execute(args.toArray(new String[args.size()]));
        } catch (Exception ex) {
            resultSB.append(
                    "Error executing hs_export_configuration for configured site: ");
            resultSB.append(siteId);
            resultSB.append(". Error: " + ex.getLocalizedMessage());
            String logDir = SvcBackupUtil
                    .getHazardServicesSvcbuProperty("HAZARD_SERVICES_LOG");
            if (logDir != null) {
                resultSB.append(" See log file in " + logDir);
            }
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
                resultSB.append(
                        "Unable to request Hazards Services SITE configuration to run as a Backup Site. Configured Backup Base Directory (siteBackupBaseDir Property): "
                                + siteBackupBaseDir
                                + " is inaccessible or does not exist");
                statusHandler.error(resultSB.toString());
                return (resultSB.toString());
            } else if (siteBackupDirectory.isDirectory() == false) {
                resultSB.append(
                        "Unable to request Hazard Services SITE configuration to run as a Backup Site. Configured Central Server X.400 Directory (siteBackupBaseDir Property): "
                                + siteBackupBaseDir + " is not a directory.");
                statusHandler.error(resultSB.toString());
                return (resultSB.toString());
            }
        } else {
            resultSB.append(
                    "Unable to request Hazard Services SITE configuration to run as a Backup Site. Configured Central Server X.400 Directory (siteBackupBaseDir Property) is null");
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
                    resultSB.append(
                            "Error executing hs_process_configuration for configured site: ");
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

    /**
     * DynamicSerialize a portion of the ProductText table.
     *
     * @param siteId
     *            Site ID of ProductText data to export
     * @return The path to the serialized file.
     * @throws Exception
     */
    private String exportProductText(String siteId) throws Exception {
        String key = null;
        String productCategory = null;
        String mode = null;
        String segment = null;
        List<String> eventIDs = null;
        String officeID = siteId;
        String filePath = getOutputFileName(siteId,
                ProductText.class.getSimpleName());

        ProductTextResponse response = ProductTextUtil.exportProductText(key,
                productCategory, mode, segment, eventIDs, officeID, filePath);
        if (response.getExceptions() != null) {
            throw response.getExceptions();
        }

        return filePath;
    }

    /**
     * DynamicSerialize a portion of the ProductData table.
     *
     * @param siteId
     *            Site ID of ProductData data to export
     * @return The path to the serialized file.
     * @throws Exception
     */
    private String exportProductData(String siteId) throws Exception {
        String mode = null;
        String productGeneratorName = null;
        ArrayList<String> eventIDs = null;
        String fileName = getOutputFileName(siteId,
                ProductData.class.getSimpleName());

        ProductDataResponse response = ProductDataUtil.exportProductData(mode,
                productGeneratorName, eventIDs, siteId, fileName);
        if (response.getExceptions() != null) {
            throw response.getExceptions();
        }

        return fileName;
    }

    /**
     *
     * @param officeID
     *            Office ID of data to export.
     * @param dataName
     *            Name of database table being exported.
     * @return File name to be used for DynamicSerialization
     * @throws FileNotFoundException
     * @throws IOException
     * @throws ConfigurationException
     */
    private String getOutputFileName(String officeID, String dataName)
            throws FileNotFoundException, IOException, ConfigurationException {

        String homeProp = "HS_SVCBU_HOME";
        String svcbuHome = SvcBackupUtil
                .getHazardServicesSvcbuProperty(homeProp);
        if (svcbuHome == null) {
            throw new IllegalStateException(
                    "Unable to find property: " + homeProp);
        }

        String officeIDCaps = officeID.toUpperCase();
        File dir = new File(svcbuHome);
        if (!dir.isDirectory()) {
            dir.mkdirs();
        }

        // Make something like /SomePath/OAX-ProductText.bin
        String outFile = FileUtil.join(svcbuHome,
                officeIDCaps + "-" + dataName + ".bin");

        return outFile;
    }
}
