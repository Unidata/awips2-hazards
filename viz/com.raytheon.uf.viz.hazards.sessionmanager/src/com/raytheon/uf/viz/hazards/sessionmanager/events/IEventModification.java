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

/**
 * Interface describing the methods that must be implemented by classes that are
 * to encapsulate a modification to a hazard event.
 * 
 * TODO: When moving to Java 8, uncomment the commented out block at the end of
 * this interface.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 20, 2017   38072    Chris.Golden Initial creation.
 * Dec 07, 2017   41886    Chris.Golden Removed Java 8/JDK 1.8 usage.
 * Dec 17, 2017   20739    Chris.Golden Refactored away access to directly
 *                                      mutable session events.
 * </pre>
 *
 * @author Chris.Golden
 */
public interface IEventModification extends IMergeable<IEventModification> {

    /**
     * Apply this modification to the specified target event, using the
     * specified source event as the place from which to take any values from
     * that are needed to perform the modification.
     * 
     * @param sourceEvent
     *            Event from which to take values needed for the modification.
     * @param targetEvent
     *            Event to which to apply the modification.
     */
    public void apply(IHazardEventView sourceEvent, IHazardEvent targetEvent);
    //
    // /*
    // * If this method is not overridden in implementations, it returns a merge
    // * result indicating that this modification has been nullified if the
    // other
    // * modification is of the same subclass.
    // */
    // @Override
    // default public MergeResult<? extends IEventModification> merge(
    // IEventModification original, IEventModification modified) {
    // if (getClass().isAssignableFrom(original.getClass())) {
    // return IMergeable.getSuccessSubjectCancellationResult(modified);
    // }
    // return IMergeable.getFailureResult();
    // }
}
