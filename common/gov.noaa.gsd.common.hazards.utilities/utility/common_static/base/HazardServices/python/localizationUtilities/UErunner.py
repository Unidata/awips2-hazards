# $Revision: 991 $  $Date: 2011-05-23 15:20:30 -0600 (Mon, 23 May 2011) $
#
# DESCRIPTION:
# This module incorporates the CLI libraries into a Python uEngine script
# runner class. Use this in a stand alone Python program to run a Python
# uEngine script within the program itself.
# The UErunner class normally does not need any arguments; the service and
# connection will determined from the CLI default configuration. They can be
# overridden by named arguments.
#
# The execute method runs the script. Supply either the script as a string
# or a filename to read the script from disk. Supply named arguments of
# script=<script> or scriptFile=<filename>.
#
# INSTALLATION:
# This module should be installed in $LOCALAPPS_LIB_python
#  (/localapps/runtime/lib/python)
#
# EXAMPLE:
# Simple example program:
#
#--------------------------------------------------------------------------
# import UErunner
# # Define the uEngine Python script:
# aScript="""import ObsRequest
# dataRequest = ObsRequest.ObsRequest("obs")
# dataRequest.addList("reportType", "METAR,SPECI")
# dataRequest.addConstraint("location.stationId", "KBUF,KROA","in")
# return dataRequest.execute()"""
#
# uer=UErunner()
# # uEngine always returns results in xml format
# xml=uer.execute(script=aScript)
# if uer.status == 0:
#     # Process the xml
#--------------------------------------------------------------------------
#
# MODIFICATION HISTORY
# yyyy-mm-dd     paul.jendrowski        initial release
# 2011-04-21     john.olsen             removed SiteConfig, as that file no
#                                       longer exists, and added import os. The
#                                       default connection string is now
#                                       formed with os.getenv()
#
# TO DO: add keyword substitution of the uEngine script
#

import lib.CommHandler as CH
import lib.Util as util
import conf.UEConfig as config
import sys
import os

class UErunner:
    def __init__(self, service=None, connection=None):
        """Initialize. If service and connection  are not supplied,
        they are determined by the installed CLI configuration. Normally,
        instantiate the class with no arguments. Only supply named arguments
        to override CLI defaults.
        """
        self._runner='python'
        if service == None:
            self._service=service=config.endpoint.get(self._runner)
        else:
            self._service=service
        if connection == None:
            self._connection=str(os.getenv("DEFAULT_HOST", "localhost") +
                                 ":" + os.getenv("DEFAULT_PORT", "9581"))
        else:
            self._connection=connection
        self.status = -1

    def execute(self,script=None,scriptFile=None):
        """This runs a uEngine script. Supply the script with the
        script=<script text> argument or use scriptFile=<filename> to
        read the script text from a file.
        Returns xml string.  The status of the uEngine script must be
        checked with class attribute status. 0 for success. Any other value
        there were problems.
        """
        # The script to run is either passed in or can be read from a file
        xml=""
        if script == None and scriptFile != None:
            script = self.readscript(scriptFile)

        if script != None:
            # submit the input to the server and obtain result
            ch = CH.CommHandler(self._connection, self._service)
            ch.process(script)

            # expect message 200, if not print error message and return error code
            if not ch.isGoodStatus():
                print "######### ERROR reportHTTPResponse"
                util.reportHTTPResponse(ch.formatResponse())
                self.status = 1
            else:
                # Pull the responses element out of the xml
                xml=ch.getContents()
                self.status = 0
        return xml

    def returnContents(self,script=None,scriptFile=None):
        xml = self.execute(script,scriptFile)
        if not isinstance(xml, str) and not isinstance(xml, unicode) :
            return None
        i = xml.find("<contents")
        if i<0 :
            return None
        j = xml.find(">",i+9)
        if j<0 :
            return None
        if xml[j-1]=="/" :
            return ""
        k = xml.find("</contents>",j)
        if k<0 :
            return None
        return str(xml[j+1:k])

    def readscript(self,scriptFile):
        """Simple method to read contents of a file. Returns contents of file,
        normally the text of a Python file. Returns None if problems with file.
        """
        script=None
        try:
            fp=open(scriptFile)
            script=fp.read()
            fp.close()
        except:
            # log any errors but don't abort
            print 'Error with reading script file=%s: %s, %s' % \
                        (scriptFile,sys.exc_info()[0], sys.exc_info()[1])
        return script
