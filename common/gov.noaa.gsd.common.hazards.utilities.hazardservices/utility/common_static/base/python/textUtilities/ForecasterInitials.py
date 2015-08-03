'''
   Description: Provides sites a way to map forecaster's user name to the 
   name/initials/forecasterID that they want display in products that have
   the initials productPart.
   
   Note: If there is no entry for the current user, the user name obtained from
   os.getlogin() will be used.
   
'''
import os

def getForecasterIdentification():
    currentUser = os.getlogin()

    forecasterInitials = {
#                      Examples
#                      "userName1" : "Full Name",
#                      "userName2" : "Forecaster ID",
#                      "userName3" : "initials",
                 }
    return forecasterInitials.get(currentUser, currentUser)
