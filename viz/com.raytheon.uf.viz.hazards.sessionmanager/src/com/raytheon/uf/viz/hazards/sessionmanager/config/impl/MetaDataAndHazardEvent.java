/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.config.impl;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;

/**
 * Encapsulation of metadata returned to
 * {@link MetaDataScriptExecutor#doExecute(ContextSwitchingPythonEval)} from the
 * Python script it calls within its body. This is used because the metadata has
 * to be converted to a JSON string, but the {@link IHazardEvent} that may be
 * returned as part of the dictionary being converted cannot be JSONed. Thus,
 * they are returned as a pair of objects, one the JSON string, the other the
 * <code>IHazardEvent</code>.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Dec 15, 2017   40923    Chris.Golden Initial creation.
 * </pre>
 *
 * @author Chris.Golden
 */
public class MetaDataAndHazardEvent
        extends com.raytheon.uf.common.util.Pair<String, IHazardEvent> {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param metaDataAsJson
     *            Metadata as a JSON string.
     * @param modifiedHazardEvent
     *            Hazard event that was modified, or <code>null</code> if none
     *            was modified.
     */
    public MetaDataAndHazardEvent(String metaDataAsJson,
            IHazardEvent modifiedHazardEvent) {
        super(metaDataAsJson, modifiedHazardEvent);
    }
}
