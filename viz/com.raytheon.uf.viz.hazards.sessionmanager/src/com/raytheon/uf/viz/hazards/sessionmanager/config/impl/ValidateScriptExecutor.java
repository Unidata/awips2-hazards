/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation and Decision Support (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.config.impl;

import java.util.List;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IReadableHazardEvent;

import jep.JepException;

/**
 * Description: Hazard event validation script executor.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 08, 2016   13788    Chris.Golden Initial creation.
 * Apr 17, 2017   33082    Robert.Blum  Validates multiple events.
 * Dec 17, 2017   20739    Chris.Golden Refactored away access to directly
 *                                      mutable session events.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ValidateScriptExecutor
        extends ContextuallyAwareScriptExecutor<String> {

    // Private Static Constants

    /**
     * Name of the variable used to store the hazard events in Python before
     * invoking the validate function.
     */
    private static final String HAZARD_EVENTS = "hazardEvents";

    /**
     * String used to invoke the validate function.
     */
    private static final String INVOKE_FUNCTION = "HazardServicesMetaDataRetriever.validate("
            + HAZARD_EVENTS + ")";

    // Private Variables

    /**
     * List of Hazard Events to be validated.
     */
    private final List<? extends IReadableHazardEvent> hazardEvents;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param hazardEvents
     *            Hazard events to be validated.
     */
    public ValidateScriptExecutor(
            List<? extends IReadableHazardEvent> hazardEvents) {
        this.hazardEvents = hazardEvents;
    }

    // Protected Methods

    @Override
    protected final boolean isContextuallyEqual(
            ContextuallyAwareScriptExecutor<?> other) {

        /*
         * Context switches should always be performed before a validate script
         * executor is invoked.
         */
        return false;
    }

    @Override
    public String doExecute(ContextSwitchingPythonEval script)
            throws JepException {
        script.set(HAZARD_EVENTS, hazardEvents);
        return (String) script.getValue(INVOKE_FUNCTION);
    }
}
