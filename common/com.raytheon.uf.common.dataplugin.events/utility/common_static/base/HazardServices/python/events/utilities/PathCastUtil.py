# #
# This software was developed and / or modified by Raytheon Company,
# pursuant to Contract DG133W-05-CQ-1067 with the US Government.
# 
# U.S. EXPORT CONTROLLED TECHNICAL DATA
# This software product contains export-restricted data whose
# export/transfer/disclosure is restricted by U.S. law. Dissemination
# to non-U.S. persons whether in the United States or abroad requires
# an export license or other authorization.
# 
# Contractor Name:        Raytheon Company
# Contractor Address:     6825 Pine Street, Suite 340
#                         Mail Stop B8
#                         Omaha, NE 68106
#                         402.291.0100
# 
# See the AWIPS II Master Rights File ("Master Rights File.pdf") for
# further licensing information.
# #


#
# Utility methods for generation of Pathcast data/locations.
#
#
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    Aug 08, 2016    21056         Robert.Blum    Initial Creation
#    Aug 16, 2016    21056         Robert.Blum    Change to use WFO Center.
#    Aug 18, 2016    21056         Robert.Blum    Fixed incomplete line of code.
#
# 
#

import datetime, time
import GeometryFactory
import SpatialQuery
from PathcastConfig import PathcastConfig
import JUtil
from dateutil import tz
from com.raytheon.uf.common.geospatial import MapUtil
from com.raytheon.uf.viz.points import PointsDataManager
from org.geotools.geometry.jts import JTS

def preparePathCastData(hazardEvent):
    '''
     This method returns the pathcast data.

     @return the Pathcast
    '''
    # Area configuration
    areaSource = PathcastConfig.get("areaSource")
    areaField = PathcastConfig.get("areaField")
    parentAreaField = PathcastConfig.get("parentAreaField")

    # Point configuration
    pointSource = PathcastConfig.get("pointSource")
    pointField = PathcastConfig.get("pointField")

    # Basic configuration
    maxCount = PathcastConfig.get("maxCount")
    maxGroup = PathcastConfig.get("maxGroup")
    withInPolygon = PathcastConfig.get("withInPolygon")

    # convert the threshold to Meters
    thresholdInMeters = PathcastConfig.get("thresholdInMiles") * 1609.344

    stormMotion = hazardEvent.get("stormMotion")
    if stormMotion:
        speed = stormMotion.get("speed")
        bearing = stormMotion.get("bearing")

    trackPoints = list(hazardEvent.get("trackPoints", []))
    if stormMotion: # Storm Track Recommender was ran
        # Create a list of all the coordinates
        coordinates = []
        for point in trackPoints:
            coord = GeometryFactory.createPoint(point.get("point"))
            coordinates.append(coord)

        if len(coordinates) < 2:
            coordinates.append(coordinates.get(0))

        # Create a LineString from the TrackPoints and then buffer it
        # to get a polygon to query locations for.
        geom = GeometryFactory.createLineString(coordinates)
        javaGeom = JUtil.pyValToJavaObj(geom)
        pdm = PointsDataManager.getInstance();
        wfoCenter = pdm.getWfoCenter();
        crs = MapUtil.constructStereographic(MapUtil.AWIPS_EARTH_RADIUS, MapUtil.AWIPS_EARTH_RADIUS, wfoCenter.y, wfoCenter.x)
        latLonToLocal = MapUtil.getTransformFromLatLon(crs)
        javaGeom = JTS.transform(javaGeom, latLonToLocal)
        javaGeom = JTS.transform(javaGeom.convexHull().buffer(thresholdInMeters), latLonToLocal.inverse())
        geom = JUtil.javaObjToPyVal(javaGeom)

        if withInPolygon:
            # Means that all points returned must be within the polygon
            bufferedPathCastArea = hazardEvent.getProductGeometry().intersection(geom)
        else:
            bufferedPathCastArea = geom
    else: # Stationary, Storm Track Recommender was not ran
        bufferedPathCastArea = hazardEvent.getProductGeometry()
        trackPoint = {"pointType": "tracking",
                      "shapeType": "point",
                      "pointID": time.mktime(hazardEvent.getStartTime().timetuple()) * 1000,
                      "point": hazardEvent.getProductGeometry().centroid,
                      }
        trackPoints.append(trackPoint)

    # Query for all the locations and areas for the pathcast based on the buffered area
    warngenLocDicts = SpatialQuery.executeConfiguredQuery(bufferedPathCastArea, hazardEvent.getSiteID(), "PathcastPoints")
    areaFeaturesDicts = SpatialQuery.executeConfiguredQuery(bufferedPathCastArea, hazardEvent.getSiteID(), "PathcastAreas")

    for trackPoint in trackPoints:
        tpPoint = trackPoint.get("point", None)
        if tpPoint:
            tpPoint = GeometryFactory.createPoint(tpPoint)
        else:
            tpPoint = hazardEvent.getProductGeometry().centroid

        myArea = None
        if areaFeaturesDicts != None:
            # Find area and parent area
            for areaDict in areaFeaturesDicts:
                if areaDict.get("geom").contains(tpPoint):
                    myArea = areaDict
                    break

        # Set area info
        if myArea != None:
            trackPoint["area"] = str(myArea.get(areaField)).strip()
            trackPoint["parentArea"] = str(myArea.get(parentAreaField))

        # Get the closest points for this track point.
        closestPoints = getClosestPoints(pointField, areaField, parentAreaField, thresholdInMeters, \
                                              tpPoint, areaFeaturesDicts, trackPoint, warngenLocDicts)
        trackPoint["closestPoints"] = closestPoints

    # Figure out which locations should go with which trackpoint. Starts
    # with first trackpoint and go through each location within maxCount,
    # check for same location in other trackpoints. If same location
    # exists, remove from which ever trackpoint is farthest away
    closestPtCoords = []
    for i in range(0, len(trackPoints)):
        tp = trackPoints[i]
        points = tp.get("closestPoints")
        for cp in list(points):
            for j in range(i+1, len(trackPoints)):
                tp2 = trackPoints[j]
                if tp2 != tp:
                    points2 = tp2.get("closestPoints")
                    found = find(cp, points2)
                    if found != None:
                        # We found a point within maxCount in this list.
                        if found.get("distance") < cp.get("distance"):
                            # This point is closer to the other pathcast
                            points.remove(cp)
                            break
                        else:
                            # Remove from other pathcast, we are closer
                            points2.remove(found)
                        tp2["closestPoints"] = points2
        tmpPoints = []
        # Truncate the list to maxCount
        del points[maxCount:]
        for cp in points:
            coord = cp.get("point")
            if not coord in closestPtCoords:
                # To prevent duplicate cities in pathcast,
                # only unused point is added to tmpPoints
                tmpPoints.append(cp)
                closestPtCoords.append(coord)
        tp["closestPoints"] = points

    # Remove any trackpoints from the list that 
    # do not contain any closest points.
    for tp in list(trackPoints):
        if len(tp.get("closestPoints", [])) == 0: 
            trackPoints.remove(tp)

    # Ensure we are within the maxGroup
    while len(trackPoints) > maxGroup:
        del trackPoints[-1]

    return trackPoints

