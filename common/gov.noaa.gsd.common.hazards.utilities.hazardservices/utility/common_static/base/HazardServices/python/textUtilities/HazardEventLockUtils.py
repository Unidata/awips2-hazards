'''
    Utility interface for dealing with Hazard Event locks in Python.

 @since: Dec 2016
 @author: Raytheon Hazard Services Team
 
'''

from com.raytheon.uf.common.message import WsId
from com.raytheon.uf.common.serialization.comm import RequestRouter
from com.raytheon.uf.common.dataplugin.events.hazards.request import LockRequest
LockRequestType = LockRequest.LockRequestType
from com.raytheon.uf.common.dataplugin.events.locks import LockInfo
LockStatus = LockInfo.LockStatus

class HazardEventLockUtils(object):

    def __init__(self, practice):
        self.practice = practice

    def isLocked(self, eventID):
        '''
            Returns whether or not the event is locked. Locks held by
            this workstation are considered unlocked and False will be 
            returned.
            
            @param eventID: Event ID to check lock status of.
            @return: True if the event is locked, False otherwise. 
        '''
        resp = self.routeRequest(LockRequestType.STATUS, [eventID])
        if resp:
            lockInfoMap = resp.getLockInfoMap()
            lockInfo = lockInfoMap.get(eventID)
            if lockInfo:
                lockStatus = lockInfo.getLockStatus()
                if lockStatus == LockStatus.LOCKED_BY_OTHER:
                    return True
        return False

    def lock(self, eventID):
        '''
            Attempts to lock the specified event.
            
            @param eventID: Event ID to lock.
            @return: True if the event was successfully locked,
                     False otherwise. 
        '''
        resp = self.routeRequest(LockRequestType.LOCK, [eventID])
        if resp:
            return resp.isSuccess()
        return False

    def unlock(self, eventID):
        '''
            Attempts to unlock the specified event.
            
            @param eventID: Event ID to unlock.
            @return: True if the event was successfully unlocked,
                     False otherwise. 
        '''
        resp = self.routeRequest(LockRequestType.UNLOCK, [eventID])
        if resp:
            return resp.isSuccess()
        return False

    def breakLock(self, eventID):
        '''
            Attempts to break the lock of the specified event.
            
            @param eventID: Event ID to break the lock of.
            @return: True if the lock was successfully broken,
                     False otherwise. 
        '''
        resp = self.routeRequest(LockRequestType.BREAK, [eventID])
        if resp:
            return resp.isSuccess()
        return False

    def getLockedEvents(self):
        '''
            Returns a list of event IDs of all the events locked by other workstations.
            
            @return: List of locked event IDs.
        '''
        lockedEvents = []
        resp = self.routeRequest(LockRequestType.STATUS, [])
        if resp:
            lockInfoMap = resp.getLockInfoMap()
            for eventID in lockInfoMap:
                lockInfo = lockInfoMap.get(eventID)
                if lockInfo:
                    lockStatus = lockInfo.getLockStatus()
                    if lockStatus == LockStatus.LOCKED_BY_OTHER:
                        lockedEvents.append(eventID)
        return lockedEvents

    def routeRequest(self, type, eventIDs):
        '''
            Sends a LockRequest with the specified type and event IDs.
            
            @param type: LockRequestType of the request.
            @param eventIDs: List of event IDs that pertain to the request.
            @return: The response of the LockRequest.
        '''
        try:
            request = LockRequest(self.practice)
            request.setType(type)
            request.setWorkstationId(WsId(None, None, "CAVE"))
            request.setEventIdList(eventIDs)
            resp = RequestRouter.route(request)
            return resp
        except Exception as err:
            msg = "HazardEventLockUtils: Failed to route request."
            raise Exception(msg, err)
