/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display;

import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;

import java.util.List;

import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;

/**
 * Model interface, describing the methods that must be implemented in order to
 * create a model for Hazard Services.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Tracy.L.Hansen      Initial induction into repo
 * 
 * </pre>
 * 
 * @author Tracy.L.Hansen
 */
public interface IHazardServicesModel {

    /**
     * Enumeration of all types of changes that may occur within the model.
     */
    public enum Element {
        EVENTS, CAVE_TIME, CURRENT_TIME, SELECTED_TIME, SELECTED_TIME_RANGE, VISIBLE_TIME_DELTA, VISIBLE_TIME_RANGE, SETTINGS, DYNAMIC_SETTING, TOOLS
    };

    // ****************
    // SESSION STATE
    // ****************

    /**
     * initialize: Initializes the Model.
     * 
     * @param selectedTime
     *            : Sets the selected time (in milliseconds)
     * @param currentTime
     *            : Sets the current time (in milliseconds)
     * @param staticSettingsID
     *            : Sets the initial settings, e.g., "Canned TOR", "TOR",
     *            "Canned WSW", "WSW" "Canned Flood", "Flood". This can be the
     *            "displayName" OR the staticSettingsID.
     * @param dynamicSettings_json
     *            : Dynamic settings
     * @param sessionState
     *            : saved session state to initialize from previous session
     */
    public void initialize(String selectedTime, String currentTime,
            String staticSettingID, String dynamicSetting_json,
            String caveMode, String siteID, String state);

    /**
     * getState: Returns the current session state
     * 
     * This can be used for saving the state for a future session The session
     * state includes information such as the selectedTime and selectedEvents
     * 
     * @param saveEvents
     *            -- if True, save the current potential, pending, and leftover
     *            events
     * @return session state
     */
    public String getState(boolean saveState);

    // ********************
    // SELECTED EVENTS
    // ********************
    /**
     * getSelectedEvents
     * 
     * @return: The eventIDs of the selected events
     */
    public String getSelectedEvents();

    /**
     * updateSelectedEvents -- change the set of selected events
     * 
     * @param eventIDsJSON
     *            : A JSON list containing the ids of the selected events
     * 
     * @param originator
     *            : Where this event came from, e.g. "Spatial" or "Temporal"
     * @return updateTime -- IF the current selected time does not overlap the
     *         first selected event, return the new selected time otherwise,
     *         return "None"
     */
    public String updateSelectedEvents(String eventIDs, String originator);

    /**
     * setAddToSelected -- Turn on / off the AddToSelected Mode
     * 
     * If on, then new selected events are added to the set If off, then new
     * selected events replace the current set
     * 
     * @param onOff
     *            --"on" or "off"
     */
    public void setAddToSelected(String onOff);

    /**
     * getLastSelectedEventID
     * 
     * @return the eventID of the most recent selected event
     */
    public String getLastSelectedEventID();

    /**
     * getEventValues -- Return the list of values for the given field and given
     * eventIDs for example, get the list of "callback" values for a set of
     * events
     * 
     * @param eventIDs
     *            -- list of eventIDs
     * @param field
     *            -- name of field e.g. "callback"
     * @param returnType
     *            -- if "json" will return the results in json format
     * @param ignoreState
     *            -- if set to a state (e.g. "potential") then skip events that
     *            have that state
     * @return list of values e.g. names of tools
     */
    public String getEventValues(String eventIDs, String fieldName,
            String returnType, String ignoreState);

    /**
     * checkForEventsWithState -- Search the user-specified events for a
     * specific state.
     * 
     * @param eventIDs
     *            : "all" or a list of specific eventIDs
     * @param searchState
     *            : A single state or list of states to search for e.g.
     *            ["issued", "pending"]
     * @return: True if the state was found or False
     */
    public Boolean checkForEventsWithState(String eventIDs, String searchStates);

    /**
     * newEvent -- This method is called when the user draws a new polygon from
     * the Spatial Display. It creates an event for this polygon.
     * 
     * @param eventArea
     *            : The event dict representing the new event, mainly interested
     *            in the shapes part of it.
     * @return: The eventID of the new event.
     */
    public String newEvent(String eventArea);

    /**
     * updateEventData -- This method is called when the event data is changed.
     * 
     * For example, it is called when the user changes the selected time or the
     * event type in the Hazard Information Dialog
     * 
     * @param updateDicts
     *            : Contains information specific to the update action. For
     *            example, the Console sends something like the following when
     *            an event's time range is modified: {eventID: 1, startTime:
     *            <new start time>, endTime: <new end time>}
     * @param source
     *            : Continuing with the above example, the source would be
     *            "Temporal"
     * 
     */
    public void updateEventData(String jsonText, String source);

