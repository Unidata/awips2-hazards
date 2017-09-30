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

import java.util.EnumSet;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionNotification;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings.Type;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.OriginatedSessionNotification;

import gov.noaa.gsd.common.utilities.IMergeable;
import gov.noaa.gsd.common.utilities.MergeResult;

/**
 * A notification that will be sent out to notify all components that the
 * settings have changed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 11, 2013    1257    bsteffen     Initial creation
 * Dec 05, 2014    4124    Chris.Golden Changed to work with parameterized config manager,
 *                                      and to include originator.
 * Feb 01, 2017   15556    Chris.Golden Changed to take set of changed settings elements
 *                                      in constructor, so that handlers of this message
 *                                      can take a more fine-grained approach to dealing
 *                                      with settings changes.
 * Sep 27, 2017   38072    Chris.Golden Implemented merge() method.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class SettingsModified extends OriginatedSessionNotification {

    // Private Variables

    /**
     * Configuration manager.
     */
    private final ISessionConfigurationManager<ObservedSettings> configManager;

    /**
     * Set of components of the settings that have changed.
     */
    private final Set<Type> changed;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param configManager
     *            Configuration manager.
     * @param changed
     *            Component of the settings that has changed.
     * @param originator
     *            Originator of the change.
     */
    public SettingsModified(
            ISessionConfigurationManager<ObservedSettings> configManager,
            Type changed, IOriginator originator) {
        super(originator);
        this.configManager = configManager;
        this.changed = ImmutableSet.of(changed);
    }

    /**
     * Construct a standard instance.
     * 
     * @param configManager
     *            Configuration manager.
     * @param changed
     *            Set of components of the settings that have changed.
     * @param originator
     *            Originator of the change.
     */
    public SettingsModified(
            ISessionConfigurationManager<ObservedSettings> configManager,
            Set<Type> changed, IOriginator originator) {
        super(originator);
        this.configManager = configManager;
        this.changed = ImmutableSet.copyOf(changed);
    }

    // Public Methods

    /**
     * Get the settings that changed.
     * 
     * @return Settings.
     */
    public ObservedSettings getSettings() {
        return configManager.getSettings();
    }

    /**
     * Get the set of components of the settings that have changed. Note that
     * the returned set is not modifiable.
     * 
     * @return Set of components.
     */
    public Set<Type> getChanged() {
        return changed;
    }

    @Override
    public MergeResult<ISessionNotification> merge(
            ISessionNotification original, ISessionNotification modified) {

        /*
         * If the new notification has the same originator as this one, and is
         * of the same type, merge the two together by combining their changes;
         * otherwise, the merge has failed.
         */
        if ((modified instanceof SettingsModified) && getOriginator()
                .equals(((SettingsModified) modified).getOriginator())) {

            Set<Type> changes = EnumSet.copyOf(getChanged());
            changes.addAll(((SettingsModified) modified).getChanged());
            return IMergeable.getSuccessObjectCancellationResult(
                    new SettingsModified(configManager, changes,
                            getOriginator()));
        }
        return IMergeable.getFailureResult();
    }
}
