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
 * Apr 27, 2016   18266    Chris.Golden Reworked to allow different trigger
 *                                      types, including the new data
 *                                      layer changed trigger.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class EventDrivenToolEntry {

    private ToolType toolType;

    private List<String> toolIdentifiers;

    private TriggerType triggerType;

    private int intervalMinutes;

    private List<DataLayerType> dataTypes;

    public ToolType getToolType() {
        return toolType;
    }

    public void setToolType(ToolType triggerType) {
        this.toolType = triggerType;
    }

    public List<String> getToolIdentifiers() {
        return Collections.unmodifiableList(toolIdentifiers);
    }

    public void setToolIdentifiers(List<String> toolIdentifiers) {
        this.toolIdentifiers = toolIdentifiers;
    }

    public TriggerType getTriggerType() {
        return triggerType;
    }

    public void setTriggerType(TriggerType triggerType) {
        this.triggerType = triggerType;
    }

    public int getIntervalMinutes() {
        return intervalMinutes;
    }

    public void setIntervalMinutes(int intervalMinutes) {
        this.intervalMinutes = intervalMinutes;
    }

    public List<DataLayerType> getDataTypes() {
        return Collections.unmodifiableList(dataTypes);
    }

    public void setDataTypes(List<DataLayerType> dataTypes) {
        this.dataTypes = dataTypes;
    }
}
