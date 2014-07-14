"""
Constants related to VTEC processing.

    @author Chris.Golden@noaa.gov
    @version 1.0

"""

# Value in seconds used to represent that a time is set to "Until Further Notice".
# This works out to be Tue Jan 19 03:14:07 GMT 2038; this is unfortunately in the
# not too distant future, but must be this value for interoperability reasons.
#
# TODO: Change this value to the second equivalent of (date.max - Jan 01 1970)
# once interoperability is no longer a concern; see Redmine Task 2904 for details.
UFN_TIME_VALUE_SECS = float(2**31-1)
