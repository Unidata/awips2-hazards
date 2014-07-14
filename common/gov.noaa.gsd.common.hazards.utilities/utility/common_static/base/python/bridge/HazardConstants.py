"""
    Description: Constants used throughout Hazard Services Python code.
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    April 5, 2013            Tracy.L.Hansen      Initial creation
    April 19, 2013           blawrenc            Added constants for 
                                                 Bridge
    April 09, 2014 2925     Chris.Golden         Added constants for
                                                 class-based metadata.
    June 17, 2014  3982     Chris.Golden         Changed megawidget
                                                 "side effects" to
                                                 "interdependencies".
    @author Tracy.L.Hansen@noaa.gov
    @version 1.0
"""

import math

#################################
# Hazard Event Keys and Values

EVENT_ID = 'eventID'
SITE_ID = 'siteID'
BACKUP_SITE_ID = 'backupSiteID'

GEOMETRY = 'geometry'
HAZARD_MODE = "hazardMode"

# State and Values
STATE = "status"
POTENTIAL = "potential"
PENDING = "pending"
PROPOSED = "proposed"
ISSUED = "issued"
ENDED = "ended"

HAZARD_TYPE = "type"
SUBTYPE = "subType"
HEADLINE = "headline"
FULLTYPE = "fullType"
PHENOMENON = "phen"
SIGNIFICANCE = "sig"
PHENSIG = "phensig"

START_TIME = "startTime"
END_TIME = "endTime"
CREATION_TIME = "creationTime"
ISSUE_TIME = "issueTime"
EXPIRATION_TIME = "expirationTime"

# Session State
SELECTED_TIME_MS = "selectedTimeMS"

# Meta Data
CAUSES = "causes"
CALLS_TO_ACTION = "cta"
REPORTED_BY = "reportedBy"
RISE_ABOVE = "riseAbove"
CREST = "crest"
FALL_BELOW = "fallBelow"
INTERMEDIATE_CAUSE = "intermediateCause"
FLOOD_RECORD = "floodRecord"
FLOOD_SEVERITY = "floodSeverity"

FORECAST_POINT = "forecastPoint"
ID = "id"
NAME = "name"
POINT = "point"
LINE = "line"
POLYGON = "polygon"

PREVIEW_STATE = "previewState"
PREVIEW_ENDED = "previewEnded"
REPLACES = "replaces"
REPLACED_BY = "replacedBy"

# Spatial Display and Geometries
#   These may change as we begin to use Shapely
SHAPES = "shapes"
SHAPE_TYPE = "shapeType"
INCLUDE = "include"
POINTS = "points"
LON_LAT = "lonLat"
POINT_TIME = "pointTime"
EDITABLE_START = "editableStart"
EDITABLE_END = "editableEnd"
PAST_FUTURE = "pastFuture"

# Tool Types
RECOMMENDER_TOOL = "Recommender"
PRODUCT_GENERATOR_TOOL = "ProductGenerator"

# Data Types
CANNED_EVENT_DATA = "cannedEvents"
SETTINGS_DATA = "settings"
HAZARD_TYPES_DATA = "hazardTypes"
HAZARD_CATEGORIES_DATA = "hazardCategories"
PRODUCT_DATA = "productGeneratorTable"
STARTUP_CONFIG_DATA = "startUpConfig"
HAZARD_METADATA = "hazardMetaData"
CLASS_METADATA = "classMetaData"
HAZARD_METADATA_FILTER = "hazardMetaData_filter"
CONFIG_DATA = "config"
VTEC_TABLE_DATA = "VTECTable"
VTEC_RECORDS_DATA = "vtecRecords"
ALERTS_DATA = "alerts"
TEST_VTEC_RECORDS_DATA = "testVtecRecords"
VIEW_DEF_CONFIG_DATA = "viewDefConfig"
VIEW_CONFIG_DATA = "viewConfig"
VIEW_DEFAULT_DATA = "viewDefaultValues"
HAZARD_INSTANCE_ALERT_CONFIG_DATA = "hazardInstanceAlertConfig"
ALERT_CONFIG_DATA = "alertConfig"
AREA_DICTIONARY_DATA = "AreaDictionary"
CITY_LOCATION_DATA = "CityLocation"
SITE_INFO_DATA = "SiteInfo"
CALLS_TO_ACTIONS_DATA = "CallToActions"
GFE_DATA = "GFEGrids"

# Localization
LOCALIZATION_LEVEL = "level"

# Hazard Services Settings
SETTINGS_ID = "settingsID"
VISIBLE_TYPES = "visibleTypes"
VISIBLE_STATES = "visibleStatuses"
VISIBLE_SITES = "visibleSites"

# Dictionary (or map) keys
DATA_TYPE_KEY = "dataType"
EVENTDICTS_KEY = "eventDicts"
PRODUCT_CATEGORY_KEY = "productCategory"
FIELDS_KEY = "fields"
FILTER_KEY = "filter"
RETURN_TYPE_KEY = "returnType"
GRID_PARAM_KEY = "gridParm"
METADATA_KEY = "metadata"
INTERDEPENDENCIES_SCRIPT_KEY = "interdependencies"


#################################
# VTEC Record Keys
ID = "id"
ETN = 'etn'
VTEC_STR = 'vtecstr'
HVTEC = 'hvtec'
HVTEC_STR = 'hvtecstr'
UFN = 'ufn'
AREA_POINTS = 'areaPoints'
VALUE_POINTS = 'valuePoints'
HDLN = 'hdln'
KEY = 'key'
PREVIOUS_START = 'previousStart'
PREVIOUS_END = 'previousEnd'
EXPIRE_TIME = 'purgeTime'
DOWNGRADE_FROM = 'downgradeFrom'
UPGRADE_FROM = 'upgradeFrom'
ACTION = 'act'
OFFICE_ID = 'officeid'
SEGMENT = 'seg'

#################################
# Math constants
PI = math.pi # 3.141592653589793238462643383279502884197169399375106
DEG_TO_RAD = 0.017453292519943295769236907684886127134428
R_EARTH = 6370.0
r_Earth2 = 2.0*R_EARTH
halfCircEarth = R_EARTH*PI
kmPerDegLat = R_EARTH*PI/180.0

#################################
# Localization Relative Paths
TEXT_UTILITY_ROOT = "gfe/userPython/textUtilities/regular/"

#################################
# Product Generation
XML_FORMAT = "XML"
LEGACY_FORMAT = "Legacy"
CAP_FORMAT = "CAP"

#################################
# Shapely Geometry Constants
SHAPELY_POINT = "Point"
SHAPELY_LINE = "LineString"
SHAPELY_POLYGON = "Polygon"
SHAPELY_MULTIPOLYGON = "MultiPolygon"
