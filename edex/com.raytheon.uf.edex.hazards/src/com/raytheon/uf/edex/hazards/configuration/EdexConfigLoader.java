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
package com.raytheon.uf.edex.hazards.configuration;

import java.util.HashMap;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.utilities.PythonBuildPaths;
import com.raytheon.uf.common.hazards.configuration.ConfigLoader;
import com.raytheon.uf.common.hazards.configuration.IServerConfigLookupWrapper;
import com.raytheon.uf.common.hazards.configuration.ServerConfigLookupProxy;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.edex.core.EDEXUtil;

/**
 *
 * Edex-side class responsible for reading a single configuration file.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 29, 2017 30536      kbisanz     Initial creation
 *
 * </pre>
 *
 * @author kbisanz
 * @param <T>
 *            Type of configuration to load
 */
public class EdexConfigLoader<T> {

    protected static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(EdexConfigLoader.class);

    private ConfigLoader<T> configLoader;

    public EdexConfigLoader(Class<T> clazz, String file, String dataType,
            IServerConfigLookupWrapper serviceConfigLookupWrapper) {
        ServerConfigLookupProxy.initInstance(serviceConfigLookupWrapper);

        Map<String, Object> configLoaderParams = new HashMap<>();
        configLoaderParams.put(HazardConstants.SITE, EDEXUtil.getEdexSite());
        configLoaderParams.put(HazardConstants.INCREMENTAL_OVERRIDE, true);
        configLoaderParams.put(HazardConstants.DATA_TYPE, dataType);

        IPathManager pathMgr = PathManagerFactory.getPathManager();
        LocalizationFile locFile = pathMgr.getStaticLocalizationFile(file);

        configLoader = new ConfigLoader<T>(locFile, clazz, null,
                PythonBuildPaths.buildIncludePath(),
                new HashMap<>(configLoaderParams));
    }

    /**
     * Get the configuration data from the file this object was constructed
     * with.
     *
     * @return Configuration data
     */
    public T getConfig() {
        return configLoader.getConfig();
    }
}
