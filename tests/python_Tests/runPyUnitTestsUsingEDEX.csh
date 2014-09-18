#!/bin/csh -f
#
#  Unabiguously determine the directory of this script.
#
cd `dirname $0`
#
#  Unit tests by default get all needed files directly out of the source
#  code directories.  This tells the unit tests to get their localization
#  based configuration information from EDEX.
#
setenv LOCALIZATION_DATA_SOURCE EDEX
#
#
./runPyUnitTests.csh
#
