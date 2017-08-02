"""
Description: Provides an interface to the contents of the
afos_to_awips.txt.

@author James.E.Ramer@noaa.gov
@version 1.0
"""
from com.raytheon.uf.common.serialization.comm import RequestRouter
from com.raytheon.uf.common.dataplugin.text.request import GetPartialAfosIdRequest

class QueryAfosToAwips(object):

    def __init__(self, productID, siteID):
        """
        Builds an instance of QueryAfosToAwips
        using the provided product id and site
        id. Makes a query to the afos_to_awips.txt using 
        request router.  Processes and stores the
        results in member variables.
        
        @param productID: The product id, e.g. WSW
        @param siteID:  The 3-letter site id, e.g. OAX
        
        @raise Exception: An error was encountered reading
                          from the afos_to_awips.txt for
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
                cccnnnxxx = afosToAwips.getAfosid()
                self.ccc = cccnnnxxx[:3]
                self.xxx = cccnnnxxx[6:]
                self.nnn = cccnnnxxx[3:6]
                self.wmoProd = afosToAwips.getWmottaaii()
                self.wmoSite = afosToAwips.getWmocccc()
                self.pil = cccnnnxxx[3:]
                self.awipsWANPil = self.wmoSite + self.pil
                self.textdbPil = cccnnnxxx
            else:
                msg = "QueryAfosToAwips: No matching data in afos_to_awips.txt for product {}, site: {}".format(productID, siteID)
                raise Exception(msg)
                 
        except Exception as err:
            msg = "QueryAfosToAwips: Product: {}, site: {}".format(productID, siteID)
            raise Exception(msg, err)

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