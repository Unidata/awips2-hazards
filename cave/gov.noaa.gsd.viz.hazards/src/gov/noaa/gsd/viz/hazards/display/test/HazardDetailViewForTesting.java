/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display.test;

import gov.noaa.gsd.viz.hazards.hazarddetail.HazardDetailPresenter;
import gov.noaa.gsd.viz.hazards.hazarddetail.IHazardDetailView;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * Description: Mock {@link IHazardDetailView} used for testing.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 22, 2013  2166      daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
@SuppressWarnings("rawtypes")
public class HazardDetailViewForTesting implements IHazardDetailView {

    private DictList eventValuesList;

    @Override
    public void dispose() {
    }

    @Override
    public List contributeToMainUI(Enum type) {
        return null;
    }

    @Override
    public void initialize(HazardDetailPresenter presenter,
            String jsonGeneralWidgets, String jsonMetadataWidgets,
            long minVisibleTime, long maxVisibleTime) {

    }

    @Override
    public void showHazardDetail(DictList eventValuesList, String topEventID,
            boolean force) {
        this.eventValuesList = eventValuesList;

    }

    @Override
    public void updateHazardDetail(DictList eventValuesList, String topEventID) {
        this.eventValuesList = eventValuesList;
    }

    @Override
    public void hideHazardDetail(boolean force) {

    }

    @Override
    public void setVisibleTimeRange(long minVisibleTime, long maxVisibleTime) {

    }

    public DictList getContents() {
        return eventValuesList;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
