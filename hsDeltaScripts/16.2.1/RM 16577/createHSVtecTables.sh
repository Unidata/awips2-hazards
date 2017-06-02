#!/bin/bash
# DR #16577 - Store Hazard Services VTEC Records

PSQL="/awips2/psql/bin/psql"

echo "INFO: Creating Hazard Services VTEC table...."

${PSQL} -U awips -d metadata -q -c "CREATE TABLE hazard_event_vtec
(
  etn integer NOT NULL,
  officeid character varying(255) NOT NULL,
  phen character varying(255) NOT NULL,
  sig character varying(255) NOT NULL,
  ugczone character varying(255) NOT NULL,
  act character varying(255),
  downgrade_from_act character varying(255),
  downgrade_from_etn bigint,
  downgrade_from_key character varying(255),
  downgrade_from_phen character varying(255),
  downgrade_from_sig character varying(255),
  downgrade_from_subtype character varying(255),
  endtime timestamp without time zone,
  eventid character varying(255),
  hdln character varying(255),
  crest timestamp without time zone,
  fallbelow timestamp without time zone,
  floodrecord character varying(255),
  floodseverity character varying(255),
  immediatecause character varying(255),
  pointid character varying(255),
  riseabove timestamp without time zone,
  hvtecstr character varying(255),
  issuetime timestamp without time zone,
  key character varying(255),
  phensig character varying(255),
  pil character varying(255),
  previousend timestamp without time zone,
  previousstart timestamp without time zone,
  seg integer,
  starttime timestamp without time zone,
  status character varying(255),
  subtype character varying(255),
  ufn integer,
  upgrade_from_act character varying(255),
  upgrade_from_etn bigint,
  upgrade_from_key character varying(255),
  upgrade_from_phen character varying(255),
  upgrade_from_sig character varying(255),
  upgrade_from_subtype character varying(255),
  vtecstr character varying(255),
  CONSTRAINT hazard_event_vtec_pkey PRIMARY KEY (etn, officeid, phen, sig, ugczone)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE hazard_event_vtec
  OWNER TO awips;"

echo "INFO: Hazard Services VTEC table successfully created."
echo "INFO: Creating Hazard Services practice VTEC table...."

${PSQL} -U awips -d metadata -q -c "CREATE TABLE hazard_event_vtec_practice
(
  etn integer NOT NULL,
  officeid character varying(255) NOT NULL,
  phen character varying(255) NOT NULL,
  sig character varying(255) NOT NULL,
  ugczone character varying(255) NOT NULL,
  act character varying(255),
  downgrade_from_act character varying(255),
  downgrade_from_etn bigint,
  downgrade_from_key character varying(255),
  downgrade_from_phen character varying(255),
  downgrade_from_sig character varying(255),
  downgrade_from_subtype character varying(255),
  endtime timestamp without time zone,
  eventid character varying(255),
  hdln character varying(255),
  crest timestamp without time zone,
  fallbelow timestamp without time zone,
  floodrecord character varying(255),
  floodseverity character varying(255),
  immediatecause character varying(255),
  pointid character varying(255),
  riseabove timestamp without time zone,
  hvtecstr character varying(255),
  issuetime timestamp without time zone,
  key character varying(255),
  phensig character varying(255),
  pil character varying(255),
  previousend timestamp without time zone,
  previousstart timestamp without time zone,
  seg integer,
  starttime timestamp without time zone,
  status character varying(255),
  subtype character varying(255),
  ufn integer,
  upgrade_from_act character varying(255),
  upgrade_from_etn bigint,
  upgrade_from_key character varying(255),
  upgrade_from_phen character varying(255),
  upgrade_from_sig character varying(255),
  upgrade_from_subtype character varying(255),
  vtecstr character varying(255),
  CONSTRAINT hazard_event_vtec_practice_pkey PRIMARY KEY (etn, officeid, phen, sig, ugczone)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE hazard_event_vtec_practice
  OWNER TO awips;
"
echo "INFO: Hazard Services practice VTEC table successfully created."
echo "Done."