/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.product.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.geotools.coverage.grid.GeneralGridEnvelope;
import org.geotools.coverage.grid.GeneralGridGeometry;
import org.geotools.geometry.GeneralEnvelope;
import org.geotools.geometry.jts.JTS;
import org.opengis.referencing.operation.MathTransform;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.warning.config.GridSpacing;
import com.raytheon.uf.common.dataplugin.warning.gis.GeospatialData;
import com.raytheon.uf.common.dataplugin.warning.portions.GisUtil;
import com.raytheon.uf.common.dataplugin.warning.portions.GisUtil.Direction;
import com.raytheon.uf.common.dataplugin.warning.portions.PortionsUtil;
import com.raytheon.uf.common.geospatial.MapUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;
import com.raytheon.uf.viz.core.IExtent;
import com.raytheon.uf.viz.core.PixelExtent;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Description: Handles computation of the county portions falling within a
 * hazard polygon.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 24, 2014            daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class PartsOfCounty {

    private final IUFStatusHandler statusHandler = UFStatus.getHandler(this
            .getClass());

    private static final String EXTREME = "Extreme";

    private static final String CENTRAL = "Central";

    private static Map<String, Map<String, Geometry>> geometryOfCountyUgcsMap = new HashMap<>();

    private static Map<String, MathTransform> localToLatLonForSite = new HashMap<>();

    private static Map<String, GeneralGridGeometry> generalGridGeometryForSite = new HashMap<>();

    private static Map<String, String> stateCodeMap;

    private CountyAreaRetriever countyAreaRetriever;

    private FIPMapBuilder fipMapBuilder;

    private GridSpacingRetriever gridSpacingRetriever;

    private IDirectionsRetriever directionsRetriever;

    PartsOfCounty() {
        this.countyAreaRetriever = new CountyAreaRetriever();
        this.fipMapBuilder = new FIPMapBuilder();
        this.gridSpacingRetriever = new GridSpacingRetriever();
        this.directionsRetriever = new DirectionsRetriever();
    }

    /*
     * Returns the Geometry for a given combination of WFO id and county UGC.
     * Separate Map objects are maintained for each site and held within another
     * static map, which is initially empty. If data is requested for a site for
     * which no previous requests have been made, the UGC to Geometry Map for
     * that site is built at that time.
     */
    private Geometry geometryOfCountyUGC(String site, String ugc) {
        Map<String, Geometry> siteGeometryMap = geometryOfCountyUgcsMap
                .get(site);
        if (siteGeometryMap != null) {
            return siteGeometryMap.get(ugc);
        }

        if (stateCodeMap == null) {
            stateCodeMap = fipMapBuilder
                    .buildFIPStateIndexToAbbreviationMapping();
        }

        /*
         * As we step through all the county geometries, we accumulate them in a
         * list, which will then be used to create a MathTransform object.
         */
        List<Geometry> allCountyGeoms = new ArrayList<>();
        GeospatialData[] countyAreas = null;

        countyAreas = countyAreaRetriever.getCountyAreasForSite(site);
        if (countyAreas == null) {
            return null;
        }

        siteGeometryMap = buildSiteGeometryMap(allCountyGeoms, countyAreas);

        geometryOfCountyUgcsMap.put(site, siteGeometryMap);

        /*
         * Now we construct a MathTransform and cache it for use elsewhere.
         */
        Coordinate c = new GeometryFactory().buildGeometry(allCountyGeoms)
                .getCentroid().getCoordinate();
        MathTransform latLonToLocal = null;
        try {
            latLonToLocal = MapUtil.getTransformFromLatLon(MapUtil
                    .constructStereographic(MapUtil.AWIPS_EARTH_RADIUS,
                            MapUtil.AWIPS_EARTH_RADIUS, c.y, c.x));
            localToLatLonForSite.put(site, latLonToLocal.inverse());
        } catch (Exception e) {
            statusHandler
                    .handle(Priority.SIGNIFICANT,
                            "Map transformation error while calculating overlap of hazard event and counties for "
                                    + site, e);
            return siteGeometryMap.get(ugc);
        }

        GridSpacing gridSpacing = null;
        try {
            gridSpacing = gridSpacingRetriever.gridSpacing(site);
        } catch (Exception e) {
            statusHandler.handle(Priority.SIGNIFICANT,
                    "Error loading grid spacing configuration", e);
            return siteGeometryMap.get(ugc);
        }

        countyAreasToGridGeometry(site, countyAreas, latLonToLocal, gridSpacing);

        return siteGeometryMap.get(ugc);
    }

    private HashMap<String, Geometry> buildSiteGeometryMap(
            List<Geometry> allCountyGeoms, GeospatialData[] countyAreas) {
        HashMap<String, Geometry> siteGeometryMap;
        siteGeometryMap = new HashMap<>();

        for (int aaa = 0; aaa < (countyAreas.length); aaa++) {
            allCountyGeoms.add(countyAreas[aaa].getGeometry());
            try {
                String fipsData = (String) countyAreas[aaa].getAttributes()
                        .get(CountyAreaRetriever.FIPS);
                if (fipsData.length() < 5) {
                    continue;
                }
                String stCode = stateCodeMap.get(fipsData.substring(0, 2));
                if (stCode == null) {
                    continue;
                }
                siteGeometryMap.put(stCode + "C" + fipsData.substring(2, 5),
                        countyAreas[aaa].getGeometry());
            } catch (Exception e) {
                continue;
            }
        }
        return siteGeometryMap;
    }

    /**
     * This was copied from legacy WarngenLayer.
     */
    private void countyAreasToGridGeometry(String site,
            GeospatialData[] countyAreas, MathTransform latLonToLocal,
            GridSpacing gridSpacing) {
        List<Geometry> locals = new ArrayList<>();
        for (int aaa = 0; aaa < (countyAreas.length); aaa++) {
            try {
                Geometry local = JTS.transform(countyAreas[aaa].getGeometry(),
                        latLonToLocal);
                locals.add(local);
            } catch (Exception e) {
                statusHandler.handle(Priority.SIGNIFICANT,
                        "JTS.transform failed for " + site, e);
            }
        }
        Envelope env = new GeometryFactory().buildGeometry(locals)
                .getEnvelopeInternal();
        IExtent localExtent = new PixelExtent(env.getMinX(), env.getMaxX(),
                env.getMinY(), env.getMaxY());
        int nx = 600;
        int ny = 600;
        boolean keepAspectRatio = true;
        if ((gridSpacing != null) && (gridSpacing.getNx() != null)
                && (gridSpacing.getNy() != null)) {
            nx = gridSpacing.getNx();
            ny = gridSpacing.getNy();
            keepAspectRatio = gridSpacing.isKeepAspectRatio();
        }

        double xinc, yinc;
        double width = localExtent.getWidth();
        double height = localExtent.getHeight();
        if (!keepAspectRatio) {
            xinc = (width / nx);
            yinc = (height / ny);
        } else {
            if (width > height) {
                ny = (int) ((height * nx) / width);
            } else if (height > width) {
                nx = (int) ((width * ny) / height);
            }
            xinc = yinc = (width / nx);
        }
        double minX = localExtent.getMinX() - xinc;
        double maxX = localExtent.getMaxX() + xinc;
        double minY = localExtent.getMinY() - yinc;
        double maxY = localExtent.getMaxY() + yinc;
        GeneralGridEnvelope range = new GeneralGridEnvelope(new int[] { 0, 0 },
                new int[] { nx, ny }, false);
        GeneralEnvelope ge = new GeneralEnvelope(new double[] { minX, maxY },
                new double[] { maxX, minY });
        generalGridGeometryForSite
                .put(site, new GeneralGridGeometry(range, ge));
    }

    void addPortionsDescriptionToEvent(Geometry polygonHazardGeometry,
            IHazardEvent event, String site) {

        /*
         * Add a portion descriptor for each UGC; start by initializing a blank
         * portion for each UGC, so we can then break out of our main UGC loop
         * arbitrarily.
         */
        Serializable serializableUgcList = event
                .getHazardAttribute(HazardConstants.UGCS);
        @SuppressWarnings("unchecked")
        List<String> ugcList = (List<String>) serializableUgcList;
        Map<String, String> portionDescriptions = new HashMap<>();

        /* Main UGC loop; only construct one PortionsUtil object. */
        PortionsUtil portionsUtil = null;
        for (String ugc : ugcList) {
            Geometry countyGeometry = null;
            try {
                /*
                 * Call this first, because it constructs some things we then
                 * need to construct our PortionsUtil object.
                 */
                countyGeometry = geometryOfCountyUGC(site, ugc);
                if (countyGeometry == null) {
                    throw new RuntimeException("no countyGeometry for " + ugc);
                }
            } catch (Exception e) {
                statusHandler.handle(Priority.SIGNIFICANT,
                        "Error in determining counties affected by hazard event for ugc: "
                                + ugc, e);
                continue;
            }
            try {
                if (portionsUtil == null) {
                    portionsUtil = new PortionsUtil(site,
                            generalGridGeometryForSite.get(site),
                            localToLatLonForSite.get(site));
                }
            } catch (Exception e) {
                statusHandler.handle(Priority.SIGNIFICANT,
                        "Could not construct a PortionsUtil object", e);
                break;
            }
            EnumSet<Direction> directionSet = null;
            try {
                directionSet = directionsRetriever.retrieveDirections(
                        polygonHazardGeometry, portionsUtil, ugc,
                        countyGeometry);
            } catch (Exception e) {
                statusHandler.handle(Priority.SIGNIFICANT,
                        "Call to portionsUtil.getPortions() failed for " + ugc,
                        e);
                continue;
            }

            /*
             * Finally, if we have a non-empty parts list, put it together into
             * a plain language part of county description.
             */
            List<String> areaPartsList = GisUtil.asStringList(directionSet);
            if (areaPartsList == null) {
                portionDescriptions.put(ugc, "");
                continue;
            }
            String portionDesc = portionDescriptionFromAreaParts(areaPartsList);

            portionDescriptions.put(ugc, portionDesc);

        }/* end main ugc loop. */

        event.addHazardAttribute(HazardConstants.UGC_PORTIONS,
                (HashMap<String, String>) portionDescriptions);

    }/* end addPortionsDescriptionToEvent() */

    private String portionDescriptionFromAreaParts(List<String> areaPartsList) {
        String portionDesc = "";
        for (String areaPart : areaPartsList) {
            areaPart = toMixedCase(areaPart);
            if (portionDesc.equals("")) {
                portionDesc = areaPart;
            } else if (portionDesc.equalsIgnoreCase(EXTREME)
                    || areaPart.equalsIgnoreCase(CENTRAL)) {
                portionDesc = portionDesc + " " + areaPart;
            } else if (portionDesc.equalsIgnoreCase(CENTRAL)
                    || areaPart.equalsIgnoreCase(EXTREME)) {
                portionDesc = areaPart + " " + portionDesc;
            } else {
                portionDesc += areaPart;
            }
        }
        if (portionDesc.length() > 0
                && portionDesc.indexOf(toMixedCase(CENTRAL)) < 0) {
            portionDesc += "ERN";
        }
        return portionDesc;
    }

    private String toMixedCase(String str) {
        if (str.length() == 0) {
            return str;
        }

        else if (str.length() == 1) {
            return str.toUpperCase();
        }

        else {
            return str.substring(0, 1).toUpperCase()
                    + str.substring(1).toLowerCase();
        }
    }

    void setCountyAreaRetriever(CountyAreaRetriever countyAreaRetriever) {
        this.countyAreaRetriever = countyAreaRetriever;
    }

    void setFipMapBuilder(FIPMapBuilder fipMapBuilder) {
        this.fipMapBuilder = fipMapBuilder;
    }

    void setGridSpacingRetriever(GridSpacingRetriever gridSpacingRetriever) {
        this.gridSpacingRetriever = gridSpacingRetriever;
    }

    void setDirectionsRetriever(IDirectionsRetriever directionsRetriever) {
        this.directionsRetriever = directionsRetriever;
    }
}
