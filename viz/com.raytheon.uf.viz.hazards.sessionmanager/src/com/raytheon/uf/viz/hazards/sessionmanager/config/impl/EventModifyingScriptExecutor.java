/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.config.impl;

import java.io.File;

import jep.JepException;

import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;

/**
 * Description: Event modifying script executor.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 20, 2014    4243    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class EventModifyingScriptExecutor extends
        ContextuallyAwareScriptExecutor<IHazardEvent> {

    // Private Static Constants

    /**
     * Name of the variable used to store the hazard event in Python before
     * invoking the event-modifying function.
     */
    private static final String HAZARD_EVENT = "hazardEvent";

    /**
     * Name of the variable used to store the function name in Python before
     * invoking the event-modifying function.
     */
    private static final String FUNCTION_NAME = "functionName";

    /**
     * String used to invoke the metadata-generating function.
     */
    private static final String INVOKE_FUNCTION = ConfigScriptFactory.EVENT_MODIFIER_FUNCTION_NAME
            + "(" + FUNCTION_NAME + ", " + HAZARD_EVENT + ")";

    // Private Variables

    /**
     * Hazard event to which to apply the event modifying script.
     */
    private final IHazardEvent hazardEvent;

    /**
     * Script file.
     */
    private final File scriptFile;

    /**
     * Function name serving as the entry point to the event modifying script.
     */
    private final String functionName;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param hazardEvent
     *            Hazard event to which to apply the event modifying script.
     * @param scriptFile
     *            Script file in which the entry point is found.
     * @param functionName
     *            Function name to serve as the entry point to the event
     *            modifying script.
     */
    public EventModifyingScriptExecutor(IHazardEvent hazardEvent,
            File scriptFile, String functionName) {
        this.hazardEvent = hazardEvent;
        this.scriptFile = scriptFile;
        this.functionName = functionName;
    }

    // Protected Methods

    @Override
    protected final boolean isContextuallyEqual(
            ContextuallyAwareScriptExecutor<?> other) {
        if ((other instanceof EventModifyingScriptExecutor) == false) {
            return false;
        }
        EventModifyingScriptExecutor otherExecutor = (EventModifyingScriptExecutor) other;
        if (functionName.equals(otherExecutor.functionName) == false) {
            return false;
        }
        String hazardType = HazardEventUtilities.getHazardType(hazardEvent);
        String otherHazardType = HazardEventUtilities
                .getHazardType(otherExecutor.hazardEvent);
        return ((hazardType == otherHazardType) || ((hazardType != null) && hazardType
                .equals(otherHazardType)));
    }

    @Override
    protected IHazardEvent doExecute(ContextSwitchingPythonEval script)
            throws JepException {
        script.run(scriptFile);
        script.set(HAZARD_EVENT, hazardEvent);
        script.set(FUNCTION_NAME, functionName);
        return (IHazardEvent) script.getValue(INVOKE_FUNCTION);
    }
}