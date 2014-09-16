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

import gov.noaa.gsd.common.utilities.JSONConverter;

import java.io.File;
import java.util.Collections;
import java.util.Map;

import jep.JepException;

import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardEventUtilities;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ModifiedHazardEvent;

/**
 * Description: Event modifying script executor.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 20, 2014    4243    Chris.Golden Initial creation.
 * Sep 16, 2014    4753    Chris.Golden Changed to include mutable properties.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class EventModifyingScriptExecutor extends
        ContextuallyAwareScriptExecutor<ModifiedHazardEvent> {

    // Private Static Constants

    /**
     * Name of the variable used to store the hazard event in Python before
     * invoking the event-modifying function.
     */
    private static final String HAZARD_EVENT = "hazardEvent";

    /**
     * Name of the variable used to store the extra data in Python before
     * invoking the event-modifying function.
     */
    private static final String EXTRA_DATA = "extraData";

    /**
     * Name of the variable used to store the function name in Python before
     * invoking the event-modifying function.
     */
    private static final String FUNCTION_NAME = "functionName";

    /**
     * String used to invoke the metadata-generating function.
     */
    private static final String INVOKE_FUNCTION = ConfigScriptFactory.EVENT_MODIFIER_FUNCTION_NAME
            + "("
            + FUNCTION_NAME
            + ", "
            + HAZARD_EVENT
            + ", "
            + EXTRA_DATA
            + ")";

    // Private Static Variables

    /**
     * Status handler, for displaying notifications to the user.
     */
    private static IUFStatusHandler statusHandler = UFStatus
            .getHandler(EventModifyingScriptExecutor.class);

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

    /**
     * Metadata megawidgets' mutable properties.
     */
    private final Map<String, Map<String, Object>> mutableProperties;

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
     * @param mutableProperties
     *            Metadata megawidgets' mutable properties to be passed to the
     *            script.
     */
    public EventModifyingScriptExecutor(IHazardEvent hazardEvent,
            File scriptFile, String functionName,
            Map<String, Map<String, Object>> mutableProperties) {
        this.hazardEvent = new BaseHazardEvent(hazardEvent);
        this.scriptFile = scriptFile;
        this.functionName = functionName;
        this.mutableProperties = mutableProperties;
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

    @SuppressWarnings("unchecked")
    @Override
    protected ModifiedHazardEvent doExecute(ContextSwitchingPythonEval script)
            throws JepException {
        script.run(scriptFile);
        script.set(HAZARD_EVENT, hazardEvent);
        script.set(FUNCTION_NAME, functionName);
        JSONConverter converter = new JSONConverter();
        script.set(EXTRA_DATA, converter.toJson(mutableProperties));
        IHazardEvent result = (IHazardEvent) script.getValue(INVOKE_FUNCTION);
        String jsonData = (result == null ? null : (String) result
                .getHazardAttribute(ConfigScriptFactory.EXTRA_DATA_ATTRIBUTE));
        Map<String, Map<String, Object>> modifiedMutableProperties = null;
        try {
            modifiedMutableProperties = (Map<String, Map<String, Object>>) (jsonData == null ? Collections
                    .emptyMap() : converter.fromJson(jsonData));
        } catch (Exception e) {
            statusHandler.error(
                    "Could not get convert mutable properties from JSON.", e);
            return null;
        }
        if (result != null) {
            result.removeHazardAttribute(ConfigScriptFactory.EXTRA_DATA_ATTRIBUTE);
        }
        return new ModifiedHazardEvent(result, modifiedMutableProperties);
    }
}