"""
10    Description: Provides classes and methods for creating and
11     manipulating projections.
12    
13    SOFTWARE HISTORY
14    Date         Ticket#    Engineer    Description
15    ------------ ---------- ----------- --------------------------
16    April 5, 2013            Tracy.L.Hansen      Initial creation
17    
18    @author Tracy.L.Hansen@noaa.gov
19    @version 1.0
20    """

import cPickle, os, types, string, copy
import sys, gzip, time, re
import xml.etree.ElementTree as ET
import json

from TextProductCommon import TextProductCommon

class HazardServicesGenericHazards:  
    '''
    This class was cloned from AWIPS 2 GFE / GHG Generic Hazards.
    This was brought into Hazard Services as part of the complete Product Generator
    infrastructure.  As development continues, this code will be leveraged
    and refined as it becomes needed in Hazard Services.
    
    In PV2, we will need the overview and text capture methods as starting points
    for capturing user text.  There is a "hook" for this in PV1 ProductGeneratorTemplate.
    The hazardBodyText method is used in AWIPS 2 GHG products: CFW, MWS, MWW, NPW, RFW, WSW.
    It is included since we are building the foundation of all of Phase 1 Hazard Services 
    which will include these programs.
    '''  
    def __init__(self):
        self._tpc = TextProductCommon()
       
   # Added for DR 21194
    def _bulletDict(self):
        return []

    # Added for DR 21309
    def _bulletOrder(self):
        return []

    def _indentBulletText(self, prevText):

        ### if previous text is empty, return nothing
        if prevText is None:
            return prevText

        ###
        ### split the text
        ###
        bullets = []
        bullets = string.split(prevText, '\n\n')
        if len(bullets) <= 1:
            return prevText

        ###
        ### process the text
        ###
        outText = ""
        for b in bullets:
            ### if first character is a * we found a bullet
            if re.match("\*", b):
                ### remove line feeds
                removeLF = re.compile(r'(s*[^\n])\n([^\n])', re.DOTALL)
                bullet = removeLF.sub(r'\1 \2',b)
                ### indent code
                bullet = self.indentText(bullet, indentFirstString = '',
                                     indentNextString = '  ', maxWidth=self._lineLength,
                                     breakStrings=[" ", "..."])
                ###
                ### the "-" in the breakStrings line above is causing issues with
                ### offices that use "-20 degrees" in the text.
                ###
                outText = outText + bullet + "\n\n"
            else:  ### not a bullet, CTA text
                outText = outText + b + "\n\n"
                
        ### that's it
        return outText
    
    #
    # The method hazardBodyText creates an attribution phrase
    #

    def hazardBodyText(self, hazardList, creationTime, testMode):

        bulletProd = self._bulletProd
        hazardBodyPhrase = ''

        #
        # First, sort the hazards for this segment by importance
        #
        
        sortedHazardList = []
        for each in ['W', 'Y', 'A', 'O', 'S']:
            for eachHazard in hazardList:
                if eachHazard['sig'] == each:
                   if eachHazard not in sortedHazardList:
                       sortedHazardList.append(eachHazard)
 
      #
      # Next, break them into individual lists based on action 
      #

        newList = []
        canList = []
        expList = []
        extList = []
        conList = []
        upgList = []
        statementList = []

        for eachHazard in sortedHazardList:
            if eachHazard['sig'] in ['S']and eachHazard['phen'] in ['CF', 'LS']:
                statementList.append(eachHazard)
            elif eachHazard['act'] in ['NEW', 'EXA', 'EXB']:
                newList.append(eachHazard)
            elif eachHazard['act'] in ['CAN']:
                canList.append(eachHazard)
            elif eachHazard['act'] in ['EXP']:
                expList.append(eachHazard)
            elif eachHazard['act'] in ['EXT']:
                extList.append(eachHazard)
            elif eachHazard['act'] in ['UPG']:
                upgList.append(eachHazard)
            else:
                conList.append(eachHazard)

        #
        # Now, go through each list and build the phrases
        #

        nwsIntroUsed = 0

        #
        # This is for the new hazards
        #
        
        phraseCount = 0
        lastHdln = None
        for eachHazard in newList:
            hdln = eachHazard['hdln']
            if len(eachHazard['hdln']) == 0:
                continue   #no defined headline, skip phrase
            endTimePhrase = self.hazardTimePhrases(eachHazard, creationTime)
            hazNameA = self.hazardName(eachHazard['hdln'], testMode, True)
            hazName = self.hazardName(eachHazard['hdln'], testMode, False)

            if hazName == "WINTER WEATHER ADVISORY" or hazName == "WINTER STORM WARNING":
                forPhrase = " FOR |* ENTER HAZARD TYPE *|"
            else:
                forPhrase =""

            if nwsIntroUsed == 0:
                hazardBodyPhrase = "THE NATIONAL WEATHER SERVICE IN " + self._wfoCity
                nwsIntroUsed = 1
            if phraseCount == 0:
                phraseCount = 1
                hazardBodyPhrase = hazardBodyPhrase + " HAS ISSUED " + \
                  hazNameA + forPhrase + \
                  "...WHICH IS IN EFFECT" + endTimePhrase + ". "
            elif phraseCount == 1:
                phraseCount = 2
                if hdln != lastHdln:
                    hazardBodyPhrase = hazardBodyPhrase + hazNameA + \
                      " HAS ALSO BEEN ISSUED. THIS " + hazName + forPhrase + \
                      " IS IN EFFECT" + endTimePhrase + ". "
                else:
                    hazardBodyPhrase = hazardBodyPhrase + hazNameA + \
                      " HAS ALSO BEEN ISSUED" + endTimePhrase + ". "
            else:
                hazardBodyPhrase = hazardBodyPhrase + "IN ADDITION..." + \
                  hazNameA + forPhrase + " HAS BEEN ISSUED. THIS " + hazName + \
                  " IS IN EFFECT" + endTimePhrase + ". "
            lastHdln = hdln                                         
            
        #
        # This is for the can hazards
        #
        
        for eachHazard in canList:
            if len(eachHazard['hdln']) == 0:
                continue   #no defined headline, skip phrase
            hazName = self.hazardName(eachHazard['hdln'], testMode, False)
            if nwsIntroUsed == 0:
                hazardBodyPhrase = "THE NATIONAL WEATHER SERVICE IN " +\
                  self._wfoCity
                nwsIntroUsed = 1
                hazardBodyPhrase = hazardBodyPhrase + \
                 " HAS CANCELLED THE " + hazName + ". "
            else:
                hazardBodyPhrase = hazardBodyPhrase + "THE " + hazName + \
                  " HAS BEEN CANCELLED. "

        #
        # This is for the exp hazards
        #
        
        phraseCount = 0
        for eachHazard in expList:
            if len(eachHazard['hdln']) == 0:
                continue   #no defined headline, skip phrase
            if self._bulletProd:
                continue   # No attribution for this case if it is a bullet product
            hazName = self.hazardName(eachHazard['hdln'], testMode, False)
            if eachHazard['endTime'] <= creationTime:
                hazardBodyPhrase = hazardBodyPhrase + "THE " + hazName + \
                  " IS NO LONGER IN EFFECT. "
            else:
               expTimeCurrent = creationTime
               timeWords = self.getTimingPhrase(eachHazard, expTimeCurrent)
                                         
               hazardBodyPhrase = hazardBodyPhrase + "THE " + hazName + \
                 " WILL EXPIRE " + timeWords + ". "

        #
        # This is for ext hazards
        #
        
        for eachHazard in extList:
            if len(eachHazard['hdln']) == 0:
                continue   #no defined headline, skip phrase
            if self._bulletProd:
                continue   # No attribution for this case if it is a bullet product
            endTimePhrase = self.hazardTimePhrases(eachHazard, creationTime)
            hazName = self.hazardName(eachHazard['hdln'], creationTime, False)
            
            hazardBodyPhrase = hazardBodyPhrase + "THE " + hazName + \
              " IS NOW IN EFFECT" + endTimePhrase + ". "

        #
        # This is for upgrade hazards
        #

        for eachHazard in upgList:
            if len(eachHazard['hdln']) == 0:
                continue   #no defined headline, skip phrase
            hazName = self.hazardName(eachHazard['hdln'], testMode, False)
            hazardBodyPhrase = hazardBodyPhrase + "THE " + hazName + \
              " IS NO LONGER IN EFFECT. "

        #
        # This is for con hazards
        #

        for eachHazard in conList:
            if len(eachHazard['hdln']) == 0:
                continue   #no defined headline, skip phrase
            if self._bulletProd:
                continue   # No attribution for this case if it is a bullet product
            endTimePhrase = self.hazardTimePhrases(eachHazard, creationTime)
            hazNameA = self.hazardName(eachHazard['hdln'], testMode, True)
            hazardBodyPhrase = hazardBodyPhrase + hazNameA + \
              " REMAINS IN EFFECT" + endTimePhrase + ". "

        #
        # This is for statement hazards
        #

        for eachHazard in statementList:
            hazardBodyPhrase = "...|* ADD STATEMENT HEADLINE *|...\n\n"
                        
        #
        # This adds segment text
        #

        segmentText = ''
        
        #
        # Check that this segment codes to determine capture or not,
        # and frame captured text or not
        #
        incTextFlag, incFramingCodes, skipCTAs, forceCTAList = \
          self.useCaptureText(sortedHazardList)

        #
        #
        # Check that the previous text exists
        #

        
        foundCTAs = []
        for eachHazard in sortedHazardList:
            if eachHazard.has_key('prevText'):
                prevText = eachHazard['prevText']
                if eachHazard['pil'] == 'MWS':
                    startPara = 0
                else:
                    startPara = 1
                    segmentText, foundCTAs = self.cleanCapturedText(prevText,
                      startPara, addFramingCodes = False,
                      skipCTAs = skipCTAs)
                    tester = segmentText[0]
                    if tester == '*':
                        startPara = 1
                    else: 
                        startPara = 2
                segmentText, foundCTAs = self.cleanCapturedText(prevText,
                  startPara, addFramingCodes = False,
                  skipCTAs = skipCTAs)

        #
        # Check that the segment text isn't very short or blank
        #

        if len(segmentText) < 6:
            incTextFlag = 0

        # DR 21309 code addition from Middendorf (BYZ)
        #
        # Now if there is a new hazard and previous segment Text, then
        # we may have to add bullets.
        #
        if incTextFlag and bulletProd:
            for eachHazard in sortedHazardList:
                if not eachHazard.has_key('prevText'):
                    newBullets = string.split(self._bulletDict().get(eachHazard['phen']),",")
                    for bullet in newBullets:
                        if not "* " + bullet + "..." in segmentText:
                            start = self._bulletOrder().index(bullet) + 1
                            end = len(self._bulletOrder())
                            bulletFlag = 1
                            for i in range(start,end):
                                if "* " + self._bulletOrder()[i] + "..." in segmentText and bulletFlag:
                                    segmentTextSplit = string.split(segmentText,"* " + self._bulletOrder()[i] + "...")
                                    segmentText = string.join(segmentTextSplit,"* " + bullet + \
                                                              "...|* ENTER BULLET TEXT *|\n\n* " + self._bulletOrder()[i] + "...")
                                    bulletFlag = 0
                            if bulletFlag:
                                segmentTextSplit = string.split(segmentText,"PRECAUTIONARY/PREPAREDNESS ACTIONS...")
                                segmentText = "\n" + string.join(segmentTextSplit,"* " + bullet + \
                                                                   "...|* ENTER BULLET TEXT *|\n\nPRECAUTIONARY/PREPAREDNESS ACTIONS...")
                                bulletFlag = 0
        #
        # Now if there is a can/exp hazard and previous segment Text, then
        # we may have to remove bullets.
        #

        if incTextFlag and bulletProd:
            # First make list of bullets that we need to keep.
            keepBulletList = []
            for eachHazard in sortedHazardList:
                if eachHazard['act'] not in ["CAN","EXP"]:
                    saveBullets = string.split(self._bulletDict().get(eachHazard['phen']),",")
                    for saveBullet in saveBullets:
                        if saveBullet not in keepBulletList:
                            keepBulletList.append(saveBullet)
            # Now determine which bullets we have to remove.
            removeBulletList = []
            for eachHazard in sortedHazardList:
                if eachHazard['act'] in ["CAN","EXP"]:
                    canBullets = string.split(self._bulletDict().get(eachHazard['phen']),",")
                    for canBullet in canBullets:
                        if canBullet not in keepBulletList and canBullet not in removeBulletList:
                            removeBulletList.append(canBullet)
            # Finally remove the bullets no longer needed.
            PRECAUTION = "PRECAUTIONARY/PREPAREDNESS ACTIONS..."
            for bullet in removeBulletList:
                segmentTextSplit = string.split(segmentText,"* " + bullet + "...")
                if len(segmentTextSplit) > 1:
                    segmentTextSplit2 = string.split(segmentTextSplit[1],"*",1)
                    if len(segmentTextSplit2) == 2:
                        segmentTextSplit[1] = "*" + segmentTextSplit2[1]
                    else:
                        segmentTextSplit2 = string.split(segmentTextSplit[1], \
                                                         PRECAUTION, 1)
                        if len(segmentTextSplit2) == 2:
                            segmentTextSplit[1] = PRECAUTION + segmentTextSplit2[1]
                    segmentText = string.join(segmentTextSplit,"")

            if removeBulletList != []:
                segmentText = "|*\n" + segmentText + "*|"

        #
        # If segment passes the above checks, add the text
        #

        if incTextFlag:
            hazardBodyPhrase = hazardBodyPhrase + "\n\n" + \
              segmentText + '\n\n'

        elif bulletProd:
            bulletFlag = 0
            if eachHazard['act'] == 'CAN':
                hazardBodyPhrase = hazardBodyPhrase + \
                  "\n\n|* WRAP-UP TEXT GOES HERE *|.\n"
            elif eachHazard['act'] == 'EXP':
                hazardBodyPhrase = hazardBodyPhrase + \
                  "\n\n|* WRAP-UP TEXT GOES HERE *|.\n"
            else:
                bulletFlag = 1
            if bulletFlag:
                newBulletList = []
                bullets = ""
                for eachHazard in sortedHazardList:
                ### get the default bullets for all hazards from the bullet diction
                    newBullets = string.split(self._bulletDict().get(eachHazard['phen']),",")
                    for newBullet in newBullets:
                        if newBullet not in newBulletList:
                            newBulletList.append(newBullet)
         ###   Determine the correct order for all bullets       
                bulletOrder = self._bulletOrder()
                staticBulletOrder = self._bulletOrder()
                for bullet in staticBulletOrder:
                    if bullet not in newBulletList:
                        bulletOrder.remove(bullet)
                for b in bulletOrder:
                    bullets = bullets + "* " + b + "...|* ENTER BULLET TEXT *|\n\n"

                hazardBodyPhrase = hazardBodyPhrase + "\n\n" + bullets

        # If segment doesn't pass the checks, put in framing codes
        else:
            hazardBodyPhrase = hazardBodyPhrase + \
                "\n\n|* STATEMENT TEXT GOES HERE *|.\n\n"

        # End code for DR 21310

        #
        # This adds the call to action statements. This is only performed
        # if the segment is 'NEW' or if the previous text has been discarded
        # due to a CAN/EXP/UPG segment
        #

        # remove items from forceCTAList if they exist in foundCTAs. Note
        # that the formats of these lists are different, thus this code
        # is more complicated
        for ent in foundCTAs:
            #only process CTAs that are vtec phen/sig based
            if ent.find('.') == 2:
                phensig = (ent[0:2], ent[3])   #phen.sig
                if phensig in forceCTAList:
                    del forceCTAList[forceCTAList.index(phensig)]

        hazardBodyPhrase = hazardBodyPhrase + '\n\n'
        ctas = []
        for (phen,sig) in forceCTAList:
            hazardPhenSig = phen + "." + sig
            cta = self.defaultCTA(hazardPhenSig)
            if cta not in ctas:
                ctas.append(cta)

        if len(ctas) > 0:
            hazardBodyPhrase = hazardBodyPhrase + \
                               'PRECAUTIONARY/PREPAREDNESS ACTIONS...\n\n'
            for c in ctas:
                hazardBodyPhrase = hazardBodyPhrase +  c + '\n\n'
            hazardBodyPhrase = hazardBodyPhrase + '&&\n\n'

        # Make sure there is only one CAP tag pairs
        hazardBodyPhrase = re.sub(r'&&\s*PRECAUTIONARY/PREPAREDNESS ACTIONS\.\.\.\n', \
                                  "", hazardBodyPhrase)

        return hazardBodyPhrase 

    def finalOverviewText(self):
        #if didn't calculate any, use the default
        if len(self.__overviewText) == 0:

            if self._includeOverviewHeadline:
                overviewHeadline = "...|*OVERVIEW HEADLINE " + \
                  "(must edit)*|...\n\n"
            else:
                overviewHeadline = ""

            if self._includeOverview:
                overviewBody = ".|*OVERVIEW (must edit)*|.\n\n"
            else:
                overviewBody = ""

            #assemble the lines
            overview = overviewHeadline + overviewBody
            return overview

        else:
            return self.__overviewText

    def overviewText(self, hazardList, pil):

        #
        # This method finds an overview in the previous product
        #
        
        overview = ""
        for each in hazardList:
            if (each.has_key('prevOverviewText') and
                each.has_key('pil') and
                each.has_key('endTime') and
                each.has_key('act')):
                if (each['pil'] == pil and
                  each['endTime'] > self._currentTime and
                  each['act'] not in ['CAN', 'EXP']):
                    overview = each['prevOverviewText']
                    self.__overviewText, dummy = self.cleanCapturedText(
                      overview, 0)
                    break

    def useCaptureText(self, hazardList):
        #Based on the hazardlist, returns a tuple indicating:
        # (inc capture text, inc framing codes, skip CTAs, forceCTAList)
        # 
        # For the values to be considered, the 'hdln' value must be 
        # present in the list, or it needs to be a Statement (sig="S")
        cans = ['CAN','UPG','EXP']
        acts = ['NEW','EXT','EXA','EXB','CON']
        foundACTS = 0
        foundCANS = 0
        foundSig = []
        for eh in hazardList:
            if eh['act'] in acts and (len(eh['hdln']) or eh['sig'] == 'S'):
                foundACTS = 1
            if eh['act'] in cans and (len(eh['hdln']) or eh['sig'] == 'S'):
                foundCANS = 1
            if eh['sig'] not in foundSig:
                foundSig.append(eh['sig'])
       
        includeFrameCodes = 0
        includeText = 1
        skipCTAs = 0
        forceCTAList = []

        # all actions are in CAN, UPG, EXP only (don't include text)
        if foundCANS and not foundACTS:
            if 'S' in foundSig and len(foundSig) == 1:   #only S
                includeFrameCodes = 1  #capture text, but frame it
            else: 
                includeText = 0  #end of non statement

        # something in CANS and something in acts (frame it, include text)
        elif foundCANS and foundACTS:
            includeFrameCodes = 1
            skipCTAs = 1
            for eh in hazardList:
                if eh['act'] in acts and \
                  (eh['phen'], eh['sig']) not in forceCTAList and \
                  len(eh['hdln']):
                    forceCTAList.append((eh['phen'], eh['sig']))

        #everything in active entries, captured text is used, but still
        # need to handle the "NEW" entries.
        else:
            for eh in hazardList:
                if eh['act'] in ['NEW'] and len(eh['hdln']):
                    forceCTAList.append((eh['phen'], eh['sig']))

        return (includeText, includeFrameCodes, skipCTAs, forceCTAList)

             

    def cleanCapturedText(self, text, paragraphs, addFramingCodes = False,
      skipCTAs = False):
        #
        # This method takes a block of text, wraps it preserving blank lines,
        # then returns the part after 'paragaphs'. So, if paragraphs is 0, it
        # returns the whole thing, if it's 2, it retunrs paragraphs 2 -> end, etc.
        # Headlines are always removed.
        # Framing codes are added if specified.
        #
        paras = self.convertSingleParas(text)  #single paragraphs

        # keep track of any call to actions found
        foundCTAs = []

        # Process the paragraphs, keep only the interested ones
        paraCount = 0
        processedText = ''
        for eachPara in paras:
            if paraCount >= paragraphs:
               found = self.ctasFound(eachPara)  #get list of ctas found
               if skipCTAs and len(found):
                   pass
               else:
                   processedText = processedText + eachPara + '\n\n'
                   #keep track of remaining CTAs in processed text
                   for f in found:
                       if f not in foundCTAs:
                           foundCTAs.append(f)
            if eachPara.find('...') == 0:
               pass   #ignore headlines
            paraCount = paraCount + 1

        # Add framing codes
        if addFramingCodes:
            processedText = processedText.rstrip()
            processedText = "|*\n" + processedText + "*|\n"

        # Wrap
        processedText = self.endline(processedText, 
          linelength=self._lineLength, breakStr=[" ", "-", "..."])

        return processedText, foundCTAs
           
    def convertSingleParas(self, text):
        #returns a list of paragraphs based on the input text.
        lf = re.compile(r'(s*[^\n])\n([^\n])', re.DOTALL)
        ptext = lf.sub(r'\1 \2', text)
        ptext = ptext.replace('\n\n', '\n')
        paragraphs = ptext.split('\n')
        return paragraphs

    def ctasFound(self, text):
        #returns types of ctas found. The identifier is the pil (e.g., ZFP), 
        #phen/sig (e.g., DU.Y), or GENERIC. Uses the CallToAction definitions.

        #convert text to single paragraphs
        paragraphs = self.convertSingleParas(text)
        for x in xrange(len(paragraphs)):
            paragraphs[x] = string.replace(paragraphs[x],' ','')

        #make list of call to actions   (type, cta text)
        if self.__procCTA is None:
            self.__procCTA = []
            ctao = CallToActions.CallToActions()
            d = ctao.ctaDict()
            for k in d.keys():
                func = d[k]
                items = func()
                for it in items:
                    if type(it) == types.TupleType:
                        it = it[1]  #get second string which is the CTA
                    ctaParas = self.convertSingleParas(it)
                    for cta in ctaParas:
                        self.__procCTA.append((k,string.replace(cta,' ','')))
            d = ctao.ctaPilDict()
            for k in d.keys():
                func = d[k]
                items = func()
                for it in items:
                    if type(it) == types.TupleType:
                        it = it[1]  #get second string which is the CTA
                    ctaParas = self.convertSingleParas(it)
                    for cta in ctaParas:
                        self.__procCTA.append((k,string.replace(cta,' ','')))
           
            ctas = ctao.genericCTAs()
            for it in ctas:
                if type(it) == types.TupleType:
                    it = it[1]  #get second string which is the CTA
                ctaParas = self.convertSingleParas(it)
                for cta in ctaParas:
                    self.__procCTA.append(("GENERIC",
                      string.replace(cta,' ','')))

        #compare
        found = []
        for para in paragraphs:
            for (ctaType, cta) in self.__procCTA:
                ## Added following line to account for framing code issues in CTA
                cta = re.sub("\|\*.*\*\|","",cta)
                if para == cta and ctaType not in found:
                    found.append(ctaType)
        return found

                

