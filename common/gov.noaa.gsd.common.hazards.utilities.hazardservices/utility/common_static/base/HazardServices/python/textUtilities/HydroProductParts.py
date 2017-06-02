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
    02/26/2015   6599       Robert.Blum    Changed to new style class
    05/27/2015   7748       Robert.Blum    Added Flood History bullet for all FL.* hazards.
    06/03/2015   8530       Robert.Blum    Added new productPart for initials and additionalComments.
                                           Also wmoHeader no longer has a CR built in.
    06/18/2015   8181       Robert.Blum    Removing cityList productPart for FA.Y and FA.W to match WarnGen.
    06/24/2015   8181       Robert.Blum    Changes for cityList/locationsAffected.
    07/22/2015   9645       Robert.Blum    Adding cityList to FF.W products to be consistent across products.
    07/27/2015   9637       Robert.Blum    Added polygonText to FL.* hazards.
    08/20/2015   9519       Robert.Blum    Removed locationsAffected when for CANs and EXPs.
    08/24/2015   9553       Robert.Blum    Removed basisAndImpactsStatement_segmentLevel
    10/19/2015   11846      Robert.Blum    Added Emergency Headline/Statement for FFS (non CAN/EXP).
    11/09/2015   7532       Robert.Blum    Changes for multiple sections per segment.
    12/10/2015   11852      Robert.Blum    Removed cityList for WarnGen products.
    03/22/2016   16044      Robert.Blum    Removed timeBullet for FL.A Cancellations and Expirations.
    05/11/2016   16914      Robert.Blum    Removed rainfall statement from FL.A hazards.
    05/12/2016   18264      Robert.Blum    Added CR to Hydrologic Outlook.
    05/27/2016   19080      dgilling       Add attribution text to point-based FFA, FLW and FLS.
    06/02/2016   19150      Roger,Ferrek   Add ending synopsis
    06/23/2016   19150      Robert.Blum    Revert change.
    06/27/2016   18277      Robert.Blum    Removed additionalComments for FFS CAN and EXP products.
    08/09/2016   18277      mduff          moved additionalComment line into if block.
    08/31/2016   21636      Sara.Stewart   updated _productParts_FFS
