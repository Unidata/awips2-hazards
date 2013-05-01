"""
 Handles the input and output of Recommenders and Product Generators.
 
Python is used for this module to work efficiently with 
non-homogeneous data structures. 
 @since: March 2012
 @author: GSD Hazard Services Team
"""

import os, json, types
import logging, UFStatusHandler
from HazardConstants import *


class ToolHandler(object):
    def __init__(self, bridge, sessionManager):
        self.bridge = bridge
        self.sessionManager = sessionManager
        self.logger = logging.getLogger("ToolHandler")
        self.logger.addHandler(UFStatusHandler.UFStatusHandler(
            "gov.noaa.gsd.viz.hazards.sessionmanager", "ToolHandler", level=logging.INFO))
        self.logger.setLevel(logging.INFO)   
                
    def runTool(self, toolID, toolType = "Recommender", runData=None):
        """
        Runs a tool and stores the results.
        @param toolID: The name of the tool to run
        @param runData: JSON string containing any information required
                  by the tool to run.
                  
        @return: A JSON string containing the tool meta information and
                 result data.
        """

        # Add to runData...
        if runData is not None:
            runDict = json.loads(runData)
        else:
            runDict = {}

        # Add CAVE session information to the tool's rundata
        
        sessionDict = {}

        if self.sessionManager.selectedEventIDs is not None and len(self.sessionManager.selectedEventIDs) > 0:           
            selectedEventID = self.sessionManager.selectedEventIDs[0]
            selectedEventDicts = self.sessionManager.findSessionEventDicts([selectedEventID])
            
            if selectedEventDicts is not None and len(selectedEventDicts) > 0:    
                sessionDict["selectedEventDict"] = selectedEventDicts[0]
        
        sessionDict["selectedTime"] = self.sessionManager.selectedTime
        sessionDict["currentTime"] = self.sessionManager.currentTime
        
        framesInfo = {}
        framesInfo["frameCount"] = self.sessionManager.frameCount
        framesInfo["frameIndex"] = self.sessionManager.frameIndex
        framesInfo["frameTimeList"] = self.sessionManager.frameTimeList
        
        sessionDict["framesInfo"] = framesInfo


        sessionDict["staticSettings"]=self.sessionManager.staticSettings
        runDict["sessionDict"] = sessionDict
        runData = json.dumps(runDict)
        # This is important...We need to remove any potential events
        # from a previous tool's run.
        self.sessionManager.removeEvents(STATE, POTENTIAL)
        
        # Run Tool with required input
        result = self.bridge.runTool(toolID, toolType, runData)
        self.logger.debug("Tool result for running tool " + toolID)
        self.logger.debug("runData is " + runData)
        
        if result is not None and result is not "NONE":
            self.logger.debug("result is " + result)
        else:
            return None
           
        metaData = self.bridge.getMetaData(toolID, toolType)
        
        self.logger.debug( "MetaData: " + str(type(metaData)))
        if metaData is not None:
            if type(metaData) is types.StringType:
                metaDict = json.loads(metaData)
            else:
                metaDict = metaData
                 
            returnType = metaDict.get("returnType")
            outputFormat = metaDict.get("outputFormat")
            eventState = metaDict.get("eventState")
        
            if returnType == "EventDicts_List":
                resultDict = json.loads(result)
                resultDict = resultDict[0]
                
                toolDict = {"metaData" : metaDict, "resultData" : resultDict}
                if eventState == "Potential":
                    self.sessionManager.removeEvents(STATE, POTENTIAL)
                self.sessionManager.addEvents(resultDict)                                        
                return json.dumps(toolDict)
            
            elif returnType== "EventDicts" or returnType == "IEvent List":
                resultDict = json.loads(result)
                toolDict = {"metaData" : metaDict, "resultData" : resultDict}
                if eventState == "Potential":
                    self.sessionManager.removeEvents(STATE, POTENTIAL)
                self.sessionManager.addEvents(resultDict)                    
                return json.dumps(toolDict)                
                
            elif returnType == "TextProduct":
                return result
            elif returnType == "ModifiedEventDict":
                modifyInfoDict = json.loads(result)
                newEventDict = modifyInfoDict.get("eventDict")
                self.sessionManager.updateEventData(newEventDict)
            else:
                # GraphData
                resultDict = json.loads(result)
                toolDict = {"metaData" : metaDict, "resultData" : resultDict}
                return json.dumps(toolDict)
        
    def handleRecommenderResult(self, toolID, eventList):
        resultDictList = self.bridge.handleRecommenderResult(toolID, eventList)
        
        if resultDictList is None:
            return None
           
        metaDict = self.bridge.getMetaData(toolID, "Recommender")
        if metaDict is not None:
            returnType = metaDict.get("returnType")
            outputFormat = metaDict.get("outputFormat")
            eventState = metaDict.get("eventState")
        
            if returnType == "EventDicts_List":
                resultDict = resultDictList[0]
                
                toolDict = {"metaData" : metaDict, "resultData" : resultDict}
                if eventState == "Potential":
                    self.sessionManager.removeEvents(STATE, POTENTIAL)
                self.sessionManager.addEvents(resultDict)                                        
                return json.dumps(toolDict)
            
            elif returnType== "EventDicts" or returnType == "IEvent List":
                toolDict = {"metaData" : metaDict, "resultData" : resultDictList}
                if eventState == "Potential":
                    self.sessionManager.removeEvents(STATE, POTENTIAL)
                self.sessionManager.addEvents(resultDictList)                    
                return json.dumps(toolDict)                
                
            elif returnType == "TextProduct":
                return resultDictList
            elif returnType == "ModifiedEventDict":
                modifyInfoDict = resultDictList
                newEventDict = modifyInfoDict.get("eventDict")
                self.sessionManager.updateEventData(newEventDict)
            else:
                # GraphData
                toolDict = {"metaData" : metaDict, "resultData" : resultDictList}
                return json.dumps(toolDict)
    
    def getDialogInfo(self, toolID, toolType="Recommender", runData=None):
        """
        @param toolID: The tool to retrieve dialog info for.
        @param runData: Run data (generally session info) which 
                        will help the tool produce the dialog info.
        @return: JSON containing GUI building instructions or None
        """
        if runData is not None:
            runDict = json.loads(runData)
        else:
            runDict = {}
            
        # Add CAVE session information to the tool's rundata
        sessionDict = {}
        
        sessionDict["selectedTime"] = self.sessionManager.selectedTime
        sessionDict["currentTime"] = self.sessionManager.currentTime

        #
        # The FollowUpRecommender needs this information passed into its
        # getDialogInfo method so that it can determine which follow up
        # recommendations are appropriate for the follow-up dialog.        
        if self.sessionManager.selectedEventIDs is not None and len(self.sessionManager.selectedEventIDs) > 0:           
            selectedEventID = self.sessionManager.selectedEventIDs[0]
            selectedEventDicts = self.sessionManager.findSessionEventDicts([selectedEventID])
            
            if selectedEventDicts is not None and len(selectedEventDicts) > 0:    
                sessionDict["selectedEventDict"] = selectedEventDicts[0]

        sessionDict["staticSettings"]=self.sessionManager.staticSettings
        runDict["sessionDict"] = sessionDict
        runData = json.dumps(runDict)
        
        result = self.bridge.getDialogInfo(toolID, runData)
        return result

    def getSpatialInfo(self, toolID, toolType="Recommender", runData=None):
        """
        @param toolID: The tool to retrieve dialog info for.
        @param runData: Run data (generally session info) which 
        @return: JSON containing the what spatial information this
                 tool needs to run or None.
        """
        self.logger.debug( "In ModelDelegator: getSpatialInfo: ToolID: " + toolID)
        result = self.bridge.getSpatialInfo(toolID, toolType, runData)
        return result
    
    def getMetaData(self, toolID, toolType="Recommender", runData=None):
        """
        @param toolID: The tool to retrieve dialog info for.
        @param runData: Run data (generally session info) which 
                        will help the tool produce the dialog info.
        @return: JSON containing the meta data describing this tool's
                 services
        """
        result = self.bridge.getMetaData(toolID, toolType, runData)
        return result
    
    #  Product Generation   
    class HazardEventSet:
        '''
        There will be one HazardEventSet per product generator
        It contains the set of eventIDs, eventDicts, and other information needed
        for generating the products
        '''     
        def __init__(self, parent, productGenerator):
            self.parent = parent
            self.productGenerator = productGenerator
            self.eventIDsWithStatus = []
            self.formats = [LEGACY_FORMAT, XML_FORMAT]
            self.combinable = False
            self.dialogInfo = {}
                 
        def getProductGenerator(self):
            return self.productGenerator
        
        def getEventIDsWithStatus(self):
            return self.eventIDsWithStatus
        
        def formats(self):
            return self.formats
        
        def setIssueFlag(self, issueFlag):
            self.issueFlag = issueFlag
        
        def setFormats(self, formats):
            self.formats = formats
            
        def addEventIDsWithStatus(self, eventIDsWithStatus):
            # [(eventID, label, status)]
            self.eventIDsWithStatus += eventIDsWithStatus                        

        def setDialogInfo(self, dialogInfo):
            self.dialogInfo = dialogInfo

        def getDialogInfo(self):
            return self.dialogInfo
             
        def setCombinable(self, value):
            self.combinable = value
            
        def getCombinable(self):
            return self.combinable
        
        def getChosenEventIDs(self):
            eventIDs = []
            for eventID, label, status in self.eventIDsWithStatus:
                if status == "ON":
                    eventIDs.append(eventID)
            return eventIDs

        def getChosenEventDicts(self):
            eventIDs = self.getChosenEventIDs()
            eventDicts = self.parent.sessionManager.findSessionEventDicts(eventIDs)
            return eventDicts
            
        def generateProduct_info(self):
            '''
            Generate the information needed by the Product Generator
            '''
            eventDicts = self.getChosenEventDicts()
            for eventDict in eventDicts:
                if eventDict.get(FORECAST_POINT):
                    eventDict["geoType"] = POINT
                else:
                    eventDict["geoType"] = "area" 
            sessionDict = {
                           "testMode": self.parent.sessionManager.caveMode,
                           "experimentalMode": 0
                           }
            if self.dialogInfo is not None:
                valueDict = self.dialogInfo.get("valueDict")
            else:
                valueDict = {}
            hazardEventSet = {
                              "eventDicts":eventDicts,
                              "valueDict":valueDict,
                              "formats": self.formats,
                              "issueFlag": self.issueFlag,
                              "currentTime":self.parent.sessionManager.currentTime,
                              SITE_ID: self.parent.sessionManager.wfoSiteID,
                              BACKUP_SITE_ID: self.parent.sessionManager.backupSiteID, 
                              "sessionDict": sessionDict,                             
                              }
            return hazardEventSet  

        def fromStagedSet(self, stagedSet): 
            '''
            Set up the HazardEventSet from stagedSet information provided by the
            Product Staging dialog

            // MegaWidget fields and valueDict
           ' stagingInfo': {'fields': [{'choices': [{'identifier': u'242', 'label': u'242 FF.A'}, {'identifier': u'241', 'label': u'241 FF.A'}], 'fieldName': 'eventIDs', 'fieldType': 'CheckList', 'label': 'When issuing this hazard, there are other related hazards that could be included in the legacy product:'}], 
            'valueDict': {'eventIDs': [u'242']}}, 
            
            // Dialog Info fields and valueDict
            'dialogInfo': {u'fields': [{u'fieldName': u'overviewHeadline', u'fieldType': u'Text', u'label': u'Overview Headline'}, {u'fieldName': u'overview', u'fieldType': u'Text', u'label': u'Overview'}], u'valueDict': {u'overviewHeadline': u'Enter overview headline here.', u'overview': u'Enter overview here.'}}, 
            
            // Label for Tab
            'productGenerator': u'FFA_ProductGenerator'}
            ]}
            '''
            eventID_field = stagedSet.get("fields")[0]
            valueDict = stagedSet.get("valueDict")
            self.setDialogInfo(stagedSet.get("dialogInfo"))
            
            chosenEventIDs = valueDict.get("eventIDs")
            eventIDsWithStatus = []
            for choice in eventID_field.get("choices"):
                eventID = choice.get("identifier")
                if eventID in chosenEventIDs:
                    status = "ON"
                else:
                    status = "OFF"
                label = choice.get("displayString")
                eventIDsWithStatus.append((eventID, label, status))
            self.addEventIDsWithStatus(eventIDsWithStatus)
        
        def generateStaging_info(self): 
            '''
            Generate the information needed for the Product Staging Dialog
            which consists of a set of eventIDs plus optional dialog info 
            from the product generator.
            '''
            choices = []
            chosen = []
            for eventID, label, status in self.eventIDsWithStatus:
                choices.append({"identifier":eventID, "displayString":label})
                if status == "ON":
                    chosen.append(eventID)
            stagingFields = [
                           {
                            "fieldType":"CheckList",
                            "label":"When issuing this hazard, there are other related hazards that could be included in the legacy product:",
                            "fieldName": "eventIDs",
                            "choices":choices,
                            "lines": len(choices)
                            }
                           ]
            stagingValueDict = {"eventIDs":chosen}
            stagingInfo = {
                           "stagingInfo": {
                                           "fields": stagingFields,
                                           "valueDict": stagingValueDict,
                                           },
                           "dialogInfo":self.dialogInfo,
                           "productGenerator": self.productGenerator,
                           }
            self.parent.logger.debug( "Dialog info" + json.dumps(self.dialogInfo) +" "+ str(type(self.dialogInfo)))
            self.parent.logger.debug( "Staging info" + json.dumps(stagingInfo))
            return stagingInfo                                                          

    def convertHazardEventSets_toJson(self, hazardEventSets):
            sets = []
            for hazardEventSet in hazardEventSets:
                sets.append(hazardEventSet.generateStaging_info())
            return sets
        
    def convertHazardEventSets_fromJson(self, hazardEventSets_json, issueFlag):
        stagingInfo = json.loads(hazardEventSets_json)
        stagedSets = stagingInfo.get("hazardEventSets")
        generatedProducts = stagingInfo.get("generatedProducts", "")
            
        stagedEventSets = []
        for stagedSet in stagedSets:
            productGenerator = stagedSet.get("productGenerator")
            stagedEventSet = stagedSet.get("stagingInfo")
            newSet = self.HazardEventSet(self, productGenerator)
            newSet.fromStagedSet(stagedEventSet)
            newSet.setIssueFlag(issueFlag)
            stagedEventSets.append(newSet)
        return stagedEventSets, generatedProducts          
                       
    def createProductsFromEventIDs(self, issueFlag):    
        '''
        This method creates the HazardEventSets from the selected events
        If product staging information is needed, it will return the HazardEventSets information for the Product Staging dialog 
        Otherwise, it calls createProductFromHazardEventSets
        
        @ issueFlag -- if True -- issue the hazard set

        '''
        self.logger.debug("ToolHandler:createProductsFromEventIDs issueFlag "+str(issueFlag))    
        eventDicts = self.sessionManager.findSessionEventDicts(self.sessionManager.selectedEventIDs)      
        hazardEventSets = self.getHazardEventSets(issueFlag, eventDicts)  
        stagingDialogFlag = self.stagingDialogFlag(hazardEventSets) 
        
        if stagingDialogFlag:
            # Return information for Product Staging Dialog
            sets = self.convertHazardEventSets_toJson(hazardEventSets)
            stagingInfo = {"returnType":"stagingInfo", "hazardEventSets":sets}
            self.logger.debug("ToolHandler createProductsFromEventIDs stagingInfo" + json.dumps(stagingInfo))
            return json.dumps(stagingInfo)            
        else:
            return self.createProductsFromHazardEventSets(issueFlag, hazardEventSets)
    
    def createProductsFromHazardEventSets(self, issueFlag, hazardEventSets):
        '''
        Call the appropriate product generator for each hazardEventSet.
        If issueFlag is False (Preview), 
                return the resulting list of "previewXML"'s                
        If issueFlag is True (Issue), 
                generate and issue the XML and ASCII formats 
        '''        

        if type(hazardEventSets) is types.StringType:
            hazardEventSets, generatedProducts = self.convertHazardEventSets_fromJson(hazardEventSets, issueFlag)

        for hazardEventSet in hazardEventSets:
            self.logger.debug( "ToolHandler createProductsFromHazardEventSets hazardEventSet" + \
                              json.dumps(hazardEventSet.generateProduct_info()))

        products = []
        self.issueFlag = issueFlag
        self.numberOfProductGenerators = len(hazardEventSets)
        self.productsReceived = []
        self.productGeneratorsReceived = 0
        self.hazardEventSets = hazardEventSets
        runningAsynch = False
        for hazardEventSet in self.hazardEventSets:
            productGenerator = hazardEventSet.getProductGenerator()
            #self.logger.info("ToolHandler calling " + productGenerator +" "+ \
            #                   str(hazardEventSet.generateProduct_info()))
            runData = json.dumps(hazardEventSet.generateProduct_info())
            resultProducts = self.bridge.runTool(productGenerator, "ProductGenerator", runData)
            self.logger.debug( "resultProducts" + json.dumps(resultProducts))
            if resultProducts is None: 
                runningAsynch = True 
                continue       
            products += resultProducts
            
        if runningAsynch:
            return json.dumps({"returnType": "NONE"})

        if issueFlag == "True":
            self.issueFromHazardEventSets(hazardEventSets, products, ISSUED) 
            return json.dumps({"returnType": "NONE"})         
        else:
            hazardEventSets_json = self.convertHazardEventSets_toJson(hazardEventSets)
            generatedProducts = {"returnType":"generatedProducts", "generatedProducts":products, 
                                 "hazardEventSets":hazardEventSets_json}
            return json.dumps(generatedProducts)                        

    def handleProductGeneratorResult(self, toolID, generatedProducts): 
        '''
        Handle the generated products from an asynchronous run of a product generator
        Collect the results for the list of product generators run 
        When all are collected, issue or display them
        @param toolID -- name of product generator
        @param generatedProducts -- list of IGeneratedProduct Java object 
        ''' 
        if generatedProducts is None:
            return 
        generatedProducts = self.bridge.handleProductGeneratorResult(toolID, generatedProducts)
        self.logger.info( "ToolHandler generatedProducts converted" + json.dumps(generatedProducts, indent=4))
        self.productsReceived += generatedProducts
        self.productGeneratorsReceived += 1
        
        if not self.productGeneratorsReceived == self.numberOfProductGenerators:
            return
        
        if self.issueFlag == "True":
            self.issueFromHazardEventSets(self.hazardEventSets, self.productsReceived, ISSUED) 
            return json.dumps({"returnType": None})        
        else:
            products = []
            
            for product in self.productsReceived:
                entries = product.get("entries")
                productID = product.get("productID")
                products.append({"productID":productID, LEGACY_FORMAT: entries.get(LEGACY_FORMAT)[0]})
            hazardEventSets_json = self.convertHazardEventSets_toJson(self.hazardEventSets)
            generatedProducts = {"returnType":"generatedProducts", 
                                 "generatedProducts":products, 
                                 "hazardEventSets":hazardEventSets_json}

            return json.dumps(generatedProducts)

    def getHazardEventSets(self, issueFlag, eventDicts):
        productGeneratorTable = self.sessionManager.productGeneratorTable           
        productGenerators = {}
        combinableFlags = {}
        productStates = [PENDING, PROPOSED, ISSUED, ENDED]
        # Determine the set of product generators and associated eventIDs
        #   { "FFA_ProductGenerator": [(eventID, label, status)]
        self.logger.debug( "ToolHandler getHazardEventSets " + json.dumps(eventDicts))
        for eventDict in eventDicts:
            if eventDict.get(STATE) not in productStates:
                continue
            eventID = eventDict.get(EVENT_ID)
            hazardKey = eventDict.get(HAZARD_TYPE) 
            for productKey in productGeneratorTable:
                allowedHazards = productGeneratorTable[productKey].get("allowedHazards")
                for hazardType, category in allowedHazards:
                    if hazardKey == hazardType:
                        productGenerators.setdefault(productKey, []).append((eventID, eventID+" "+hazardType, "ON"))
                        # If any of the hazard types for this product are "combinableSegments", then 
                        # set the flag for the product 
                        if self.sessionManager.hazardTypes.get(hazardKey).get("combinableSegments") == True:
                            combinableFlags[productKey] = allowedHazards 
                                                    
        # Add in related hazards if there are combinableSegments 
        for productKey in combinableFlags:
            allowedHazards = combinableFlags.get(productKey, None)
            if allowedHazards is not None:
                allowedTypes = [hType for hType, category in allowedHazards]
                # Add in from sessionManager eventDicts
                for eventDict in self.sessionManager.eventDicts:
                    if eventDict.get(STATE) in productStates:
                        hType = eventDict.get(HAZARD_TYPE)
                        eventID = eventDict.get(EVENT_ID)
                        if hType in allowedTypes and eventID not in self.sessionManager.selectedEventIDs:
                            productGenerators.setdefault(productKey, []).append((eventID, eventID+" "+hType, "OFF"))
                # TO DO:  Add in from Hazard Database -- will need to bring into the session if not there already                  
                             
        # Create the hazardEventSets
        hazardEventSets = []
        for productKey in productGenerators:
            hazardEventSet = self.HazardEventSet(self, productKey)
            hazardEventSet.addEventIDsWithStatus(productGenerators[productKey])
            hazardEventSet.setIssueFlag(issueFlag)
            if issueFlag:
                hazardEventSet.setFormats([XML_FORMAT, LEGACY_FORMAT])
            else:
                hazardEventSet.setFormat([LEGACY_FORMAT])
            if productGenerators.get(productKey) is not None:
                hazardEventSet.setCombinable(True)
            hazardEventSets.append(hazardEventSet)
            dialogInfo = {}
            hazardEventSet.setDialogInfo(dialogInfo)
        return hazardEventSets    

    def stagingDialogFlag(self, hazardEventSets):
        '''
        Determine if a Staging Dialog is necessary
        Conditions: We need a staging dialog if any of the product generators:
          Need user input (getDialogInfo)  OR 
          Are working with "combinableSegments" hazards AND
             Have an eventID that is not selected  AND
             That eventID is in a state of PENDING or PROPOSED
               (Once ISSUED, the user cannot chose to remove them from the product
             (Alternative: if there is more than one eventID....)           
        ''' 
        for hazardEventSet in hazardEventSets:
            if hazardEventSet.getCombinable():
                idsWithStatus = hazardEventSet.getEventIDsWithStatus()
                if len(idsWithStatus) > 1:
                    for eventID, label, status in idsWithStatus:
                        if eventID not in self.sessionManager.selectedEventIDs:
                            eventDict = self.sessionManager.findSessionEventDicts([eventID])[0]
                            if eventDict.get(STATE) != ISSUED:
                                return True
            if hazardEventSet.getDialogInfo() != {}:
                return True 
        return False 
    
    def issueFromHazardEventSets(self, hazardEventSets, products, state): 
        '''
        For all the event IDs in the hazardEventSets, change the state and store in db.
        Production Version 2: Transmit the products
                              SOMEHOW save the products with associated eventIDs
        '''
        for hazardEventSet in hazardEventSets:
            eventIDs = hazardEventSet.getChosenEventIDs()
            self.sessionManager.changeState(eventIDs, state)
                
    def flush(self):
        """ Flush the print buffer """
        os.sys.__stdout__.flush()
        
