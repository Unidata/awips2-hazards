package gov.noaa.gsd.viz.hazards.display.test;

import gov.noaa.gsd.common.utilities.TimeResolution;
import gov.noaa.gsd.viz.hazards.console.ConsolePresenter;
import gov.noaa.gsd.viz.hazards.console.IConsoleView;
import gov.noaa.gsd.viz.hazards.display.RCPMainUserInterfaceElement;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.mvp.IMainUiContributor;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jface.action.Action;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.google.common.collect.Range;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.viz.hazards.sessionmanager.alerts.IHazardAlert;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;

/**
 * Description: {@link IConsoleView} used for {@link AutomatedTests}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 04, 2013 2182     daniel.s.schaffer@noaa.gov      Initial creation
 * Apr 09, 2014    2925  Chris.Golden Fixed to work with new HID event propagation.
 * May 18, 2014    2925  Chris.Golden More changes to get it to work with the new HID.
 * Nov 18, 2014    4124  Chris.Golden Changed to work with new version of IConsoleView.
 * Dec 05, 2014    4124  Chris.Golden Changed to work with ObservedSettings.
 * May 05, 2015    6898   Chris.Cody  Pan & Scale Visible and Selected Time
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class ConsoleViewForTesting implements
        IConsoleView<Action, RCPMainUserInterfaceElement> {

    private List<Dict> hazardEvents;

    private ObservedSettings currentSettings;

    private ImmutableList<IHazardAlert> activeAlerts;

    public ConsoleViewForTesting() {
        hazardEvents = Lists.newArrayList();
    }

    @Override
    public void dispose() {

    }

    @Override
    public List<Action> contributeToMainUI(RCPMainUserInterfaceElement type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void ensureVisible() {

    }

    @Override
    public void updateCurrentTime(Date jsonCurrentTime) {

    }

    @Override
    public void updateSelectedTimeRange(Date start, Date end) {

    }

    @Override
    public void updateVisibleTimeDelta(long vsibleTimeDelta) {

    }

    @Override
    public void updateVisibleTimeRange(long earliestVisibleTime,
            long latestVisibleTime) {

    }

    @Override
    public List<Dict> getHazardEvents() {
        List<Dict> result = Lists.newArrayList();
        for (Object o : hazardEvents) {
            result.add((Dict) o);
        }
        return result;
    }

    @Override
    public void updateHazardEvent(String hazardEventJSON) {
        Dict updatedEvent = Dict.getInstance(hazardEventJSON);
        String eventID = updatedEvent
                .getDynamicallyTypedValue(HazardConstants.HAZARD_EVENT_IDENTIFIER);
        Dict matchedEvent = null;
        for (Object o : hazardEvents) {
            Dict event = (Dict) o;
            String eid = event
                    .getDynamicallyTypedValue(HazardConstants.HAZARD_EVENT_IDENTIFIER);
            if (eid.equals(eventID)) {
                matchedEvent = event;
            }
        }
        hazardEvents.set(hazardEvents.indexOf(matchedEvent), updatedEvent);
    }

    @Override
    public void setActiveAlerts(ImmutableList<IHazardAlert> activeAlerts) {
        this.activeAlerts = activeAlerts;
    }

    @Override
    public ObservedSettings getCurrentSettings() {
        return currentSettings;
    }

    @Override
    public void setSettings(String currentSettingsID, List<Settings> settings) {
    }

    @Override
    public void updateTitle(String title) {

    }

    public void setCurrentSettings(ObservedSettings currentSettings) {
        this.currentSettings = currentSettings;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    ImmutableList<IHazardAlert> getActiveAlerts() {
        return activeAlerts;
    }

    @Override
    public void updateEventTimeRangeBoundaries(Set<String> eventIds) {
    }

    @Override
    public void setHazardEvents(List<Dict> eventsAsDicts,
            ObservedSettings currentSettings) {
        this.hazardEvents = eventsAsDicts;
    }

    @Override
    public void acceptContributionsToMainUI(
            List<? extends IMainUiContributor<Action, RCPMainUserInterfaceElement>> contributors,
            RCPMainUserInterfaceElement type) {
    }

    @Override
    public void initialize(ConsolePresenter presenter, Date selectedTime,
            Date currentTime, long visibleTimeRange, List<Dict> hazardEvents,
            Map<String, Range<Long>> startTimeBoundariesForEventIds,
            Map<String, Range<Long>> endTimeBoundariesForEventIds,
            TimeResolution timeResolution,
            Map<String, TimeResolution> timeResolutionsForEventIds,
            ObservedSettings currentSettings, List<Settings> availableSettings,
            String jsonFilters, ImmutableList<IHazardAlert> activeAlerts,
            Set<String> eventIdentifiersAllowingUntilFurtherNotice,
            boolean temporalControlsInToolBar) {
    }

    @Override
    public void updateTimeResolution(TimeResolution timeResolution,
            Date currentTime) {
    }
}
