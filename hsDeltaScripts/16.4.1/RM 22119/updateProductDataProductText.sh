#!/bin/bash
# DR 22119 - Add officeid column to productdata and producttext tables to
# assist in export/import to other sites. The officeid column can't contain
# null values, so 'unknown' will be used.

PSQL="/awips2/psql/bin/psql"

cmd="
BEGIN;

ALTER TABLE IF EXISTS awips.productdata
ADD COLUMN officeid character varying(8) NOT NULL DEFAULT 'unknown';

ALTER TABLE IF EXISTS awips.productdata
ALTER COLUMN officeid DROP DEFAULT;

ALTER TABLE IF EXISTS awips.productdata
DROP CONSTRAINT IF EXISTS productdata_pkey;

ALTER TABLE IF EXISTS awips.productdata
ADD CONSTRAINT productdata_pkey PRIMARY KEY (eventids, mode, productgeneratorname, issuetime, officeid);

ALTER TABLE IF EXISTS awips.producttext
ADD COLUMN officeid character varying(8) NOT NULL DEFAULT 'unknown';

ALTER TABLE IF EXISTS awips.producttext
ALTER COLUMN officeid DROP DEFAULT;

ALTER TABLE IF EXISTS awips.producttext
DROP CONSTRAINT IF EXISTS producttext_pkey;

ALTER TABLE IF EXISTS awips.producttext
ADD CONSTRAINT producttext_pkey PRIMARY KEY (eventids, key, productcategory, productid, segment, officeid);

COMMIT;
"

echo "INFO: Modifying AWIPS productdata and producttext tables..."

${PSQL} -U awipsadmin -d metadata -c "${cmd}"

echo "Done."