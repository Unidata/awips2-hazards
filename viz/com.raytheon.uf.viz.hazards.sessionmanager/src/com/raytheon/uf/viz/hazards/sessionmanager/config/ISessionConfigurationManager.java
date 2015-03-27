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

import java.io.File;
import java.util.List;
import java.util.Map;

import com.raytheon.uf.common.colormap.Color;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypes;
import com.raytheon.uf.viz.core.IGraphicsTarget.LineStyle;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardAlertsConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.ProductGeneratorTable;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Field;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.HazardInfoConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ISettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.SettingsConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.StartUpConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Tool;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

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
 * Jul 03, 2014 3512       Chris.Golden Added ability to fetch duration choices for
 *                                      hazard events, and also default durations.
 * Aug 20, 2014 4243       Chris.Golden Added new method to run an event-modifying
 *                                      script.
 * Sep 16, 2014 4753       Chris.Golden Added mutable properties to event script.
 * Dec 05, 2014 4124       Chris.Golden Changed to have generic ISettings parameter,
 *                                      in support of proper use of ObservedSettings.
 * Jan 21, 2015 3626       Chris.Golden Added method to retrieve hazard-type-first
 *                                      recommender based upon hazard type.
 * Jan 29, 2015 4375       Dan Schaffer Console initiation of RVS product generation
 * Feb 01, 2015 2331       Chris.Golden Added methods to determine the value of flags
 *                                      indicating the constraints that a hazard event
 *                                      type puts on start and end time editability.
 * Mar 06, 2015 3850       Chris.Golden Added ability to determine if a hazard type
 *                                      requires a point identifier, and which hazard
 *                                      types can be used to replace a particular
 *                                      hazard event.
 * Apr 10, 2015 6898       Chris.Cody   Refactored async messaging
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public interface ISessionConfigurationManager<S extends ISettings> {

    /**
     * Switch the settings to a new settings ID.
     * 
     * @param settingsId
     * @param originator
     */
    public void changeSettings(String settingsId, IOriginator originator);

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
    public S getSettings();

    /**
     * Save the current settings.
     * 
     */
    public void saveSettings(IOriginator originator);

    /**
     * Save the current settings under a different Settings ID.
     * 
     * @param saveAsSettingsId
     *            New Settings ID
     * @param originator
     *            The UI component creating the new settings
     */
    public void saveAsSettings(String saveAsSettingsId, IOriginator originator);

    /**
     * Overwrite the settings with new values. Note: This should not persist the
     * changes. Dispatch update messages as necessary.
     * 
     * @param updateSettings
     * @param originator
     */
    public void updateCurrentSettings(ISettings updateSettings,
            IOriginator originator);

    /**
     * Delete the current settings from user level, or write an empty file if
     * only at base Dispatch update message.
     */
    public void deleteSettings();

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
     * Get the metadata for the specified hazard event.
     * <p>
     * <strong>Note</strong>: This method does not ever return a cached object;
     * it creates a new metadata object each time it is invoked. The method
     * {@link ISessionEventManager#getMegawidgetSpecifiers(IHazardEvent)} should
     * be used if a cached copy of the megawidget specifier manager is desired.
     * </p>
     * 
     * @param hazardEvent
     *            Hazard event for which to retrieve the metadata.
     * @return Metadata.
     */
    public HazardEventMetadata getMetadataForHazardEvent(
            IHazardEvent hazardEvent);

    /**
     * Run the event modifying script with the specified entry-point function
     * name.
     * 
     * @param hazardEvent
     *            Hazard event to which to apply the script.
     * @param scriptFile
     *            Script file in which to find the entry-point function.
     * @param functionName
     *            Name of the entry-point function.
     * @param mutableProperties
     *            Metadata megawidgets' mutable properties.
     * @param listener
     *            Listener to be notified if the event modifying script runs
     *            successfully.
     */
    public void runEventModifyingScript(IHazardEvent hazardEvent,
            File scriptFile, String functionName,
            Map<String, Map<String, Object>> mutableProperties,
            IEventModifyingScriptJobListener listener);

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
     * @param selected
     * @return
     */
    public int getBorderWidth(IHazardEvent event, boolean selected);

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
     * Get the default duration from the hazard types configuration file for an
     * event.
     * 
     * @param event
     *            Event for which to fetch the default duration.
     * @return Default duration in millliseconds.
     */
    public long getDefaultDuration(IHazardEvent event);

    /**
     * Get the duration selector choices from the hazard types configuration
     * file for an event.
     * <p>
     * <strong>Note</strong>: The list of choices that is returned is the
     * complete list as specified for the event's type. If the list should be
     * pruned so that it only includes choices available for the event's current
     * status, {@link ISessionEventManager#getDurationChoices(IHazardEvent)}
     * should be used instead.
     * </p>
     * 
     * @param event
     *            Event for which to fetch the duration selector choices.
     * @return List of choices; each of these is of the form given by the
     *         description of the
     *         {@link gov.noaa.gsd.viz.megawidgets.validators.SingleTimeDeltaStringChoiceValidatorHelper}
     *         class. If the specified event does not use a duration selector
     *         for its end time, an empty list is returned.
     */
    public List<String> getDurationChoices(IHazardEvent event);

    /**
     * Get the start-time-is-current-time flag from the hazard types
     * configuration file for an event.
     * 
     * @param event
     *            Event for which to fetch the flag.
     * @return True if the (unissued) event's start time should be the CAVE
     *         current time,
     */
    public boolean isStartTimeIsCurrentTime(IHazardEvent event);

    /**
     * Get allow-time-to-expand flag from the hazard types configuration file
     * for an event.
     * 
     * @param event
     *            Event for which to fetch the flag.
     * @return True if the (issued) event's end time should be allowed to be
     *         pushed farther into the future.
     */
    public boolean isAllowTimeExpand(IHazardEvent event);

    /**
     * Get allow-time-to-shrink flag from the hazard types configuration file
     * for an event.
     * 
     * @param event
     *            Event for which to fetch the flag.
     * @return True if the (issued) event's end time should be allowed to be
     *         pushed closer to the start time.
     */
    public boolean isAllowTimeShrink(IHazardEvent event);

    /**
     * Get the recommender identifier associated with the specified hazard type
     * for type-first hazard event creation.
     * 
     * @param hazardType
     *            Type of the hazard for which the type-first recommender is
     *            desired.
     * @return The associated recommender, or <code>null</code> if there is
     *         none.
     */
    public Tool getTypeFirstRecommender(String hazardType);

    /**
     * Determine whether or not the specified hazard type requires that the
     * hazard event have a point identifier.
     * 
     * @param hazardType
     *            Hazard event type to be checked.
     * @return True if the hazard event must have a point identifier to use this
     *         type, false otherwise.
     */
    public boolean isPointIdentifierRequired(String hazardType);

    /**
     * Get the list of hazard types that can be used to replace hazards of the
     * specified type.
     * 
     * @param hazardType
     *            Type of the hazard for which to fetch the replace-by types.
     * @return List of hazard types by which the specified type can be replaced;
     *         this may be an empty list.
     */
    public List<String> getReplaceByTypes(String hazardType);

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
