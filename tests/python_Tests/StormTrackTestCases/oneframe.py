InputTestCaseData = { \
"caseDesc" : "Single frame test",
"sessionAttributes" : {
    "currentTime": 1297137600000, 
    "defaultDuration": 10800000, 
    "phenomena": "SV", 
    "framesInfo": {
        "frameTimeList": [
            1297137600000
        ], 
        "frameIndex": 0, 
        "currentFrame": "2011-02-08 04:00:00.0", 
        "frameCount": 1
    }, 
    "subType": "", 
    "hazardMode": "PRACTICE", 
    "siteID": "OAX", 
    "significance": "W"
},
"spatialInputMap" : {
    "spatialInfo": {
        "points": [
            [
                [
                    41.61032054257462, 
                    -96.043550969641
                ], 
                1297137600.0
            ]
        ]
    }
}
}

TestCaseResults = { \
   "status": "pending", 
    "pivots": [
        0
    ], 
    "forJavaObj": {
        "currentTime": 1297137600000, 
        "track": [
            [
                -96.043550969641, 
                41.61032054257462
            ], 
            [
                -95.90154180101092, 
                41.716235953423364
            ]
        ], 
        "hazardPolygon": [
            [
                -95.90083802749517, 
                42.03424323142969
            ], 
            [
                -95.47552233065433, 
                41.71492405368774
            ], 
            [
                -96.04355086502702, 
                41.48311732151781
            ], 
            [
                -96.21368165891256, 
                41.61019520983544
            ]
        ], 
        "phenomena": "SV", 
        "subType": "", 
        "SiteID": "OAX", 
        "startTime": 1297137600000, 
        "significance": "W", 
        "endTime": 1297148400000
    }, 
    "trackPoints": [
        {
            "pointType": "tracking", 
            "pointID": 1297137600000, 
            "shapeType": "point", 
            "point": [
                -96.043550969641, 
                41.61032054257462
            ]
        }, 
        {
            "pointType": "tracking", 
            "pointID": 1297148400000, 
            "shapeType": "point", 
            "point": [
                -95.90154180101092, 
                41.716235953423364
            ]
        }
    ], 
    "pivotTimes": [
        1297137600000
    ], 
    "stormMotion": {
        "bearing": 225, 
        "speed": 3
    }, 
    "creationTime": 1297137600000, 
    "lastFrameTime": 1297137600000, 
    "modifyCallbackToolName": "ModifyStormTrackTool", 
    "startTime": 1297137600000, 
    "endTime": 1297148400000, 
    "type": "SV.W"
}
