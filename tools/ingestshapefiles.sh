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
# 10 Jan 17    7137        Jim Ramer      Will automatically pick up result of
#                                         parseWarngenTemplate.py, can use shape
#                                         file name as standin for feature name,
#                                         and cleans up unneeded columns.
# 24 Jan 17    7137        Jim Ramer      Refactor everything so that data for
#                                         each site can be handled completely
#                                         independently; accounts for service
#                                         backup.

SCHEMA=mapdata
PGUSER=awipsadmin
PGPORT=5432
PSQLBINDIR='/awips2/psql/bin/'
PH='-hdx1f'
AUTH=/awips2/fxa/bin/a2dbauth

if [ $# -lt 1 ] ;then
  scriptname=`basename $0`
  echo "Usage:"
  echo "$ $scriptname -b|-d|-r -x -s shapefilepath -m mergedfilename -c columnname -w CWA/WFO" 
  echo '      -- or --'
  echo "$ $scriptname -i -w CWA/WFO"
  echo "where:"
  echo " -i             - Summarize impact areas already stored."
  echo " -x             - Optionally force clearing out entire table in case logic that"
  echo "                  processes each CWA independently gets confused."
  echo " -b|-d|-r       - database table where the merged shapefile is to be imported"
  echo "                  b: burnscararea, d: daminudation, r:riverpointinundation"
  echo " shapefilepath  - directory where shapefiles to be merged are located, or" 
  echo "                  full path to a tar, tgz, or zip file containing the shapes"
  echo " mergedfilename - optional name under which merged shapefile will be saved"
  echo " columnname     - optional name of column that needs to be changed to 'Name'"
  echo " CWA/WFO        - localization associated with this data. With access to"
  echo "                  /awips2/edex/bin/setup.env can default to the primary site id."
  echo "example: ./ingestshapefiles.sh -d -s /scratch/OAX_Shapefiles/DamBreak -m mymergedfile -c dampoints -w OAX"
  exit -1
fi

cmdstr="ingestshapefiles.sh $*"

VMSHAPE="_null_"
DROPALL="no"

while getopts ":bdrixs:m:c:w:" opt; do
  case $opt in
    b)
      TABLE=burnscararea
      VMSHAPE=burnScar
      ;;
    d)
      TABLE=daminundation
      VMSHAPE=damInfo
      ;;
    r)
      TABLE=riverpointinundation
      ;;
    i)
      TABLE=show
      ;;
    x)
      DROPALL="yes"
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

if [ -z $CWA ] ; then
   if [ -e /awips2/edex/bin/setup.env ] ; then
       CWA=`cat /awips2/edex/bin/setup.env | grep AW_SITE_IDENTIFIER | \
            head -n 1 | cut '-d=' -f2 | tr -d ' '`
   else
       CWA=`ssh -q dx3 "cat /awips2/edex/bin/setup.env" | grep AW_SITE_IDENTIFIER | \
            head -n 1 | cut '-d=' -f2 | tr -d ' '`
   fi
   n=`echo -n $CWA | wc -c`
   if [ $n -ne 3 ] ;then
      echo "-w CWA/WFO ID required."
      exit 1
   fi
fi

# Attempt to make this still work for a dev environment
if [ -e /awips2/edex/bin/setup.env ] ; then
    grep localhost /awips2/edex/bin/setup.env >& /dev/null
    if [ $? -eq 0 ] ; then
        PGUSER=awipsadmin
        PH=''
        AUTH=''
    fi
fi
psqlcmd=`echo $AUTH ${PSQLBINDIR}psql -d maps $PH -U ${PGUSER} -q -p ${PGPORT}`

if [ "$TABLE" = "show" ] ; then
   for tbl in daminundation riverpointinundation burnscararea ; do
       nt=`$psqlcmd -c "\dt mapdata.${tbl}" | grep table | wc -l`
       if [ $nt -eq 1 ] ; then
           echo Have ${tbl} table
           cols=`$psqlcmd -c "\d+ mapdata.${tbl}" | grep '|' | grep -vE 'Column|the_geom' | \
                 cut '-d|' -f1 | tr -d ' ' | tr '\n' ','`
           cols=`echo $cols | sed 's/,$//g'`
           $psqlcmd -c "select $cols from mapdata.${tbl} where cwa='$CWA'" | cat
       else
           echo ${tbl} table not found
       fi
   done | more
   exit
