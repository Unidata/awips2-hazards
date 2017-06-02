#!/bin/bash
# DR #10203 - Add issueTime to the primary key for the productdata table.

PSQL="/awips2/psql/bin/psql"

echo "INFO: Removing the current primary key for the productdata table"

${PSQL} -U awips -d metadata -q -c "ALTER TABLE IF EXISTS productdata DROP CONSTRAINT productdata_pkey;"

echo "INFO: Adding the updated primary key for the productdata table"

${PSQL} -U awips -d metadata -q -c "ALTER TABLE IF EXISTS productdata ADD CONSTRAINT productdata_pkey PRIMARY KEY (eventids, mode, productgeneratorname, issueTime);"

echo "Done."
