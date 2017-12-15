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

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEventView;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;

/**
 * Base class for notifications that will be sent out to notify all components
 * that an event in the session has changed in some way.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 11, 2013    1257    bsteffen     Initial creation.
 * Sep 27, 2017   38072    Chris.Golden Changed to make it explicitly abstract.
 * Dec 17, 2017   20739    Chris.Golden Refactored away access to directly
 *                                      mutable session events.
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public abstract class AbstractSessionEventModified
        extends SessionEventsModified {

    // Private Variables

    /**
     * Event that has been modified.
     */
    private final IHazardEventView event;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param eventManager
     *            Event manager.
     * @param event
     *            Event that has been modified.
     * @param originator
     *            Originator of the change.
     */
    public AbstractSessionEventModified(ISessionEventManager eventManager,
            IHazardEventView event, IOriginator originator) {
        super(eventManager, originator);
        this.event = event;
    }

    // Public Methods

    /**
     * Get the event that has been modified.
     *
     * @return Event that has been modified.
     */
    public IHazardEventView getEvent() {
        return event;
    }
}
