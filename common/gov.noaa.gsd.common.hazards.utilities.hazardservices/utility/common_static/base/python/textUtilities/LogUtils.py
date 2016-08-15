'''
Logging utilities.
'''
import inspect, sys, os

def logMessage(*args):
    """Returns the current line number in our program."""
    s = ", ".join(str(x) for x in list(args))
    frame,fName,lineNo,function_name,lines,index = inspect.stack()[1]
    fName = os.path.basename(fName)
    print '\t*** [' + str(fName) + ' // Line ' + str(lineNo) + ']:  ' + s
    sys.stdout.flush()
    return


