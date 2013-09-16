/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards

import java.io.File;
import java.util.List;

import gov.noaa.gsd.common.hazards.utilities.Utils;
import gov.noaa.gsd.viz.hazards.display.IHazardServicesModel
import gov.noaa.gsd.viz.hazards.display.ModelDecorator;
import gov.noaa.gsd.viz.hazards.utilities.FileUtilities
import gov.noaa.gsd.viz.hazards.utilities.Utilities;

import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.IHazardEventManager
import com.raytheon.uf.common.dataplugin.events.hazards.datastorage.InMemoryHazardEventManager
import com.raytheon.uf.common.localization.PathManagerFactoryTest;



/**
 * Description: Utilities used for various tests.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 13, 2013            daniel.s.schaffer      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer
 * @version 1.0
 */
class TestingUtils {

    public TestingUtils() {
        PathManagerFactoryTest.initLocalization();
        FileUtilities.fillFiles();
    }

    /**
     * TODO For some reason if I try to use the {@link ConsoleViewPart} 
     * constants for these, I get a compilation error regarding
     * an object indirectly referenced from a class file.
     */
    static String EVENTS = "Events"
    static String SETTINGS = "Views"

    static CONSOLE_COMPONENT = "Temporal"
    static SPATIAL_COMPONENT = "Spatial"
    static HID_COMPONENT = "HID"
    static ALL_EVENTS = "all"
    static FLOOD_SETTING = "Flood"
    static EVENT_ID = "eventID"
    static CAUSE = "cause"
    static TYPE = "type"
    static SUBTYPE = "subType"
    static CURRENT_SETTING_ID = "currentSettingsID"
    static SETTING_ID = "settingsID"
    static TRUE = "True"
    static ISSUED = "issued"
    static DAM_NAME = "damName"
    static STATE = "state"
    static PENDING = "pending"
    static SELECTED = "selected"

    ModelDecorator buildModel() {
        IHazardServicesModel result;
        File currentDir = new File(System.getProperty("user.dir")).getParentFile();
        String basePath = currentDir.getPath();
        List<String> sourcePaths = Utilities.buildPythonSourcePaths(basePath);
        List<String> pythonUtilityPaths = Utilities.buildPythonPath();
        sourcePaths.addAll(pythonUtilityPaths);

        result = TestingUtils.getSessionManager(sourcePaths);


        IHazardEventManager hazardEventManager = new InMemoryHazardEventManager()
        String eventsAsJson = Utilities.getCannedEventsAsJSON()
        HazardEventsBuilder hazardEventBuilder = new HazardEventsBuilder(eventsAsJson)

        hazardEventManager.storeEvents(hazardEventBuilder.getEvents())
        result.setHazardEventManager(hazardEventManager)


        result.initialize("1297137637240", "1297137637240", "Flood", "{}", "Operational", "OAX","{}")
        result.reset(EVENTS)
        result.reset(SETTINGS)
        result = new ModelDecorator((IHazardServicesModel) result);
        return result
    }

    private static  IHazardServicesModel getSessionManager(List<String> sourcePaths) {

        try {
            ClassLoader loader = ClassLoader.systemClassLoader
            String jepIncludePath = Utilities.buildJepIncludePath(sourcePaths);
            jep.Jep jep = new jep.Jep(false, jepIncludePath, loader);
            for (String sourcePath : sourcePaths) {
                if (sourcePath.contains("session")) {
                    jep.eval("import JavaImporter");
                    jep.runScript(sourcePath + "/sessionManager/SessionManager.py");

                    IHazardServicesModel result = (IHazardServicesModel) jep.invoke("getProxy");
                    return result;
                }
            }
        }
        catch (jep.JepException e) {
            throw new RuntimeException("Unable to start JEP", e);
        }
    }

    String testFileAsString(String fileName) {
        String filePath = getClass().getResource(fileName).getPath()
        return Utils.textFileAsString(filePath)
    }

    static String benchmarkResults(String className, ModelDecorator model) {
        return String.format("%s\n%s\n", className,
        model.getBenchmarkingStats())
    }
}