def getClosestPoints(pointField, areaField, parentAreaField, thresholdInMeters, tpPoint, areaFeatures, trackPoint, warngenLocations):
    '''
        Returns a list of impacted points/areas that are relative to the centroid.
        
        @param pointField:
        @param areaField:
        @param thresholdInMeters:
        @param tpGeom: 
        @param areaFeatures:
        @param trackPoint:
        @param warngenLocations:
        @return
    '''
    # Find closest points
    points = []
    for warngenLocation in warngenLocations:
        locationPoint = warngenLocation.get("geom").centroid
        closestCoord = None
        if tpPoint != None:
            # Must convert both Points to java
            # otherwise the python distance() call fails.
            jTrackPt = JUtil.pyValToJavaObj(tpPoint)
            jLocationPoint = JUtil.pyValToJavaObj(locationPoint)
            distance = jTrackPt.distance(jLocationPoint)
            if distance <= thresholdInMeters:
                found = False
                area = None
                parentArea = None
                for areaRslt in areaFeatures:
                    if areaRslt.get("geom").contains(locationPoint):
                        area = str(areaRslt.get(areaField))
                        parentArea = str(areaRslt.get(parentAreaField))
                        found = True
                        break
                if not found:
                    area = trackPoint.get("area")
                    parentArea = trackPoint.get("parentArea")

                cp = createClosestPoint(pointField, warngenLocation, distance, closestCoord, area, parentArea)
                points.append(cp)
    return points

def createClosestPoint(pointField, warngenLocation, distance, closestCoord, area, parentArea):
    cp = {}
    cp["name"] = str(warngenLocation.get(pointField))
    cp["point"] = GeometryFactory.createPoint((warngenLocation.get("lon"), warngenLocation.get("lat")))
    cp["population"] = warngenLocation.get("population", 0)
    cp["warngenlev"] = warngenLocation.get("warngenlev", 3)
    cp["distance"] = distance
    cp["area"] = area
    cp["parentArea"] = parentArea
    return cp

def find(searchFor, searchIn):
    found = None
    for i in range(0, len(searchIn)):
        check = searchIn[i]
        if searchFor.get("name") == check.get("name") and \
           searchFor.get("point").x == check.get("point").x and \
           searchFor.get("point").y == check.get("point").y:
            found = check
            break
    return found
