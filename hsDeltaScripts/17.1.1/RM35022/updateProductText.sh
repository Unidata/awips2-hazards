#!/bin/bash
# DR 35022 - Recreate awips.producttext table
#              1) Remove productid column
#              2) Add mode column (i.e. practice or operational)
#              3) Change value column to text
#              4) Change event id column to character varying(255)
#
# NOTE:  Because some data doesn't transfer well (the old eventids column to
# the new eventid column), the table is dropped and recreated from scratch with
# no data.

PSQL="/awips2/psql/bin/psql"

cmd="
BEGIN;

DROP TABLE IF EXISTS awips.producttext;

CREATE TABLE awips.producttext
(
  eventid character varying(255) NOT NULL,
  key character varying(255) NOT NULL,
  officeid character varying(255) NOT NULL,
  productcategory character varying(255) NOT NULL,
  mode character varying(255) NOT NULL,
  segment text NOT NULL,
  value text,
  inserttime timestamp without time zone NOT NULL,
  CONSTRAINT producttext_pkey PRIMARY KEY (eventid, key, mode, officeid, productcategory, segment)
)
WITH (
  OIDS=FALSE
);

ALTER TABLE awips.producttext
  OWNER TO awipsadmin;
GRANT ALL ON TABLE awips.producttext TO awipsadmin;
GRANT SELECT, UPDATE, INSERT, TRUNCATE, DELETE, TRIGGER ON TABLE awips.producttext TO awips;

COMMIT;
"

echo "INFO: Modifying awips.producttext table..."

${PSQL} -U awipsadmin -d metadata -c "${cmd}"
retval=$?

echo "Done."
exit $retval
