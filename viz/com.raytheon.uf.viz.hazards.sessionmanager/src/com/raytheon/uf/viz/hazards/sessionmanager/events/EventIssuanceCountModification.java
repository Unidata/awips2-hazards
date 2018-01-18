/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.events;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEventView;

import gov.noaa.gsd.common.utilities.IMergeable;
import gov.noaa.gsd.common.utilities.MergeResult;

/**
 * Modification of the issuance count of an event.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jan 26, 2018   33428    Chris.Golden Initial creation.
 * </pre>
 *
 * @author Chris.Golden
 */
public class EventIssuanceCountModification implements IEventModification {

    @Override
    public void apply(IHazardEventView sourceEvent, IHazardEvent targetEvent) {
        targetEvent.setIssuanceCount(sourceEvent.getIssuanceCount());
    }

    /*
     * TODO: Remove this when moving to Java 8, as the interface will have this
     * as a default method.
     */
    @Override
    @Deprecated
    public MergeResult<? extends IEventModification> merge(
            IEventModification original, IEventModification modified) {
        if (getClass().isAssignableFrom(original.getClass())) {
            return IMergeable.Helper
                    .getSuccessSubjectCancellationResult(modified);
        }
        return IMergeable.Helper.getFailureResult();
    }
}
