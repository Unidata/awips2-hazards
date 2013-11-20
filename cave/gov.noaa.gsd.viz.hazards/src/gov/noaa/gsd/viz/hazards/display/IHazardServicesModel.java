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

import java.util.Date;
import java.util.List;

import com.google.common.eventbus.EventBus;
import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.hazards.productgen.IGeneratedProduct;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;

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
 * Jul 15, 2013      585   Chris.Golden        Changed to take an event bus so as
 *                                             to avoid having the latter be a
 *                                             singleton.
 * Aug 06, 2013     1265   Bryon.Lawrence      Updated to support undo/redo.
 * Aug  9, 2013 1921       daniel.s.schaffer@noaa.gov  Support of replacement of JSON with POJOs
 * Nov 04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Nov 20, 2013 2460    daniel.s.schaffer@noaa.gov  Reset now removing all events from practice table
 * </pre>
 * 
 * @author Tracy.L.Hansen
 */
public interface IHazardServicesModel {

    /**
     * Enumeration of all types of changes that may occur within the model.
     */
    public enum Element {
        EVENTS, CAVE_TIME, CURRENT_TIME, SELECTED_TIME, SELECTED_TIME_RANGE, VISIBLE_TIME_DELTA, VISIBLE_TIME_RANGE, SETTINGS, DYNAMIC_SETTING, TOOLS, SITE;
    };

    // ****************
    // SESSION STATE
    // ****************

    /**
     * initialize: Initializes the Model.
     * 
     * @param selectedTime
     *            : Sets the selected time
     * @param staticSettingsID
     *            : Sets the initial settings, e.g., "Canned TOR", "TOR",
     *            "Canned WSW", "WSW" "Canned Flood", "Flood". This can be the
     *            "displayName" OR the staticSettingsID.
     * @param dynamicSettings_json
     *            : Dynamic settings
     * @param eventBus
     *            : Event bus.
     * @param sessionState
     *            : saved session state to initialize from previous session
     */
    public void initialize(Date selectedTime, String staticSettingID,
            String dynamicSetting_json, String caveMode, String siteID,
            EventBus eventBus);

    // ********************
    // SELECTED EVENTS
    // ********************

    /**
     * getLastSelectedEventID
     * 
     * @return the eventID of the most recent selected event
     */
    public String getLastSelectedEventID();

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
     * reset -- Reset to canned events or canned settings.
     * 
     * This will be used in Practice or Test Mode, not Operationally
     * 
     */
    public void reset();

    // ********************
    // SELECTED TIME
    // ********************

    /**
     * getSelectedTime --Return the Selected Time displayed in the Console
     * 
     * @return: The selectedTime
     */
    public Date getSelectedTime();

    /**
     * updateSelectedTime --Change the Selected Time displayed in the Console
     * 
     */
    public void updateSelectedTime(Date selectedTime_ms);

    /**
     * getSelectedTimeRange -- Get the Selected Time Range displayed in the
     * Console
     * 
     * @return: The selectedTimeRange (startTime_ms, endTime_ms) in milliseconds
     */
    public String getSelectedTimeRange();

    // ********************
    // CURRENT TIME
    // ********************
    /**
     * getCurrentTime -- Get the current time as displayed in the Console
     * */
    public Date getCurrentTime();

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

    public String getDynamicSettings();

    public String getTimeLineDuration();

    public String getSettingsList();

    public String getToolList();

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
    public String handleRecommenderResult(String toolID,
            EventSet<IEvent> eventList);

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
     * Undo user edits.
     * 
     * @param
     * @return
     */
    public void undo();

    /**
     * Redo user edits.
     * 
     * @param
     * @return
     */
    public void redo();

    /**
     * Tests if there is an undoable edit.
     * 
     * @param
     * @return
     */
    public Boolean isUndoable();

    /**
     * Tests if there is a redoable edit.
     * 
     * @param
     * @return
     */
    public Boolean isRedoable();

    /**
     * 
     * @param
     * @return current {@link ISessionManager} instance.
     */
    public ISessionManager getSessionManager();
}
