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
 * 
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
     */
    public void initialize(HazardDetailPresenter presenter);

    /**
     * Show the hazard detail subview.
     * 
     * @param jsonGeneralWidgets
     *            JSON string holding a dictionary that specifies the general
     *            widgets for the dialog.
     * @param jsonMetadataWidgets
     *            JSON string holding a list of dictionaries specifying
     *            megawidgets for the metadata specific to each hazard type.
     * @param eventValuesList
     *            List of dictionaries, each holding key-value pairs that
     *            specify a hazard event.
     * @param topEventID
     *            Identifier for the hazard event that should be foregrounded
     *            with respect to other hazard events; must be one of the
     *            identifiers in the hazard events of
     *            <code>jsonEventValues</code>.
     * @param minVisibleTime
     *            Minimum visible time to be shown in the time megawidgets.
     * @param maxVisibleTime
     *            Maximum visible time to be shown in the time megawidgets.
     */
    public void showHazardDetail(String jsonGeneralWidgets,
            String jsonMetadataWidgets, DictList eventValuesList,
            String topEventID, long minVisibleTime, long maxVisibleTime);

    /**
     * Update the hazard detail subview, if it is showing.
     * 
     * @param eventValuesList
     *            List of dictionaries, each holding key-value pairs that
     *            specify a hazard event.
     */
    public void updateHazardDetail(DictList eventValuesList);

    /**
     * Hide the hazard detail subview.
     */
    public void hideHazardDetail();

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
