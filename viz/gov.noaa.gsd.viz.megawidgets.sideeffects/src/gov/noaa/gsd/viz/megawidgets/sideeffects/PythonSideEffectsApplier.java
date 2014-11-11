/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets.sideeffects;

import gov.noaa.gsd.viz.megawidgets.ISideEffectsApplier;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import jep.Jep;
import jep.JepException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.util.FileUtil;

/**
 * Description: Side effects applier for megawidget managers that uses Python
 * scripts to govern the application of side effects (also known as
 * interdependencies). The class as a whole must be initialized before any
 * instances are created, and shut down when all instances have been disposed of
 * and no more are to be created (for example, just prior to the closing of the
 * application).
 * <p>
 * When created, instances of this class take the path of a Python script to be
 * executed. This script must contain the definition of a method called
 * {@link #applySideEffects(Collection, Map, boolean)} that takes two
 * parameters, the first being a list of strings holding the identifiers of the
 * megawidgets whose invocations are causing side effects to be applied (or
 * <code>None
 * </code> if the megawidgets are being initialized), and the second being a
 * dictionary mapping megawidget identifiers to sub-dictionaries holding their
 * mutable properties, with each sub-dictionary mapping the property names to
 * their current values. This method must return either <code>None</code>, or
 * else a dictionary identical in structure to the one passed in as a parameter,
 * but with mappings only for those megawidgets whose mutable properties have
 * been changed as a side effect, and with the sub-dictionaries likewise holding
 * only mappings for the specific properties that have been changed.
 * <p>
 * The file holding the script must not disappear or be modified for the life of
 * the side effects applier instance that is using it. No guarantees are made as
 * to when it will be executed, or how many times; thus, scripts should be
 * written with the assumption that only the <code>applySideEffects()</code>
 * method will be guaranteed to be run each time side effects are to be applied.
 * <p>
 * <strong>Note</code>: The {@link #initialize()} and
 * {@link #prepareForShutDown()} static methods are thread-safe; multiple
 * threads may call these class-scoped methods without danger. Additionally,
 * calls to <code>applySideEffects()</code> are also thread safe, as they are
 * synchronized at the class (not merely object) level.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 06, 2013    1277    Chris.Golden      Initial creation
 * Jul 15, 2013     585    Chris.Golden      Changed to ensure that if initialize
 *                                           and shutdown methods are called out
 *                                           of order (e.g. first two calls to
 *                                           initialize, then two to shutdown),
 *                                           the class will always be initialized
 *                                           unless the number of calls to the
 *                                           latter equal or exceed the number of
 *                                           the former. Also added synchronization
 *                                           for thread safety.
 * Feb 14, 2014    2161    Chris.Golden      Updated Javadoc comments, and changed
 *                                           to work with change to interface (i.e.
 *                                           multiple trigger identifiers being
 *                                           supplied as a string).
 * Jun 17, 2014    3982    Chris.Golden      Changed to use "interdependencies"
 *                                           instead of "side effects" in user-
 *                                           facing situations, and changed to use
 *                                           better inline comments.
 * Jun 24, 2014    4009    Chris.Golden      Changed to allow Python include path
 *                                           to be specified at initialization
 *                                           time.
 * Aug 15, 2014    4243    Chris.Golden      Changed to always expect a file as the
 *                                           script to be run, and to accept scripts
 *                                           in which the entry point function
 *                                           references other functions in the same
 *                                           or another module.
 * Oct 10, 2014    4042    Chris.Golden      Fixed bug that caused any trigger
 *                                           identifiers to not be deserialized
 *                                           properly on the Python side in some
 *                                           cases.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class PythonSideEffectsApplier implements ISideEffectsApplier {

    // Private Static Constants

    /**
     * Python script used for initializing the Jep instance.
     */
    private static final String INITIALIZE = "import json, JavaImporter";

    /**
     * Name of the Python function that calls the instance's script's
     * apply-interdependencies entry point function, and returns the result as a
     * JSON string.
     */
    private static final String NAME_APPLY_INTERDEPENDENCIES_WRAPPER = "_applyInterdependenciesWrapper_";

    /**
     * First part of the Python script used to define the function that calls
     * the instance's script's <code>applyInterdependencies()</code> entry point
     * function, and returns the result as a JSON string.
     */
    private static final String DEFINE_APPLY_INTERDEPENDENCIES_WRAPPER_FUNCTION = "def "
            + NAME_APPLY_INTERDEPENDENCIES_WRAPPER
            + "(triggerIdentifiers, mutableProperties, mutablePropertiesChanged):\n"
            + "   global _megawidgetMutableProperties_\n"
            + "   if triggerIdentifiers is not None:\n"
            + "      triggerIdentifiers = json.loads(triggerIdentifiers)\n"
            + "   if mutablePropertiesChanged:\n"
            + "      _megawidgetMutableProperties_ = json.loads(mutableProperties)\n"
            + "   result = applyInterdependencies(triggerIdentifiers, _megawidgetMutableProperties_)\n"
            + "   if result is not None:\n"
            + "      for identifier in result:\n"
            + "         for name in result[identifier]:\n"
            + "            _megawidgetMutableProperties_[identifier][name] = result[identifier][name]\n"
            + "      return json.dumps(result)\n" + "   return None\n\n";

    /**
     * Name of the Python function used to clean up between Jep context
     * switches.
     */
    private static final String NAME_CLEANUP_FOR_CONTEXT_SWITCH = "_cleanupForContextSwitch_";

    /**
     * Python script used for defining the function used to clean up between Jep
     * context switches.
     */
    private static final String DEFINE_CLEANUP_FOR_CONTEXT_SWITCH_FUNCTION = "def "
            + NAME_CLEANUP_FOR_CONTEXT_SWITCH
            + "():\n"
            + "   g = globals()\n"
            + "   for i in g:\n"
            + "      if not i.startswith('__') "
            + "and not i == '"
            + NAME_CLEANUP_FOR_CONTEXT_SWITCH
            + "' "
            + "and not i == 'jep' and not i == '"
            + NAME_APPLY_INTERDEPENDENCIES_WRAPPER
            + "' and not i == 'json':\n"
            + "         g[i] = None\n\n";

    /**
     * Python script used for cleaning up between Jep context switches.
     */
    private static final String CLEANUP_FOR_CONTEXT_SWITCH = NAME_CLEANUP_FOR_CONTEXT_SWITCH
            + "(); " + NAME_CLEANUP_FOR_CONTEXT_SWITCH + " = None\n";

    /**
     * Name of the Python function used to clean up before shutdown.
     */
    private static final String NAME_CLEANUP_FOR_SHUTDOWN = "_cleanupForShutdown_";

    /**
     * Python script used for defining the function used to clean up before
     * shutdown.
     */
    private static final String DEFINE_CLEANUP_FOR_SHUTDOWN_FUNCTION = "def "
            + NAME_CLEANUP_FOR_SHUTDOWN + "():\n" + "   g = globals()\n"
            + "   for i in g:\n"
            + "      if not i.startswith('__') and not i == '"
            + NAME_CLEANUP_FOR_SHUTDOWN + "':\n" + "         g[i] = None\n\n";

    /**
     * Python script used for cleaning up before shutdown.
     */
    private static final String CLEANUP_FOR_SHUTDOWN = NAME_CLEANUP_FOR_SHUTDOWN
            + "(); "
            + NAME_CLEANUP_FOR_SHUTDOWN
            + " = None; "
            + "import gc; "
            + "_uncollected_ = gc.collect(2); "
            + "_uncollected_ = None; "
            + "gc = None\n";

    /**
     * Regular expression pattern used to find the apply-interdependencies entry
     * point function definition in Python scripts.
     */
    private static final Pattern APPLY_INTERDEPENDENCIES_ENTRY_POINT_DEFINITION = Pattern
            .compile("(.*\n|)def +applyInterdependencies *\\(.+",
                    Pattern.DOTALL);

    // Private Static Variables

    /**
     * Logging mechanism.
     */
    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(PythonSideEffectsApplier.class);

    /**
     * Counter indicating the total number of initializations requested minus
     * the total number of shutdowns requested.
     */
    private static int requestCounter = 0;

    /**
     * Single Jep instance, shared between the instances of this class. It is
     * created by {@link #initialize()} and disposed of via
     * {@link #prepareForShutDown()}.
     */
    private static Jep jep;

    /**
     * Gson converter. As with {@link #jep}, it is created by
     * {@link #initialize()} and disposed of via {@link #prepareForShutDown()}.
     */
    private static Gson gson;

    /**
     * Last instance to have had its
     * {@link #applySideEffects(Collection, Map, boolean)} method invoked, or
     * <code>null</code> if no instance has had this method called, or if the
     * last such instance has been disposed of.
     */
    private static WeakReference<PythonSideEffectsApplier> lastApplier = null;

    /**
     * Flag indicating whether side effects are currently being applied.
     */
    private static boolean sideEffectsBeingApplied = false;

    // Private Variables

    /**
     * File holding the script run by the side effects applier prior to
     * application of side effects.
     */
    private final File scriptFile;

    // Public Static Methods

    /**
     * Initialize the single instance of this class with no extra Python include
     * path.
     */
    public static void initialize() {
        initialize(null, PythonSideEffectsApplier.class.getClassLoader());
    }

    /**
     * Initialize the single instance of this class.
     * 
     * @param includePath
     *            Python include path to be used. If not <code>null</code>, it
     *            is used when running Python code so that the latter can import
     *            non-standard modules.
     * @param classLoader
     *            Class loader to be used. This must be a class loader from a
     *            project that has access to any Java classes that will be used
     *            by the Python scripts that are run.
     */
    public static void initialize(String includePath, ClassLoader classLoader) {
        synchronized (PythonSideEffectsApplier.class) {
            if ((++requestCounter == 1) && (jep == null)) {
                try {

                    jep = new Jep(false, includePath, classLoader);
                    jep.eval(INITIALIZE);
                    jep.eval(DEFINE_APPLY_INTERDEPENDENCIES_WRAPPER_FUNCTION);
                } catch (JepException e) {
                    statusHandler.error(
                            "Internal error while initializing Python "
                                    + "side effects applier.", e);
                }
                gson = new Gson();
            }
        }
    }

    /**
     * Dispose of the single instance of this class.
     */
    public static void prepareForShutDown() {
        synchronized (PythonSideEffectsApplier.class) {
            if ((--requestCounter < 1) && (jep != null)) {
                lastApplier = null;
                gson = null;
                try {
                    jep.eval(DEFINE_CLEANUP_FOR_SHUTDOWN_FUNCTION);
                    jep.eval(CLEANUP_FOR_SHUTDOWN);
                    jep.close();
                    jep = null;
                } catch (JepException e) {
                    statusHandler.error(
                            "Internal error while preparing for shutdown "
                                    + "of Python side effects applier.", e);
                }
            }
        }
    }

    /**
     * Determine whether the script within the specified file appears to contain
     * a definition of an Python interdependency script entry point function.
     * This method does not parse the script for any sort of correctness; it
     * simply determines whether such a function appears to be defined, not
     * whether the script would compile.
     * 
     * @param file
     *            File in which to look for the entry point function.
     * @return True
     */
    public static boolean containsSideEffectsEntryPointFunction(File file) {
        try {
            String script = FileUtil.file2String(file);
            return APPLY_INTERDEPENDENCIES_ENTRY_POINT_DEFINITION.matcher(
                    script).matches();
        } catch (IOException e) {
            statusHandler.error("Could not read in " + file + " to check for "
                    + "apply-interdependencies entry point function.", e);
        }
        return false;
    }

    // Public Constructors

    /**
     * Construct a standard instance with a Python script.
     * 
     * @param scriptFile
     *            File holding the Python script that defines the
     *            <code>applyInterdependencies()</code> entry point function
     *            used by this instance to apply side effects.
     * @throws IllegalStateException
     *             If {@link #initialize()} has not been invoked already, or if
     *             {@link #prepareForShutDown()} has been invoked since the last
     *             invocation of <code>initialize()</code>.
     */
    public PythonSideEffectsApplier(File scriptFile) {
        ensureClassInitialized();
        this.scriptFile = scriptFile;
    }

    // Public Methods

    /**
     * @throws IllegalStateException
     *             If the class has not been initialized, or if this method is
     *             called recursively.
     */
    @Override
    public Map<String, Map<String, Object>> applySideEffects(
            Collection<String> triggerIdentifiers,
            Map<String, Map<String, Object>> mutableProperties,
            boolean propertiesMayHaveChanged) {
        synchronized (PythonSideEffectsApplier.class) {

            /*
             * Ensure that the the Jep instance has been initialized.
             */
            ensureClassInitialized();

            /*
             * Ensure that side effects are not already in the process of being
             * applied, and set the application-occurring flag.
             */
            if (sideEffectsBeingApplied) {
                throw new IllegalStateException(
                        "Illegal reentry to applySideEffects().");
            }
            sideEffectsBeingApplied = true;

            /*
             * If this method has not been run by any instance of this class
             * since initialization, or if the last instance that ran it is not
             * the same as this instance, perform a Jep context switch.
             */
            if ((lastApplier == null) || (lastApplier.get() != this)) {

                /*
                 * Remember that this instance is the last to have had this
                 * method invoked.
                 */
                lastApplier = new WeakReference<PythonSideEffectsApplier>(this);

                /*
                 * Clean up to prepare for the context switch.
                 */
                try {
                    jep.eval(DEFINE_CLEANUP_FOR_CONTEXT_SWITCH_FUNCTION);
                    jep.eval(CLEANUP_FOR_CONTEXT_SWITCH);
                } catch (JepException e) {
                    statusHandler.error("Internal error while cleaning up "
                            + "before context switch of Python side effects "
                            + "applier.", e);
                    sideEffectsBeingApplied = false;
                    return null;
                }

                /*
                 * Switch context by running the script for this instance. The
                 * script has to define the Python applyInterdependencies()
                 * method.
                 */
                try {
                    jep.runScript(scriptFile.getPath());
                } catch (JepException e) {
                    statusHandler
                            .error("Error while loading Python interdependency script.",
                                    e);
                    sideEffectsBeingApplied = false;
                    return null;
                }

                /*
                 * Set the flag indicating that properties have changed since
                 * the last invocation, since owing to the context switch the
                 * mutable properties will certainly need to be reloaded into
                 * the Jep instance.
                 */
                propertiesMayHaveChanged = true;
            }

            /*
             * Invoke the side effects wrapper and get the result.
             */
            Map<String, Map<String, Object>> resultMap = null;
            try {
                Object result = jep.invoke(
                        NAME_APPLY_INTERDEPENDENCIES_WRAPPER,
                        (triggerIdentifiers == null ? null : gson
                                .toJson(triggerIdentifiers)), gson
                                .toJson(mutableProperties),
                        propertiesMayHaveChanged);
                if (result != null) {
                    Type type = new TypeToken<HashMap<String, HashMap<String, Object>>>() {
                    }.getType();
                    resultMap = gson.fromJson((String) result, type);
                }
            } catch (JepException e) {
                statusHandler
                        .error("Python script error occurred;"
                                + "Python method applyInterdependencies() should either "
                                + "return None or else a dictionary mapping "
                                + "megawidget identifiers to dictionaries holding "
                                + "name-value pairs for changed mutable properties.",
                                e);
            }

            /*
             * Reset the application-occurring flag and return the result, which
             * is either null if the side effects did not affect the megawidgets
             * or a map of megawidget identifiers to maps of mutable properties
             * that have changed.
             */
            sideEffectsBeingApplied = false;
            return resultMap;
        }
    }

    // Private Methods

    /**
     * Ensure the class has been initialized.
     * 
     * @throws IllegalStateException
     *             If the class has not been initialized.
     */
    private void ensureClassInitialized() {
        if (jep == null) {
            throw new IllegalStateException("PythonSideEffectsApplier class "
                    + "not initialized.");
        }
    }
}
