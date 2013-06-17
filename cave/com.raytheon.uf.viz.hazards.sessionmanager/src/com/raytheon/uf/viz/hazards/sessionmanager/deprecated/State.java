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
package com.raytheon.uf.viz.hazards.sessionmanager.deprecated;

import org.apache.commons.lang.builder.ToStringBuilder;


/**
 * Reverse engineered to represent model state as JSON.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 21, 2013 1257       bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@Deprecated
public class State {

    private String[] potentialEvents;

    private String latestVisibleTime;

    private String[] leftoverEvents;

    private String earliestVisibleTime;

    private String addToSelected;

    private Event[] pendingEvents;

    private String lastSelectedEventID;

    private String[] selectedEventIDs;

    public String[] getPotentialEvents() {
        return potentialEvents;
    }

    public void setPotentialEvents(String[] potentialEvents) {
        this.potentialEvents = potentialEvents;
    }

    public String getLatestVisibleTime() {
        return latestVisibleTime;
    }

    public void setLatestVisibleTime(String latestVisibleTime) {
        this.latestVisibleTime = latestVisibleTime;
    }

    public String[] getLeftoverEvents() {
        return leftoverEvents;
    }

    public void setLeftoverEvents(String[] leftoverEvents) {
        this.leftoverEvents = leftoverEvents;
    }

    public String getEarliestVisibleTime() {
        return earliestVisibleTime;
    }

    public void setEarliestVisibleTime(String earliestVisibleTime) {
        this.earliestVisibleTime = earliestVisibleTime;
    }

    public String getAddToSelected() {
        return addToSelected;
    }

    public void setAddToSelected(String addToSelected) {
        this.addToSelected = addToSelected;
    }

    public Event[] getPendingEvents() {
        return pendingEvents;
    }

    public void setPendingEvents(Event[] pendingEvents) {
        this.pendingEvents = pendingEvents;
    }

    public String getLastSelectedEventID() {
        return lastSelectedEventID;
    }

    public void setLastSelectedEventID(String lastSelectedEventID) {
        this.lastSelectedEventID = lastSelectedEventID;
    }

    public String[] getSelectedEventIDs() {
        return selectedEventIDs;
    }

    public void setSelectedEventIDs(String[] selectedEventIDs) {
        this.selectedEventIDs = selectedEventIDs;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
