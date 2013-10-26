

CAP_Fields = [ 
           { 
            'fieldName': 'urgency',
            'fieldType':'ComboBox',
            'label':'Urgency:',
            'values': 'Immediate',
            'expandHorizontally': True,
            'choices': ['Immediate', 'Expected', 'Future','Past','Unknown']
            },
           { 

            'fieldName': 'responseType',
            'fieldType':'ComboBox',
            'label':'Response Type:',
            'values': 'Avoid',
            'expandHorizontally': True,
            'choices': ['Shelter','Evacuate','Prepare','Execute','Avoid','Monitor','Assess','AllClear','None']
            },                    
           { 
            'fieldName': 'severity',
            'fieldType':'ComboBox',
            'label':'Severity:',
            'values': 'Severe',
            'expandHorizontally': True,
            'choices': ['Extreme','Severe','Moderate','Minor','Unknown']
            },
           { 
            'fieldName': 'certainty',
            'fieldType':'ComboBox',
            'label':'Certainty:',
            'values': 'Likely',
            'expandHorizontally': True,
            'choices': ['Observed','Likely','Possible','Unlikely','Unknown']
            },
            {
            'fieldName': 'WEA_Text',
            'fieldType':'Text',
            'label':'WEA Text:',
            'values': '',
            'maxChars': 90,
            'visibleChars': 6,
            'expandHorizontally': True
            },                   
            {
            'fieldName': 'WEA_Label',
            'fieldType':'Label',
            'label':'Note: In WEA text, "%s" is replaced with end time/day.',
            'wrap': True
            }                   
]


#######################  WEA Messages
#
#     Tsunami Warning (coming late 2013)    Tsunami danger on the coast.  Go to high ground or move inland. Check local media. -NWS 
#     Tornado Warning                       Tornado Warning in this area til hh:mm tzT. Take shelter now. Check local media. -NWS 
#     Extreme Wind Warning                  Extreme Wind Warning this area til hh:mm tzT ddd. Take shelter. -NWS
#     Flash Flood Warning                   Flash Flood Warning this area til hh:mm tzT. Avoid flooded areas. Check local media. -NWS
#     Hurricane Warning                     Hurricane Warning this area til hh:mm tzT ddd. Check local media and authorities. -NWS
#     Typhoon Warning                       Typhoon Warning this area til hh:mm tzT ddd. Check local media and authorities. -NWS
#     Blizzard Warning                      Blizzard Warning this area til hh:mm tzT ddd. Prepare. Avoid travel. Check media. -NWS
#     Ice Storm Warning                     Ice Storm Warning this area til hh:mm tzT ddd. Prepare. Avoid travel. Check media. -NWS
#     Dust Storm Warning                    Dust Storm Warning in this area til hh:mm tzT ddd. Avoid travel. Check local media. -NWS
#         
#     Legend
#     hh:mm tzT ddd
#     tzT = timezone
#     ddd= three letter abbreviation for day of the week 
#      