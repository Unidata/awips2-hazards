InputTestCaseData = { \
"caseDesc" : "Single frame test",
"sessionAttributes" :
{
    "framesInfo": {
        "frameTimeList": [
            1297137600000
        ], 
        "frameIndex": 0, 
        "currentFrame": "2011-02-08 04:00:00.0", 
        "frameCount": 1
    }, 
    "currentTime": 1297137600000,
    "siteID" : "OAX"
},
"spatialInputMap" :
{
    "spatialInfo": {
        "points": [
            [
                [
                    41.928390087208854, 
                    -95.81677274717461
                ], 
                1297137600.0
            ]
        ]
    }
}
}

TestCaseResults = \
{
   "creationTime": 1297137600000, 
    "endTime": 1297141200000, 
    "forJavaObj": {
        "SiteID": "OAX", 
        "currentTime": 1297137600000, 
        "endTime": 1297141200000, 
        "hazardPolygon": [
            [
                -95.6576, 
                42.2368
            ], 
            [
                -95.4012, 
                42.0454
            ], 
            [
                -95.8167, 
                41.8011
            ], 
            [
                -95.9877, 
                41.9282
            ]
        ], 
        "phenomena": "FF", 
        "significance": "W", 
        "startTime": 1297137600000, 
        "subType": "Convective", 
        "track": [
            [
                -95.8167, 
                41.9283
            ], 
            [
                -95.6581, 
                42.046
            ]
        ]
    }, 
    "modifyCallbackToolName": "ModifyStormTrackTool", 
    "pivotTimes": [
        1297137600000
    ], 
    "pivots": [
        0
    ], 
    "startTime": 1297137600000, 
    "status": "pending",
    "stormMotion": {
        "bearing": 225, 
        "speed": 10
    }, 
    "trackPoints": [
        {
            "point": [
                -95.8167, 
                41.9283
            ], 
            "pointID": 1297137600000, 
            "pointType": "tracking", 
            "shapeType": "point"
        }, 
        {
            "point": [
                -95.6581, 
                42.046
            ], 
            "pointID": 1297141200000, 
            "pointType": "tracking", 
            "shapeType": "point"
        }
    ], 
    "type": "FF.W"
}