fi

vmshapepath=/awips2/edex/data/utility/common_static/configured/$CWA/shapefiles/hazardServices/$VMSHAPE

if [ -z $SHAPEFILEPATH ] ; then
  if [ -e ${vmshapepath}.shp ] ; then
      echo "Processing based ONLY on shape file from warnGen vm/xml"
  else
      echo "-s shapefilepath required."
      exit 1
  fi
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
while [ ! -d "$SHAPEFILEPATH" ] ; do
  shparg=$SHAPEFILEPATH
  TMPWRKDIR=/tmp/hydroTMPshapes$$
  SHAPEFILEPATH=$TMPWRKDIR
  if [ "$shparg" == "" ] ; then
      mkdir -p $TMPWRKDIR
      break
  fi
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

# Our logic for updating this table independently for each CWA is dependent
# on having a bare minimum set of columns already existing in the table.
# It this bare minimum set is not there, clear out the whole table. 
n=0
if [ "$DROPALL" = "no" ] ; then
    n=`$psqlcmd -c "\d+ ${SCHEMA}.${TABLE}" | grep '|' | cut '-d|' -f1 | \
        tr -d ' ' | grep -E '^cwa$|^gid$|^name$' | wc -l`
fi
if [ $n -eq 3 ] ; then
    echo "Dropping contents of mapdata.${TABLE} for ${CWA}"
    $psqlcmd -c "delete from ${SCHEMA}.${TABLE} where cwa='$CWA' ;"
    brandnew=no
else
    if [ "$DROPALL" = "no" ] ; then
        echo "The minimum set of columns is missing from mapdata.${TABLE}"
        echo "Therefore, we are dropping entire contents of mapdata.${TABLE}"
        echo "You many need to recreate entries in this table for other CWAs."
    else
        echo "Dropping entire contents of mapdata.${TABLE} on request."
    fi
    $psqlcmd -c "
        DELETE FROM public.geometry_columns WHERE f_table_schema = '${SCHEMA}' AND f_table_name = '${TABLE}';
        DELETE from ${SCHEMA}.map_version WHERE table_name='${TABLE}';
        DROP TABLE IF EXISTS ${SCHEMA}.${TABLE}"
    brandnew=yes
fi
newtable=$brandnew

#find all shapefiles in (this) directory and merge them
if [ -e ${vmshapepath}.shp ] ; then
    ln -s ${vmshapepath}.shp fromWarngen_${VMSHAPE}.shp
    ln -s ${vmshapepath}.shx fromWarngen_${VMSHAPE}.shx
    ln -s ${vmshapepath}.dbf fromWarngen_${VMSHAPE}.dbf
