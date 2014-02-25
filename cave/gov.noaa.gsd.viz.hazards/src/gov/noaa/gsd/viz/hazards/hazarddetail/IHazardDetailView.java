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

import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;
import gov.noaa.gsd.viz.mvp.IView;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;

/**
 * Interface describing the methods that must be implemented by a class that
 * functions as a hazard detail view, managed by an hazard detail presenter.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * May 10, 2013            Chris.Golden      Change to Eclipse view implementation.
 * Nov 14, 2013            Bryon.Lawrence    Added code to support hazard conflict 
 *                                           detection.
 * Feb 19, 2014    2161    Chris.Golden      Added passing of set of events allowing
 *                                           "until further notice" to the view
 *                                           during initialization.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IHazardDetailView<C, E extends Enum<E>> extends IView<C, E> {

    // Public Methods

    /**
     * Initialize the view.
     * 
     * @param presenter
     *            Presenter managing this view.
     * @param jsonGeneralWidgets
     *            JSON string holding a dictionary that specifies the general
     *            widgets for the dialog.
     * @param jsonMetadataWidgets
     *            JSON string holding a list of dictionaries specifying
     *            megawidgets for the metadata specific to each hazard type.
     * @param minVisibleTime
     *            Minimum visible time to be shown in the time megawidgets.
     * @param maxVisibleTime
     *            Maximum visible time to be shown in the time megawidgets.
     * @param eventIdentifiersAllowingUntilFurtherNotice
     *            Set of the hazard event identifiers that at any given moment
     *            allow the toggling of their "until further notice" mode. The
     *            set is unmodifiable; attempts to modify it will result in an
     *            {@link UnsupportedOperationException}. Note that this set is
     *            kept up-to-date, and thus will always contain only those
     *            events that can have their "until further notice" mode toggled
     *            at the instant at which it is checked.
     */
    public void initialize(HazardDetailPresenter presenter,
            String jsonGeneralWidgets, String jsonMetadataWidgets,
            long minVisibleTime, long maxVisibleTime,
            Set<String> eventIdentifiersAllowingUntilFurtherNotice);

    /**
     * Show the hazard detail subview.
     * 
     * @param eventValuesList
     *            List of dictionaries, each holding key-value pairs that
     *            specify a hazard event.
     * @param topEventID
     *            Identifier for the hazard event that should be foregrounded
     *            with respect to other hazard events; must be one of the
     *            identifiers in the hazard events of
     *            <code>eventValuesList</code>.
     * @param force
     *            Flag indicating whether or not to force the showing of the
     *            subview. This may be used as a hint by views if they are
     *            considering not showing the subview for whatever reason.
     * @param eventConflictMap
     *            Map of selected events and lists of corresponding conflicting
     *            events
     */
    public void showHazardDetail(DictList eventValuesList, String topEventID,
            Map<String, Collection<IHazardEvent>> eventConflictMap,
            boolean force);

    /**
     * Update the hazard detail subview, if it is showing.
     * 
     * @param eventValuesList
     *            List of dictionaries, each holding key-value pairs that
     *            specify a hazard event.
     * @param topEventID
     *            Identifier for the hazard event that should be foregrounded
     *            with respect to other hazard events; must be one of the
     *            identifiers in the hazard events of
     *            <code>eventValuesList</code>.
     * @param eventConflictMap
     *            Contains a map of event ids and associated lists of
     *            conflicting hazards.
     */
    public void updateHazardDetail(DictList eventValuesList, String topEventID,
            Map<String, Collection<IHazardEvent>> eventConflictMap);

    /**
     * Hide the hazard detail subview.
     * 
     * @param force
     *            Flag indicating whether or not to force the hiding of the
     *            subview. This may be used as a hint by views if they are
     *            considering not hiding the subview for whatever reason.
     */
    public void hideHazardDetail(boolean force);

    /**
     * Set the visible time range.
     * 
     * @param minVisibleTime
     *            Minimum visible time to be shown in the time megawidgets.
     * @param maxVisibleTime
     *            Maximum visible time to be shown in the time megawidgets.
     */
    public void setVisibleTimeRange(long minVisibleTime, long maxVisibleTime);

}
