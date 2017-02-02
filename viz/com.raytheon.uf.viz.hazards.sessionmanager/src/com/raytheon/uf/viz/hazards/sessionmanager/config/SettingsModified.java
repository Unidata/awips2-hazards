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

import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings.Type;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.OriginatedSessionNotification;

/**
 * A notification that will be sent out through the SessionManager to notify all
 * components that the settings have changed.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 11, 2013 1257       bsteffen     Initial creation
 * Dec 05, 2014 4124       Chris.Golden Changed to work with parameterized config manager,
 *                                      and to include originator.
 * Feb 01, 2017 15556      Chris.Golden Changed to take set of changed settings elements
 *                                      in constructor, so that handlers of this message
 *                                      can take a more fine-grained approach to dealing
 *                                      with settings changes.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */

public class SettingsModified extends OriginatedSessionNotification {

    private final ISessionConfigurationManager<ObservedSettings> configManager;

    private final Set<Type> changed;

    public SettingsModified(
            ISessionConfigurationManager<ObservedSettings> configManager,
            Type changed, IOriginator originator) {
        super(originator);
        this.configManager = configManager;
        this.changed = EnumSet.of(changed);
    }

    public SettingsModified(
            ISessionConfigurationManager<ObservedSettings> configManager,
            Set<Type> changed, IOriginator originator) {
        super(originator);
        this.configManager = configManager;
        this.changed = changed;
    }

    public ObservedSettings getSettings() {
        return configManager.getSettings();
    }

    public Set<Type> getChanged() {
        return changed;
    }
}
