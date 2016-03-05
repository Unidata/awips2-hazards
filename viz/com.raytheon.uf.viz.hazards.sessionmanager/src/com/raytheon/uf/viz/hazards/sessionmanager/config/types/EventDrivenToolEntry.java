/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.config.types;

import java.util.Collections;
import java.util.List;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Entry in the event-driven tools list.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Nov 10, 2015   12762    Chris.Golden Initial creation.
 * Mar 04, 2016   15933    Chris.Golden Changed to allow specification of
 *                                      a sequence of tools to be run.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class EventDrivenToolEntry {

    @XmlJavaTypeAdapter(ToolTypeAdapter.class)
    private ToolType type;

    private List<String> identifiers;

    private int intervalMinutes;

    public ToolType getType() {
        return type;
    }

    public void setType(ToolType type) {
        this.type = type;
    }

    public List<String> getIdentifiers() {
        return Collections.unmodifiableList(identifiers);
    }

    public void setIdentifiers(List<String> identifiers) {
        this.identifiers = identifiers;
    }

    public int getIntervalMinutes() {
        return intervalMinutes;
    }

    public void setIntervalMinutes(int intervalMinutes) {
        this.intervalMinutes = intervalMinutes;
    }
}
