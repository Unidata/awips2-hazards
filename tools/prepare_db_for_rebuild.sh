# Modifies the DB to support most recent commits.
# WARNING!!!  WILL DELETE DATA!  Will you lose any hazard events you care about?
#
# With these tables deleted, EDEX is able to rebuild them with modified
# schemas when it is bounced.
/awips2/psql/bin/psql -U awips -d metadata --command="drop table practice_hazards cascade"
/awips2/psql/bin/psql -U awips -d metadata --command="drop table practice_hazard_attributes cascade"
/awips2/psql/bin/psql -U awips -d metadata --command="drop table practice_hazards_practice_hazard_attributes cascade"
/awips2/psql/bin/psql -U awips -d metadata --command="update plugin_info set initialized=FALSE where name='hazards'"
