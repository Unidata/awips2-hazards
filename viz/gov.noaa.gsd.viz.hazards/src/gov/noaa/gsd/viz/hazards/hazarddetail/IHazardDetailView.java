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
import gov.noaa.gsd.viz.mvp.widgets.IStateChanger;

import java.util.Map;

import com.google.common.collect.ImmutableList;
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
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IHazardDetailView {

    /**
     * Initialize the view.
     * 
     * @param hazardCategories
     *            List of hazard categories.
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
            ImmutableList<String> hazardCategories,
            long minVisibleTime,
            long maxVisibleTime,
            ICurrentTimeProvider currentTimeProvider,
            Map<String, Map<String, Map<String, Object>>> extraDataForEventIdentifiers);

    /**
     * Get the visible time range state changer.
     * 
     * @return Visible time range state changer.
     */
    public IStateChanger<String, TimeRange> getVisibleTimeRangeChanger();

    /**
     * Get the selected event state changer.
     * 
     * @return Selected event state changer.
     */
    public IChoiceStateChanger<String, String, String, DisplayableEventIdentifier> getVisibleEventChanger();

    /**
     * Get the category state changer.
     * 
     * @return Category state changer.
     */
    public IChoiceStateChanger<String, String, String, String> getCategoryChanger();

    /**
     * Get the type state changer.
     * 
     * @return Type state changer.
     */
    public IChoiceStateChanger<String, String, String, String> getTypeChanger();

    /**
     * Get the time range state changer.
     * 
     * @return Time range state changer.
     */
    public IStateChanger<String, TimeRange> getTimeRangeChanger();

    /**
     * Get the metadata state changer.
     * 
     * @return Metadata state changer.
     */
    public IMetadataStateChanger getMetadataChanger();

    /**
     * Get the button invoker.
     * 
     * @return Button invoker.
     */
    public ICommandInvoker<Command> getButtonInvoker();
}
