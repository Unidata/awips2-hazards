# #
# This software was developed and / or modified by Raytheon Company,
# pursuant to Contract DG133W-05-CQ-1067 with the US Government.
#
# U.S. EXPORT CONTROLLED TECHNICAL DATA
# This software product contains export-restricted data whose
# export/transfer/disclosure is restricted by U.S. law. Dissemination
# to non-U.S. persons whether in the United States or abroad requires
# an export license or other authorization.
#
# Contractor Name:        Raytheon Company
# Contractor Address:     6825 Pine Street, Suite 340
#                         Mail Stop B8
#                         Omaha, NE 68106
#                         402.291.0100
#
# See the AWIPS II Master Rights File ("Master Rights File.pdf") for
# further licensing information.   
# #

#    Formats a dictionary 'data' and generates raw ASCII text. The dictionary values
#    will be extracted and concatenated together separated by newline. 
#
#
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    01/10/13                      jsanchez       Initial Creation.
#    
# 
#
import FormatTemplate

class Format(FormatTemplate.Formatter):
    
    def execute(self, data):
        """
        Main method of execution to generate ASCII
        @param data: dictionary values provided by the product generator
        @return: Returns only the values in the dictionary
        """
        return self.dictionary(data)
    
    def dictionary(self, data):
        """
        Returns the values in a dictionary.
        @param data: dictionary values
        @return: Returns the values in a dictionary.
        """      
        text = ''
        
        for key in data:
            value = data[key]
            if isinstance(value, dict):
                text += '\n' + self.dictionary(value)
            elif isinstance(value, list):
                text += '\n' + self.list(value)
            else:
                text += '\n' + value
                
        return text
    
    def list(self, data):
        """
        Returns the values in a list.
        @param data: list of values
        @return: Returns the values in a list.
        """   
        text = ''
        
        for value in data:
            if isinstance(value, dict):
                text += '\n' + self.dictionary(value)
            elif isinstance(value, list):
                text += '\n' + self.list(value)
            else:
                text += '\n' + value
        
        return text