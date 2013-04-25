"""
Description: This class is deprecated.  Do not use it. It is being 
             replaced throughout Hazard Services with UFStatusHandler.py,
             a baseline python module for logging using UFStatus. This
             transition is expected to be completed by the beginning of
             PV2.

SOFTWARE HISTORY
Date         Ticket#    Engineer    Description
------------ ---------- ----------- --------------------------
Jan 11, 2013            Bryon.Lawrence      Initial creation

@author Bryon.Lawrence@noaa.gov
@version 1.0
"""
import traceback
from time import gmtime, strftime

class HazardServicesLogger():
    
    fileName = '/tmp/HazardServices.log'
    singleInstance = None
    
    def __init__(self):
        """
        Treat as a singleton.  Do not call this 
        initializer directly. Instead, call
        getInstance()
        """
        self._handlers = {}
        self._ufStatusAvailable = False
        
        try:
            from com.raytheon.uf.common.status import UFStatus #@UnresolvedImport
            from com.raytheon.uf.common.status import UFStatus_Priority as Priority #@UnresolvedImport
            self._ufStatus = UFStatus
            self._priority = Priority
            self._ufStatusAvailable = True            
        except:    
            timeString = strftime("%Y-%m-%d %H:%M:%S", gmtime())
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
        if HazardServicesLogger.singleInstance is None:
            HazardServicesLogger.singleInstance = HazardServicesLogger()
            
        return HazardServicesLogger.singleInstance
    
    def logMessage(self, message, status, moduleName="HazardServicesLogger", category="WORKSTATION", source="CAVE"):
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
        if moduleName is not None and isinstance(moduleName, str):
            message = "(" + moduleName + ") " + message
            
        status = status.lower()
            
        if self._ufStatusAvailable:
            if status == "fatal":
                importance = self._priority.CRITICAL
            elif status == "error":
                importance = self._priority.SIGNIFICANT
            elif status == "warning":
                importance = self._priority.PROBLEM
            elif status == "info":
                importance = self._priority.EVENTA
            elif status == "debug":
                importance = self._priority.VERBOSE
            else:
                importance = self._priority.SIGNIFICANT
                
            if category not in self._handlers:
                self._handlers[category] = self._ufStatus.getHandler(moduleName, category, source)
            
            self._handlers[category].handle(importance, message)
        else:
            #
            # Write to a file in /tmp
            timeString = strftime("%Y-%m-%d %H:%M:%S", gmtime())
            f = open(HazardServicesLogger.fileName, 'a')
            output = status + '|' + category + '|' + timeString + '\n'
            f.write(output)
            f.write(message + '\n')
            f.close()