'''
import types, collections

class HydroProductParts(object):
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
                'CR',
                ('segments', segmentParts),
                'initials',
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
                'partsList': sectionPartsList,
                }
            sectionParts.append(section)
        return {
                'partsList': [
                    'setUp_segment',
                    'ugcHeader',
                    'CR',
                    'productHeader',
                    'CR',
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
                    # Coded to match WarnGen with no CR
                    'wmoHeader',
                    ('segments', segmentParts),
                    'initials',
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
                    'polygonText',
                    'endSegment',
                    # TODO Add point sections
                    # 'pointSections'
                    ]
        return {
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
                'additionalComments',
                'callsToAction_sectionLevel',
                'endSection',
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
                    'CR',
                    'productHeader', 
                    'CR',
                    ('segments', segments),
                    'initials',
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
                'partsList': self._areaSectionPartsList_FFS(vtecRecord),
                }
            sectionParts.append(section)

        segmentParts = [
                    'setUp_segment',
                    'ugcHeader',
                    'vtecRecords',
                    'areaList',
                    'issuanceTimeDate',
                    'CR',
                    ]
        if action in ['CAN', 'EXP']:
            return {
                'partsList': segmentParts + 
                    [
                    'summaryHeadlines',
                    'CR',
                    ('sections', sectionParts),
                    'endingSynopsis',
                    'polygonText',
                    'endSegment']
                }
        else: # CON
            return {
                    'partsList': segmentParts +
                        [
                        'emergencyHeadline',
                        'summaryHeadlines',
                        'CR',
                        ('sections', sectionParts),
                        'polygonText',
                        'endSegment']
                    }

    def _areaSectionPartsList_FFS(self, areaRecord):
        action = areaRecord.get('act')
        if action in ['CAN', 'EXP']:
            return [
                    'setUp_section',
                    ]
        else:
            return [
                    'setUp_section',
                    'basisBullet',
                    'emergencyStatement',
                    'locationsAffected',
                    'additionalComments',
                    'callsToAction_sectionLevel',
                    'endSection',
                    ]

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

        partsList =  ['setUp_product', 'wmoHeader', 'CR']

        if easActivationRequested:
            partsList.append('easMessage')

        partsList += [ 'productHeader', 'CR']

        if not non_CAN_EXP:
            partsList += ['overviewSynopsis_area']

        partsList += [
                ('segments', segmentParts),
                 'initials',
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
        for vtecRecord in vtecRecords:
            productVtecRecord = vtecRecord
            section = {
                'partsList': self._sectionPartsList_FFA_FLW_FLS_area(vtecRecord),
                }
            sectionParts.append(section)
            pil = vtecRecord['pil']  # All vtec records in this segment must have the same pil

        partsList = [
            'setUp_segment',
            'ugcHeader',
            'vtecRecords',
            'areaList',
            ]

        if pil == 'FFA':
            partsList.append('cityList')

        partsList.append('issuanceTimeDate')
        partsList.append('CR')

        if pil == 'FFA':
            partsList.append('summaryHeadlines')
            partsList.append('CR')

        partsList.append(('sections', sectionParts))

        phensig = ""
        if productVtecRecord is not None: 
            phensig = productVtecRecord['phensig']

        if pil in ['FLW', 'FLS']:
            partsList.append('polygonText')

        partsList.append('endSegment')
        return {
                'partsList': partsList,
                }

    def _sectionPartsList_FFA_FLW_FLS_area(self, vtecRecord):
        pil = vtecRecord['pil']
        action = vtecRecord['act']
        phen = vtecRecord['phen']
        sig = vtecRecord['sig']
        phenSig = phen + '.' + sig
        # CAN EXP
        if action in ['CAN', 'EXP']:
            if pil in ['FLS', 'FFA']:
                partsList = [
                    'setUp_section',
                    'attribution',
                    'endingSynopsis',
                    ]
        # FFA, FLW, or FLS FA.Y NEW OR EXT
        elif pil in ['FFA', 'FLW'] or (phenSig == 'FA.Y' and action in ['NEW', 'EXT']):
            partsList = [
                    'setUp_section',
                    'attribution', 
                    'firstBullet',
                    'timeBullet',
                    'basisBullet',
                    'impactsBullet'
                    ]
        # FLS FA.Y, FA.W CON, CAN, EXP
        elif phenSig in ['FA.Y', 'FA.W']:
            partsList = [
                    'setUp_section',
                    'attribution',
                    'basisAndImpactsStatement',
                    ]
        # Otherwise (FLS)
        else:
            partsList = [
                    'setUp_section',
                    'attribution',
                    'firstBullet',
                    'basisAndImpactsStatement',
                    ]
        if phen == "FA" and sig != "A":  # FA.W and FA.Y
            if action != 'CAN' and action != 'EXP':
                partsList.append('locationsAffected')
                partsList.append('additionalComments')

        if action != 'CAN' and action != 'EXP':
            partsList.append('callsToAction_sectionLevel')
            partsList.append('endSection')

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
                'CR',
                'easMessage',  
                'productHeader', 
                'CR',
                'overviewHeadline_point',  #(optional)
                'overviewSynopsis_point',  #(optional)
                ]

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
#                 'floodPointTable',         #(for entire product -- optional
                'wrapUp_product',
                'initials',
                ]

        return {
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
                'partsList': self._sectionPartsList_FFA_FLW_FLS_point(vtecRecord),
                }
            sectionParts.append(section)

        return {
                'partsList': [
                    'setUp_segment',
                    'ugcHeader',
                    'vtecRecords',
                    'issuanceTimeDate',
                    'CR',
                    ('sections', sectionParts),
                    'floodPointTable',   #(for segment -- optional)
                    'polygonText',
                    'endSegment',
                    ]
                }

    def _sectionPartsList_FFA_FLW_FLS_point(self, vtecRecord):
        pil = vtecRecord['pil']
        action = vtecRecord['act']
        phen = vtecRecord['phen']
        sig = vtecRecord['sig']

        partsList = ['setUp_section']
        
        partsList.append('attribution_point')

        partsList.append('firstBullet_point')

        if action not in ['ROU', 'CAN', 'EXP']:
            # NOT for ROU, CAN, EXP
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

        # RM 7748 The below if statement is being removed to allow the Flood History
        # bullet for all FL.* hazards. This was requested by the IWT and also matches
        # the capability of RiverPro.
        # if not pil == 'FFA' and not (pil == 'FLS' and phen == 'FL' and sig == 'Y'):
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
                'CR',
                'ugcHeader',
                'productHeader',
                'CR',
                'headlineStatement',
                'CR',
                'narrativeInformation',
                'CR',
                'floodPointTable',
                'initials',
            ]
            }

    def flush(self):
        ''' Flush the print buffer '''
        import os
        os.sys.__stdout__.flush()