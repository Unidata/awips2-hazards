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
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

import javax.xml.bind.JAXB;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.PropertiesConfiguration;

import com.raytheon.uf.common.hazards.configuration.backup.BackupSites;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.LocalizationUtil;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.util.CollectionUtil;
import com.raytheon.uf.common.util.FileUtil;
import com.raytheon.uf.common.util.RunProcess;
import com.raytheon.uf.edex.core.EDEXUtil;

/**
 * 
 * Utility class for Service Backup
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 01, 2015 3473       Chris.Cody  Import SvcBackupUtil. Originally from com.raytheon.edex.plugin.gfe
 * Nov 23, 2015 3473       Robert.Blum Removed un-needed code and reading backups sites from xml file.
 * Feb 10, 2016 8837       Benjamin.Phillippe  Fixed location of service backup scripts
 * Nov 03, 2016 22119      Kevin.Bisanz Error if no backup sites listed.
 * Nov 14, 2016 22119      Kevin.Bisanz Add getHazardServicesSvcbuProperty(..)
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public class SvcBackupUtil {
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(SvcBackupUtil.class);

    public static final String OPERATION_FAIL = "Failure";

    public static final String OPERATION_SUCCESS = "Success";

    /**
     * A private constructor so that Java does not attempt to create one for us.
     * As this class should not be instantiated, do not attempt to ever call
     * this constructor; it will simply throw an AssertionError.
     * 
     */
    private SvcBackupUtil() {
        throw new AssertionError();
    }

    public static String execute(String... args) throws Exception {

        String hazardServicesScriptsDir = getHazardServicesScriptsDir();
        String[] newArgs = new String[args.length + 1];
        newArgs[0] = "sh";
        newArgs[1] = hazardServicesScriptsDir + File.separator + args[0];
        System.arraycopy(args, 1, newArgs, 2, args.length - 1);
        return executeProcess(newArgs);
    }

    /**
     * Executes a process using the java.lang.ProcessBuilder.
     * <p>
     * The first argument is the command to execute. The proceeding arguments
     * are the arguments to pass to the command for execution
     * 
     * @param args
     *            First argument is the command. The proceeding arguments are
     *            the arguments to pass to the command for execution
     * @return The output of the process
     * @throws Exception
     *             If errors occur while executing the process
     */
    private static String executeProcess(String... args) throws Exception {
        StringBuilder sb = new StringBuilder();
        String[] backupSites = getBackupSites();
        if (CollectionUtil.isNullOrEmpty(backupSites)) {
            throw new Exception("Unable to determine backup sites.");
        }

        for (String site : backupSites) {
            sb.append(site).append(",");
        }

        RunProcess proc = RunProcess.getRunProcess();
        ProcessBuilder pBuilder = new ProcessBuilder();
        pBuilder.environment().put("LOCALIZATION_PATH",
                EDEXUtil.getEdexUtility());
        // Ensure Environment variables are set
        String awipsHome = getAwipsHomeDir();
        pBuilder.environment().put("AWIPS_HOME", awipsHome);
        String awSiteId = System.getProperty("AW_SITE_IDENTIFIER");
        pBuilder.environment().put("AW_SITE_IDENTIFIER", awSiteId);
        pBuilder.environment().put("EXPORT_SITES",
                sb.substring(0, sb.length() - 1));
        pBuilder.redirectErrorStream(true);
        pBuilder.command(args);
        try {
            proc.setProcess(pBuilder.start());
        } catch (IOException e) {
            throw new Exception("Process terminated abnormally: ", e);
        }

        int exitValue = 0;
        String processOutput = "";

        exitValue = proc.waitFor();
        if (proc.isProcessInterrupted()) {
            throw new Exception("Process interrupted");
        }
        processOutput = proc.getStdout();
        if (exitValue != 0) {
            statusHandler.error(processOutput);
            throw new Exception(
                    "Process terminated abnormally: " + processOutput);
        }
        return processOutput;

    }

    /**
     * Examines the InputStream of a process and extracts any output into a
     * String
     * 
     * @param p
     *            The process to get the output from
     * @return The output
     * @throws Exception
     *             If problems occur reading the process output
     */
    public static String getProcessOutput(Process p) throws Exception {

        String retVal = null;
        InputStream in = p.getInputStream();
        StringBuilder out = new StringBuilder();
        int read = 0;
        final byte[] buffer = new byte[0x10000];
        try {
            do {
                read = in.read(buffer, 0, buffer.length);
                if (read > 0) {
                    out.append(new String(buffer), 0, read);
                }
            } while (read >= 0);
        } catch (IOException e) {
            throw new Exception("Error reading process output", e);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    statusHandler.error("Unable to close process input stream!",
                            e);
                }
            }
            try {
                p.getOutputStream().close();
            } catch (IOException e1) {
                statusHandler.error("Unable to close process output stream!",
                        e1);
            }

            try {
                p.getErrorStream().close();
            } catch (IOException e1) {
                statusHandler.error("Unable to close process error stream!",
                        e1);
            }
        }

        retVal = out.toString();
        if (retVal.endsWith("\n")) {
            retVal = retVal.substring(0, retVal.length() - 1);
        }
        return retVal;
    }

    private static String getAwipsHomeDir() {
        String awipsHome = "/awips2";
        String edexHome = System.getProperty("edex.home");
        if ((edexHome != null) && (edexHome.isEmpty() == false)) {
            int idx = edexHome.indexOf("/", 2);
            if (idx > 2) {
                awipsHome = edexHome.substring(0, idx);
            }
        }
        return (awipsHome);
    }

    private static String getEdexScriptsDir() {
        String awipsHome = getAwipsHomeDir();
        String edexScriptsDir = FileUtil.join(awipsHome, "edex", "scripts");
        return (edexScriptsDir);
    }

    private static String getHazardServicesScriptsDir() {
        return FileUtil.join(EDEXUtil.getEdexUtility(), "edex_static", "base",
                "HazardServices", "ServiceBackup", "scripts");
    }

    /**
     * Load the service backup properties file and return the value of the
     * requested property.
     *
     * @param prop
     *            Property key to find.
     * @return Value of the property, or null if not found.
     */
    public static String getHazardServicesSvcbuProperty(String prop) {
        String propFile = FileUtil.join(EDEXUtil.getEdexUtility(),
                "edex_static", "base", "HazardServices", "ServiceBackup",
                "configuration", "svcbu.properties");

        String value = null;

        try {
            Configuration config = new PropertiesConfiguration(propFile);
            value = config.getString(prop);
        } catch (ConfigurationException e) {
            statusHandler.error(e.getLocalizedMessage(), e);
        }

        return value;
    }

    /**
     * Load the backup sites.
     * 
     * @return Array of backup sites, or null if no sites configured
     */
    private static String[] getBackupSites() {
        IPathManager pathMgr = PathManagerFactory.getPathManager();

        Map<LocalizationLevel, LocalizationFile> files = pathMgr
                .getTieredLocalizationFile(LocalizationType.COMMON_STATIC,
                        LocalizationUtil.join("HazardServices", "settings",
                                "backupSites.xml"));
        LocalizationFile file = null;
        if (files.containsKey(LocalizationLevel.SITE)) {
            file = files.get(LocalizationLevel.SITE);
        } else {
            file = files.get(LocalizationLevel.BASE);
        }

        BackupSites backupSites = null;
        try (InputStream is = file.openInputStream()) {
            backupSites = JAXB.unmarshal(is, BackupSites.class);
            return backupSites.getSites();
        } catch (Exception e) {
            statusHandler.error(
                    "Error loading backup sites from backupSites.xml", e);
        }

        return null;
    }
}
