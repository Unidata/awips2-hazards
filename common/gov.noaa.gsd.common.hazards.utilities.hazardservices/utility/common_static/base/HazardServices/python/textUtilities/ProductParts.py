'''
    Description: Holds the configuration of all the Product Parts. New Product Parts
    can be created by overriding this module and adding new dictionary entries into this
    file. The function of each dictionary entry is described below: Note that each key has
    a default value. If that key is missing from the dictionary, the default value
    will be used. 
    
    displayable:    Determines whether or not the Product Part will be visible on the 
                    Product Editor.
                    Default Value: False

    label:          The label that will be displayed above the Product Part if it is 
                    displayed on the Product Editor. If displayable is False then this
                    does not need to be set.
                    Default Value: None

    eventIDsInLabel:Determines whether or not the associated eventIDs will be added to
                    the Product Part Label in the Product Editor. Rule of thumb is to add them
                    to section or segment level parts but not to product level.
                    Default Value: True
 
    editable:       Determines whether or not the Product Part is editable on the Product
                    Editor. If False, the Product Part will be an uneditable Label instead
                    of a Text Box.
                    Default Value: False

    required:       Determines whether or not the Product Part is required in order to Issue
                    the Product. If there is no text for this part and it is configured as
                    required, the Product Editor will prevent the product from being issued.
                    Default Value: True

    numLines:       The number of lines the TextBox should be on the Product Editor. If not set
                    or set to less than 1 the system will attempt to automatically size it the 
                    best that it can to a maximum of 6 lines. If set the value can be greater
                    than 6.
                    Default Value: 0

    segmentDivider: Determines whether or not this part should be a set to Bold and colored Blue.
                    This is used to help split apart different segments on the Product Editor. This
                    should only be set on uneditable Product Parts.
                    Default Value: False

    formatMethod:   The name of the method in the formatter that will be called to format the text
                    for this Product Part. If not defined it will default to using a method with the
                    same name as the Product Part.
                    Default Value: Product Part Name

    @author: Raytheon Hazard Services Team
'''

DISPLAYABLE_DEFAULT = False
LABEL_DEFAULT = None
EVENTIDS_IN_LABEL_DEFAULT = True
EDITABLE_DEFAULT = False
REQUIRED_DEFAULT = True
NUMLINES_DEFAULT = 0
SEGMENT_DIVIDER_DEFAULT = False

