#!/bin/sh

###############################################################
# ./updateCentralProcessorHostname.sh CENTRAL_PROCESSOR_HOSTNAME
# E.g. ./updateCentralProcessorHostname.sh ewp6.protect.nssl
# Should affect only the following files:
#   CommonMetaData.py
#   HazardTypes.py
#   EventDrivenTools.py
#
# kevin.manross@noaa.gov
###############################################################

myHost=$1

find /awips2/edex/data/utility/common_static/base/HazardServices -type f -exec sed -i 's/CENTRAL_PROCESSOR_HOSTNAME = "[a-zA-Z0-9.]*"/CENTRAL_PROCESSOR_HOSTNAME = "'$myHost'"/g' {} \;




