/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.viz.hazards.sessionmanager.deprecated;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.SessionManagerFactory;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.SettingsConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.deprecated.ProductGenerationResult.Choice;
import com.raytheon.uf.viz.hazards.sessionmanager.deprecated.ProductGenerationResult.Field;
import com.raytheon.uf.viz.hazards.sessionmanager.deprecated.ProductGenerationResult.GeneratedProduct;
import com.raytheon.uf.viz.hazards.sessionmanager.deprecated.ProductGenerationResult.HazardEventSet;
import com.raytheon.uf.viz.hazards.sessionmanager.deprecated.ProductGenerationResult.StagingInfo;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ISessionProductManager;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductFailed;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGenerated;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductInformation;
import com.raytheon.uf.viz.hazards.sessionmanager.time.ISessionTimeManager;
import com.raytheon.uf.viz.hazards.sessionmanager.undoable.IUndoRedoable;

/**
 * Provides backwards compatibility with old IHazardServicesModel. Does not
 * formally implement the interface to avoid circular dependency.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 20, 2013 1257       bsteffen    Initial creation
 * Jul 24, 2013  585       C. Golden   Changed to allow loading from bundles.
 * Aug 06, 2013 1265       B. Lawrence Updated to support undo/redo.
 * Aug 12, 2013 1921       B. Lawrence Added logic to clear the VTEC files
 *                                     in localization when events are reset.
 * Aug  9, 2013 1921       daniel.s.schaffer@noaa.gov  Removed code made obsolete by 
 *                                                     replacement of JSON with POJOs
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@Deprecated
public abstract class ModelAdapter {

    private final ISessionManager model;

    private final ObjectMapper jsonObjectMapper = new ObjectMapper();

    // The product editor presenter doesn't handle async multiple generations
    // very well so this code must accumulate products.
    private ProductGenerationResult generatedProducts = null;

    private int numProducts = 0;

    private boolean issue;

    /**
     * Files in localization to be removed when the events are reset from the
     * Console. These are VTEC-related. If VTEC information is allowed to
     * persist after events are deleted, VTEC processing could be compromised
     * for future events. These are assumed to be CAVE_STATIC files.
     */
    private static final String[] filesToDeleteOnReset = {
            "gfe/userPython/testVtecRecords_local.json",
            "gfe/userPython/vtecRecords.json",
            "gfe/userPython/vtecRecords.lock" };

    public ModelAdapter() {
        this.model = SessionManagerFactory.getSessionManager();
        this.model.registerForNotification(this);
        this.jsonObjectMapper
                .configure(
                        DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES,
                        false);
        this.jsonObjectMapper.getSerializationConfig()
                .setSerializationInclusion(Inclusion.NON_NULL);
    }

    /*
     * Call individual setters on managers instead.
     */
    @Deprecated
    public void initialize(String selectedTime, String currentTime,
            String staticSettingID, String dynamicSetting_json,
            String caveMode, String siteID, EventBus eventBus, String state) {
        model.getTimeManager().setSelectedTime(toDate(selectedTime));
        model.getConfigurationManager().setSiteID(siteID);
        model.getConfigurationManager().changeSettings(staticSettingID);
        if (dynamicSetting_json != null && !dynamicSetting_json.isEmpty()) {
            Settings dynamicSettings = fromJson(dynamicSetting_json,
                    Settings.class);
            model.getConfigurationManager().getSettings()
                    .apply(dynamicSettings);
        }
    }

    /*
     * This isn't used
     */
    @Deprecated
    public String getState(boolean saveState) {
        State state = new State();
        TimeRange visibleRange = model.getTimeManager().getVisibleRange();
        state.setLatestVisibleTime(fromDate(visibleRange.getEnd()));
        state.setEarliestVisibleTime(fromDate(visibleRange.getStart()));
        state.setLastSelectedEventID(getLastSelectedEventID());
        Collection<IHazardEvent> events = model.getEventManager()
                .getSelectedEvents();
        state.setSelectedEventIDs(toIDs(events));

        Collection<IHazardEvent> pending = model.getEventManager()
                .getEventsByState(HazardState.PENDING);
        Collection<Event> pending2 = new ArrayList<Event>(pending.size());
        for (IHazardEvent event : pending) {
            pending2.add(new Event(event));
        }
        state.setPendingEvents(pending2.toArray(new Event[0]));
        return toJson(state);
    }

    /*
     * Use ISessionEventManager.getSelectedEvents()
     */
    @Deprecated
    public String getSelectedEvents() {
        return toJson(toIDs(model.getEventManager().getSelectedEvents()));
    }

    /*
     * Use ISessionEventManager.setSelectedEvents()
     */
    @Deprecated
    public String updateSelectedEvents(String eventIDs, String originator) {
        Collection<IHazardEvent> selectedEvents = fromIDs(eventIDs);
        ISessionEventManager eventManager = model.getEventManager();
        ISessionTimeManager timeManager = model.getTimeManager();
        Date selectedTime = timeManager.getSelectedTime();

        eventManager.setSelectedEvents(selectedEvents);
        if (originator.equalsIgnoreCase("Temporal")
                && !selectedTime.equals(timeManager.getSelectedTime())) {
            return "Single";
        } else {
            return "None";
        }
    }

    /*
     * Use Settings.setAddToSelected()
     */
    @Deprecated
    public void setAddToSelected(String onOff) {
        model.getConfigurationManager().getSettings()
                .setAddToSelected(onOff.equalsIgnoreCase("on"));
    }

    /*
     * HazardDetailPresenter will need to move to front on selection.
     */
    @Deprecated
    public String getLastSelectedEventID() {
        IHazardEvent event = model.getEventManager()
                .getLastModifiedSelectedEvent();
        if (event != null) {
            return event.getEventID();
        }
        return "";
    }

    /*
     * This isn't used
     */
    @Deprecated
    public String getEventValues(String eventIDs, String fieldName,
            String returnType, String ignoreState) {
        if (fieldName.equals("selectionCallback")) {
            return "[]";
        } else {
            throw new UnsupportedOperationException("Not implemented");
        }
    }

    /*
     * Use ISessionEventManager.getEventsByState()
     */
    @Deprecated
    public Boolean checkForEventsWithState(String eventIDs, String searchStates) {
        HazardState state = HazardState.valueOf(searchStates.toUpperCase());
        Collection<IHazardEvent> events = fromIDs(eventIDs);
        events.retainAll(model.getEventManager().getEventsByState(state));
        return events.isEmpty();
    }

    /*
     * Use ISessionEventManager.addEvent()
     */
    @Deprecated
    public String newEvent(String eventArea) {
        Event event = fromJson(eventArea, Event.class);
        IHazardEvent hevent = event.toHazardEvent();
        hevent.addHazardAttribute("creationTime", model.getTimeManager()
                .getCurrentTime());
        hevent = model.getEventManager().addEvent(hevent);
        return hevent.getEventID();
    }

    /*
     * Use IHazardEvent.set*() or IHazardEvent.addHazardAttribute()
     */
    @Deprecated
    public void updateEventData(String jsonText, String source) {
        ISessionEventManager eventManager = model.getEventManager();
        ISessionConfigurationManager configManager = model
                .getConfigurationManager();
        JsonNode jnode = fromJson(jsonText, JsonNode.class);
        IHazardEvent event = eventManager.getEventById(jnode.get("eventID")
                .getValueAsText());
        Iterator<String> fields = jnode.getFieldNames();
        while (fields.hasNext()) {
            String key = fields.next();
            if ("eventID".equals(key)) {
                ;
            } else if ("fullType".equals(key)) {
                IHazardEvent oldEvent = null;
                if (!eventManager.canChangeType(event)) {
                    oldEvent = event;
                    event = new BaseHazardEvent(event);
                    event.setState(HazardState.PENDING);
                    event.addHazardAttribute("replaces",
                            configManager.getHeadline(oldEvent));
                    Collection<IHazardEvent> selection = eventManager
                            .getSelectedEvents();
                    event = eventManager.addEvent(event);
                    selection.add(event);
                    eventManager.setSelectedEvents(selection);
                }
                String fullType = jnode.get(key).getValueAsText();
                if (!fullType.isEmpty()) {
                    String[] phenSig = fullType.split(" ")[0].split("\\.");
                    event.setPhenomenon(phenSig[0]);
                    event.setSignificance(phenSig[1]);
                    if (phenSig.length > 2) {
                        event.setSubtype(phenSig[2]);
                    } else {
                        event.setSubtype(null);
                    }
                }
                if (oldEvent != null) {
                    oldEvent.addHazardAttribute("replacedBy",
                            configManager.getHeadline(event));
                    oldEvent.addHazardAttribute("previewState", "ended");
                }
            } else if ("startTime".equals(key)) {
                if (!eventManager.canChangeTimeRange(event)) {
                    event = new BaseHazardEvent(event);
                    event.setState(HazardState.PENDING);
                    Collection<IHazardEvent> selection = eventManager
                            .getSelectedEvents();
                    event = eventManager.addEvent(event);
                    selection.add(event);
                    eventManager.setSelectedEvents(selection);
                }
                event.setStartTime(new Date(jnode.get(key).getLongValue()));
            } else if ("endTime".equals(key)) {
                if (!eventManager.canChangeTimeRange(event)) {
                    event = new BaseHazardEvent(event);
                    event.setState(HazardState.PENDING);
                    Collection<IHazardEvent> selection = eventManager
                            .getSelectedEvents();
                    event = eventManager.addEvent(event);
                    selection.add(event);
                    eventManager.setSelectedEvents(selection);
                }
                event.setEndTime(new Date(jnode.get(key).getLongValue()));
            } else if (jnode.get(key).isArray()) {
                ArrayNode arrayNode = (ArrayNode) jnode.get(key);
                String[] array = new String[arrayNode.size()];
                for (int i = 0; i < array.length; i += 1) {
                    array[i] = arrayNode.get(i).getValueAsText();
                }
                event.addHazardAttribute(key, array);
            } else {
                JsonNode primitive = jnode.get(key);
                if (primitive.isTextual()) {
                    event.addHazardAttribute(key, primitive.getValueAsText());
                } else if (primitive.isBoolean()) {
                    event.addHazardAttribute(key, primitive.getBooleanValue());
                } else if (primitive.isNumber()) {
                    Object currentVal = event.getHazardAttribute(key);
                    if (currentVal instanceof Integer) {
                        event.addHazardAttribute(key, primitive.getIntValue());
                    } else {
                        event.addHazardAttribute(key,
                                new Date(primitive.getLongValue()));
                    }
                } else {
                    throw new UnsupportedOperationException("Not implemented");
                }
            }
        }

    }

    /*
     * Use IHazardEvent.setGeometry()
     */
    @Deprecated
    public void modifyEventArea(String jsonText) {
        ISessionEventManager eventManager = model.getEventManager();
        Event jevent = fromJson(jsonText, Event.class);
        IHazardEvent event = eventManager.getEventById(jevent.getEventID());
        if (event != null) {
            if (!eventManager.canChangeGeometry(event)) {
                event = new BaseHazardEvent(event);
                event.setState(HazardState.PENDING);
                Collection<IHazardEvent> selection = eventManager
                        .getSelectedEvents();
                event = eventManager.addEvent(event);
                selection.add(event);
                eventManager.setSelectedEvents(selection);
            }
            event.setGeometry(jevent.getGeometry());
        }
    }

    /*
     * Use ISessionEventManager.removeEvent()
     */
    @Deprecated
    public void deleteEvent(String eventIDs) {
        for (IHazardEvent event : fromIDs(eventIDs)) {
            model.getEventManager().removeEvent(event);
        }
    }

    /*
     * Use ISessionEventManager.removeEvent()
     */
    @Deprecated
    public void removeEvents(String field, String value) {
        if (field.equals("state")) {
            HazardState state = HazardState.valueOf(value.toUpperCase());
            ISessionEventManager eventManager = model.getEventManager();
            for (IHazardEvent event : eventManager.getEventsByState(state)) {
                eventManager.removeEvent(event);
            }
        } else {
            throw new UnsupportedOperationException("Not implemented");
        }
    }

    /*
     * This isn't used
     */
    @Deprecated
    public void handleAction(String action, String jsonText) {
        if (action.equals("riseAbove:crest:fallBelow")) {
            return;
        } else if (action.equals("__startTime__:__endTime__")) {
            return;
        }
        throw new UnsupportedOperationException("Not implemented: " + action);
    }

    /*
     * Use IHazardEvent.setState()
     */
    @Deprecated
    public void changeState(String eventID, String state) {
        if (state.toUpperCase().equals("PREVIEWENDED")) {
            for (IHazardEvent event : fromIDs(eventID)) {
                event.addHazardAttribute("previewState", "ended");
            }
        } else {
            HazardState hstate = HazardState.valueOf(state.toUpperCase());
            for (IHazardEvent event : fromIDs(eventID)) {
                event.setState(hstate);
            }
        }
    }

    /*
     * This isn't used
     */
    @Deprecated
    public void putHazards() {
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * Use ISessionEventManager.removeEvent()
     */
    @Deprecated
    public void reset(String name) {
        if (name.equals("Events")) {
            ISessionEventManager eventManager = model.getEventManager();
            for (IHazardEvent event : eventManager.getEvents()) {
                eventManager.removeEvent(event);
            }

            /*
             * Reset the VTEC information in the VTEC files. This needs to be
             * done. Otherwise, it is difficult to test against the job sheets
             * and functional tests when the forecaster selects Reset->Events
             * but old VTEC information remains in the VTEC files. This solution
             * will change once VTEC is stored in the database.
             */
            IPathManager pathManager = PathManagerFactory.getPathManager();
            LocalizationContext localizationContext = pathManager.getContext(
                    LocalizationContext.LocalizationType.CAVE_STATIC,
                    LocalizationContext.LocalizationLevel.USER);

            for (String fileToDelete : filesToDeleteOnReset) {
                File file = pathManager.getLocalizationFile(
                        localizationContext, fileToDelete).getFile();

                if (file.exists()) {
                    file.delete();
                }
            }
        }
    }

    /*
     * Use ISessionEventManager.sortEvents()
     */
    @Deprecated
    public void sendSelectedHazardsToFront() {
        model.getEventManager().sortEvents(
                ISessionEventManager.SEND_SELECTED_FRONT);
    }

    /*
     * Use ISessionEventManager.sortEvents()
     */
    @Deprecated
    public void sendSelectedHazardsToBack() {
        model.getEventManager().sortEvents(
                ISessionEventManager.SEND_SELECTED_BACK);
    }

    /*
     * Use ISessionTimeManager.getSelectedTime()
     */
    @Deprecated
    public String getSelectedTime() {
        return fromDate(model.getTimeManager().getSelectedTime());
    }

    /*
     * Use ISessionTimeManager.setSelectedTime()
     */
    @Deprecated
    public void updateSelectedTime(String selectedTime_ms) {
        model.getTimeManager().setSelectedTime(toDate(selectedTime_ms));
    }

    /*
     * Use ISessionTimeManager.getSelectedTimeRange()
     */
    @Deprecated
    public String getSelectedTimeRange() {
        TimeRange range = model.getTimeManager().getSelectedTimeRange();
        return toJson(new String[] { fromDate(range.getStart()),
                fromDate(range.getEnd()) });

    }

    /*
     * Use ISessionTimeManager.setSelectedTimeRange()
     */
    @Deprecated
    public void updateSelectedTimeRange(String startTime_ms, String endTime_ms) {
        TimeRange selectedRange = new TimeRange(toDate(startTime_ms),
                toDate(endTime_ms));
        model.getTimeManager().setSelectedTimeRange(selectedRange);
    }

    /*
     * Use SimulatedTime.getSystemTime().getTime()
     */
    @Deprecated
    public String getCurrentTime() {
        return fromDate(model.getTimeManager().getCurrentTime());
    }

    /*
     * This isn't necessary
     */
    @Deprecated
    public void updateCurrentTime(String currentTime_ms) {
        // I prefer to get the current time directly from simulated time.
    }

    /*
     * This isn't necessary
     */
    @Deprecated
    public void updateFrameInfo(String framesJSON) {
        // Nothing internally uses this.
    }

    /*
     * Use ISessionTimeManager.getVisibleRange().getStart()
     */
    @Deprecated
    public String getTimeLineEarliestVisibleTime() {
        return fromDate(model.getTimeManager().getVisibleRange().getStart());
    }

    /*
     * Use ISessionTimeManager.getVisibleRange().getEnd()
     */
    @Deprecated
    public String getTimeLineLatestVisibleTime() {
        return fromDate(model.getTimeManager().getVisibleRange().getEnd());
    }

    /*
     * Use ISessionTimeManager.setVisibleRange()
     */
    @Deprecated
    public String setTimeLineVisibleTimes(String earliest_ms, String latest_ms) {
        TimeRange visibleRange = new TimeRange(toDate(earliest_ms),
                toDate(latest_ms));
        model.getTimeManager().setVisibleRange(visibleRange);
        return null;
    }

    /*
     * Use ISessionConfigurationManager.getSettings().getSettingsID()
     */
    @Deprecated
    public String getCurrentSettingsID() {
        return model.getConfigurationManager().getSettings().getSettingsID();
    }

    /*
     * Use ISessionConfigurationManager.getSettings()
     */
    @Deprecated
    public String getStaticSettings(String settingsID) {
        return toJson(new Settings(model.getConfigurationManager()
                .getSettings()));
    }

    /*
     * This isn't used
     */
    @Deprecated
    public String newStaticSettings(String settings) {
        Settings settingsObj = fromJson(settings, Settings.class);
        settingsObj.setSettingsID(settingsObj.getDisplayName());
        model.getConfigurationManager().getSettings().apply(settingsObj);
        model.getConfigurationManager().saveSettings();
        return settingsObj.getSettingsID();
    }

    /*
     * This isn't used
     */
    @Deprecated
    public void updateStaticSettings(String settings) {
        Settings settingsObj = fromJson(settings, Settings.class);
        model.getConfigurationManager().getSettings().apply(settingsObj);
        model.getConfigurationManager().saveSettings();
    }

    /*
     * This isn't used
     */
    @Deprecated
    public void deleteStaticSettings(String settings) {
        // Cannot find how this gets called
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * Use ISessionConfigurationManager.getSettings()
     */
    @Deprecated
    public String getDynamicSettings() {
        Settings dset = model.getConfigurationManager().getSettings();
        return toJson(dset);
    }

    /*
     * Use
     * ISessionConfigurationManager.getSettings().getDefaultTimeDisplayDuration
     * ()
     */
    @Deprecated
    public String getTimeLineDuration() {
        return Long.toString(model.getConfigurationManager().getSettings()
                .getDefaultTimeDisplayDuration());
    }

    /*
     * Use ISessionConfigurationManager.getSettingsList()
     */
    @Deprecated
    public String getSettingsList() {
        ISessionConfigurationManager configManager = model
                .getConfigurationManager();
        Settings[] s = configManager.getSettingsList().toArray(new Settings[0]);
        SettingsList list = new SettingsList();
        list.setSettingsList(s);
        list.setCurrentSettingsID(configManager.getSettings().getSettingsID());
        return toJson(list);
    }

    /*
     * Use ISessionConfigurationManager.getSettings().getToolbarTools()
     */
    @Deprecated
    public String getToolList() {
        return toJson(model.getConfigurationManager().getSettings()
                .getToolbarTools());
    }

    /*
     * This isn't used
     */
    @Deprecated
    public String getAlertConfigValues() {
        // Cannot get to be called
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * This can be determined by the presenter.
     */
    @Deprecated
    public String getContextMenuEntries() {
        List<String> entries = new ArrayList<String>();
        EnumSet<HazardState> states = EnumSet.noneOf(HazardState.class);
        for (IHazardEvent event : model.getEventManager().getSelectedEvents()) {
            states.add(event.getState());
        }
        entries.add("Hazard Information Dialog");
        if (states.contains(HazardState.ISSUED)) {
            entries.add("End Selected Hazards");
        }
        if (!states.contains(HazardState.PROPOSED) || states.size() > 1) {
            entries.add("Propose Selected Hazards");
        }
        if (!states.contains(HazardState.ISSUED) || states.size() > 1) {
            entries.add("Issue Selected Hazards");
            entries.add("Delete Selected Hazards");
        }
        if (states.contains(HazardState.PROPOSED)) {
            entries.add("Save Proposed Hazards");
        }
        entries.add("Send to Back");
        entries.add("Bring to Front");
        if (states.contains(HazardState.POTENTIAL)) {
            entries.add("Remove Potential Hazards");
        }
        entries.add("Hazard Occurrence Alerts");
        return toJson(entries.toArray(new String[0]));
    }

    /*
     * This isn't used
     */
    @Deprecated
    public String getContextMenuEntryCallback(String menuItemName) {
        // Throws JepException in python implementation.
        throw new UnsupportedOperationException("Not implemented");
    }

    /*
     * Use AbstractRecommenderEngine.runExecuteRecommender()
     */
    @Deprecated
    public String runTool(String toolName, String runData) {
        RunData rData = fromJson(runData, RunData.class);
        if (rData.getEventSet() != null) {
            throw new UnsupportedOperationException("Not implemented");
        }
        EventSet<IEvent> eventSet = new EventSet<IEvent>();
        eventSet.addAttribute("currentTime", model.getTimeManager()
                .getCurrentTime().getTime());
        model.getRecommenderEngine().runExecuteRecommender(toolName, eventSet,
                rData.getSpatialInfo(), rData.getDialogInfoSerializable(),
                getRecommenderListener(toolName));
        return null;
    }

    protected abstract IPythonJobListener<List<IEvent>> getRecommenderListener(
            String toolName);

    /*
     * Use AbstractRecommenderEngine.getDialogInfo()
     */
    @Deprecated
    public String getDialogInfo(String toolName) {
        return toJson(model.getRecommenderEngine().getDialogInfo(toolName));
    }

    /*
     * Use AbstractRecommenderEngine.getSpatialInfo()
     */
    @Deprecated
    public String getSpatialInfo(String toolName) {
        Map<String, String> map = model.getRecommenderEngine().getSpatialInfo(
                toolName);
        if (map.isEmpty()) {
            return null;
        } else {
            throw new UnsupportedOperationException("Not implemented");
        }
    }

    /*
     * Use ISessionProductManager.getSelectedProducts()
     */
    @Deprecated
    public String createProductsFromEventIDs(String issueFlag) {
        ISessionProductManager productManager = model.getProductManager();
        Collection<ProductInformation> products = productManager
                .getSelectedProducts();
        boolean gen = true;
        for (ProductInformation info : products) {
            if (info.getDialogInfo() != null && !info.getDialogInfo().isEmpty()) {
                gen = false;
            } else if (info.getPotentialEvents() != null
                    && !info.getPotentialEvents().isEmpty()) {
                gen = false;
            }
            if (!gen) {
                break;
            }
        }
        ProductGenerationResult result = new ProductGenerationResult();
        if (gen) {
            generatedProducts = new ProductGenerationResult();
            numProducts = products.size();
            issue = issueFlag.equalsIgnoreCase(Boolean.TRUE.toString());
            for (ProductInformation info : products) {
                productManager.generate(info,
                        issueFlag.equalsIgnoreCase(Boolean.TRUE.toString()));

            }
            result.setReturnType("NONE");
        } else {
            result.setReturnType("stagingInfo");
            HazardEventSet[] sets = new HazardEventSet[products.size()];
            int setIndex = 0;
            for (ProductInformation info : products) {
                sets[setIndex] = new HazardEventSet();
                StagingInfo stage = new StagingInfo();
                Field[] fields = { new Field() };
                fields[0].setLines(info.getSelectedEvents().size());
                List<IHazardEvent> echoices = new ArrayList<IHazardEvent>();
                echoices.addAll(info.getSelectedEvents());
                echoices.addAll(info.getPotentialEvents());
                int choiceIndex = 0;
                Choice[] choices = new Choice[echoices.size()];
                for (IHazardEvent event : echoices) {
                    choices[choiceIndex] = new Choice();
                    StringBuilder displayString = new StringBuilder();
                    displayString.append(event.getEventID());
                    displayString.append(" ");
                    displayString.append(event.getPhenomenon());
                    displayString.append(".");
                    displayString.append(event.getSignificance());
                    if (event.getSubtype() != null) {
                        displayString.append(".");
                        displayString.append(event.getSubtype());
                    }
                    choices[choiceIndex].setDisplayString(displayString
                            .toString());
                    choices[choiceIndex].setIdentifier(event.getEventID());
                    choiceIndex += 1;
                }
                fields[0].setChoices(choices);
                fields[0].setFieldName("eventIDs");
                fields[0].setFieldType("CheckList");
                fields[0]
                        .setLabel("When issuing this hazard, there are other related hazards that could be included in the legacy product:");
                stage.setFields(fields);
                Map<String, String[]> valueDict = new HashMap<String, String[]>();
                valueDict.put("eventIDs", toIDs(info.getSelectedEvents()));
                stage.setValueDict(valueDict);
                sets[setIndex].setStagingInfo(stage);
                sets[setIndex].setDialogInfo(info.getDialogInfo());
                sets[setIndex].setProductGenerator(info.getProductName());
                setIndex += 1;
            }
            result.setHazardEventSets(sets);
        }
        return toJson(result);
    }

    protected abstract IPythonJobListener<List<IGeneratedProduct>> getProductGenerationListener(
            String toolName);

    /*
     * Use ISessionProductManager.generate()
     */
    @Deprecated
    public String createProductsFromHazardEventSets(String issueFlag,
            String hazardEventSets) {
        ISessionProductManager productManager = model.getProductManager();
        Collection<ProductInformation> products = productManager
                .getSelectedProducts();
        ProductGenerationResult result = fromJson(hazardEventSets,
                ProductGenerationResult.class);
        generatedProducts = new ProductGenerationResult();
        numProducts = result.getHazardEventSets().length;
        issue = issueFlag.equalsIgnoreCase(Boolean.TRUE.toString());
        for (HazardEventSet set : result.getHazardEventSets()) {
            ProductInformation info = null;
            for (ProductInformation testInfo : products) {
                if (set.getProductGenerator().equals(testInfo.getProductName())) {
                    info = testInfo;
                    break;
                }
            }
            Set<IHazardEvent> selectedEvents = new HashSet<IHazardEvent>();
            String[] events = set.getStagingInfo().getValueDict()
                    .get("eventIDs");
            for (String eventID : events) {
                for (IHazardEvent event : info.getSelectedEvents()) {
                    if (event.getEventID().equals(eventID)) {
                        selectedEvents.add(event);
                        break;
                    }
                }
                for (IHazardEvent event : info.getPotentialEvents()) {
                    if (event.getEventID().equals(eventID)) {
                        selectedEvents.add(event);
                        break;
                    }
                }
            }
            info.setSelectedEvents(selectedEvents);
            productManager.generate(info,
                    issueFlag.equalsIgnoreCase(Boolean.TRUE.toString()));

        }
        result = new ProductGenerationResult();
        result.setReturnType("NONE");
        return toJson(result);
    }

    /*
     * Use ISessionEventManager.addEvent
     */
    @Deprecated
    public String handleRecommenderResult(String toolID, List<IEvent> eventList) {
        RecommenderResult result = new RecommenderResult();
        List<Event> events = new ArrayList<Event>();
        for (IEvent event : eventList) {
            if (event instanceof IHazardEvent) {
                IHazardEvent hevent = (IHazardEvent) event;
                hevent = model.getEventManager().addEvent(hevent);
                Event jevent = new Event(hevent);
                String type = jevent.getType();
                if (type != null) {
                    String headline = model.getConfigurationManager()
                            .getHeadline(hevent);
                    jevent.setHeadline(headline);
                    jevent.setFullType(type + " (" + headline + ")");
                }
                events.add(jevent);
            }
        }
        result.setResultData(events.toArray(new Event[0]));
        result.setMetaData(model.getRecommenderEngine().getScriptMetadata(
                toolID));
        return toJson(result);
    }

    @Subscribe
    public void handleProductGeneratorResult(ProductGenerated generated) {
        getProductGenerationListener(
                generated.getProductInformation().getProductName())
                .jobFinished(generated.getProductInformation().getProducts());
    }

    @Subscribe
    public void handleProductGeneratorResult(ProductFailed failed) {
        getProductGenerationListener(
                failed.getProductInformation().getProductName()).jobFailed(
                failed.getProductInformation().getError());
    }

    /**
     * Undoes user-edits to a selected hazard
     * 
     * @param
     * @return
     * 
     */
    @Deprecated
    public void undo() {
        Collection<IHazardEvent> events = model.getEventManager()
                .getSelectedEvents();

        if (events.size() == 1) {
            Iterator<IHazardEvent> eventIter = events.iterator();
            ObservedHazardEvent obsEvent = (ObservedHazardEvent) eventIter
                    .next();
            obsEvent.undo();
        }

    }

    /**
     * Reapplies any undone user-edits.
     * 
     * @param
     * @return
     */
    @Deprecated
    public void redo() {
        Collection<IHazardEvent> events = model.getEventManager()
                .getSelectedEvents();

        /*
         * Limited to single selected hazard events.
         */
        if (events.size() == 1) {
            Iterator<IHazardEvent> eventIter = events.iterator();
            ObservedHazardEvent obsEvent = (ObservedHazardEvent) eventIter
                    .next();
            obsEvent.redo();
        }

    }

    /**
     * Tests if there are undoable user-edits.
     * 
     * @param
     * @return True - undoable user edits exist, False, undoable user-edits do
     *         not exist.
     */
    @Deprecated
    public Boolean isUndoable() {
        Collection<IHazardEvent> hazardEvents = model.getEventManager()
                .getSelectedEvents();

        /*
         * Limited to single selected hazard events.
         */
        if (hazardEvents.size() == 1) {
            Iterator<IHazardEvent> iterator = hazardEvents.iterator();
            return ((IUndoRedoable) iterator.next()).isUndoable();
        }

        return false;
    }

    /**
     * Tests if there are any redoable user-edits.
     * 
     * @param
     * @return True - user edits exist, False, user edits do not exist.
     */
    @Deprecated
    public Boolean isRedoable() {

        Collection<IHazardEvent> hazardEvents = model.getEventManager()
                .getSelectedEvents();

        /*
         * Limit to single selection.
         */
        if (hazardEvents.size() == 1) {
            Iterator<IHazardEvent> iterator = hazardEvents.iterator();
            return ((IUndoRedoable) iterator.next()).isRedoable();
        }

        return false;
    }

    /*
     * Use ProductGenerated Notifications
     */
    @Deprecated
    public String handleProductGeneratorResult(String toolID,
            List<IGeneratedProduct> generatedProductsList) {
        numProducts -= 1;

        Collection<IHazardEvent> selectedEvents = model.getEventManager()
                .getSelectedEvents();

        ProductGenerationResult result = generatedProducts;
        result.setReturnType("generatedProducts");
        List<GeneratedProduct> products = new ArrayList<GeneratedProduct>();
        for (IGeneratedProduct product : generatedProductsList) {
            GeneratedProduct genProduct = new GeneratedProduct();
            genProduct.setProductID(product.getProductID());
            genProduct.setLegacy(product.getEntry("Legacy").get(0).toString());
            products.add(genProduct);
        }
        if (products.isEmpty()) {
            GeneratedProduct genProduct = new GeneratedProduct();
            genProduct.setProductID("EMPTY");
            genProduct
                    .setLegacy(" EMPTY PRODUCT!  PLEASE MAKE SURE HAZARD(S) ARE WITHIN YOUR SITE CWA. ");
            products.add(genProduct);
        }
        if (result.getGeneratedProducts() != null) {
            products.addAll(Arrays.asList(result.getGeneratedProducts()));
        }
        result.setGeneratedProducts(products.toArray(new GeneratedProduct[0]));
        Field field = new Field();
        field.setLines(selectedEvents.size());
        List<Choice> choices = new ArrayList<Choice>();
        List<String> eventIDs = new ArrayList<String>();
        for (IHazardEvent event : selectedEvents) {
            Choice choice = new Choice();
            StringBuilder eventDisplayString = new StringBuilder(
                    event.getEventID());
            if (event.getPhenomenon() != null) {
                eventDisplayString.append(" ");
                eventDisplayString.append(event.getPhenomenon());
                if (event.getSignificance() != null) {
                    eventDisplayString.append(".");
                    eventDisplayString.append(event.getSignificance());
                    if (event.getSubtype() != null) {
                        eventDisplayString.append(".");
                        eventDisplayString.append(event.getSubtype());
                    }
                }
            }
            choice.setDisplayString(eventDisplayString.toString());
            choice.setIdentifier(event.getEventID());
            choices.add(choice);
            eventIDs.add(event.getEventID());
        }
        if (issue) {
            result = new ProductGenerationResult();
            result.setReturnType(null);
            return toJson(result);
        }
        field.setChoices(choices.toArray(new Choice[0]));
        field.setFieldName("eventIDs");
        field.setFieldType("CheckList");
        field.setLabel("When issuing this hazard, there are other related hazards that could be included in the legacy product:");
        StagingInfo stagingInfo = new StagingInfo();
        stagingInfo.setFields(new Field[] { field });
        Map<String, String[]> valueDict = new HashMap<String, String[]>();
        valueDict.put("eventIDs", eventIDs.toArray(new String[0]));
        stagingInfo.setValueDict(valueDict);
        HazardEventSet hes = new HazardEventSet();
        hes.setStagingInfo(stagingInfo);
        hes.setProductGenerator(toolID);
        hes.setDialogInfo(new HashMap<String, String>());
        List<HazardEventSet> sets = new ArrayList<HazardEventSet>();
        sets.add(hes);
        if (result.getHazardEventSets() != null) {
            sets.addAll(Arrays.asList(result.getHazardEventSets()));
        }
        result.setHazardEventSets(sets.toArray(new HazardEventSet[0]));
        if (numProducts > 0) {
            return null;
        }
        return toJson(result);
    }

    /*
     * Use ISessionEventManager.getEvents() or
     * ISessionEventManager.getCheckedEvents() or
     * ISessionEventManager.getSelectedEvents()
     */
    @Deprecated
    public String getComponentData(String component, String eventID) {
        ISessionEventManager eventManager = model.getEventManager();
        ISessionTimeManager timeManager = model.getTimeManager();

        Collection<IHazardEvent> events = null;
        if (component.equalsIgnoreCase("Temporal")) {
            events = eventManager.getEvents();
        } else if (component.equalsIgnoreCase("Spatial")) {
            events = eventManager.getCheckedEvents();
            TimeRange selectedRange = timeManager.getSelectedTimeRange();
            Date selectedTime = timeManager.getSelectedTime();
            Iterator<IHazardEvent> it = events.iterator();
            while (it.hasNext()) {
                IHazardEvent event = it.next();
                TimeRange eventRange = new TimeRange(event.getStartTime(),
                        event.getEndTime());
                if (selectedRange == null || !selectedRange.isValid()) {
                    if (!eventRange.contains(selectedTime)) {
                        it.remove();
                    }
                } else if (!eventRange.overlaps(selectedRange)) {
                    it.remove();
                }
            }
        } else if (component.equalsIgnoreCase("HID")) {
            events = eventManager.getSelectedEvents();
        }
        ISessionConfigurationManager configManager = model
                .getConfigurationManager();
        Date time = timeManager.getSelectedTime();
        Event[] events2 = new Event[events.size()];
        ArrayNode jevents = jsonObjectMapper.createArrayNode();
        Iterator<IHazardEvent> it = events.iterator();
        for (int i = 0; i < events2.length; i += 1) {
            IHazardEvent hevent = it.next();
            events2[i] = new Event(hevent);
            String type = events2[i].getType();
            if (type != null) {
                String headline = configManager.getHeadline(hevent);
                events2[i].setHeadline(headline);
                events2[i].setFullType(type + " (" + headline + ")");
            }
            TimeRange hetr = new TimeRange(hevent.getStartTime(),
                    hevent.getEndTime());
            if (time != null && !hetr.contains(time)) {
                events2[i].setShapes(new Shape[0]);
            }
            if (component.equalsIgnoreCase("HID")) {
                // HID needs all the extra attributes.
                JsonNode jobj = fromJson(toJson(events2[i]), JsonNode.class);
                ObjectNode node = (ObjectNode) jobj;
                for (Entry<String, Serializable> entry : hevent
                        .getHazardAttributes().entrySet()) {
                    if (entry.getValue() instanceof String) {
                        node.put(entry.getKey(), (String) entry.getValue());
                    } else if (entry.getValue() instanceof Boolean) {
                        node.put(entry.getKey(), (Boolean) entry.getValue());
                    } else if (entry.getValue() instanceof Date) {
                        node.put(entry.getKey(),
                                ((Date) entry.getValue()).getTime());
                    } else if (entry.getValue() instanceof String[]) {
                        ArrayNode tmpArray = jsonObjectMapper.createArrayNode();
                        for (Object obj : (String[]) entry.getValue()) {
                            tmpArray.add(obj.toString());
                        }
                        node.put(entry.getKey(), tmpArray);
                    } else if (entry.getValue() instanceof Integer) {
                        node.put(entry.getKey(), (Integer) entry.getValue());
                    }
                }
                jevents.add(jobj);
            }
        }
        if (component.equalsIgnoreCase("Temporal")) {
            return toJson(new TemporalComponentData(
                    configManager.getSettings(), events2));
        } else if (component.equalsIgnoreCase("Spatial")) {
            return toJson(events2);
        } else if (component.equalsIgnoreCase("HID")) {
            return toJson(jevents);
        } else {
            return "";
        }
    }

    /*
     * Use ISessionConfigurationManager.getStartUpConfig() or
     * ISessionConfigurationManager.getHazardInfoConfig() or
     * ISessionConfigurationManager.getFilterConfig() or
     * ISessionConfigurationManager.getHazardInfoOptions() or
     * ISessionConfigurationManager.getViewConfig()
     */
    @Deprecated
    public String getConfigItem(String item) {
        ISessionConfigurationManager cm = model.getConfigurationManager();
        if (item.equals("startUpConfig")) {
            return toJson(cm.getStartUpConfig());
        } else if (item.equals("hazardInfoConfig")) {
            return toJson(cm.getHazardInfoConfig());
        } else if (item.equals("filterConfig")) {
            return toJson(cm.getFilterConfig());
        } else if (item.equals("hazardInfoOptions")) {
            return toJson(cm.getHazardInfoOptions());
        } else if (item.equals("viewConfig")) {
            return toJson(new SettingsConfig[] { cm.getSettingsConfig() });
        } else {
            throw new UnsupportedOperationException("Not implemented");
        }
    }

    /*
     * This isn't used
     */
    @Deprecated
    public void putEvent(String eventDictAsJSON) {
        // never called
        throw new UnsupportedOperationException("Not implemented");

    }

    /*
     * This isn't used
     */
    @Deprecated
    public String getSessionEvent(String eventID) {
        // never called
        throw new UnsupportedOperationException("Not implemented");

    }

    /*
     * This isn't used
     */
    @Deprecated
    public void setHazardEventManager(Object hazardEventManager) {
        // I'd rather make my own?
    }

    /*
     * This isn't used
     */
    @Deprecated
    public String[] getHazardsForDynamicSettings() {
        // never called.
        throw new UnsupportedOperationException("Not implemented");
    }

    private <T> T fromJson(String str, Class<T> clazz) {
        try {
            return jsonObjectMapper.readValue(str, clazz);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private String toJson(Object obj) {
        try {
            return jsonObjectMapper.writeValueAsString(obj);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Collection<IHazardEvent> fromIDs(String eventIDsAsAJsonStringList) {
        ISessionEventManager eventManager = model.getEventManager();
        String[] eventIds = fromJson(eventIDsAsAJsonStringList, String[].class);
        Collection<IHazardEvent> events = new ArrayList<IHazardEvent>();
        for (String eventId : eventIds) {
            events.add(eventManager.getEventById(eventId));
        }
        return events;
    }

    private String[] toIDs(Collection<IHazardEvent> actualHazardEventObjects) {
        List<String> ids = new ArrayList<String>();
        for (IHazardEvent event : actualHazardEventObjects) {
            ids.add(event.getEventID());
        }
        return ids.toArray(new String[0]);
    }

    private String fromDate(Date actualDateObject) {
        return Long.toString(actualDateObject.getTime());
    }

    private Date toDate(String timeInMillisAsLongAsString) {
        return new Date(Long.valueOf(timeInMillisAsLongAsString));
    }

    public ISessionManager getSessionManager() {
        return model;
    }

}
