# ----------------------------------------------------------------------------
# This software is in the public domain, furnished "as is", without technical
# support, and with no warranty, express or implied, as to its usefulness for
# any purpose.
#
# Headlines Timing
#
# Author:
# ----------------------------------------------------------------------------

scripts = [
    {
    "commentary": "Clear out all Hazards Table and Grids.",
    "name": "EXPNEW_0",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },

    {
    "commentary": "Generating WS.W Test events",
    "name": "ETNReset_1",
    "drtTime": "20100101_0510",
    "productType": "WSW",
    "createGrids": [
       (12, 24, "WS.W", ["FLZ050"]),
       (30, 36, "WS.W", ["FLZ050"]),
       (40, 48, "WS.W", ["FLZ050"]),
       ],
    "vtecMode": "T",
    "checkStrings": [
      "FLZ050-",
      "/T.NEW.KTBW.WS.W.0001.100101T1700Z-100102T0500Z/",
      "/T.NEW.KTBW.WS.W.0002.100102T1100Z-100102T1700Z/",
      "/T.NEW.KTBW.WS.W.0003.100102T2100Z-100103T0500Z/",
                     ],
    },

    {
    "commentary": """Running operational mode. Adds new event.  Formatter 
      ignores previous test mode vtec table entries, except for ETN
      determination.""",
    "name": "ETNReset_2",
    "drtTime": "20100108_1801",
    "productType": "WSW",
    "vtecMode": "O",
    "createGrids": [
       (8*24, 11*24, "WS.W", ["FLZ050"]),
       ],
    "checkStrings": [
      "FLZ050-",
      "/O.NEW.KTBW.WS.W.0004.100109T0500Z-100112T0500Z/",
                     ],
    },

    {
    "commentary": "Operational event issued. ETN increments from last event.",
    "name": "ETNReset_3",
    "drtTime": "20100115_1800",
    "productType": "WSW",
    "vtecMode": "O",
    "createGrids": [
       (15*24, 18*24, "WS.W", ["FLZ050"]),
       ],
    "checkStrings": [
      "FLZ050-",
      "/O.NEW.KTBW.WS.W.0005.100116T0500Z-100119T0500Z/",
                     ],
    },

    {
    "commentary": "Test event issued. ETNs increments.",
    "name": "ETNReset_4",
    "drtTime": "20100115_2100",
    "productType": "WSW",
    "vtecMode": "T",
    "createGrids": [
       (15*24, 16*24, "WS.W", ["FLZ050"]),
       (20*24, 21*24, "WS.W", ["FLZ050"]),
       ],
    "checkStrings": [
       "FLZ050-",
       "/T.NEW.KTBW.WS.W.0006.100116T0500Z-100117T0500Z/",
       "/T.NEW.KTBW.WS.W.0007.100121T0500Z-100122T0500Z/",
                     ],
    },
    {
    "commentary": "Operational Event issued. Formatter ignores test events.",
    "name": "ETNReset_5",
    "drtTime": "20100115_2200",
    "productType": "WSW",
    "vtecMode": "O",
    "createGrids": [
       (15*24, 18*24, "WS.W", ["FLZ050"]),
       (20*24, 21*24, "WS.W", ["FLZ050"]),
       ],
    "checkStrings": [
       "FLZ050-",
       "/O.NEW.KTBW.WS.W.0008.100121T0500Z-100122T0500Z/",
       "/O.CON.KTBW.WS.W.0005.100116T0500Z-100119T0500Z/",
                     ],
    },

    {
    "commentary": "Test event issued. ETN increments.",
    "name": "ETNReset_6",
    "drtTime": "20100123_2000",
    "productType": "WSW",
    "vtecMode": "T",
    "createGrids": [
       (23*24, 25*24, "WS.W", ["FLZ050"]),
       ],
    "checkStrings": [
       "FLZ050-",
       "/T.NEW.KTBW.WS.W.0009.100124T0500Z-100126T0500Z/",
                     ],
    },

    {
    "commentary": "Operational event issued. ETN increments.",
    "name": "ETNReset_7",
    "drtTime": "20100131_2000",
    "productType": "WSW",
    "vtecMode": "O",
    "createGrids": [
       (31*24, (31*24)+2, "WS.W", ["FLZ050"]),
       ],
    "checkStrings": [
       "FLZ050-",
       "/O.NEW.KTBW.WS.W.0010.100201T0500Z-100201T0700Z/",
              ],
    },
    {
    "commentary": "Cleanup.",
    "name": "ETNReset_8_cleanup",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },
    {
    "commentary": """Simulating PQR's SU.Y set of events.  Issue 3 events
      in test mode.  The issue was that the SU.Y ETN was stuck due to 
      the interaction between test and operational events and ETNs.""",
    "name": "ETNReset_9",
    "drtTime": "20100201_0000",
    "gridsStartTime": "20100201_0000",
    "productType": "CFW",
    "createGrids": [
       (0, 6, "SU.Y", ["FLZ049"]),
       (10, 16, "SU.Y", ["FLZ049"]),
       (20, 26, "SU.Y", ["FLZ049"]),
       ],
    "vtecMode": "T",
    "checkStrings": [
       "FLZ049-",
       "/T.NEW.KTBW.SU.Y.0001.100201T0000Z-100201T0600Z/",
       "/T.NEW.KTBW.SU.Y.0002.100201T1000Z-100201T1600Z/",
       "/T.NEW.KTBW.SU.Y.0003.100201T2000Z-100202T0200Z/",
                     ],
    },
    {
    "commentary": """Simulating PQR's SU.Y set of events - issue SU.Y 0004 NEW
      in test mode.""",
    "name": "ETNReset_10",
    "drtTime": "20100527_1600",
    "gridsStartTime": "20100527_0000",
    "productType": "CFW",
    "createGrids": [
       (16, 34, "SU.Y", ["FLZ049"]),
       ],
    "vtecMode": "T",
    "checkStrings": [
       "FLZ049-",
       "/T.NEW.KTBW.SU.Y.0004.100527T1600Z-100528T1000Z/",
                     ],
    },
    {
    "commentary": "CAN the SU.Y 0004 in test mode.",
    "name": "ETNReset_11",
    "drtTime": "20100527_1805",
    "gridsStartTime": "20100527_0000",
    "productType": "CFW",
    "createGrids": [
       ],
    "vtecMode": "T",
    "checkStrings": [
      "FLZ049-",
      "/T.CAN.KTBW.SU.Y.0004.000000T0000Z-100528T1000Z/",
                     ],
    },
    {
    "name": "ETNReset_12",
    "commentary": """Issue a new SU.Y in operational mode.  Gets assigned the
      next ETN.""",
    "drtTime": "20100914_0000",
    "gridsStartTime": "20100914_0000",
    "productType": "CFW",
    "createGrids": [
       (0, 24, "SU.Y", ["FLZ051"]),
       ],
    "vtecMode": "O",
    "checkStrings": [
        "FLZ051-",
        "/O.NEW.KTBW.SU.Y.0005.100914T0000Z-100915T0000Z/",
                     ],
    },
    {
    "name": "ETNReset_12a",
    "commentary": "Cancel the SU.Y.",
    "drtTime": "20100914_1200",
    "gridsStartTime": "20100914_0000",
    "productType": "CFW",
    "createGrids": [
       ],
    "vtecMode": "O",
    "checkStrings": [
      "FLZ051-",
      "/O.CAN.KTBW.SU.Y.0005.000000T0000Z-100915T0000Z/",
                     ],
    },
    {
    "commentary": "force a vtec table squeeze, to remove old events",
    "name": "ETNReset_12b",
    "drtTime": "20100918_0000",
    "gridsStartTime": "20100918_0000",
    "productType": "NPW",
    "createGrids": [
       (0, 24, "HW.W", ["FLZ048","FLZ049","FLZ051"]),
       ],
    "vtecMode": "O",
    "checkStrings": [
      "FLZ048-049-051-",
      "/O.NEW.KTBW.HW.W.0001.100918T0000Z-100919T0000Z/",
                     ],
    },
    {
    "name": "ETNReset_13",
    "commentary": "issue a new SU.Y in operational mode",
    "drtTime": "20100922_0000",
    "gridsStartTime": "20100922_0000",
    "productType": "CFW",
    "createGrids": [
       (0, 24, "SU.Y", ["FLZ048","FLZ049","FLZ051"]),
       ],
    "vtecMode": "O",
    "checkStrings": [
      "FLZ048-049-051-",
      "/O.NEW.KTBW.SU.Y.0006.100922T0000Z-100923T0000Z/",
                     ],
    },
    {
    "name": "ETNReset_13a",
    "commentary": "expire time for the event",
    "drtTime": "20100923_0000",
    "gridsStartTime": "20100922_0000",
    "productType": "CFW",
    "createGrids": [
       (0, 24, "SU.Y", ["FLZ048","FLZ049","FLZ051"]),
       ],
    "vtecMode": "O",
    "checkStrings": [
      "FLZ048-049-051-",
      "/O.EXP.KTBW.SU.Y.0006.000000T0000Z-100923T0000Z/",
                     ],
    },
    {
    "name": "ETNReset_14",
    "commentary": "Issue an operational SU.Y event, with new ETN.",
    "drtTime": "20100928_0000",
    "gridsStartTime": "20100928_0000",
    "productType": "CFW",
    "createGrids": [
       (0, 24, "SU.Y", ["FLZ048","FLZ049","FLZ051"]),
       ],
    "vtecMode": "O",
    "checkStrings": [
      "FLZ048-049-051-",
      "/O.NEW.KTBW.SU.Y.0007.100928T0000Z-100929T0000Z/",
                     ],
    },
    {
    "commentary": "Cleanup.",
    "name": "ETNReset_15",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    }
    ]

       
import test.VTEC_Tests.TestScript as TestScript
def testScript():
    defaults = {
        "decodeVTEC": 1,
        "gridsStartTime": "20100101_0500",
        "vtecMode": "O",
        }
    return TestScript.generalTestScript(scripts, defaults)




