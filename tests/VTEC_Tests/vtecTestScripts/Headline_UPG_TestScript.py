# ----------------------------------------------------------------------------
# This software is in the public domain, furnished "as is", without technical
# support, and with no warranty, express or implied, as to its usefulness for
# any purpose.
#
# Headline UPG phrasing for tropical events
#
# Author:
# ----------------------------------------------------------------------------

scripts = [
    {
    "commentary": "Clear out all Hazards Table and Grids.",
    "name": "HeadlineUPG_Init",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },


#scenario SC.Y ---> GL.W (no UPG headline phrase present)
    {
    "commentary": "Initial setup of SC.Y for SC.Y--->GL.W (no UPG headline phrase present)",
    "name": "HeadlineUPG_1a",
    "drtTime": "20101203_0200",
    "gridsStartTime": "20101203_0000",
    "productType": "MWW",
    "createGrids": [
       (0, 36, "SC.Y", ["GMZ870"]),
       ],
    "checkStrings": [
       "GMZ870-",
       "/O.NEW.KTBW.SC.Y.0001.101203T0200Z-101204T1200Z/",
       ],
    },

    {
    "commentary": "SC.Y ---> GL.W for SC.Y--->GL.W (no UPG headline phrase present)",
    "name": "HeadlineUPG_1b",
    "drtTime": "20101203_0200",
    "gridsStartTime": "20101203_0000",
    "productType": "MWW",
    "createGrids": [
       (0, 36, "GL.W", ["GMZ870"]),
       ],
    "checkStrings": [
       "GMZ870-",
       "/O.UPG.KTBW.SC.Y.0001.000000T0000Z-101204T1200Z/",
       "/O.NEW.KTBW.GL.W.0001.101203T0200Z-101204T1200Z/",
       ],
    },

    {
    "commentary": "Deleting hazard grids.",
    "name": "HeadlineUPG_1c",
    "productType": None,
    "clearHazardsTable": 1,
    "checkStrings": [],
    },

    ]

       
import TestScript as TestScript
def testScript():
    defaults = {
        "database": "<site>_GRID__Official_00000000_0000",
        "publishGrids": 1,
        "decodeVTEC": 1,
        "gridsStartTime": "20100101_0500",
        "orderStrings": 1,
        "vtecMode": "O",
        "deleteGrids": [("Fcst", "Hazards", "SFC", "all", "all")],
        }
    return TestScript.generalTestScript(scripts, defaults)




