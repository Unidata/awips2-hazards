InputTestCaseData = { \
"caseDesc" : "Test override of a UGC.",
"siteID": "OAX",
"inputPolygon" : [ ( -96.31, 41.36 ),
                   ( -95.69, 41.07 ),
                   ( -95.62, 40.96 ),
                   ( -95.97, 40.87 ),
                   ( -96.31, 41.36 ) ],
"overrideXml" :
[
'<?xml version="1.0" encoding="ISO-8859-1" standalone="no"?>',
'    <feAreaTable>',
'        <ugc31153>EAST</ugc31153>',
'    </feAreaTable>'
]
}


TestCaseResults = \
[
"EAST",
"SOUTHEAST",
"SOUTHWEST",
"EAST CENTRAL"
]
