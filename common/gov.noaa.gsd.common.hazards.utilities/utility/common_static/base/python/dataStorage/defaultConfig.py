statuses = [ 
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
                      "displayString": "ending", 
                     },

                     {
                      "displayString": "ended", 
                     },

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
               "expandHorizontally": True,
               "expandVertically": True,
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
                                          "choices": [],
                                          "lines": 16,
                                          "columnName": "Site ID"
                                         },
                                         {
                                          "fieldName": "visibleStatuses",
                                          "label": "Status:",
                                          "fieldType": "CheckList",
                                          "choices": statuses,
                                          "lines": 16,
                                          "columnName": "Status"
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
                                          "expandHorizontally": True,
                                          "expandVertically": True,
                                          "fields": [
                                                     {
                                                      "fieldName": "visibleColumns",
                                                      "label": "Available:",
                                                      "selectedLabel": "Selected:",
                                                      "fieldType": "BoundedListBuilder",
                                                      "choices": [],
                                                      "lines": 10
                                                     }
                                          ]
                                         },
                                        ],
                         },
                         {
                          "pageName": "Recommenders",
                          "pageFields": [
                                         {
                                          "fieldType": "SwtWrapper",
                                          "fieldName": "Recommenders",
                                         },
                                        ],
                         },
                        ],
              },
            ]
