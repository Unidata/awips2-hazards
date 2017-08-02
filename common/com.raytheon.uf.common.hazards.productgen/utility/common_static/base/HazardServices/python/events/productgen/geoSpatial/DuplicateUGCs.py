#    Description: Contains UGCs with names which are duplicated (or similar)
#    within a WFO.  If a hazard contains one or more of these UGCs, the
#    containing state will be listed in the text.  In some fields (e.g.
#    LocationsAffected when no city is nearby and summary headlines of WarnGen
#    followup products) the containing state would normally not be listed.
#
#    SOFTWARE HISTORY
#    Date         Ticket#    Engineer    Description
#    ------------ ---------- ----------- --------------------------
#    Sep 20, 2016 21609      Kevin.Bisanz Initial creation

# NOTES:
#  1) This variable must be named the same as the file name (minus .py)
#  2) This variable must be a list.
#  3) Creating or modifying a localized version of this file requires a restart
#     of Hazard Services for the changes to take effect.

# Example:
#DuplicateUGCs = [
## Bristol Rhode Island zone
#'RIZ005',
## Northern Bristol Massachusetts zone
#'MAZ017',
##Southern Bristol Massachusetts zone
#'MAZ020',
## Bristol Rhode Island county
#'RIC001',
## Bristol Massachusetts county
#'MAC005',
#]
DuplicateUGCs = []
