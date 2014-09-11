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
package com.raytheon.uf.common.localization;

import java.io.File;
import java.io.IOException;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;

import com.raytheon.uf.common.localization.TestPathManager.TestLocalizationAdapter;
import com.raytheon.uf.common.util.FileUtil;
import com.raytheon.uf.common.util.TestUtil;

/**
 * Utility class to initialize the test {@link IPathManager} implementation.
 * This allows tests to lookup baselined localization files.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 18, 2012 740        djohnson     Initial creation
 * Oct 23, 2012 1286       djohnson     Handle executing tests in Eclipse/command-line transparently.
 * Apr 18, 2013 1914       djohnson     Allow initializing test localization support from Spring.
 * Jan 08, 2014 2615       bgonzale     Fixes for PropertiesFactory configuration loading in test.
 * Jul 28, 2014 3214       jsanchez     PathManager's fileCache is no longer static. Removed the call to fix junit tests.
 * Sep 10, 2014 4703       Robert.Blum  Fixes for PathManagerFactory errors caused by changes in AWIPS2 baseline.
 * </pre>
 * 
 * @author djohnson
 * @version 1.0
 */

public class PathManagerFactoryTest implements BeanFactoryPostProcessor {

    private static File savedLocalizationFileDir;

    /**
     * Creates a test-only PathManager that can be used during tests.
     */
    public static void initLocalization() {
        initLocalization("OAX");
    }

    /**
     * Creates a test-only PathManager that can be used during tests, it is
     * configured for the specified site.
     */
    public static void initLocalization(final String site) {

        File file = TestUtil.setupTestClassDir(PathManagerFactoryTest.class);
        savedLocalizationFileDir = new File(file, "data");
        savedLocalizationFileDir = new File(savedLocalizationFileDir, "utility");
        savedLocalizationFileDir.mkdirs();

        // But only install the path manager if the test version is not already
        // installed
        if (!(PathManagerFactory.getPathManager() instanceof TestPathManager)) {
            TestLocalizationAdapter adapter = (isRunningInEclipse()) ? new EclipseTestLocalizationAdapter(
                    site, savedLocalizationFileDir)
                    : new CommandLineTestLocalizationAdapter(site,
                            savedLocalizationFileDir);
            PathManagerFactory.setAdapter(adapter);

            System.setProperty("edex.home", file.getAbsolutePath());
            File confResDataDir = new File(file, "conf/res");
            confResDataDir.mkdirs();
            File confResTestDataDir = new File("conf/res/");

            try {
                FileUtil.copyDirectory(confResTestDataDir, confResDataDir);
            } catch (IOException e) {
                throw new RuntimeException(
                        "Failed to setup test configuration directory conf/res",
                        e);
            }

        }
    }

    /**
     * Returns true if the JUnit test is running in Eclipse.
     * 
     * @return true if running in Eclipse
     */
    private static boolean isRunningInEclipse() {
        return new File("..", "edexOsgi").isDirectory();
    }

    /**
     * Initializes test localization support before any Spring beans are
     * created.
     * 
     * @param beanFactory
     *            the bean factory
     * @throws BeansException
     *             shouldn't happen
     */
    @Override
    public void postProcessBeanFactory(
            ConfigurableListableBeanFactory beanFactory) throws BeansException {
        PathManagerFactoryTest.initLocalization();
    }
}
