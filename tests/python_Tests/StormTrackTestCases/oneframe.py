InputTestCaseData = { \
"caseDesc" : "Single frame test",
"sessionAttributes" :
{
    "staticSettings": {
        "defaultDuration": 1800000, 
        "defaultSiteID": "OAX"
    }, 
    "framesInfo": {
        "frameTimeList": [
            1297137600000
        ], 
        "frameIndex": 0, 
        "currentFrame": "2011-02-08 04:00:00.0", 
        "frameCount": 1
    }, 
    "currentTime": 1297137600000
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
    "pivots": [
        0
    ], 
    "forJavaObj": {
        "currentTime": 1297137600000, 
        "track": [
            [
                -95.81677274717461, 
                41.928390087208854
            ], 
            [
                -95.65816823041222, 
                42.04606185924833
            ]
        ], 
        "hazardPolygon": [
            [
                -95.6577709389584, 
                42.205065594171856
            ], 
            [
                -95.44405398078004, 
                42.045568286347866
            ], 
            [
                -95.81677264204296, 
                41.80118686615204
            ], 
            [
                -95.98774906334067, 
                41.928263345301275
            ]
        ], 
        "phenomena": "FF", 
        "subType": "Convective", 
        "SiteID": "OAX", 
        "startTime": 1297137600000, 
        "significance": "W", 
        "endTime": 1297139400000
    }, 
    "trackPoints": [
        {
            "pointType": "tracking", 
            "pointID": 1297137600000, 
            "shapeType": "point", 
            "point": [
                -95.81677274717461, 
                41.928390087208854
            ]
        }, 
        {
            "pointType": "tracking", 
            "pointID": 1297139400000, 
            "shapeType": "point", 
            "point": [
                -95.65816823041222, 
                42.04606185924833
            ]
        }
    ], 
    "pivotTimes": [
        1297137600000
    ], 
    "stormMotion": {
        "bearing": 225, 
        "speed": 20
    }, 
    "creationTime": 1297137600000, 
    "modifyCallbackToolName": "ModifyStormTrackTool", 
    "state": "pending", 
    "startTime": 1297137600000, 
    "endTime": 1297139400000, 
    "type": "FF.W"
}
