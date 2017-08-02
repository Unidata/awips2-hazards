'''
   Description: Provides sites a way to map forecaster's user name to the 
   name/initials/forecasterID that they want display in products that have
   the initials productPart.
   
   Note: If there is no entry for the current user, the user name of the user 
   running CAVE will be used.
'''

import os
import pwd

def getForecasterIdentification():
    currentUser = pwd.getpwuid(os.getuid()).pw_name

    forecasterInitials = {
#                      Examples
#                      "userName1" : "Full Name",
#                      "userName2" : "Forecaster ID",
#                      "userName3" : "initials",
#                      "userName4" : "", # No initials in product
                 }
    return forecasterInitials.get(currentUser, currentUser)
