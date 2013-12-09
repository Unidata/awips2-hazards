InputTestCaseData = { \
"caseDesc" : "Test override of a code.",
"siteID": "OAX",
"inputPolygon" : [ ( -95.59, 41.11 ),
                   ( -95.56, 40.85 ),
                   ( -95.97, 40.87 ),
                   ( -96.17, 41.13 ),
                   ( -95.59, 41.11 ) ],
"overrideXml" :
[
'<?xml version="1.0" encoding="ISO-8859-1" standalone="no"?>',
'    <feAreaTable>',
'        <sw>SOUTHWESTERN</sw>',
'    </feAreaTable>'
]
}


TestCaseResults = \
[
"EAST CENTRAL",
"SOUTHEAST",
"SOUTHWESTERN",
"SOUTHWESTERN"
]
