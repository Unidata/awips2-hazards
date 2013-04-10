/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.jsonutilities;

import gov.noaa.gsd.viz.hazards.spatialdisplay.drawableelements.HazardServicesPolygon;
import gov.noaa.gsd.viz.hazards.utilities.Utilities;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.Platform;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * JSON helper methods for creating JSON messages. Also, contains utilities for
 * writing JSON to files, which is useful for debugging.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Bryon.Lawrence      Initial induction into repo
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class JSONUtilities {

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(JSONUtilities.class);

    /**
     * Convenience method for creating a JSON string describing a modified
     * hazard event. A modified event is one for which the boundary coordinates
     * have changed.
     * 
     * @param modifiedPolygon
     *            The modified polygon
     * @param polygonsForEvent
     *            A list of polygons for the event.
     * @param newCoords
     *            The new coordinates of the boundary of the modified polygon.
     * 
     * @return JSON string representing a modified event.
     */
    static public String createModifiedHazardJSON(
            HazardServicesPolygon modifiedPolygon,
            List<HazardServicesPolygon> polygonsForEvent,
            List<Coordinate> newCoords) {
        String eventID = modifiedPolygon.getEventID();

        EventDict modifiedAreaObject = new EventDict();

        modifiedAreaObject.put(Utilities.HAZARD_EVENT_IDENTIFIER, eventID);
        modifiedAreaObject.put(Utilities.HAZARD_EVENT_SHAPE_TYPE,
                Utilities.HAZARD_EVENT_SHAPE_TYPE_POLYGON);

        for (HazardServicesPolygon polygon : polygonsForEvent) {

            Polygon newPolygon = null;

            Coordinate[] coords = null;

            if (polygon.equals(modifiedPolygon)) {
                coords = newCoords.toArray(new Coordinate[0]);

            } else {
                coords = polygon.getPoints().toArray(new Coordinate[0]);
            }

            newPolygon = new Polygon("", "true", "true", "true", "", 2, "", "",
                    coords);

            modifiedAreaObject.addShape(newPolygon);

        }

        return modifiedAreaObject.toJSONString();

    }

    /**
     * Convenience method for creating JSON for a new hazard area.
     * 
     * @param eventID
     *            The event id assigned to this new hazard
     * @param shapeType
     *            The type of the shape representing this hazard
     * @param points
     *            The coordinates defining the new hazard geometry
     * 
     * @return
     */
    static public String createNewHazardJSON(String eventID, String shapeType,
            List<Coordinate> points) {
        Coordinate[] coords = points.toArray(new Coordinate[0]);

        // Convert the object to JSON.
        Polygon polygon = new Polygon("", "true", "true", "true", "White", 2,
                "SOLID", "White", coords);

        EventDict dict = new EventDict();
        dict.put(Utilities.HAZARD_EVENT_IDENTIFIER, "");
        dict.addShape(polygon);

        return dict.toJSONString();
    }

    /**
     * Convenience method for creating the JSON for the drag drop dot.
     * 
     * @param
     * @return
     */
    static public String createDragDropPointJSON(double lat, double lon,
            long selectedTime) {
        String json = "{\"spatialInfo\":{\"points\":[[[" + lat + "," + lon
                + "]," + selectedTime + "]]}}";
        return json;

    }

    /**
     * Writes the contents of a JSON string to a file. Useful for debugging.
     * 
     * @param
     * @return
     */
    static public void writeJSONtoFile(String JSONString, String fileName) {
        // Place the file in the user's caveData directory.
        String caveDataDir = Platform.getUserLocation().getURL().getPath();

        try {
            FileWriter fw = new FileWriter(caveDataDir + File.separator
                    + fileName);
            fw.write(JSONString);
            fw.close();
        } catch (IOException e) {
            statusHandler.error("JSONUtilities.writeJSONtoFile(): Write "
                    + "failed.", e);
        }

    }

    /**
     * Creates the JSON string which instructs the Spatial View on how to draw a
     * drag drop dot.
     * 
     * @param
     * @return
     */
    public static String createDragDropDotJSON(String label) {
        List<EventDict> dragDropDictArray = Lists.newArrayList();
        EventDict dragDropDict = new EventDict();
        dragDropDictArray.add(dragDropDict);

        dragDropDict.put(Utilities.HAZARD_EVENT_IDENTIFIER, "DragDropDot");

        DragDropDot dot = new DragDropDot(label, "true", "true", "true",
                "255 000 000", 1, "SOLID", "255 000 255", 3, 0, new int[] {
                        -9999, -9999 });
        dragDropDict.addShape(dot);
        Gson gson = createGsonInterpreter();
        return gson.toJson(dragDropDictArray);
    }

    /**
     * 
     * @param
     * @return
     */
    static public Gson createGsonInterpreter() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(EventDict.class,
                new EventDictDeserializer());
        gsonBuilder.registerTypeAdapter(Dict.class, new DictDeserializer());
        gsonBuilder.registerTypeAdapter(DictList.class,
                new DictListDeserializer());
        gsonBuilder.registerTypeAdapter(ComparableLazilyParsedNumber.class,
                new LazilyParsedNumberSerializer());

        return gsonBuilder.create();
    }

    /**
     * 
     * @param
     * @return
     */
    static public Gson createPrettyGsonInterpreter() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(EventDict.class,
                new EventDictDeserializer());
        gsonBuilder.registerTypeAdapter(Dict.class, new DictDeserializer());
        gsonBuilder.registerTypeAdapter(DictList.class,
                new DictListDeserializer());
        gsonBuilder.registerTypeAdapter(ComparableLazilyParsedNumber.class,
                new LazilyParsedNumberSerializer());
        gsonBuilder.setPrettyPrinting();

        return gsonBuilder.create();
    }

    /**
     * 
     * @param
     * @return
     */
    static public String createListOfEventDictsJSON(
            List<List<EventDict>> listOfDicts) {
        Gson gson = JSONUtilities.createGsonInterpreter();
        String json = gson.toJson(listOfDicts);
        return json;
    }

    /**
     * 
     * @param
     * @return
     */
    static public Dict createDictFromJSON(String json) {
        Gson gson = JSONUtilities.createGsonInterpreter();
        return gson.fromJson(json, Dict.class);
    }

    /**
     * 
     * @param
     * @return
     */
    public static String jsonFromMap(Map<String, Object> map) {
        Gson gson = JSONUtilities.createGsonInterpreter();
        return gson.toJson(map);
    }

    /**
     * 
     * @param
     * @return
     */
    public static Map<String, Object> mapFromJson(String json) {
        Gson gson = JSONUtilities.createGsonInterpreter();
        Type hashMapType = new TypeToken<HashMap<String, Object>>() {
        }.getType();
        Map<String, Object> map = gson.fromJson(json, hashMapType);

        return map;
    }
}
