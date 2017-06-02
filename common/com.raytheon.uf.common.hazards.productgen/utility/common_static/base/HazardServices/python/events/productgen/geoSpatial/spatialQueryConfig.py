'''
    Description: Configuration file for configuring spatial queries.
    The queries configured here are executed using the SpatialQuery.py file.
    
    The key to the dictionary is the query name.
    The values of the dictionary are the parameters used by SpatialQuery.py
    These parameters include:
        tablename: The table to execute the spatial query on
        locationField: The field to be returned from the query
        constraints(optional): This optional entry defines any additional parameters
                               to be used in the query as key/value pairs.  The key
                               is the column name and the value can be a single value
                               or a list of values
        sortBy(optional): The field or fields to sort the results by.  This is a list
                          of fields and sort directions (asc or desc)
        maxResults(optional): The maximum number of results returned from the query

    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Mar 02, 2016 14032    Ben.Phillippe Initial creation
    Jul 21, 2016 19216    Kevin.Bisanz  Changed LocationsAffected query to
                                        include warngenlev 3 in response to
                                        Focal Point direction indicated in a
                                        note on issue 19098 (related to this
                                        ticket).
    Aug 08, 2016 21056    Robert.Blum   Added configs for pathcast.
    
'''
SpatialQueries = {
                  'LocationsAffected': {
                                        'tableName': 'warngenloc',
                                        'returnFields':['name'],
                                        'constraints':{'warngenlev' : [1, 2, 3],
                                                       'landwater' :['L', 'LW', 'LC']},
                                        'sortBy':['warngenlev', 'asc',
                                                  'population', 'desc',
                                                  'distance', 'asc'],
                                        'maxResults':20
                                        },
                  'ListOfDrainages': {
                                      'tableName': 'ffmp_basins',
                                      'returnFields':['streamname'],
                                      'constraints':{},
                                      'sortBy':['streamname', 'asc'],
                                      },
                  'PathcastOtherPoints': {
                                          'tableName': 'warngenloc',
                                          'returnFields': ['name'],
                                          'constraints': {"warngenlev": [3,4],
                                                          "landwater": ["L", "LW", "LC"]},
                                          'sortBy':['distance', 'asc', 'warngenloc', 'asc'],
                                          'maxResults':10
                                          },
                  "PathcastPoints" : {
                                      "tableName" : "warngenloc",
                                      "returnFields" : ["name", "state", "the_geom", "lat", "lon", "population", "warngenlev"],
                                      "constraints" : {"warngenlev": [1,2],
                                                       "landwater" : ["L", ",LW", "LC"]},
                                      "sortBy":["warngenlev", "asc", "population", "desc", "distance", "asc"],
                                      },
                  "PathcastAreas" : {
                                     "tableName" : "county",
                                     "returnFields" : ["countyName", "state", "the_geom"],
                                     },
                  }
