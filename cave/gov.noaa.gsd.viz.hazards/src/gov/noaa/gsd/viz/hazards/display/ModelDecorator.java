/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display;

import gov.noaa.gsd.common.utilities.Timer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Maps;
import com.google.common.eventbus.EventBus;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;

/**
 * Description: Decorates instances of IHazardServicesModel for such purposes as
 * logging and performance benchmarking.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * DEC 06, 2012            daniel.s.schaffer      Initial creation
 * Jul 15, 2013      585   Chris.Golden           Changed to take event bus so as
 *                                                to avoid the latter being a
 *                                                singleton.
 * Aug 06, 2013     1265   bryon.lawrence         Added support for undo/redo
 * Aug  9, 2013 1921       daniel.s.schaffer@noaa.gov  Support of replacement of JSON with POJOs
 * </pre>
 * 
 * @author daniel.s.schaffer
 * @version 1.0
 */
public class ModelDecorator implements IHazardServicesModel {

    private final boolean loggingOn = false;

    private final boolean benchmarkingOn = true;

    private final Map<String, Timer> benchmarkingStats = Maps.newHashMap();

    private final IHazardServicesModel decorated;

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ModelDecorator.class);

    private BufferedWriter writer;

    private String logFilePath;

    public ModelDecorator(IHazardServicesModel decorated) {
        this.decorated = decorated;
        if (loggingOn) {
            this.logFilePath = "TBD specify your scratchDir";

            buildLogWriter();
        }
    }

    private void buildLogWriter() {
        if (loggingOn) {
            try {
                writer = new BufferedWriter(new FileWriter(logFilePath));
            } catch (IOException e) {
                statusHandler.error("ModelDecorator.buildLogWriter(): "
                        + "Unable to build log writer.", e);
            }
        }
    }

    @Override
    public Boolean checkForEventsWithState(String eventIDs, String searchStates) {
        final String methodName = "checkForEventsWithState";
        logCallingMethod(methodName);
        log(String.format("eventdIDs: %s", eventIDs));
        log(String.format("searchStates: %s", searchStates));

        benchmarkStart(methodName);
        Boolean result = decorated.checkForEventsWithState(eventIDs,
                searchStates);
        benchmarkStop(methodName);
        log(String.format("Result: %s", result));
        return result;
    }

    @Override
    public void initialize(String selectedTime, String currentTime,
            String staticSettingID, String dynamicSetting_json,
            String caveMode, String siteID, EventBus eventBus, String state) {
        final String methodName = "initialize";
        logCallingMethod(methodName);
        log(String.format("selectedTime: %s", selectedTime));
        log(String.format("currentTime: %s", currentTime));
        log(String.format("staticSettingID: %s", staticSettingID));
        log(String.format("dynamicSetting_json: %s", dynamicSetting_json));
        log(String.format("caveMode: %s", caveMode));
        log(String.format("siteID: %s", siteID));
        log(String.format("state: %s", state));
        benchmarkStart(methodName);
        decorated.initialize(selectedTime, currentTime, staticSettingID,
                dynamicSetting_json, caveMode, siteID, eventBus, state);
        benchmarkStop(methodName);
    }

    @Override
    public void reset(String name) {
        final String methodName = "reset";
        logCallingMethod(methodName);
        log(String.format("name: %s", name));
        benchmarkStart(methodName);
        decorated.reset(name);
        benchmarkStop(methodName);
    }

    @Override
    public String getComponentData(String component, String eventID) {
        final String methodName = "getComponentData";
        logCallingMethod(methodName);
        log(String.format("component: %s", component));
        log(String.format("eventID: %s", eventID));
        benchmarkStart(methodName);
        String result = decorated.getComponentData(component, eventID);
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public String getSelectedEvents() {
        final String methodName = "getSelectedEvents";
        logCallingMethod(methodName);
        benchmarkStart(methodName);
        String result = decorated.getSelectedEvents();
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public String getLastSelectedEventID() {
        final String methodName = "getLastSelectedEventID";
        logCallingMethod(methodName);
        benchmarkStart(methodName);
        String result = decorated.getLastSelectedEventID();
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public String getSelectedTime() {
        final String methodName = "getSelectedTime";
        logCallingMethod(methodName);
        benchmarkStart(methodName);
        String result = decorated.getSelectedTime();
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public String getTimeLineEarliestVisibleTime() {
        final String methodName = "getTimeLineEarliestVisibleTime";
        logCallingMethod(methodName);
        benchmarkStart(methodName);
        String result = decorated.getTimeLineEarliestVisibleTime();
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public String getTimeLineLatestVisibleTime() {
        final String methodName = "getTimeLineLatestVisibleTime";
        logCallingMethod(methodName);
        benchmarkStart(methodName);
        String result = decorated.getTimeLineLatestVisibleTime();
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public String setTimeLineVisibleTimes(String earliest_ms, String latest_ms) {
        final String methodName = "setTimeLineVisibleTimes";
        logCallingMethod(methodName);

        benchmarkStart(methodName);
        String result = decorated.setTimeLineVisibleTimes(earliest_ms,
                latest_ms);
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public String getSelectedTimeRange() {
        final String methodName = "getSelectedTimeRange";
        logCallingMethod(methodName);
        benchmarkStart(methodName);
        String result = decorated.getSelectedTimeRange();
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public String getCurrentTime() {
        final String methodName = "getCurrentTime";
        logCallingMethod(methodName);
        benchmarkStart(methodName);
        String result = decorated.getCurrentTime();
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public String getTimeLineDuration() {
        final String methodName = "getTimeLineDuration";
        logCallingMethod(methodName);
        benchmarkStart(methodName);
        String result = decorated.getTimeLineDuration();
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public String getCurrentSettingsID() {
        final String methodName = "getCurrentSettingsID";
        logCallingMethod(methodName);
        benchmarkStart(methodName);
        String result = decorated.getCurrentSettingsID();
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public String getState(boolean saveState) {
        final String methodName = "getState";
        logCallingMethod(methodName);
        log(String.format("saveState: %s", saveState));
        benchmarkStart(methodName);
        String result = decorated.getState(saveState);
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public String getEventValues(String eventIDs, String fieldName,
            String returnType, String ignoreState) {
        final String methodName = "getEventValues";
        logCallingMethod(methodName);
        log(String.format("eventdIDs: %s", eventIDs));
        log(String.format("fieldName: %s", fieldName));
        log(String.format("returnType: %s", returnType));
        log(String.format("ignoreState: %s", ignoreState));
        benchmarkStart(methodName);
        String result = decorated.getEventValues(eventIDs, fieldName,
                returnType, ignoreState);
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public void updateCurrentTime(String currentTime_ms) {
        final String methodName = "updateCurrentTime";
        logCallingMethod(methodName);
        benchmarkStart(methodName);
        decorated.updateCurrentTime(currentTime_ms);
        benchmarkStop(methodName);
    }

    @Override
    public void updateSelectedTime(String selectedTime_ms) {
        final String methodName = "updateSelectedTime";
        logCallingMethod(methodName);
        log("Calling updateSelectedTime");
        log(String.format("selectedTime_ms: %s", selectedTime_ms));
        benchmarkStart(methodName);
        decorated.updateSelectedTime(selectedTime_ms);
        benchmarkStop(methodName);
    }

    @Override
    public void updateSelectedTimeRange(String startTime_ms, String endTime_ms) {
        final String methodName = "updateSelectedTimeRange";
        logCallingMethod(methodName);
        log("Calling updateSelectedTimeRange");
        log(String.format("startTime_ms: %s", startTime_ms));
        log(String.format("endTime_ms: %s", endTime_ms));
        benchmarkStart(methodName);
        decorated.updateSelectedTimeRange(startTime_ms, endTime_ms);
        benchmarkStop(methodName);
    }

    @Override
    public String updateSelectedEvents(String eventIDs, String originator) {
        final String methodName = "updateSelectedEvents";
        logCallingMethod(methodName);
        log("Calling updateSelectedEvents");
        log(String.format("eventdIDs: %s", eventIDs));
        log(String.format("originator: %s", originator));
        benchmarkStart(methodName);
        String result = decorated.updateSelectedEvents(eventIDs, originator);
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public void setAddToSelected(String onOff) {
        final String methodName = "setAddToSelected";
        logCallingMethod(methodName);
        log("Calling setAddToSelected");
        log(String.format("onOff: %s", onOff));
        benchmarkStart(methodName);
        decorated.setAddToSelected(onOff);
        benchmarkStop(methodName);
    }

    @Override
    public void updateEventData(String jsonText, String source) {
        final String methodName = "updateEventData";
        logCallingMethod(methodName);
        log(String.format("jsonText: %s", jsonText));
        log(String.format("source: %s", source));
        benchmarkStart(methodName);
        decorated.updateEventData(jsonText, source);
        benchmarkStop(methodName);
    }

    @Override
    public String newEvent(String eventArea) {
        final String methodName = "newEvent";
        logCallingMethod(methodName);
        log(String.format("eventArea: %s", eventArea));
        benchmarkStart(methodName);
        String result = decorated.newEvent(eventArea);
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public void deleteEvent(String eventIDs) {
        final String methodName = "deleteEvent";
        logCallingMethod(methodName);
        log(String.format("eventdIDs: %s", eventIDs));
        benchmarkStart(methodName);
        decorated.deleteEvent(eventIDs);
        benchmarkStop(methodName);
    }

    @Override
    public String runTool(String toolName, String runData) {
        final String methodName = "runTool";
        logCallingMethod(methodName);
        log(String.format("toolName: %s", toolName));
        log(String.format("runData: %s", runData));
        benchmarkStart(methodName);
        String result = decorated.runTool(toolName, runData);
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public String getDialogInfo(String toolName) {
        final String methodName = "getDialogInfo";
        logCallingMethod(methodName);
        log(String.format("toolName: %s", toolName));
        benchmarkStart(methodName);
        String result = decorated.getDialogInfo(toolName);
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public String getSpatialInfo(String toolName) {
        final String methodName = "getSpatialInfo";
        logCallingMethod(methodName);
        log(String.format("toolName: %s", toolName));
        benchmarkStart(methodName);
        String result = decorated.getSpatialInfo(toolName);
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public void modifyEventArea(String jsonText) {
        final String methodName = "modifyEventArea";
        logCallingMethod(methodName);
        log(String.format("jsonText: %s", jsonText));
        benchmarkStart(methodName);
        decorated.modifyEventArea(jsonText);
        benchmarkStop(methodName);
    }

    @Override
    public String getContextMenuEntries() {
        final String methodName = "getContextMenuEntries";
        logCallingMethod(methodName);
        benchmarkStart(methodName);
        String result = decorated.getContextMenuEntries();
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public String getContextMenuEntryCallback(String menuItemName) {
        final String methodName = "getContextMenuEntryCallback";
        logCallingMethod(methodName);
        log(String.format("menuItemName: %s", menuItemName));
        benchmarkStart(methodName);
        String result = decorated.getContextMenuEntryCallback(menuItemName);
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public String getConfigItem(String item) {
        final String methodName = "getConfigItem";
        logCallingMethod(methodName);
        log(String.format("item: %s", item));
        benchmarkStart(methodName);
        String result = decorated.getConfigItem(item);
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public void removeEvents(String field, String value) {
        final String methodName = "removeEvents";
        logCallingMethod(methodName);
        log(String.format("field: %s", field));
        log(String.format("value: %s", value));
        benchmarkStart(methodName);
        decorated.removeEvents(field, value);
        benchmarkStop(methodName);
    }

    @Override
    public void handleAction(String action, String jsonText) {
        final String methodName = "handleAction";
        logCallingMethod(methodName);
        log(String.format("action: %s", action));
        log(String.format("jsonText: %s", jsonText));
        benchmarkStart(methodName);
        decorated.handleAction(action, jsonText);
        benchmarkStop(methodName);
    }

    @Override
    public void changeState(String eventID, String state) {
        final String methodName = "changeState";
        logCallingMethod(methodName);
        log(String.format("eventID: %s", eventID));
        log(String.format("state: %s", state));
        benchmarkStart(methodName);
        decorated.changeState(eventID, state);
        benchmarkStop(methodName);
    }

    @Override
    public void putHazards() {
        final String methodName = "putHazards";
        logCallingMethod(methodName);
        benchmarkStart(methodName);
        decorated.putHazards();
        benchmarkStop(methodName);
    }

    @Override
    public String newStaticSettings(String setting) {
        final String methodName = "newStaticSettings";
        logCallingMethod(methodName);
        log(String.format("setting: %s", setting));
        benchmarkStart(methodName);
        String result = decorated.newStaticSettings(setting);
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public void updateStaticSettings(String setting) {
        final String methodName = "updateStaticSettings";
        logCallingMethod(methodName);
        log(String.format("setting: %s", setting));
        benchmarkStart(methodName);
        decorated.updateStaticSettings(setting);
        benchmarkStop(methodName);
    }

    @Override
    public void deleteStaticSettings(String setting) {
        final String methodName = "deleteStaticSettings";
        logCallingMethod(methodName);
        log(String.format("setting: %s", setting));
        benchmarkStart(methodName);
        decorated.deleteStaticSettings(setting);
        benchmarkStop(methodName);
    }

    @Override
    public String getStaticSettings(String settingID) {
        final String methodName = "getStaticSettings";
        logCallingMethod(methodName);
        log(String.format("settingID: %s", settingID));
        benchmarkStart(methodName);
        String result = decorated.getStaticSettings(settingID);
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public String getDynamicSettings() {
        final String methodName = "getDynamicSettings";
        logCallingMethod(methodName);
        benchmarkStart(methodName);
        String result = decorated.getDynamicSettings();
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public String getSettingsList() {
        final String methodName = "getSettingsList";
        logCallingMethod(methodName);
        benchmarkStart(methodName);
        String result = decorated.getSettingsList();
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public String getToolList() {
        final String methodName = "getToolList";
        logCallingMethod(methodName);
        benchmarkStart(methodName);
        String result = decorated.getToolList();
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public void updateFrameInfo(String framesJSON) {
        final String methodName = "updateFrameInfo";
        logCallingMethod(methodName);
        log(String.format("framesJSON: %s", framesJSON));
        benchmarkStart(methodName);
        decorated.updateFrameInfo(framesJSON);
        benchmarkStop(methodName);
    }

    @Override
    public String createProductsFromEventIDs(String issueFlag) {
        final String methodName = "createProductsFromEventIDs";
        logCallingMethod(methodName);
        log(String.format("issueFlag: %s", issueFlag));
        benchmarkStart(methodName);
        String result = decorated.createProductsFromEventIDs(issueFlag);
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public String createProductsFromHazardEventSets(String issueFlag,
            String hazardEventSets) {
        final String methodName = "createProductsFromHazardEventSets";
        logCallingMethod(methodName);
        log(String.format("eventdIDs: %s", issueFlag));
        log(String.format("hazardEventSets: %s", hazardEventSets));
        benchmarkStart(methodName);
        String result = decorated.createProductsFromHazardEventSets(issueFlag,
                hazardEventSets);
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public String getAlertConfigValues() {
        final String methodName = "getAlertConfigValues";
        logCallingMethod(methodName);
        benchmarkStart(methodName);
        String result = decorated.getAlertConfigValues();
        benchmarkStop(methodName);
        logResult(result);
        return result;
    }

    @Override
    public String[] getHazardsForDynamicSettings() {
        final String methodName = "getHazardsForDynamicSettings";
        logCallingMethod(methodName);
        benchmarkStart(methodName);
        String[] result = decorated.getHazardsForDynamicSettings();
        benchmarkStop(methodName);
        return result;
    }

    @Override
    public void setHazardEventManager(Object hazardEventManager) {
        final String methodName = "setHazardEventManager";
        logCallingMethod(methodName);
        benchmarkStart(methodName);
        decorated.setHazardEventManager(hazardEventManager);
        benchmarkStop(methodName);
    }

    @Override
    public void putEvent(String eventDictAsJSON) {
        final String methodName = "putEvent";
        logCallingMethod(methodName);
        benchmarkStart(methodName);
        decorated.putEvent(eventDictAsJSON);
        benchmarkStop(methodName);
    }

    @Override
    public String getSessionEvent(String eventID) {
        final String methodName = "getSessionEvent";
        logCallingMethod(methodName);
        benchmarkStart(methodName);
        String result = decorated.getSessionEvent(eventID);
        benchmarkStop(methodName);
        return result;
    }

    @Override
    public void sendSelectedHazardsToFront() {
        final String methodName = "sendSelectedHazardsToFront";
        logCallingMethod(methodName);
        benchmarkStart(methodName);
        decorated.sendSelectedHazardsToFront();
        benchmarkStop(methodName);
    }

    @Override
    public void sendSelectedHazardsToBack() {
        final String methodName = "sendSelectedHazardsToBack";
        logCallingMethod(methodName);
        benchmarkStart(methodName);
        decorated.sendSelectedHazardsToBack();
        benchmarkStop(methodName);
    }

    private void log(String message) {
        if (loggingOn) {
            try {
                writer.write("\n" + message.replaceAll("\"", "'") + "\n");
                writer.flush();
            } catch (IOException e) {
                statusHandler.error("ModelDecorator.log(): "
                        + "Unable to log message.", e);
            }
        }
    }

    private void logCallingMethod(final String methodName) {
        log(String.format("Calling %s", methodName));
    }

    private void logResult(String result) {
        log(String.format("Result: %s", result));
    }

    private void benchmarkStart(String methodName) {
        if (benchmarkingOn) {
            Timer stats = benchmarkingStats.get(methodName);
            if (stats == null) {
                stats = new Timer();
                benchmarkingStats.put(methodName, stats);
            }
            stats.on(methodName);
        }
    }

    private void benchmarkStop(String methodName) {
        if (benchmarkingOn) {
            try {
                Timer stats = benchmarkingStats.get(methodName);
                stats.off(methodName);
            } catch (NullPointerException e) {
                throw new IllegalArgumentException(
                        String.format(
                                "Mismatched benchmark call for method %s?",
                                methodName), e);
            }
        }
    }

    public String getBenchmarkingStats() {
        StringBuilder sb = new StringBuilder();
        sb.append("Benchmarking Stats\n");
        for (String methodName : benchmarkingStats.keySet()) {
            Timer stats = benchmarkingStats.get(methodName);
            sb.append(String.format("%s", stats.statsString(methodName)));
            sb.append("\n");
        }
        return sb.toString();
    }

    @Override
    public String handleRecommenderResult(String toolID, List<IEvent> eventList) {
        final String methodName = "handleRecommenderResult";
        logCallingMethod(methodName);
        // ToStringBuilder.reflectionToString(eventList);
        log(String.format("toolID: %s", toolID));
        benchmarkStart(methodName);
        String result = decorated.handleRecommenderResult(toolID, eventList);
        benchmarkStop(methodName);
        return result;
    }

    @Override
    public String handleProductGeneratorResult(String toolID,
            List<IGeneratedProduct> generatedProductList) {
        final String methodName = "handleProductGeneratorResult";
        logCallingMethod(methodName);
        // ToStringBuilder.reflectionToString(generatedProductList);
        log(String.format("toolID: %s", toolID));
        benchmarkStart(methodName);
        String result = decorated.handleProductGeneratorResult(toolID,
                generatedProductList);
        benchmarkStop(methodName);
        return result;
    }

    @Override
    public void undo() {
        final String methodName = "undo";
        logCallingMethod(methodName);
        benchmarkStart(methodName);
        decorated.undo();
        benchmarkStop(methodName);
    }

    @Override
    public void redo() {
        final String methodName = "redo";
        logCallingMethod(methodName);
        benchmarkStart(methodName);
        decorated.redo();
        benchmarkStop(methodName);
    }

    @Override
    public Boolean isUndoable() {
        final String methodName = "isUndoable";
        logCallingMethod(methodName);
        benchmarkStart(methodName);
        Boolean result = decorated.isUndoable();
        benchmarkStop(methodName);
        return result;
    }

    @Override
    public Boolean isRedoable() {
        final String methodName = "isRedoable";
        logCallingMethod(methodName);
        benchmarkStart(methodName);
        Boolean result = decorated.isRedoable();
        benchmarkStop(methodName);
        return result;
    }

    @Override
    public ISessionManager getSessionManager() {
        return decorated.getSessionManager();
    }
}
