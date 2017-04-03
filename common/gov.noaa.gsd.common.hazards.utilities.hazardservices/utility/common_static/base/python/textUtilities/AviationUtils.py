'''
Utility for Aviation Products
'''
import shapely
import EventFactory, EventSetFactory, GeometryFactory
import AdvancedGeometry
import numpy as np
import datetime, math
from math import sin, cos, sqrt, atan2, radians, pi
import time
import shapely.ops as so
import os, sys
import matplotlib
from matplotlib import path as mPath
from scipy import ndimage
from shapely.geometry import Polygon
from scipy.io import netcdf
from collections import defaultdict
from shutil import copy2
import HazardDataAccess
import TimeUtils
from VisualFeatures import VisualFeatures
import Domains
import csv

class AviationUtils:
    
    def getHeightVal(self, arrayVal):
        heightDict = {1000.0: "010",1100.0: "011",1200.0: "012",1300.0: "013",1400.0: "014",
                      1500.0: "015",1600.0: "016",1700.0: "017",1800.0: "018",1900.0: "019",
                      2000.0: "020",2100.0: "021",2200.0: "022",2300.0: "023",2400.0: "024",
                      2500.0: "025",2600.0: "026",2700.0: "027",2800.0: "028",2900.0: "029",
                      3000.0: "030",3100.0: "031",3200.0: "032",3300.0: "033",3400.0: "034",
                      3500.0: "035",3600.0: "036",3700.0: "037",3800.0: "038",3900.0: "039",
                      4000.0: "040",4100.0: "041",4200.0: "042",4300.0: "043",4400.0: "044",
                      4500.0: "045",4600.0: "046",4700.0: "047",4800.0: "048",4900.0: "049",
                      5000.0: "050",5100.0: "051",5200.0: "052",5300.0: "053",5400.0: "054",
                      5500.0: "055",5600.0: "056",5700.0: "057",5800.0: "058",5900.0: "059",
                      6000.0: "060",6100.0: "061",6200.0: "062",6300.0: "063",6400.0: "064",
                      6500.0: "065",6600.0: "066",6700.0: "067",6800.0: "068",6900.0: "069",
                      7000.0: "070",7100.0: "071",7200.0: "072",7300.0: "073",7400.0: "074",
                      7500.0: "075",7600.0: "076",7700.0: "077",7800.0: "078",7900.0: "079",
                      8000.0: "080",8100.0: "081",8200.0: "082",8300.0: "083",8400.0: "084",
                      8500.0: "085",8600.0: "086",8700.0: "087",8800.0: "088",8900.0: "089",
                      9000.0: "090",9100.0: "091",9200.0: "092",9300.0: "093",9400.0: "094",
                      9500.0: "095",9600.0: "096",9700.0: "097",9800.0: "098",9900.0: "099",
                      10000.0: "100",10100.0: "101",10200.0: "102",10300.0: "103",10400.0: "104",
                      10500.0: "105",10600.0: "106",10700.0: "107",10800.0: "108",10900.0: "109",
                      11000.0: "110",11100.0: "111",11200.0: "112",11300.0: "113",11400.0: "114",
                      11500.0: "115",11600.0: "116",11700.0: "117",11800.0: "118",11900.0: "119",
                      12000.0: "120",12100.0: "121",12200.0: "122",12300.0: "123",12400.0: "124",
                      12500.0: "125",12600.0: "126",12700.0: "127",12800.0: "128",12900.0: "129",
                      13000.0: "130",13100.0: "131",13200.0: "132",13300.0: "133",13400.0: "134",
                      13500.0: "135",13600.0: "136",13700.0: "137",13800.0: "138",13900.0: "139",
                      14000.0: "140",14100.0: "141",14200.0: "142",14300.0: "143",14400.0: "144",
                      14500.0: "145",14600.0: "146",14700.0: "147",14800.0: "148",14900.0: "149",
                      15000.0: "150",15100.0: "151",15200.0: "152",15300.0: "153",15400.0: "154",
                      15500.0: "155",15600.0: "156",15700.0: "157",15800.0: "158",15900.0: "159",
                      16000.0: "160",16100.0: "161",16200.0: "162",16300.0: "163",16400.0: "164",
                      16500.0: "165",16600.0: "166",16700.0: "167",16800.0: "168",16900.0: "169",
                      17000.0: "170",17100.0: "171",17200.0: "172",17300.0: "173",17400.0: "174",
                      17500.0: "175",17600.0: "176",17700.0: "177",17800.0: "178",17900.0: "179",
                      18000.0: "FL180",18100.0: "FL181",18200.0: "FL182",18300.0: "FL183",18400.0: "FL184",
                      18500.0: "FL185",18600.0: "FL186",18700.0: "FL187",18800.0: "FL188",18900.0: "FL189",
                      19000.0: "FL190",19100.0: "FL191",19200.0: "FL192",19300.0: "FL193",19400.0: "FL194",
                      19500.0: "FL195",19600.0: "FL196",19700.0: "FL197",19800.0: "FL198",19900.0: "FL199",
                      20000.0: "FL200",20100.0: "FL201",20200.0: "FL202",20300.0: "FL203",20400.0: "FL204",
                      20500.0: "FL205",20600.0: "FL206",20700.0: "FL207",20800.0: "FL208",20900.0: "FL209",
                      21000.0: "FL210",21100.0: "FL211",21200.0: "FL212",21300.0: "FL213",21400.0: "FL214",
                      21500.0: "FL215",21600.0: "FL216",21700.0: "FL217",21800.0: "FL218",21900.0: "FL219",
                      22000.0: "FL220",22100.0: "FL221",22200.0: "FL222",22300.0: "FL223",22400.0: "FL224",
                      22500.0: "FL225",22600.0: "FL226",22700.0: "FL227",22800.0: "FL228",22900.0: "FL229",
                      23000.0: "FL230",23100.0: "FL231",23200.0: "FL232",23300.0: "FL233",23400.0: "FL234",
                      23500.0: "FL235",23600.0: "FL236",23700.0: "FL237",23800.0: "FL238",23900.0: "FL239",
                      24000.0: "FL240",24100.0: "FL241",24200.0: "FL242",24300.0: "FL243",24400.0: "FL244",
                      24500.0: "FL245",24600.0: "FL246",24700.0: "FL247",24800.0: "FL248",24900.0: "FL249",
                      25000.0: "FL250",25100.0: "FL251",25200.0: "FL252",25300.0: "FL253",25400.0: "FL254",
                      25500.0: "FL255",25600.0: "FL256",25700.0: "FL257",25800.0: "FL258",25900.0: "FL259",
                      26000.0: "FL260",26100.0: "FL261",26200.0: "FL262",26300.0: "FL263",26400.0: "FL264",
                      26500.0: "FL265",26600.0: "FL266",26700.0: "FL267",26800.0: "FL268",26900.0: "FL269",
                      27000.0: "FL270",27100.0: "FL271",27200.0: "FL272",27300.0: "FL273",27400.0: "FL274",
                      27500.0: "FL275",27600.0: "FL276",27700.0: "FL277",27800.0: "FL278",27900.0: "FL279",
                      28000.0: "FL280",28100.0: "FL281",28200.0: "FL282",28300.0: "FL283",28400.0: "FL284",
                      28500.0: "FL285",28600.0: "FL286",28700.0: "FL287",28800.0: "FL288",28900.0: "FL289",
                      29000.0: "FL290",29100.0: "FL291",29200.0: "FL292",29300.0: "FL293",29400.0: "FL294",
                      29500.0: "FL295",29600.0: "FL296",29700.0: "FL297",29800.0: "FL298",29900.0: "FL299",
                      30000.0: "FL300",30100.0: "FL301",30200.0: "FL302",30300.0: "FL303",30400.0: "FL304",
                      30500.0: "FL305",30600.0: "FL306",30700.0: "FL307",30800.0: "FL308",30900.0: "FL309",
                      31000.0: "FL310",31100.0: "FL311",31200.0: "FL312",31300.0: "FL313",31400.0: "FL314",
                      31500.0: "FL315",31600.0: "FL316",31700.0: "FL317",31800.0: "FL318",31900.0: "FL319",
                      32000.0: "FL320",32100.0: "FL321",32200.0: "FL322",32300.0: "FL323",32400.0: "FL324",
                      32500.0: "FL325",32600.0: "FL326",32700.0: "FL327",32800.0: "FL328",32900.0: "FL329",
                      33000.0: "FL330",33100.0: "FL331",33200.0: "FL332",33300.0: "FL333",33400.0: "FL334",
                      33500.0: "FL335",33600.0: "FL336",33700.0: "FL337",33800.0: "FL338",33900.0: "FL339",
                      34000.0: "FL340",34100.0: "FL341",34200.0: "FL342",34300.0: "FL343",34400.0: "FL344",
                      34500.0: "FL345",34600.0: "FL346",34700.0: "FL347",34800.0: "FL348",34900.0: "FL349",
                      35000.0: "FL350",35100.0: "FL351",35200.0: "FL352",35300.0: "FL353",35400.0: "FL354",
                      35500.0: "FL355",35600.0: "FL356",35700.0: "FL357",35800.0: "FL358",35900.0: "FL359",
                      36000.0: "FL360",36100.0: "FL361",36200.0: "FL362",36300.0: "FL363",36400.0: "FL364",
                      36500.0: "FL365",36600.0: "FL366",36700.0: "FL367",36800.0: "FL368",36900.0: "FL369",
                      37000.0: "FL370",37100.0: "FL371",37200.0: "FL372",37300.0: "FL373",37400.0: "FL374",
                      37500.0: "FL375",37600.0: "FL376",37700.0: "FL377",37800.0: "FL378",37900.0: "FL379",
                      38000.0: "FL380",38100.0: "FL381",38200.0: "FL382",38300.0: "FL383",38400.0: "FL384",
                      38500.0: "FL385",38600.0: "FL386",38700.0: "FL387",38800.0: "FL388",38900.0: "FL389",
                      39000.0: "FL390",39100.0: "FL391",39200.0: "FL392",39300.0: "FL393",39400.0: "FL394",
                      39500.0: "FL395",39600.0: "FL396",39700.0: "FL397",39800.0: "FL398",39900.0: "FL399",                      
                      40000.0: "FL400",40100.0: "FL401",40200.0: "FL402",40300.0: "FL403",40400.0: "FL404",
                      40500.0: "FL405",40600.0: "FL406",40700.0: "FL407",40800.0: "FL408",40900.0: "FL409",
                      41000.0: "FL410",41100.0: "FL411",41200.0: "FL412",41300.0: "FL413",41400.0: "FL414",
                      41500.0: "FL415",41600.0: "FL416",41700.0: "FL417",41800.0: "FL418",41900.0: "FL419",
                      42000.0: "FL420",42100.0: "FL421",42200.0: "FL422",42300.0: "FL423",42400.0: "FL424",
                      42500.0: "FL425",42600.0: "FL426",42700.0: "FL427",42800.0: "FL428",42900.0: "FL429",
                      43000.0: "FL430",43100.0: "FL431",43200.0: "FL432",43300.0: "FL433",43400.0: "FL434",
                      43500.0: "FL435",43600.0: "FL436",43700.0: "FL437",43800.0: "FL438",43900.0: "FL439",
                      44000.0: "FL440",44100.0: "FL441",44200.0: "FL442",44300.0: "FL443",44400.0: "FL444",
                      44500.0: "FL445",44600.0: "FL446",44700.0: "FL447",44800.0: "FL448",44900.0: "FL449",
                      45000.0: "FL450",45100.0: "FL451",45200.0: "FL452",45300.0: "FL453",45400.0: "FL454",
                      45500.0: "FL455",45600.0: "FL456",45700.0: "FL457",45800.0: "FL458",45900.0: "FL459",
                      46000.0: "FL460",46100.0: "FL461",46200.0: "FL462",46300.0: "FL463",46400.0: "FL464",
                      46500.0: "FL465",46600.0: "FL466",46700.0: "FL467",46800.0: "FL468",46900.0: "FL469",
                      47000.0: "FL470",47100.0: "FL471",47200.0: "FL472",47300.0: "FL473",47400.0: "FL474",
                      47500.0: "FL475",47600.0: "FL476",47700.0: "FL477",47800.0: "FL478",47900.0: "FL479",
                      48000.0: "FL480",48100.0: "FL481",48200.0: "FL482",48300.0: "FL483",48400.0: "FL484",
                      48500.0: "FL485",48600.0: "FL486",48700.0: "FL487",48800.0: "FL488",48900.0: "FL489",
                      49000.0: "FL490",49100.0: "FL491",49200.0: "FL492",49300.0: "FL493",49400.0: "FL494",
                      49500.0: "FL495",49600.0: "FL496",49700.0: "FL497",49800.0: "FL498",49900.0: "FL499",
                      50000.0: "FL500"}

        return heightDict[arrayVal]
    
    def createFreezingLevelInformation(self):
        import matplotlib.pyplot as plt
        
        grid = np.load('/home/nathan.hardin/Desktop/GFEtoHS/GFEGrids/freezingLevelData.npz')
        lats = grid['lat']
        lons = grid['lon']
        t9Grid = grid['t9Grid']
        t12Grid = grid['t12Grid']
        t15Grid = grid['t15Grid']
        
        #find minimum of three times
        gridMin = np.minimum(t9Grid,t12Grid)
        gridMin = np.minimum(gridMin,t15Grid)
        
        minimum = np.amin(gridMin)
        maximum = np.amax(gridMin)
        
        minimum = int(math.ceil(minimum/100.0))*100
        maximum = int(math.ceil(maximum/100.0))*100
        
        minimum = self.getHeightVal(minimum)
        maximum = self.getHeightVal(maximum)
        
        freezingLevelInformation = str(minimum) + '-' + str(maximum)        
        
        return freezingLevelInformation
    
    def createVolcanoDict(self):
        f = open(self.volcanoFilePath(), 'rb')
        reader = csv.reader(f)
        headers = reader.next()
        column = {}
        for h in headers:
            column[h] = []
        
        for row in reader:
            for h, v in zip(headers, row):
                column[h].append(v)
        
        volcanoName = column['VolcanoName']
        volcanoLatitude = column['LatitudeDecimal']
        volcanoLongitude = column['LongitudeDecimal']
        volcanoNumber = column['VolcanoNumber']
        
        volcanoDict = {}
        for i in range(0,len(volcanoName)):
            volcanoDict[volcanoName[i]] = [volcanoNumber[i], volcanoLatitude[i], volcanoLongitude[i]]        
        
        return volcanoDict    
        
    def getGeometryType(self, hazardEvent):        
        for g in hazardEvent.getFlattenedGeometry():
            geomType = g.geom_type           
        
        return geomType
    
    def updateVisualFeatures(self, event, vertices, polyPoints):
        startTime = event.getStartTime().replace(second=0, microsecond=0)
        startTime = startTime - datetime.timedelta(hours=2)
        endTime = TimeUtils.roundDatetime(event.getEndTime())
        
        self._originalGeomType = event.get('originalGeomType')
        self._width = event.get('convectiveSigmetWidth')

        polygonArea = self.polygonArea(event, self._originalGeomType, self._width)
        label = self.createLabel(event, polygonArea)
        
        eventID = event.getEventID()
        selectedFeatures = []
        VOR_points = event.getHazardAttributes().get('VOR_points')
               
        if self._originalGeomType != 'Polygon':
            poly = GeometryFactory.createPolygon(polyPoints)
            basePoly = vertices
            if self._originalGeomType == 'Point':
                basePoly = GeometryFactory.createPoint(basePoly)
            elif self._originalGeomType == 'LineString':
                basePoly = GeometryFactory.createLineString(basePoly)
            basePoly = AdvancedGeometry.createShapelyWrapper(basePoly, 0)       
        else:
            poly = GeometryFactory.createPolygon(VOR_points)
            try:
                basePoly = AdvancedGeometry.createShapelyWrapper(GeometryFactory.createPolygon(vertices), 0)
            except ValueError:
                basePoly = event.getGeometry()
        event.setGeometry(basePoly)
            
        poly = AdvancedGeometry.createShapelyWrapper(poly, 0)
        
        if self._originalGeomType != 'Polygon':       
            borderColorHazard = {"red": 255 / 255.0, "green": 255 / 255.0, "blue": 0 / 255.0, "alpha": 1.0 } #yellow
            fillColorHazard = {"red": 1, "green": 1, "blue": 1, "alpha": 0}
            borderColorBase = {"red": 255 / 255.0, "green": 255 / 255.0, "blue": 255 / 255.0, "alpha": 1.0 } #white
            fillColorBase = {"red": 255 / 255.0, "green": 255 / 255.0, "blue": 0 / 255.0, "alpha": 0.0 }
            hazardPolyVisibility = "always"
            basePolyVisibility = "selected"
            hazardPolyLabel = label
            basePolyLabel = ""
        else:
            borderColorHazard = "eventType"
            fillColorHazard = {"red": 255 / 255.0, "green": 255 / 255.0, "blue": 0 / 255.0, "alpha": 0.0 }
            borderColorBase = {"red": 255 / 255.0, "green": 255 / 255.0, "blue": 0 / 255.0, "alpha": 1.0 } #yellow
            fillColorBase = {"red": 1, "green": 1, "blue": 1, "alpha": 0}
            hazardPolyVisibility = "selected"
            basePolyVisibility = "always"
            hazardPolyLabel = ""
            basePolyLabel = label        
        
        hazardEventPoly = {
            "identifier": "hazardEventPolygon_" + eventID,
            "visibilityConstraints": hazardPolyVisibility,
            "diameter": "eventType",
            "borderColor": borderColorHazard,
            "fillColor": fillColorHazard,
            "label": hazardPolyLabel,
            "geometry": {
                (TimeUtils.datetimeToEpochTimeMillis(startTime), TimeUtils.datetimeToEpochTimeMillis(endTime) + 1000): poly
            }
        }
        
        basePoly = {
            "identifier": "basePreview_" + eventID,
            "visibilityConstraints": basePolyVisibility,
            "dragCapability": "all",
            "borderThickness": "eventType",
            "diameter": "eventType",
            "borderColor": borderColorBase,
            "fillColor": fillColorBase,
            "label": basePolyLabel,
            "geometry": {
                (TimeUtils.datetimeToEpochTimeMillis(startTime), TimeUtils.datetimeToEpochTimeMillis(endTime) + 1000): basePoly
            }
        }                 

        selectedFeatures.append(basePoly)                      
        selectedFeatures.append(hazardEventPoly)            
        event.setVisualFeatures(VisualFeatures(selectedFeatures))

        return True
    
    def createLabel(self, event, polygonArea):
        domain = event.getHazardAttributes().get('convectiveSigmetDomain')
        direction = event.getHazardAttributes().get('convectiveSigmetDirection')
        speed = event.getHazardAttributes().get('convectiveSigmetSpeed')
        cloudTop = event.getHazardAttributes().get('convectiveSigmetCloudTop')
        cloudTopText = event.getHazardAttributes().get('convectiveSigmetCloudTopText')
        status = event.getStatus()
        
        if status == 'ISSUED':
            area = str(polygonArea) + "sq mi"
            numberStr = event.getHazardAttributes().get('convectiveSigmetNumberStr')
            number = "\n" + numberStr + domain[0]
        
            if cloudTop == 'topsAbove':
                tops = "\nAbove FL450"
            elif cloudTop == 'topsTo':
                tops = "\nTo FL" + str(cloudTopText)
            else:
                tops = "\nN/A"                
            
            motion = "\n" + str(direction)+"@"+str(speed)+"kts"
            label = number + area + tops + motion
        else:
            area = str(polygonArea) + "sq mi"
            if cloudTop == 'topsAbove':
                tops = "\nAbove FL450"
            elif cloudTop == 'topsTo':
                tops = "\nTo FL" + str(cloudTopText)
            
            motion = "\n" + str(direction)+"@"+str(speed)+"kts"                        
            label = area + tops + motion       
        
        return label         
    
    def polygonArea(self, hazardEvent, geomType, width):
        hazGeometry = hazardEvent.getFlattenedGeometry()
        try:
            for g in hazGeometry.geoms:
                    vertices = shapely.geometry.base.dump_coords(g)
        except AttributeError:
            vertices = shapely.geometry.base.dump_coords(hazGeometry)
        
        if geomType == 'Point':
            polygonArea = pi * width**2
        elif geomType == 'LineString':
            width = width*1.15078
            polygonArea = 0
            for i in range(0, len(vertices)-1):
                lat1 = radians(vertices[i][1])
                lat2 = radians(vertices[i+1][1])
                lon1 = radians(vertices[i][0])
                lon2 = radians(vertices[i+1][0])

                dlon = lon2 - lon1
                dlat = lat2 - lat1
                a = sin(dlat/2)**2 + cos(lat1) * cos(lat2) * sin(dlon/2)**2
                c = 2 * atan2(sqrt(a), sqrt(1-a))
                d = 3961 * c 
                polygonArea = polygonArea + d*width
        elif geomType == 'Polygon':
            polygonArea = 0
            if len(vertices) >= 3:
                for i in range(0, len(vertices)-1):
                    area = radians(vertices[i+1][0] - vertices[i][0]) * (2 + sin(radians(vertices[i][1])) + sin(radians(vertices[i+1][1])))
                    polygonArea = polygonArea + area

            polygonArea = abs(polygonArea * 6378137.0 * 6378137.0 / 2.0)
            polygonArea = polygonArea * 3.861e-7
        
        polygonArea = int(round(polygonArea))
        return polygonArea
    
    def lineToPolygon(self, vertices, width):
        leftBuffer = [] 
        rightBuffer = []
        # Compute for start point
        bearing = self.gc_bearing(vertices[0],vertices[1])
        leftBuffer.append(self.gc_destination(vertices[0],width,(bearing-90.0)))
        rightBuffer.append(self.gc_destination(vertices[0],width,(bearing+90.0)))
        # Compute distance from points in middle of line
        for n in range(1,len(vertices)-1):
            b_1to2 = self.gc_bearing(vertices[n-1],vertices[n])
            b_2to3 = self.gc_bearing(vertices[n],vertices[n+1])
            theta = (b_2to3-b_1to2)/2.0
            bearing = b_1to2 + theta
            D = width / math.sin(math.radians(theta+90.0))
            leftBuffer.append(self.gc_destination(vertices[n],D,(bearing-90.0)))
            rightBuffer.append(self.gc_destination(vertices[n],D,(bearing+90.0)))
            # Compute for end point, right and left reversed for different direction
        bearing = self.gc_bearing(vertices[-1],vertices[-2])
        leftBuffer.append(self.gc_destination(vertices[-1],width,(bearing+90.0)))
        rightBuffer.append(self.gc_destination(vertices[-1],width,(bearing-90.0)))
        # Construct final corridor by combining both sides 
        poly = leftBuffer + rightBuffer[::-1] + [leftBuffer[0]]  

        return poly
    
    def pointToPolygon(self, vertices, width):
        buffer = []
        if len(vertices) == 1:
            width = width/2
            for bearing in range(0,360,15):
                loc = self.gc_destination(vertices[0],width,bearing)
                buffer.append((round(loc[0],2),round(loc[1],3)))
            poly = buffer
            
        return poly                
                 
    def createPolygon(self,vertices,width,originalGeomType):
        vertices = [x[::-1] for x in vertices]
        width = float(width) * 1.852  # convert Nautical Miles to KM
        
        if originalGeomType == 'Point':
            poly = self.pointToPolygon(vertices,width)
        elif originalGeomType == 'LineString':
            poly = self.lineToPolygon(vertices,width)
                
        poly = [x[::-1] for x in poly]    

        return poly 

    def gc_bearing(self,latlong_1, latlong_2):
        lat1, lon1 = latlong_1
        lat2, lon2 = latlong_2
        rlat1, rlon1 = math.radians(lat1), math.radians(lon1)
        rlat2, rlon2 = math.radians(lat2), math.radians(lon2)
        dLat = math.radians(lat2 - lat1)
        dLon = math.radians(lon2 - lon1)

        y = math.sin(dLon) * math.cos(rlat2)
        x = math.cos(rlat1) * math.sin(rlat2) - \
            math.sin(rlat1) * math.cos(rlat2) * math.cos(dLon)
        bearing = math.degrees(math.atan2(y,x))
        return bearing

    def gc_destination(self,latlong_1, dist, bearing):
        R = 6378.137 # earth radius in km
        lat1, lon1 = latlong_1
        rlat1, rlon1 = math.radians(lat1), math.radians(lon1)
        d = dist
        bearing = math.radians(bearing)

        rlat2 = math.asin(math.sin(rlat1) * math.cos(d/R) + \
          math.cos(rlat1) * math.sin(d/R) * math.cos(bearing))
        rlon2 = rlon1 + math.atan2(math.sin(bearing) * math.sin(d/R) * math.cos(rlat1), \
          math.cos(d/R) - math.sin(rlat1) * math.sin(rlat2))
        lat2 = math.degrees(rlat2)
        lon2 = math.degrees(rlon2)
        if lon2 > 180.: lon2 = lon2 - 360.
        latlong_2 = (lat2, lon2)
        return latlong_2           
    
    def selectDomain(self, hazardEvent, vertices, geomType, trigger):
        domains = Domains.AviationDomains
        
        if trigger == 'modification':
            pass
        else:
            hazGeometry = hazardEvent.getFlattenedGeometry()
            for g in hazGeometry.geoms:
                vertices = shapely.geometry.base.dump_coords(g)
        
        #create longitude list including all vertices
        hazardLonsList = []   
        for vertice in vertices:
            hazardLonsList.append(vertice[0])
        if geomType == 'Polygon':
            hazardLonsList.pop()
        
        softDict = {}
        sumDict = {}
        for domain in domains:
            sumDict[domain.domainName()] = 0
            
        #Iterate through longitudes to assess where they fall relative to boundaries
        #add points in soft areas to their own lists
        #find absolute difference from hard boundary and sum value
        for lon in hazardLonsList:
            for domain in domains:
                if type(domain.lowerSoftBound()) is list:
                    if lon <= domain.lowerSoftBound()[0] and lon > domain.upperSoftBound()[0]:
                        if domain.domainName() in softDict:
                            softDict[domain.domainName()].append(lon)
                        else:
                            softDict[domain.domainName()] = [lon]
                        sumDict[domain.domainName()] = sumDict[domain.domainName()] + abs(lon+(abs(domain.lowerSoftBound()[0])))
                    elif lon <= domain.lowerSoftBound()[1] and lon > domain.upperSoftBound()[1]:
                        if domain.domainName() in softDict:
                            softDict[domain.domainName()].append(lon)
                        else:
                            softDict[domain.domainName()] = [lon]
                        sumDict[domain.domainName()] = sumDict[domain.domainName()] + abs(lon+(abs(domain.upperSoftBound()[1])))
                else:
                    if lon <= domain.lowerSoftBound() and lon > domain.upperSoftBound():
                        if domain.domainName() in softDict:
                            softDict[domain.domainName()].append(lon)
                        else:
                            softDict[domain.domainName()] = [lon]
                        sumDict[domain.domainName()] = sumDict[domain.domainName()] + abs(lon + (abs(domain.lowerSoftBound())))    
        
        for domain in domains:
            #all points fall within a domain (none in soft boundaries)
            if domain.lowerLonBound() == None:
                if all(lon >= domain.upperLonBound() for lon in hazardLonsList) == True:
                    convectiveSigmetAreas = domain.domainName()
            elif domain.upperLonBound() == None:
                if all(lon <= domain.lowerLonBound() for lon in hazardLonsList) == True:
                    convectiveSigmetAreas = domain.domainName()
            elif all(lon <= domain.lowerLonBound() and lon >= domain.upperLonBound() for lon in hazardLonsList) == True:
                convectiveSigmetAreas = domain.domainName()
            #if any point falls within a hard boundary and other points exist in allowable soft boundary
            elif domain.absMinBound() == None:
                if (any(lon >= domain.upperLonBound() for lon in hazardLonsList) == True) and (all(lon >= domain.absMaxBound() for lon in hazardLonsList) == True):
                    convectiveSigmetAreas = domain.domainName()    
            elif domain.absMaxBound() == None:
                if (any(lon >= domain.lowerLonBound() for lon in hazardLonsList) == True) and (all(lon <= domain.absMinBound() for lon in hazardLonsList) == True):
                    convectiveSigmetAreas = domain.domainName()
            elif (any(lon >= domain.lowerLonBound() and lon <= domain.upperLonBound() for lon in hazardLonsList) == True) and \
                 (all(lon >= absMaxBound() and lon <= absMinBound() for lon in hazardLonsList) == True):
                convectiveSigmetAreas = domain.domainName()       
            
        #all points in the soft boundaries
        maxLength = 0
        domain = []
        if bool(softDict) == True:
            #iterate through the keys (domains) and check how many points from each fall in soft boundary
            #select whichever domain has more points, or if points are equal, select whichever has the
            #maximum absolute difference in longitude from the hard boundary 
            for key, value in softDict.iteritems():
                if len(value) > maxLength:
                    domain = []
                    domain.append(key)
                    maxLength = len(value)
                elif len(value) == maxLength:
                    domain.append(key)       
            if len(domain) == 1:
                convectiveSigmetAreas = domain[0]
            else:
                import operator
                convectiveSigmetAreas = max(sumDict.iteritems(), key=operator.itemgetter(1))[0]            
        hazardEvent.set('convectiveSigmetDomain', convectiveSigmetAreas)                              
        
        return convectiveSigmetAreas        
        
    def boundingStatement(self, hazardEvent, geomType, TABLEFILE, vertices, trigger):
        
        per_row = []
        with open(TABLEFILE, 'r') as fr:
            for line in fr:
                if not line.startswith("!"):
                    per_row.append(line.split())
                    
        self.phenomenon = hazardEvent.getPhenomenon()
        if self.phenomenon == 'SIGMET':
            boundingStatement = self.findClosestPoint(per_row,trigger,hazardEvent,geomType,vertices,7)
            
            if any(char.isdigit() for char in boundingStatement):
                boundingStatement = self.findClosestPoint(per_row,trigger,hazardEvent,geomType,vertices,6)
                
        elif self.phenomenon in ['LLWS', 'Strong_Surface_Wind', 'Turbulence', 'Mountain_Obscuration',
                                    'IFR', 'Icing', 'Multiple_Freezing_Levels']:
            boundingStatement = self.findClosestPoint(per_row,trigger,hazardEvent,geomType,vertices,50)        
        
        hazardEvent.set('boundingStatement', boundingStatement)
        
        return boundingStatement
    
    def findClosestPoint(self,per_row,trigger,hazardEvent,geomType,vertices,numvertices):
        if geomType is not 'Point':
            boundingStatement = 'FROM '
        else:
            boundingStatement = ''
                    
        lats = []
        lons = []
        names = []
        stid = []
        
        # ! SNAP.TBL SAMPLE FORMAT
        # !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        # !
        # !STID    STNM   NAME                            ST CO   LAT    LON   ELV  PRI
        # !(8)     (6)    (32)                            (2)(2)  (5)    (6)   (5)  (2)
        # YSJ00000      9 YSJ                              -  -   4532  -6588     0  1
        # YSJ00001      9 20N_YSJ                          -  -   4565  -6588     0  2
        # YSJ00002      9 30N_YSJ                          -  -   4582  -6588     0  2
        # YSJ00003      9 40N_YSJ                          -  -   4599  -6588     0  2

        for row in per_row:
            stid.append(row[0])
            lats.append(row[5])
            lons.append(row[6])
            names.append(row[2])

        # add decimal points 
        latNew = []
        for lat in lats:
            if len(lat) == 4:
                latNew.append(float(lat[:2] + '.' + lat[2:]))
            else:
                latNew.append(float(lat))
        lats = latNew

        lonNew = []
        for lon in lons:
            if len(lon) == 5:
                lonNew.append(float(lon[:3] + '.' + lon[3:]))
            elif len(lon) == 6:
                lonNew.append(float(lon[:4] + '.' + lon[4:]))
            else:
                lonNew.append(float(lon))    
        lons = lonNew
        
        vorLat = []
        vorLon = []        
        
        if trigger == 'modification':
            pass
        elif trigger == 'swathGeneration':
            vertices = shapely.geometry.base.dump_coords(vertices)
        else:
            for g in hazardEvent.getFlattenedGeometry().geoms:
                vertices = shapely.geometry.base.dump_coords(g)
                
        if geomType == 'Polygon':
            vertices = self._reducePolygon(hazardEvent,vertices, geomType, numvertices)
            vertices = shapely.geometry.base.dump_coords(vertices)
           
        for vertice in vertices:
            hazardLat = vertice[1]
            hazardLon = vertice[0]

            diffList = []
            for x in range(0, len(lats)):
                diffList.append(abs(hazardLat - lats[x]) + abs(hazardLon - lons[x]))

            index = diffList.index(min(diffList))
  
            boundingStatement += names[index] + '-'
            vorLat.append(lats[index])
            vorLon.append(lons[index])

        self._setVORPoints(vorLat, vorLon, hazardEvent)
        selectedVisualFeatures = []
           
        boundingStatement = boundingStatement[:-1]
        
        return boundingStatement        
    
    def _reducePolygon(self, hazardEvent, vertices, geomType, numPoints):
        try:
            initialPoly = GeometryFactory.createPolygon(vertices) 
        except ValueError:
            for g in hazardEvent.getFlattenedGeometry().geoms:
                vertices = shapely.geometry.base.dump_coords(g)
            initialPoly = GeometryFactory.createPolygon(vertices)
          
        #numPoints = 6
        tolerance = 0.001
        newPoly = initialPoly.simplify(tolerance, preserve_topology=True)
        while len(newPoly.exterior.coords) > numPoints:
            tolerance += 0.001
            newPoly = initialPoly.simplify(tolerance, preserve_topology=True)
            
        return newPoly  
    
    def _setVORPoints(self, vorLat, vorLon, hazardEvent):
        VOR_points = zip(vorLon, vorLat)
        hazardEvent.set('VOR_points', VOR_points)
        
        return
    
    #########################################
    ### OVERRIDES
    
    def volcanoFilePath(self):
        return '/home/nathan.hardin/Desktop/volcanoes.csv'           