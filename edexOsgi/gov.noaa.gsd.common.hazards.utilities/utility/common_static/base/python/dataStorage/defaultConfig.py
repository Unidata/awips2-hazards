siteIDs = [ 
                     {
                      "displayString": "BOU", 
                     },
                     {
                      "displayString": "PUB", 
                     },
                     {
                      "displayString": "GJT", 
                     },
                     {
                      "displayString": "CYS", 
                     },
                     {
                      "displayString": "OAX", 
                     },
                     {
                      "displayString": "FSD", 
                     },
                     {
                      "displayString": "DMX",
                     },
                     {
                      "displayString": "GID", 
                     },
                     {
                      "displayString": "EAX", 
                     },
                     {
                      "displayString": "TOP", 
                     },
                     {
                      "displayString": "RAH"
                     }
            ]

states = [ 
                     {
                      "displayString": "potential", 
                     },
                     {
                      "displayString": "proposed", 
                     },

                     {
                      "displayString": "pending", 
                     },

                     {
                      "displayString": "issued", 
                     },

                     {
                      "displayString": "ended", 
                     },

            ]
columns = [ 
                     {
                      "displayString": "Event ID", 
                     },
                     {
                      "displayString": "Hazard Type", 
                     },
                     {
                      "displayString": "State", 
                     },
                     {
                      "displayString": "Start Time", 
                     },
                     {
                      "displayString": "End Time", 
                     },
                     {
                      "displayString": "Phen", 
                     },
                     {
                      "displayString": "Sig", 
                     },
                     {
                      "displayString": "Expiration Time",
                     },
                     {
                      "displayString": "Issue Time", 
                     },
                     {
                      "displayString": "Site ID", 
                     },
                     {
                      "displayString": "VTEC Codes", 
                     },
                     {
                      "displayString": "ETNs"
                     },
                     {
                      "displayString": "PILs"
                     },
                     {
                      "displayString": "Time Remaining"
                     },
                     {
                      "displayString": "Description"
                     },
                     {
                      "displayString": "Point ID"
                     },
                     {
                      "displayString": "Stream"
                     }
            ]

# This lists the fields that are possible in a Setting
# The Setting Dialog will use this information to display and allow manipulation
# of a Setting Definition from the user.
#
# NOTE:
#
# Some megawidgets defined here include the non-megawidget-specifier key
# "columnName", with a value that matches one of the "columns" names. These
# key-value pairs are used to determine which filters may be paired with which
# console table columns. 
viewConfig = [
              {
               "fieldName": "tabbedPanel",
               "fieldType": "TabbedComposite",
               "leftMargin": 10,
               "rightMargin": 10,
               "topMargin": 7,
               "bottomMargin": 7,
               "spacing": 10,
               "expandHorizontally": 1,
               "pages": [
                         {
                          "pageName": "Hazards Filter",
                          "numColumns": 3,
                          "pageFields": [
                                         {
                                          "fieldName": "hazardCategoriesAndTypes",
                                          "label": "Hazard Categories and Types:",
                                          "fieldType": "HierarchicalChoicesTree",
                                          "lines": 16,
                                          "columnName": "Hazard Type"
                                         },
                                         {
                                          "fieldName": "visibleSites",
                                          "label": "Site IDs:",
                                          "fieldType": "CheckList",
                                          "choices": siteIDs,
                                          "lines": 16,
                                          "columnName": "Site ID"
                                         },              
                                         {
                                          "fieldName": "visibleStates",
                                          "label": "State:",
                                          "fieldType": "CheckList",
                                          "choices": states,
                                          "lines": 16,
                                          "columnName": "State"
                                         },              
                                        ],
                         },
                         {
                          "pageName": "Console",
                          "pageFields": [
                                         {
                                          "fieldName": "visibleColumnsGroup",
                                          "label": "Columns",
                                          "fieldType": "Group",
                                          "leftMargin": 10,
                                          "rightMargin": 10,
                                          "bottomMargin": 10,
                                          "fields": [
                                                     {
                                                      "fieldName": "visibleColumns",
                                                      "label": "Available:",
                                                      "selectedLabel": "Selected:",
                                                      "fieldType": "BoundedListBuilder",
                                                      "choices": columns,
                                                      "lines": 10
                                                     }
                                          ]
                                         },
                                        ],
                         },
                        ],
              },
            ]
              
                
