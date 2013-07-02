"""
10    Description: Class to define a part of a product.
12    
13    SOFTWARE HISTORY
14    Date         Ticket#    Engineer    Description
15    ------------ ---------- ----------- --------------------------
16    July, 2013   784         Tracy.L.Hansen      Initial creation
17    
18    @author Tracy.L.Hansen@noaa.gov
19    @version 1.0
20    """



class ProductPart(object):    
    '''
    Class to define a part of product.
    A list of product parts will be created by the Product Generators.
    The list will guide the Legacy formatter in creating the product.
    '''      
    def __init__(self, name, productParts=None):
        '''
        Every Product Part needs a name.  Keyword names are:
        
        wmoHeader      --  WGUS61 KRNK 270652
                           FFARNK
        wmoHeader_noCR    (For ESF -- do not put \n after wmoHeader)
        CR             --  Insert \n 
        easMessage     --  URGENT - IMMEDIATE BROADCAST REQUESTED
        overview            -- '|* DEFAULT OVERVIEW SECTION *|'
        
        productHeader       -- Encompasses all of the following: productName, sender, issuanceTimeDate
        productName         -- FLOOD WATCH
        sender              -- NATIONAL WEATHER SERVICE EASTERN NORTH DAKOTA/GRAND FORKS ND
        issuanceTimeDate    -- 330 PM CDT FRI APR 03 2009
        
        segments            -- Contains the Product Parts for the segments
        
        ugcHeader           -- NCZ003>006-019-020-VAZ022>024-032>035-043>047-058-059-271300-
        vtecRecords         -- /O.NEW.KRNK.FA.A.0002.030527T1400Z-030528T0800Z/
                               /00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/
        areaList            -- AMHERST-APPOMATTOX-BEDFORD-BOTETOURT-BUCKINGHAM-CAMPBELL-CASWELL-
                               CHARLOTTE-FRANKLIN-HALIFAX-HENRY-PATRICK-PITTSYLVANIA-ROANOKE-
                               ROCKBRIDGE-ROCKINGHAM-STOKES-SUNNY-WILKES-YADKIN-
        cityList            -- INCLUDING THE CITIES OF ...
        headline            -- ...FLOOD WATCH IN EFFECT FROM 10 AM THIS MORNING THROUGH WEDNESDAY MORNING...
        
        bulletHeading       -- THE NATIONAL WEATHER SERVICE IN BLACKSBURG HAS ISSUED A
        firstBullet         -- * FLOOD WATCH FOR PORTIONS OF SOUTHWESTERN VIRGINIA AND NORTHWEST
                               NORTH CAROLINA...INCLUDING THE FOLLOWING COUNTIES IN VIRGINIA...
                               AMHERST...APPOMATTOX...BEDFORD...BOTETOURT...BUCKINGHAM...CAMPBELL...
                               CHARLOTTE...FRANKLIN...HALIFAX...HENRY...PATRICK...PITTSYLVANIA...
                               ROANOKE AND ROCKBRIDGE.
                               IN NORTH CAROLINA...CASWELL...ROCKINGHAM...STOKES...SURRY...WILKES
                               AND YADKIN.
        timePhrase          -- * FROM 10 AM THIS MORNING THROUGH WEDNESDAY MORNING.
        basis               -- * LOW PRESSURE IS EXPECTED TO STRENGTHEN OVER THE CAROLINAS AND MOVE
                               EAST THIS AFTERNOON AS A POTENT UPPER DISTURBANCE OVER KENTUCKY
                               MOVES OVER THE SOUTHERN APPALACHIANS. AS A RESULT RAIN IS EXPECTED
                               TO REDEVELOP AND BECOME HEAVY AT TIMES ALONG AND EAST OF THE BLUE
                               RIDGE LATER THIS MORNING AND THIS AFTERNOON.

        impacts             -- * LOWER PORTIONS OF THE DAN AND ROANOKE RIVERS ARE ALREADY IN OR NEAR
                               FLOOD DUE TO HEAVY RAINFALL SUNDAY...AND ADDITIONAL RAINFALL COULD
                               EASILY CAUSE SHARP RISES ON THESE RIVERS. AREA CREEKS AND STREAMS
                               ARE ALSO RUNNING HIGH AND COULD FLOOD WITH MORE HEAVY RAIN.
                               
        callsToAction       -- PRECAUTIONARY/PREPAREDNESS ACTIONS...
                               REMEMBER...A FLOOD WATCH MEANS THAT FLOODING IS POSSIBLE BUT NOT
                               IMMINENT IN THE WATCH AREA.
                               &&
        polygonText         -- LAT...LON 4153 7264 4131 7255 4138 7190 4159 7198
        
        narrativeForecastInformation
        tabularForecastInformation
        probabilisticForecastInformation
        end                  -- $$

        
        Focal Points adding customized Product Parts should create names that begin with
        an underscore to avoid conflicts with keywords e.g.
        _myForecastTable
        _mySpecialSection
        
        '''
        self.name = name
        self.productParts = productParts
        
    def getName(self):
        return self.name
    def getProductParts(self):
        return self.productParts
    def __str__(self):
        return 'Product Part '+self.key + ' ' + self.name + ' ' + self.productParts

