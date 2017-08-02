from com.raytheon.uf.common.serialization.comm import RequestRouter
from com.raytheon.uf.common.hazards.productgen.request import SpatialQueryRequest
from GeometryHandler import jtsToShapely, shapelyToJTS
import JUtil
from Bridge import Bridge
JUtil.registerJavaToPython(jtsToShapely)
JUtil.registerPythonToJava(shapelyToJTS)
from com.vividsolutions.jts.io import WKBReader


'''
    Description: Utility class to retrieve points from the map database.
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Oct 22, 2014    3572    jsanchez    Initial creation
    Apr 07, 2015    6690    Robert.Blum Fixed bug and added parameter to allow 
                                        the query to be more flexible.
    Jun 17, 2015    7636    Robert.Blum Added maxResults.
    Aug 03, 2015    9920    Robert.Blum Fixed duplicate alias sql error.
    Mar 02, 2016    14032 Ben.Phillippe Reworked class to use a request object to allow more complex
                                        PostGIS geometric functions to be utilized
    Aug 08, 2016    21056   Robert.Blum Added retrievePathcastLocations.
    Oct 17, 2016    21699   Robert.Blum Updates for incremental overrides.
    @version 1.0
'''

def executeConfiguredQuery(geometryCollection,siteID,queryName):
    bridge = Bridge()
    spatialQueries = bridge.getSpatialQueries()
    query = spatialQueries[queryName]
    if query is None:
         raise Exception("No spatial query with name ", queryName," is configured")

    constraints=query.get('constraints',{})
    sortBy=query.get('sortBy',[])
    returnFields=query.get('returnFields',['name'])
    maxResults=query.get('maxResults',None)

    if 'cwa' not in constraints:
        constraints['cwa']=siteID
        
    return retrievePoints(geometryCollection, query['tableName'], constraints, sortBy, returnFields, maxResults)

def retrievePoints(geometryCollection, tablename, constraints=None, sortBy=None, returnFields=['name'],
                   maxResults=None):
    """
    Returns the list of location names.
    @param geometryCollection: the geometry that will be used to intersect the points
    @param tablename: the name of the mapdata table to query against
    @param constraints: a dictionary that has a table field name map to a value or a list of values
    @param sortBy: a list of table field names and sort directions to sort the results by
    @param returnFields: The fields returned by the query
    @param maxResults: the max points allowed to be returned
    @return: Returns the list of location names.
    """
    jGeom = JUtil.pyValToJavaObj(geometryCollection)
    
    request = SpatialQueryRequest()
    request.setReturnFields(JUtil.pyValToJavaObj(returnFields))
    request.setGeometry(jGeom)
    if maxResults is not None:
        request.setMaxResults(maxResults)
    request.setTableName(tablename)
    if sortBy is not None:
        request.setSortBy(JUtil.pylistToJavaStringList(sortBy))
    if constraints is not None:
        request.setConstraints(JUtil.pyDictToJavaMap(constraints))
    results = RequestRouter.route(request)
    if "the_geom" in returnFields:
        reader = WKBReader()
        for result in results:
            bytes = result.remove("the_geom")
            if bytes:
                result["geom"] = reader.read(bytes)
    return JUtil.javaObjToPyVal(results)
