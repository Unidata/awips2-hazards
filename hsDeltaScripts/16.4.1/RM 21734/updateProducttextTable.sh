#!/bin/bash
# DR 21734 - Change type of segment column to text.

PSQL="/awips2/psql/bin/psql"

ALTER_SEGMENT="ALTER TABLE awips.producttext ALTER COLUMN segment SET DATA TYPE text;"

echo "INFO: Modifying AWIPS Product Text table..."

${PSQL} -U awipsadmin -d metadata -q -c "${ALTER_SEGMENT}"

echo "Done."

