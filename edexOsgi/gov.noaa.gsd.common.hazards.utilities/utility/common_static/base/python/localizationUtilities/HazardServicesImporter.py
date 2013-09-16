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
        @return: A module representing the import. 
        """
        mod = sys.modules.get(full_name)
        
        if mod is None:
            mod = imp.new_module(full_name)
            mod.__loader__ = self
            commandString = full_name + " = " + json.dumps(self.data)
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
            #print "HazardServicesImporter: criteria: ", criteria 
            hazardMetaData = HazardServicesConfig.HazardServicesConfig("hazardMetaData")
            configData = hazardMetaData.getConfigData(criteria)
            return configData if (configData is not None and len(configData) > 0) else None
        else:
            #print "Skipped: ", module_name
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
            #print "HazardServicesImporter: critieria ", criteria 

            hazardCategories = HazardServicesConfig.HazardServicesConfig("hazardCategories")
            configData = hazardCategories.getConfigData(criteria)
            return configData if (configData is not None and len(configData) > 0) else None
        else:
            #print "Skipped: ", module_name
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