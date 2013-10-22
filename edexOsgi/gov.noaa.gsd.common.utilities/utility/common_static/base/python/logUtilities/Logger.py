##logging module
logger = None

import logging
import logging.handlers

#-------------------------------------------------------------------------
#setup for the logger, simply importing this module will set up the
#logging for the server.
#-------------------------------------------------------------------------
logging.addLevelName(10, "DEBUG")
logging.addLevelName(15, "VERBOSE")
logging.addLevelName(75, "INFO")

logger = logging.getLogger("logger")
logger.VERBOSE = 15
logger.DEBUG = 10
logger.INFO = 75

loggingLevel = logger.VERBOSE
loggingLevel = logger.DEBUG
loggingLevel = logger.INFO

logger.setLevel(loggingLevel)   #set to DEBUG to log practically everything

ch = logging.StreamHandler()
ch.setLevel(loggingLevel)

rfh = logging.handlers.RotatingFileHandler("status.log", 'a', 50000000, 9)
rfh.setLevel(loggingLevel)  #set to DEBUG to log practically everything

formatter = logging.Formatter("%(asctime)s - %(levelname)s - %(message)s")
rfh.setFormatter(formatter)
ch.setFormatter(formatter)

logger.addHandler(rfh)
logger.addHandler(ch)


#-------------------------------------------------------------------------
#this section is to simulate LogStream, but use the logging Python 
#mechanism.  The purpose is to avoid modifying existing code that uses
#lots of LogStream entries.   To use this as LogStream, you will need a
#"import Logger as LogStream" statement in each module.   You can also
#use this directly as Logger.   The LogStream commands accept multiple
#arguments, while the logger module accepts single arguments.
#-------------------------------------------------------------------------

# concatentate the arguments
def _concat(*args):
    fargs = args[0]   # variable args delivered as a tuple of len 1 
    s = ""
    for a in fargs:
        if len(s) == 0:
            s = str(a)
        else:
            s = s + " " + str(a)
    return s

def exc(*args):
    pass  # no traceback available

def ttyLogOn():
    pass  # no command to turn on tty

def logEvent(*args):
    logger.info(_concat(args))

def logVerbose(*args):
    logger.log(15, _concat(args))
    
def logDebug(*args):
    logger.debug(_concat(args))
    
def logProblem(*args):
    logger.warning(_concat(args))

def logBug(*args):
    logger.error(_concat(args))

def logUse(*args):
    logger.debug(_concat(args))

def logDiag(*args):
    logger.debug(_concat(args))

def logFatal(*args):
    logger.error(_concat(args))