ProductParts = {
    "additionalComments" : {
        "displayable" : True,
        "label" : "Additional Comments",
        "editable" : True,
        "required" : False,
        },
    "additionalInfoStatement" : {
        "displayable" : True,
        "label" : "Additional Info Statement",
        "editable" : True,
        "required" : False,
        "eventIDsInLabel" : False,
        },
    "areaList" : {
        # Uses all default values
        },
    "attribution" : {
        # Uses all default values
        },
    "attribution_point" : {
        # Uses all default values
        },
    "basisBullet" : {
        "displayable" : True,
        "label" : "Basis Bullet",
        "editable" : True,
        "required" : True,
        },
    "basisAndImpactsStatement" : {
        "displayable" : True,
        "label" : "Basis and Impacts Bullet",
        "editable" : True,
        },
    "callsToAction_productLevel" : {
        "displayable" : True,
        "label" : "Calls To Action",
        "editable" : True,
        "required" : False,
        "eventIDsInLabel" : False,
        },
    "callsToAction_sectionLevel" : {
        "displayable" : True,
        "label" : "Calls To Action",
        "editable" : True,
        "required" : False,
        },
    "cityList" : {
        # Uses all default values
        },
    "CR" : {
        # Uses all default values
        },
    "easMessage" : {
        # Uses all default values
        },
    "emergencyHeadline" : {
        # Uses all default values
        },
    "emergencyStatement" : {
        # Uses all default values
        },
    "endingSynopsis" : {
        "displayable" : True,
        "label" : "Ending Synopsis",
        "editable" : True,
        "required" : False,
        },
    "endSection" : {
        "required" : False,
        },
    "endSegment" : {
        # Uses all default values
        },
    "firstBullet" : {
        # Uses all default values
        },
    "firstBullet_point" : {
        # Uses all default values
        },
    "floodCategoryBullet" : {
        "displayable" : True,
        "label" : "Flood Category Bullet",
        "editable" : True,
        },
    "floodHistoryBullet" : {
        "displayable" : True,
        "label" : "Flood History Bullet",
        "editable" : True,
        "required" : False,
        },
    "floodPointTable" : {
        "displayable" : True,
        "label" : "Flood Point Table",
        "editable" : True,
        "required" : False,
        },
    "floodStageBullet" : {
        "displayable" : True,
        "label" : "Flood Stage Bullet",
        "editable" : True,
        },
    "forecastStageBullet" : {
        "displayable" : True,
        "label" : "Forecast Stage Bullet",
        "editable" : True,
        "required" : False,
        },
    "groupSummary" : {
        "displayable" : True,
        "label" : "Group Summary",
        "editable" : True,
        "required" : False,
        "eventIDsInLabel" : False,
        },
    "headlineStatement" : {
        "displayable" : True,
        "label" : "Headline Statement",
        "editable" : True,
        "eventIDsInLabel" : False,
        },
    "impactsBullet" : {
        "displayable" : True,
        "label" : "Impacts Bullet",
        "editable" : True,
        "required" : False,
        },
    "initials" : {
        "displayable" : True,
        "label" : "Initials",
        "editable" : True,
        "required" : False,
        "eventIDsInLabel" : False,
        "numLines" : 1,
        },
    "issuanceTimeDate" : {
        # Uses all default values
        },
    "locationsAffected" : {
        "displayable" : True,
        "label" : "Locations Affected",
        "editable" : True,
        "required" : False,
        },
    "narrativeForecastInformation" : {
        "displayable" : True,
        "label" : "Narrative Forecast Information",
        "editable" : True,
        },
    "narrativeInformation" : {
        "displayable" : True,
        "label" : "Narrative Information",
        "editable" : True,
        },
    "nextIssuanceStatement" : {
        "displayable" : True,
        "label" : "Next Issuance Statement",
        "editable" : True,
        "required" : False,
        "eventIDsInLabel" : False,
        },
    "observedStageBullet" : {
        "displayable" : True,
        "label" : "Observed Stage Bullet",
        "editable" : True,
        },
    "overviewHeadline_point" : {
        "displayable" : True,
        "label" : "Overview Headline",
        "editable" : True,
        "required" : False,
        "eventIDsInLabel" : False,
        },
    "overviewSynopsis_area" : {
        "displayable" : True,
        "label" : "Overview Synopsis",
        "editable" : True,
        "eventIDsInLabel" : False,
        "required" : False,
        },
    "overviewSynopsis_point" : {
        "displayable" : True,
        "label" : "Overview Synopsis",
        "editable" : True,
        "required" : False,
        "eventIDsInLabel" : False,
        },
    "pointImpactsBullet" : {
        "displayable" : True,
        "label" : "Impacts Bullet",
        "editable" : True,
        "required" : False,
        },
    "polygonText" : {
        # Uses all default values
        },
    "productHeader" : {
        # Uses all default values
        },
    "recentActivityBullet" : {
        "displayable" : True,
        "label" : "Recent Activity Bullet",
        "editable" : True,
        },
    "sections" : {
        "required" : False,
        "formatMethod" : "processSubParts",
        },
    "segments" : {
        "required" : False,
        "formatMethod" : "processSubParts",
        },
    "setUp_section" : {
        "required" : False,
        },
    "setUp_segment" : {
        "required" : False,
        },
    "summaryHeadlines" : {
        "displayable" : True,
        "label" : "Summary Headlines",
        "editable" : True,
        "required" : False,
        },
    "timeBullet" : {
        # Uses all default values
        },
    "ugcHeader" : {
        "displayable" : True,
        "segmentDivider" : True,
        },
    "vtecRecords" : {
        "displayable" : True,
        "segmentDivider" : True,
        },
    "wmoHeader" : {
        # Uses all default values
        },
    }
