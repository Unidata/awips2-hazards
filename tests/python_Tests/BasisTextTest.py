'''
    Description: Application that tests BasisText
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Nov 17, 2014       4763     Daniel.S.Schaffer      Initial creation
    @author Daniel.S.Schaffer@noaa.gov
'''
from BasisText import BasisText
basisText = BasisText()
hazardType = "FF.W.NonConvective"
for source in "countySource", "localLawEnforcementSource", "corpsOfEngineersSource", "damOperatorSource", "bureauOfReclamationSource", "publicSource", "gaugesSource", "civilAirPatrolSource":
    for immediateCause in "dam", "siteImminent", "siteFailed", "levee", "floodgate", "glacier", "icejam", "snowMelt", "volcano", "volcanoLahar":
        hazardEventAttributes = {
        "source":source,
        "immediateCause":immediateCause,
        }
        print
        print hazardType
        print hazardEventAttributes
        print basisText.getBulletText(hazardType, hazardEventAttributes)

for source in "dopplerSource", "dopplerGaugesSource", "trainedSpottersSource", "publicSource", "localLawEnforcementSource", "emergencyManagementSource", "satelliteSource", "satelliteGaugesSource", "gaugesSource":
    for eventType in "thunderEvent", "rainEvent", "flashFlooding":
        for immediateCause in "ER", "RS":
            for hazardType in "FF.W.Convective", "FF.W.BurnScar":
                hazardEventAttributes = {
                "source":source,
                "eventType":eventType,
                "immediateCause":immediateCause,
                }
                print
                print hazardType
                print hazardEventAttributes
                print basisText.getBulletText(hazardType, hazardEventAttributes)
            
hazardType = "FA.Y"
for source in "dopplerSource", "dopplerGaugesSource", "trainedSpottersSource", "publicSource", "localLawEnforcementSource", "emergencyManagementSource", "satelliteSource", "gaugesSource":
    for eventType in "thunderEvent", "rainEvent", "minorFlooding":
        for immediateCause in "ER", "SM", "RS", "IJ", "IC", "DR":
            for advisoryType in "generalMinorFlooding", "smallStreams", "urbanAreasSmallStreams", "arroyoSmallStreams", "hydrologic":
                hazardEventAttributes = {
                "source":source,
                "eventType":eventType,
                "immediateCause":immediateCause,
                "advisoryType":advisoryType
                }
                print
                print hazardType
                print hazardEventAttributes
                print basisText.getBulletText(hazardType, hazardEventAttributes)

hazardType = "FA.W"
for source in "dopplerSource", "dopplerGaugesSource", "trainedSpottersSource", "publicSource", "localLawEnforcementSource", "emergencyManagementSource", "satelliteSource", "satelliteGaugesSource", "gaugesSource":
    for eventType in "thunderEvent", "rainEvent", "flooding", "genericFlooding":
        hazardEventAttributes = {
        "source":source,
        "eventType":eventType,
        "rainSoFarLowerBound":0.2,
        "rainSoFarUpperBound":2.5,
        }
        print
        print hazardType
        print hazardEventAttributes
        print basisText.getBulletText(hazardType, hazardEventAttributes)
