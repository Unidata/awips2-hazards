
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

#
# A HazardEventSet, based on HazardEventSet
#
#
#     SOFTWARE HISTORY
#
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    03/04/13                      jsanchez       Initial Creation.
#
#
#

import JUtil
import datetime
import EventConverter

from HazardEvent import HazardEvent

from java.util import Date

class EventSet(JUtil.JavaWrapperClass):

    def __init__(self, wrappedObject):
        self.jobj = wrappedObject
        if wrappedObject is not None :
            converter = EventConverter.findConverter(wrappedObject)
            if converter is not None :
                self.events = EventConverter.convert(wrappedObject, converter)
            else :
                self.events = set()
            self.attributes = JUtil.javaMapToPyDict(wrappedObject.getAttributes())
        else :
            self.attributes = {}

    def add(self, hazardEvent):
        self.events.add(hazardEvent)

    def addAll(self, hazardEvents):
        for event in hazardEvents:
            self.events.add(event)

    def iterator(self):
        return self.events.iterator()

    def hasNext(self):
        return self.events.hasNext()

    def next(self):
        return self.events.next()

    def addAttribute(self, key, value):
        attributes[key] = value

    def getAttribute(self, key):
        return attributes[key]

    def getAttributes(self):
        return self.attributes

    def getEvents(self):
        return self.events

    def toJavaObj(self):
        self.jobj.addAll(JUtil.pyValToJavaObj(self.events))
        self.jobj.setAttributes(JUtil.pyDictToJavaMap(self.attributes))
        return self.jobj
