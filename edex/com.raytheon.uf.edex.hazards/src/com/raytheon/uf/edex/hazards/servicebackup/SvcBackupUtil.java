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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.google.common.collect.Sets;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.localization.LocalizationContext.LocalizationType;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.localization.exception.LocalizationException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.util.FileUtil;
import com.raytheon.uf.common.util.RunProcess;
import com.raytheon.uf.edex.core.EDEXUtil;
import com.raytheon.uf.edex.site.SiteAwareRegistry;

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

        RunProcess proc = RunProcess.getRunProcess();
        ProcessBuilder pBuilder = new ProcessBuilder();
        pBuilder.environment().put("LOCALIZATION_PATH",
                EDEXUtil.getEdexUtility());
        // Ensure Environment variables are set
        String awipsHome = getAwipsHomeDir();
        pBuilder.environment().put("AWIPS_HOME", awipsHome);
        String awSiteId = System.getProperty("AW_SITE_IDENTIFIER");
        pBuilder.environment().put("AW_SITE_IDENTIFIER", awSiteId);
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
            throw new Exception("Process terminated abnormally: "
                    + processOutput);
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
                    statusHandler.error(
                            "Unable to close process input stream!", e);
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
                statusHandler
                        .error("Unable to close process error stream!", e1);
            }
        }

        retVal = out.toString();
        if (retVal.endsWith("\n")) {
            retVal = retVal.substring(0, retVal.length() - 1);
        }
        return retVal;
    }

    public static Properties getSvcBackupProperties() {
        Properties svcbuProperties = new Properties();

        IPathManager pathMgr = PathManagerFactory.getPathManager();

        LocalizationFile basePropsFile = pathMgr.getLocalizationFile(pathMgr
                .getContext(LocalizationType.EDEX_STATIC,
                        LocalizationLevel.BASE), getHazardServicesPropsFile());
        try (InputStream input = basePropsFile.openInputStream()) {
            svcbuProperties.load(input);
        } catch (IOException | LocalizationException e) {
            statusHandler.error(
                    "Unable to load BASE level svcbu.properties file.", e);
        }

        LocalizationFile sitePropsFile = pathMgr.getLocalizationFile(pathMgr
                .getContextForSite(LocalizationType.EDEX_STATIC,
                        EDEXUtil.getEdexSite()), getHazardServicesPropsFile());
        if (sitePropsFile.exists()) {
            try (InputStream input = sitePropsFile.openInputStream()) {
                svcbuProperties.load(input);
            } catch (IOException | LocalizationException e) {
                statusHandler.error(
                        "Unable to load SITE level svcbu.properties file.", e);
            }
        }

        return svcbuProperties;
    }

    /**
     * Returns the base lock directory for service backup. All site specific
     * lock directories will be children to this directory.
     * 
     * @return The {@code Path} that represents the base directory for service
     *         backup locks.
     */
    public static Path getLockDir() {
        String lockDir = SvcBackupUtil.getSvcBackupProperties().getProperty(
                "LOCK_DIR");
        return Paths.get(lockDir);
    }

    /**
     * Returns the site-specific lock directory for service backup.
     * 
     * @param siteID
     *            The 3-character site identifier.
     * @return he {@code Path} that represents the site-specific directory for
     *         service backup locks.
     */
    public static Path getLockDir(final String siteID) {
        return getLockDir().resolve(siteID.toUpperCase());
    }

    public static Set<String> getPrimarySites() {
        Properties svcbuProps = SvcBackupUtil.getSvcBackupProperties();
        String siteList = EDEXUtil.getEdexSite();
        if (svcbuProps != null) {
            String propVal = svcbuProps.getProperty("PRIMARY_SITES", "").trim();
            if (!propVal.isEmpty()) {
                siteList = propVal;
            }
        }

        String[] sites = siteList.split(",");
        Set<String> retVal = new HashSet<String>(sites.length, 1.0f);
        Set<String> validSites = Sets.newHashSet(SiteAwareRegistry
                .getInstance().getActiveSites());
        for (String site : sites) {
            String siteId = site.trim().toUpperCase();
            if (!siteId.isEmpty()) {
                if (validSites.contains(siteId)) {
                    retVal.add(siteId);
                } else {
                    final String msg = "Service backup primary site "
                            + site
                            + " is not a currently activated site. Service backup and export grids tasks cannot be run for this site. Check the PRIMARY_SITES setting in svcbu.properties.";
                    statusHandler.warn(msg);
                }
            }
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
        String edexScriptsDir = getEdexScriptsDir();
        String hazardServicesScriptsDir = FileUtil.join(edexScriptsDir,
                "HazardServices", "ServiceBackup", "scripts");
        return (hazardServicesScriptsDir);
    }

    private static String getHazardServicesPropsFile() {
        String hazardServicesScriptsDir = getHazardServicesScriptsDir();
        String hazardServicesPropsFile = FileUtil.join(
                hazardServicesScriptsDir, "svcbu.properties");
        return (hazardServicesPropsFile);
    }

}
