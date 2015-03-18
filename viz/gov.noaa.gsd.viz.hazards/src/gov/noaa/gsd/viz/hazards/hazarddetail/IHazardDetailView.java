/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.hazarddetail;

import gov.noaa.gsd.common.utilities.ICurrentTimeProvider;
import gov.noaa.gsd.viz.hazards.hazarddetail.HazardDetailPresenter.Command;
import gov.noaa.gsd.viz.hazards.hazarddetail.HazardDetailPresenter.DisplayableEventIdentifier;
import gov.noaa.gsd.viz.mvp.widgets.IChoiceStateChanger;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;
import gov.noaa.gsd.viz.mvp.widgets.IQualifiedStateChanger;
import gov.noaa.gsd.viz.mvp.widgets.IStateChanger;

import java.util.Map;

import com.google.common.collect.Range;
import com.raytheon.uf.common.time.TimeRange;

/**
 * Description: Interface describing the methods that must be implemented by a
 * class that functions as a hazard detail view.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 09, 2014    2925    Chris.Golden Initial creation.
 * Jun 25, 2014    4009    Chris.Golden Added code to cache extra data held by
 *                                      metadata megawidgets between view
 *                                      instantiations.
 * Jul 03, 2014    3512    Chris.Golden Added code to allow a duration selector
 *                                      to be displayed instead of an absolute
 *                                      date/time selector for the end time of
 *                                      a hazard event.
 * Aug 15, 2014    4243    Chris.Golden Added notifier invoker fetcher, to
 *                                      allow the invoker used to receive
 *                                      notifications generated by notifier
 *                                      megawidgets.
 * Sep 16, 2014    4753    Chris.Golden Changed event script running to include
 *                                      mutable properties.
 * Feb 03, 2015    2331    Chris.Golden Added support for limiting the values
 *                                      that an event's start or end time can
 *                                      take on.
 * Mar 06, 2015    3850    Chris.Golden Added code to make the category and type
 *                                      lists change according to whether the
 *                                      event being shown has a point ID (if not
 *                                      yet issued), or what it can be replaced
 *                                      by (if issued).
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IHazardDetailView {

    // Public Enumerated Types

    /**
     * Time range boundary components.
     */
    public enum TimeRangeBoundary {
        START, END
    };

    // Public Methods

    /**
     * Initialize the view.
     * 
     * @param minVisibleTime
     *            Minimum visible time to be shown in the time widgets.
     * @param maxVisibleTime
     *            Maximum visible time to be shown in the time widgets.
     * @param currentTimeProvider
     *            Current time provider, used for the time range widgets.
     * @param extraDataForEventIdentifiers
     *            Map pairing event identifiers with any extra data they may
     *            have used in previous view instantiations, allowing such data
     *            to persist between different views.
     */
    public void initialize(
            long minVisibleTime,
            long maxVisibleTime,
            ICurrentTimeProvider currentTimeProvider,
            Map<String, Map<String, Map<String, Object>>> extraDataForEventIdentifiers);

    /**
     * Get the visible time range state changer. The identifier is ignored.
     * 
     * @return Visible time range state changer.
     */
    public IStateChanger<String, TimeRange> getVisibleTimeRangeChanger();

    /**
     * Get the selected event state changer. The identifier is ignored.
     * 
     * @return Selected event state changer.
     */
    public IChoiceStateChanger<String, String, String, DisplayableEventIdentifier> getVisibleEventChanger();

    /**
     * Get the category state changer. The identifier is that of the hazard
     * event.
     * 
     * @return Category state changer.
     */
    public IChoiceStateChanger<String, String, String, String> getCategoryChanger();

    /**
     * Get the type state changer. The identifier is that of the hazard event.
     * 
     * @return Type state changer.
     */
    public IChoiceStateChanger<String, String, String, String> getTypeChanger();

    /**
     * Get the time range state changer. The identifier is that of the hazard
     * event.
     * 
     * @return Time range state changer.
     */
    public IStateChanger<String, TimeRange> getTimeRangeChanger();

    /**
     * Get the time range boundaries state changer. The qualifier is that of the
     * hazard event, while the identifier indicates which boundary.
     * <p>
     * <strong>Note</strong>: This class uses a {@link Range} instead of a
     * {@link TimeRange} because the latter is not intended to have a
     * zero-length interval between its start and end times (such an instance is
     * considered invalid by its {@link TimeRange#isInvalid()} method). However,
     * the allowable boundaries for event start and end times may have
     * zero-length intervals.
     * </p>
     * 
     * @return Time range state changer.
     */
    public IQualifiedStateChanger<String, TimeRangeBoundary, Range<Long>> getTimeRangeBoundariesChanger();

    /**
     * Get the duration changer. This is only used to set the choices for
     * possible durations; changes in the chosen duration are handled as time
     * range changes. The identifier is that of the hazard event.
     * 
     * @return Duration changer.
     */
    public IChoiceStateChanger<String, String, String, String> getDurationChanger();

    /**
     * Get the metadata state changer. The qualifier is the identifier of the
     * hazard event, while the identifier is that of the metadata.
     * 
     * @return Metadata state changer.
     */
    public IMetadataStateChanger getMetadataChanger();

    /**
     * Get the notifier invoker. The identifier is that of the hazard event
     * coupled with that of the notifier.
     * 
     * @return Notifier invoker.
     */
    public ICommandInvoker<EventScriptInfo> getNotifierInvoker();

    /**
     * Get the button invoker. The identifier is the command.
     * 
     * @return Button invoker.
     */
    public ICommandInvoker<Command> getButtonInvoker();
}
