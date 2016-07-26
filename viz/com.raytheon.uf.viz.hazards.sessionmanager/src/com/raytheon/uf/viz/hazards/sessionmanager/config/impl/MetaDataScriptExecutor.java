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

import gov.noaa.gsd.common.utilities.JsonConverter;

import java.io.Serializable;
import java.util.Map;

import jep.JepException;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Description: Metadata fetching configuration script executor.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 19, 2014    4243    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class MetaDataScriptExecutor extends
        ContextuallyAwareScriptExecutor<Map<String, Object>> {

    // Private Static Constants

    /**
     * Name of the variable used to store the hazard event in Python before
     * invoking the metadata-generating function.
     */
    private static final String HAZARD_EVENT = "hazardEvent";

    /**
     * Name of the variable used to store the environmental dictionary in Python
     * before invoking the metadata-generating function.
     */
    private static final String ENVIRONMENTAL_DICT = "environmentDict";

    /**
     * String used to invoke the metadata-generating function.
     */
    private static final String INVOKE_FUNCTION = "HazardServicesMetaDataRetriever.getMetaData("
            + HAZARD_EVENT + ", " + ENVIRONMENTAL_DICT + ")";

    // Private Static Variables

    /**
     * Status handler, for displaying notifications to the user.
     */
    private static IUFStatusHandler statusHandler = UFStatus
            .getHandler(MetaDataScriptExecutor.class);

    // Private Variables

    /**
     * Hazard event for which to generate metadata.
     */
    private final IHazardEvent hazardEvent;

    /**
     * Map of environmental factors.
     */
    private final Map<String, Serializable> environment;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param hazardEvent
     *            Hazard event for which to fetch metadata.
     * @param environment
     *            Map of environmental information.
     */
    public MetaDataScriptExecutor(IHazardEvent hazardEvent,
            Map<String, Serializable> environment) {
        this.hazardEvent = hazardEvent;
        this.environment = environment;
    }

    // Protected Methods

    @Override
    protected final boolean isContextuallyEqual(
            ContextuallyAwareScriptExecutor<?> other) {

        /*
         * Context switches should always be performed before a metadata script
         * executor is invoked.
         */
        return false;
    }

    @Override
    public Map<String, Object> doExecute(ContextSwitchingPythonEval script)
            throws JepException {
        script.set(HAZARD_EVENT, hazardEvent);
        script.set(ENVIRONMENTAL_DICT, environment);
        String result = (String) script.getValue(INVOKE_FUNCTION);
        JsonConverter converter = new JsonConverter();
        try {
            return converter.fromJson(result);
        } catch (Exception e) {
            statusHandler.error("Could not get hazard metadata.", e);
            return null;
        }
    }
}