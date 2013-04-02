# routines to convert data to XML for the IHISdb.

import xml
from xml.etree.cElementTree import Element, SubElement
from xml.etree import ElementTree

class xmlUtil:

    # constructor
    def __init__(self):
        pass

    #-----------------------------------------------------------------------
    # Map and Polygon Routines
    #-----------------------------------------------------------------------

    # mapType
    def mapType(self, maptype, atts):
        root = Element('ihis')
        ele = SubElement(root, "map_type")
        if maptype is not None:
            ele.text = maptype
        self._addAttributes(ele, atts)
        return ElementTree.tostring(root)

    # mapBasename
    def mapBasename(self, basename, atts):
        root = Element('ihis')
        ele = SubElement(root, "map_basename")
        if basename is not None:
            ele.text = basename
        self._addAttributes(ele, atts)
        return ElementTree.tostring(root)

    # get UGCs (for all UGC requests). Polygon is [x,y,x,y,x,y,...]
    def ugcs(self, ugcs, atts, polygon=None):
        root = Element('ihis')
        ele = SubElement(root, "ugcs")
        self._addAttributes(ele, atts)
        if ugcs is not None:
            for ugc in ugcs:
                ugcE = SubElement(ele, "ugc_code")
                ugcE.text = ugc

        # if polygon provided, output it, we only have 1 polygon, and we
        # assume it is inclusive.
        if polygon is not None:
            self._encodePolygons(root, [(True, polygon)])
        return ElementTree.tostring(root)

    # get map polygons
    # format of data: [(bbox, [(inc,data), (inc,data), ...], atts),...]
    def mapPolygons(self, polygons, atts):
        att = {}
        att['xmlns'] = "http://www.opengis.net/kml/2.2" 
        root = Element('kml')
        self._addAttributes(root,att)
        doc = SubElement(root,"Documemt")
        folder = SubElement(doc,"Folder")
        name = SubElement(folder,"name")
        name.text = "IHIS Frame"

        # for each polygon in the list
        for bbox, polygonData, shapeAtts in polygons:
            mark = SubElement(folder,"Placemark")
     
            keys = shapeAtts.keys()
            for key in keys:
                keye = SubElement(mark, key)
                keye.text = str(shapeAtts[key])

            bbe = SubElement(mark, "bounding_box")
            boundBox = {"minx": bbox[0], "miny": bbox[1], "maxx": bbox[2],
              "maxy": bbox[3]}
            self._addAttributes(bbe, boundBox)

        #    polygonsE = SubElement(pge, "polygons")
            self._encodePolygons(mark, polygonData)
        return ElementTree.tostring(root)


    # encode Polygon with KML.  This is for multiple polygons in the format
    # of [(inc, data), (inc, data), ...]
    def _encodePolygons(self, root, polygons):
        pgE = SubElement(root, "Polygon")
        se = SubElement(pgE, "extrude")
        se.text = "1"
        ame = SubElement(pgE, "altitudeMode")
        ame.text = "relativeToGround"
        for incFlag, polygon in polygons:
            if incFlag:
                ore = SubElement(pgE, "outerBoundaryIs")
            else:
                ore = SubElement(pgE, "innerBoundaryIs")
            lre = SubElement(ore, "LinearRing")
            ce = SubElement(lre, "coordinates")

            #according to the KML documentation, coordinates must follow the
            #right-hand rule, i.e., coordinates specified in counterclockwise
            #order.
            if self._clockwise(polygon):
                polygon = self._reversePolygon(polygon)
            text = ""
            polygon = self.reducePolygon(polygon)
            for i in xrange(0, len(polygon), 2):
                text = text + str(polygon[i]) + "," + str(polygon[i+1]) + "\n"
            ce.text = text

    # Returns a list of reduced polygon points (lon/lat),
    # given a list of lon/lat points
    def reducePolygon(self, polygon):

        xDelta = 0.01
        yDelta = 0.01

        pointList = []
        lastPointx = polygon[0]
        lastPointy = polygon[1]
        pointList.append(lastPointx)
        pointList.append(lastPointy)
        for i in xrange(0,len(polygon),2):
            xDiff = abs(polygon[i] - lastPointx)
            yDiff = abs(polygon[i+1] - lastPointy)

            if xDiff > xDelta and xDiff > yDelta or \
               abs(xDiff - yDiff) > 0.01:
                pointList.append(polygon[i])
                pointList.append(polygon[i+1])
                lastPointx = polygon[i]  # update only when we save the point
                lastPointy = polygon[i+1]

        return pointList


    # reverse polygon, [x,y,x,y,x,y,...] is reversed to [x,y,x,y,x,y,...]
    def _reversePolygon(self, polygon):
        revPolygon = []
        # reverse it, can't use reverse() since these are x,y,x,y,...
        for x in xrange(len(polygon)-2, -2, -2): 
            revPolygon.append(polygon[x])
            revPolygon.append(polygon[x+1])
        return revPolygon

    # clockwise routine for polygon.  Returns true if coordinates are
    # in a clockwise direction.   NOTE: Similar code as in Shapefile.py.
    # Code duplication is BAD.
    def _clockwise(self, polygon): 
        count = 0.0
        for i in xrange(0, len(polygon), 2):
            if i+2 == len(polygon):
                j = 0
            else:
                j = i+2
            count = count + (polygon[i] * polygon[j+1]) \
              - (polygon[i+1] * polygon[j])
        if count > 0.0:
            return False
        else:
            return True

    #-----------------------------------------------------------------------
    # Allowed Hazard Routines
    #-----------------------------------------------------------------------

    # encodeProducts, given a product list, encode it.
    def products(self, productList, atts):
        root = Element('ihis')
        ele = SubElement(root, "product_categories")
        self._addAttributes(ele, atts)
        for product in productList:
            pe = SubElement(ele, "product_category")
            pe.text = product
        return ElementTree.tostring(root)

    # encode hazard codes, with attributes
    def hazardCodes(self, codes, atts): 
        root = Element('ihis')
        ele = SubElement(root, "hazard_codes")
        self._addAttributes(ele, atts)
        if codes is not None:
            for code in codes:
                pe = SubElement(ele, "hazard_code")
                pe.text = code
        return ElementTree.tostring(root)

    # encode hazard details, with attributes
    def hazardDetails(self, details, atts):
        root = Element('ihis')
        ele = SubElement(root, "hazard_details")
        self._addAttributes(ele, atts)
        if details is not None:
            for hazCode, actions, category in details:
                pe = SubElement(ele, "hazard_detail")
                hazCodeE = SubElement(pe, "hazard_code")
                hazCodeE.text = hazCode
                ae = SubElement(pe, "action_codes")
                for action in actions:
                    ase = SubElement(ae, "action_code")
                    ase.text = action
                ce = SubElement(pe, "hazard_category")
                ce.text = category

        return ElementTree.tostring(root)

    #-----------------------------------------------------------------------
    # Hazard Database Routines (VTEC and HazardDict)
    #-----------------------------------------------------------------------

    # encoding vtec and haz dict entries
    def vtecHazDict(self, data, atts, **kv):
        mergedOption = kv.get('mergedRecords', 'separate')

        # set up formatting for vtec fields
        vtecTimeFields = ['start','end','issueTime','purgeTime',
          'previousStart', 'previousEnd']
        vtecSkipFields = []
        vtecMappings = {'id': ('ugcs','ugc')}

        # set up formatting for hazDict fields
        hazDictSkipFields = ['ugcs']
        hazDictTimeFields = ['startTime', 'endTime', 'currentTime']
        hazDictMappings = {'polygonPoints': ('polygonPoint', 'lonlat')}

        vtecRecords, hazDictRecords = data

        root = Element('ihis')
        ele = SubElement(root, "hazard_entries")
        self._addAttributes(ele, atts)

        # keep records separate from each other
        if mergedOption == "separate":
            # vtec records
            if vtecRecords is not None:
                vtecE = SubElement(ele, "vtec_entries")
                for vtecRec in vtecRecords:
                    vRecE = SubElement(vtecE, "vtec_record")
                    self._addElements(vRecE, vtecRec, vtecMappings, 
                      vtecTimeFields, vtecSkipFields)

            # hazardDict records
            if hazDictRecords is not None:
                hazE = SubElement(ele, "hazardDict_entries")
                keys = hazDictRecords.keys()
                for key in keys:
                    hazRecE = SubElement(hazE, "hazardDict_record")
                    self._addAttributes(hazRecE, {'hazard_sequence': key})
                    self._addElements(hazRecE, hazDictRecords[key], 
                      hazDictMappings, hazDictTimeFields, hazDictSkipFields)

   
        # merge hazDict records into vtec records (should only be one
        # hazDict record possible per vtec record)
        elif mergedOption == "VTEC":
            # vtec records are priority
            if vtecRecords is not None:
                vtecE = SubElement(ele, "vtec_entries")
                for vtecRec in vtecRecords:
                    # handle the main part of each vtec record
                    vRecE = SubElement(vtecE, "vtec_record")
                    self._addElements(vRecE, vtecRec, vtecMappings, 
                      vtecTimeFields, vtecSkipFields)

                    # extract the hazard sequence and get the hazardDict
                    # record.
                    hazSequence = vtecRec.get('hazardSequence', None)
                    hazE = SubElement(vRecE, "hazardDict_entries")
                    if hazSequence is not None and hazDictRecords is not None:
                        hazRec = hazDictRecords.get(hazSequence, None)
                        if hazRec is not None:
                            hazRecE = SubElement(hazE, "hazardDict_record")
                            self._addElements(hazRecE, hazRec, 
                              hazDictMappings, hazDictTimeFields, 
                              hazDictSkipFields)

        # merge vtec records into hazDict records (could be 0 or more vtec
        # records possible per hazDict record)
        elif mergedOption == "HazardDict":                            
            # Hazard Dict records are priority
            if hazDictRecords is not None:
                hazE = SubElement(ele, "hazardDict_entries")
                keys = hazDictRecords.keys()
                for key in keys:
                    hazRecE = SubElement(hazE, "hazardDict_record")
                    self._addAttributes(hazRecE, {'hazard_sequence': key})
                    self._addElements(hazRecE, hazDictRecords[key], 
                      hazDictMappings, hazDictTimeFields, hazDictSkipFields)
                    vtecE = SubElement(ele, "vtec_entries")
                    if vtecRecords is not None:
                        for vtecRec in vtecRecords:
                            hazSeq = vtecRec.get('hazardSequence', None)
                            if key == hazSeq:
                               vRecE = SubElement(vtecE, "vtec_record")
                               self._addElements(vRecE, vtecRec, vtecMappings, 
                                 vtecTimeFields, vtecSkipFields)

        return ElementTree.tostring(root)
            

    #-----------------------------------------------------------------------
    # Common Routines
    #-----------------------------------------------------------------------

    # general status message return value
    def returnStatus(self, status, messageString = None):
        root = Element('ihis')
        ele = SubElement(root, "status")
        ele.text = str(status)
        if messageString is not None:
            msgE = SubElement(ele, "message")
            msgE.text = str(messageString)
        return ElementTree.tostring(root)
        

    # adds attributes to the given element.  Atts is a dictionary.
    def _addAttributes(self, element, atts):
        if atts is not None:
            keys = atts.keys()
            for key in keys:
                if atts[key] is not None:
                    element.set(key, str(atts[key]))

    # adds elements and values to the given element.  Atts is a dictionary.
    # Mappings is an optional dictionary of key (within the atts) and the 
    # desired subelement name and individual value names. Example:
    # 'id' field is a list of ugcs with ugcs in the list: 'id':('ugcs','ugc').
    # TimeFields are those fields that represent time, which are output
    # with the time in sec since epoch, and also with iso time representation.
    def _addElements(self, element, atts, mappings={}, timeFields=[],
      skipFields=[]):
        if atts is not None:
            keys = atts.keys()
            for key in keys:
                if key in skipFields:
                    continue   #don't want this in the output XML
                value = atts[key]
                if value is not None:
                    elementMappings = mappings.get(key, key)
                    if type(elementMappings) in [tuple, list]:
                        elementName = elementMappings[0]
                        subElementName = elementMappings[1]
                    else:
                        elementName = elementMappings
                        subElementName = "item"
                    subE = SubElement(element, elementName)
                    if type(value) in [tuple, list]:
                        for indValue in value:
                            indE = SubElement(subE, subElementName)
                            if key in timeFields:
                                self._encodeTimeField(indE, value)
                            else:
                                indE.text = str(indValue)
                    else:
                        if key in timeFields:
                            self._encodeTimeField(subE, value)
                        else:
                            subE.text = str(value)

    # encodes time fields by creating two subElements, one for epoch-seconds
    # and the other iso-time.
    def _encodeTimeField(self, element, value):
        tE = SubElement(element, 'epoch_seconds')
        tE.text = str(value)
        isoE = SubElement(element, 'isotime')
        isoE.text = self._convertToIsoTime(value)

    # convert to iso time, returns str representing the iso time
    def _convertToIsoTime(self, t):
        import datetime
        isoT = datetime.datetime.utcfromtimestamp(t)
        return isoT.isoformat()
   
