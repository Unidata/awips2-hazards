"""
10    Description: Constants used throughout Hazard Services Python code.
12    
13    SOFTWARE HISTORY
14    Date         Ticket#    Engineer    Description
15    ------------ ---------- ----------- --------------------------
16    April 5, 2013            Tracy.L.Hansen      Initial creation
17    
18    @author Tracy.L.Hansen@noaa.gov
19    @version 1.0
20    """

#################################
# Hazard Event Keys and Values

EVENT_ID = 'eventID'
SITE_ID = 'siteID'
BACKUP_SITE_ID = 'backupSiteID'

GEOMETRY = 'geometry'
HAZARD_MODE = "hazardMode"

# State and Values
STATE = "state"
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

START_TIME = "startTime"
END_TIME = "endTime"
CREATION_TIME = "creationTime"
ISSUE_TIME = "issueTime"

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

PREVIEW_STATE = "previewState"
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

#################################
# VTEC Record Keys
GEO_ID = "geoid"
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


