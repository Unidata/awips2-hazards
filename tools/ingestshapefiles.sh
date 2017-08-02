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
# 26 Jan 16    7135        Joe Wakefield  Improved process of saving merged
#                                         shapefiles
# 18 May 16    17342       Ben.Phillippe  Added CWA/WFO column
#  3 Aug 16    20629       Joe Wakefield  Removed dx1/dx2 restriction, made -w
#                                         mandatory
#  1 Dec 16    20629       Jim Ramer      Now accepting either a directory or a
#                                         tar/zip file; -m option changed (shape
#                                         always saved, provides name); added -i
#                                         option to summarize current shapes

SCHEMA=mapdata
PGUSER=awips
PGPORT=5432
PSQLBINDIR='/awips2/psql/bin/'

if [ "$1" = "-i" ] ; then
   for tbl in daminundation riverpointinundation burnscararea ; do
       nt=`${PSQLBINDIR}psql -d maps -U ${PGUSER} -q -p ${PGPORT} -c \
            "\dt mapdata.${tbl}" | grep table | wc -l`
       if [ $nt -eq 1 ] ; then
           echo Have ${tbl} table
           cols=`${PSQLBINDIR}psql -d maps -U ${PGUSER} -q -p ${PGPORT} -c \
            "\d+ mapdata.${tbl}" | grep '|' | grep -vE 'Column|the_geom' | \
            cut '-d|' -f1 | tr -d ' ' | tr '\n' ','`
            cols=`echo $cols | sed 's/,$//g'`
            ${PSQLBINDIR}psql -d maps -U ${PGUSER} -q -p ${PGPORT} -c \
             "select $cols from  mapdata.${tbl}" | cat
       else
           echo ${tbl} table not found
       fi
   done | more
   exit
fi

if [ $# -lt 2 ] ;then
  scriptname=`basename $0`
  echo "Usage:"
  echo "$ $scriptname -b|-d|-r -s shapefilepath -m mergedfilename -c columnname -w CWA/WFO" 
  echo '      -- or --'
  echo "$ $scriptname -i" 
  echo "where:"
  echo " -i             - Summarize impact areas already stored."
  echo " -b|-d|-r       - database table where the merged shapefile is to be imported"
  echo "                  b: burnscararea, d: daminudation, r:riverpointinundation"
  echo " shapefilepath  - directory where shapefiles to be merged are located, or" 
  echo "                  full path to a tar, tgz, or zip file containing the shapes"
  echo " mergedfilename - optional name under which merged shapefile will be saved"
  echo " columnname     - optional name of column that needs to be changed to 'Name'"
  echo " CWA/WFO        - location associated with this data"
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

if [ -z $CWA ]
  then echo "-w CWA/WFO ID required."
  exit 1
fi

# If no -m supplied, use a shortened version of the area type
if [ -z $MERGEDFILENAME ]
  then MERGEDFILENAME=`echo ${TABLE} | sed 's/inundation//g'`
fi

SIMPLEVS=0.064,0.016,0.004,0.001
PGBINDIR='/awips2/postgresql/bin/'
TMPWRKDIR=

# make it so the argument can be either a directory with shape files
# or a tar/zip containing shape files.
while [ ! -d $SHAPEFILEPATH ] ; do
  shparg=$SHAPEFILEPATH
  TMPWRKDIR=/tmp/hydroTMPshapes$$
  SHAPEFILEPATH=$TMPWRKDIR
  echo $shparg | grep -iE 'tgz$|tar.gz$' >& /dev/null
  if [ $? -eq 0 ] ; then
      mkdir -p $TMPWRKDIR
      ( cd $TMPWRKDIR ; tar xzf $shparg ) && break
  fi
  echo $shparg | grep -i 'tar$' >& /dev/null
  if [ $? -eq 0 ] ; then
      mkdir -p $TMPWRKDIR
      ( cd $TMPWRKDIR ; tar xf $shparg ) && break
  fi
  echo $shparg | grep -i 'zip$' >& /dev/null
  if [ $? -eq 0 ] ; then
      mkdir -p $TMPWRKDIR
      ( cd $TMPWRKDIR ; unzip $shparg ) && break
  fi
  echo "$shparg does not exist. Please try again."
  exit 1
done

if [ "$TMPWRKDIR" != "" ] ; then
    find $TMPWRKDIR -mindepth 2 -type f -exec mv -f '{}' $TMPWRKDIR \;
    find $TMPWRKDIR -mindepth 1 -maxdepth 1 -type d -exec rm -rf '{}' \; 
fi

cd ${SHAPEFILEPATH}
rm -f ${MERGEDFILENAME}.*		#remove any existing merged shapes

#find all shapefiles in (this) directory and merge them
nshp=`find . -maxdepth 1 -name '*.shp' | wc -l`
find . -maxdepth 1 -name '*.shp' | sed 's/.shp$//g' | \
while read oneshape
   do
     echo ${oneshape}.shp
     if [ $nshp -eq 1 ] ; then
         cp ${oneshape}.shp ${MERGEDFILENAME}.shp
         cp ${oneshape}.dbf ${MERGEDFILENAME}.dbf
         cp ${oneshape}.shx ${MERGEDFILENAME}.shx
     else
         ${PGBINDIR}ogr2ogr -append -update -f "ESRI Shapefile" ${MERGEDFILENAME}.shp "${oneshape}.shp"
     fi
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

# Add Name column in case it's missing (shapefiles with no Name field)
nameyes=`${PSQLBINDIR}psql -d maps -U ${PGUSER} -q -p ${PGPORT} -c \
         "\d+ ${SCHEMA}.${TABLE}" | grep '^ *name *|' | wc -l`
if [ $nameyes -eq 0 ] ; then
    ${PSQLBINDIR}psql -d maps -U ${PGUSER} -q -p ${PGPORT} -c "
    ALTER TABLE ${SCHEMA}.${TABLE} ADD COLUMN Name varchar(50);
    "
fi

# force all name values to be set to the merged file name if blank.
${PSQLBINDIR}psql -d maps -U ${PGUSER} -q -p ${PGPORT} -c \
    "select gid,name from ${SCHEMA}.${TABLE}" | grep '| *$' | \
      tr -d ' ' | tr -d '|' | \
while read onegid ; do
    ${PSQLBINDIR}psql -d maps -U ${PGUSER} -q -p ${PGPORT} -c \
      "UPDATE ${SCHEMA}.${TABLE} SET name='$MERGEDFILENAME' WHERE gid='$onegid' ; "
done

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

# save the merged files
shpdir=/awips2/edex/data/utility/edex_static/site/$CWA/shapefiles/hazardServices
if [ ! -d $shpdir ] ; then
    mkdir -p $shpdir
    chmod 775 $shpdir
fi
mv ${MERGEDFILENAME}.* $shpdir
echo Saved merged shapefile:
ls $shpdir/${MERGEDFILENAME}.* | sed 's|/awips2/edex/data/utility/||g'

# clean up
if [ "$TMPWRKDIR" != "" ] ; then
    rm -rf $TMPWRKDIR
fi

echo "$0 completed."
