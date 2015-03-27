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
package com.raytheon.uf.viz.hazards.sessionmanager.config.impl;

import static com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ATTR_HAZARD_CATEGORY;

import java.util.Map.Entry;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.configuration.ConfigLoader;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardCategories;

/**
 * This class is used in place of passing a SessionConfigurationManager instance
 * to every ObservedSettings instance.
 * <p>
 * Messaging has been removed from ObservedSettings and the
 * SessionConfigurationManager contains messaging and more functionality and
 * dependencies than needed by the ObservedSettings Data container class.
 * <p>
 * This Helper class is needed by Json to fill in the Megawidgets.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 10, 2015 6898       Chris.Cody   Initial creation
 * </pre>
 * 
 * @author Chris.Cody
 * @version 1.0
 */

public class ObservedSettingsHelper {
    static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ObservedSettingsHelper.class);

    private static ObservedSettingsHelper helper = null;

    private ConfigLoader<HazardCategories> hazardCategories = null;

    private static final String hazardCategoriesFileName = "hazardServices/hazardCategories/HazardCategories.py";

    private ObservedSettingsHelper() {
        try {
            IPathManager pathManager = PathManagerFactory.getPathManager();
            LocalizationFile file = pathManager
                    .getStaticLocalizationFile(hazardCategoriesFileName);

            hazardCategories = new ConfigLoader<HazardCategories>(file,
                    HazardCategories.class);
        } catch (Exception ex) {
            String msg = "Exception Caught attempting to retrieve Hazard Event Utility: Categoy and Type Data for Settings.\n"
                    + "Check Localization File: " + hazardCategoriesFileName;
            statusHandler.error(msg, ex);
            hazardCategories = null;
        }
    }

    public static ObservedSettingsHelper getInstance() {
        if (helper == null) {
            helper = new ObservedSettingsHelper();
        }
        return (helper);
    }

    /**
     * Retrieve Hazard Category from Hazard Event.
     * 
     * Hazard Category is Hazard Attribute ATTR_HAZARD_CATEGORY when no
     * Phenomenon is set. Otherwise it is derived from Configured Hazard
     * Categories with values that match the Hazard Event values for Phenomenon,
     * Significance, and SubType.
     * 
     * @param event
     * @return
     */
    public String getHazardCategory(IHazardEvent event) {
        if (event != null) {
            if (event.getPhenomenon() == null) {
                return (String) event.getHazardAttribute(ATTR_HAZARD_CATEGORY);
            }
            for (Entry<String, String[][]> entry : hazardCategories.getConfig()
                    .entrySet()) {
                for (String[] str : entry.getValue()) {
                    if (str.length >= 2) {
                        if (event.getPhenomenon() == null) {
                            continue;
                        } else if (!event.getPhenomenon().equals(str[0])) {
                            continue;
                        } else if (event.getSignificance() == null) {
                            continue;
                        } else if (!event.getSignificance().equals(str[1])) {
                            continue;
                        }
                        if (str.length >= 3) {
                            if (event.getSubType() == null) {
                                continue;
                            } else if (!event.getSubType().equals(str[2])) {
                                continue;
                            }
                        }
                        return entry.getKey();
                    }
                }
            }
            String attrHazardCategory = (String) event
                    .getHazardAttribute(ATTR_HAZARD_CATEGORY);
            if (attrHazardCategory != null) {
                return (attrHazardCategory);
            }
        } else {
            String msg = "Unable to retrieve Hazard Event Utility: Categoy and Type Data for Settings.\n"
                    + "\tCheck Localization File: " + hazardCategoriesFileName;
            statusHandler.error(msg);
        }
        return ("");
    }
}
