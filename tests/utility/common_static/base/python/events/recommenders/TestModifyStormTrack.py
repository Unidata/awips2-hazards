#!/usr/bin/env python

import os
import sys
import subprocess
import json
import traceback
import unittest

# Client provides lists of extra paths added to the sys.path data structure,
# which controls the directories one can import from.
# These are fromRoot, which are relative to the root directory, and fromHere,
# which are relative to the directory this source code is in.
# The root directory is lowest directory above source code directory
# with name of rootName.  Also pulls out any paths that are in the EDEX
# utility directories.
def updateSysPath(rootName, fromRoot, fromHere) :

    # Get absolute path to this source code using current working directory
    # and contents of __file__ variable.
    here = os.environ["PWD"]
    me = __file__
    if me[0]!="/" :
        me = here+"/"+me

    # Break this path into its individual directory parts and locate the
    # "root" part.
    pathList = []
    pathParts = me.split("/")
    m = len(pathParts)-1
    basename = pathParts[m]
    pathParts = pathParts[:m]
    nparts = 0
    rootPart = -1
    ok = False
    for part in pathParts :
        if part == '.' :
            pass
        elif part == '..' :
            nparts = nparts - 1
            pathList = pathList[0:nparts]
        elif len(part)>0 :
            nparts = nparts + 1
            pathList.append(part)
        if part == rootName :
            rootPart = nparts

    if rootPart < 0 :
        sys.stderr.write("location of "+basename+" does not make sense\n")
        exit()

    # Reconstitute full paths to the root directory and the source code
    # directory.
    hsRoot = ""
    for part in pathList[0:rootPart] :
        hsRoot += "/"+part
    meDir = hsRoot
    for part in pathList[rootPart:] :
        meDir += "/"+part

    # Initialize with current contents of sys.path, and add all the
    # requested paths.
    pyPathParts = sys.path
    if isinstance(fromRoot, str) or isinstance(fromRoot, unicode) :
        pyPathParts.append(hsRoot+"/"+fromRoot)
    elif isinstance(fromRoot, list) or isinstance(fromRoot, tuple) :
        for part in fromRoot :
            pyPathParts.append(hsRoot+"/"+part)
    if isinstance(fromHere, str) or isinstance(fromHere, unicode) :
        pyPathParts.append(meDir+"/"+fromHere)
    elif isinstance(fromHere, list) or isinstance(fromHere, tuple) :
        for part in fromHere :
             pyPathParts.append(meDir+"/"+part)
    pyPathParts.append(meDir)

    # Eliminate redundancies and paths to EDEX localization file directories.
    newPyPath = []
    for part in pyPathParts :
        if part.find("edex/data/utility")>=0 :
            continue
        if not part in newPyPath :
            newPyPath.append(part)

    sys.path = newPyPath

