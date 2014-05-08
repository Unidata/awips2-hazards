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

import gov.noaa.gsd.common.utilities.Utils
import gov.noaa.gsd.viz.hazards.utilities.FileUtilities

import com.raytheon.uf.common.localization.PathManagerFactoryTest



/**
 * Description: Utilities used for various tests.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 13, 2013            daniel.s.schaffer      Initial creation
 * Nov 25, 2013    2336    Chris.Golden           Altered to handle new
 *                                                location of utility
 *                                                classes.
 * Dec 03, 2013    2182    daniel.s.schaffer@noaa.gov Refactoring - Removed unused code.
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
    static STATE = "status"
    static PENDING = "pending"
    static SELECTED = "selected"


    String testFileAsString(String fileName) {
        String filePath = getClass().getResource(fileName).getPath()
        return Utils.textFileAsString(filePath)
    }
}
