InputTestCaseData = { \
"caseDesc" : "Editing a point not belonging to current frame",
"sessionAttributes" :
{
    "staticSettings": {
        "defaultDuration": 1800000, 
        "defaultSiteID": "OAX"
    }, 
    "framesInfo": {
        "frameTimeList": [
            1297127700000, 
            1297128600000, 
            1297129500000, 
            1297130400000, 
            1297131300000, 
            1297132200000, 
            1297133100000, 
            1297134000000, 
            1297134900000, 
            1297135800000, 
            1297136700000, 
            1297137600000
        ], 
        "frameIndex": 5, 
        "currentFrame": "2011-02-08 02:30:00.0", 
        "frameCount": 12
    }, 
    "currentTime": 1297137600000
},
"eventAttributes" :
{
    "pivots": [
        11
    ], 
    "checked": True, 
    "trackPoints": [
        {
            "pointType": "tracking", 
            "pointID": 1297127700000, 
            "shapeType": "point", 
            "point": [
                -96.85291290283203, 
                40.82378005981445
            ]
        }, 
        {
            "pointType": "tracking", 
            "pointID": 1297128600000, 
            "shapeType": "point", 
            "point": [
                -96.77578735351562, 
                40.88322067260742
            ]
        }, 
        {
            "pointType": "tracking", 
            "pointID": 1297129500000, 
            "shapeType": "point", 
            "point": [
                -96.69852447509766, 
                40.94261169433594
            ]
        }, 
        {
            "pointType": "tracking", 
            "pointID": 1297130400000, 
            "shapeType": "point", 
            "point": [
                -96.62112426757812, 
                41.001949310302734
            ]
        }, 
        {
            "pointType": "tracking", 
            "pointID": 1297131300000, 
            "shapeType": "point", 
            "point": [
                -96.54358673095703, 
                41.06123352050781
            ]
        }, 
        {
            "pointType": "tracking", 
            "pointID": 1297132200000, 
            "shapeType": "point", 
            "point": [
                -96.46590423583984, 
                41.12046813964844
            ]
        }, 
        {
            "pointType": "tracking", 
            "pointID": 1297133100000, 
            "shapeType": "point", 
            "point": [
                -96.3880844116211, 
                41.179649353027344
            ]
        }, 
        {
            "pointType": "tracking", 
            "pointID": 1297134000000, 
            "shapeType": "point", 
            "point": [
                -96.31012725830078, 
                41.23877716064453
            ]
        }, 
        {
            "pointType": "tracking", 
            "pointID": 1297134900000, 
            "shapeType": "point", 
            "point": [
                -96.23202514648438, 
                41.297855377197266
            ]
        }, 
        {
            "pointType": "tracking", 
            "pointID": 1297135800000, 
            "shapeType": "point", 
            "point": [
                -96.15377807617188, 
                41.356876373291016
            ]
        }, 
        {
            "pointType": "tracking", 
            "pointID": 1297136700000, 
            "shapeType": "point", 
            "point": [
                -96.07539367675781, 
                41.41584777832031
            ]
        }, 
        {
            "pointType": "tracking", 
            "pointID": 1297137600000, 
            "shapeType": "point", 
            "point": [
                -95.99686431884766, 
                41.47476577758789
            ]
        }, 
        {
            "pointType": "tracking", 
            "pointID": 1297138500000, 
            "shapeType": "point", 
            "point": [
                -95.91819763183594, 
                41.53363037109375
            ]
        }, 
        {
            "pointType": "tracking", 
            "pointID": 1297139400000, 
            "shapeType": "point", 
            "point": [
                -95.8393783569336, 
                41.592437744140625
            ]
        }
    ], 
    "issued": False, 
    "stormMotion": {
        "bearing": 225, 
        "speed": 20
    }, 
    "selected": True, 
    "pivotTimes": [
        1297137600000
    ], 
    "modifyCallbackToolName": "ModifyStormTrackTool", 
    "status": "pending",
    "startTime": 1297137600000, 
    "creationTime": 1297137600000, 
    "endTime": 1297139400000, 
    "type": "TO.W", 
    "hazardCategory": "Convective"
},
"spatialInputMap" :
{
    "eventID": "62", 
    "newLatLon": [
        -96.86804969588897, 
        41.09393782351508
    ], 
    "pointID": 1297127700000.0, 
    "shapeType": "dot"
}
}

