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
import java.util.concurrent.TimeUnit;

import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;

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
 * Nov  04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Nov 15, 2013  2182       daniel.s.schaffer@noaa.gov    Refactoring JSON - ProductStagingDialog
 * Nov 16, 2013  2166       daniel.s.schaffer@noaa.gov    More constants to help tidy the code
 * Dec 2, 2013  1472      bkowal     subtype is now subType
 * 
 *  
 * Nov 29, 2013 2380    daniel.s.schaffer@noaa.gov Fixing bugs in settings-based filtering
 * 
 * Dec 03, 2013 2182 daniel.s.schaffer@noaa.gov Refactoring - eliminated IHazardsIF
 * Mar 3, 2014  3034    bkowal      Added a constant for the gfe interoperability flag
 * Jun 17, 2014  3982     Chris.Golden  Changed megawidget "side effects" to "interdependencies".
 * Apr 23,2014  1480    jsanchez    Added CORRECTION_FLAG
 * Jul 09, 2014 3214    jsanchez    Added REGENERATE_FLAG
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public final class HazardConstants {

    // part of the hazard lifecycle that the user will see
    public enum HazardStatus {
        PENDING("pending"), POTENTIAL("potential"), PROPOSED("proposed"), ISSUED(
                "issued"), ENDING("ending"), ENDED("ended");
        private final String value;

        private HazardStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static boolean hasEverBeenIssued(HazardStatus status) {
            return issuedButNotEnded(status) || status == ENDED;
        }

        public static boolean issuedButNotEnded(HazardStatus status) {
            return status == ISSUED || status == ENDING;
        }
    }

    public static HazardStatus hazardStatusFromString(String value) {
        return HazardStatus.valueOf(String.valueOf(value).toUpperCase());
    }

    public static List<String> hazardStatusesAsStringList() {
        List<String> vals = new ArrayList<String>();
        for (HazardStatus state : HazardStatus.values()) {
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
        PROPOSE("Propose"), PREVIEW("Preview"), ISSUE("Issue"), CORRECT(
                "Correct");

        private final String value;

        private HazardAction(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Enumeration of all types of changes that may occur within the model.
     */
    public static enum Element {
        EVENTS, CAVE_TIME, CURRENT_TIME, SELECTED_TIME, SELECTED_TIME_RANGE,

        VISIBLE_TIME_DELTA, VISIBLE_TIME_RANGE, SETTINGS, CURRENT_SETTINGS, TOOLS, SITE;
    }

    /**
     * Enumeration of possible geometry types.
     */
    public static enum GeometryType {
        POINT("point"), LINE("line"), POLYGON("polygon");

        private final String value;

        private GeometryType(String value) {
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

    /**
     * Minimum interval in milliseconds allowed between adjacent thumbs in time
     * range widgets, such as those controlling event start/end times and, for
     * some events, rise/crest/fall times.
     */
    public static final long TIME_RANGE_MINIMUM_INTERVAL = TimeUnit.MINUTES
            .toMillis(1L);

    /**
     * Value in milliseconds used to represent that a time is set to
     * "Until Further Notice". This works out to be Tue Jan 19 03:14:07 GMT
     * 2038; this is unfortunately in the not too distant future, but must be
     * this value for interoperability reasons.
     * <p>
     * TODO: Change this value to the millisecond equivalent of Python's
     * <code>date.max</code>, or to {@link Long#MAX_VALUE}, as is decided once
     * interoperability is no longer a concern; see Redmine Task 2904 for
     * details.
     * </p>
     */
    public static final long UNTIL_FURTHER_NOTICE_TIME_VALUE_MILLIS = 2147483647000L;

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
     * The following are used to identify attributes which are returned from
     * River Flood Recommender
     */

    public static final String FLOOD_STAGE = "floodStage";

    public static final String FLOOD_SEVERITY_CATEGORY = "floodSeverity";

    public static final String FLOOD_RECORD = "floodRecord";

    public static final String ACTION_STAGE = "actionStage";

    public static final String CREST_STAGE = "crestStage";

    public static final String CURRENT_STAGE = "currentStage";

    public static final String CURRENT_STAGE_TIME = "currentStageTime";

    public static final String IMMEDIATE_CAUSE = "immediateCause";

    public static final String RIVER_POINT_NAME = "name";

    public static final String RIVER_POINT_ID = "id";

    /*
     * The following are used for any further filters that are required using
     * the getEventsByFilter() method, as well as defining the fields in both
     * the database implementation and the registry implementation
     */

    public static final String SITE_ID = "siteID";

    public static final String GEOMETRY = "geometry";

    public static final String SYMBOL_NEW_LAT_LON = "newLatLon";

    public static final String PHENOMENON = "phenomenon";

    public static final String PHEN_SIG = "phensig";

    public static final String SIGNIFICANCE = "significance";

    public static final String UNIQUE_ID = "uniqueID";

    public static final String EXPIRATION_TIME = "expirationTime";

    public static final String HAZARD_MODE = "hazardMode";

    public static final String RISE_ABOVE = "riseAbove";

    public static final String CREST = "crest";

    public static final String FALL_BELOW = "fallBelow";

    public static final String STREAM_NAME = "streamName";

    public static final String UNTIL_FURTHER_NOTICE_SUFFIX = "UntilFurtherNotice";

    public static final String BEFORE_UNTIL_FURTHER_NOTICE_PREFIX = "__beforeUntilFurtherNotice__";

    public static final String FALL_BELOW_UNTIL_FURTHER_NOTICE = "fallBelow"
            + UNTIL_FURTHER_NOTICE_SUFFIX;

    /**
     * Event identifier key
     */
    public static final String HAZARD_EVENT_IDENTIFIER = "eventID";

    /**
     * Category key for hazard
     */
    public static final String HAZARD_EVENT_CATEGORY = "hazardCategory";

    /**
     * Type key for hazard
     */
    public static final String HAZARD_EVENT_TYPE = "type";

    /**
     * Phen key for hazard
     */
    public static final String HAZARD_EVENT_PHEN = "phen";

    /**
     * Sig key for hazard
     */
    public static final String HAZARD_EVENT_SIG = "sig";

    /**
     * Sub-type key for hazard
     */
    public static final String HAZARD_EVENT_SUB_TYPE = "subType";

    /**
     * Full type key for hazard
     */
    public static final String HAZARD_EVENT_FULL_TYPE = "fullType";

    /**
     * VTEC mode key for hazard
     */
    public static final String HAZARD_EVENT_VTEC_MODE = "VTECmode";

    /**
     * End time key in hazard
     */
    public static final String ISSUE_TIME = "issueTime";

    /**
     * Creation time key in hazard
     */
    public static final String CREATION_TIME = "creationTime";

    /**
     * Start time key in hazard
     */
    public static final String HAZARD_EVENT_START_TIME = "startTime";

    /**
     * End time key in hazard
     */
    public static final String HAZARD_EVENT_END_TIME = "endTime";

    /**
     * The persistence time of the hazard
     */
    public static final String PERSIST_TIME = "persistTime";

    /**
     * interoperability flag. Used to indicate when a hazard has been created
     * based on a GFE grid.
     */
    public static final String GFE_INTEROPERABILITY = "interoperability-gfe";

    /**
     * Attribute name for storing interval between start and end time before the
     * end time "until further notice" was set.
     */
    public static final String END_TIME_INTERVAL_BEFORE_UNTIL_FURTHER_NOTICE = "__beforeUntilFurtherNoticeEndTimeInterval__";

    /**
     * End time "until further notice" key in hazard.
     */
    public static final String HAZARD_EVENT_END_TIME_UNTIL_FURTHER_NOTICE = "endTime"
            + UNTIL_FURTHER_NOTICE_SUFFIX;

    /**
     * Event tracking numbers
     */
    public static final String ETNS = "etns";

    /**
     * Product IDs
     */
    public static final String PILS = "pils";

    /**
     * VTEC codes
     */
    public static final String VTEC_CODES = "vtecCodes";

    /**
     * UGCs
     */
    public static final String UGCS = "ugcs";

    /**
     * Group identifier key in hazard
     */
    public static final String HAZARD_EVENT_GROUP_IDENTIFIER = "groupID";

    /**
     * Shapes key in hazard
     */
    public static final String HAZARD_EVENT_SHAPES = "shapes";

    /**
     * Shape type key in hazard
     */
    public static final String HAZARD_EVENT_SHAPE_TYPE = "shapeType";

    /**
     * Geometry type key in hazard
     */
    public static final String HAZARD_EVENT_GEOMETRY_TYPE = "geometryType";

    /**
     * Circle shape type
     */
    public static final String HAZARD_EVENT_SHAPE_TYPE_CIRCLE = "circle";

    /**
     * Dot shape type
     */
    public static final String HAZARD_EVENT_SHAPE_TYPE_DOT = "dot";

    /**
     * Star shape type
     */
    public static final String HAZARD_EVENT_SHAPE_TYPE_STAR = "star";

    /**
     * Event-is-checked key in hazard
     */
    public static final String HAZARD_EVENT_CHECKED = "checked";

    /**
     * Color key in hazard
     */
    public static final String HAZARD_EVENT_COLOR = "color";

    /**
     * Event-is-selected key in hazard
     */
    public static final String HAZARD_EVENT_SELECTED = "selected";

    /**
     * State key in hazard
     */
    public static final String HAZARD_EVENT_STATUS = "status";

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

    public static final String CORRECTION_FLAG = "correctionFlag";

    public static final String REGENERATE_FLAG = "regenerateFlag";

    public static final String HEADLINE = "headline";

    public static final String REPLACES = "replaces";

    public static final String REPLACED_BY = "replacedBy";

    public static final String GENERATED_PRODUCTS = "generatedProducts";

    public static final String HAZARD_EVENT_SETS = "hazardEventSets";

    public static final String HAZARD_EVENT_IDS = "eventIDs";

    public static final String FIELDS = "fields";

    public static final String LABEL = "label";

    public static final String FIELD_NAME = "fieldName";

    public static final String VISIBLE_CHARS = "visibleChars";

    public static final String MAX_CHARS = "maxChars";

    public static final String FIELD_TYPE = "fieldType";

    public static final String EXPAND_HORIZONTALLY = "expandHorizontally";

    /*
     * The following are related to hazard event metadata.
     */
    public static final String METADATA_KEY = "metadata";

    public static final String SIDE_EFFECTS_SCRIPT_KEY = "interdependencies";

    public static final String ENDING_SYNOPSIS = "endingSynopsis";

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

    public static final String POLYGON_TYPE = "polygon";

    /*
     * Context menu entries.
     */
    public static final String CONTEXT_MENU_CONTRIBUTION_KEY = "contextMenu";

    public static final String CONTEXT_MENU_ADD_REMOVE_SHAPES = "Add/Remove Shapes";

    public static final String CONTEXT_MENU_SELECTED = "ContextMenuSelected";

    public static final String CONTEXT_MENU_BRING_TO_FRONT = "Bring to Front";

    public static final String CONTEXT_MENU_SEND_TO_BACK = "Send to Back";

    public static final String CONTEXT_MENU_HAZARD_INFORMATION_DIALOG = "Hazard Information Dialog";

    public static final String CONTEXT_MENU_CLIP_AND_REDUCE_SELECTED_HAZARDS = "Clip and Reduce Selected Hazards";

    public static final String CONTEXT_MENU_DELETE = "Delete";

    public static final String CONTEXT_MENU_ADD_VERTEX = "Add Vertex";

    public static final String CONTEXT_MENU_DELETE_VERTEX = "Delete Vertex";

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

    /**
     * String column type value in column definition dictionary in setting
     * dictionary.
     */
    public static final String SETTING_COLUMN_TYPE_STRING = "string";

    /**
     * Date column type value in column definition dictionary in setting
     * dictionary.
     */
    public static final String SETTING_COLUMN_TYPE_DATE = "date";

    /**
     * Number column type value in column definition dictionary in setting
     * dictionary.
     */
    public static final String SETTING_COLUMN_TYPE_NUMBER = "number";

    /**
     * Countdown column type value in column definition dictionary in setting
     * dictionary.
     */
    public static final String SETTING_COLUMN_TYPE_COUNTDOWN = "countdown";

    /**
     * Visible columns key in setting dictionary.
     */
    public static final String SETTING_VISIBLE_COLUMNS = "visibleColumns";

    /**
     * Sort direction key in setting dictionary.
     */
    public static final String SETTING_COLUMN_SORT_DIRECTION = "sortDir";

    /**
     * Ascending sort direction value in column definition dictionary in setting
     * dictionary.
     * 
     * TODO These should be converted to an ENUM
     */
    public static final String SETTING_COLUMN_SORT_DIRECTION_ASCENDING = "ascending";

    /**
     * Descending sort direction value in column definition dictionary in
     * setting dictionary.
     */
    public static final String SETTING_COLUMN_SORT_DIRECTION_DESCENDING = "descending";

    /**
     * No sort direction value in column definition dictionary in setting
     * dictionary.
     */
    public static final String SETTING_COLUMN_SORT_DIRECTION_NONE = "none";

    /**
     * Key indicating how to display an empty field in a column.
     */
    public static final String SETTING_COLUMN_DISPLAY_EMPTY_AS = "displayEmptyAs";

    /**
     * Columns key in setting dictionary.
     */
    public static final String SETTING_COLUMNS = "columns";

    /**
     * Hazard categories and types key in setting dictionary.
     */
    public static final String SETTING_HAZARD_CATEGORIES_AND_TYPES = "hazardCategoriesAndTypes";

    /**
     * Hazard categories key setting dictionary.
     */
    public static final String SETTING_HAZARD_CATEGORIES = "hidHazardCategories";

    /*
     * Hazard types key in settings.
     */
    public static final String SETTING_HAZARD_TYPES = "visibleTypes";

    public static final String SETTING_HAZARD_STATES = "visibleStatuses";

    public static final String SETTING_HAZARD_SITES = "visibleSites";

    /*
     * TODO The following need to be moved to something specific to storm track
     */
    public static final String POINTID = "pointID";

    public static final String PIVOTS = "pivots";

    public static final String TRACK_POINTS = "trackPoints";

    public static final String STORM_TRACK_LINE = "stormTrackLine";

    public static final String MODIFY_STORM_TRACK_TOOL = "ModifyStormTrackTool";

    public static final String STATIC_SETTINGS = "staticSettings";

    public static final String SPATIAL_INFO = "spatialInfo";

    /*
     * Constants related to Data Access Framework Requests.
     */
    public static final String TABLE_IDENTIFIER = "table";

    public static final String GEOMETRY_FIELD_IDENTIFIER = "geomField";

    public static final String IN_LOCATION_IDENTIFIER = "inLocation";

    public static final String LOCATION_FIELD_IDENTIFIER = "locationField";

    public static final String CWA_IDENTIFIER = "cwa";

    /*
     * Constants related to retrieving data from the maps database.
     */
    public static final String MAPDATA_COUNTY = "county";

    public static final String MAPDATA_ZONE = "zone";

    public static final String MAPDATA_CWA = "cwa";

    public static final String MAPDATA_BASINS = "basins";

    public static final String MAPDATA_FFMP_BASINS = "ffmp_basins";

    public static final String MAPDATA_FIRE_ZONES = "firewxzones";

    public static final String MAPDATA_COUNTY_LABEL = "countyname";

    public static final String MAPDATA_ZONE_LABEL = "zone";

    public static final String MAPDATA_CWA_LABEL = "cwa";

    public static final String MAPDATA_BASINS_LABEL = "name";

    public static final String MAPDATA_FFMP_BASINS_LABEL = "huc_name";

    public static final String MAPDATA_FIRE_ZONES_LABEL = "name";

    public static final String MAPDATA_MARINE_ZONES = "marinezones";

    public static final String MAPDATA_OFFSHORE = "offshore";

    public static final String NEW_ACTION = "NEW";

    public static final String EXTEND_IN_TIME_ACTION = "EXT";

    public static final String CONTINUE_ACTION = "CON";

    public static final String EXPIRE_ACTION = "EXP";

    public static final String ROUTINE_ACTION = "ROU";

    public static final String CANCEL_ACTION = "CAN";

    /*
     * Constants related to retrieving UGC information from the maps database.
     */
    public static final String UGC_FIPS = "fips";

    public static final String UGC_STATE = "state";

    public static final String UGC_ZONE = "zone";

    public static final String UGC_ID = "id";

    /*
     * TODO The following need to be organized better.
     */
    public static final String CONTINUE_BUTTON = "Continue";

    public static final String CANCEL_BUTTON = "Cancel";

    public static final String IS_SELECTED_KEY = "isSelected";

    public static final String IS_VISIBLE_KEY = "isVisible";

    public static final String SHAPES = "shapes";

    /*
     * Constants related to the python data dictionary keys
     */

    public static final String SEGMENTS = "segments";

    public static final String SEGMENT = "segment";

    public static final String VTEC_RECORDS = "vtecRecords";

    public static final String VTEC_RECORD_TYPE = "vtecRecordType";

    public static final String PVTEC_RECORD = "pvtecRecord";

    public static final String ACTION = "action";

    public static final String SITE = "site";

    public static final String ETN = "eventTrackingNumber";

    public static final String UGC_CODES = "ugcCodes";

    public static final String UGC_CODE = "ugcCode";

    public static final String TEXT = "text";

    /**
     * Drag drop dot identifier.
     */
    public static final String DRAG_DROP_DOT = "DragDropDot";

    /**
     * Persistent shape key.
     */
    public static final String PERSISTENT_SHAPE = "persistentShape";

    /**
     * Timeline navigation key in console dictionary in start up configuration
     * item.
     */
    public static final String START_UP_CONFIG_CONSOLE_TIMELINE_NAVIGATION_BELOW = "belowTimeLine";

    /**
     * Timeline navigation key in console dictionary in start up configuration
     * item.
     */
    public static final String START_UP_CONFIG_CONSOLE_TIMELINE_NAVIGATION = "TimeLineNavigation";

    /**
     * Console key in start up configuration item.
     */
    public static final String START_UP_CONFIG_CONSOLE = "Console";

    /**
     * Point metadata of hazard events key in hazard metadata megawidgets
     * definitions list.
     */
    public static final String HAZARD_INFO_METADATA_MEGAWIDGETS_POINTS_LIST = "pointOptions";

    /**
     * Metadata of hazard events key in hazard metadata megawidgets definitions
     * list.
     */
    public static final String HAZARD_INFO_METADATA_MEGAWIDGETS_LIST = "metaData";

    /**
     * Types of hazard events key in hazard metadata megawidgets definitions
     * list.
     */
    public static final String HAZARD_INFO_METADATA_TYPES = "hazardTypes";

    /**
     * General megawidgets key in hazard information dialog dictionary.
     */
    public static final String HAZARD_INFO_GENERAL_CONFIG_WIDGETS = "hazardCategories";

    /**
     * Key into {@link IHazardEvent} attributes for the time associated with a
     * point in storm track, for example.
     */
    public static final String POINT_TIME = "pointTime";

    /**
     * Maximum time as an epoch time in milliseconds. This is arbitrarily large,
     * but not so large as to be anywhere close to the limit of what a long
     * integer value can represent, so that accidental overflow during
     * calculations does not occur.
     */
    public static final long MAX_TIME = Long.MAX_VALUE / 2L;

    /**
     * Minimum time as an epoch time in milliseconds.
     */
    public static final long MIN_TIME = 0L;

    /**
     * Constants related to localization
     */
    public static final String PYTHON_LOCALIZATION_DIR = "python";

    public static final String PYTHON_LOCALIZATION_UTILITIES_DIR = "localizationUtilities";

    public static final String PYTHON_LOCALIZATION_VTEC_UTILITIES_DIR = "VTECutilities";

    public static final String PYTHON_LOCALIZATION_LOG_UTILITIES_DIR = "logUtilities";

    public static String UGC_PARTS_OF_COUNTY = "ugcPortions";

    public static String UGC_PARTS_OF_STATE = "ugcPartsOfState";

}