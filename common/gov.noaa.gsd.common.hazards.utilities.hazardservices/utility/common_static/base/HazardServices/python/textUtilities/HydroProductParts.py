import types, collections
from TextProductCommon import TextProductCommon

class HydroProductParts(object):

    def __init__(self):
        self.tpc = TextProductCommon()

    def requestEAS(self, vtecRecord):
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
    def productParts_ESF(self, productSegments):
        eventIDs , ugcs = self.tpc.parameterSetupForProductLevel(productSegments)
        segmentParts = []
        for productSegment in productSegments:
            segmentParts.append(self.segmentParts_ESF(productSegment))

        partsList = [
            'wmoHeader',
            'CR',
            ('segments', segmentParts),
            'initials',
            ]

        return {
            'partsList' : partsList,
            'eventIDs' : eventIDs,
            'ugcs' : ugcs,
            }

    def segmentParts_ESF(self, productSegment): 
        eventIDs , ugcs = self.tpc.parameterSetupForSegmentLevel(productSegment)

        sectionParts = []
        for vtecRecord in productSegment.vtecRecords:
            sectionParts.append(self.sectionParts_ESF(vtecRecord))

        partsList = [
            'setUp_segment',
            'ugcHeader',
            'CR',
            'productHeader',
            'CR',
            ('sections', sectionParts),
            'endSegment',
            ]

        return {
            'partsList' : partsList,
            'eventIDs' : eventIDs,
            'ugcs' : ugcs,
            }

    def sectionParts_ESF(self, vtecRecord):
        eventIDs , ugcs = self.tpc.parameterSetupForSectionLevel(vtecRecord)

        partsList = [
            'setUp_section',
            'narrativeForecastInformation',
            ]

        return {
            'partsList' : partsList,
            'eventIDs' : eventIDs,
            'ugcs' : ugcs,
            }

    ######################################################
    #  Product Parts for FFW, FFS         
    ######################################################
    def productParts_FFW(self, productSegments):
        '''
        @productSegments -- list of ProductSegments -- (segment, vtecRecords)
        @return  Dictionary for FFW productParts including the given segments
        '''
        eventIDs , ugcs = self.tpc.parameterSetupForProductLevel(productSegments)

        segmentParts = []
        for productSegment in productSegments:
            segmentParts.append(self.segmentParts_FFW(productSegment))

        partsList = [
            # TODO Example doesn't match directive
            # Example: Has CR for wmoHeader - pg 26
            # Coded to match WarnGen with no CR
            'wmoHeader',
            ('segments', segmentParts),
            'initials',
            ]

        return {
            'partsList' : partsList,
            'eventIDs' : eventIDs,
            'ugcs' : ugcs,
            }

    def segmentParts_FFW(self, productSegment):
        '''
        @productSegment -- (segment, vtecRecords)
        @return FFW productParts for the given segment
        '''
        eventIDs , ugcs = self.tpc.parameterSetupForSegmentLevel(productSegment)

        easActivationRequested = False
        for vtecRecord in productSegment.vtecRecords:
            if self.requestEAS(vtecRecord):
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
                ('sections', [self.sectionPartsList_FFW(vtecRecord)]),
                'polygonText',
                'endSegment',
                ]

        return {
            'partsList' : partsList,
            'eventIDs' : eventIDs,
            'ugcs' : ugcs,
            }

    def sectionPartsList_FFW(self, vtecRecord):
        eventIDs , ugcs = self.tpc.parameterSetupForSectionLevel(vtecRecord)

        partsList = [
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

        return {
            'partsList' : partsList,
            'eventIDs' : eventIDs,
            'ugcs' : ugcs,
            }

    ############################  FFS
    def productParts_FFS(self, productSegments):
        '''
        @productSegments -- list of ProductSegments (segment, vtecRecords)
        @return  FFW productParts including the given segments
        '''
        eventIDs , ugcs = self.tpc.parameterSetupForProductLevel(productSegments)
        segments = [self.segmentParts_FFS(productSegment) for productSegment in productSegments]

        partsList = [
            'wmoHeader',
            'CR',
            'productHeader',
            'CR',
            ('segments', segments),
            'initials',
            ]

        return {
            'partsList' : partsList,
            'eventIDs' : eventIDs,
            'ugcs' : ugcs,
            }

    def segmentParts_FFS(self, productSegment):
        '''
        @productSegment -- (segment, vtecRecords)
        @return FFS productParts for the given segment
        '''
        eventIDs , ugcs = self.tpc.parameterSetupForSegmentLevel(productSegment)

        # There is only one action / vtec record per segment
        vtecRecord = productSegment.vtecRecords[0]
        action = vtecRecord.get('act')

        partsList = [
                    'setUp_segment',
                    'ugcHeader',
                    'vtecRecords',
                    'areaList',
                    'issuanceTimeDate',
                    'CR',
                    ]

        if action in ['CAN', 'EXP']:
            partsList += [
                    'summaryHeadlines',
                    'CR',
                    ('sections', [self.areaSectionPartsList_FFS(vtecRecord)]),
                    'endingSynopsis',
                    'polygonText',
                    'endSegment',
                    ]
        else: # CON
            partsList += [
                    'emergencyHeadline',
                    'summaryHeadlines',
                    'CR',
                    ('sections', [self.areaSectionPartsList_FFS(vtecRecord)]),
                    'polygonText',
                    'endSegment',
                    ]

        return {
            'partsList' : partsList,
            'eventIDs' : eventIDs,
            'ugcs' : ugcs,
            }

    def areaSectionPartsList_FFS(self, vtecRecord):
        eventIDs , ugcs = self.tpc.parameterSetupForSectionLevel(vtecRecord)

        partsList = [
                'setUp_section',
                ]
        if vtecRecord.get('act') not in ['CAN', 'EXP']:
            partsList += [
                    'basisBullet',
                    'emergencyStatement',
                    'locationsAffected',
                    'additionalComments',
                    'callsToAction_sectionLevel',
                    'endSection',
                    ]

        return {
            'partsList' : partsList,
            'eventIDs' : eventIDs,
            'ugcs' : ugcs,
            }

    #############################################################################################################
    #  Product Parts for FFA, FLW, FLS -- NOTE: The format for the Watches is similar to that for the Warnings  #       
    #############################################################################################################

    ###########  AREA-based  ################

    def productParts_FFA_FLW_FLS_area(self, productSegments):
        '''
        @productSegments -- list of ProductSegments (segment, vtecRecords)
        @return  Dictionary for productParts including the given segments
        '''
        eventIDs , ugcs = self.tpc.parameterSetupForProductLevel(productSegments)

        # Note: Product Parts for each segment / section may be different e.g. NEW, CON, CAN...      
        # non_CAN_EXP is True if the segment has only CAN, EXP in it
        non_CAN_EXP = True
        easActivationRequested = False
        segmentParts = []
        for productSegment in productSegments:
            segmentParts.append(self.segmentParts_FFA_FLW_FLS_area(productSegment))
            for vtecRecord in productSegment.vtecRecords:
                if vtecRecord['act'] not in ['CAN', 'EXP']:
                    non_CAN_EXP = False
                if self.requestEAS(vtecRecord):
                    easActivationRequested = True

        partsList =  [
                'wmoHeader',
                'CR',
                ]

        if easActivationRequested:
            partsList.append('easMessage')

        partsList += [
                'productHeader',
                'CR',
                ]

        if not non_CAN_EXP:
            partsList.append('overviewSynopsis_area')

        partsList += [
                ('segments', segmentParts),
                 'initials',
                ]

        return {
            'partsList' : partsList,
            'eventIDs' : eventIDs,
            'ugcs' : ugcs,
            }

    def segmentParts_FFA_FLW_FLS_area(self, productSegment):
        '''
        @productSegment -- (segment, vtecRecords)
        @return  productParts for the given segment
        '''
        eventIDs , ugcs = self.tpc.parameterSetupForSegmentLevel(productSegment)

        sectionParts = []
        for vtecRecord in productSegment.vtecRecords:
            sectionParts.append(self.sectionPartsList_FFA_FLW_FLS_area(vtecRecord))
            pil = vtecRecord['pil']  # All vtec records in this segment must have the same pil
            action = vtecRecord['act']

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

        if pil == 'FFA' or (pil in ['FLW', 'FLS'] and action not in ['NEW', 'EXT']):
            partsList.append('summaryHeadlines')
            partsList.append('CR')

        partsList.append(('sections', sectionParts))

        if pil in ['FLW', 'FLS']:
            partsList.append('polygonText')

        partsList.append('endSegment')

        return {
            'partsList' : partsList,
            'eventIDs' : eventIDs,
            'ugcs' : ugcs,
            }

    def sectionPartsList_FFA_FLW_FLS_area(self, vtecRecord):
        eventIDs , ugcs = self.tpc.parameterSetupForSectionLevel(vtecRecord)
        pil = vtecRecord['pil']
        action = vtecRecord['act']
        phen = vtecRecord['phen']
        sig = vtecRecord['sig']
        phenSig = phen + '.' + sig

        # Defaults the partsList
        partsList = [
                'setUp_section',
                'attribution',
                'firstBullet',
                'basisAndImpactsStatement',
        ]

        # CAN EXP
        if action in ['CAN', 'EXP']:
            if pil == 'FFA':
                partsList = [
                    'setUp_section',
                    'attribution',
                    'endingSynopsis',
                    ]
            else:
                partsList = [
                    'setUp_section',
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
                    'impactsBullet',
                    ]
        # FLS FA.Y, FA.W CON
        elif phenSig in ['FA.Y', 'FA.W']:
            partsList = [
                    'setUp_section',
                    'basisBullet',
                    ]

        if phen == "FA" and sig != "A":  # FA.W and FA.Y
            if action != 'CAN' and action != 'EXP':
                partsList.append('locationsAffected')
                partsList.append('additionalComments')

        if action != 'CAN' and action != 'EXP':
            partsList.append('callsToAction_sectionLevel')
            partsList.append('endSection')

        return {
            'partsList' : partsList,
            'eventIDs' : eventIDs,
            'ugcs' : ugcs,
            }


    ###########  POINT-based  ################
    def productParts_FFA_FLW_FLS_point(self, productSegments):
        '''
        @productSegments -- list of ProductSegments(segment, vtecRecords)
        @return  Dictionary for productParts including the given segments
        '''
        eventIDs , ugcs = self.tpc.parameterSetupForProductLevel(productSegments)

        # Note: Product Parts for each segment / section may be different e.g. NEW, CON, CAN...      
        # non_CAN_EXP is True if the segment has only CAN, EXP in it
        non_CAN_EXP = True
        segmentParts = []
        for productSegment in productSegments:
            segmentParts.append(self.segmentParts_FFA_FLW_FLS_point(productSegment))
            for vtecRecord in productSegment.vtecRecords:
                if vtecRecord['act'] in ['CAN', 'EXP']:
                    non_CAN_EXP = False

        partsList = [
                'wmoHeader',
                'CR',
                'easMessage',
                'productHeader',
                'CR',
                'overviewHeadline_point',
                'overviewSynopsis_point',
                ]

        if non_CAN_EXP:
            partsList += [
                'groupSummary',
                'callsToAction_productLevel',
                'additionalInfoStatement',
                'nextIssuanceStatement',
                'CR',
                ]
        partsList += [
                ('segments', segmentParts),
                'initials',
                ]

        return {
            'partsList' : partsList,
            'eventIDs' : eventIDs,
            'ugcs' : ugcs,
            }

    def segmentParts_FFA_FLW_FLS_point(self, productSegment):
        '''
        @productSegment -- (segment, vtecRecords)
        @return  productParts for the given segment
        '''
        eventIDs , ugcs = self.tpc.parameterSetupForSegmentLevel(productSegment)
        sectionParts = [self.sectionPartsList_FFA_FLW_FLS_point(vtecRecord) for vtecRecord in productSegment.vtecRecords]
        partsList = [
            'setUp_segment',
            'ugcHeader',
            'vtecRecords',
            'issuanceTimeDate',
            'CR',
            ('sections', sectionParts),
            'floodPointTable',
            'polygonText',
            'endSegment',
            ]

        return {
            'partsList' : partsList,
            'eventIDs' : eventIDs,
            'ugcs' : ugcs,
            }

    def sectionPartsList_FFA_FLW_FLS_point(self, vtecRecord):
        eventIDs , ugcs = self.tpc.parameterSetupForSectionLevel(vtecRecord)
        action = vtecRecord['act']
        phensig = vtecRecord['phensig']

        partsList = [
                 'setUp_section',
                 'attribution_point',
                 'firstBullet_point',
                 ]

        if action not in ['ROU', 'CAN', 'EXP']:
            partsList.append('timeBullet')

        if action in ['CAN', 'EXP']:
            partsList += [
                'observedStageBullet',
                'recentActivityBullet',
                'forecastStageBullet',
            ]
        else:
            if phensig in ['FL.Y']:
                partsList += [
                    'observedStageBullet',
                    'floodStageBullet',
                    'recentActivityBullet',
                    'forecastStageBullet',
                    'pointImpactsBullet',
                ] 
            else:
                partsList += [
                    'observedStageBullet',
                    'floodStageBullet',
                    'floodCategoryBullet',
                    'recentActivityBullet',
                    'forecastStageBullet',
                    'pointImpactsBullet',
                ] 

        # According to the Directives (NWSI 10-922, Section 4), 
        # the "Flood History" bullet is not allowed for FL.A. 
        # Rather than check for FL.A, however, Mark Armstrong
        # in an email on 9/29/2016 has requested the below
        # logic
        if phensig in ['FL.W'] and action not in ['CAN', 'EXP']:
            partsList.append('floodHistoryBullet')

        return {
            'partsList' : partsList,
            'eventIDs' : eventIDs,
            'ugcs' : ugcs,
            }

    ######################################################
    #  Product Parts for RVS         
    ######################################################
    def productParts_RVS(self):
        eventIDs , ugcs = self.tpc.parameterSetupForProductLevel(productSegments)
        partsList = [
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

        # EventIDs and ugcs will be populated later based
        # on the events used for the RVS.
        return {
            'partsList' : partsList,
            'eventIDs' : [],
            'ugcs' : '',
            }
