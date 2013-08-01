"""
    Description: Constants used in some recommenders.
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    Aug 15, 2013  750       JRamer      Initial creation
    
    @author James.E.Ramer@noaa.gov
    @version 1.0
"""

# Some constants for working with time.
MILLIS_PER_SECOND = 1000
SECONDS_PER_HOUR = 3600
SECONDS_PER_MINUTE = 60
MILLIS_PER_HOUR = SECONDS_PER_HOUR*MILLIS_PER_SECOND
MILLIS_PER_MINUTE = SECONDS_PER_MINUTE*MILLIS_PER_SECOND

# This time is Jan 26,1970 at about 17Z in milliseconds, and is Jun 2, 2040
# at about 04Z in seconds.  If a time is less than this, we can safely
# assume it is in seconds.
VERIFY_MILLISECONDS = 2222222222

# Where there are hidden millisecond to second conversions
# (and vice-versa) in the underlying code, we want to allow a slop of
# 999 milliseconds when comparing two millisecond times for equivalence.
TIME_EQUIVALENCE_TOLERANCE = MILLIS_PER_SECOND -1