fi
mycols=""
passdata=/tmp/passdata$$
nshp=`find . -maxdepth 1 -name '*.shp' | wc -l`
find . -maxdepth 1 -name '*.shp' | cut -c3-999 | sed 's/.shp$//g' | \
while read oneshape
   do

     echo "${oneshape}.shp"
     echo "Importing ${oneshape} into ${SCHEMA}.${TABLE} ..."
     if [ "$newtable" = "yes" ] ; then
         ${PGBINDIR}shp2pgsql -W LATIN1 -s 4326 -g the_geom -I "${oneshape}.shp" ${SCHEMA}.${TABLE} | \
              $psqlcmd -f -
     else
         if [ "$mycols" = "" ] ; then
             mycols=`$psqlcmd -c \
                     "\d+ ${SCHEMA}.${TABLE}" | grep '|' | cut '-d|' -f1 | \
                     grep -v Column | grep -v the_geom | tr -d ' ' | tr '\n' ' '`
         fi
         # For each new shape file we make sure we have all the columns we need.
         ${PGBINDIR}shp2pgsql -W LATIN1 -s 4326 -g the_geom -a "${oneshape}.shp" ${SCHEMA}.${TABLE} > \
              $passdata
         newcols=`grep 'INSERT INTO' $passdata | head -n 1 | cut '-d)' -f1 | cut '-d(' -f2 | \
                  tr -d '"' | tr ',' '\n' | grep -v the_geom |tr '\n' ' ' `
         for onecol in $newcols ; do
             colyes=`echo " $mycols " | grep " $onecol " | wc -l`
             if [ $colyes -eq 0 ] ; then
                 $psqlcmd -c "ALTER TABLE ${SCHEMA}.${TABLE} ADD COLUMN $onecol varchar(80);"
             fi
         done
         cat $passdata | $psqlcmd -f -
         rm -rf $passdata
     fi
     newtable=no

     # Record the columns we now have
     mycols=`$psqlcmd -c "\d+ ${SCHEMA}.${TABLE}" | grep '|' | cut '-d|' -f1 | \
             grep -v Column | grep -v the_geom | tr -d ' ' | tr '\n' ' '`

     # If needed add columns with the original shape file name and the cwa
     colyes=`echo " $mycols " | grep " orig_shape_name " | wc -l`
     if [ $colyes -eq 0 ] ; then
         $psqlcmd -c "ALTER TABLE ${SCHEMA}.${TABLE} ADD COLUMN orig_shape_name varchar(80);"
     fi
     colyes=`echo " $mycols " | grep " cwa " | wc -l`
     if [ $colyes -eq 0 ] ; then
         $psqlcmd -c "ALTER TABLE ${SCHEMA}.${TABLE} ADD COLUMN cwa varchar(4);"
     fi

     # Set original shape file name and cwa for records just added
     # Changes underscores in file name into spaces, as this can in certain
     # circumstances directly end up in text products.
     echo "$oneshape" | grep fromWarngen_ >& /dev/null
     if [ $? -eq 0 ] ; then
         origshape="$oneshape"
     else
         origshape=`echo "$oneshape" | tr '_' ' '`
     fi
     $psqlcmd -c "select gid,cwa from ${SCHEMA}.${TABLE}" | grep '| *$' | \
          tr -d ' ' | tr -d '|' | \
     while read onegid ; do
         $psqlcmd -c "UPDATE ${SCHEMA}.${TABLE} SET orig_shape_name='$origshape' WHERE gid='$onegid' ; "
         $psqlcmd -c "UPDATE ${SCHEMA}.${TABLE} SET cwa='$CWA' WHERE gid='$onegid' ; "
     done

     if [ $nshp -eq 1 ] ; then
         cp "${oneshape}.shp" "${MERGEDFILENAME}.shp"
         cp "${oneshape}.dbf" "${MERGEDFILENAME}.dbf"
         cp "${oneshape}.shx" "${MERGEDFILENAME}.shx"
     else
         ${PGBINDIR}ogr2ogr -append -update -f "ESRI Shapefile" ${MERGEDFILENAME}.shp "${oneshape}.shp"
     fi
done

if [ -e fromWarngen_${VMSHAPE}.shp ] ; then
    rm -rf fromWarngen_${VMSHAPE}.*
fi

if [ "$brandnew" = "yes" ] ; then
    $psqlcmd -c "INSERT INTO ${SCHEMA}.map_version (table_name, filename) values ('${TABLE}','${MERGEDFILENAME}.shp');
        SELECT AddGeometryColumn('${SCHEMA}','${TABLE}','the_geom_0','4326',(SELECT type FROM public.geometry_columns WHERE f_table_schema='${SCHEMA}' and f_table_name='${TABLE}' and f_geometry_column='the_geom'),2);
        UPDATE ${SCHEMA}.${TABLE} SET the_geom_0=ST_Segmentize(the_geom,0.1);
        CREATE INDEX ${TABLE}_the_geom_0_gist ON ${SCHEMA}.${TABLE} USING gist(the_geom_0);"
else
    $psqlcmd -c "UPDATE ${SCHEMA}.${TABLE} SET the_geom_0=ST_Segmentize(the_geom,0.1) where cwa='$CWA';"
fi

# Record the columns we now have
mycols=`$psqlcmd -c "\d+ ${SCHEMA}.${TABLE}" | grep '|' | cut '-d|' -f1 | \
        grep -v Column | grep -v the_geom | grep -v gid | grep -v cwa | \
        tr -d ' ' | tr '\n' ' '`

# Add Name column in case it's missing
colyes=`echo " $mycols " | grep " name " | wc -l`
if [ $colyes -eq 0 ] ; then
    $psqlcmd -c "ALTER TABLE ${SCHEMA}.${TABLE} ADD COLUMN Name varchar(80);"
fi

