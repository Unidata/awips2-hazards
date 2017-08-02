import copy
import os
import sys
import json

import LocalizationInterface

class HazardServicesConfig :
    """
    Class for retrieving any Hazard Services configuration dataset stored in
    localization. It:
          - Reads Python or JSON localization files and performs incremental overrides on them
          - Provides an interface for the user to query the localization data, effectively allowing 
             the data to be filtered and the result set to be refined
          - Can be easily run from a Python command line interpreter for testing
          - Does not need to be modified when new configuration files and directories are
            added in localization
    @author: Bryon Lawrence
    @since: 12/17/2012
    """

    def __init__(self, configType, host="", configDir=None) :
        """
        @param configType: "alerts", "settings", "hazardTypes", 
                           "hazardCategories", "productGeneratorTable",
                           "hazardMetaData", "startUpConfig"
        @param host: host identifier of localization server
        """
        if isinstance(configType, unicode):
            configType = configType.encode()
        if configDir == None:
            self.__configDir = "HazardServices/"+configType
        else:
            self.__configDir = configDir
        self.__myLI = LocalizationInterface.LocalizationInterface(host)
        self.__extension = ".py"

    def getConfigFileList(self) :
        """
        Retrieve a list of configuration files for the 
        specified configuration type.
        @return: A list of localization files for the
                 configuration type
        """
        fileList = self.__myLI.listLocFiles(\
                       self.__configDir, "COMMON", "", "", "", self.__extension) or []
        baseConfigNameList = []
    
        for configFileName in fileList:
            baseName = os.path.splitext(configFileName)[0]
            baseConfigNameList.append(baseName)

        return baseConfigNameList

    def filterConfigData(self, allData, fields) :
        """
        Filters the configuration data based on the fields.
        @param allData: The configuration data dict
        @param fields: A list of keys to filter on.
        @return: A dict containing only the key/value pairs based on the
                 passed in fields
        """
        if fields == None or not isinstance(fields, list) or len(fields) == 0 :
            return allData
        filterData = {}
        for key in fields :
            if key in allData :
                filterData[key] = allData[key]
        return filterData

    def getConfigFileData(self, baseConfigFileName, fields=None, incrementalOverride=True,
                          incrementalOverrideImports=True) :
        """
        @param baseConfigFileName: the name of the file containing 
                                   the configuration data, without the extension
        @param fields: The fields to filter the retrieved data by.
        @param incrementalOverride: Flag indicating whether or not 
                                    to perform incremental override on 
                                    configuration files
        @param incrementalOverrideImports:  Flag indicating whether or not
                                            to perform incremental override
                                            on imports.

        @return: A Python object containing configuration data, filtered by
                 the fields, with incremental override applied.
        """
        if not isinstance(baseConfigFileName, str) and not isinstance(baseConfigFileName, unicode):
            return None

        locPath = self.__configDir+"/"+baseConfigFileName+self.__extension
        configData = self.__myLI.getLocData(locPath, "COMMON", 
                                            incrementalOverride=incrementalOverride,
                                            incrementalOverrideImports=incrementalOverrideImports)
        if configData is None :
            return configData

        return self.filterConfigData(configData, fields)
    
    def getConfigDataList(self, fileName=None, incrementalOverride=True, 
                          incrementalOverrideImports=True):
        """
        @param fileName: The name of the file to retrieve configuration
                         data for.
        @param incrementalOverride: Flag indicating whether or not 
                                    to perform incremental override on 
                                    configuration files
        @param incrementalOverrideImports:  Flag indicating whether or not
                                            to perform incremental override
                                            on imports.
        @return: A list of dicts representing the 
        configuration information in each 
        of the files for this configuration type.
        """
        retList = []
        
        for configID in self.getConfigFileList():
            
            if fileName is None:
                retList.append(self.getConfigFileData(configID, incrementalOverride=incrementalOverride, 
                                                      incrementalOverrideImports=incrementalOverrideImports))
            elif fileName == configID:
                retList.append(self.getConfigFileData(configID, incrementalOverride=incrementalOverride,
                                                      incrementalOverrideImports=incrementalOverrideImports))

        return retList

    def getConfigData(self, criteriaInput=None) :
        """
        @param criteriaInput: a filtering criteria of the following format:
              {'filter': {'key':'value', ...}
               'fields': ['field1', 'field2', 'field3']}
         
        @return: A list containing the filtered configuration data
        """
        incrementalOverride = True
        incrementalOverrideImports = True
        
        if isinstance(criteriaInput, str) or isinstance(criteriaInput, unicode):
            try:
                criteria = json.loads(criteriaInput)
            except :
                criteria = None
        else :
            criteria = copy.deepcopy(criteriaInput)
            
        if isinstance(criteria, dict) :
            filter = criteria.get("filter")
            if filter is not None and not isinstance(filter, dict) :
                filter = None
            fields = criteria.get("fields")
            if fields is not None and not isinstance(fields, list) :
                fields = None
            incrementalOverride = criteria.get("incrementalOverride")
            if incrementalOverride is None or not isinstance(incrementalOverride, bool):
                incrementalOverride = True
                
            incrementalOverrideImports = criteria.get("incrementalOverrideImports")
            if incrementalOverrideImports is None or not isinstance(incrementalOverrideImports, bool):
                incrementalOverrideImports = True
                
            if fields is not None or filter is not None :

                if filter is not None and "name" in filter:
                    configDataList = self.getConfigDataList(filter["name"], incrementalOverride=incrementalOverride,
                                                            incrementalOverrideImports=incrementalOverrideImports)
                    del filter["name"]
                else:
                    configDataList = self.getConfigDataList(incrementalOverride=incrementalOverride,
                                                            incrementalOverrideImports=incrementalOverrideImports)
                            
                retList = []
        
                for configData in configDataList :
                    if configData is None:
                        continue
                    ok = True
                    if filter is not None:
                        for onekey in filter :
                            ok = False
                            if not onekey in configData :
                                break
                            if filter[onekey] != configData[onekey] :
                                break
                            ok = True
                    if not ok :
                        continue
                    retList.append(self.filterConfigData(configData, fields))
                        
                #
                # If the criteria was able to narrow 
                # the result down to one settings, return the settings dict,
                # otherwise, return a list of matching settings dicts.
                #if len(retList) == 1:
                #    return retList[0]
                #else:
                #    return retList
                #return configDataList
            else:
                retList = self.getConfigDataList(incrementalOverride=incrementalOverride,
                                              incrementalOverrideImports=incrementalOverrideImports)
        else:
            retList = self.getConfigDataList(incrementalOverride=incrementalOverride,
                                          incrementalOverrideImports=incrementalOverrideImports)

        if retList is not None and len(retList) == 1:
            return retList[0]
        else:
            return retList

    def writeConfigData(self, fileName, configData, forSite=False) :
        """
        Writes configuration data to the localization file
        system.
        @param fileName: The name (without the extension) of the file to write. It is
                         assumed to be *.py
        @param configData: A Python dict or list containing the data to write to the
                           localization file. 
        @param forSite: Flag indicating whether to store as Site (True) or User (False)
        @return:  True or False based on success of writing to localization file system.
        """
        status = False

        if fileName is not None and configData is not None:
            if isinstance(fileName, unicode):
                fileName = fileName.encode()

            locPath = self.__configDir + "/" + fileName + self.__extension
            level = "Site" if forSite else "User"
            status = self.__myLI.putLocFile(configData, locPath, "COMMON", level, "")
        return status