TestCaseResults = \
{
    "checked": True, 
    "creationTime": 1297137600000, 
    "endTime": 1297139400000, 
    "forJavaObj": {
        "hazardPolygon": None, 
        "track": [
            [
                -96.85291290283203, 
                40.823780059814446
            ], 
            [
                -96.77578955619573, 
                40.8832208114198
            ], 
            [
                -96.69852761239207, 
                40.94261009953199
            ], 
            [
                -96.62112665185056, 
                41.00194772352017
            ], 
            [
                -96.54358625434216, 
                41.06123348198384
            ], 
            [
                -96.46590599898477, 
                41.120467172750296
            ], 
            [
                -96.38808546424917, 
                41.17964859287192
            ], 
            [
                -96.31012422796495, 
                41.23877753862369
            ], 
            [
                -96.23202186732637, 
                41.297853805500395
            ], 
            [
                -96.15377795889863, 
                41.356877188214156
            ], 
            [
                -96.07539207862406, 
                41.41584748069175
            ], 
            [
                -95.99686380182845, 
                41.47476447607203
            ], 
            [
                -95.91819270322755, 
                41.53362796670328
            ], 
            [
                -95.8393783569336, 
                41.592437744140625
            ]
        ]
    }, 
    "hazardCategory": "Convective", 
    "issued": False, 
    "modifyCallbackToolName": "ModifyStormTrackTool", 
    "pivotTimes": [
        1297137600000
    ], 
    "pivots": [
        11
    ], 
    "selected": True, 
    "startTime": 1297137600000, 
    "status": "pending",
    "stormMotion": {
        "bearing": 225.0001402222228,
        "speed": 20.00001004989287
    }, 
    "trackPoints": [
        {
            "point": [
                -96.85291290283203, 
                40.823780059814446
            ], 
            "pointID": 1297127700000, 
            "pointType": "tracking", 
            "shapeType": "point"
        }, 
        {
            "point": [
                -96.77578955619573, 
                40.8832208114198
            ], 
            "pointID": 1297128600000, 
            "pointType": "tracking", 
            "shapeType": "point"
        }, 
        {
            "point": [
                -96.69852761239207, 
                40.94261009953199
            ], 
            "pointID": 1297129500000, 
            "pointType": "tracking", 
            "shapeType": "point"
        }, 
        {
            "point": [
                -96.62112665185056, 
                41.00194772352017
            ], 
            "pointID": 1297130400000, 
            "pointType": "tracking", 
            "shapeType": "point"
        }, 
        {
            "point": [
                -96.54358625434216, 
                41.06123348198384
            ], 
            "pointID": 1297131300000, 
            "pointType": "tracking", 
            "shapeType": "point"
        }, 
        {
            "point": [
                -96.46590599898477, 
                41.120467172750296
            ], 
            "pointID": 1297132200000, 
            "pointType": "tracking", 
            "shapeType": "point"
        }, 
        {
            "point": [
                -96.38808546424917, 
                41.17964859287192
            ], 
            "pointID": 1297133100000, 
            "pointType": "tracking", 
            "shapeType": "point"
        }, 
        {
            "point": [
                -96.31012422796495, 
                41.23877753862369
            ], 
            "pointID": 1297134000000, 
            "pointType": "tracking", 
            "shapeType": "point"
        }, 
        {
            "point": [
                -96.23202186732637, 
                41.297853805500395
            ], 
            "pointID": 1297134900000, 
            "pointType": "tracking", 
            "shapeType": "point"
        }, 
        {
            "point": [
                -96.15377795889863, 
                41.356877188214156
            ], 
            "pointID": 1297135800000, 
            "pointType": "tracking", 
            "shapeType": "point"
        }, 
        {
            "point": [
                -96.07539207862406, 
                41.41584748069175
            ], 
            "pointID": 1297136700000, 
            "pointType": "tracking", 
            "shapeType": "point"
        }, 
        {
            "point": [
                -95.99686380182845, 
                41.47476447607203
            ], 
            "pointID": 1297137600000, 
            "pointType": "tracking", 
            "shapeType": "point"
        }, 
        {
            "point": [
                -95.91819270322755, 
                41.53362796670328
            ], 
            "pointID": 1297138500000, 
            "pointType": "tracking", 
            "shapeType": "point"
        }, 
        {
            "point": [
                -95.8393783569336, 
                41.592437744140625
            ], 
            "pointID": 1297139400000, 
            "pointType": "tracking", 
            "shapeType": "point"
        }
    ], 
    "type": "TO.W"
}
