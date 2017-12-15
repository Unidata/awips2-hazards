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

import java.io.Serializable;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

import gov.noaa.gsd.common.utilities.JsonConverter;
import jep.JepException;

/**
 * Description: Metadata fetching configuration script executor.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 19, 2014    4243    Chris.Golden Initial creation.
 * Sep 12, 2016   15934    Chris.Golden Changed to work with JsonConverter
 *                                      static methods.
 * Dec 15, 2017   40923    Chris.Golden Added use of MetaDataAndHazardEvent
 *                                      for passing both the metadata 
 *                                      dictionary as JSON and the hazard
 *                                      event back to this class's
 *                                      doExecute().
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class MetaDataScriptExecutor
        extends ContextuallyAwareScriptExecutor<Map<String, Object>> {

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
        MetaDataAndHazardEvent result = (MetaDataAndHazardEvent) script
                .getValue(INVOKE_FUNCTION);
        Map<String, Object> metaData = null;
        try {
            metaData = JsonConverter.fromJson(result.getFirst());
        } catch (Exception e) {
            statusHandler.error("Could not get hazard metadata.", e);
            return null;
        }
        metaData.put(HazardConstants.MODIFIED_HAZARD_EVENT_KEY,
                result.getSecond());
        return metaData;
    }
}