/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.config;

import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;

import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;

/**
 * Description: Encapsulation of the description of all metadata parameters for
 * a hazard event, including the megawidget specifiers, the path to the script
 * file, the set of metadata keys that should trigger a refresh of the metadata,
 * and any event modifying scripts.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 20, 2014    4243    Chris.Golden Initial creation.
 * Sep 04, 2014    4560    Chris.Golden Added the set of metadata keys that
 *                                      trigger a refresh of the metadata.
 * Feb 17, 2015    3847    Chris.Golden Added edit-rise-crest-fall metadata trigger.
 * Nov 10, 2015   12762    Chris.Golden Added recommender running in response to
 *                                      hazard event metadata changes.
 * Mar 27, 2017   15528    Chris.Golden Added gathering of set of metadata megawidget
 *                                      identifiers for which modification of their
 *                                      underlying values does not affect their
 *                                      enclosing hazard event's modify flag.
 * Jun 01, 2017   23056    Chris.Golden Added the set of metadata keys that are not
 *                                      to use previously existing values for those
 *                                      attributes, but rather are to use the default
 *                                      values given in their metadata definitions.
 * Dec 13, 2017   40923    Chris.Golden Added modified hazard event data member.
 * Feb 13, 2018   44514    Chris.Golden Removed event-modifying script code, as such
 *                                      scripts are not to be used.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class HazardEventMetadata {

    // Private Variables

    /**
     * Megawidget specifier manager.
     */
    private final MegawidgetSpecifierManager megawidgetSpecifierManager;

    /**
     * Hazard event that was modified during the metadata fetch, or
     * <code>null</code> if no modifications to the event for which the fetching
     * was being done occurred.
     */
    private final IHazardEvent modifiedHazardEvent;

    /**
     * Set of metadata keys that are to trigger a metadata reload when any one
     * of them is changed or invoked.
     */
    private final Set<String> refreshTriggeringMetadataKeys;

    /**
     * Set of metadata keys indicating those attributes that should always use
     * the values defined within the metadata definitions instead of any
     * existing values for those identifiers.
     */
    private final Set<String> overrideOldValuesMetadataKeys;

    /**
     * Set of metadata keys that, when their associated values are modified,
     * affect the enclosing hazard event modified flag.
     */
    private final Set<String> affectingModifyFlagMetadataKeys;

    /**
     * Map of metadata keys that are to trigger the running of recommenders when
     * any one of them is changed or invoked to the recommenders to be run.
     */
    private final Map<String, String> recommendersTriggeredForMetadataKeys;

    /**
     * Set of metadata keys that are to trigger the edit of rise-crest-fall
     * information when any one of them is changed or invoked.
     */
    private final Set<String> editRiseCrestFallTriggeringMetadataKeys;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param megawidgetSpecifierManager
     *            Megawidget specifier manager.
     * @param modifiedHazardEvent
     *            Hazard event that was modified during the metadata fetch, or
     *            <code>null</code> if no modifications to the event for which
     *            the fetching was being done occurred.
     * @param refreshTriggeringMetadataKeys
     *            Set of metadata keys that are to trigger a metadata reload
     *            when any one of them is changed or invoked.
     * @param overrideOldValuesMetadataKeys
     *            Set of metadata keys indicating which metadata variables
     *            should use the default values given by the metadata
     *            definitions instead of any existing values for their
     *            identifiers.
     * @param affectingModifyFlagMetadataKeys
     *            Set of metadata keys that, when their associated values are
     *            modified, affect the enclosing hazard event modified flag.
     * @param recommendersTriggeredForMetadataKeys
     *            Map of metadata keys that are to trigger the running of
     *            recommenders to the recommenders that are to be run.
     * @param editRiseCrestFallTriggeringMetadataKeys
     *            Set of metadata keys that are to trigger the editing of
     *            rise-crest-fall when any one of them is changed or invoked.
     */
    public HazardEventMetadata(
            MegawidgetSpecifierManager megawidgetSpecifierManager,
            IHazardEvent modifiedHazardEvent,
            Set<String> refreshTriggeringMetadataKeys,
            Set<String> overrideOldValuesMetadataKeys,
            Set<String> affectingModifyFlagMetadataKeys,
            Map<String, String> recommendersTriggeredForMetadataKeys,
            Set<String> editRiseCrestFallTriggeringMetadataKeys) {
        this.megawidgetSpecifierManager = megawidgetSpecifierManager;
        this.modifiedHazardEvent = modifiedHazardEvent;
        this.recommendersTriggeredForMetadataKeys = recommendersTriggeredForMetadataKeys;
        this.refreshTriggeringMetadataKeys = refreshTriggeringMetadataKeys;
        this.overrideOldValuesMetadataKeys = overrideOldValuesMetadataKeys;
        this.affectingModifyFlagMetadataKeys = affectingModifyFlagMetadataKeys;
        this.editRiseCrestFallTriggeringMetadataKeys = editRiseCrestFallTriggeringMetadataKeys;
    }

    // Public Methods

    /**
     * Get the megawidget specifier manager.
     * 
     * @return Megawidget specifier manager.
     */
    public final MegawidgetSpecifierManager getMegawidgetSpecifierManager() {
        return megawidgetSpecifierManager;
    }

    /**
     * Get the hazard event that was modified during the metadata fetch, if any
     * modifications were made to the event for which the fetching was being
     * done.
     * 
     * @return Modified hazard event, or <code>null</code> if no modifications
     *         were made to the event.
     */
    public final IHazardEvent getModifiedHazardEvent() {
        return modifiedHazardEvent;
    }

    /**
     * Get the set of metadata keys that, when changed or invoked, are to
     * trigger a metadata reload.
     * 
     * @return Set of metadata keys.
     */
    public final Set<String> getRefreshTriggeringMetadataKeys() {
        return refreshTriggeringMetadataKeys;
    }

    /**
     * Get the set of metadata keys indicating the attributes that should always
     * use the values defined in their metadata definitions rather than any
     * existing old values for their identifiers.
     * 
     * @return Set of metadata keys.
     */
    public final Set<String> getOverrideOldValuesMetadataKeys() {
        return overrideOldValuesMetadataKeys;
    }

    /**
     * Get the set of metadata keys that when changed do not affect the
     * enclosing hazard event's modified flag.
     * 
     * @return Set of metadata keys.
     */
    public final Set<String> getAffectingModifyFlagMetadataKeys() {
        return affectingModifyFlagMetadataKeys;
    }

    /**
     * Get the map of metadata keys that are to trigger the running of a
     * recommender when any one of them is changed or invoked to the
     * recommenders to be run.
     * 
     * @return Map of metadata keys to recommender identifiers.
     */
    public final Map<String, String> getRecommendersTriggeredForMetadataKeys() {
        return recommendersTriggeredForMetadataKeys;
    }

    /**
     * Get the set of metadata keys that, when changed or invoked, are to
     * trigger the editing of rise-crest-fall information.
     * 
     * @return Set of metadata keys.
     */
    public final Set<String> getEditRiseCrestFallTriggeringMetadataKeys() {
        return editRiseCrestFallTriggeringMetadataKeys;
    }
}