    /**
     * modifyEventArea -- Modifies an event, replacing a portion or portions of
     * it with the event provided in jsonText.
     * 
     * @param jsonText
     *            : A JSON string containing a dict to replace portions of the
     *            event with.
     * 
     *            Example modifyDict: {"eventID":"1","shapeType":"polygon",
     *            "shapes": [{"points":[[-103.82006673621783,39.75122509913088],
     *            [-103.78,39.61],[-104.05,39.57],[-104.06,39.67],[-103.83,39.74
     *            ]] "include": "true", }] }
     * 
     */
    public void modifyEventArea(String jsonText);

    /**
     * deleteEvent -- Deletes an event.
     * 
     * @param eventID
     *            : The id of the event to delete.
     */
    public void deleteEvent(String eventIDs);

    /**
     * removeEvents -- Removes events meeting the search criteria.
     * 
     * @param field
     *            : the field to search on
     * @param value
     *            : the value to search on
     */
    public void removeEvents(String field, String value);

    /**
     * handleAction -- Modifies an event based on a curve, riseAbove or
     * fallBelow action
     * 
     * @param action
     *            : "curve", "riseAbove" or "fallBelow"
     * @param actionInfo
     *            : dict containing information about the event.
     */
    public void handleAction(String action, String jsonText);

    /**
     * changeState -- Alters the state of events.
     * 
     * @param eventIDs
     *            : The eventIDs of the events
     * @param state
     *            : The new state
     */
    public void changeState(String eventID, String state);

    /**
     * putHazards -- Writes an event or list of events to the database.
     * 
     * @param eventDicts
     *            : The list of events ... if empty, the selected events are
     *            used.
     */
    public void putHazards();

    /**
     * reset -- Reset to canned events or canned settings.
     * 
     * This will be used in Practice or Test Mode, not Operationally
     * 
     * @param name
     *            : "events" or "settings"
     */
    public void reset(String name);

    /**
     * Sends the selected hazards to the "front" or top of the list of event
     * dicts so that they are displayed on top of other hazards in the spatial
     * display.
     * 
     * @param
     * @return
     */
    public void sendSelectedHazardsToFront();

    /**
     * Sends the selected hazards to the "back" or bottom of the list of event
     * dicts so that they are displayed beneath or under other hazards in the
     * spatial display.
     * 
     * @param
     * @return
     */
    public void sendSelectedHazardsToBack();

    // ********************
    // SELECTED TIME
    // ********************

    /**
     * getSelectedTime --Return the Selected Time displayed in the Console
     * 
     * @return: The selectedTime in milliseconds
     */
    public String getSelectedTime();

    /**
     * updateSelectedTime --Change the Selected Time displayed in the Console
     * 
     * @param selectedTime_ms
     *            : The new selected time, milliseconds
     */
    public void updateSelectedTime(String selectedTime_ms);

    /**
     * getSelectedTimeRange -- Get the Selected Time Range displayed in the
     * Console
     * 
     * @return: The selectedTimeRange (startTime_ms, endTime_ms) in milliseconds
     */
    public String getSelectedTimeRange();

    /**
     * updateSelectedTimeRange -- Update the Selected Time Range displayed in
     * the Console
     * 
     * @param selectedTime_ms
     *            : The new selected time range, milliseconds NOTE -- Cave and
     *            underlying data is always synced to the Selected Time only --
     *            Events are synced with the Selected Time Range if it's there
     *            otherwise with the Selected Time
     */
    public void updateSelectedTimeRange(String startTime_ms, String endTime_ms);

    // ********************
    // CURRENT TIME
    // ********************
    /**
     * getCurrentTime -- Get the current time as displayed in the Console
     * 
     * @return: The currentTime in milliseconds
     */
    public String getCurrentTime();

    /**
     * updateCurrentTime -- Change the current time as displayed in the Console
     * 
     * @param currentTime_ms
     *            : The new current time, milliseconds
     */
    public void updateCurrentTime(String currentTime_ms);

    /**
     * updateFrameInfo
     * 
     * @param framesJSON
     *            : a json string containing the number of frames available
     *            (frameCount), the current frame being displayed in CAVE
     *            (frameIndex), and a list of frame times in milliseconds
     *            (frameTimes).
     */
    public void updateFrameInfo(String framesJSON);

    // ********************
    // CONSOLE VISIBLE TIME
    // ********************

