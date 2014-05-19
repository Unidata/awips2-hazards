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
package com.raytheon.uf.viz.hazards.sessionmanager.config;

import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;

import java.util.List;

import com.raytheon.uf.common.colormap.Color;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypes;
import com.raytheon.uf.viz.core.IGraphicsTarget.LineStyle;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardAlertsConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.ProductGeneratorTable;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Field;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.HazardInfoConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.SettingsConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.StartUpConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;

/**
 * Manages all settings and configuration files for a session.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 21, 2013 1257       bsteffen    Initial creation
 * Aug 01, 2013  1325      daniel.s.schaffer@noaa.gov     Added support for alerting
 * Apr 28, 2014 3556       bkowal      Updated to use the new hazards common 
 *                                     configuration plugin.
 * Apr 29, 2014 2925       Chris.Golden Added method to get a megawidget specifier
 *                                      manager for a given hazard event.
 * May 15, 2014 2925       Chris.Golden Removed hazard info options fetcher.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public interface ISessionConfigurationManager {

    /**
     * Switch the settings to a new settings ID.
     * 
     * @param settingsId
     */
    public void changeSettings(String settingsId);

    /**
     * Get the current siteID that is used for new Events.
     * 
     * @return
     */
    public String getSiteID();

    /**
     * Set a new site ID to be used for new Events, this should only be used if
     * a site is backing up another site.
     * 
     * @param siteID
     */
    public void setSiteID(String siteID);

    /**
     * Get the currently selected settings for the session.
     * 
     * @return
     */
    public Settings getSettings();

    /**
     * Save the current settings.
     * 
     * @return
     */
    public void saveSettings();

    /**
     * Get a list of the available settings a user could change to.
     * 
     * @return
     */
    public List<Settings> getAvailableSettings();

    /**
     * Get the startUpConfig. This object is currently stored in the
     * StartUpConfig localization file.
     * 
     * @return
     */
    public StartUpConfig getStartUpConfig();

    /**
     * Get the HazardInfoConfig
     * 
     * @return
     */
    public HazardInfoConfig getHazardInfoConfig();

    /**
     * Get the megawidget specifier manager for the specified hazard event.
     * <p>
     * <strong>Note</strong>: This method does not ever return a cached manager;
     * it creates a manager each time it is invoked. The method
     * {@link ISessionEventManager#getMegawidgetSpecifiers(IHazardEvent)} should
     * be used instead if caching behavior is desired.
     * </p>
     * 
     * @param hazardEvent
     *            Hazard event for which to retrieve the manager.
     * @return Megawidget specifier manager, holding specifiers for the
     *         megawidgets as well as any side effects applier to be used with
     *         the megawidgets.
     */
    public MegawidgetSpecifierManager getMegawidgetSpecifiersForHazardEvent(
            IHazardEvent hazardEvent);

    /**
     * Get the HazardAlertConfig
     * 
     * @param
     * @return
     */
    public HazardAlertsConfig getAlertConfig();

    /**
     * Get the FilterConfig.
     * 
     * @return
     */
    public Field[] getFilterConfig();

    /**
     * Get the SettingsConfig.
     * 
     * @return
     */
    public SettingsConfig getSettingsConfig();

    /**
     * Use color table to determine which color should be used for an event.
     * 
     * @param event
     * @return
     */
    public Color getColor(IHazardEvent event);

    /**
     * Get the border width to use when displaying an event.
     * 
     * @param event
     * @return
     */
    public int getBorderWidth(IHazardEvent event);

    /**
     * Get the border style to use when displaying an event.
     * 
     * @param event
     * @return
     */
    public LineStyle getBorderStyle(IHazardEvent event);

    /**
     * Get the headline from the hazardTypes configuration file for an event.
     * 
     * @param event
     * @return
     */
    public String getHeadline(IHazardEvent event);

    /**
     * Get the hazard category from the hazardCategories configuration file for
     * an event.
     * 
     * @param event
     * @return
     */
    public String getHazardCategory(IHazardEvent event);

    /**
     * Get the product generator table.
     */
    public ProductGeneratorTable getProductGeneratorTable();

    /**
     * Get the hazard types
     */
    public HazardTypes getHazardTypes();

    /**
     * Execute any shutdown needed.
     */
    public void shutdown();

}
