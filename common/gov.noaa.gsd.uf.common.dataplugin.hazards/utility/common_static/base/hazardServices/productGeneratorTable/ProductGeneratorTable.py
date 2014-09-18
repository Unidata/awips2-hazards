
ProductGeneratorTable = {
        "FFA_ProductGenerator": {
            "allowedHazards": [
             ('FF.A', "Flood"),
             ('FA.A', "Flood"),
             ('FL.A', "Flood1"),                                                   
             ],
            "previewFormatters": ["Legacy", "XML", "CAP"],
            "issueFormatters": ["Legacy"], 
            },
        "FLW_FLS_ProductGenerator" : {
            "allowedHazards": [
             ('FA.W', "Flood1"),
             ('FA.Y', "Flood2"),
             ('FL.W', "Flood3"),             
             ('FL.Y', "Flood4"),             
             ('HY.S', "Flood5"),
            ],
            "previewFormatters": ["Legacy", "XML", "CAP"],
            "issueFormatters": ["Legacy"],  
            },
        "FFW_FFS_ProductGenerator" : {
            "allowedHazards": [
             ('FF.W.Convective',     "Flood"),
             ('FF.W.NonConvective',  "Flood"),             
             ('FF.W.BurnScar',  "Flood"),             
             ],
            "previewFormatters": ["Legacy", "XML", "CAP"],
            "issueFormatters": ["Legacy"],   
            }, 
        "ESF_ProductGenerator": {
            "allowedHazards": [
             ('HY.O',     "Flood"),           
             ],
            "previewFormatters": ["Legacy", "XML", "CAP"],
            "issueFormatters": ["Legacy"],   
            },           
#         "FFW_FFS_Convective_ProductGenerator" : {
#             "allowedHazards": [
#              ('FF.W.Convective',     "Flood"),
#              ],
#            "previewFormatters": ["Legacy", "XML", "CAP"],
#            "issueFormatters": ["Legacy", "XML", "CAP"],
#              },
#         "FFW_FFS_NonConvective_ProductGenerator" : {
#             "allowedHazards": [
#              ('FF.W.NonConvective',  "Flood"),             
#              ],
#            "previewFormatters": ["Legacy", "XML", "CAP"],
#            "issueFormatters": ["Legacy", "XML", "CAP"],   
#             },
        "WSW_ProductGenerator" : {
            "allowedHazards": [
            ('BZ.W', 'WinterWx'),     # BLIZZARD WARNING
            ('IS.W', 'WinterWx'),     # ICE STORM WARNING
            ('LE.W', 'WinterWx'),     # LAKE EFFECT SNOW WARNING
            ('WS.W', 'WinterWx'),     # WINTER STORM WARNING
            ('ZR.Y', 'WinterWx'),     # FREEZING RAIN ADVISORY
            ('LE.Y', 'WinterWx'),     # LAKE EFFECT SNOW ADVISORY
            ('WW.Y', 'WinterWx'),     # WINTER WEATHER ADVISORY
            ('BZ.A', 'WinterWx'),     # BLIZZARD WATCH
            ('LE.A', 'WinterWx'),     # LAKE EFFECT SNOW WATCH
            ('WS.A', 'WinterWx'),     # WINTER STORM WATCH
            ('WC.W', 'WindChill'),    # WIND CHILL WARNING
            ('WC.Y', 'WindChill'),    # WIND CHILL ADVISORY
            ('WC.A', 'WindChill'),    # WIND CHILL WATCH
            ],
            "previewFormatters": ["Legacy"],
            "issueFormatters": ["Legacy", "XML", "CAP"],  
            "reservedNameNotYetImplemented": True,
            },
        "NPW_ProductGenerator" : {
            "allowedHazards": [
            ('DS.W', 'Dust'),         # DUST STORM WARNING
            ('DU.Y', 'Dust'),         # BLOWING DUST ADVISORY
            ('EC.W', 'Cold'),         # EXTREME COLD WARNING
            ('EC.A', 'Cold'),         # EXTREME COLD WATCH
            ('EH.W', 'Heat'),         # EXCESSIVE HEAT WARNING
            ('EH.A', 'Heat'),         # EXCESSIVE HEAT WATCH
            ('HT.Y', 'Heat'),          # HEAT ADVISORY
            ('FG.Y', 'Fog'),          # DENSE FOG ADVISORY
            ('HZ.W', 'FrostFreeze'),  # HARD FREEZE WARNING
            ('FZ.W', 'FrostFreeze'),  # FREEZE WARNING
            ('FR.Y', 'FrostFreeze'),  # FROST ADVISORY
            ('HZ.A', 'FrostFreeze'),  # HARD FREEZE WATCH
            ('FZ.A', 'FrostFreeze'),  # FREEZE WATCH
            ('HW.W', 'Wind'),         # HIGH WIND WARNING
            ('WI.Y', 'Wind'),         # WIND ADVISORY
            ('LW.Y', 'Wind'),         # LAKE WIND ADVISORY
            ('HW.A', 'Wind'),         # HIGH WIND WATCH
            ('SM.Y', 'Smoke'),        # DENSE SMOKE ADVISORY
            ('ZF.Y', 'FreezeFog'),     # FREEZING FOG ADVISORY
            ('AF.Y', 'Ashfall'),       # ASHFALL ADVISORY
            ('AS.Y', 'AirStagnation'), # AIR STAGNATION ADVISORY
            ('AS.O', 'AirStagnation'), # AIR STAGNATION OUTLOOK
            ],
            "previewFormatters": ["Legacy"],
            "issueFormatters": ["Legacy", "XML", "CAP"],  
            "reservedNameNotYetImplemented": True,
            },
        "HLS_ProductGenerator" : {
            "allowedHazards": [
            ('HU.A','Hurricane'),
            ('HU.W','Hurricane'),
            ('TY.A','Typhoon'),
            ('TY.W','Typhoon'),
            ('TR.A','Tropical'),
            ('TR.W','Tropical'),
            ],
            "previewFormatters": ["Legacy"],
            "issueFormatters": ["Legacy", "XML", "CAP"],  
            "reservedNameNotYetImplemented": True,
            },
        "MWS_ProductGenerator" : {
            "allowedHazards": [
            ('MA.S', 'MarineStatement'), # MARINE STATEMENT
            ],
            "previewFormatters": ["Legacy"],
            "issueFormatters": ["Legacy", "XML", "CAP"],  
            "reservedNameNotYetImplemented": True,
            },
        "AQA_ProductGenerator" : {
            "allowedHazards": [
            ('AQ.Y', 'AirQual'),      # AIR QUALITY ALERT
            ],
            "previewFormatters": ["Legacy"],
            "issueFormatters": ["Legacy", "XML", "CAP"],  
            "reservedNameNotYetImplemented": True,
            },
        "WCN_ProductGenerator" : {
            "allowedHazards": [
            ('TO.A', 'Convective'),
            ('SV.A', 'Convective')
            ],
            "previewFormatters": ["Legacy"],
            "issueFormatters": ["Legacy", "XML", "CAP"],  
            "reservedNameNotYetImplemented": True,
            },
        "MWW_ProductGenerator" : {
            "allowedHazards": [
            ('GL.W', 'Marine3'),  # GALE WARNING
            ('HF.W', 'Marine'),   # HURRICANE FORCE WIND WARNING
            ('SE.W', 'Marine4'),  # HAZARDOUS SEAS
            ('SR.W', 'Marine'),   # STORM WARNING
            ('UP.W', 'IceAccr'),  # HEAVY FREEZING SPRAY WARNING
            ('MH.Y', 'Ashfall'),   # VOLCANIC ASHFALL ADVISORY
            ('BW.Y', 'Marine'),    # BRISK WIND ADVISORY
            ('MF.Y', 'Fog'),       # DENSE FOG ADVISORY
            ('LO.Y', 'LowWater'),  # LOW WATER ADVISORY
            ('RB.Y', 'Marine1'),   # ROUGH BAR
            ('SI.Y', 'Marine2'),   # SMALL CRAFT ADVISORY
            ('SC.Y', 'Marine3'),   # SMALL CRAFT ADVISORY
            ('MS.Y', 'Smoke'),     # DENSE SMOKE ADVISORY
            ('SW.Y', 'Marine4'),   # SMALL CRAFT ADVISORY
            ('UP.Y', 'IceAccr'),   # HEAVY FREEZING SPRAY ADVISORY
            ('GL.A', 'Marine3'),  # GALE WATCH
            ('HF.A', 'Marine'),   # HURRICANE FORCE WIND WATCH
            ('SE.A', 'Marine4'),  # HAZARDOUS SEAS WATCH
            ('SR.A', 'Marine'),   # STORM WATCH
            ('UP.A', 'IceAccr'),  # HEAVY FREEZING SPRAY WATCH
            ],
            "previewFormatters": ["Legacy"],
            "issueFormatters": ["Legacy", "XML", "CAP"],  
            "reservedNameNotYetImplemented": True,
            },
        "RFW_ProductGenerator" : {
            "allowedHazards": [
            ('FW.W', 'FireWx'),
            ('FW.A', 'FireWx')
            ],
            "previewFormatters": ["Legacy"],
            "issueFormatters": ["Legacy", "XML", "CAP"],  
            "reservedNameNotYetImplemented": True,
            },
        "CFW_ProductGenerator" : {
            "allowedHazards": [
            ('BH.S', 'CoastalFlood'),     # BEACH HAZARD STATEMENT
            ('CF.W', 'CoastalFlood'),     # COASTAL FLOOD WARNING
            ('CF.Y', 'CoastalFlood'),     # COASTAL FLOOD ADVISORY 
            ('CF.A', 'CoastalFlood'),     # COASTAL FLOOD WATCH
            ('CF.S', 'CoastalFloodStatement'),     # COASTAL FLOOD STATEMENT
            ('LS.W', 'CoastalFlood'),     # LAKESHORE FLOOD WARNING
            ('LS.Y', 'CoastalFlood'),     # LAKESHORE FLOOD ADVISORY
            ('LS.A', 'CoastalFlood'),     # LAKESHORE FLOOD WATCH
            ('SU.W', 'HighSurf'),         # HIGH SURF WARNING
            ('SU.Y', 'HighSurf'),         # HIGH SURF ADVISORY
            ],
            "previewFormatters": ["Legacy"],
            "issueFormatters": ["Legacy", "XML", "CAP"],  
            "reservedNameNotYetImplemented": True,
            },
        "TOR_SVR_SVS_ProductGenerator" : {
            "allowedHazards": [
            ('TO.W', 'Tornado'),     # TORNADO Warning
            ('SV.W', 'SvrThunderstorm'), # Svr Thunderstm Warning
            ],
            "previewFormatters": ["Legacy"],
            "issueFormatters": ["Legacy", "XML", "CAP"],  
            "reservedNameNotYetImplemented": True,
            },
        "EWW_ProductGenerator" : {
            "allowedHazards": [
            ('EW.W', 'Extreme Wind'),     # Extreme Wind Warning
            ],
            "previewFormatters": ["Legacy"],
            "issueFormatters": ["Legacy", "XML", "CAP"],  
            "reservedNameNotYetImplemented": True,
            },
        "SMW_MWS_ProductGenerator" : {
            "allowedHazards": [
            ('MA.W', 'MarineWarning'),     # Marine Warning
            ],
            "previewFormatters": ["Legacy"],
            "issueFormatters": ["Legacy", "XML", "CAP"],  
            "reservedNameNotYetImplemented": True,
            }
        }
