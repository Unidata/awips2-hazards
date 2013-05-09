package gov.noaa.gsd.viz.hazards.mixedhazardstory
import static org.junit.Assert.*;
import gov.noaa.gsd.viz.hazards.TestingUtils
import gov.noaa.gsd.viz.hazards.utilities.Utilities;



import spock.lang.*

/**
 *
 * Description: Tests the getThisHost method of the LocalizationInterface.py
 *              module. This method has returned an empty host name string
 *              on some machines. We are trying to track down this problem,
 *              but this test should alert the developer if this issue is
 *              encountered.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 12, 2013            blawrenc    Initial creation
 *
 * </pre>
 *
 * @author bryon.lawrence
 * @version 1.0
 */
class LocalizationHostnameTest extends spock.lang.Specification {
    jep.Jep jepInstance
    String  localhostname

    /**
     * Sets up this test.
     * @param
     * @return
     */
    def setup() {
        new TestingUtils()
        jepInstance = getJepInstance()
        localhostname = java.net.InetAddress.getLocalHost().getHostName();
    }

    /**
     * Cleans up any resources used by this test.
     * @param
     * @return
     */
    def cleanup() {
        jepInstance.close()
    }

    /**
     * Test whether or not the LocalizationInterface getThisHost method
     * returns an non empty hostname string representing the correct
     * hostname for the machine this test is running on.
     * @param
     * @return
     */
    def "LocalizationInterface host name retrieval test"() {
        when: "The hostname is received from localization"
        jepInstance.eval("import JavaImporter");
        jepInstance.eval("from LocalizationInterface import *")
        jepInstance.eval("localizationInterface = LocalizationInterface()")
        jepInstance.eval("host = localizationInterface.getThisHost()")
        String hostNameResult = (String)jepInstance.getValue("host")
        then: "The host name is not null"
        hostNameResult != null
        and: "The host name is not empty"
        hostNameResult.length() > 0
        and: "The host name is equal to my machine"
        hostNameResult.contains(localhostname)
    }

    /**
     * Builds a jep instance for this test.
     * @param
     * @return
     */
    private static jep.Jep getJepInstance() {

        jep.Jep jepInstance = null

        File currentDir = new File(System.getProperty("user.dir")).getParentFile();
        String basePath = currentDir.getPath();
        List<String> sourcePaths = Utilities.buildPythonSourcePaths(basePath);
        List<String> pythonUtilityPaths = Utilities.buildPythonPath();
        sourcePaths.addAll(pythonUtilityPaths);

        try {
            ClassLoader loader = ClassLoader.systemClassLoader
            String jepIncludePath = Utilities.buildJepIncludePath(sourcePaths);
            jepInstance = new jep.Jep(false, jepIncludePath, loader);
        }
        catch (jep.JepException e) {
            throw new RuntimeException("Unable to start JEP", e);
        }

        return jepInstance
    }
}
