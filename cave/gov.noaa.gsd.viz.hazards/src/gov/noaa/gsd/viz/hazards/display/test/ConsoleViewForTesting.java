package gov.noaa.gsd.viz.hazards.display.test;

import gov.noaa.gsd.viz.hazards.console.ConsolePresenter;
import gov.noaa.gsd.viz.hazards.console.IConsoleView;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;

import java.util.Date;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
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
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
@SuppressWarnings("rawtypes")
public class ConsoleViewForTesting implements IConsoleView {

    private List hazardEvents;

    private Settings currentSettings;

    private ImmutableList activeAlerts;

    public ConsoleViewForTesting() {
        hazardEvents = Lists.newArrayList();
    }

    @Override
    public void dispose() {

    }

    @Override
    public List contributeToMainUI(Enum type) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void initialize(ConsolePresenter presenter, Date selectedTime,
            Date currentTime, long visibleTimeRange, List hazardEvents,
            Settings currentSettings, List settings, String jsonFilters,
            ImmutableList activeAlerts,
            Set eventIdentifiersAllowingUntilFurtherNotice,
            boolean temporalControlsInToolBar) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void acceptContributionsToMainUI(List contributors, Enum type) {

    }

    @Override
    public void ensureVisible() {

    }

    @Override
    public void updateCurrentTime(Date jsonCurrentTime) {

    }

    @Override
    public void updateSelectedTime(Date jsonSelectedTime) {

    }

    @Override
    public void updateSelectedTimeRange(String jsonRange) {

    }

    @Override
    public void updateVisibleTimeDelta(String jsonVisibleTimeDelta) {

    }

    @Override
    public void updateVisibleTimeRange(String jsonEarliestVisibleTime,
            String jsonLatestVisibleTime) {

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
    public void setHazardEvents(List hazardEvents, Settings currentSettings) {
        this.hazardEvents = hazardEvents;

    }

    @SuppressWarnings("unchecked")
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
        hazardEvents.remove(matchedEvent);
        hazardEvents.add(updatedEvent);
    }

    @Override
    public void setActiveAlerts(ImmutableList activeAlerts) {
        this.activeAlerts = activeAlerts;
    }

    @Override
    public Settings getCurrentSettings() {
        return currentSettings;
    }

    @Override
    public void setSettings(String currentSettingsID, List settings) {
    }

    @Override
    public void updateTitle(String title) {

    }

    public void setCurrentSettings(Settings currentSettings) {
        this.currentSettings = currentSettings;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    ImmutableList getActiveAlerts() {
        return activeAlerts;
    }

}
