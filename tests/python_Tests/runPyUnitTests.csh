#!/bin/csh -f
#
#  Just in case there is no ~/caveData
#
setenv DEFAULT_HOST localhost
#
#  Unabiguously determine the directory of this script.
#
cd `dirname $0`
#
#  Get a list of the python unit tests here.
#
set pythonTests = `find . -maxdepth 1 -name 'Test*.py'`
#
# Run the tests
#
set npassed = 0
set nfailed = 0
foreach pythonTest ( $pythonTests )
    $pythonTest
    if ( $status == 0 ) then
        @ npassed ++
    else
        @ nfailed ++
    endif
end
echo $npassed test programs were completely successful
if ( $nfailed > 0 ) \
    echo $nfailed test programs failed at least one test case 
#
