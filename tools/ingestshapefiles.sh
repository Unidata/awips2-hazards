#!/bin/bash
##
# This software was developed and / or modified by the
# National Oceanic and Atmospheric Administration (NOAA)
# Earth System Research Laboratory (ESRL)
# Global Systems Division (GSD)
# 325 Broadway, Boulder, CO 80305
##
#
# This script manages burn scar, dam break, and river inundation shapefiles in
# the maps database.
#
# SOFTWARE HISTORY
# Date         Ticket #    Engineer       Description
# ------------ --------    -------------- -------------------------
# July 2015    7135        Nkosi Muse     Initial version
# 19 Aug 15    7135        Joe Wakefield  Improved arguments management
# 26 Jan 16    7135        Joe Wakefield  Improved process of saving merged shapefiles
# 18 May 18    17342       Ben.Phillippe  Added CWA/WFO column

## Good to go?
hostid=`hostname | cut -c 1-3`
if [ "$hostid" != "dx1" ] && [ "$hostid" != "dx2" ]
then
  echo "ERROR : $0 must be run on dx1 or dx2"
  exit 1
fi

if [ $# -lt 2 ] ;then
  echo "Usage:"
  echo "$ scriptname -b|-d|-r -s shapefilepath -m mergedfilename -c columnname -w CWA/WFO" 
  echo "where:"
  echo " -b|-d|-r       - database table where the merged shapefile is to be imported"
  echo "                  b: burnscararea, d: daminudation, r:riverpointinundation"
  echo " CWA/WFO        - location associated with this data"
  echo " shapefilepath  - directory where shapefiles to be merged are located" 
  echo " mergedfilename - optional name under which merged shapefile will be saved"
  echo " columnname     - optional name of column that needs to be changed to 'Name'"
  echo "example: ./ingestshapefiles.sh -d -s /scratch/OAX_Shapefiles/DamBreak -m mymergedfile -c dampoints -w OAX"
  exit -1
fi

while getopts ":bdrs:m:c:w:" opt; do
  case $opt in
    b)
      TABLE=burnscararea
      ;;
    d)
      TABLE=daminundation
      ;;
    r)
      TABLE=riverpointinundation
      ;;
    s)
      SHAPEFILEPATH="$OPTARG"
      ;;
    m)
      MERGEDFILENAME="$OPTARG"
      ;;
    c)
      COLUMNNAME="$OPTARG"
      ;;
    w)
      CWA="$OPTARG"
    ;;
    \?)
      echo "Invalid option: -$OPTARG. Must input one of -b|-d|-r." >&2
      exit 1
      ;;
  esac
done 

if [ -z $TABLE ]
  then echo "Must input one of -b|-d|-r."
  exit 1
fi

if [ -z $SHAPEFILEPATH ]
  then echo "-s shapefilepath required."
  exit 1
fi

saveMerge=""
if [ -z $MERGEDFILENAME ]
  then MERGEDFILENAME=merged
  saveMerge=no
fi

SIMPLEVS=0.064,0.016,0.004,0.001
SCHEMA=mapdata
PGUSER=awips
PGPORT=5432
PGBINDIR='/awips2/postgresql/bin/'
PSQLBINDIR='/awips2/psql/bin/'
SAVEDIR=xxxSaveShapesxxx

if [ ! -d $SHAPEFILEPATH ]
  then echo "$SHAPEFILEPATH does not exist. Please try again."
  exit 1
fi
cd ${SHAPEFILEPATH}
mkdir -p $SAVEDIR				#put saved collection out of sight
rm -f ${SAVEDIR}/${MERGEDFILENAME}.*		#remove any existing merged shapes
FILES=`find . -maxdepth 1 -name '*.shp'`	#find all shapefiles in (this) directory 

