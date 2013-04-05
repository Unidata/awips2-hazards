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
    "name": "ITR_0",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },
    {
    "commentary": "setup for cross year with EXB test",
    "name": "ITR_1",
    "drtTime": "20091231_2300",
    "gridsStartTime": "20091231_2300",
    "productType": "MWW",
    "createGrids": [
       (0, 23, "GL.W", ["GMZ830","GMZ870"]),
       ],
    "checkStrings": [
       "GMZ830-870-",
       "/E.NEW.KTBW.GL.W.0001.091231T2300Z-100101T2200Z/"
       ],
    },
    {
    "commentary": "Testing cross year with EXT, EXB action",
    "name": "ITR_2",
    "drtTime": "20100101_1500",
    "gridsStartTime": "20100101_0500",
    "productType": "MWW",
    "createGrids": [
       (0, 14, "GL.W", ["GMZ856","GMZ830","GMZ870"]),
       ],
    "checkStrings": [
       "GMZ856-",
       "/E.EXB.KTBW.GL.W.0001.000000T0000Z-100101T1900Z/",
       "GMZ830-870-",
       "/E.EXT.KTBW.GL.W.0001.000000T0000Z-100101T1900Z/",
        ],
    },
    ]
       
import test.VTEC_Tests.TestScript as TestScript
def testScript():
    defaults = {
        "decodeVTEC": 1,
        "vtecMode": "E",
        }
    return TestScript.generalTestScript(scripts, defaults)




