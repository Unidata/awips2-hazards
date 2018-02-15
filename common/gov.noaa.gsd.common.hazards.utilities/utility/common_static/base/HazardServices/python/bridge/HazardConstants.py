"""
    Description: Constants used throughout Hazard Services Python code.

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

# Linear units
LINEAR_UNITS_FEET = "feet"
LINEAR_UNITS_MILES = "miles"
LINEAR_UNITS_NAUTICAL_MILES = "nauticalMiles"
LINEAR_UNITS_KILOMETERS = "kilometers"
LINEAR_UNITS_METERS = "meters"

# Meta Data
CAUSES = "causes"
CALLS_TO_ACTION = "callsToAction"
REPORTED_BY = "reportedBy"
RISE_ABOVE = "riseAbove"
CREST = "crest"
FALL_BELOW = "fallBelow"
INTERMEDIATE_CAUSE = "intermediateCause"
FLOOD_RECORD = "floodRecord"
FLOOD_SEVERITY = "floodSeverity"
CREST_STAGE = "crestStage"

# Missing value magic number.
MISSING_VALUE = -9999
MISSING_VALUE_STR = str(MISSING_VALUE)

FORECAST_POINT = "forecastPoint"
NAME = "name"
POINT = "point"
LINE = "line"
POLYGON = "polygon"

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
METADATA = "metaData"
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
NATIONAL = 'National'

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
METADATA_FILE_PATH_KEY = "filePath"
METADATA_MODIFIED_HAZARD_EVENT = "modifiedHazardEvent"
INTERDEPENDENCIES_SCRIPT_KEY = "interdependencies"
METADICT_KEY = "metaDict"
FILENAME_KEY = "fileName"

SESSION_OBJECT_KEY = "sessionObjects"
SAVE_TO_HISTORY_KEY = "saveToHistory"
SAVE_TO_DATABASE_KEY = "saveToDatabase"
KEEP_SAVED_TO_DATABASE_LOCKED_KEY = "lockEvents"
DO_NOT_COUNT_AS_MODIFICATION_KEY = "doNotCountAsModification"
TREAT_AS_ISSUANCE_KEY = "treatAsIssuance"
DELETE_EVENT_IDENTIFIERS_KEY = "deleteEventIdentifiers"
SELECTED_TIME_KEY = "selectedTime"
RESULTS_MESSAGE_KEY = "resultsMessage"
RESULTS_DIALOG_KEY = "resultsDialog"

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
# 'Maps' database tables
DAMINUNDATION_TABLE = "daminundation"
BURNSCARAREA_TABLE = "burnscararea"



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
SITE_CFG_ROOT = "gfe/python/"

#################################
# Product Generation
SITE = "site"
#################################
# Shapely Geometry Constants
SHAPELY_POINT = "Point"
SHAPELY_LINE = "LineString"
SHAPELY_POLYGON = "Polygon"
SHAPELY_MULTIPOLYGON = "MultiPolygon"


#################################
# Previous Text Checkbox constants
PREVIOUS_CTA_TEXT_CHECKBOX = "prevCTATextCheckBox"
PREVIOUS_BASIS_TEXT_CHECKBOX = "prevBasisTextCheckBox"
PREVIOUS_LOCATIONS_AFFECTED_TEXT_CHECKBOX = "prevLocationsAffectedTextCheckBox"
PREVIOUS_ADDITIONAL_INFO_TEXT_CHECKBOX = "prevAdditionalInfoTextCheckBox"
PREVIOUS_ENDING_OPTION_TEXT_CHECKBOX = "prevEndingOptionTextCheckBox"
PREVIOUS_CRESTS_TEXT_CHECKBOX= "prevCrestsTextCheckBox"
PREVIOUS_IMPACTS_TEXT_CHECKBOX = "prevImpactsTextCheckBox"


#################################
# PHI Configuration
DEFAULTLOWTHRESHOLD = 40
DEFAULTPHIOUTPUTDIR = '/scratch/PHIGridTesting'
DEFAULTDOMAINBUFFER = 0.5
DEFAULTDOMAINULLON = -104.0
DEFAULTDOMAINULLAT = 37.0
DEFAULTDOMAINLRLON = -92.0
DEFAULTDOMAINLRLAT = 27.0
    
LOWTHRESHKEY = 'lowThreshold'
OUTPUTDIRKEY = 'OUTPUTDIR'
DOMAINBUFFERKEY = 'domainBuffer'
DOMAINULLONKEY = 'domainULLon'
DOMAINULLATKEY = 'domainULLat'
DOMAINLRLONKEY = 'domainLRLon'
DOMAINLRLATKEY = 'domainLRLat'