    public String getTimeLineEarliestVisibleTime();

    public String getTimeLineLatestVisibleTime();

    public String setTimeLineVisibleTimes(String earliest_ms, String latest_ms);

    // ********************
    // SETTINGS
    // ********************
    /**
     * "Settings" allow the user to filter the hazards viewed and customize the
     * display to focus on various time scales and locations
     * 
     * An instance of "static" named settings is established on initialization
     * of the Session Manager As the user adjusts the display to filter for
     * various hazards and change the time scale, an instance of "dynamic"
     * settings is created and kept in memory. The user may choose to Save the
     * dynamic settings as named settings. At this point, the dynamic settings
     * is transferred to the static settings. Settings also contain a list of
     * Tools to display on the Toolbar.
     */
    public String getCurrentSettingsID();

    public String getStaticSettings(String settingsID);

    public String newStaticSettings(String settings);

    public void updateStaticSettings(String settings);

    public void deleteStaticSettings(String settings);

    public String getDynamicSettings();

    public String getTimeLineDuration();

    public String getSettingsList();

    public String getToolList();

    // ********************
    // ALERTS
    // ********************
    /**
     * Alerts can be configured by the user. This method returns the current
     * alert configuration.
     */
    public String getAlertConfigValues();

    // ********************
    // CONTEXT MENUS
    // ********************

    /**
     * Button 3 context menu entries are provided based on the currently
     * selected events. These entries are then associated with callbacks to be
     * processed.
     */
    public String getContextMenuEntries();

    public String getContextMenuEntryCallback(String menuItemName);

    // ********************
    // TOOL HANDLER
    // ********************

    /**
     * Methods to run tools (Recommenders and Product Generators) as well as to
     * get the Dialog information, Spatial information required.
     */
    public String runTool(String toolName, String runData);

    public String getDialogInfo(String toolName);

    public String getSpatialInfo(String toolName);

    /**
     * createProductsFromEventIDs -- Generate products from the set of selected
     * events
     * 
     * If a staging dialog is needed, returns a specification for the staging
     * dialog Otherwise, creates the Hazard Event Sets and calls
     * "createProductsFromHazardEventSets"
     * 
     * @param issueFlag
     *            - if True, issue the results
     * @return If a staging dialog is needed, staging information is returned
     *         otherwise, generated products are returned.
     */
    public String createProductsFromEventIDs(String issueFlag);

    /**
     * createProductsFromHazardEventSets -- Generate products from Hazard Event
     * Sets created from the Product Staging Dialog or by the previous method,
     * "createProductsFromEventIDs"
     * 
     * @param issueFlag
     *            -- if True, issue the results
     * @param hazardEventSets
     *            -- each hazard event set (list of events and additional
     *            product information) represents input for a product generator
     * @return a list of generated products
     */
    public String createProductsFromHazardEventSets(String issueFlag,
            String hazardEventSets);

    /**
     * 
     */
    public String handleRecommenderResult(String toolID, List<IEvent> eventList);

    /**
     * Handle the generated products from an asynchronous run of a product
     * generator Collect the results for the list of product generators run When
     * all are collected, issue or display them
     * 
     * @param toolID
     *            -- name of product generator
     * @param generatedProducts
     *            -- list of IGeneratedProduct Java object
     * 
     */
    public String handleProductGeneratorResult(String toolID,
            List<IGeneratedProduct> generatedProductsList);

    // ********************
    // PRESENTER HELPER
    // ********************

    /**
     * getComponentData
     * 
     * @component -- "Temporal", "Spatial", "HID"
     * @eventID -- typically "all" to get information for all selected events
     * @return -- Event information to be displayed for given events The
     *         information is tailored, formatted, and augmented for the
     *         specific needs of each component.
     */
    public String getComponentData(String component, String eventID);

    /**
     * getConfigItem -- get configuration information from localization such as
     * hazardTypes, hazardCategories
     * 
     * @param item
     *            -- specifies the type of configuration requested
     * @return -- configuration information
     */
    public String getConfigItem(String item);

    // ********************
    // TESTING
    // ********************
    /**
     * Persist an event using the JSON representation of its information.
     */
    public void putEvent(String eventDictAsJSON);

    /**
     * Get a String representation of a {@link Dict} representing the event
     * information for the given eventID
     */
    public String getSessionEvent(String eventID);

    /**
     * TODO Provide the real type for the event manager, not Object.
     */
    public void setHazardEventManager(Object hazardEventManager);

    public String[] getHazardsForDynamicSettings();

}
