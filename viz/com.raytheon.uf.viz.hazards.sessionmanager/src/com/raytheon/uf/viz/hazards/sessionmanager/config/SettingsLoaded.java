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

import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

/**
 * Notification that is sent out when the Settings have been loaded for the
 * first time.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 13, 2013    1257    bsteffen     Initial creation
 * Dec 05, 2014    4124    Chris.Golden Changed to work with parameterized config manager,
 *                                      and to include originator.
 * Feb 01, 2017   15556    Chris.Golden Changed to pass set of changed settings elements
 *                                      to superclass constructor.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class SettingsLoaded extends SettingsModified {

    public SettingsLoaded(
            ISessionConfigurationManager<ObservedSettings> manager,
            IOriginator originator) {
        super(manager, EnumSet.allOf(ObservedSettings.Type.class), originator);
    }

}
