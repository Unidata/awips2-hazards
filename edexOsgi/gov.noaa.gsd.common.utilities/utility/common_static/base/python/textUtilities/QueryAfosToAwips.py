"""
Description: Provides an interface to the contents of the
afos_to_awips table in the fxatext database.

SOFTWARE HISTORY
Date         Ticket#    Engineer    Description
------------ ---------- ----------- --------------------------
April 29, 2013            James Ramer      Initial creation
May 6, 2013               Bryon Lawrence   Removed test for 
                                           less than 9 character
                                           product id. Sometimes
                                           these can be 8.
May 21, 2013              Bryon Lawrence   Refactored to use
                                           Python Thrift Client
                                           and GetPartialAfosIdRequest
                                           dstype      

May 22, 2013              Bryon Lawrence   Refactored to use
                                           RequestRouter instead
                                           of ThriftClient.                                           
                                                                                
@author James.E.Ramer@noaa.gov
@version 1.0
"""
from com.raytheon.uf.common.serialization.comm import RequestRouter
from com.raytheon.uf.common.dataplugin.text.request import GetPartialAfosIdRequest

class QueryAfosToAwips() :

    def __init__(self, productID, siteID):
        """
        Builds an instance of QueryAfosToAwips
        using the provided product id and site
        id. Makes a query to the afos_to_awips
        table in the fxatext db using 
        request router.  Processes and stores the
        results in member variables.
        
        @param productID: The product id, e.g. WSW
        @param siteID:  The 3-letter site id, e.g. OAX
        
        @raise Exception: An error was encountered reading
                          from the afos_to_awips table for
                          the given productID and siteID. 
                          This is a fatal circumstance. 
                          A product should not be 
                          generated unless it has valid
                          product information in its header.  
        """
        #
        # Build the request to read
        # information from the afos_to_awips
        # table      
        request = GetPartialAfosIdRequest()
        request.setNnn(productID)
        request.setXxx(siteID)
        requestRouter = RequestRouter
        
        try:
            resp = requestRouter.route(request)
            idList = resp.getIdList()
            
            if idList.size() > 0 :
                afosToAwips = idList.get(0)
                id = afosToAwips.getId()
                cccnnnxxx = id.getAfosid()
                self.ccc = cccnnnxxx[:3]
                self.xxx = cccnnnxxx[6:]
                self.nnn = cccnnnxxx[3:6]
                self.wmoProd = id.getWmottaaii()
                self.wmoSite = id.getWmocccc()
                self.pil = cccnnnxxx[3:]
                self.awipsWANPil = self.wmoSite + self.pil
                self.textdbPil = cccnnnxxx
            else:
                raise Exception("QueryAfosToAwips: Could not read record from afos_to_awips for product ", \
                                  productID, ", site ", siteID)
                 
        except:
            raise Exception("QueryAfosToAwips: Could not read record from afos_to_awips for product ", \
                              productID, ", site ", siteID)

    def getWMOsite(self) :
        """
        @return: The WMO site id, e.g. KOAX
        """
        return self.wmoSite

    def getCCC(self) :
        """
        @return: The ccc id, e.g. DEN from DENWSWBOU
        """
        return self.ccc
    
    def getXXX(self):
        """
        @return: The xxx identifier, e.g. BOU from DENWSWBOU
        """
        return self.xxx
    
    def getNNN(self):
        """
        @return: The nnn identifier, e.g. WSW from DENWSWBOU
        """
        return self.nnn
    
    def getWMOprod(self):
        """
        @return: The WMO ttaaii, e.g. WWUS45 for DENWSWBOU
        """
        return self.wmoProd

    def getPIL(self):
        """
        @return: The product PIL, e.g. WSWBOU from DENWSWBOU
        """
        return self.pil
    
    def getAwipsWANpil(self):
        """
        @return: The AWIPS wan pil; KBOUWSWBOU for DENWSWBOU
        """
        return self.awipsWANPil
    
    def getTextDBpil(self):
        """
        @return: The text db PIL, e.g. DENWSWBOU
        """
        return self.textdbPil