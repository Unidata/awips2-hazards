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

import com.raytheon.uf.common.hazards.configuration.HazardsConfigurationConstants;
import com.raytheon.uf.common.hazards.configuration.IServerConfigLookupWrapper;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypes;
import com.raytheon.uf.common.localization.ILocalizationFile;
import com.raytheon.uf.common.localization.ILocalizationPathObserver;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 *
 * Edex-side class responsible for reading the various configuration files
 * required by an Edex process.
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
 */
public class EdexHazardConfigManager {

    protected static final IUFStatusHandler statusHandler = UFStatus
            .getHandler(EdexHazardConfigManager.class);

    private EdexConfigLoader<HazardTypes> hazardTypesLoader;

    private IServerConfigLookupWrapper serverConfig;

    /**
     * @param observeFiles
     *            True if the config files should be reread when they change.
     */
    public EdexHazardConfigManager(boolean observeFiles) {
        this(observeFiles, new EdexServerConfigLookupWrapper());
    }

    /**
     * @param observeFiles
     *            True if the config files should be reread when they change.
     * @param serverConfig
     *            Localization configuration data data,
     */
    public EdexHazardConfigManager(boolean observeFiles,
            IServerConfigLookupWrapper serverConfig) {
        hazardTypesLoader = createHazardTypesLoader(serverConfig);
        this.serverConfig = serverConfig;

        if (observeFiles) {
            addObservers();
        }
    }

    public HazardTypes getHazardTypes() {
        return hazardTypesLoader.getConfig();
    }

    private void addObservers() {
        IPathManager pathMgr = PathManagerFactory.getPathManager();

        class HazardTypesObserver implements ILocalizationPathObserver {
            @Override
            public void fileChanged(ILocalizationFile file) {
                statusHandler.info("fileChanged: " + file);
                /*
                 * File has changed, get a new loader.
                 */
                hazardTypesLoader = createHazardTypesLoader(serverConfig);
            }
        }
        pathMgr.addLocalizationPathObserver(
                HazardsConfigurationConstants.HAZARD_TYPES_PY,
                new HazardTypesObserver());

    }

    /**
     * @param serverConfig
     * @return Config loader for HazardTypes.py
     */
    private EdexConfigLoader<HazardTypes> createHazardTypesLoader(
            IServerConfigLookupWrapper serverConfig) {
        return new EdexConfigLoader<HazardTypes>(HazardTypes.class,
                HazardsConfigurationConstants.HAZARD_TYPES_PY,
                HazardsConfigurationConstants.HAZARD_TYPES_LOCALIZATION_DIR,
                serverConfig);
    }
}
