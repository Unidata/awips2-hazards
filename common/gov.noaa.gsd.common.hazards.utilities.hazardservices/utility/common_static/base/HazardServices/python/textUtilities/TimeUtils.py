'''
Time-related utilities.
'''
import datetime
from GeneralConstants import VERIFY_MILLISECONDS, MILLIS_PER_MINUTE, SECONDS_PER_MINUTE

# Round the specified datetime object (or the current time if none is given)
# to the nearest time increment given by delta (or if the latter is not given,
# to the nearest minute).
def roundDatetime(timestamp=None, delta=datetime.timedelta(minutes=1)):
    """
    Author: Thierry Husson 2012 - Use it as you want but don't blame me.
            Stijn Nevens 2014 - Changed to use only datetime objects as variables
    """
    roundTo = delta.total_seconds()    
    if timestamp is None:
        timestamp = datetime.datetime.now()
    seconds = (timestamp - timestamp.min).seconds
    # // is a floor division, not a comment on following line:
    rounding = ((seconds + roundTo / 2) // roundTo) * roundTo
    return timestamp + datetime.timedelta(0, rounding - seconds, -timestamp.microsecond)

# Round the specified epoch time in milliseconds to the nearest time increment
# given by delta (or if the latter is not given, to the nearest minute).
def roundEpochTimeMilliseconds(milliseconds, delta=datetime.timedelta(minutes=1)):
    timestamp = epochTimeMillisToDatetime(milliseconds)
    timestamp = roundDatetime(timestamp, delta)
    return datetimeToEpochTimeMillis(timestamp)

# Convert the specified datetime object into an epoch time in milliseconds.
def datetimeToEpochTimeMillis(timestamp):
    return int(((timestamp - datetime.datetime.utcfromtimestamp(0)).total_seconds()) * 1000.0)

# Convert the specified epoch time in milliseconds to a datetime object.
def epochTimeMillisToDatetime(milliseconds):
    return datetime.datetime.utcfromtimestamp(milliseconds / 1000.0)

# Get the epoch time in seconds or milliseconds (whichever is supplied
# as input) that is equal to the beginning of the minute for the supplied
# time.
def minuteOf(timeInMillisOrSeconds):
    if timeInMillisOrSeconds == None:
        return 0
    if timeInMillisOrSeconds < VERIFY_MILLISECONDS:
        return timeInMillisOrSeconds - (timeInMillisOrSeconds % SECONDS_PER_MINUTE)
    return timeInMillisOrSeconds - (timeInMillisOrSeconds % MILLIS_PER_MINUTE)

# Return True if the two epoch times supplied (which must both be in
# either seconds or milliseconds) are within a minute of one another,
# False otherwise.
def isSameMinute(timeMillisOrSeconds1, timeMillisOrSeconds2) :
    if timeMillisOrSeconds1 == None or timeMillisOrSeconds2 == None :
        return False
    delta = timeMillisOrSeconds2 - timeMillisOrSeconds1
    if timeMillisOrSeconds1 < VERIFY_MILLISECONDS :
        return delta > -SECONDS_PER_MINUTE and delta < SECONDS_PER_MINUTE
    return delta > -MILLIS_PER_MINUTE and delta < MILLIS_PER_MINUTE