# For any blank values of the name column, set them to alternate naming column.
if [ -n "$COLUMNNAME" ] ; then 
    $psqlcmd -c "select gid,name from ${SCHEMA}.${TABLE} where cwa='$CWA'" | grep '| *$' | \
          tr -d ' ' | tr -d '|' | \
    while read onegid ; do
        $psqlcmd -c "UPDATE ${SCHEMA}.${TABLE} SET name=$COLUMNNAME WHERE gid='$onegid' ; "
    done
fi

# force all name values to be set to the original shape file name if still blank.
$psqlcmd -c "select gid,name from ${SCHEMA}.${TABLE} where cwa='$CWA'" | grep '| *$' | \
      tr -d ' ' | tr -d '|' | \
while read onegid ; do
    $psqlcmd -c "UPDATE ${SCHEMA}.${TABLE} SET name=orig_shape_name WHERE gid='$onegid' ; "
done

# Remove any entries from a warnGen shape file for which there is a user
# shape file entry with the same name.
tmp1=/tmp/usershapenames$$
$psqlcmd -c "select gid,name,orig_shape_name from ${SCHEMA}.${TABLE} where cwa='$CWA'" | \
     grep '^ *[0-9]' > $tmp1
cat $tmp1 | grep fromWarngen_ | cut '-d|' -f1,2 | tr -d '|' | \
while read gid name
   do
       cat $tmp1 | grep -v fromWarngen_ | grep -qi "| *$name *|"
       if [ $? == 0 ] ; then
           $psqlcmd -c "delete from ${SCHEMA}.${TABLE} where gid='$gid' ;"
       fi
   done
rm -f $tmp1

# now drop any columns other than gid, name, and cwa.
for onecol in $mycols ; do
    if [ "$onecol" != "name" ] ; then
        $psqlcmd -c "alter table ${SCHEMA}.${TABLE} drop column $onecol ;" 
    fi
done

# For some unknown reason, using prepackaged form of the psql command does
# not work in this section, so expand it out.
if [ -n "$SIMPLEVS" ] ; then
  if [ "$brandnew" = "yes" ] ; then
     echo "  Creating simplification levels ${SIMPLEVS}..."
     IFS=",	 "
     for LEV in $SIMPLEVS ; do
       echo "    Creating simplified geometry level $LEV ..."
       IFS="."
       SUFFIX=
       for x in $LEV ; do SUFFIX=${SUFFIX}_${x} ; done
       $AUTH ${PSQLBINDIR}psql -d maps $PH -U ${PGUSER} -q -p ${PGPORT} -c \
       "SELECT AddGeometryColumn('${SCHEMA}','${TABLE}','the_geom${SUFFIX}','4326',(SELECT type FROM public.geometry_columns WHERE f_table_schema='${SCHEMA}' and f_table_name='${TABLE}' and f_geometry_column='the_geom'),2);
       UPDATE ${SCHEMA}.${TABLE} SET the_geom${SUFFIX}=ST_Segmentize(ST_Multi(ST_SimplifyPreserveTopology(the_geom,${LEV})),0.1);
       CREATE INDEX ${TABLE}_the_geom${SUFFIX}_gist ON ${SCHEMA}.${TABLE} USING gist(the_geom${SUFFIX});"
     done
  else
     echo "  Updating simplification levels ${SIMPLEVS}..."
     IFS=",	 "
     for LEV in $SIMPLEVS ; do
       echo "    Updating simplified geometry level $LEV ..."
       IFS="."
       SUFFIX=
       for x in $LEV ; do SUFFIX=${SUFFIX}_${x} ; done
       $AUTH ${PSQLBINDIR}psql -d maps $PH -U ${PGUSER} -q -p ${PGPORT} -c \
         "UPDATE ${SCHEMA}.${TABLE} SET the_geom${SUFFIX}=ST_Segmentize(ST_Multi(ST_SimplifyPreserveTopology(the_geom,${LEV})),0.1) where cwa='$CWA';"
     done
  fi
fi
$AUTH ${PGBINDIR}vacuumdb -d maps -t ${SCHEMA}.${TABLE} $PH -U ${PGUSER} -p ${PGPORT} -qz

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
#
# Record the exact form of the ingestshapefiles.sh command used, just in
# case it is not default and we need to reuse same form later
#
echo "$cmdstr" >> ~/wgn2hazSer_commands.txt
