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

import gov.noaa.gsd.common.utilities.TimeResolution;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import com.google.common.collect.ImmutableMap;

/**
 * Constants to be used by both Java and Python code, Python will have a class
 * that mirrors many of the values within this class for use by the tool
 * writers.
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
 *                                   to clean up the use of stings
 *                                   as keys in other code modules.
 * Aug 21, 2013 1921       daniel.s.schaffer@noaa.gov  Call recommender framework directly
 * Nov  04, 2013 2182     daniel.s.schaffer@noaa.gov      Started refactoring
 * Nov 15, 2013  2182       daniel.s.schaffer@noaa.gov    Refactoring JSON - ProductStagingDialog
 * Nov 16, 2013  2166       daniel.s.schaffer@noaa.gov    More constants to help tidy the code
 * Nov 29, 2013 2380    daniel.s.schaffer@noaa.gov Fixing bugs in settings-based filtering
 * Dec 2, 2013  1472      bkowal     subtype is now subType
 * Dec 03, 2013 2182 daniel.s.schaffer@noaa.gov Refactoring - eliminated IHazardsIF
 * Mar 3, 2014  3034    bkowal      Added a constant for the gfe interoperability flag
 * Jun 17, 2014  3982     Chris.Golden  Changed megawidget "side effects" to "interdependencies".
 * Apr 23,2014  1480    jsanchez    Added CORRECTION_FLAG
 * Jul 09, 2014 3214    jsanchez    Added REGENERATE_FLAG
 * Aug 15, 2014 4243      Chris.Golden  Changed megawidget-metadata-fetching related constants.
 * Sep 04, 2014 4560      Chris.Golden  Added constant for metadata-reload-triggering megawidgets.
 * Sep 09, 2014 4042      Chris.Golden  Added constant for UI scrolling increment.
 * Oct 03, 2014 4918      Robert.Blum   Added constant for hazard metadata.
 * Oct 08, 2014 4042      Chris.Golden  Added constants for productgen-related Python import path.
 * Oct 22, 2014 4818      Chris.Golden  Removed constant for UI scrolling increment (moved to
 *                                      UiBuilder).
 * Nov 18, 2014 4124      Chris.Golden  Removed unused change element.
 * Dec 05, 2014 4124      Chris.Golden  Added sort priority constants.
 * Jan 22, 2015 4959      Dan Schaffer  MB3 to add/remove UGCs to a hazard
 * Jan 29, 2015 3626      Chris.Golden  Added event type constant for recommenders.
 * Jan 29, 2015 5005      Dan Schaffer  Fixed bug in MB3 context menu for showing product geometry
 * Feb 12, 2015 4959      Dan Schaffer  Modify MB3 add/remove UGCs to match Warngen
 * Feb 15, 2015 2271      Dan Schaffer  Incur recommender/product generator init costs immediately
 * Feb 22, 2015 6561      Mike Duff     Removed PERSIST_TIME
 * Feb 17, 2015 3847      Chris.Golden  Added edit-rise-crest-fall metadata trigger constant.
 * Feb 19, 2015 5071      Robert.Blum   Added HAZARD_CATEGORIES_LOCALIZATION_DIR
 * Feb 23, 2015 3618      Chris.Golden  Added possible sites to settings.
 * May 18, 2015 8227      Chris.Cody    Remove NullRecommender
 * May 29, 2015 6895      Ben.Phillippe Refactored Hazard Service data access
 * Jul 29, 2015 9306      Chris.Cody    Add HazardSatus.ELAPSED status
 * Jul 31, 2015 7458      Robert.Blum   Added new USER_NAME and WORKSTATION constants.
 * Aug 06, 2015 9968      Chris.Cody    Added Ended/Elapsed time status checking
 * Aug 18, 2015 9650      Chris.Golden  Added "deleteEventIdentifiers" constant for recommenders.
 * Sep 28, 2015 10302,8167 hansen       Added backupSites, eventIdDisplayType, mapCenter
 * Nov 10, 2015 12762     Chris.Golden  Added constants and enums related to running tools.
 * Nov 17, 2015 3473      Robert.Blum   Moved localization constants to HazardsConfigurationConstants.
 * Jan 14, 2016 12935     Robert.Blum   Removed unused FALL_BELOW_UNTIL_FURTHER_NOTICE constant.
 * Jan 28, 2016 12762     Chris.Golden  Changed constant for attribute identifiers when running
 *                                      tools to reflect that it now means identifiers plural,
 *                                      not a single identifier.
 * Feb 19, 2016 15014     Robert.Blum   Fixed determination of ugcs for point hazards.
 * Feb 24, 2016 14667     Robert.Blum   Limiting Flash Flood Recommender to basins inside the CWA.
 * Mar 01, 2016 15676     Chris.Golden  Changed to support visual features.
 * Apr 05, 2016 16885     Chris.Golden  Added definitions for boolean column type in temporal
 *                                      display.
 * Apr 27, 2016 18266     Chris.Golden  Added support for event-driven tools triggered by data
 *                                      layer changes.
 * May 13, 2016 15676     Chris.Golden  Added database as a recommender trigger origin.
 * Jun 10, 2016 19537     Chris.Golden  Combined base and selected visual feature lists for each
 *                                      hazard event into one, replaced by visibility constraints
 *                                      based upon selection state to individual visual features.
 * Jun 23, 2016 19537     Chris.Golden  Added recommender-related constants, and removed storm-
 *                                      track-specific constants.
 * Jul 25, 2016 19537     Chris.Golden  Changed as part of partial MVP refactor for spatial
 *                                      display.
 * Jul 26, 2016 20755     Chris.Golden  Added constant for recommender result event set attribute.
 * Jul 27, 2016 19924     Chris.Golden  Changed recommmender-related data layer update constants
 *                                      to reflect the fact that now all data times are wanted,
 *                                      not just the latest one.
 * Aug 31, 2016 15934     Chris.Golden  Added constant related to advanced geometries.
 * Oct 05, 2016 22870     Chris.Golden  Added frame change to triggers.
 * Oct 19, 2016 21873     Chris.Golden  Added time resolution.
 * Feb 01, 2017 15556     Chris.Golden  Removed obsolete elements, and added constants for the
 *                                      console refactor.
 * Feb 16, 2017 29138     Chris.Golden  Removed unneeded visibility-in-history-list constant and
 *                                      added new recommender result event set attribute name
 *                                      for saving to the history list.
 * Mar 16, 2017 15528     Chris.Golden  Removed hazard event checked attribute, and added
 *                                      metadata megawidget property to indicate that a
 *                                      particular metadata value being changed does not affect
 *                                      the modify flag of the enclosing hazard event.
 * Mar 30, 2017 15528     Chris.Golden  Added recommender result event set property to indicate
 *                                      whether or not to treat saved events as issuances or
 *                                      not.
 * Apr 04, 2017 32732     Chris.Golden  Added constant for indicating whether or not the origin
 *                                      (user name and workstation identifier) should be updated
 *                                      when a recommender returns modified event(s).
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public final class HazardConstants {

    /**
     * Map pairing time resolutions with the truncation units to be used when
     * rounding down a time to that particular resolution.
     */
    public static final Map<TimeResolution, Integer> TRUNCATION_UNITS_FOR_TIME_RESOLUTIONS;
    static {
        Map<TimeResolution, Integer> map = new EnumMap<>(TimeResolution.class);
        map.put(TimeResolution.SECONDS, Calendar.SECOND);
        map.put(TimeResolution.MINUTES, Calendar.MINUTE);
        TRUNCATION_UNITS_FOR_TIME_RESOLUTIONS = ImmutableMap.copyOf(map);
    }

    /**
     * Key for time resolution in settings.
     */
    public static final String TIME_RESOLUTION = "timeResolution";

    /**
     * Key for a hazard's label in a spatial input parameters dictionary.
     */
    public static final String SPATIAL_PARAMETERS_HAZARD_LABEL = "label";

    /**
     * Possible return types for hazard dictionaries
     */
    public static final String SPATIAL_PARAMETERS_RETURN_TYPE = "returnType";

    public static final String SPATIAL_PARAMETERS_RETURN_TYPE_POINT = "Point";

    // Frame information parameter names.

    public static final String FRAMES_INFO = "framesInfo";

    public static final String FRAME_TIMES = "frameTimeList";

    public static final String FRAME_INDEX = "frameIndex";

    public static final String FRAME_COUNT = "frameCount";

    public static final String CURRENT_FRAME = "currentFrame";

    public static final Integer NO_FRAMES_INDEX = -1;

    // Recommender execution context parameter names.

    public static final String RECOMMENDER_EXECUTION_TRIGGER = "trigger";

    public static final String RECOMMENDER_EVENT_TYPE = "eventType";

    public static final String RECOMMENDER_TRIGGER_EVENT_IDENTIFIER = "eventIdentifier";

    public static final String RECOMMENDER_TRIGGER_ATTRIBUTE_IDENTIFIERS = "attributeIdentifiers";

    public static final String RECOMMENDER_TRIGGER_ORIGIN = "origin";

    // Recommender metadata keys.

    public static final String RECOMMENDER_METADATA_TOOL_NAME = "toolName";

    public static final String RECOMMENDER_METADATA_BACKGROUND = "background";

    public static final String RECOMMENDER_METADATA_ONLY_INCLUDE_TRIGGER_EVENT = "onlyIncludeTriggerEvent";

    public static final String RECOMMENDER_METADATA_INCLUDE_EVENT_TYPES = "includeEventTypes";

    public static final String RECOMMENDER_METADATA_INCLUDE_DATA_LAYER_TIMES = "includeDataLayerTimes";

    public static final String RECOMMENDER_METADATA_INCLUDE_CWA_GEOMETRY = "includeCwaGeometry";

    // Recommender method names

    public static final String RECOMMENDER_GET_DIALOG_INFO_METHOD = "getDialogInfo";

    public static final String RECOMMENDER_GET_METADATA_METHOD = "getScriptMetadata";

    public static final String RECOMMENDER_GET_SPATIAL_INFO_METHOD = "getSpatialInfo";

    // Recommender result attribute names

    public static final String RECOMMENDER_RESULT_SAVE_TO_DATABASE = "saveToDatabase";

    public static final String RECOMMENDER_RESULT_SAVE_TO_HISTORY = "saveToHistory";

    public static final String RECOMMENDER_RESULT_TREAT_AS_ISSUANCE = "treatAsIssuance";

    public static final String RECOMMENDER_RESULT_SET_ORIGIN = "setOrigin";

    public static final String RECOMMENDER_RESULT_DELETE_EVENT_IDENTIFIERS = "deleteEventIdentifiers";

    /**
     * Types of changes or events that may trigger a recommender execution.
     */
    public enum Trigger {
        NONE("none"), HAZARD_TYPE_FIRST("hazardTypeFirst"), HAZARD_EVENT_MODIFICATION(
                "hazardEventModification"), HAZARD_EVENT_VISUAL_FEATURE_CHANGE(
                "hazardEventVisualFeatureChange"), TIME_INTERVAL("timeInterval"), FRAME_CHANGE(
                "frameChange"), DATA_LAYER_UPDATE("dataLayerUpdate");

        // Private Variables

        /**
         * Identifier of the type.
         */
        private final String identifier;

        // Private Constructors

        /**
         * Construct a standard instance.
         * 
         * @param identifier
         *            Identifier of the type.
         */
        private Trigger(String identifier) {
            this.identifier = identifier;
        }

        // Public Methods

        @Override
        public String toString() {
            return identifier;
        }
    }

    /**
     * Possible origins of a triggered recommender execution.
     */
    public enum RecommenderTriggerOrigin {
        USER("user"), DATABASE("database"), OTHER("other");

        // Private Variables

        /**
         * Identifier of the type.
         */
        private final String identifier;

        // Private Constructors

        /**
         * Construct a standard instance.
         * 
         * @param identifier
         *            Identifier of the type.
         */
        private RecommenderTriggerOrigin(String identifier) {
            this.identifier = identifier;
        }

        // Public Methods

        @Override
        public String toString() {
            return identifier;
        }
    }

    /**
     * First-class attributes of hazard events.
     */
    public enum HazardEventFirstClassAttribute {
        TIME_RANGE(HAZARD_EVENT_TIME_RANGE), GEOMETRY(HazardConstants.GEOMETRY), STATUS(
                HAZARD_EVENT_STATUS), VISUAL_FEATURE(
                HAZARD_EVENT_VISUAL_FEATURE);

        // Private Static Constants

        /**
         * Map of identifiers to instances.
         */
        private static final Map<String, HazardEventFirstClassAttribute> INSTANCES_FOR_IDENTIFIERS;
        static {
            Map<String, HazardEventFirstClassAttribute> map = new HashMap<>();
            for (HazardEventFirstClassAttribute value : values()) {
                map.put(value.toString(), value);
            }
            INSTANCES_FOR_IDENTIFIERS = ImmutableMap.copyOf(map);
        }

        // Private Variables

        /**
         * Identifier of the type.
         */
        private final String identifier;

        // Public Static Methods

        /**
         * Get the instance with the specified identifier.
         * 
         * @param identifier
         *            Identifier.
         * @return Instance, or <code>null</code> if there is no instance with
         *         the specified identifier.
         */
        public static HazardEventFirstClassAttribute getInstanceWithIdentifier(
                String identifier) {
            return INSTANCES_FOR_IDENTIFIERS.get(identifier);
        }

        // Private Constructors

        /**
         * Construct a standard instance.
         * 
         * @param identifier
         *            Identifier of the type.
         */
        private HazardEventFirstClassAttribute(String identifier) {
            this.identifier = identifier;
        }

        // Public Methods

        @Override
        public String toString() {
            return identifier;
        }
    }

    // part of the hazard lifecycle that the user will see
    public enum HazardStatus {
        PENDING("pending"), POTENTIAL("potential"), PROPOSED("proposed"), ISSUED(
                "issued"), ELAPSED("elapsed"), ENDING("ending"), ENDED("ended");
        private final String value;

        private HazardStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        public static boolean hasEverBeenIssued(HazardStatus status) {
            return issuedButNotEndedOrElapsed(status) || status == ELAPSED
                    || status == ENDED;
        }

        public static boolean issuedButNotEndedOrElapsed(HazardStatus status) {
            return status == ISSUED || status == ENDING;
        }

        public static boolean endingEndedOrElapsed(HazardStatus status) {
            return status == ENDING || status == ENDED || status == ELAPSED;
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
                "F"), SYNOPSIS("N"), CONVECTIVE("Convective");
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
        ISSUE("Issue"), CORRECT("Correct");

        private final String value;

        private HazardAction(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * Enumeration of all types of changes that may occur within the model. This
     * is deprecated, and values are being removed from the enum as refactoring
     * goes on and the Model-View-Presenter architecture is more fully
     * implemented.
     */
    @Deprecated
    public static enum Element {
        SETTINGS, CURRENT_SETTINGS;
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

    /**
     * Dialog info key for recommender gage identifier.
     */
    public static final String SELECTED_POINT_ID = "selectedPointID";

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
     * the getHistoryByFilter() method, as well as defining the fields in both
     * the database implementation and the registry implementation
     */
    public static final String MODIFIED = "modified";

    public static final String SITE_ID = "siteID";

    public static final String GEOMETRY = "geometry";

    public static final String VISIBLE_GEOMETRY = "visibleGeometry";

    public static final String HIGH_RESOLUTION_GEOMETRY_IS_VISIBLE = "highResolutionGeometryIsVisible";

    public static final String LOW_RESOLUTION_GEOMETRY_IS_VISIBLE = "lowResolutionGeometryIsVisible";

    public static final String LOW_RESOLUTION_GEOMETRY = "lowResolutionGeometry";

    public static final String FLATTENED_GEOMETRY = "flattenedGeometry";

    public static final String VISUAL_FEATURES = "visualFeatures";

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

    /**
     * Event identifier key
     */
    public static final String HAZARD_EVENT_IDENTIFIER = "eventID";

    /**
     * Displayable event identifier key.
     */
    public static final String HAZARD_EVENT_DISPLAY_IDENTIFIER = "displayEventID";

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
     * EventSet attribute key for vtecMode
     */
    public static final String VTEC_MODE = "vtecMode";

    /**
     * EventSet attribute key for runMode
     */
    public static final String RUN_MODE = "runMode";

    /**
     * End time key in hazard
     */
    public static final String ISSUE_TIME = "issueTime";

    /**
     * Insert time key in hazard
     */

    public static final String INSERT_TIME = "insertTime";

    /**
     * Creation time key in hazard
     */
    public static final String CREATION_TIME = "creationTime";

    /**
     * Time range in hazard (start time to end time).
     */
    public static final String HAZARD_EVENT_TIME_RANGE = "timeRange";

    /**
     * Visual feature.
     */
    public static final String HAZARD_EVENT_VISUAL_FEATURE = "visualFeature";

    /**
     * Start time key in hazard
     */
    public static final String HAZARD_EVENT_START_TIME = "startTime";

    /**
     * End time key in hazard
     */
    public static final String HAZARD_EVENT_END_TIME = "endTime";

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
     * The UGC hatching algorithms
     */

    /*
     * TODO. Would have preferred to use an enum here but the serialization
     * fails during the conversion to python in product generation.
     */
    public static final String HAZARD_AREA = "hazardArea";

    public static final String HAZARD_AREA_ALL = "hazardAreaAll";

    public static final String HAZARD_AREA_NONE = "hazardAreaNone";

    public static final String HAZARD_AREA_INTERSECTION = "hazardAreaIntersection";

    /**
     * Group identifier key in hazard
     */
    public static final String HAZARD_EVENT_GROUP_IDENTIFIER = "groupID";

    /**
     * Shapes key in hazard
     */
    public static final String HAZARD_EVENT_SHAPES = "shapes";

    /**
     * Color key in hazard
     */
    public static final String HAZARD_EVENT_COLOR = "color";

    /**
     * Event-is-selected key in hazard.
     * 
     * @deprecated Should not be used once recommenders, etc. switch to being
     *             told which events are selected via a separate set of event
     *             identifiers
     */
    @Deprecated
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

    public static final String CENTER_POINT_LAT_LON = "centerPointLatLon";

    public static final String COORDINATE_LAT = "lat";

    public static final String COORDINATE_LON = "lon";

    public static final String SELECTED_TIME = "selectedTime";

    public static final String BACKUP_SITEID = "backupSiteID";

    public static final String TEST_MODE = "testMode";

    public static final String DATA_TIMES = "dataLayerTimes";

    public static final String CWA_GEOMETRY = "cwaGeometry";

    /*
     * The following are related to product generation.
     */
    /**
     * Legacy ASCII Product Key
     */
    public static final String ASCII_PRODUCT_KEY = "Legacy";

    /**
     * Partner XML Product Key
     */
    public static final String XML_PRODUCT_KEY = "XML";

    /**
     * Partner CAP Product Key
     */
    public static final String CAP_PRODUCT_KEY = "CAP";

    /**
     * Product identifier
     */
    public static final String PRODUCT_ID = "productID";

    /**
     * Key for the collection of products generated.
     */
    public static final String PRODUCTS = "products";

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

    public static final String USER_NAME = "userName";

    public static final String WORKSTATION = "workStation";

    /*
     * The following are related to hazard event metadata and recommender
     * dialogs.
     */
    public static final String METADATA_KEY = "metadata";

    public static final String METADATA_RELOAD_TRIGGER = "refreshMetadata";

    public static final String METADATA_NOT_CONSIDERED_MODIFICATION = "doesNotAffectModifyFlag";

    public static final String RECOMMENDER_RUN_TRIGGER = "modifyRecommender";

    public static final String METADATA_EDIT_RISE_CREST_FALL = "editRiseCrestFall";

    public static final String FILE_PATH_KEY = "filePath";

    public static final String EVENT_MODIFIERS_KEY = "eventModifiers";

    public static final String ENDING_SYNOPSIS = "endingSynopsis";

    /**
     * Key for values dictionary within the recommender dialog.
     */
    public static final String VALUES_DICTIONARY_KEY = "valueDict";

    /**
     * Key for run tool triggers list in the recommender dialog.
     */
    public static final String RUN_TOOL_TRIGGERS_LIST_KEY = "runToolTriggers";

    /**
     * Key for title text.
     */
    public static final String TITLE_KEY = "title";

    /**
     * Maximum initial width key.
     */
    public static final String MAX_INITIAL_WIDTH_KEY = "maxInitialWidth";

    /**
     * Maximum initial height key.
     */
    public static final String MAX_INITIAL_HEIGHT_KEY = "maxInitialHeight";

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

    public static final String CONTEXT_MENU_BRING_TO_FRONT = "Front";

    public static final String CONTEXT_MENU_SEND_TO_BACK = "Back";

    public static final String CONTEXT_MENU_HAZARD_INFORMATION_DIALOG = "Hazard Information Dialog";

    public static final String CONTEXT_MENU_HIGH_RESOLUTION_GEOMETRY_FOR_SELECTED_EVENTS = "Show High Resolution Geometies For Selected Events";

    public static final String CONTEXT_MENU_LOW_RESOLUTION_GEOMETRY_FOR_SELECTED_EVENTS = "Show Low Resolution Geometries For Selected Events";

    public static final String CONTEXT_MENU_HIGH_RESOLUTION_GEOMETRY_FOR_CURRENT_EVENT = "Show High Resolution Geometry For This";

    public static final String CONTEXT_MENU_LOW_RESOLUTION_GEOMETRY_FOR_CURRENT_EVENT = "Show Low Resolution Geometry For This";

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
     * Group megawidget field type for settings.
     */
    public static final String SETTING_FIELD_TYPE_GROUP = "Group";

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
     * Boolean column type value in column definition dictionary in setting
     * dictionary.
     */
    public static final String SETTING_COLUMN_TYPE_BOOLEAN = "boolean";

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
     * Sort priority key in setting dictionary.
     */
    public static final String SETTING_COLUMN_SORT_PRIORITY = "sortPriority";

    /**
     * Value indicating no sort priority when made the value associated with the
     * {@link #SETTING_COLUMN_SORT_PRIORITY} key in a setting dictionary.
     */
    public static final int SETTING_COLUMN_SORT_PRIORITY_NONE = 0;

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

    /*
     * Hazard types key in settings.
     */
    public static final String SETTING_HAZARD_TYPES = "visibleTypes";

    public static final String SETTING_HAZARD_STATES = "visibleStatuses";

    public static final String SETTING_HAZARD_POSSIBLE_SITES = "possibleSites";

    public static final String SETTING_HAZARD_SITES = "visibleSites";

    public static final String BACKUP_HAZARD_SITES = "backupSites";

    public static final String POINTID = "pointID";

    public static final String MAP_CENTER = "mapCenter";

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

    public static final String UGC_ZONE_ABBREVIATION = "Z";

    public static final String UGC_COUNTY_ABBREVIATION = "C";

    /*
     * TODO The following need to be organized better.
     */
    public static final String CONTINUE_BUTTON = "Continue";

    public static final String CANCEL_BUTTON = "Cancel";

    public static final String IS_SELECTED_KEY = "isSelected";

    public static final String IS_VISIBLE_KEY = "isVisible";

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

    public static final String UGC_PARTS_OF_COUNTY = "ugcPortions";

    public static final String UGC_PARTS_OF_STATE = "ugcPartsOfState";

    public static final int MISSING_VALUE = -9999;

    public static final String EVENT_ID_DISPLAY_TYPE = "eventIdDisplayType";

    public static final String NATIONAL = "National";

}
