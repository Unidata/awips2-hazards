"""
Description: This class is deprecated.  Do not use it. It is being 
             replaced throughout Hazard Services with UFStatusHandler.py,
             a baseline python module for logging using UFStatus. This
             transition is expected to be completed by the beginning of
             PV2.

Now that this logs through alertviz if enabled, there is no absolute reason
to deprecate this class.  In fact, this is now the preferred means of logging
for python modules that may run in standalone applications.

SOFTWARE HISTORY
Date         Ticket#    Engineer    Description
------------ ---------- ----------- --------------------------
Jan 11, 2013            Bryon.Lawrence      Initial creation
Apr 18, 2013            Jim Ramer           Enabled formal alertviz logging.

@author Bryon.Lawrence@noaa.gov
@version 1.0
"""
import traceback
import sys
import os
from time import gmtime, strftime

try :
    import logging, UFStatusHandler #@UnresolvedImport
except :
    import logging

class HazardServicesLogger():
    
    fileName = '/tmp/HazardServices.log'
    instanceFactory = {}
    
    def __init__(self, package, module):
        """
        Treat as a singleton.  Do not call this 
        initializer directly. Instead, call
        getInstance()
        """

        self._ufStatusAvailable = False
        self._module = module
        self._package = package
        
        try:
            self._myhandler = UFStatusHandler.UFStatusHandler( \
                 package, module, level=logging.INFO)
            self._logger = logging.getLogger(module)
            self._logger.addHandler(self._myhandler)
            self._logger.setLevel(logging.INFO)
            self._ufStatusAvailable = True
        except:    
            timeString = strftime("%Y-%m-%d %H:%M:%S", gmtime())
            if len(HazardServicesLogger.instanceFactory)==0 :
                f = open(HazardServicesLogger.fileName, 'a')
                f.write(timeString + '\n')
                traceback.print_exc(file=f)
                f.close()

    @staticmethod            
    def getInstance():
        """
        This is a static method. It does not take 
        self as an argument.
        @return: a singleton instance of the 
                 HazardServicesLogger
        """

        package = "unknown.package"
        module = "UnknownModule"
        try :
            tbdata = traceback.format_stack(limit=2)
            updata = tbdata[0].split()
            pathparts = updata[1].split('"')[1].split('/')
            m = len(pathparts)-1
            if m>=0 :
                module = pathparts[m]
                if module[-3:]==".py" :
                    module = module[:-3]
            if m==0 :
                pathparts = os.environ["PWD"].split('/')
                m = len(pathparts)-1
            while m>0 :
                m = m-1
                if pathparts[m].find(".")>0 :
                    package = pathparts[m]
                    break
                if pathparts[m]=="utility" :
                    package = "gov.noaa.gsd.common.utilities"
        except :
            pass

        thisInstance = HazardServicesLogger.instanceFactory.get(package+module)
        if thisInstance != None :
            return thisInstance

        thisInstance = HazardServicesLogger(package, module)
        HazardServicesLogger.instanceFactory[package+module] = thisInstance

        return thisInstance
    
    def logMessage(self, message, status, moduleName="", \
                   category="WORKSTATION", source="CAVE"):
        """
        Logs a message to the AWIPS II UFStatus utilities
        if available. Otherwise, logs to /tmp/HazardServices.log.
        @param message: The message to log 
        @param status: The level of importance:  Fatal, Error,
                                                 Warning, Info,
                                                 Debug
        @param moduleName: The optional name of the module this error
                           occurred within, default None 
        @param category: the log category, default WORKSTATION
        @param source: the log source, default CAVE
        """

        moduleNow = self._module
        if isinstance(moduleName, str) or isinstance(moduleName, unicode) :
            if len(moduleName)>0 :
                moduleNow = str(moduleName)
        message = "(" + moduleNow + ") " + message
            
        status = status.lower()
            
        if self._ufStatusAvailable:
            message = category + '|' + message
            if status == "fatal":
                self._logger.critical(message)
            elif status == "error":
                self._logger.error(message)
            elif status == "warning":
                self._logger.warning(message)
            elif status == "info":
                self._logger.info(message)
            elif status == "debug":
                self._logger.debug(message)
            else:
                self._logger.info(message)
        else:
            #
            # Write to a file in /tmp
            timeString = strftime("%Y-%m-%d %H:%M:%S", gmtime())
            f = open(HazardServicesLogger.fileName, 'a')
            output = status + '|' + category + '|' + timeString + '\n'
            f.write(output)
            f.write(message + '\n')
            f.close()
