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

import gov.noaa.gsd.common.utilities.JsonConverter;
import jep.JepException;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Description: Hazard event validation script executor.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 08, 2016   13788    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ValidateScriptExecutor extends
        ContextuallyAwareScriptExecutor<String> {

    // Private Static Constants

    /**
     * Name of the variable used to store the hazard event in Python before
     * invoking the validate function.
     */
    private static final String HAZARD_EVENT = "hazardEvent";

    /**
     * String used to invoke the validate function.
     */
    private static final String INVOKE_FUNCTION = "HazardServicesMetaDataRetriever.validate("
            + HAZARD_EVENT + ")";

    // Private Static Variables

    /**
     * Status handler, for displaying notifications to the user.
     */
    private static IUFStatusHandler statusHandler = UFStatus
            .getHandler(ValidateScriptExecutor.class);

    // Private Variables

    /**
     * Hazard event to be validated.
     */
    private final IHazardEvent hazardEvent;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param hazardEvent
     *            Hazard event to be validated.
     */
    public ValidateScriptExecutor(IHazardEvent hazardEvent) {
        this.hazardEvent = hazardEvent;
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
        script.set(HAZARD_EVENT, hazardEvent);
        String result = (String) script.getValue(INVOKE_FUNCTION);
        try {
            return JsonConverter.fromJson(result);
        } catch (Exception e) {
            statusHandler.error("Could not validate hazard event.", e);
            return null;
        }
    }
}
