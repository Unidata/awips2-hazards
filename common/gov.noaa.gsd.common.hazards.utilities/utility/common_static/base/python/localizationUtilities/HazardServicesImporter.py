import imp
import sys
import json
import HazardServicesConfig

class HazardServicesImporter(object):
    """
    Class which allows Hazard Services to intercept imports of Hazard Services
    configuration files from localization and perform incremental override on them.
    The result is passed back to the Python importer as a new module.
    @author: blawrenc
    @since: January 3, 2013
    """
    singleInstance = None
    
    def __init__(self, incrementalOverrideImports=True):
        self.incrementalOverrideImports=incrementalOverrideImports
        self.metaDataConfig = HazardServicesConfig.HazardServicesConfig("hazardMetaData")
        self.categoriesConfig = HazardServicesConfig.HazardServicesConfig("hazardCategories")
        self.metaDataFileList = self.metaDataConfig.getConfigFileList()
        self.categoriesFileList = self.categoriesConfig.getConfigFileList()
        
    def find_module(self, module_name, package_path):
        """
        Returns a custom import loader if the module name
        passed in is one of the Hazard Services 
        configuration files.
        @param module_name: The name of the module to import
        @param package_path: The path of the module
        @return: this importer (self) if this a HazardServices config import or
                 None otherwise.  
        """
        self.data = None
        
        if package_path is None:
            self.data = self.checkForHazardConfig(module_name)
            
        return self if self.data else None
    
    
    def load_module(self, full_name):
        """
        Loads a module. Creates a new module in the
        system modules if one by this name doesn't already
        exist.
        @param full_name: The full name of the module to create.
        @return: A module represesnting the import. 
        """
        mod = sys.modules.get(full_name)
        
        if mod is None:
            mod = imp.new_module(full_name)
            mod.__loader__ = self
            commandString = HazardServicesImporter.formatAsPythonInit(self.data, full_name)   
            exec commandString in mod.__dict__ 
            sys.modules[full_name] = mod
        
        return mod
    
    def checkMetaData (self, module_name):
        """
        Checks if the module name is one of the Hazard Services Meta 
        Data configuration files.
        @param module_name: The name of the module
        @return: A python object representing the configuration file, 
                with incremental overrides applied. 
        """
        if module_name in self.metaDataFileList: 
            criteria = {'filter':{'name':module_name}, 'incrementalOverride':self.incrementalOverrideImports,\
                        'incrementalOverrideImports':self.incrementalOverrideImports}
            hazardMetaData = HazardServicesConfig.HazardServicesConfig("hazardMetaData")
            configData = hazardMetaData.getConfigData(criteria)
            return configData if (configData is not None and len(configData) > 0) else None
        else:
            return None 
    
    def checkCategories (self, module_name):
        """
        Checks if the module name is one of the Hazard Services Categories
        configuration files.
        @param module_name: The name of the module
        @return: A python object representing the configuration file,
                 with incremental overrides applied. 
        """
        if module_name in self.categoriesFileList:
            criteria = {'filter':{'name':module_name}, 'incrementalOverride':self.incrementalOverrideImports,\
                        'incrementalOverrideImports':self.incrementalOverrideImports}

            hazardCategories = HazardServicesConfig.HazardServicesConfig("hazardCategories")
            configData = hazardCategories.getConfigData(criteria)
            return configData if (configData is not None and len(configData) > 0) else None
        else:
            return None
    
    def checkForHazardConfig (self, module_name):
        """
        Checks if the module is one of the Hazard Services
        configuration files.
        @param module_name: the name of the module
        @return: A python object representing the configuration file,
                 with incremental overrides applied.
        """
        return self.checkMetaData(module_name) or self.checkCategories(module_name)
    
    @staticmethod
    def getInstance(incrementalOverrideImports=True):
        """
        @return: a singleton instance of the HazardServicesImporter.
        """
        if HazardServicesImporter.singleInstance is None:
                HazardServicesImporter.singleInstance = HazardServicesImporter(incrementalOverrideImports=incrementalOverrideImports)
        return HazardServicesImporter.singleInstance
    
    @staticmethod
    def formatAsPythonInit(dataObject, variableName) :
        """
        This method formats an object into python that can initialize the
        indicated variable as this object.
        @param dataObject:  The data object to translate into python.
        @param variableName: The name of the variable to assign the translated
                             data object to. 
        """
        result = json.dumps(dataObject, indent=4)

        # JSON has different representations for the python symbols
        # None, True, and False.
        c1 = 0
        c2 = result.find('"',c1)
        while True :
            if c2<0 :
                c2 = len(result)
            if c2-c1>4 :
                c = result.find("null",c1,c2)
                while c>=0 :
                    result = result[:c]+"None"+result[c+4:]
                    c = result.find("null",c+4,c2)
                c = result.find("true",c1,c2)
                while c>=0 :
                    result = result[:c]+"True"+result[c+4:]
                    c = result.find("true",c+4,c2)
                c = result.find("false",c1,c2)
                while c>=0 :
                    result = result[:c]+"False"+result[c+5:]
                    c = result.find("false",c+5,c2)
            c1 = result.find('"',c2+1)
            if c1<0 :
                break
            c2 = result.find('"',c1+1)

        return variableName + " = \\\n" + result + "\n"
