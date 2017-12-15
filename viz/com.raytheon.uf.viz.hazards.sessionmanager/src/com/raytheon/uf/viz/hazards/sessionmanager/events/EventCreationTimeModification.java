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
 * Modification of the creation time of an event.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Sep 21, 2017   38072    Chris.Golden Initial creation.
 * Dec 07, 2017   41886    Chris.Golden Removed Java 8/JDK 1.8 usage.
 * Dec 17, 2017   20739    Chris.Golden Refactored away access to directly
 *                                      mutable session events.
 * </pre>
 *
 * @author Chris.Golden
 */
public class EventCreationTimeModification implements IEventModification {

    @Override
    public void apply(IHazardEventView sourceEvent, IHazardEvent targetEvent) {
        targetEvent.setCreationTime(sourceEvent.getCreationTime());
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