#${PGBINDIR}ogr2ogr -a_srs EPSG:4326 ${MERGEDFILENAME}.shp  #create shapefile to merge to
for i in $FILES					#merge all shapefiles
   do
     ${PGBINDIR}ogr2ogr -append -update -f "ESRI Shapefile" ${MERGEDFILENAME}.shp $i
done

echo "  Importing ${MERGEDFILENAME} into ${SCHEMA}.${TABLE} ..."
${PSQLBINDIR}psql -d maps -U ${PGUSER} -q -p ${PGPORT} -c "
    DELETE FROM public.geometry_columns WHERE f_table_schema = '${SCHEMA}' AND f_table_name = '${TABLE}';
    DELETE from ${SCHEMA}.map_version WHERE table_name='${TABLE}';
    DROP TABLE IF EXISTS ${SCHEMA}.${TABLE}
"
${PGBINDIR}shp2pgsql -W LATIN1 -s 4326 -g the_geom -I ${MERGEDFILENAME}.shp ${SCHEMA}.${TABLE} | ${PSQLBINDIR}psql -d maps -U ${PGUSER} -q -p ${PGPORT} -f -
${PSQLBINDIR}psql -d maps -U ${PGUSER} -q -p ${PGPORT} -c "
    INSERT INTO ${SCHEMA}.map_version (table_name, filename) values ('${TABLE}','${MERGEDFILENAME}.shp');
    SELECT AddGeometryColumn('${SCHEMA}','${TABLE}','the_geom_0','4326',(SELECT type FROM public.geometry_columns WHERE f_table_schema='${SCHEMA}' and f_table_name='${TABLE}' and f_geometry_column='the_geom'),2);
    UPDATE ${SCHEMA}.${TABLE} SET the_geom_0=ST_Segmentize(the_geom,0.1);
    CREATE INDEX ${TABLE}_the_geom_0_gist ON ${SCHEMA}.${TABLE} USING gist(the_geom_0);
"

if [ -n "$COLUMNNAME" ]
  then ${PSQLBINDIR}psql -d maps -U ${PGUSER} -q -p ${PGPORT} -c "
       ALTER TABLE ${SCHEMA}.${TABLE} RENAME COLUMN ${COLUMNNAME} TO Name;
       "
fi

if [ -n "$SIMPLEVS" ] ; then
  echo "  Creating simplification levels ${SIMPLEVS}..."
  IFS=",	 "
  for LEV in $SIMPLEVS ; do
    echo "    Creating simplified geometry level $LEV ..."
    IFS="."
    SUFFIX=
    for x in $LEV ; do SUFFIX=${SUFFIX}_${x} ; done
    ${PSQLBINDIR}psql -d maps -U ${PGUSER} -q -p ${PGPORT} -c "
    SELECT AddGeometryColumn('${SCHEMA}','${TABLE}','the_geom${SUFFIX}','4326',(SELECT type FROM public.geometry_columns WHERE f_table_schema='${SCHEMA}' and f_table_name='${TABLE}' and f_geometry_column='the_geom'),2);
    UPDATE ${SCHEMA}.${TABLE} SET the_geom${SUFFIX}=ST_Segmentize(ST_Multi(ST_SimplifyPreserveTopology(the_geom,${LEV})),0.1);
    CREATE INDEX ${TABLE}_the_geom${SUFFIX}_gist ON ${SCHEMA}.${TABLE} USING gist(the_geom${SUFFIX});"
  done
fi
${PSQLBINDIR}psql -d maps -U ${PGUSER} -q -p ${PGPORT} -c "ALTER TABLE ${SCHEMA}.${TABLE} ADD COLUMN cwa varchar(4); update ${SCHEMA}.${TABLE} SET cwa='${CWA}'"
${PGBINDIR}vacuumdb -d maps -t ${SCHEMA}.${TABLE} -U ${PGUSER} -p ${PGPORT} -qz

if [ -n "$saveMerge" ]
  then rm merged.*
else
  mv ${MERGEDFILENAME}.* ${SAVEDIR}/
fi

echo "$0 completed."