# This is a unit test of the track updater, ModifyStormTrackTool.
class TestModifyStormTrack(unittest.TestCase):

    # Here we set up the exact set of paths we need in the PYTHONPATH
    # environment variable so we can access the code we need.
    def setUp(self):
        edexPython = \
         "edexOsgi/gov.noaa.gsd.common.utilities/utility/"+ \
         "common_static/base/python"
        recommenderPython = \
          "edexOsgi/gov.noaa.gsd.uf.common.recommenders.hydro/utility/"+ \
          "common_static/base/python/events/recommenders"
        fromRoot = []
        fromRoot.append(edexPython+"/geoUtilities")
        fromRoot.append(edexPython+"/trackUtilities")
        fromRoot.append(edexPython+"/generalUtilities")
        fromRoot.append(edexPython+"/bridge")
        fromRoot.append(recommenderPython)
        updateSysPath("hazardServices", fromRoot, "config")

    def test_ModifyStormTrack1(self) :
        # Most common case of moving point for current frame away from
        # last pivot.
        from ModifyStormTrackTool import Recommender

        sessionAttributes = \
        {
            "selectedEventDict": {
                "eventID": "191", 
                "pivots": [
                    1297137600000
                ], 
                "backupSiteID": "OAX", 
                "headline": "TORNADO WARNING", 
                "stormMotion": {
                    "bearing": 225, 
                    "speed": 20
                }, 
                "creationTime": 1297137600002, 
                "phen": "TO", 
                "modifyCallbackToolName": "ModifyStormTrackTool", 
                "shapes": [
                    {
                        "pointType": "tracking", 
                        "pointID": 1297127700002, 
                        "shapeType": "point", 
                        "point": [
                            -96.89545440673828, 
                            41.10292053222656
                        ]
                    }, 
                    {
                        "pointType": "tracking", 
                        "pointID": 1297128600002, 
                        "shapeType": "point", 
                        "point": [
                            -96.8180160522461, 
                            41.1623649597168
                        ]
                    }, 
                    {
                        "pointType": "tracking", 
                        "pointID": 1297129500002, 
                        "shapeType": "point", 
                        "point": [
                            -96.74043273925781, 
                            41.22175979614258
                        ]
                    }, 
                    {
                        "pointType": "tracking", 
                        "pointID": 1297130400002, 
                        "shapeType": "point", 
                        "point": [
                            -96.66270446777344, 
                            41.28110122680664
                        ]
                    }, 
                    {
                        "pointType": "tracking", 
                        "pointID": 1297131300002, 
                        "shapeType": "point", 
                        "point": [
                            -96.5848388671875, 
                            41.34039306640625
                        ]
                    }, 
                    {
                        "pointType": "tracking", 
                        "pointID": 1297132200002, 
                        "shapeType": "point", 
                        "point": [
                            -96.50682830810547, 
                            41.399627685546875
                        ]
                    }, 
                    {
                        "pointType": "tracking", 
                        "pointID": 1297133100002, 
                        "shapeType": "point", 
                        "point": [
                            -96.42868041992188, 
                            41.45881271362305
                        ]
                    }, 
                    {
                        "pointType": "tracking", 
                        "pointID": 1297134000002, 
                        "shapeType": "point", 
                        "point": [
                            -96.35038757324219, 
                            41.5179443359375
                        ]
                    }, 
                    {
                        "pointType": "tracking", 
                        "pointID": 1297134900002, 
                        "shapeType": "point", 
                        "point": [
                            -96.2719497680664, 
                            41.577022552490234
                        ]
                    }, 
                    {
                        "pointType": "tracking", 
                        "pointID": 1297135800002, 
                        "shapeType": "point", 
                        "point": [
                            -96.19337463378906, 
                            41.63604736328125
                        ]
                    }, 
                    {
                        "pointType": "tracking", 
                        "pointID": 1297136700002, 
                        "shapeType": "point", 
                        "point": [
                            -96.1146469116211, 
                            41.69501876831055
                        ]
                    }, 
                    {
                        "pointType": "tracking", 
                        "pointID": 1297137600002, 
                        "shapeType": "point", 
                        "point": [
                            -96.03578186035156, 
                            41.753936767578125
                        ]
                    }, 
                    {
                        "pointType": "tracking", 
                        "pointID": 1297138500002, 
                        "shapeType": "point", 
                        "point": [
                            -95.95677185058594, 
                            41.812801361083984
                        ]
                    }, 
                    {
                        "pointType": "tracking", 
                        "pointID": 1297139400002, 
                        "shapeType": "point", 
                        "point": [
                            -95.87760925292969, 
                            41.87160873413086
                        ]
                    }, 
                    {
                        "include": "true", 
                        "points": [
                            [
                                -95.8772201538086, 
                                42.03061294555664
                            ], 
                            [
                                -95.6640853881836, 
                                41.87112045288086
                            ], 
                            [
                                -96.03578186035156, 
                                41.626731872558594
                            ], 
                            [
                                -96.20629119873047, 
                                41.75381088256836
                            ]
                        ], 
                        "shapeType": "polygon"
                    }
                ], 
                "subType": "", 
                "state": "pending", 
                "siteID": "OAX", 
                "cta": [], 
                "startTime": 1297137600002, 
                "fullType": "TO.W (TORNADO WARNING)", 
                "sig": "W", 
                "endTime": 1297139400002, 
                "type": "TO.W", 
                "draggedPoints": [
                    [
                        [
                            -96.03578186035156, 
                            41.753936767578125
                        ], 
                        1297137600000
                    ]
                ]
            }, 
            "selectedTime": "1297127700002", 
            "staticSettings": {
                "settingsID": "TOR", 
                "hidHazardCategories": [
                    "Short Fused"
                ], 
                "defaultBaseLayer": "Google Hybrid", 
                "hazardsFilter": "TOR", 
                "visibleTypes": [
                    "TO.W", 
                    "SV.W", 
                    "EW.W"
                ], 
                "radar": "true", 
                "defaultVectorLayers": [], 
                "controlLayers": [
                    "Hazards", 
                    "LandScan Population", 
                    "CO Gov", 
                    "CO Airports", 
                    "USA WFO Locations", 
                    "USA EM State Offices", 
                    "CO EOC Offices"
                ], 
                "caveSettings": "True", 
                "pointHazardShape": "triangle", 
                "hazardCategoriesAndTypes": [
                    {
                        "displayString": "Convective", 
                        "children": [
                            "TO.W", 
                            "SV.W", 
                            "EW.W"
                        ]
                    }
                ], 
                "draggedTrackPointShape": "star", 
                "visibleStates": [
                    "potential", 
                    "proposed", 
                    "pending", 
                    "issued", 
                    "ended"
                ], 
                "toolbarTools": [
                    {
                        "toolName": "StormTrackTool", 
                        "displayName": "Storm Track"
                    }
                ], 
                "defaultControlLayers": [
                    "Hazards"
                ], 
                "defaultTimeDisplayDuration": 14400000, 
                "trackPointShape": "circle", 
                "maxDuration": 90, 
                "columns": {
                    "VTEC Code": {
                        "type": "string", 
                        "fieldName": "vtecCode", 
                        "sortDir": "none"
                    }, 
                    "Issue Time": {
                        "fieldName": "issueTime", 
                        "width": 123, 
                        "type": "date", 
                        "sortDir": "none"
                    }, 
                    "Hazard Type": {
                        "type": "string", 
                        "fieldName": "type", 
                        "sortDir": "ascending", 
                        "hintTextFieldName": "headline"
                    }, 
                    "Description": {
                        "fieldName": "description", 
                        "width": 100, 
                        "type": "string", 
                        "sortDir": "none"
                    }, 
                    "State": {
                        "fieldName": "state", 
                        "width": 60, 
                        "type": "string", 
                        "sortDir": "none"
                    }, 
                    "Site ID": {
                        "type": "string", 
                        "fieldName": "siteID", 
                        "sortDir": "none"
                    }, 
                    "Phen": {
                        "fieldName": "phen", 
                        "width": 50, 
                        "type": "string", 
                        "sortDir": "none"
                    }, 
                    "Start Time": {
                        "fieldName": "startTime", 
                        "width": 126, 
                        "type": "date", 
                        "sortDir": "none"
                    }, 
                    "End Time": {
                        "fieldName": "endTime", 
                        "width": 128, 
                        "type": "date", 
                        "sortDir": "none"
                    }, 
                    "Sig": {
                        "fieldName": "sig", 
                        "width": 50, 
                        "type": "string", 
                        "sortDir": "none"
                    }, 
                    "ETN": {
                        "type": "number", 
                        "fieldName": "ETN", 
                        "sortDir": "none"
                    }, 
                    "Purge Time": {
                        "fieldName": "purgeTime", 
                        "width": 130, 
                        "type": "date", 
                        "sortDir": "none"
                    }, 
                    "Event ID": {
                        "type": "string", 
                        "fieldName": "eventID", 
                        "sortDir": "none"
                    }
                }, 
                "mapCenter": {
                    "lat": 39.29999923706055, 
                    "lon": -104.69999694824219, 
                    "zoom": 7
                }, 
                "defaultSiteID": "BOU", 
                "temporalCounts": {
                    "distance": 12, 
                    "step": 2000, 
                    "panDistance": 7200000, 
                    "loopCount": 0, 
                    "increment": 300000
                }, 
                "defaultCategory": "Convective", 
                "visibleSites": [
                    "BOU", 
                    "OAX"
                ], 
                "displayName": "Canned TOR", 
                "defaultDuration": 1800000, 
                "visibleColumns": [
                    "Event ID", 
                    "Hazard Type", 
                    "State", 
                    "Start Time", 
                    "End Time"
                ], 
                "defaultImageLayers": [], 
                "durationIncrement": 1, 
                "defaultHazardCategory": "shortFused"
            }, 
            "framesInfo": {
                "frameTimeList": [
                    1297127700002, 
                    1297128600002, 
                    1297129500002, 
                    1297130400002, 
                    1297131300002, 
                    1297132200002, 
                    1297133100002, 
                    1297134000002, 
                    1297134900002, 
                    1297135800002, 
                    1297136700002, 
                    1297137600002
                ], 
                "frameIndex": 0, 
                "frameCount": 12
            }, 
            "currentTime": "1297137600002"
        }
        dialogInputMap = {}
        spatialInputMap = \
        {
            "eventID": "191", 
            "pointID": 1297127700002, 
            "newLonLat": [
                -96.83812713623047, 
                41.362911224365234
            ], 
            "shapeType": "dot"
        }

        recommenderObject = Recommender()
        result = recommenderObject.executeImpl( \
                 sessionAttributes, dialogInputMap, spatialInputMap)

        expectedResult = \
        {
            "eventID": "191", 
            "pivots": [
                1297127700002, 
                1297137600002
            ], 
            "backupSiteID": "OAX", 
            "headline": "TORNADO WARNING", 
            "stormMotion": {
                "bearing": 237.1904880852871, 
                "speed": 15.641697873153278
            }, 
            "creationTime": 1297137600002, 
            "phen": "TO", 
            "modifyCallbackToolName": "ModifyStormTrackTool", 
            "shapes": [
                {
                    "pointType": "tracking", 
                    "pointID": 1297127700002, 
                    "shapeType": "point", 
                    "point": [
                        -96.83812713623047, 
                        41.362911224365234
                    ]
                }, 
                {
                    "pointType": "tracking", 
                    "pointID": 1297128600002, 
                    "shapeType": "point", 
                    "point": [
                        -96.76558722546356, 
                        41.39868865950635
                    ]
                }, 
                {
                    "pointType": "tracking", 
                    "pointID": 1297129500002, 
                    "shapeType": "point", 
                    "point": [
                        -96.69296745708567, 
                        41.434420486890275
                    ]
                }, 
                {
                    "pointType": "tracking", 
                    "pointID": 1297130400002, 
                    "shapeType": "point", 
                    "point": [
                        -96.62026774484713, 
                        41.47010659887632
                    ]
                }, 
                {
                    "pointType": "tracking", 
                    "pointID": 1297131300002, 
                    "shapeType": "point", 
                    "point": [
                        -96.54748800292415, 
                        41.50574688765364
                    ]
                }, 
                {
                    "pointType": "tracking", 
                    "pointID": 1297132200002, 
                    "shapeType": "point", 
                    "point": [
                        -96.47462814592286, 
                        41.54134124524161
                    ]
                }, 
                {
                    "pointType": "tracking", 
                    "pointID": 1297133100002, 
                    "shapeType": "point", 
                    "point": [
                        -96.401688088883, 
                        41.576889563490134
                    ]
                }, 
                {
                    "pointType": "tracking", 
                    "pointID": 1297134000002, 
                    "shapeType": "point", 
                    "point": [
                        -96.32866774728201, 
                        41.612391734080134
                    ]
                }, 
                {
                    "pointType": "tracking", 
                    "pointID": 1297134900002, 
                    "shapeType": "point", 
                    "point": [
                        -96.25556703703883, 
                        41.64784764852382
                    ]
                }, 
                {
                    "pointType": "tracking", 
                    "pointID": 1297135800002, 
                    "shapeType": "point", 
                    "point": [
                        -96.18238587451788, 
                        41.683257198165215
                    ]
                }, 
                {
                    "pointType": "tracking", 
                    "pointID": 1297136700002, 
                    "shapeType": "point", 
                    "point": [
                        -96.10912417653302, 
                        41.718620274180424
                    ]
                }, 
                {
                    "pointType": "tracking", 
                    "pointID": 1297137600002, 
                    "shapeType": "point", 
                    "point": [
                        -96.03578186035156, 
                        41.753936767578125
                    ]
                }, 
                {
                    "pointType": "tracking", 
                    "pointID": 1297138500002, 
                    "shapeType": "point", 
                    "point": [
                        -95.96235884369818, 
                        41.789206569199955
                    ]
                }, 
                {
                    "pointType": "tracking", 
                    "pointID": 1297139400002, 
                    "shapeType": "point", 
                    "point": [
                        -95.88885504475896, 
                        41.82442956972097
                    ]
                }, 
                {
                    "include": "true", 
                    "shapeType": "polygon", 
                    "points": [
                        [
                            -95.84333189568963, 
                            41.97978164573295
                        ], 
                        [
                            -95.68048098717338, 
                            41.79039984116413
                        ], 
                        [
                            -96.07171774346361, 
                            41.62959623511651
                        ], 
                        [
                            -96.2025176030745, 
                            41.780676901166835
                        ]
                    ]
                }
            ], 
            "subType": "", 
            "state": "pending", 
            "siteID": "OAX", 
            "cta": [], 
            "startTime": 1297137600002, 
            "fullType": "TO.W (TORNADO WARNING)", 
            "sig": "W", 
            "endTime": 1297139400002, 
            "type": "TO.W", 
            "draggedPoints": [
                [
                    [
                        -96.83812713623047, 
                        41.362911224365234
                    ], 
                    1297127700002
                ], 
                [
                    [
                        -96.03578186035156, 
                        41.753936767578125
                    ], 
                    1297137600002
                ]
            ]
        }

        self.assertEqual(json.dumps(result, sort_keys=True), \
                         json.dumps(expectedResult, sort_keys=True) )

    def test_ModifyStormTrack2(self) :
        # Editing a point not belonging to current frame,
        from ModifyStormTrackTool import Recommender

        sessionAttributes = \
        {
            "selectedEventDict": {
                "eventID": "199", 
                "pivots": [
                    1297127700003, 
                    1297137600003
                ], 
                "backupSiteID": "OAX", 
                "headline": "TORNADO WARNING", 
                "stormMotion": {
                    "bearing": 239.659912109375, 
                    "speed": 15.313946723937988
                }, 
                "creationTime": 1297137600003, 
                "phen": "TO", 
                "modifyCallbackToolName": "ModifyStormTrackTool", 
                "shapes": [
                    {
                        "pointType": "tracking", 
                        "pointID": 1297127700003, 
                        "shapeType": "point", 
                        "point": [
                            -96.8731689453125, 
                            41.42255783081055
                        ]
                    }, 
                    {
                        "pointType": "tracking", 
                        "pointID": 1297128600003, 
                        "shapeType": "point", 
                        "point": [
                            -96.80014038085938, 
                            41.45526123046875
                        ]
                    }, 
                    {
                        "pointType": "tracking", 
                        "pointID": 1297129500003, 
                        "shapeType": "point", 
                        "point": [
                            -96.72704315185547, 
                            41.4879150390625
                        ]
                    }, 
                    {
                        "pointType": "tracking", 
                        "pointID": 1297130400003, 
                        "shapeType": "point", 
                        "point": [
                            -96.65386199951172, 
                            41.52052688598633
                        ]
                    }, 
                    {
                        "pointType": "tracking", 
                        "pointID": 1297131300003, 
                        "shapeType": "point", 
                        "point": [
                            -96.58061218261719, 
                            41.5530891418457
                        ]
                    }, 
                    {
                        "pointType": "tracking", 
                        "pointID": 1297132200003, 
                        "shapeType": "point", 
                        "point": [
                            -96.50729370117188, 
                            41.585609436035156
                        ]
                    }, 
                    {
                        "pointType": "tracking", 
                        "pointID": 1297133100003, 
                        "shapeType": "point", 
                        "point": [
                            -96.43389892578125, 
                            41.618080139160156
                        ]
                    }, 
                    {
                        "pointType": "tracking", 
                        "pointID": 1297134000003, 
                        "shapeType": "point", 
                        "point": [
                            -96.36042785644531, 
                            41.6505012512207
                        ]
                    }, 
                    {
                        "pointType": "tracking", 
                        "pointID": 1297134900003, 
                        "shapeType": "point", 
                        "point": [
                            -96.28688049316406, 
                            41.68288040161133
                        ]
                    }, 
                    {
                        "pointType": "tracking", 
                        "pointID": 1297135800003, 
                        "shapeType": "point", 
                        "point": [
                            -96.21326446533203, 
                            41.7152099609375
                        ]
                    }, 
                    {
                        "pointType": "tracking", 
                        "pointID": 1297136700003, 
                        "shapeType": "point", 
                        "point": [
                            -96.13957214355469, 
                            41.747493743896484
                        ]
                    }, 
                    {
                        "pointType": "tracking", 
                        "pointID": 1297137600003, 
                        "shapeType": "point", 
                        "point": [
                            -96.06580352783203, 
                            41.779727935791016
                        ]
                    }, 
                    {
                        "pointType": "tracking", 
                        "pointID": 1297138500003, 
                        "shapeType": "point", 
                        "point": [
                            -95.9919662475586, 
                            41.81191635131836
                        ]
                    }, 
                    {
                        "pointType": "tracking", 
                        "pointID": 1297139400003, 
                        "shapeType": "point", 
                        "point": [
                            -95.91804504394531, 
                            41.844058990478516
                        ]
                    }, 
                    {
                        "include": "true", 
                        "points": [
                            [
                                -95.8635482788086, 
                                41.997802734375
                            ], 
                            [
                                -95.71178436279297, 
                                41.803367614746094
                            ], 
                            [
                                -96.10889434814453, 
                                41.65665817260742
                            ], 
                            [
                                -96.23091125488281, 
                                41.81180191040039
                            ]
                        ], 
                        "shapeType": "polygon"
                    }
                ], 
                "subType": "", 
                "state": "pending", 
                "siteID": "OAX", 
                "cta": [], 
                "startTime": 1297137600003, 
                "fullType": "TO.W (TORNADO WARNING)", 
                "sig": "W", 
                "endTime": 1297139400003, 
                "type": "TO.W", 
                "draggedPoints": [
                    [
                        [
                            -96.8731689453125, 
                            41.42255783081055
                        ], 
                        1297127700003
                    ], 
                    [
                        [
                            -96.06580352783203, 
                            41.779727935791016
                        ], 
                        1297137600003
                    ]
                ]
            }, 
            "selectedTime": "1297137600003", 
            "staticSettings": {
                "settingsID": "TOR", 
                "hidHazardCategories": [
                    "Short Fused"
                ], 
                "defaultBaseLayer": "Google Hybrid", 
                "hazardsFilter": "TOR", 
                "visibleTypes": [
                    "TO.W", 
                    "SV.W", 
                    "EW.W"
                ], 
                "radar": "true", 
                "defaultVectorLayers": [], 
                "controlLayers": [
                    "Hazards", 
                    "LandScan Population", 
                    "CO Gov", 
                    "CO Airports", 
                    "USA WFO Locations", 
                    "USA EM State Offices", 
                    "CO EOC Offices"
                ], 
                "caveSettings": "True", 
                "pointHazardShape": "triangle", 
                "hazardCategoriesAndTypes": [
                    {
                        "displayString": "Convective", 
                        "children": [
                            "TO.W", 
                            "SV.W", 
                            "EW.W"
                        ]
                    }
                ], 
                "draggedTrackPointShape": "star", 
                "visibleStates": [
                    "potential", 
                    "proposed", 
                    "pending", 
                    "issued", 
                    "ended"
                ], 
                "toolbarTools": [
                    {
                        "toolName": "StormTrackTool", 
                        "displayName": "Storm Track"
                    }
                ], 
                "defaultControlLayers": [
                    "Hazards"
                ], 
                "defaultTimeDisplayDuration": 14400000, 
                "trackPointShape": "circle", 
                "maxDuration": 90, 
                "columns": {
                    "VTEC Code": {
                        "type": "string", 
                        "fieldName": "vtecCode", 
                        "sortDir": "none"
                    }, 
                    "Issue Time": {
                        "fieldName": "issueTime", 
                        "width": 123, 
                        "type": "date", 
                        "sortDir": "none"
                    }, 
                    "Hazard Type": {
                        "type": "string", 
                        "fieldName": "type", 
                        "sortDir": "ascending", 
                        "hintTextFieldName": "headline"
                    }, 
                    "Description": {
                        "fieldName": "description", 
                        "width": 100, 
                        "type": "string", 
                        "sortDir": "none"
                    }, 
                    "State": {
                        "fieldName": "state", 
                        "width": 60, 
                        "type": "string", 
                        "sortDir": "none"
                    }, 
                    "Site ID": {
                        "type": "string", 
                        "fieldName": "siteID", 
                        "sortDir": "none"
                    }, 
                    "Phen": {
                        "fieldName": "phen", 
                        "width": 50, 
                        "type": "string", 
                        "sortDir": "none"
                    }, 
                    "Start Time": {
                        "fieldName": "startTime", 
                        "width": 126, 
                        "type": "date", 
                        "sortDir": "none"
                    }, 
                    "End Time": {
                        "fieldName": "endTime", 
                        "width": 128, 
                        "type": "date", 
                        "sortDir": "none"
                    }, 
                    "Sig": {
                        "fieldName": "sig", 
                        "width": 50, 
                        "type": "string", 
                        "sortDir": "none"
                    }, 
                    "ETN": {
                        "type": "number", 
                        "fieldName": "ETN", 
                        "sortDir": "none"
                    }, 
                    "Purge Time": {
                        "fieldName": "purgeTime", 
                        "width": 130, 
                        "type": "date", 
                        "sortDir": "none"
                    }, 
                    "Event ID": {
                        "type": "string", 
                        "fieldName": "eventID", 
                        "sortDir": "none"
                    }
                }, 
                "mapCenter": {
                    "lat": 39.29999923706055, 
                    "lon": -104.69999694824219, 
                    "zoom": 7
                }, 
                "defaultSiteID": "BOU", 
                "temporalCounts": {
                    "distance": 12, 
                    "step": 2000, 
                    "panDistance": 7200000, 
                    "loopCount": 0, 
                    "increment": 300000
                }, 
                "defaultCategory": "Convective", 
                "visibleSites": [
                    "BOU", 
                    "OAX"
                ], 
                "displayName": "Canned TOR", 
                "defaultDuration": 1800000, 
                "visibleColumns": [
                    "Event ID", 
                    "Hazard Type", 
                    "State", 
                    "Start Time", 
                    "End Time"
                ], 
                "defaultImageLayers": [], 
                "durationIncrement": 1, 
                "defaultHazardCategory": "shortFused"
            }, 
            "framesInfo": {
                "frameTimeList": [
                    1297127700003, 
                    1297128600003, 
                    1297129500003, 
                    1297130400003, 
                    1297131300003, 
                    1297132200003, 
                    1297133100003, 
                    1297134000003, 
                    1297134900003, 
                    1297135800003, 
                    1297136700003, 
                    1297137600003
                ], 
                "frameIndex": 11, 
                "frameCount": 12
            }, 
            "currentTime": "1297137600003"
        }
        dialogInputMap = {}
        spatialInputMap = \
        {
            "eventID": "199", 
            "pointID": 1297129500003, 
            "newLonLat": [
                -96.7336196899414, 
                41.58121109008789
            ], 
            "shapeType": "dot"
        }

        recommenderObject = Recommender()
        result = recommenderObject.executeImpl( \
                 sessionAttributes, dialogInputMap, spatialInputMap)

        expectedResult = \
        {
            "eventID": "199", 
            "pivots": [
                1297127700003, 
                1297137600003
            ], 
            "backupSiteID": "OAX", 
            "headline": "TORNADO WARNING", 
            "stormMotion": {
                "bearing": 239.65992204072612, 
                "speed": 15.313992035539101
            }, 
            "creationTime": 1297137600003, 
            "phen": "TO", 
            "modifyCallbackToolName": "ModifyStormTrackTool", 
            "shapes": [
                {
                    "pointType": "tracking", 
                    "pointID": 1297127700003, 
                    "shapeType": "point", 
                    "point": [
                        -96.8731689453125, 
                        41.42255783081056
                    ]
                }, 
                {
                    "pointType": "tracking", 
                    "pointID": 1297128600003, 
                    "shapeType": "point", 
                    "point": [
                        -96.8001407895622, 
                        41.45526058438634
                    ]
                }, 
                {
                    "pointType": "tracking", 
                    "pointID": 1297129500003, 
                    "shapeType": "point", 
                    "point": [
                        -96.7270390099219, 
                        41.487917107030434
                    ]
                }, 
                {
                    "pointType": "tracking", 
                    "pointID": 1297130400003, 
                    "shapeType": "point", 
                    "point": [
                        -96.6538635519678, 
                        41.520527298954796
                    ]
                }, 
                {
                    "pointType": "tracking", 
                    "pointID": 1297131300003, 
                    "shapeType": "point", 
                    "point": [
                        -96.58061436176229, 
                        41.55309106025185
                    ]
                }, 
                {
                    "pointType": "tracking", 
                    "pointID": 1297132200003, 
                    "shapeType": "point", 
                    "point": [
                        -96.50729138585736, 
                        41.58560829089503
                    ]
                }, 
                {
                    "pointType": "tracking", 
                    "pointID": 1297133100003, 
                    "shapeType": "point", 
                    "point": [
                        -96.43389457129801, 
                        41.618078890739326
                    ]
                }, 
                {
                    "pointType": "tracking", 
                    "pointID": 1297134000003, 
                    "shapeType": "point", 
                    "point": [
                        -96.36042386562563, 
                        41.6505027595218
                    ]
                }, 
                {
                    "pointType": "tracking", 
                    "pointID": 1297134900003, 
                    "shapeType": "point", 
                    "point": [
                        -96.28687921688146, 
                        41.68287979686217
                    ]
                }, 
                {
                    "pointType": "tracking", 
                    "pointID": 1297135800003, 
                    "shapeType": "point", 
                    "point": [
                        -96.21326057360994, 
                        41.715209902263354
                    ]
                }, 
                {
                    "pointType": "tracking", 
                    "pointID": 1297136700003, 
                    "shapeType": "point", 
                    "point": [
                        -96.1395678848622, 
                        41.74749297511204
                    ]
                }, 
                {
                    "pointType": "tracking", 
                    "pointID": 1297137600003, 
                    "shapeType": "point", 
                    "point": [
                        -96.06580110019947, 
                        41.779728914679254
                    ]
                }, 
                {
                    "pointType": "tracking", 
                    "pointID": 1297138500003, 
                    "shapeType": "point", 
                    "point": [
                        -95.99196016969657, 
                        41.81191762012094
                    ]
                }, 
                {
                    "pointType": "tracking", 
                    "pointID": 1297139400003, 
                    "shapeType": "point", 
                    "point": [
                        -95.91804504394531, 
                        41.84405899047853
                    ]
                }, 
                {
                    "include": "true", 
                    "points": [
                        [
                            -95.8635482788086, 
                            41.997802734375
                        ], 
                        [
                            -95.71178436279297, 
                            41.803367614746094
                        ], 
                        [
                            -96.10889434814453, 
                            41.65665817260742
                        ], 
                        [
                            -96.23091125488281, 
                            41.81180191040039
                        ]
                    ], 
                    "shapeType": "polygon"
                }
            ], 
            "subType": "", 
            "state": "pending", 
            "siteID": "OAX", 
            "cta": [], 
            "startTime": 1297137600003, 
            "fullType": "TO.W (TORNADO WARNING)", 
            "sig": "W", 
            "endTime": 1297139400003, 
            "type": "TO.W", 
            "draggedPoints": [
                [
                    [
                        -96.8731689453125, 
                        41.42255783081055
                    ], 
                    1297127700003
                ], 
                [
                    [
                        -96.06580352783203, 
                        41.779727935791016
                    ], 
                    1297137600003
                ]
            ]
        }

        self.assertEqual(json.dumps(result, sort_keys=True), \
                         json.dumps(expectedResult, sort_keys=True) )


if __name__ == '__main__':
    unittest.main()
#
