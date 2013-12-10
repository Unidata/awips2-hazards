'''
 * Pure Python implementation of the Java-backed HazardEvent.py 
 *  To be utilized for unit testing of Recommenders, Product Generators, local applications.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 31, 2013            thansen     Initial creation
 * 
 * </pre>
 * 
 * @author thansen
 * @version 1.0

'''

class HazardEvent:
    def __init__(self, siteID=None, eventID=None, uniqueID=None, state=None, phenomenon=None, 
                 significance=None, subtype=None, startTime=None, endTime=None, issueTime=None,
                 hazardMode=None, geometry=None, hazardAttributes={}):

        self.siteID = siteID
        self.eventID = eventID
        self.uniqueID = uniqueID
    
        '''
         * The state of the record at this point in time
         HazardState
        '''
        self.state = state
    
        '''
         * Phenomenon that is being recorded
         String
        '''
        self.phenomenon = phenomenon
    
        '''
         * Significance of the hazard
         String
        '''
        self.significance = significance
    
        '''
         * subtype of the hazard
         String
        '''
        self.subtype = subtype
    
        '''
        Date
        '''
        self.startTime = startTime
    
        '''
        Date
        '''
        self.endTime = endTime
    
        '''
        Date
        '''
        self.issueTime = issueTime
    
        '''
        ProductClass
        '''
        self.hazardMode = hazardMode
    
        '''
        Geometry
        '''
        self.geometry = geometry
    
        '''
        private Set<HazardAttribute> hazardAttributesSerializable;    
        '''
        self.hazardAttributes = hazardAttributes

    '''
     * @return the siteID
    '''    
    def getSiteID(self):
        return self.siteID;    
    '''
     * @param siteID
     *            the siteID to set
     '''    
    def setSiteID(self, siteID):
        self.siteID = siteID    
    '''
     * @return the eventID
    '''    
    def getEventID(self):
        return self.eventID
    
    '''
     * @param eventID
     *            the eventID to set
    '''
    def setEventID(self, eventId):
        self.eventID = eventId
    
    '''
     * @return the uniqueID
    '''
    def getUniqueID(self):
        return self.uniqueID
    
    '''
     * @param uniqueID
     *            the uniqueID to set
    '''
    def setUniqueID(self, uniqueID):
        self.uniqueID = uniqueID
    
    '''
     * @return the state
    '''    
    def getState(self):
        return self.state
    
    '''
     * @param state
     *            the state to set
    '''
    def setState(self, state):
        self.state = state
    
    '''
     * @return the phenomenon
     '''    
    def getPhenomenon(self):
        return self.phenomenon
    
    '''
     * @param phenomenon
     *            the phenomenon to set
     '''    
    def setPhenomenon(self, phenomenon):
        self.phenomenon = phenomenon
    

    '''
     * @return the significance
     '''
    
    def getSignificance(self):
        return self.significance
    

    '''
     * @param significance
     *            the significance to set
     '''    
    def setSignificance(self, significance):
        self.significance = significance
    
    '''
     * @return subtype
     '''    
    def getSubtype(self):
        return self.subtype
    
    '''
     * @param subtype
     '''    
    def setSubtype(self, subtype):
        self.subtype = subtype
                
    '''
     * @return hazardType e.g. FA.A or FF.W.Convective
     '''
    def getHazardType(self):
        hazardType = self.phenomenon + "." + self.significance
        if self.subtype is not None and len(self.subtype) > 0:
            hazardType += "." + self.subtype
        return hazardType    
    '''
     * @return the startTime
     '''    
    def getStartTime(self):
        return self.startTime
    
    '''
     * @param startTime
     *            the startTime to set
     '''    
    def setStartTime(self, startTime):
        self.startTime = startTime # new Date(startTime.getTime())
    
    '''
     * @return the endTime
     '''    
    def getEndTime(self):
        return self.endTime
    
    '''
     * @param endTime
     *            the endTime to set
     '''    
    def setEndTime(self, endTime):
        self.endTime = endTime # new Date(endTime.getTime())
    
    '''
     * @return the issueTime
     '''    
    def getIssueTime(self):
        return self.issueTime
    

    '''
     * @param issueTime
     *            the issueTime to set
     '''    
    def setIssueTime(self, issueTime):
        self.issueTime = issueTime # new Date(issueTime.getTime())
    
    '''
     * @return the hazardMode
     '''    
    def getHazardMode(self):
        return self.hazardMode
    
    '''
     * @param hazardType
     *            the hazardType to set
     '''    
    def setHazardMode(self, hazardMode):
        self.hazardMode = hazardMode
    
    '''
     * @return the geometry
    '''    
    def getGeometry(self):
        return self.geometry
    
    '''
     * @param geometry
     *            the geometry to set
     '''
    def setGeometry(self, geometry):
        self.geometry = geometry

    def getHazardAttributes(self):
        return self.hazardAttributes

    def setHazardAttributes(self, attributes):
        self.hazardAttributes = attributes
        
    def addHazardAttributes(self, attributes):
        for attr in attributes:
            self.hazardAttributes[attr] = attributes[attr];
                   
    def get(self, key, default=None):
        return self.hazardAttributes.get(key, default);

    def set(self, key, value):
        self.hazardAttributes[key] = value
        
    def addToList(self, key, value):
        '''
        The equivalent of hazardAttributes.setdefault(key, []).append(value)
        '''
        currentVal = self.get(key)
        if currentVal:
            newVal = currentVal.append(value)
        else:
            newVal = [value]
        self.set(key, newVal)

    def removeHazardAttribute(self, key): 
        self.hazardAttributes().pop(key, 0);
         
    def __repr__(self):
        outStr = ''
        outStr = outStr + "EventId : " + self.eventID + "\n"
        outStr = outStr + "Site : " + str(self.siteID) + "\n"
        outStr = outStr + "Phensig : " + self.phenomenon  + "." + self.significance  + "\n"
        outStr = outStr + "Start Time : " + str(self.startTime)+ "\n"
        outStr = outStr + "End Time : " +str(self.endTime) + "\n"
        outStr = outStr + "State : " +self.state + "\n"
        if self.hazardAttributes:
            outStr = outStr + "--Attributes--\n"
            for key in self.hazardAttributes:
                outStr = outStr + key+":" + str(self.hazardAttributes.get(key)) + "\n"
        return outStr
