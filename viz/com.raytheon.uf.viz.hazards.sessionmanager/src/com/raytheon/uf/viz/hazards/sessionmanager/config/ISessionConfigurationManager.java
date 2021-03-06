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
import java.util.Set;

import com.raytheon.uf.common.colormap.Color;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardEventFirstClassAttribute;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEventView;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IReadableHazardEvent;
import com.raytheon.uf.common.hazards.configuration.types.HazardTypes;
import com.raytheon.uf.viz.core.IGraphicsTarget.LineStyle;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.FilterIcons;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.HazardAlertsConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.types.ProductGeneratorTable;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.EventDrivenTools;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Field;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.HazardInfoConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ISettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.SettingsConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.StartUpConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

import gov.noaa.gsd.common.utilities.TimeResolution;

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
 * Sep 28, 2015 10302,8167 hansen       Added "getSettingsValue"
 * Nov 02, 2015 11864      Robert.Blum  Added getExpirationWindow().
 * Nov 10, 2015 12762      Chris.Golden Added recommender running in response to
 *                                      hazard event metadata changes, as well as the
 *                                      use of the new recommender manager.
 * Nov 17, 2015 11776      Roger.Ferrel Added {@link #containsUserLevelSettings()}
 * Nov 17, 2015  3473      mduff        Added getBackupSites().
 * Apr 01, 2016 16225      Chris.Golden Added ability to cancel tasks that are
 *                                      scheduled to run at regular intervals.
 * Apr 27, 2016 18266      Chris.Golden Added support for event-driven tools triggered
 *                                      by data layer changes.
 * Apr 28, 2016 18267      Chris.Golden Added support for unrestricted event start
 *                                      times.
 * May 04, 2016 18266      Chris.Golden Added passing of data time to method allowing
 *                                      triggering by data layer change.
 * May 12, 2016 16374      mduff        Added getFilterIcons.
 * Jul 08, 2016 13788      Chris.Golden Added validation of hazard events.
 * Jul 27, 2016 19924      Chris.Golden Removed obsolete code related to data layer
 *                                      changes triggering event-driven tools; the
 *                                      configuration of such is now read in within
 *                                      the configuration manager, but the work of
 *                                      tracking data layer changes is done by the
 *                                      app builder where it belongs.
 * Sep 27, 2016 15928      Chris.Golden Changed line thickness for hazard events.
 * Oct 05, 2016 22870      Chris.Golden Added support for event-driven tools triggered
 *                                      by frame changes.
 * Oct 06, 2016 22894      Chris.Golden Added method to get session attributes for a
 *                                      hazard type.
 * Oct 12, 2016 21873      Chris.Golden Added code to track the time resolutions of all 
 *                                      managed hazard events.
 * Feb 01, 2017 15556      Chris.Golden Added originator parameter for setting site ID.
 * Mar 21, 2017 29996      Robert.Blum  Added methods so Staging dialog can refreshMetadata.
 * Apr 17, 2017 33082      Robert.Blum  Validating multiple events at once.
 * May 17, 2017 34152      Robert.Blum  Fix Product Generation case that results in
 *                                      invalid products.
 * Dec 17, 2017 20739      Chris.Golden Refactored away access to directly mutable
 *                                      session events.
 * Feb 13, 2018 44514      Chris.Golden Removed event-modifying script code, as such
 *                                      scripts are not to be used.
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
     *            New site identifier.
     * @param originator
     *            Originator of the change.
     */
    public void setSiteID(String siteID, IOriginator originator);

    /**
     * Get the currently selected settings for the session.
     * 
     * @return
     */
    public S getSettings();

    /**
     * Save the current settings.
     * 
     * @return
     */
    public void saveSettings();

    /**
     * Delete the current settings from user level, or write an empty file if
     * only at base
     */
    public void deleteSettings();

    /**
     * Does the current setting contain a user level localization file?
     * 
     * @return
     */
    public boolean containsUserLevelSettings();

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
     * {@link ISessionEventManager#getMegawidgetSpecifiers(IHazardEventView)}
     * should be used if a cached copy of the megawidget specifier manager is
     * desired.
     * </p>
     * 
     * @param event
     *            Hazard event for which to retrieve the metadata.
     * @return Metadata.
     */
    public HazardEventMetadata getMetadataForHazardEvent(IHazardEvent event);

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
     * Get the getSettingsValue.
     * 
     * @return
     */
    public <T> T getSettingsValue(String identifier, ISettings settings);

    /**
     * Use color table to determine which color should be used for an event.
     * 
     * @param event
     * @return
     */
    public Color getColor(IReadableHazardEvent event);

    /**
     * Use color table to determine which color should be used for a persistent
     * shape.
     * 
     * @param identifier
     *            Identifier of the persistent shape.
     * @return Color for the persistent shape.
     */
    public Color getColor(String identifier);

    /**
     * Get the border width to use when displaying an event.
     * 
     * @param event
     * @param selected
     * @return
     */
    public double getBorderWidth(IReadableHazardEvent event, boolean selected);

    /**
     * Get the border style to use when displaying an event.
     * 
     * @param event
     * @return
     */
    public LineStyle getBorderStyle(IReadableHazardEvent event);

    /**
     * Get the headline from the hazardTypes configuration file for an event.
     * 
     * @param event
     * @return
     */
    public String getHeadline(IReadableHazardEvent event);

    /**
     * Get the default duration from the hazard types configuration file for an
     * event.
     * 
     * @param event
     *            Event for which to fetch the default duration.
     * @return Default duration in millliseconds.
     */
    public long getDefaultDuration(IReadableHazardEvent event);

    /**
     * Get the duration selector choices from the hazard types configuration
     * file for an event.
     * <p>
     * <strong>Note</strong>: The list of choices that is returned is the
     * complete list as specified for the event's type. If the list should be
     * pruned so that it only includes choices available for the event's current
     * status, {@link ISessionEventManager#getDurationChoices(IHazardEventView)}
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
    public List<String> getDurationChoices(IReadableHazardEvent event);

    /**
     * Get the time resolution used for the specified event.
     * 
     * @param event
     *            Event for which to get the time resolution.
     * @return Time resolution.
     */
    public TimeResolution getTimeResolution(IReadableHazardEvent event);

    /**
     * Get the identifier of the recommender that is triggered by a change to
     * the specified first-class property of the specified hazard event.
     * 
     * @param event
     *            Event that experienced a change.
     * @param change
     *            Change that occurred.
     * @return Recommender identifier to be run in response, or
     *         <code>null</code> if no recommender is triggered by this change.
     */
    public String getRecommenderTriggeredByChange(IReadableHazardEvent event,
            HazardEventFirstClassAttribute change);

    /**
     * Get the start-time-is-current-time flag from the hazard types
     * configuration file for an event.
     * 
     * @param event
     *            Event for which to fetch the flag.
     * @return True if the event's start time should be the CAVE current time.
     */
    public boolean isStartTimeIsCurrentTime(IReadableHazardEvent event);

    /**
     * Get the start-time-is-unrestricted flag from the hazard types
     * configuration file for an event.
     * 
     * @param event
     *            Event for which to fetch the flag.
     * @return True if the event's start time is unrestricted.
     */
    public boolean isAllowAnyStartTime(IReadableHazardEvent event);

    /**
     * Get allow-time-to-expand flag from the hazard types configuration file
     * for an event.
     * 
     * @param event
     *            Event for which to fetch the flag.
     * @return True if the (issued) event's end time should be allowed to be
     *         pushed farther into the future.
     */
    public boolean isAllowTimeExpand(IReadableHazardEvent event);

    /**
     * Get allow-time-to-shrink flag from the hazard types configuration file
     * for an event.
     * 
     * @param event
     *            Event for which to fetch the flag.
     * @return True if the (issued) event's end time should be allowed to be
     *         pushed closer to the start time.
     */
    public boolean isAllowTimeShrink(IReadableHazardEvent event);

    /**
     * Get the recommender identifier associated with the specified hazard type
     * for type-first hazard event creation.
     * 
     * @param hazardType
     *            Type of the hazard for which the type-first recommender is
     *            desired.
     * @return The identifier of the associated recommender, or
     *         <code>null</code> if there is none.
     */
    public String getTypeFirstRecommender(String hazardType);

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
     * Get the session attributes for the specified hazard event, if any.
     * Session attributes are those which are used to store information by
     * recommenders, etc. during a H.S. session, but which should not be
     * persisted.
     * 
     * @param hazardType
     *            Type of the hazard for which to fetch the session attributes.
     * @return List of attributes that are not to be persisted for this hazard
     *         type; may be empty.
     */
    public List<String> getSessionAttributes(String hazardType);

    /**
     * Get the hazard category from the hazardCategories configuration file for
     * an event.
     * 
     * @param event
     * @return
     */
    public String getHazardCategory(IReadableHazardEvent event);

    /**
     * Get the product generator table.
     */
    public ProductGeneratorTable getProductGeneratorTable();

    /**
     * Get the hazard types
     */
    public HazardTypes getHazardTypes();

    /**
     * Get the configured backup site list.
     * 
     * @return Array of backup sites
     */
    public String[] getBackupSites();

    /**
     * Get the event-driven tools.
     */
    public EventDrivenTools getEventDrivenTools();

    /**
     * Determine whether or not the event-driven tool running is currently
     * enabled.
     */
    public boolean isEventDrivenToolRunningEnabled();

    /**
     * Enable or disable event-driven tool running.
     */
    public void setEventDrivenToolRunningEnabled(boolean enable);

    /**
     * Trigger the appropriate tool sequence, if any, in response to a data
     * layer change.
     */
    public void triggerDataLayerChangeDrivenTool();

    /**
     * Trigger the appropriate tool sequence, if any, in response to a frame
     * change.
     */
    public void triggerFrameChangeDrivenTool();

    /**
     * Validate the specified hazard events.
     * 
     * @param hazardEvents
     *            Hazard events to be validated.
     * @return Description of validation problems if one or more of the hazard
     *         events are invalid, or <code>null</code> if the events validate
     *         properly.
     */
    public String validateHazardEvents(
            List<? extends IReadableHazardEvent> hazardEvents);

    /**
     * Get the expiration before/after minutes for the specified hazard event.
     * 
     * @param event
     *            Hazard event for which to fetch the expiration window.
     * @return Expiration before/after minutes.
     */
    public int[] getExpirationWindow(IReadableHazardEvent event);

    /**
     * Gets all the includeAll hazard types that share a generator with the
     * specified event.
     * 
     * @param newEvent
     *            Event for which to check for hazard types that share a
     *            generator with it.
     * @return Hazard types that share a generator with the event.
     */
    public Set<String> getIncludeAllHazards(IReadableHazardEvent newEvent);

    /**
     * Get the set of megawidget identifiers from the specified list, which may
     * contain raw specifiers and their descendants, of any megawidget
     * specifiers that include the specified parameter name.
     * 
     * @param list
     *            List to be checked.
     * @param parameterName
     *            Parameter name for which to search.
     * @return Set of megawidget identifiers that contain the specified
     *         parameter.
     */
    public Set<String> getMegawidgetIdentifiersWithParameter(List<?> list,
            String parameterName);

    /**
     * Get the file holding the script for the specified product.
     * 
     * @param product
     *            Product for which to find the script file.
     * @return Script file.
     */
    public File getScriptFile(String productGeneratorName);

    /**
     * Get the filter icon config data.
     * 
     * @return FilterIcons config data
     */
    public FilterIcons getFilterIcons();

    /**
     * Execute any shutdown needed.
     */
    public void shutdown();
}
