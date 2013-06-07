# Note:  expirationTime is a tuple (beforeMinutes, afterMinutes)
HazardTypes = {
    'AF.Y' : {'headline': 'ASHFALL ADVISORY',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'AQ.Y' : {'headline': 'AIR QUALITY ALERT',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'AS.O' : {'headline': 'AIR STAGNATION OUTLOOK',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'AS.Y' : {'headline': 'AIR STAGNATION ADVISORY',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'BW.Y' : {'headline': 'BRISK WIND ADVISORY',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'BZ.A' : {'headline' : 'BLIZZARD WATCH',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'BZ.W' : {'headline' : 'BLIZZARD WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'CF.A' : {'headline': 'COASTAL FLOOD WATCH',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'CF.W' : {'headline': 'COASTAL FLOOD WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'CF.Y' : {'headline': 'COASTAL FLOOD ADVISORY',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'CF.S' : {'headline': '',   # No headline for this VTEC
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'DS.W' : {'headline': 'DUST STORM WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'DU.Y' : {'headline': 'BLOWING DUST ADVISORY',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'EC.A' : {'headline': 'EXTREME COLD WATCH',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'EC.W' : {'headline': 'EXTREME COLD WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'EH.A' : {'headline': 'EXCESSIVE HEAT WATCH',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'EH.W' : {'headline': 'EXCESSIVE HEAT WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'EW.W' : {'headline': 'EXCESSIVE WIND WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'FA.A' : {'headline': 'AREAL FLOOD WATCH',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30),
              'replacedBy': ['FA.W', 'FA.Y']
              },
    'FA.W' : {'headline': 'AREAL FLOOD WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': False,
              'allowAreaChange': False,
              'allowTimeChange': True,
              'expirationTime': (-30, 30),
              'replacedBy': ['FA.Y']
              },
    'FA.Y' : {'headline': 'AREAL FLOOD ADVISORY',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': False,
              'allowAreaChange': False,
              'allowTimeChange': True,
              'expirationTime': (-30, 30),
              'replacedBy': ['FA.W']
              },
    'FF.A' : {'headline': 'FLASH FLOOD WATCH',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30),
              'replacedBy': ['FF.W.Convective', 'FF.W.NonConvective'],
              },
    'FF.W.Convective' : {
              'headline': 'FLASH FLOOD WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': False,
              'allowAreaChange': False,
              'allowTimeChange': True,
              'expirationTime': (-10, 10)
              },
    'FF.W.NonConvective' : {
              'headline': 'FLASH FLOOD WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': False,
              'allowAreaChange': False,
              'allowTimeChange': True,
              'expirationTime': (-10, 10)
              },
    'FG.Y' : {'headline': 'DENSE FOG ADVISORY',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'FL.A' : {'headline': 'FLOOD WATCH',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': False,
              'allowAreaChange': False,
              'allowTimeChange': True,
              'expirationTime': (-30, 30),
              'replacedBy': ['FL.W', 'FL.Y']
              },
    'FL.W' : {'headline': 'FLOOD WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': False,
              'allowAreaChange': False,
              'allowTimeChange': True,
              'expirationTime': (-30, 30),
              'replacedBy': ['FL.Y']
              },
    'FL.Y' : {'headline': 'FLOOD ADVISORY',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': False,
              'allowAreaChange': False,
              'allowTimeChange': True,
              'expirationTime': (-30, 30),
              'replacedBy': ['FL.W']
              },
    'FR.Y' : {'headline': 'FROST ADVISORY',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30),
              },
    'FW.A' : {'headline': 'FIRE WEATHER WATCH',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'FW.W' : {'headline': 'RED FLAG WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'FZ.A' : {'headline': 'FREEZE WATCH',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'FZ.W' : {'headline': 'FREEZE WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'GL.A' : {'headline': 'GALE WATCH',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'GL.W' : {'headline': 'GALE WARNING',            
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'HF.A' : {'headline': 'HURRICANE FORCE WIND WATCH',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'HF.W' : {'headline': 'HURRICANE FORCE WIND WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'HI.A' : {'headline': 'HURRICANE WIND WATCH',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'HI.W' : {'headline': 'HURRICANE WIND WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'HT.Y' : {'headline': 'HEAT ADVISORY',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'HU.A' : {'headline': 'HURRICANE WATCH',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'HU.S' : {'headline': '',  # No headline for this VTEC
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'HU.W' : {'headline': 'HURRICANE WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'HW.A' : {'headline': 'HIGH WIND WATCH',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'HW.W' : {'headline': 'HIGH WIND WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'HY.S' : {'headline': '',  # No headline for this VTEC
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': False,
              'allowAreaChange': False,
              'allowTimeChange': False,
              'expirationTime': (-30, 30),
              'replacedBy': ['FL.A']
              },
    'HZ.A' : {'headline': 'HARD FREEZE WATCH',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'HZ.W' : {'headline': 'HARD FREEZE WARNING',  
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'IS.W' : {'headline': 'ICE STORM WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'LE.A' : {'headline': 'LAKE EFFECT SNOW WATCH',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'LE.W' : {'headline': 'LAKE EFFECT SNOW WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'LE.Y' : {'headline': 'LAKE EFFECT SNOW ADVISORY',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'LO.Y' : {'headline': 'LOW WATER ADVISORY',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'LS.A' : {'headline': 'LAKESHORE FLOOD WATCH',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'LS.S' : {'headline': '',  # No headline for this VTEC
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'LS.W' : {'headline': 'LAKESHORE FLOOD WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'LS.Y' : {'headline': 'LAKESHORE FLOOD ADVISORY',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'LW.Y' : {'headline': 'LAKE WIND ADVISORY',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'MA.S' : {'headline': '',  # No headline for this VTEC
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'MA.W' : {'headline': 'SPECIAL MARINE WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': False,
              'allowAreaChange': False,
              'allowTimeChange': False,
              'expirationTime': (-10, 10)
              },
    'MF.Y' : {'headline': 'DENSE FOG ADVISORY',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              }, # Marine Fog
    'MH.Y' : {'headline': 'ASHFALL ADVISORY',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              }, # Marine Ashfall
    'MS.Y' : {'headline': 'DENSE SMOKE ADVISORY',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              }, # Marine Smoke
    'RB.Y' : {'headline': 'SMALL CRAFT ADVISORY FOR ROUGH BAR',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'SC.Y' : {'headline': 'SMALL CRAFT ADVISORY',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'SE.A' : {'headline': 'HAZARDOUS SEAS WATCH',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'SE.W' : {'headline': 'HAZARDOUS SEAS WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'SI.Y' : {'headline': 'SMALL CRAFT ADVISORY FOR WINDS',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'SM.Y' : {'headline': 'DENSE SMOKE ADVISORY',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'SR.A' : {'headline': 'STORM WATCH',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'SR.W' : {'headline': 'STORM WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'SU.W' : {'headline': 'HIGH SURF WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'SU.Y' : {'headline': 'HIGH SURF ADVISORY',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },             
    'SV.A' : {'headline': 'SEVERE THUNDERSTORM WATCH',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'SV.W' : {'headline': 'SEVERE THUNDERSTORM WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': False,
              'allowAreaChange': False,
              'allowTimeChange': False,
              'expirationTime': (-10, 10)
              },
    'SW.Y' : {'headline': 'SMALL CRAFT ADVISORY FOR HAZARDOUS SEAS',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'TI.A' : {'headline': 'TROPICAL STORM WIND WATCH',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'TI.W' : {'headline': 'TROPICAL STORM WIND WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'TO.A' : {'headline': 'TORNADO WATCH',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'TO.W' : {'headline': 'TORNADO WARNING',
              'subType':'' ,
              'combinableSegments': False,
              'allowAreaChange': False,
              'allowTimeChange': False,
              'expirationTime': (-10, 10)
              },
    'TR.A' : {'headline': 'TROPICAL STORM WATCH',
              'subType':'' ,
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'TR.W' : {'headline': 'TROPICAL STORM WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'TS.A' : {'headline': 'TSUNAMI WATCH',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'TS.W' : {'headline': 'TSUNAMI WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': False,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'TY.A' : {'headline': 'TYPHOON WATCH',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'TY.W' : {'headline': 'TYPHOON WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'UP.A' : {'headline': 'HEAVY FREEZING SPRAY WATCH',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'UP.W' : {'headline': 'HEAVY FREEZING SPRAY WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'UP.Y' : {'headline': 'FREEZING SPRAY ADVISORY',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'WC.A' : {'headline': 'WIND CHILL WATCH',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'WC.W' : {'headline': 'WIND CHILL WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'WC.Y' : {'headline': 'WIND CHILL ADVISORY',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'WI.Y' : {'headline': 'WIND ADVISORY',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
             },
    'WS.A' : {'headline': 'WINTER STORM WATCH',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'WS.W' : {'headline': 'WINTER STORM WARNING',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'WW.Y' : {'headline': 'WINTER WEATHER ADVISORY',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'ZF.Y' : {'headline': 'FREEZING FOG ADVISORY',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
              },
    'ZR.Y' : {'headline': 'FREEZING RAIN ADVISORY',
              '_override_lock_': ['headline','combinableSegments', 'allowAreaChange', 'allowTimeChange', 'expirationTime', True],
              'combinableSegments': True,
              'allowAreaChange': True,
              'allowTimeChange': True,
              'expirationTime': (-30, 30)
             },
    }
