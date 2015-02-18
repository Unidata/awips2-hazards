'''
    Description: Specification of Product Parts for Product Generators.
                 Method for processing Product Parts.
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    4/14         1633       thansen   Initial creation.
    11/10        4933       Robert.Blum    Added endSegment part for FFS
    1/12         4937       Robert.Blum    PGFv3 changes for FLW_FLS
    01/26/2015   4936       chris.cody     Implement scripts for Flash Flood Watch Products (FFA,FAA,FLA)
    01/31/2015   4937       Robert.Blum    Removed unneeded code.

'''
import types, collections

class HydroProductParts():
    def __init__(self):
        pass

    def _requestEAS(self, vtecRecord):
        '''
        Return True if we should request an EAS phrase
        @param vtec record
        @return boolean 
        '''
        phen = vtecRecord['phen']
        sig = vtecRecord['sig']
        act = vtecRecord['act']
        if sig == 'Y' and phen in ['FL', 'FA']:
            return False
        elif act in ['NEW', 'EXA', 'EXB', 'EXT'] and phen in ['FF', 'FA', 'FL']:
            return True
        else:
            return False
        
    ######################################################
    #  Product Parts for ESF         
    ######################################################
    def _productParts_ESF(self, productSegments):            
        segmentParts = []
        for productSegment in productSegments:
            segmentParts.append(self._segmentParts_ESF(productSegment))
        return {
            'partsList': [
                'setUp_product',
                'wmoHeader',
                ('segments', segmentParts),
            ]
            }

    def _segmentParts_ESF(self, productSegment): 
        segment = productSegment.segment
        vtecRecords = productSegment.vtecRecords
        sectionPartsList = [
                    'setUp_section',
                    'narrativeForecastInformation'
                    ]
        sectionParts = []
        for vtecRecord in vtecRecords:
            section = {
                'arguments': ((segment, vtecRecords), vtecRecord, {'bulletFormat':'bulletFormat_CR'}),
                'partsList': sectionPartsList,
                }
            sectionParts.append(section)
        return {
                'arguments': (segment, vtecRecords),
                'partsList': [
                    'setUp_segment',
                    'ugcHeader',
                    'CR',
                    'productHeader',
                    ('sections', sectionParts),
                    'endSegment',
                    ]
                }
    
    ######################################################
    #  Product Parts for FFW, FFS         
    ######################################################

    ############################  FFW

    def _productParts_FFW(self, productSegments):
        '''
        @productSegments -- list of ProductSegments -- (segment, vtecRecords)
        @return  Dictionary for FFW productParts including the given segments
        '''          
        # Note: Product Parts for each segment may be different e.g. NEW, CON, CAN...      
        segmentParts = []
        for productSegment in productSegments:
            segmentParts.append(self._segmentParts_FFW(productSegment))
        return {
                'partsList': [
                    'setUp_product',
                    # TODO Example doesn't match directive
                    #  Example: Has CR for wmoHeader - pg 26
                    #  Directive does not - pg 31
                    #  The CR is needed to pass the WarningDecoder.
                    'wmoHeader',
                    ('segments', segmentParts),
                    ]
                }
        
    def _segmentParts_FFW(self, productSegment): 
        '''               
        @productSegment -- (segment, vtecRecords)
        @return FFW productParts for the given segment
        '''
        segment = productSegment.segment
        vtecRecords = productSegment.vtecRecords
        sectionParts = []
        easActivationRequested = False
        for vtecRecord in vtecRecords:
            section = {
                'arguments': ((segment, vtecRecords), vtecRecord, {'bulletFormat':'bulletFormat_CR'}),
                'partsList': self._sectionPartsList_FFW(),
                }
            sectionParts.append(section)
            
            if self._requestEAS(vtecRecord):
                easActivationRequested = True
                    
        partsList = [
                    'setUp_segment',
                    'ugcHeader',
                    'vtecRecords',
                    'CR',
                    ]
        if easActivationRequested:
            partsList.append('easMessage')
            
        partsList += [
                    'productHeader',
                    'CR',
                    ('sections', sectionParts),
                    'callsToAction', # optional
                    'polygonText',
                    'endSegment',
                    # TODO Add point sections
                    # 'pointSections'
                    ]
        return {
                'arguments': (segment, vtecRecords),
                'partsList': partsList,
                }

    def _sectionPartsList_FFW(self):
        return [
                'setUp_section',
                'emergencyHeadline',
                'attribution',  
                'firstBullet',
                'timeBullet',
                'basisBullet',
                'emergencyStatement',
                'locationsAffected',
                ]

    ############################  FFS
    def _productParts_FFS(self, productSegments):
        '''
        @productSegments -- list of ProductSegments (segment, vtecRecords)
        @return  FFW productParts including the given segments
        '''        
        segments = []
        for productSegment in productSegments:
            segments.append(self._segmentParts_FFS(productSegment))
        return {
                'partsList' : [
                    'setUp_product',
                    'wmoHeader',
                    'productHeader', 
                    'CR',
                    ('segments', segments),
                    ]
                }

    def _segmentParts_FFS(self, productSegment): 
        '''               
        @productSegment -- (segment, vtecRecords)
        @return FFS productParts for the given segment
        '''
        segment = productSegment.segment
        vtecRecords = productSegment.vtecRecords
        # TODO placeholder for point records to be optionally included in FFS
        pointRecords = [] 

        sectionParts = []        
        for vtecRecord in vtecRecords:
            # There is only one action / vtec record per segment
            action = vtecRecord.get('act')
            section = {
                'arguments': ((segment, vtecRecords), vtecRecord, {'bulletFormat':'bulletFormat_CR'}),
                'partsList': ['setUp_section', 'locationsAffected'],
                }
            sectionParts.append(section)
          
        if action in ['CAN', 'EXP']:
            parts = [
                ('sections', sectionParts),
               'endingSynopsis',
               'polygonText',
               ]
        else: # CON
            pointSections = []
            for pointRecord in pointRecords:
                pointSection = {
                           'arguments': ((segment, vtecRecords), pointRecord, {'bulletFormat':'bulletFormat_noCR'}),
                           'partsList': self._pointSectionPartsList_FFS(pointRecord),
                           }
                pointSections.append(pointSection)
            parts = [
                #'emergencyStatement', # optional
                'basisAndImpactsStatement_segmentLevel',
                ('sections', sectionParts),
                'callsToAction',
                'polygonText',
                ('pointSections', pointSections), # Sections are optional --only if points are included                   
                ]
        return {
                'arguments': (segment, vtecRecords),
                'partsList': [
                    'setUp_segment',
                    'ugcHeader',
                    'vtecRecords',
                    'areaList',
                    'issuanceTimeDate',
                    'CR',
                    'summaryHeadlines',
                    'CR',
                    ] + parts +
                    ['endSegment']
                }

    def _pointSectionPartsList_FFS(self, pointRecord):
        return  [
                'setUp_section',
                'ugcHeader',
                'issuanceTimeDate',
                'nwsli',
                'CR'
                'floodPointHeader',
                'floodPointHeadline',    
                'observedStageBullet',
                'floodStageBullet',
                'floodCategoryBullet',
                #'otherStageBullet',
                'forecastStageBullet',
                'pointImpactsBullet',
                'floodPointTable',
                ]

    #############################################################################################################
    #  Product Parts for FFA, FLW, FLS -- NOTE: The format for the Watches is similar to that for the Warnings  #       
    #############################################################################################################

    ###########  AREA-based  ################
    
    def _productParts_FFA_FLW_FLS_area(self, productSegments):
        '''
        @productSegments -- list of ProductSegments (segment, vtecRecords)
        @return  Dictionary for productParts including the given segments
        '''
        # Note: Product Parts for each segment / section may be different e.g. NEW, CON, CAN...      
        # non_CAN_EXP is True if the segment has only CAN, EXP in it
        non_CAN_EXP = True
        easActivationRequested = False
        segmentParts = []
        for productSegment in productSegments:
            segmentParts.append(self._segmentParts_FFA_FLW_FLS_area(productSegment))
            
            segment = productSegment.segment
            vtecRecords = productSegment.vtecRecords
            for vtecRecord in vtecRecords:
                if vtecRecord['act'] not in ['CAN', 'EXP']:
                    non_CAN_EXP = False
                if self._requestEAS(vtecRecord):
                    easActivationRequested = True
       
        partsList =  ['setUp_product', 'wmoHeader']
        
        if easActivationRequested:
            partsList.append('easMessage')
            
        partsList += [ 'productHeader', 'CR']
        
        if not non_CAN_EXP:
            partsList += ['overviewSynopsis_area']

        partsList += [
                ('segments', segmentParts),
                ]         
        return {
            'partsList': partsList,
            }

    def _segmentParts_FFA_FLW_FLS_area(self, productSegment):
        '''               
        @productSegment -- (segment, vtecRecords)
        @return  productParts for the given segment
        '''
        
        productVtecRecord = None
        segment = productSegment.segment
        vtecRecords = productSegment.vtecRecords
        sectionParts = []
        # non_CAN_EXP is True if the segment has only CAN, EXP in it
        non_CAN_EXP = True
        phen = None
        for vtecRecord in vtecRecords:
            #This product has a single vtecRecord
            productVtecRecord = vtecRecord
            section = {
                'arguments': ((segment, vtecRecords), vtecRecord, {'bulletFormat':'bulletFormat_CR'}),
                'partsList': self._sectionPartsList_FFA_FLW_FLS_area(vtecRecord),
                }
            sectionParts.append(section)
            
            if vtecRecord['act'] in ['CAN', 'EXP']:
                non_CAN_EXP = False
            pil = vtecRecord['pil']  # All vtec records in this segment must have the same pil
            phen = vtecRecord['phen']
            
        if phen == "FA" :
            partsList = [
                'setUp_segment',
                'ugcHeader',
                'vtecRecords',
                'areaList',
                'issuanceTimeDate',
                'CR'
                ]
        else :
            partsList = [
                'setUp_segment',
                'ugcHeader',
                'vtecRecords',
                'areaList',
                'cityList',
                'issuanceTimeDate',
                'CR'
                ]

        if pil == 'FFA':  
            partsList.append('summaryHeadlines')
            partsList.append('CR')

        partsList.append(('sections', sectionParts))
        
        # TODO Example doesn't match directive 
        # Should the statement be part of the CTA's (example) or separate (directive)?
        #if pil == 'FFA' and non_CAN_EXP: 
            #partsList.append('meaningOfStatement')           
        phensig = ""
        if productVtecRecord is not None: 
            phensig = productVtecRecord['phensig']
            
        #if ((non_CAN_EXP) and (phensig != 'FA.A')):
        if (non_CAN_EXP):
            partsList.append('callsToAction')
            
        if pil in ['FLW', 'FLS']:
            partsList.append('polygonText')
        
        partsList.append('endSegment')    
        return {
                'arguments': ((segment, vtecRecords)),
                'partsList': partsList,
                }

    def _sectionPartsList_FFA_FLW_FLS_area(self, vtecRecord):
        pil = vtecRecord['pil']
        action = vtecRecord['act']
        phen = vtecRecord['phen']
        sig = vtecRecord['sig']
        # CAN EXP
        if action in ['CAN', 'EXP']:
            if pil == 'FLS':
                partsList = [
                    'setUp_section',
                    'attribution',
                    'endingSynopsis',
                    ]
            elif pil == 'FFA':
                 partsList = [
                    'setUp_section',
                    'attribution',
                    'firstBullet',
                    'endingSynopsis',
                    ]
        # FFA, FLW, or FLS FA.Y NEW OR EXT
        elif pil in ['FFA', 'FLW'] or (phen == 'FA' and sig == 'Y' and action in ['NEW', 'EXT']):
            partsList = [
                    'setUp_section',
                    'attribution', 
                    'firstBullet',
                    'timeBullet',
                    'basisBullet',
                    'impactsBullet'
                    ]
        # Otherwise (FLS)
        else:
            partsList = [
                    'setUp_section',
                    'attribution',
                    'firstBullet',
                    'basisAndImpactsStatement',
                    ] 
        if phen == "FA" :
            partsList.append('locationsAffected')
           
        return partsList

    ###########  POINT-based  ################

    def _productParts_FFA_FLW_FLS_point(self, productSegments):
        '''
        @productSegments -- list of ProductSegments(segment, vtecRecords)
        @return  Dictionary for productParts including the given segments
        '''
        # Note: Product Parts for each segment / section may be different e.g. NEW, CON, CAN...      
        # non_CAN_EXP is True if the segment has only CAN, EXP in it
        non_CAN_EXP = True
        segmentParts = []
        productSegment_tuples = []
        for productSegment in productSegments:
            segmentParts.append(self._segmentParts_FFA_FLW_FLS_point(productSegment))
            
            segment = productSegment.segment
            vtecRecords = productSegment.vtecRecords
            productSegment_tuples.append((segment, vtecRecords))
            for vtecRecord in vtecRecords:
                pil = vtecRecord['pil']   # Pil will be the same for all vtecRecords in this segment
                if vtecRecord['act'] in ['CAN', 'EXP']:
                    non_CAN_EXP = False
            
        partsList = [
                'setUp_product',
                'wmoHeader',
                'easMessage',  
                'productHeader', 
                'CR',
                
                'overviewHeadline_point',  #(optional)
                'overviewSynopsis_point',        #(optional)
                ]
        if pil == 'FFA' and non_CAN_EXP:
            partsList.append('rainFallStatement')
            
        if non_CAN_EXP:
            partsList += [
                'groupSummary', 
                'callsToAction_productLevel',  #(optional)
                'additionalInfoStatement',     #(optional)
                'nextIssuanceStatement',
                'CR',
                ]        
        partsList += [
                ('segments', segmentParts),
                'floodPointTable',         #(for entire product -- optional)
                'wrapUp_product',
                ]

        return {
            'arguments': productSegment_tuples,
            'partsList': partsList,
            }
        
    def _segmentParts_FFA_FLW_FLS_point(self, productSegment):
        '''               
        @productSegment -- (segment, vtecRecords)
        @return  productParts for the given segment
        '''
        segment = productSegment.segment
        vtecRecords = productSegment.vtecRecords
        sectionParts = []
        for vtecRecord in vtecRecords:
            section = {
                'arguments': ((segment, vtecRecords), vtecRecord, {'bulletFormat':'bulletFormat_noCR'}),
                'partsList': self._sectionPartsList_FFA_FLW_FLS_point(vtecRecord),
                }
            sectionParts.append(section)
            
        return {
                'arguments': (segment, vtecRecords),
                'partsList': [
                    'setUp_segment',
                    'ugcHeader',
                    'vtecRecords',
                    'issuanceTimeDate',
                    'CR',
                                
                    ('sections', sectionParts),
                    'floodPointTable',   #(for segment -- optional)
                    'endSegment',
                    ]
                }

    def _sectionPartsList_FFA_FLW_FLS_point(self, vtecRecord):
        pil = vtecRecord['pil']
        action = vtecRecord['act']
        phen = vtecRecord['phen']
        sig = vtecRecord['sig']
        
        partsList = ['setUp_section']
        if action in ['NEW', 'EXP']:
            partsList.append('attribution_point')
  
        partsList.append('firstBullet_point')
        
        if not action in 'ROU' and not (pil == 'FLS' and action in ['CAN', 'EXP']):
            # NOT for ROU, FLS CAN, EXP
            partsList.append('timeBullet')
            
        if action in ['CAN', 'EXP']:
            partsList += [
                'observedStageBullet',
                'recentActivityBullet',  
                'forecastStageBullet',
            ]
        else:
            partsList += [
                'observedStageBullet',
                'floodStageBullet',
                #'otherStageBullet',  
                'floodCategoryBullet', 
                'recentActivityBullet',  
                'forecastStageBullet',
                'pointImpactsBullet',
            ] 
         
        if not pil == 'FFA' and not (pil == 'FLS' and phen == 'FL' and sig == 'Y'):
            # NOT for FFA, FLS FL.Y
            partsList.append('floodHistoryBullet')  
        return partsList



    ######################################################
    #  Product Parts for RVS         
    ######################################################
    def _productParts_RVS(self, productSegments):            
        return {
            'partsList': [
                'setUp_product',
                'wmoHeader',
                'headlineStatement',
                'narrativeInformation',
                'floodPointTable',
            ]
            }

    
    def flush(self):
        ''' Flush the print buffer '''
        import os
        os.sys.__stdout__.flush()
    
