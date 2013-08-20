InputTestCaseData = { \
    "caseDesc" : "Single frame test",
    "sessionAttributes" :
        {
            "selectedTime": "1297137600002", 
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
                    1297137600002
                ], 
                "frameIndex": 0, 
                "frameCount": 1
            }, 
            "currentTime": "1297137600002"
        },
    "spatialInputMap" :
        {
            "spatialInfo": {
                "points": [
                    [
                        [
                            41.98193359375, 
                            -96.24107360839844
                        ], 
                        1297137600
                    ]
                ]
            }
        }
}

TestCaseResults = { \
            "pivots": [
                1297137600000
            ], 
            "stormMotion": {
                "bearing": 225, 
                "speed": 20
            }, 
            "creationTime": 1297137600002, 
            "modifyCallbackToolName": "ModifyStormTrackTool", 
            "shapes": [
                {
                    "pointType": "tracking", 
                    "pointID": 1297137600002, 
                    "shapeType": "point", 
                    "point": [
                        -96.24107360839844, 
                        41.98193359375
                    ]
                }, 
                {
                    "pointType": "tracking", 
                    "pointID": 1297139400002, 
                    "shapeType": "point", 
                    "point": [
                        -96.08233523807331, 
                        42.0996051604707
                    ]
                }, 
                {
                    "include": "true", 
                    "shapeType": "polygon", 
                    "points": [
                        [
                            -96.08193686043711, 
                            42.258608894363356
                        ], 
                        [
                            -95.86804029496534, 
                            42.0991106590306
                        ], 
                        [
                            -96.24107350317884, 
                            41.854730372693176
                        ], 
                        [
                            -96.41219362363283, 
                            41.9818066132438
                        ]
                    ]
                }
            ], 
            "state": "pending", 
            "draggedPoints": [
                [
                    [
                        -96.24107360839844, 
                        41.98193359375
                    ], 
                    1297137600000
                ]
            ], 
            "startTime": 1297137600002, 
            "endTime": 1297139400002, 
            "type": "TO.W"
}
