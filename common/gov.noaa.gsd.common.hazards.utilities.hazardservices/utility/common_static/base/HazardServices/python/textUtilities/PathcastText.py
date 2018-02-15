'''
    Description: Handles all the text formatting needed to generate the
    PathCast portion of a product.
     
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Aug 08, 2016 21056      Robert.Blum Initial creation
    Aug 22, 2016 21056      Robert.Blum Update to pathcast for creating the fall back locations.


'''

from dateutil import tz
import datetime
from TextProductCommon import TextProductCommon

class PathcastText(object):

    def __init__(self, sectionDict, testMode, action, timeZones, locationsFallBack):
        # Currently all Products will only have single event per section.
        self.hazardDict = sectionDict.get('hazardEvents', [])[0]
        phen = self.hazardDict.get("phen")
        sig = self.hazardDict.get("sig")
        subType = self.hazardDict.get("subType")
        self.hazardType = phen + "." + sig
        if subType:
            self.hazardType += "." + subType

        # List of StormTrackPoints that contain closestPoints
        self.pathcastData = self.hazardDict.get('pathcastData', [])

        # A list of typically 3rd level towns
        self.otherPoints = self.hazardDict.get('pathcastOtherPoints', [])

        self.timeFormat = "%I%M %p %Z"
        self.action = action
        self.localTimeZone = tz.gettz(timeZones[0])
        self.tpc = TextProductCommon()
        stormMotion = self.hazardDict.get("stormMotion", {})
        self.speed = stormMotion.get("speed", 0)
        self.testMode = testMode
        self.locationsFallBack = locationsFallBack

    def getPathcastText(self):
        text = ""
        if self.hazardType == "FF.W.NonConvective":
            text += self.pathcast_FF_W_NonConvective()
        else:
            text += self.defaultHydroPathcast()
        return text

    def defaultHydroPathcast(self, otherLead="heavy rain"):
        text = ""
        if self.speed < 3:
            pathcastLead = "Heavy rain will continue over the following locations...\n"
            text += self.pathcastWorker(pathcastLead, otherLead, False)
        else:
            pathcastLead = "Heavy rain will move over the following locations...\n"
            text += self.pathcastWorker(pathcastLead, otherLead, False)
        return text

    def pathcast_FF_W_NonConvective(self):
        return self.pathcastWorker("The flood will be near...\n", "This flooding", False)

    def pathcastWorker(self, pathcastLead, otherLead, marineFlag):
        pathcastString = ""
        if self.pathcastData:
            pathcastString += pathcastLead
            for trackPoint in self.pathcastData:
                points = trackPoint.get("closestPoints", [])
                if points:
                    pathcastString += "  "
                    cityTextList = []
                    for city in points:
                        cityText = city.get("name", "").title().strip()
                        if cityText:
                            cityTextList.append(cityText)
                    pathcastString += self.tpc.formatDelimitedList(cityTextList, delimiter=", ")
                    tpTime = trackPoint.get("pointID")
                    tpTime = datetime.datetime.fromtimestamp(tpTime/1000)
                    tpTime = tpTime.replace(tzinfo=tz.tzutc())
                    localTime = tpTime.astimezone(self.localTimeZone)
                    localTime = self.tpc.round(localTime, roundMinute=5)
                    fmtTime = localTime.strftime("%I%M %p %Z")
                    if fmtTime[0] == '0' :
                        fmtTime = fmtTime[1:]
                    pathcastString = pathcastString + " around " + fmtTime + ".\n"
            ###NOW SEARCH FOR OTHER POINTS (PRESUMABLY 3s) AND LIST THEM HERE
            numOtherPoints = len(self.otherPoints)
            if numOtherPoints > 0:
                # Focal points assert that otherLead will never be proper noun, so add .lower()
                # in case of inadvertant use of all upper case.
                pathcastString += "\nOther locations impacted by " + otherLead.lower() + " include "
                strippedLocs = [loc.strip() for loc in self.otherPoints]
                pathcastString += self.tpc.formatDelimitedList(strippedLocs, ', ')
                pathcastString += '.'
        else:
            if pathcastString:
                pathcastString += "\n"
            if marineFlag:
                pathcastString += otherLead.capitalize() + " will remain over mainly open waters."
            else:
                pathcastString += otherLead.capitalize() + " will remain over mainly rural areas of " + self.locationsFallBack + '.'
        if pathcastString:
            return pathcastString
        return "|* ENTER PATHCAST *|"
