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

import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;

/**
 * Reverse engineered to represent temporal cononent data as JSON.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 20, 2013 1257       bsteffen    Initial creation
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@Deprecated
public class TemporalComponentData {

    private Settings dynamicSettings = new Settings();

    // TODO Im guessing these aren't really strings.
    private Event[] events = {};

    public TemporalComponentData() {

    }

    public TemporalComponentData(Settings dynamicSettings,
 Event[] events) {
        this.dynamicSettings = dynamicSettings;
        this.events = events;
    }

    public Event[] getEvents() {
        return events;
    }

    public void setEvents(Event[] events) {
        this.events = events;
    }

    public Settings getDynamicSettings() {
        return dynamicSettings;
    }

    public void setDynamicSettings(Settings dynamicSettings) {
        this.dynamicSettings = dynamicSettings;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
