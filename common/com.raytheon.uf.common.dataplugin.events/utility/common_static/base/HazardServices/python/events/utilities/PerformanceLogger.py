# #
# This software was developed and / or modified by Raytheon Company,
# pursuant to Contract DG133W-05-CQ-1067 with the US Government.
# 
# U.S. EXPORT CONTROLLED TECHNICAL DATA
# This software product contains export-restricted data whose
# export/transfer/disclosure is restricted by U.S. law. Dissemination
# to non-U.S. persons whether in the United States or abroad requires
# an export license or other authorization.
# 
# Contractor Name:        Raytheon Company
# Contractor Address:     6825 Pine Street, Suite 340
#                         Mail Stop B8
#                         Omaha, NE 68106
#                         402.291.0100
# 
# See the AWIPS II Master Rights File ("Master Rights File.pdf") for
# further licensing information.
# #

"""
    Python Performance Logger
    
    Provides a way to log the performance of specific Python methods. Override files
    of Hazard Services Python localization files can be created to add Performance
    Logging on the fly. See below for the needed steps to setup.
    
    Usage:
    Add the following import:
    from PerformanceLogger import perfLog
    
    Add the following annotation to methods that you want to log:
    @perfLog
    
    Output:
    Log File located at ~/pythonLogs/performanceYYYYMMDD.log
    
    @author Robert.Blum
"""
from functools import wraps
from time import time, gmtime, strftime
import os, errno

# Log file created in home directory
PERFORMANCE_LOG_FILE = os.path.expanduser('~') + "/pythonLogs/performance"

def perfLog(f):
    @wraps(f)
    def wrap(*args, **kw):
        # Run the function and get the two timing values.
        ts = time()
        result = f(*args, **kw)
        te = time()

        currentTime = gmtime()
        dateString = strftime("%Y%m%d", currentTime)
        timeString = strftime("%H:%M:%S", currentTime)
        logName = PERFORMANCE_LOG_FILE + dateString + ".log"
        # See if the log file/directory exists
        if not os.path.exists(os.path.dirname(logName)):
            try:
                os.makedirs(os.path.dirname(logName))
            except OSError as exc:
                if exc.errno != errno.EEXIST:
                    raise
        # Open the file and write to it
        file = open(logName, 'a')
        output = 'At ' + timeString + ' took %2.4f sec to run: func:%r args:[%r, %r]\n\n' % \
          (te-ts, f.__name__, args, kw)
        file.write(output)
        file.close()
        return result
    return wrap
