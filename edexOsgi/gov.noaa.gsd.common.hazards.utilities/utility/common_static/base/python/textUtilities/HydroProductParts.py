'''
    Description: Specification of Product Parts for Product Generators.
                 Method for processing Product Parts.
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    4/14         1633       thansen   Initial creation.
    
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
        elif act in ['NEW', 'EXA', 'EXB', 'EXT'] and phen in ['FF', 'FA']:
            return True
        else:
            return False
        
    ######################################################
    #  Product Parts for ESF         
    ######################################################
    def _productParts_ESF(self, segment_vtecRecords_tuples):            
        segmentParts = []
        for segment_vtecRecords_tuple in segment_vtecRecords_tuples:
            segmentParts.append(self._segmentParts_ESF(segment_vtecRecords_tuple))
        return {
            'partsList': [
                'wmoHeader',
                ('segments', segmentParts),
            ]
            }

    def _segmentParts_ESF(self, segment_vtecRecords_tuple): 
        segment, vtecRecords = segment_vtecRecords_tuple
        sectionPartsList = [
                    'setup_section',
                    'narrativeForecastInformation'
                    ]
        sectionParts = []
        for vtecRecord in vtecRecords:
            section = {
                'arguments': (segment_vtecRecords_tuple, vtecRecord),
                'partsList': sectionPartsList,
                }
            sectionParts.append(section)
        return {
                'arguments': segment_vtecRecords_tuple,
                'partsList': [
                    'setup_segment',
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

    def _productParts_FFW(self, segment_vtecRecords_tuples):
        '''
        @segment_vtecRecords_tuples -- list of (segment, vtecRecords)
        @return  Dictionary for FFW productParts including the given segments
        '''          
        # Note: Product Parts for each segment may be different e.g. NEW, CON, CAN...      
        segmentParts = []
        for segment_vtecRecords_tuple in segment_vtecRecords_tuples:
            segmentParts.append(self._segmentParts_FFW(segment_vtecRecords_tuple))
        return {
                'partsList': [
                    'wmoHeader_noCR',
                    ('segments', segmentParts),
                    ]
                }
        
    def _segmentParts_FFW(self, segment_vtecRecords_tuple): 
        '''               
        @segment_vtecRecords_tuple -- (segment, vtecRecords)
        @return FFW productParts for the given segment
        '''
        segment, vtecRecords = segment_vtecRecords_tuple
        sectionParts = []
        easActivationRequested = False
        for vtecRecord in vtecRecords:
            section = {
                'arguments': (segment_vtecRecords_tuple, vtecRecord),
                'partsList': self._sectionPartsList_FFW(),
                }
            sectionParts.append(section)
            
            if self._requestEAS(vtecRecord):
                easActivationRequested = True
                    
        partsList = [
                    'setup_segment',
                    'ugcHeader',
                    'vtecRecords',
                    'CR',
                    ]
        if easActivationRequested:
            partsList.append('easMessage')
            
        partsList += [
                    'productHeader',                       
                    ('sections', sectionParts),
                    'callsToAction', # optional
                    'polygonText',
                    'endSegment',
                    # TODO Add point sections
                    # 'pointSections'
                    ]
        return {
                'arguments': segment_vtecRecords_tuple,
                'partsList': partsList,
                }

    def _sectionPartsList_FFW(self):
        return [
                'setup_section',
                'attribution',  
                'firstBullet',
                'timeBullet',
                'basisBullet',
                'locationsAffected',
                ]

    ############################  FFS
    def _productParts_FFS(self, segment_vtecRecords_tuples):
        '''
        @segment_vtecRecords_tuples -- list of (segment, vtecRecords)
        @return  FFW productParts including the given segments
        '''        
        segments = []
        for segment_vtecRecords_tuple in segment_vtecRecords_tuples:
            segments.append(self._segmentParts_FFS(segment_vtecRecords_tuple))
        return {
                'partsList' : [
                    'wmoHeader',
                    'productHeader', 
                    'CR',
                    ('segments', segments),
                    ]
                }

    def _segmentParts_FFS(self, segment_vtecRecords_tuple): 
        '''               
        @segment_vtecRecords_tuple -- (segment, vtecRecords)
        @return FFS productParts for the given segment
        '''
        segment, vtecRecords = segment_vtecRecords_tuple
        # TODO placeholder for point records to be optionally included in FFS
        pointRecords = [] 

        sectionParts = []        
        for vtecRecord in vtecRecords:
            # There is only one action / vtec record per segment
            action = vtecRecord.get('act')
            section = {
                'arguments': (segment_vtecRecords_tuple, vtecRecord),
                'partsList': ['setup_section'],
                }
            sectionParts.append(section)
          
        if action in ['CAN', 'EXP']:
            parts = [
               'endingSynopsis',
               'polygonText',
               ]
        else: # CON
            pointSections = []
            for pointRecord in pointRecords:
                pointSection = {
                           'arguments': pointRecord,
                           'partsList': self._pointSectionPartsList_FFS(pointRecord),
                           }
                pointSections.append(pointSection)
            parts = [
                #'emergencyStatement', # optional
                'basisAndImpactsStatement_segmentLevel',
                'callsToAction',
                'polygonText',
                ('pointSections', pointSections), # Sections are optional --only if points are included                   
                ]
        return {
                'arguments': segment_vtecRecords_tuple,
                'partsList': [
                    'setup_segment',
                    'ugcHeader',
                    'vtecRecords',
                    'areaList',
                    'cityList',
                    'issuanceTimeDate',
                    'CR',
                    'summaryHeadlines',
                    ('sections', sectionParts), # Sections so not have information displayed, but need to call section_setup                    
                    ] + parts
                }

    def _pointSectionPartsList_FFS(self, pointRecord):
        return  [
                'setup_section',
                'ugcHeader',
                'issuanceTimeDate',
                'nwsli',
                'CR'
                'floodPointHeader',
                'floodPointHeadline',    
                'floodPointTimeBullet',
                'floodStageBullet',
                'floodCategoryBullet',
                'otherStageBullet',
                'forecastStageBullet',
                'pointImpactsBullet',
                'floodPointTable',
                ]

    #############################################################################################################
    #  Product Parts for FFA, FLW, FLS -- NOTE: The format for the Watches is similar to that for the Warnings  #       
    #############################################################################################################

    ###########  AREA-based  ################
    
    def _productParts_FFA_FLW_FLS_area(self, segment_vtecRecords_tuples):
        '''
        @segment_vtecRecords_tuples -- list of (segment, vtecRecords)
        @return  Dictionary for productParts including the given segments
        '''
        # Note: Product Parts for each segment / section may be different e.g. NEW, CON, CAN...      
        # non_CAN_EXP is True if the segment has only CAN, EXP in it
        non_CAN_EXP = True
        easActivationRequested = False
        segmentParts = []
        for segment_vtecRecords_tuple in segment_vtecRecords_tuples:
            segmentParts.append(self._segmentParts_FFA_FLW_FLS_area(segment_vtecRecords_tuple))
            
            segment, vtecRecords = segment_vtecRecords_tuple
            for vtecRecord in vtecRecords:
                if vtecRecord['act'] not in ['CAN', 'EXP']:
                    non_CAN_EXP = False
                if self._requestEAS(vtecRecord):
                    easActivationRequested = True
       
        partsList =  ['wmoHeader']
        
        if easActivationRequested:
            partsList.append('easMessage')
            
        partsList += [ 'productHeader', 'CR']
        
        if not non_CAN_EXP:
            partsList += ['overviewHeadline_area', 'overviewSynopsis']  
        
        partsList += [
                ('segments', segmentParts),
                ]         
        return {
            'partsList': partsList,
            }

    def _segmentParts_FFA_FLW_FLS_area(self, segment_vtecRecords_tuple):
        '''               
        @segment_vtecRecords_tuple -- (segment, vtecRecords)
        @return  productParts for the given segment
        '''
        segment, vtecRecords = segment_vtecRecords_tuple
        sectionParts = []
        # non_CAN_EXP is True if the segment has only CAN, EXP in it
        non_CAN_EXP = True
        for vtecRecord in vtecRecords:
            section = {
                'arguments': (segment_vtecRecords_tuple, vtecRecord),
                'partsList': self._sectionPartsList_FFA_FLW_FLS_area(vtecRecord),
                }
            sectionParts.append(section)
            
            if vtecRecord['act'] in ['CAN', 'EXP']:
                non_CAN_EXP = False
            pil = vtecRecord['pil']  # All vtec records in this segment must have the same pil
            
        partsList = [
            'setup_segment',
            'ugcHeader',
            'vtecRecords',
            'areaList',
            'cityList',
            'issuanceTimeDate',
            'CR'
            ]
        #if pil == 'FFA':  
        partsList.append('summaryHeadlines')
            
        partsList.append(('sections', sectionParts))
        
        if pil == 'FFA' and non_CAN_EXP: 
            partsList.append('meaningOfStatement')
            
        if non_CAN_EXP:
            partsList.append('callsToAction')
            
        if pil in ['FLW', 'FLS']:
            partsList.append('polygonText')
        
        partsList.append('endSegment')    
        return {
                'arguments': segment_vtecRecords_tuple,
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
                    'setup_section',
                    'attribution',
                    'firstBullet',
                    'endingSynopsis',
                    ]
            elif pil == 'FFA':
                 partsList = [
                    'setup_section',
                    'attribution',
                    'firstBullet',
                    ]
        # FFA, FLW, or FLS FA.Y NEW OR EXT
        elif pil in ['FFA', 'FLW'] or (phen == 'FA' and sig == 'Y' and action in ['NEW', 'EXT']):
            partsList = [
                    'setup_section',
                    'attribution', 
                    'firstBullet',
                    'timeBullet',
                    'basisBullet',
                    'impactsBullet'
                    ]
        # Otherwise (FLS)
        else:
            partsList = [
                    'setup_section',
                    'attribution',
                    'firstBullet',
                    'basisAndImpactsStatement',
                    ]            
        return partsList

    ###########  POINT-based  ################

    def _productParts_FFA_FLW_FLS_point(self, segment_vtecRecords_tuples):
        '''
        @segment_vtecRecords_tuples -- list of (segment, vtecRecords)
        @return  Dictionary for productParts including the given segments
        '''
        # Note: Product Parts for each segment / section may be different e.g. NEW, CON, CAN...      
        # non_CAN_EXP is True if the segment has only CAN, EXP in it
        non_CAN_EXP = True
        segmentParts = []
        for segment_vtecRecords_tuple in segment_vtecRecords_tuples:
            segmentParts.append(self._segmentParts_FFA_FLW_FLS_point(segment_vtecRecords_tuple))
            
            segment, vtecRecords = segment_vtecRecords_tuple
            for vtecRecord in vtecRecords:
                pil = vtecRecord['pil']   # Pil will be the same for all vtecRecords in this segment
                if vtecRecord['act'] not in ['CAN', 'EXP']:
                    non_CAN_EXP = False
            
        partsList = [
                'wmoHeader',
                'easMessage',  
                'productHeader',
                
                'overviewHeadline_point',  #(optional)
                'overviewSynopsis',        #(optional)
                ]
        if pil == 'FFA' and non_CAN_EXP:
            partsList.append('rainFallStatement')
            
        if non_CAN_EXP:
            partsList += [
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
            'arguments': segment_vtecRecords_tuples,
            'partsList': partsList,
            }
        
    def _segmentParts_FFA_FLW_FLS_point(self, segment_vtecRecords_tuple):
        '''               
        @segment_vtecRecords_tuple -- (segment, vtecRecords)
        @return  productParts for the given segment
        '''
        segment, vtecRecords = segment_vtecRecords_tuple
        sectionParts = []
        for vtecRecord in vtecRecords:
            section = {
                'arguments': (segment_vtecRecords_tuple, vtecRecord),
                'partsList': self._sectionPartsList_FFA_FLW_FLS_point(vtecRecord),
                }
            sectionParts.append(section)
            
        return {
                'arguments': segment_vtecRecords_tuple,
                'partsList': [
                    'setup_segment',
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
        
        partsList = ['setup_section']
        if  not (pil == 'FLS' and phen == 'FL' and sig == 'Y' and action in ['CON', 'EXT']):
            # NOT for FLS FL.Y CON, EXT
            partsList.append('attribution')
  
        partsList.append('firstBullet')
        
        if not action in 'ROU' and not (pil == 'FLS' and action in ['CAN', 'EXP']):
            # NOT for ROU, FLS CAN, EXP
            partsList.append('timeBullet')
            
        partsList += [
            'floodPointTimeBullet',
            'floodStageBullet',
            'otherStageBullet',  
            'floodCategoryBullet', 
            'recentActivityBullet',  
            'forecastStageBullet',
            'pointImpactsBullet',
            ] 
         
        if not pil == 'FFA' and not (pil == 'FLS' and phen == 'FL' and sig == 'Y'):
            # NOT for FFA, FLS FL.Y
            partsList.append('floodHistoryBullet')  
        return partsList
    
    def flush(self):
        ''' Flush the print buffer '''
        import os
        os.sys.__stdout__.flush()
    

