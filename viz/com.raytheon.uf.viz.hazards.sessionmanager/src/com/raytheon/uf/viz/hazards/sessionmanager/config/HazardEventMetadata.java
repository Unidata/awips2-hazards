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

import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;

import java.io.File;
import java.util.Map;
import java.util.Set;

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

    /**
     * Script file.
     */
    private final File scriptFile;

    /**
     * Map of event modifying script identifiers to their corresponding entry
     * point function names.
     */
    private final Map<String, String> eventModifyingFunctionNamesForIdentifiers;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param megawidgetSpecifierManager
     *            Megawidget specifier manager.
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
     * @param scriptFile
     *            File holding the script from which the metadata was produced.
     * @param eventModifyingFunctionNamesForIdentifiers
     *            Map of event modifying script identifiers to their
     *            corresponding entry point function names.
     */
    public HazardEventMetadata(
            MegawidgetSpecifierManager megawidgetSpecifierManager,
            Set<String> refreshTriggeringMetadataKeys,
            Set<String> overrideOldValuesMetadataKeys,
            Set<String> affectingModifyFlagMetadataKeys,
            Map<String, String> recommendersTriggeredForMetadataKeys,
            Set<String> editRiseCrestFallTriggeringMetadataKeys,
            File scriptFile,
            Map<String, String> eventModifyingFunctionNamesForIdentifiers) {
        this.megawidgetSpecifierManager = megawidgetSpecifierManager;
        this.recommendersTriggeredForMetadataKeys = recommendersTriggeredForMetadataKeys;
        this.refreshTriggeringMetadataKeys = refreshTriggeringMetadataKeys;
        this.overrideOldValuesMetadataKeys = overrideOldValuesMetadataKeys;
        this.affectingModifyFlagMetadataKeys = affectingModifyFlagMetadataKeys;
        this.editRiseCrestFallTriggeringMetadataKeys = editRiseCrestFallTriggeringMetadataKeys;
        this.scriptFile = scriptFile;
        this.eventModifyingFunctionNamesForIdentifiers = eventModifyingFunctionNamesForIdentifiers;
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

    /**
     * Get the script file.
     * 
     * @return Script file.
     */
    public final File getScriptFile() {
        return scriptFile;
    }

    /**
     * Get the map of event modifying script identifiers to their corresponding
     * entry point function names.
     * 
     * @return Map.
     */
    public final Map<String, String> getEventModifyingFunctionNamesForIdentifiers() {
        return eventModifyingFunctionNamesForIdentifiers;
    }
}
