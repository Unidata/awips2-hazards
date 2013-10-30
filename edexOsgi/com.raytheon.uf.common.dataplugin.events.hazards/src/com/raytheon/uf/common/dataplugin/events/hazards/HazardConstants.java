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
package com.raytheon.uf.common.dataplugin.events.hazards;

import java.util.ArrayList;
import java.util.List;

/**
 * Contants to be used by both Java and Python code, Python will have a class
 * that mirrors many of the values within this class for use by the tool writers
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 12, 2012            mnash     Initial creation
 * Aug 01, 2013  1325      daniel.s.schaffer@noaa.gov     Added support for alerting
 * Aug 10, 2013  1265      blawrenc  Added more constants. This helps
 *                                   to clean up the use of strings
 *                                   as keys in other code modules.
 * Aug 21, 2013 1921       daniel.s.schaffer@noaa.gov  Call recommender framework directly
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public final class HazardConstants {

    // part of the hazard lifecycle that the user will see
    public enum HazardState {
        PENDING("pending"), POTENTIAL("potential"), PROPOSED("proposed"), ISSUED(
                "issued"), ENDED("ended");
        private final String value;

        private HazardState(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static HazardState hazardStateFromString(String value) {
        return HazardState.valueOf(String.valueOf(value).toUpperCase());
    }

    public static List<String> hazardStatesAsStringList() {
        List<String> vals = new ArrayList<String>();
        for (HazardState state : HazardState.values()) {
            vals.add(state.toString().toLowerCase());
        }
        return vals;
    }

    public static enum ProductClass {
        OPERATIONAL("O"), TEST("T"), EXPERIMENTAL("E"), EXPERIMENTAL_IN_CURRENT(
                "X");

        private final String abbreviation;

        private ProductClass(String value) {
            this.abbreviation = value;
        }

        /**
         * @return the abbreviation
         */
        public String getAbbreviation() {
            return abbreviation;
        }
    }

    public static ProductClass productClassFromAbbreviation(String value) {
        for (ProductClass clazz : ProductClass.values()) {
            if (clazz.getAbbreviation().equals(value)) {
                return clazz;
            }
        }
        throw new IllegalArgumentException("No enum const "
                + ProductClass.class.getName() + "." + value);
    }

    public static ProductClass productClassFromName(String value) {
        return ProductClass.valueOf(String.valueOf(value).toUpperCase());
    }

    public static List<String> productClassesAsStringList() {
        List<String> vals = new ArrayList<String>();
        for (ProductClass clazz : ProductClass.values()) {
            vals.add(clazz.getAbbreviation());
        }
        return vals;
    }

    public static enum Significance {
        WARNING("W"), WATCH("A"), ADVISORY("Y"), OUTLOOK("O"), STATEMENT("S"), FORECAST(
                "F"), SYNOPSIS("N");
        private final String abbreviation;

        private Significance(String value) {
            this.abbreviation = value;
        }

        /**
         * @return the abbreviation
         */
        public String getAbbreviation() {
            return abbreviation;
        }
    }

    public static enum HazardAction {
        PROPOSE("Propose"), PREVIEW("Preview"), ISSUE("Issue");

        private final String value;

        private HazardAction(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    public static Significance significanceFromAbbreviation(String value) {
        for (Significance clazz : Significance.values()) {
            if (clazz.getAbbreviation().equals(value)) {
                return clazz;
            }
        }
        throw new IllegalArgumentException("No enum const "
                + Significance.class.getName() + "." + value);
    }

    public static Significance significanceFromName(String value) {
        return Significance.valueOf(String.valueOf(value).toUpperCase());
    }

    public static List<String> significancesAsStringList() {
        List<String> vals = new ArrayList<String>();
        for (Significance sig : Significance.values()) {
            vals.add(sig.getAbbreviation());
        }
        return vals;
    }

    /*
     * The following constants are for use with the hazard attributes object,
     * for use with keys. Also used with the required fields.
     */

    public static final String CALL_TO_ACTION = "Call To Action";

    public static final String BASIS_FOR_WARNING = "Basis For Warning";

    public static final String THREAT = "Threat";

    public static final String FLOOD_CREST_TIME = "Flood Crest Time";

    public static final String FLOOD_SEVERITY = "Flood Severity";

    public static final String FLOOD_BEGIN_TIME = "Flood Begin Time";

    public static final String FLOOD_END_TIME = "Flood End Time";

    public static final String FLOOD_RECORD_STATUS = "Flood Record Status";

    public static final String FLOOD_IMMEDIATE_CAUSE = "Flood Immediate Cause";

    /*
     * The following are used for any further filters that are required using
     * the getEventsByFilter() method, as well as defining the fields in both
     * the database implementation and the registry implementation
     */

    public static final String SITEID = "siteID";

    public static final String GEOMETRY = "geometry";

    public static final String SYMBOL_NEW_LAT_LON = "newLatLon";

    public static final String STARTTIME = "startTime";

    public static final String PHENOMENON = "phenomenon";

    public static final String PHENSIG = "phensig";

    public static final String SIGNIFICANCE = "significance";

    public static final String SUBTYPE = "subtype";

    public static final String EVENTID = "eventID";

    public static final String UNIQUEID = "uniqueID";

    public static final String STATE = "state";

    public static final String ENDTIME = "endTime";

    public static final String ISSUETIME = "issueTime";

    public static final String EXPIRATIONTIME = "expirationTime";

    public static final String HAZARDMODE = "hazardMode";

    public static final String RISE_ABOVE = "riseAbove";

    public static final String CREST = "crest";

    public static final String FALL_BELOW = "fallBelow";

    public static final String TYPE = "type";

    public static final String CAUSE = "cause";

    public static final String COLOR = "color";

    /*
     * The following are used to identify elements in the session state.
     */
    public static final String SESSION_DICT = "sessionDict";

    public static final String CURRENT_TIME = "currentTime";

    public static final String BACKUP_SITEID = "backupSiteID";

    public static final String TEST_MODE = "testMode";

    /*
     * The following are related to product generation.
     */
    public static final String ISSUE_FLAG = "issueFlag";

    public static final String HEADLINE = "headline";

    public static final String PREVIEW_STATE = "previewState";

    public static final String REPLACES = "replaces";

    public static final String GENERATED_PRODUCTS = "generatedProducts";

    public static final String HAZARD_EVENT_SETS = "hazardEventSets";

    /*
     * The following are related to hazard geometries and their supporting meta
     * information.
     */
    public static final String FORECAST_POINT = "forecastPoint";

    public static final String GEO_TYPE = "geoType";

    public static final String POINT_TYPE = "point";

    public static final String LINE_TYPE = "line";

    public static final String AREA_TYPE = "area";

    public static final String POINTS = "points";

    /*
     * Information related to the type of a reset action.
     */
    public static final String RESET_EVENTS = "Events";

    public static final String RESET_SETTINGS = "Settings";

    public static final String RESET_ACTION = "Reset";

    /*
     * Context menu entries.
     */
    public static final String CONTEXT_MENU_CONTRIBUTION_KEY = "contextMenu";

    public static final String CONTEXT_MENU_ADD_REMOVE_SHAPES = "Add/Remove Shapes";

    public static final String CONEXT_MENU_SELECTED = "ContextMenuSelected";

    public static final String CONETXT_MENU_BRING_TO_FRONT = "Bring to Front";

    public static final String CONTEXT_MENU_SEND_TO_BACK = "Send to Back";

    public static final String CONTEXT_MENU_HAZARD_INFORMATION_DIALOG = "Hazard Information Dialog";

    public static final String CONTEXT_MENU_SAVE = "Save";

    public static final String CONTEXT_MENU_REMOVE_POTENTIAL_HAZARDS = "Remove Potential Hazards";

    public static final String CONTEXT_MENU_DELETE = "Delete";

    public static final String CONTEXT_MENU_ADD_NODE = "Add Node";

    public static final String CONTEXT_MENU_DELETE_NODE = "Delete Node";

    public static final String CONTEXT_MENU_END = "End";

    public static final String CONTEXT_MENU_ISSUE = "Issue";

    public static final String CONTEXT_MENU_PROPOSE = "Propose";

    /*
     * Constants specific to draw-by-area hazard creation operations.
     */
    public static final String GEOMETRY_REFERENCE_KEY = "geometryReference";

    public static final String GEOMETRY_MAP_NAME_KEY = "geometryMapName";

    public static final String NEW_EVENT_SHAPE = "NewEventShape";

    /*
     * Constants relating to settings
     */
    public static final String HYDROLOGY_SETTING = "Hydrology";

    /*
     * Constants relating to hazard actions
     */
    public static final String UPDATE_EVENT_METADATA = "updateEventMetadata";

    public static final String UPDATE_EVENT_TYPE = "updateEventType";

    public static final String SELECTED_EVENTS_CHANGED = "SelectedEventsChanged";

    public static final String MODIFY_EVENT_AREA = "ModifyEventArea";

    /*
     * TODO The following need to be moved to something specific to storm track
     */
    public static final String POINTID = "pointID";

    public static final String SHAPES = "shapes";

    public static final String PIVOTS = "pivots";

    public static final String TRACK_POINTS = "trackPoints";

    public static final String STORM_TRACK_LINE = "stormTrackLine";

    public static final String MODIFY_STORM_TRACK_TOOL = "ModifyStormTrackTool";

    public static final String STATIC_SETTINGS = "staticSettings";

    public static final String RUN_AUTOMATED_TESTS = "runAutomatedTests";

    public static final String END_SELECTED_HAZARDS = "End Selected Hazards";

    public static final String PROPOSE_SELECTED_HAZARDS = "Propose Selected Hazards";

    public static final String REMOVE_POTENTIAL_HAZARDS = "Remove Potential Hazards";
}
