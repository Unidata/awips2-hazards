# Utility classes for reading/writing/locking the VTEC active table

import sys, os, time, string, copy, types, stat, os.path, gzip, fcntl
import cPickle, glob, random
import Logger as LogStream

class VTECTableIO(object):

    def __init__(self, vtecRecordsFilename):
        self._vtecRecordsFilename = vtecRecordsFilename
        self._vtecRecordsLockFilename = vtecRecordsFilename + "lock"
        self._activeTableLockFD = None

#------------------------------------------------------------------
# Table lock/unlock read/write utility
#------------------------------------------------------------------

    def readVtecRecords(self):
        #reads the active table and returns the list of records
        
        if self._vtecRecordsFilename is None:
            raise Exception, "readVtecRecords without filename"
        return self._readVtecRecords(self._vtecRecordsFilename)

    def _readVtecRecords(self, filename, inputIsGZIP = 0):
        #reads the active table and returns the list of records
        
        #get the file and unpickle it
        if inputIsGZIP == 0:
            fd = open(filename, 'rb')
        else:
            fd = gzip.open(filename, 'rb')
            
        buf = fd.read()
        fd.close()
        records = cPickle.loads(buf)

        return records

    # writeVtecRecords is needed in VTECIngester ...
    def writeVtecRecords(self, table):
        #outputs the active table to disk.
        
        if self._vtecRecordsFilename is None:
            raise Exception, "writeVtecRecords without filename"

        buf = cPickle.dumps(table)
        fd = open(self._vtecRecordsFilename, 'w+')
        fd.write(buf)
        fd.close()
        try:
            os.chmod(self._vtecRecordsFilename, 0664)
        except:
            pass

    def lockAT(self, initialData=[]):
        LogStream.logDebug("LOCKing table")
        
        if self._vtecRecordsFilename is None:
            raise Exception, "lockAT without filename"

        t1 = time.time()
        self._activeTableLockFD = open(self._vtecRecordsLockFilename, 'a+')
        try:
            os.chmod(self._vtecRecordsLockFilename, 0664)
        except:
            pass
        fcntl.lockf(self._activeTableLockFD.fileno(), fcntl.LOCK_EX)

        opened = 1
        while opened:
           try:
              fcntl.lockf(self._activeTableLockFD.fileno(), fcntl.LOCK_EX|fcntl.LOCK_NB)
              fcntl.lockf(sofficeelf._activeTableLockFD.fileno(), fcntl.LOCK_EX)
              opened = 0
           except IOError:
              time.sleep(random.random())
              opened = 1


        t2 = time.time()
        # REFACTOR rename _activeTableFilename to _vtecRecordsFilename
        LogStream.logDebug("LOCK granted. Wait: ", "%.3f" % (t2-t1), "sec.,",
          " file=", os.path.basename(self._vtecRecordsFilename))
        self._lockTime = t2

        # if file doesn't exist or zero length, create it (first time)
        if not os.path.exists(self._vtecRecordsFilename) or \
          os.stat(self._vtecRecordsFilename)[stat.ST_SIZE] == 0:
            LogStream.logDebug("creating new active table")
            pickleData = cPickle.dumps(initialData)
            # REFACTOR rename _activeTableFilename to _vtecRecordsFilename
            fd = open(self._vtecRecordsFilename, 'w+')
            try:
                # REFACTOR rename _activeTableFilename to _vtecRecordsFilename
                os.chmod(self._vtecRecordsFilename, 0664)
            except:
                pass
            fd.write(pickleData)
            fd.close()

    def unlockAT(self):
        LogStream.logDebug("UNLOCKing table")
        if self._vtecRecordsFilename is None:
            raise Exception, "unlockAT without filename"

        fcntl.lockf(self._activeTableLockFD.fileno(), fcntl.LOCK_UN)
        t2 = time.time()
        LogStream.logDebug("LOCK duration: ", "%.3f" % (t2-self._lockTime),
          "sec., file=", os.path.basename(self._vtecRecordsFilename))
        self._activeTableLockFD.close()
