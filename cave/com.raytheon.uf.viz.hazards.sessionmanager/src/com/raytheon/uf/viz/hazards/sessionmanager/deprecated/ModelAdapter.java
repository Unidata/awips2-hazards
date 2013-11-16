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

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;

import com.google.common.eventbus.EventBus;
import com.google.common.eventbus.Subscribe;
import com.raytheon.uf.common.colormap.Color;
import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardState;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.HazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.IHazardEventManager;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.common.localization.IPathManager;
import com.raytheon.uf.common.localization.LocalizationContext;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.PathManagerFactory;
import com.raytheon.uf.common.localization.exception.LocalizationOpFailedException;
import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.TimeRange;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.SettingsConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.events.ISessionEventManager;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductFailed;
import com.raytheon.uf.viz.hazards.sessionmanager.product.ProductGenerated;
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
 * Aug 26, 2012 1921       B. Lawrence  Replaced "replaces" string with 
 *                                      HazardConstants.REPLACES constant.
 * Aug 26, 2013 1921    Bryon.Lawrence    Put back block of code which adds color
 *                                        information to hazard events until all
 *                                        Hazard Services views have been converted
 *                                        to use event objects instead of event dicts.
 * Aug 29, 2013 1921    Bryon.Lawrence  Modified getContextMenuEntries to support 
 *                                      hazard-specific context menu entries. This was
 *                                      done to fix "Add/Remove Shapes" functionality
 * Sep 06, 2013 1921    Bryon.Lawrence  Modified getContextMenuEntries to support the
 *                                      "Remove Potential Hazards" option.
 * Sep 10, 2013  752    Bryon.Lawrence  Modified updateEventData to reset the eventID
 *                                      on newly created hazards.
 * Sep 13, 2013 1921    Chris.Golden    Added ability to handle arbitrary long values
 *                                      in getComponentData().
 * Sept 18, 2013 1298    Tracy.L.Hansen Product information in Hazard Event needs to be
 *                                      re-initialized when copying a Hazard Event to
 *                                      create a new one e.g. FA.A --> FA.W
 * Sep 19, 2013 2046    mnash           Update for product generation.
 * Sep 25, 2013 1298    blawrenc        Updated the reset method to operate
 *                                      on LocalizationFile not File. This 
 *                                      helps to ensure that localization is 
 *                                      properly updated when files are
 *                                      deleted from it.
 * Oct 22, 2013 2155    blawrenc        Fixed getContextMenuEntries() to 
 *                                      receive hazard-specific menu entries
 *                                      as a List<String> instead of a String[].
 * Nov 04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Nov 15, 2013  2182       daniel.s.schaffer@noaa.gov    Refactoring JSON - ProductStagingDialog
 * Nov 20, 2013 2460    daniel.s.schaffer@noaa.gov  Reset now removing all events from practice table
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
@Deprecated
public abstract class ModelAdapter {

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ModelAdapter.class);

    private final ISessionManager sessionManager;

    private final ObjectMapper jsonObjectMapper = new ObjectMapper();

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

    public ModelAdapter(ISessionManager sessionManager) {
        this.sessionManager = sessionManager;
        this.sessionManager.registerForNotification(this);
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
    public void initialize(Date selectedTime, String staticSettingID,
            String dynamicSetting_json, String caveMode, String siteID,
            EventBus eventBus) {
        sessionManager.getTimeManager().setSelectedTime(selectedTime);
        sessionManager.getConfigurationManager().setSiteID(siteID);
        sessionManager.getConfigurationManager()
                .changeSettings(staticSettingID);
        if (dynamicSetting_json != null && !dynamicSetting_json.isEmpty()) {
            Settings dynamicSettings = fromJson(dynamicSetting_json,
                    Settings.class);
            sessionManager.getConfigurationManager().getSettings()
                    .apply(dynamicSettings);
        }
    }

    /*
     * HazardDetailPresenter will need to move to front on selection.
     */
    @Deprecated
    public String getLastSelectedEventID() {
        IHazardEvent event = sessionManager.getEventManager()
                .getLastModifiedSelectedEvent();
        if (event != null) {
            return event.getEventID();
        }
        return "";
    }

    /*
     * Use ISessionEventManager.addEvent()
     */
    @Deprecated
    public String newEvent(String eventShape) {
        Event event = fromJson(eventShape, Event.class);
        IHazardEvent hevent = event.toHazardEvent();
        hevent.addHazardAttribute("creationTime", sessionManager
                .getTimeManager().getCurrentTime());
        hevent = sessionManager.getEventManager().addEvent(hevent);
        return hevent.getEventID();
    }

    /*
     * Use IHazardEvent.setGeometry()
     */
    @Deprecated
    public void modifyEventArea(String jsonText) {
        ISessionEventManager eventManager = sessionManager.getEventManager();
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
            event.addHazardAttribute("polyModified", new Boolean(true));
        }
    }

    /*
     * Use ISessionEventManager.removeEvent()
     */
    @Deprecated
    public void reset() {

        ISessionEventManager eventManager = sessionManager.getEventManager();
        for (IHazardEvent event : eventManager.getEvents()) {
            eventManager.removeEvent(event);
        }

        IHazardEventManager manager = new HazardEventManager(
                HazardEventManager.Mode.PRACTICE);
        manager.removeAllEvents();

        /*
         * Reset the VTEC information in the VTEC files. This needs to be done.
         * Otherwise, it is difficult to test against the job sheets and
         * functional tests when the forecaster selects Reset->Events but old
         * VTEC information remains in the VTEC files. This solution will change
         * once VTEC is stored in the database.
         */
        IPathManager pathManager = PathManagerFactory.getPathManager();
        LocalizationContext localizationContext = pathManager.getContext(
                LocalizationContext.LocalizationType.CAVE_STATIC,
                LocalizationContext.LocalizationLevel.USER);

        for (String fileToDelete : filesToDeleteOnReset) {
            LocalizationFile localizationFile = pathManager
                    .getLocalizationFile(localizationContext, fileToDelete);

            if (localizationFile.exists()) {
                try {
                    localizationFile.delete();
                } catch (LocalizationOpFailedException e) {
                    statusHandler.error("Error while reseting.", e);
                }
            }
        }
    }

    /*
     * Use ISessionTimeManager.getSelectedTime()
     */
    @Deprecated
    public Date getSelectedTime() {
        return sessionManager.getTimeManager().getSelectedTime();
    }

    /*
     * Use ISessionTimeManager.setSelectedTime()
     */
    @Deprecated
    public void updateSelectedTime(Date selectedTime_ms) {
        sessionManager.getTimeManager().setSelectedTime(selectedTime_ms);
    }

    /*
     * Use ISessionTimeManager.getSelectedTimeRange()
     */
    @Deprecated
    public String getSelectedTimeRange() {
        TimeRange range = sessionManager.getTimeManager()
                .getSelectedTimeRange();
        return toJson(new String[] { fromDate(range.getStart()),
                fromDate(range.getEnd()) });

    }

    /*
     * Use SimulatedTime.getSystemTime().getTime()
     */
    @Deprecated
    public Date getCurrentTime() {
        return sessionManager.getTimeManager().getCurrentTime();
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
        return fromDate(sessionManager.getTimeManager().getVisibleRange()
                .getStart());
    }

    /*
     * Use ISessionTimeManager.getVisibleRange().getEnd()
     */
    @Deprecated
    public String getTimeLineLatestVisibleTime() {
        return fromDate(sessionManager.getTimeManager().getVisibleRange()
                .getEnd());
    }

    /*
     * Use ISessionConfigurationManager.getSettings().getSettingsID()
     */
    @Deprecated
    public String getCurrentSettingsID() {
        return sessionManager.getConfigurationManager().getSettings()
                .getSettingsID();
    }

    /*
     * Use ISessionConfigurationManager.getSettings()
     */
    @Deprecated
    public String getStaticSettings(String settingsID) {
        return toJson(new Settings(sessionManager.getConfigurationManager()
                .getSettings()));
    }

    /*
     * Use ISessionConfigurationManager.getSettings()
     */
    @Deprecated
    public String getDynamicSettings() {
        Settings dset = sessionManager.getConfigurationManager().getSettings();
        return toJson(dset);
    }

    /*
     * Use
     * ISessionConfigurationManager.getSettings().getDefaultTimeDisplayDuration
     * ()
     */
    @Deprecated
    public String getTimeLineDuration() {
        return Long.toString(sessionManager.getConfigurationManager()
                .getSettings().getDefaultTimeDisplayDuration());
    }

    /*
     * Use ISessionConfigurationManager.getSettingsList()
     */
    @Deprecated
    public String getSettingsList() {
        ISessionConfigurationManager configManager = sessionManager
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
        return toJson(sessionManager.getConfigurationManager().getSettings()
                .getToolbarTools());
    }

    protected abstract IPythonJobListener<List<IGeneratedProduct>> getProductGenerationListener(
            String toolName);

    /*
     * Use ISessionEventManager.addEvent
     */
    @Deprecated
    public String handleRecommenderResult(String toolID,
            EventSet<IEvent> eventList) {
        RecommenderResult result = new RecommenderResult();
        List<Event> events = new ArrayList<Event>();
        for (IEvent event : eventList) {
            if (event instanceof IHazardEvent) {
                IHazardEvent hevent = (IHazardEvent) event;
                hevent = sessionManager.getEventManager().addEvent(hevent);
                Event jevent = new Event(hevent);
                String type = jevent.getType();
                if (type != null) {
                    String headline = sessionManager.getConfigurationManager()
                            .getHeadline(hevent);
                    jevent.setHeadline(headline);
                    jevent.setFullType(type + " (" + headline + ")");
                }
                events.add(jevent);
            }
        }
        result.setResultData(events.toArray(new Event[0]));
        result.setMetaData(sessionManager.getRecommenderEngine()
                .getScriptMetadata(toolID));
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
        Collection<IHazardEvent> events = sessionManager.getEventManager()
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
        Collection<IHazardEvent> events = sessionManager.getEventManager()
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
        Collection<IHazardEvent> hazardEvents = sessionManager
                .getEventManager().getSelectedEvents();

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

        Collection<IHazardEvent> hazardEvents = sessionManager
                .getEventManager().getSelectedEvents();

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
     * Use ISessionEventManager.getEvents() or
     * ISessionEventManager.getCheckedEvents() or
     * ISessionEventManager.getSelectedEvents()
     */

    /*
     * TODO For events that have been replaced (i.e. FL.A to FL.W), this method
     * does returns the old type, not the new. The full type and
     * phen/sig/subtype are all correct. Apparently the type is not used
     * anywhere so nothing bad has happened so far. Solve this when we refactor
     * this method away.
     */
    @Deprecated
    @SuppressWarnings("unchecked")
    public String getComponentData(String component, String eventID) {
        ISessionEventManager eventManager = sessionManager.getEventManager();
        ISessionTimeManager timeManager = sessionManager.getTimeManager();

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
        ISessionConfigurationManager configManager = sessionManager
                .getConfigurationManager();
        Date time = timeManager.getSelectedTime();
        Event[] events2 = new Event[events.size()];
        ArrayNode jevents = jsonObjectMapper.createArrayNode();
        Iterator<IHazardEvent> it = events.iterator();
        for (int i = 0; i < events2.length; i += 1) {
            IHazardEvent hevent = it.next();
            events2[i] = new Event(hevent);

            /*
             * This logic adds hazard color information to an event dict.
             * 
             * This block of code cannot be removed until all Hazard Services
             * views have been converted from using event Dicts to using event
             * objects.
             */
            Color color = configManager.getColor(hevent);
            String fillColor = (int) (color.getRed() * 255) + " "
                    + (int) (color.getGreen() * 255) + " "
                    + (int) (color.getBlue() * 255);
            events2[i].setColor(fillColor);

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
                    } else if (entry.getValue() instanceof Long) {
                        node.put(entry.getKey(), (Long) entry.getValue());
                    } else if (entry.getValue() instanceof List) {
                        ArrayNode tmpArray = jsonObjectMapper.createArrayNode();
                        for (Object obj : (List<Object>) entry.getValue()) {
                            tmpArray.add(obj.toString());
                        }
                        node.put(entry.getKey(), tmpArray);
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
        ISessionConfigurationManager cm = sessionManager
                .getConfigurationManager();
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

    private String fromDate(Date actualDateObject) {
        return Long.toString(actualDateObject.getTime());
    }

    public ISessionManager getSessionManager() {
        return sessionManager;
    }

}
