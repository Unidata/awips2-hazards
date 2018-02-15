#!/bin/bash
# DR 29776 - Add insertTime column to producttext table to assist in saving
# product text.

PSQL="/awips2/psql/bin/psql"

cmd="
BEGIN;

ALTER TABLE IF EXISTS awips.producttext
ADD COLUMN inserttime timestamp without time zone NOT NULL DEFAULT now();

ALTER TABLE IF EXISTS awips.producttext
ALTER COLUMN inserttime DROP DEFAULT;

COMMIT;
"

echo "INFO: Modifying AWIPS producttext table..."

${PSQL} -U awipsadmin -d metadata -c "${cmd}"
retval=$?

echo "Done."
exit $retval
