"""
Adapter to the recommender framework

Created on Mar 20, 2013
@author: blawrenc

 Jul 15, 2013     585    Chris.Golden Added passing of event bus
                                      to job listeners, since event
                                      bus is no longer a singleton.

"""
import json, traceback
import os

#
# These imports rely on Java object. We want them to fail 
# gracefully to support our python unit tests.
try:

    from com.raytheon.uf.viz.recommenders import CAVERecommenderEngine
    from com.raytheon.uf.common.hazards.productgen import ProductGeneration

    import HazardEventJSONAdapter as Adapter
    from com.raytheon.uf.common.dataplugin.events import EventSet
    from gov.noaa.gsd.viz.hazards.pythonjoblistener import HazardServicesRecommenderJobListener
    from gov.noaa.gsd.viz.hazards.pythonjoblistener import HazardServicesGeneratorJobListener
    from gov.noaa.gsd.viz.hazards.events import HazardServicesEvent
    import JUtil
    from java.util import HashSet
    from java.lang import String
    import jep
except:
    tbData = traceback.format_exc()
    print tbData


class ScriptAdapter:
    
    def __init__(self, toolType):
        
        self.toolType = toolType
        
        try:

            if toolType == "Recommender":
                self._scriptManager = CAVERecommenderEngine()
            else:
                self._scriptManager = ProductGeneration()

        except:
            pass
        
    def getScriptSpatialInfo(self, scriptID, runDataDict=None):
        """
        Return the definition of any spatial info that the script
        needs to run.
        @param scriptID:  The name of the script
        @param runDataDict: JSON run data dictionary the tool may need
        @return: A dictionary defining spatial info
                 required to run this script.
        """
        result = None
        scriptSpatialInfo = self._scriptManager.getSpatialInfo(scriptID)
        
        if scriptSpatialInfo is not None and not scriptSpatialInfo.isEmpty() :
            result = JUtil.javaMapToPyDict(scriptSpatialInfo)
            
        return result
        
    def getScriptDialogInfo(self, scriptID, runDataDict=None):
        """
        Return the megawidget definition of a dialog required
        to gather user input to run this script.
        @param scriptID: The name of the script
        @param runDataDict: JSON run data dictionary the script may need.
        @return: A dictionary a megawidget user interface
                 which will gather user input required to 
                 run this script. 
        """
        result = None
        scriptDialogInfo = self._scriptManager.getDialogInfo(str(scriptID))
        
        if scriptDialogInfo is not None and not scriptDialogInfo.isEmpty():
            result = JUtil.javaMapToPyDict(scriptDialogInfo)
            
        return result
    
    def getScriptMetaData(self, scriptID, runDataDict=None):
        """
        Return meta data describing a script.
        @param scriptID: The name of the script
        @param runDataDict: JSON run data dictionary the script may need.
        @return:  A dictionary containing meta information
                  about this script.
        """
        result = None
        scriptMetadata = None
        
        scriptID = str(scriptID)
        
        if self.toolType == "Recommender":
            scriptMetadata = self._scriptManager.getScriptMetadata(scriptID)
        else:
            scriptMetadata = self._scriptManager.getMetadata(scriptID)
        
        if scriptMetadata is not None:
            result = JUtil.javaMapToPyDict(scriptMetadata)
            
        return result
    
    def executeRecommenderScript(self, scriptID, listener, runDataDict=None):
        """
        Runs the script. This is done asynchronously.
        So, no result is returned directly from this method.
        
        @param scriptID: The name of the script.
        @param listener: Listener to be notified of when a script completes.
                         Must be a Java object which implements
                         IPythonJobListener<List<IEvent>> 
        @param runDataDict: JSON data dictionary which the script may need to
                            run.
        @return: No return  
        """
        runDict = json.loads(runDataDict)

        # Convert all unicode characters to strings
        # Bad things happen if unicode characters are
        # passed on to the frameworks.
        runDict = self.convert(runDict)
        
        eventSet = self.buildHashSet(runDict)
        
        sessionDict = runDict['sessionDict']
        sessionJavaMap = JUtil.pyDictToJavaMap(sessionDict)
        hazardEventSet = EventSet(eventSet, sessionJavaMap)
        
        if 'spatialInfo' in runDict:
            spatialDict = runDict['spatialInfo']
            spatialJavaMap = JUtil.pyDictToJavaMap(spatialDict)
        else:
            spatialJavaMap = JUtil.pyDictToJavaMap({})

        if 'dialogInfo' in runDict:
            dialogDict = runDict['dialogInfo']
            dialogJavaMap = JUtil.pyDictToJavaMap(dialogDict)
        else:
            dialogJavaMap = JUtil.pyDictToJavaMap({})
                   
        self._scriptManager.runExecuteRecommender(scriptID,
                                                  hazardEventSet,
                                                  spatialJavaMap,
                                                  dialogJavaMap,
                                                  listener)

    def executeProductGeneratorScript(self, scriptID, listener, runDict):
        # Convert Python runDict version of HazardEventSet
        # to Java version
        runDict = json.loads(runDict)
        runDict = self.convert(runDict)
        eventSet = self.buildHashSet(runDict)            
        # Take out eventDicts from runDict to produce metaDict
        metaDict = {}
        for key in runDict:
            if key != 'eventDicts':
                metaDict[key] = runDict[key]
        formats = runDict.get("formats")
        metaDict = JUtil.pyDictToJavaMap(metaDict)
        hazardEventSet = EventSet(eventSet, metaDict)
        
        #This might be needed if the arguments to generate change for 'formats'
        #from String[] to List<String>  
        #javaFormats = JUtil.pylistToJavaStringList(formats)
        javaFormatsArray = jep.jarray(len(formats), jep.JSTRING_ID)
        
        i = 0
        
        for format in formats:
           javaFormatsArray[i] = format
           i = i + 1 
        
        self.flush()
        self._scriptManager.generate(str(scriptID), hazardEventSet, javaFormatsArray, listener)

    def convert(self, input):
        """
        Routine which recursively changes all unicode strings
        to python string strings in the given Python object.
        @param input: The python object to convert unicode strings
                      to python strings in.
        @return: The input python object with all unicode strings
                 converted to python strings.
        """
        if isinstance(input, dict):
            return {self.convert(key): self.convert(value) for key, value in input.iteritems()}
        elif isinstance(input, list):
            return [self.convert(element) for element in input]
        elif isinstance(input, unicode):
            return input.encode('utf-8')
        return input
        
    def buildHashSet(self, runDict) :
        eventSet = HashSet()
        if 'eventDicts' in runDict:
            #
            # create list of event sets
            eventDicts = runDict.get('eventDicts')

            for eventDict in eventDicts:
                javaMap = JUtil.pyDictToJavaMap(eventDict)
                javaEvent = HazardServicesEvent.buildBaseHazardEventFromMap(javaMap)
                eventSet.add(javaEvent)
        return eventSet
        
    def buildRecommenderJobListener(self, eventBus, toolID):
        """
        Constructs a python job listener for the specified
        tool name.
        @param eventBus: Event bus to be used
        @param toolID: The name of the tool
        @return The Python Job listener 
        """       
        return HazardServicesRecommenderJobListener(eventBus, toolID)

    def buildGeneratorJobListener(self, eventBus, toolID):
        """
        Constructs a python job listener for the specified
        tool name.
        @param eventBus: Event bus to be used
        @param toolID: The name of the tool
        @return The Python Job listener 
        """       
        toolID = str(toolID)
        return HazardServicesGeneratorJobListener(eventBus, toolID)
         
    def flush(self):
        """ Flush the print buffer """
        os.sys.__stdout__.flush()
