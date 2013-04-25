import xml.etree.ElementTree as ET
"""
Description: Class for returning a KML representation of a storm track

SOFTWARE HISTORY
Date         Ticket#    Engineer    Description
------------ ---------- ----------- --------------------------
Mar 05, 2013            Tracy.L.Hansen      Initial creation

@author Tracy.L.Hansen@noaa.gov
@version 1.0
"""
class KML:
    def __init__(self, root):
        self._root = root

#############################
#### KML creation methods

    def root(self, name=""):
        root = ET.Element("kml")
        root.set("xmlns", "http://www.opengis.net/kml/2.2")
        root.text = "\n" # for indent format
        doc = self.makeSubElement(root, "Document")
        folder = self.makeSubElement(doc, "Folder")
        folder.name = name
        return root, folder

    def makeSubElement(self, parent, name, text="\n", tail="\n"):
        ele = ET.SubElement(parent, name)
        ele.text = text
        ele.tail = tail
        return ele
        
    def createStormTrackPoint(self, point):
        root, folder = self.root()
        self.createPoint(folder, point)
        #print "KML.py createStormTrackPoint: ", ET.tostring(root)
        return ET.tostring(root)
    
    def createStormTrackPolygon(self, polyDict):
        root, folder = self.root()
        self.createPolygon(folder, polyDict)
        #print "KML.py createStormTrackPolygon: ", ET.tostring(root)
        return ET.tostring(root)
    
    def createStormTrack(self, points, polygon):
        root, folder = self.root()
        self.createPolygon(folder, polygon, reduce=False)
        self.createTrackPoints(folder, points)
        print "KML.py createStormTrack: ", ET.tostring(root)
        return ET.tostring(root)        

    def createTrackPoints(self, root, points):
        """
        Creates the following KML for a set of track points:
        
        <Placemark>
           <time>1281984600000</time>
             <Point>
              <coordinates>-104.6315608072117,39.872935860249797</coordinates>
            </Point>
        </Placemark>
        """
        for point, tSec in points:            
            pm = self.makeSubElement(root, "Placemark", text="\n ", tail="\n")
            self.createTime(pm, "time", tSec)
            self.createPoint(pm, point)

    def createPoint(self, root, point):
        """
        Creates the following KML:
        <Point>
            <coordinates>-104.6315608072117,39.872935860249797</coordinates>
        </Point>
        """
        p = self.makeSubElement(root, "Point", text="\n        ", tail="\n")
        c = self.makeSubElement(p, "coordinates", text=`point.lon` + "," + `point.lat`, tail="\n  ")
        return p
    
    def createTime(self, root, label, tSec):
        # Convert from seconds to ms
        tStr = `tSec*1000`
        tStr = tStr.replace('L', "")
        s = self.makeSubElement(root, label, text=tStr, tail="\n  ")
        return s

    def createPolygon(self, root, polyDict, reduce=True):
        """
        Creates the following KML representing a polygon:
        
        <Placemark>
            <Polygon>
               <extrude>1</extrude>
               <altitudeMode>relativeToGround</altitudeMode>
               <outerBoundaryIs>
                 <LinearRing>
                   <coordinates>
                     -103.83,39.74
                     -103.78,39.61
                     -104.05,39.57
                     -104.06,39.67
                   </coordinates>
                 </LinearRing>
               </outerBoundaryIs>
            </Polygon>
        </Placemark>    
        """
        pm = self.makeSubElement(root, "Placemark", text="\n ", tail="\n")
        #extrude = self.makeSubElement(pm, "extrude", text='1', tail='\n      ')
        #altitude = self.makeSubElement(pm, "altitudeMode", text="relativeToGround", tail="\n      ")
        polygon = self.makeSubElement(pm, "Polygon", text='\n     ', tail='\n      ')
        extrude = self.makeSubElement(polygon, "extrude", text='1', tail='\n      ')
        altitude = self.makeSubElement(polygon, "altitudeMode", text="relativeToGround", tail="\n      ")
        ring = None
        for boundary in polyDict.keys():
            exec boundary + ' = ET.SubElement(polygon, "'+\
                 boundary+'BoundaryIs")'
            exec boundary + '.text = "\\n        "'
            if boundary == polyDict.keys()[0]:
                exec boundary + '.tail = "\\n      "'
            else:
                exec boundary + '.tail = "\\n    "'
            exec 'ring = ET.SubElement('+boundary+', "LinearRing")'
            ring.text = "\n          "
            ring.tail = "\n      "
            coord = ET.SubElement(ring, "coordinates")
            coord.text = "\n            "
            coord.tail = "\n        "

            for poly in polyDict[boundary]:
                if reduce:
                    poly = self.reducePolygon(poly)
                for (x, y) in poly:
                    coord.text += str(x) + ',' + str(y) + '\n            '
            coord.text = coord.text[:-2]                  


