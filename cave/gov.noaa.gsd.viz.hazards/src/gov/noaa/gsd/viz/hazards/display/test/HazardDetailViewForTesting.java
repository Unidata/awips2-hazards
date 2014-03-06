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

import gov.noaa.gsd.viz.hazards.display.RCPMainUserInterfaceElement;
import gov.noaa.gsd.viz.hazards.hazarddetail.HazardDetailPresenter;
import gov.noaa.gsd.viz.hazards.hazarddetail.IHazardDetailView;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jface.action.Action;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;

/**
 * Description: Mock {@link IHazardDetailView} used for testing.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 22, 2013  2166      daniel.s.schaffer@noaa.gov  Initial creation
 * Nov 14, 2013  1463      bryon.lawrence Updated to support hazard conflict
 *                                        detection.
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class HazardDetailViewForTesting implements
        IHazardDetailView<Action, RCPMainUserInterfaceElement> {

    private DictList eventValuesList;

    private Map<String, Collection<IHazardEvent>> eventConflictMap;

    @Override
    public void dispose() {
    }

    @Override
    public void initialize(HazardDetailPresenter presenter,
            String jsonGeneralWidgets, String jsonMetadataWidgets,
            long minVisibleTime, long maxVisibleTime,
            Set<String> eventIdentifiersAllowingUntilFurtherNotice) {

    }

    @Override
    public void showHazardDetail(DictList eventValuesList, String topEventID,
            Map<String, Collection<IHazardEvent>> eventConflictMap,
            boolean force) {
        this.eventValuesList = eventValuesList;
        this.eventConflictMap = eventConflictMap;

    }

    @Override
    public void updateHazardDetail(DictList eventValuesList, String topEventID,
            Map<String, Collection<IHazardEvent>> eventConflictMap) {
        this.eventValuesList = eventValuesList;
        this.eventConflictMap = eventConflictMap;
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

    public Map<String, Collection<IHazardEvent>> getConflictMap() {
        return eventConflictMap;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    @Override
    public List<? extends Action> contributeToMainUI(
            RCPMainUserInterfaceElement type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setPreviewOngoing(boolean previewOngoing) {
    }

    @Override
    public void setIssueOngoing(boolean issueOngoing) {
    }

}
