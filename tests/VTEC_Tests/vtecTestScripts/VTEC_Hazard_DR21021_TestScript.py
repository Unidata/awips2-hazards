# ----------------------------------------------------------------------------
# This software is in the public domain, furnished "as is", without technical
# support, and with no warranty, express or implied, as to its usefulness for
# any purpose.
#
# DR21021
#
# Author: mathewson
# ----------------------------------------------------------------------------

import TestScript

scripts = [
    {    
    "name":"TAE_DR21021_1", 
    "productType": None,
    "commentary": "Clear out all Hazard Tables and Grids",
    "checkStrings": [
         ],
    "clearHazardsTable": 1,
    },

    {    
    "name":"TAE_DR21021_2", 
    "productType": "HLS",
    "drtTime": "20100201_0000",
    "gridsStartTime": "20100201_0000",
    "commentary": "TAE initial setup - step 1, create TR.W etn=1022",
    "checkStrings": [
      "GMZ830-",
      "/O.NEW.KTBW.TR.W.1022.100201T0000Z-000000T0000Z/",
         ],
    "createGrids": [
      (0, 24, "TR.W", ["GMZ830"], {'forceEtn': 1022}),
         ],
    },

    {    
    "name":"TAE_DR21021_3", 
    "productType": "HLS",
    "drtTime": "20100202_0000",
    "commentary": "TAE initial setup - step 1, cancel TR.W",
    "checkStrings": [
      "GMZ830-",
      "/O.CAN.KTBW.TR.W.1022.000000T0000Z-000000T0000Z/",
      ],
    "createGrids": [
      ],
    },

    {    
    "name":"TAE_DR21021_4", 
    "productType": "HLS",
    "drtTime": "20100401_0000",
    "gridsStartTime": "20100401_0000",
    "commentary": "TAE initial setup - setting up active table, create events for testing (NEW)",
    "checkStrings": [
      "FLZ048>050-",
      "/O.NEW.KTBW.HU.W.1022.100401T0000Z-000000T0000Z/",
      "GMZ870-873-",
      "/O.NEW.KTBW.HU.W.1022.100401T0000Z-000000T0000Z/",
      "FLZ039-042-043-",
      "/O.NEW.KTBW.TR.W.1022.100401T0000Z-000000T0000Z/",
      "GMZ830-850-853-856-",
      "/O.NEW.KTBW.TR.W.1022.100401T0000Z-000000T0000Z/",
      ],

    "createGrids": [
      (0, 24, "TR.W", ["GMZ830","GMZ850","GMZ853","GMZ856"], {'forceEtn': 1022}),
      (0, 24, "HU.W", ["GMZ873","GMZ870"], {'forceEtn': 1022}),
      (0, 24, "TR.W", ["FLZ039","FLZ042","FLZ043"], {'forceEtn': 1022}),
      (0, 24, "HU.W", ["FLZ048","FLZ049","FLZ050"], {'forceEtn': 1022}),
      ],
    },

    {    
    "name":"TAE_DR21021_5", 
    "productType": "HLS",
    "drtTime": "20100401_0000",
    "gridsStartTime": "20100401_0000",
    "commentary": "TAE initial setup - setting up active table, create events for testing (CON) - this is the initial testing state we need",
    "checkStrings": [
      "FLZ048>050-",
      "/O.CON.KTBW.HU.W.1022.000000T0000Z-000000T0000Z/",
      "GMZ870-873-",
      "/O.CON.KTBW.HU.W.1022.000000T0000Z-000000T0000Z/",
      "FLZ039-042-043-",
      "/O.CON.KTBW.TR.W.1022.000000T0000Z-000000T0000Z/",
      "GMZ830-850-853-856-",
      "/O.CON.KTBW.TR.W.1022.000000T0000Z-000000T0000Z/",
      ],
    "createGrids": [
      (0, 24, "TR.W", ["GMZ830","GMZ850","GMZ853","GMZ856"]),
      (0, 24, "HU.W", ["GMZ873","GMZ870"]),
      (0, 24, "TR.W", ["FLZ039","FLZ042","FLZ043"], {'forceEtn': 1022}),
      (0, 24, "HU.W", ["FLZ048","FLZ049","FLZ050"], {'forceEtn': 1022}),
      ],
    },

    {    
    "name":"TAE_DR21021_6", 
    "productType": "HLS",
    "drtTime": "20100401_0100",
    "gridsStartTime": "20100401_0000",
    "commentary": "TAE setting grids that caused error, change HU.W to TR.W",
    "checkStrings": [
      "FLZ048>050-",
      "/O.CAN.KTBW.HU.W.1022.000000T0000Z-000000T0000Z/",
      "/O.EXA.KTBW.TR.W.1022.000000T0000Z-000000T0000Z/",
      "GMZ870-873-",
      "/O.CAN.KTBW.HU.W.1022.000000T0000Z-000000T0000Z/",
      "/O.EXA.KTBW.TR.W.1022.000000T0000Z-000000T0000Z/",
      "FLZ039-042-043-",
      "/O.CON.KTBW.TR.W.1022.000000T0000Z-000000T0000Z/",
      "GMZ830-850-853-856-",
      "/O.CON.KTBW.TR.W.1022.000000T0000Z-000000T0000Z/",
      ],
    "createGrids": [
      (0, 24, "TR.W", ["FLZ048","FLZ049","FLZ050","FLZ039","FLZ042","FLZ043"], {'forceEtn': 1022}),
      (0, 24, "TR.W", ["GMZ830","GMZ850","GMZ853","GMZ873","GMZ856","GMZ870"]),
      ],
    },

    {    
    "name":"TAE_DR21021_7, MOB_DR21021_1", 
    "productType": None,
    "commentary": "Clear out all Hazard Tables and Grids",
    "checkStrings": [
         ],
    "clearHazardsTable": 1,
    },

    {    
    "name":"MOB_DR21021_2", 
    "productType": "HLS",
    "drtTime": "20101108_2214",
    "gridsStartTime": "20101108_2200",
    "commentary": "MOB initial setup - setting up active table",
    "checkStrings": [
      "FLZ039-042-043-048>050-",
      "/O.NEW.KTBW.HU.A.1011.101108T2214Z-000000T0000Z/",
      ],
    "createGrids": [
      (0, 24, "HU.A", ["FLZ049","FLZ050","FLZ039","FLZ042","FLZ043","FLZ048"], {'forceEtn': 1011}),
      ],
    },

    {    
    "name":"MOB_DR21021_3", 
    "productType": "HLS",
    "drtTime": "20101108_2349",
    "gridsStartTime": "20101108_2200",
    "commentary": "MOB test - adding marine HU.A",
    "checkStrings": [
      "GMZ850-853-870-876-",
      "/O.EXA.KTBW.HU.A.1011.000000T0000Z-000000T0000Z/",
      "FLZ039-042-043-048>050-",
      "/O.CON.KTBW.HU.A.1011.000000T0000Z-000000T0000Z/",
      ],

    "createGrids": [
      (0, 24, "HU.A", ["FLZ048","FLZ049", "FLZ039", "FLZ042", "FLZ043","FLZ050"], {'forceEtn': 1011}),
      (24, 48, "HU.A", ["GMZ850", "GMZ853","GMZ870","GMZ876"]),
      ],
    },

    {    
    "name":"MOB_DR21021_4", 
    "productType": None,
    "commentary": "Clear out all Hazard Tables and Grids",
    "checkStrings": [
         ],
    "clearHazardsTable": 1,
    },
    ]

#import AFPS
def testScript():
    gridsStartTime = "20100601_0400"
    drtTime = "20100601_0800"
    defaults = {
        "gridsStartTime": gridsStartTime,
        "drtTime": drtTime,
        "decodeVTEC": 1,
        }
    return TestScript.generalTestScript(scripts, defaults)
