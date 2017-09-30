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
package com.raytheon.uf.viz.hazards.sessionmanager.events;

import java.util.Map;

import com.google.common.collect.ImmutableMap;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

/**
 * A notification to be sent out through the session manager indicating that a
 * script command invocation resulted in extra data being returned.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Sep 16, 2014    4753    Chris.Golden Initial creation.
 * Sep 27, 2017   38072    Chris.Golden Marked as deprecated.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @deprecated Event scripts are no longer needed and should be removed.
 */
@Deprecated
public class SessionEventScriptExtraDataAvailable
        extends AbstractSessionEventModified {

    private final Map<String, Map<String, Object>> mutableProperties;

    public SessionEventScriptExtraDataAvailable(
            ISessionEventManager<ObservedHazardEvent> eventManager,
            ObservedHazardEvent event,
            Map<String, Map<String, Object>> mutableProperties,
            IOriginator originator) {
        super(eventManager, event, originator);
        this.mutableProperties = ImmutableMap.copyOf(mutableProperties);
    }

    public Map<String, Map<String, Object>> getMutableProperties() {
        return mutableProperties;
    }
}
