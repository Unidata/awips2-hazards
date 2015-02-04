'''
    Description: A Sample HTML formatter. Currently it outputs
    a html file in '/tmp' directory that can be opened to view the
    sample output. The html code is also outputted to the product
    editor.
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Feb 04, 2015    6322    Robert.Blum Initial creation
'''

import FormatTemplate
import os
import time
import collections

class Format(FormatTemplate.Formatter):

    def execute(self, productDict):
        '''
        Returns an html formatted string.
        @param productDict: dictionary values
        @return: Returns the html string
        '''
        self.productDict = productDict
        return self.createHTMLProduct()

    def createHTMLProduct(self):
        html = "<!DOCTYPE html>\n"
        html += "<html>\n"
        html += self.createHTMLHead()
        html += self.createHTMLBody()
        html += "</html>\n"
        file = self.writeToFile(html)
        return file + '\n\n' + html

    def createHTMLHead(self):
        html =  "<head>\n<script src=\" http://maps.googleapis.com/maps/api/js?key=AIzaSyDY0kkJiTPVd2U7aTOAwhc9ySH6oHxOIYM&sensor=false\"></script>"
        html +=  "<script>\n"
        html +=  "var x=new google.maps.LatLng(41.3, 263.9);"
        
        polygonPointLists = self.getPolygonPointLists()
        for polygon in polygonPointLists:
            idx = 0
            for lon,lat in polygon:
                if (lon < 0):
                    lon = lon + 360
                html +=  'var point' +  str(idx) + '=new google.maps.LatLng(' + str(lat) + ',' + str(lon) + ');'
                idx = idx + 1;
                
        html +=  "function initialize() {\n"
        html +=  "var mapProp = {\n"
        html +=  " center:x, zoom:7, mapTypeId: google.maps.MapTypeId.ROADMAP };\n"

        html +=  "var map=new google.maps.Map(document.getElementById(\"googleMap\"), mapProp);\n"

        html +=  "var points=[\n"
        
        for i in range(idx):
            html += 'point' + str(i)
            if i < idx - 1:
                html += ','
        
        html += "];"
        html +=  "var polygon=new google.maps.Polygon({ path:points, strokeColor:\"#0000FF\",\n"
        html +=  " strokeOpacity:0.8, strokeWeight:2, fillColor:\"#0000FF\", fillOpacity:0.4 });\n"
        html +=  "polygon.setMap(map); }\n"
        html +=  "google.maps.event.addDomListener(window, 'load', initialize);\n"
        html +=  "</script></head>\n"
        return html

    def createHTMLBody(self):
        html = "<body>\n"
        html += self.createHTMLTable(html)
        html +=  "</body>"
        return html

    def createHTMLTable(self, html):
        html += "<table border=\"1\" style=\"width:100%\">\n"
        # First Row is just the productName in a Header
        html += "<tr>\n"
        html += "<th colspan=\"2\">" + self.productDict['productName'] + "</th>\n"
        html += "</tr>\n"
        # Second Row is the LegacyText and googleMap
        html += "<tr>\n"
        html += "<td rowspan=\"2\">\n"
        # Add the legacy text using <pre> to retain formatting
        html +=  "<pre>" + self.getLegacyText() + "</pre>\n"
        html +=  "</td>\n"
        impactedLocationsHTML = self.addImpactedLocationsToTable()
        # Add the google map
        if impactedLocationsHTML:
            html += "<td>\n"
            html += "<div id=\"googleMap\" style=\"width:800px;height:580px;\"></div>\n"
            html +=  "</td>\n"
            html += "</tr>\n"
            #Add 3rd row for locations
            html += impactedLocationsHTML
        else:
            # No locations so span 2 rows
            html += "<td rowspan=\"2\">\n"
            html += "<div id=\"googleMap\" style=\"width:800px;height:580px;\"></div>\n"
            html +=  "</td>\n"
            html += "</tr>\n"
        html += "</table>\n"
        return html

    def getLegacyText(self):
        productCategory = self.productDict.get('productCategory')
        # Added try block since ESF Formatter is not in baseline yet.
        try:
            # Import the correct legacy formatter
            if productCategory == 'FFA':
                import Legacy_FFA_Formatter
                legacyFormatter = Legacy_FFA_Formatter.Format()
            elif productCategory == 'FFW_FFS':
                import Legacy_FFW_FFS_Formatter
                legacyFormatter = Legacy_FFW_FFS_Formatter.Format()
            elif productCategory == 'FLW_FLS':
                import Legacy_FLW_FLS_Formatter
                legacyFormatter = Legacy_FLW_FLS_Formatter.Format()
            elif productCategory == 'ESF':
                import Legacy_ESF_Formatter
                legacyFormatter = Legacy_ESF_Formatter.Format()
            else:
                return 'There is no product formatter for this hazard type: ' + productCategory
            legacyText = legacyFormatter.execute(self.productDict)
        except:
            legacyText = 'Error running legacy formatter.'
        return legacyText

    def getPolygonPointLists(self):
        polygonPointLists = []
        segments = self.productDict.get('segments')
        for segment in segments:
            sections = segment.get('sections')
            for section in sections:
                for geometry in section.get('geometry'):
                    polygonPointLists.append(list(geometry.exterior.coords))
        return polygonPointLists

    def addImpactedLocationsToTable(self):
        addToTable = False
        html = ''
        cityList = []
        # Get all the cities from the sections
        for segment in self.productDict.get('segments'):
            for section in segment.get('sections'):
                if section.get('citiesListFlag', False):
                    addToTable = True
                    cityList.extend(section.get('cityList'))
        
        if addToTable:
            html += "<tr>\n"
            html += "<td>\n"
            html += "<pre> Impacted cities include: </pre>\n"
            # Create the html list
            html += "<ul style=\"list-style-type:disc\">\n"
            for city in cityList:
                html += "<li>" + city + "</li>\n"
            html += "</ul>\n" 
            html += "</td>\n"
            html += "</tr>\n"
        return html 

    def writeToFile(self, html):
        seconds = int(time.time())
        file = '/tmp/' + self.productDict.get('productCategory') + '_' + str(seconds) + '.html'
        f = open(file, 'w')
        f.write(html)
        f.close()
        return file
