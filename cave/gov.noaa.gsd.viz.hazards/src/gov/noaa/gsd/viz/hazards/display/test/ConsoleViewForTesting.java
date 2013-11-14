package gov.noaa.gsd.viz.hazards.display.test;

import gov.noaa.gsd.viz.hazards.console.ConsolePresenter;
import gov.noaa.gsd.viz.hazards.console.IConsoleView;
import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;
import gov.noaa.gsd.viz.hazards.jsonutilities.DictList;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;

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

    private DictList hazardEvents;

    private Dict dynamicSetting;

    private ImmutableList activeAlerts;

    public ConsoleViewForTesting() {
        hazardEvents = new DictList();
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
            Date currentTime, long visibleTimeRange, String jsonHazardEvents,
            String jsonSettings, String jsonFilters,
            ImmutableList activeAlerts, boolean temporalControlsInToolBar) {

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
    public void setHazardEvents(String hazardEventsJSON) {
        Dict dict = Dict.getInstance(hazardEventsJSON);
        ArrayList elements = (ArrayList) dict.get("events");
        this.hazardEvents = new DictList();
        for (Object object : elements) {
            hazardEvents.add(object);

        }

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
        hazardEvents.remove(matchedEvent);
        hazardEvents.add(updatedEvent);
    }

    @Override
    public void setActiveAlerts(ImmutableList activeAlerts) {
        this.activeAlerts = activeAlerts;
    }

    @Override
    public Dict getDynamicSetting() {
        return dynamicSetting;
    }

    @Override
    public void setSettings(String jsonSettings) {

    }

    @Override
    public void updateTitle(String title) {

    }

    public void setDynamicSetting(Dict dynamicSetting) {
        this.dynamicSetting = dynamicSetting;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    ImmutableList getActiveAlerts() {
        return activeAlerts;
    }
}
