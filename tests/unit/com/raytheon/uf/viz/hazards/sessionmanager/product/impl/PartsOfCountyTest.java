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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.Serializable;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants;
import com.raytheon.uf.common.dataplugin.events.hazards.event.BaseHazardEvent;
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent;
import com.raytheon.uf.common.dataplugin.warning.config.GridSpacing;
import com.raytheon.uf.common.dataplugin.warning.gis.GeospatialData;
import com.raytheon.uf.common.dataplugin.warning.portions.GisUtil.Direction;
import com.raytheon.uf.common.dataplugin.warning.portions.PortionsUtil;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;

/**
 * Description: Test of {@link SessionProductManager}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 22, 2014            daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class PartsOfCountyTest {

    private static final String OAX = "OAX";

    private static final String NEBRASKA_ABBR = "NE";

    private static final String IOWA_ABBR = "IA";

    private static final String NEC095 = "NEC095";

    private static final String NEC067 = "NEC067";

    private final GeometryFactory gf = new GeometryFactory();

    @SuppressWarnings("unchecked")
    @Test
    public void basic() {

        CountyAreaRetriever countyAreaRetriever = mockCountyAreaRetriever();
        GridSpacingRetriever gridSpacingRetriever = mockGridSpacingRetriever();

        PartsOfGeographicalAreas partsOfCounty = new PartsOfGeographicalAreas();

        partsOfCounty.setGridSpacingRetriever(gridSpacingRetriever);
        partsOfCounty.setCountyAreaRetriever(countyAreaRetriever);

        Geometry hazardGeometry = buildDummyHazardGeometry();
        partsOfCounty.setFipMapBuilder(mockFipMapBuilder());

        IDirectionsRetriever directionsRetriever = mockDirectionsRetriever();
        partsOfCounty.setDirectionsRetriever(directionsRetriever);

        IHazardEvent event = buildDummyEvent();
        String site = OAX;
        partsOfCounty
                .addPortionsDescriptionToEvent(hazardGeometry, event, site);
        Map<String, String> partOfCounty = (Map<String, String>) event
                .getHazardAttribute(HazardConstants.UGC_PARTS_OF_COUNTY);
        assertTrue(partOfCounty.get(NEC067).equals("West Central"));
        assertTrue(partOfCounty.get(NEC095).equals("East Central"));
        Map<String, String> partOfState = (Map<String, String>) event
                .getHazardAttribute(HazardConstants.UGC_PARTS_OF_STATE);
        assertTrue(partOfState.get(NEC067).equals("Southeast"));
        assertTrue(partOfState.get(NEC095).equals("Southeast"));
    }

    private IDirectionsRetriever mockDirectionsRetriever() {
        IDirectionsRetriever result = new IDirectionsRetriever() {

            @Override
            public EnumSet<Direction> retrieveDirections(
                    Geometry polygonHazardGeometry, PortionsUtil portionsUtil,
                    String ugc, Geometry countyGeometry) throws Exception {
                if (ugc.equals(NEC067)) {
                    return EnumSet.of(Direction.CENTRAL, Direction.WEST);
                } else {
                    return EnumSet.of(Direction.CENTRAL, Direction.EAST);
                }
            }

        };
        return result;
    }

    private GridSpacingRetriever mockGridSpacingRetriever() {
        GridSpacingRetriever result = mock(GridSpacingRetriever.class);
        GridSpacing gridSpacing = new GridSpacing();
        gridSpacing.setNx(600);
        gridSpacing.setNy(600);
        gridSpacing.setKeepAspectRatio(true);
        when(result.gridSpacing(OAX)).thenReturn(gridSpacing);
        return result;
    }

    private CountyAreaRetriever mockCountyAreaRetriever() {
        CountyAreaRetriever result = mock(CountyAreaRetriever.class);

        GeospatialData county0 = mockCounty0();
        GeospatialData county1 = mockCounty1();
        GeospatialData[] countyAreas = new GeospatialData[] { county0, county1 };
        when(result.getCountyAreasForSite(OAX)).thenReturn(countyAreas);
        return result;
    }

    private GeospatialData mockCounty0() {
        GeospatialData result = mock(GeospatialData.class);
        Coordinate[] coordinates = new Coordinate[] {
                new Coordinate(-97.37, 40.0), new Coordinate(-96.91, 40.0),
                new Coordinate(-96.91, 40.35), new Coordinate(-97.37, 40.35),
                new Coordinate(-97.37, 40.0) };
        Geometry geometry0 = geometryFromCoordinates(coordinates);
        Map<String, Object> attrs = new HashMap<>();
        attrs.put(CountyAreaRetriever.FIPS, "31095");
        attrs.put(CountyAreaRetriever.FE_AREA, "se");
        when(result.getGeometry()).thenReturn(geometry0);
        when(result.getAttributes()).thenReturn(attrs);
        return result;
    }

    private GeospatialData mockCounty1() {
        GeospatialData result = mock(GeospatialData.class);
        Coordinate[] coordinates = new Coordinate[] {
                new Coordinate(-96.91, 40.0), new Coordinate(-96.46, 40.0),
                new Coordinate(-96.46, 40.52), new Coordinate(-96.91, 40.52),
                new Coordinate(-96.91, 40.0) };
        Geometry geometry0 = geometryFromCoordinates(coordinates);
        when(result.getGeometry()).thenReturn(geometry0);
        Map<String, Object> attrs = new HashMap<>();
        attrs.put(CountyAreaRetriever.FIPS, "31067");
        attrs.put(CountyAreaRetriever.FE_AREA, "se");
        when(result.getAttributes()).thenReturn(attrs);
        return result;
    }

    private FIPMapBuilder mockFipMapBuilder() {
        FIPMapBuilder result = mock(FIPMapBuilder.class);
        Map<String, String> fipMapping = new HashMap<>();
        fipMapping.put("19", IOWA_ABBR);
        fipMapping.put("31", NEBRASKA_ABBR);
        when(result.buildFIPStateIndexToAbbreviationMapping()).thenReturn(
                fipMapping);
        return result;
    }

    private IHazardEvent buildDummyEvent() {
        IHazardEvent result = new BaseHazardEvent();
        Serializable ugcs = Lists.newArrayList(NEC067, NEC095);
        result.addHazardAttribute(HazardConstants.UGCS, ugcs);
        return result;
    }

    private Geometry buildDummyHazardGeometry() {
        Coordinate[] coordinates = new Coordinate[] {
                new Coordinate(-97.25, 40.15), new Coordinate(-96.75, 40.15),
                new Coordinate(-96.75, 40.25), new Coordinate(-97.25, 40.25),
                new Coordinate(-97.25, 40.15) };
        Geometry result = geometryFromCoordinates(coordinates);
        return result;
    }

    private Geometry geometryFromCoordinates(Coordinate[] coordinates) {
        Geometry geometry = gf.createPolygon(coordinates);
        Geometry[] geometries = new Geometry[] { geometry };
        Geometry result = gf.createGeometryCollection(geometries);
        return result;
    }

}
