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

import jep.Jep;
import jep.JepException;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Description: Side effects applier for megawidget managers that uses Python
 * scripts to govern the application of side effects. The class as a whole must
 * be initialized before any instances are created, and shut down when all
 * instances have been disposed of and no more are to be created (for example,
 * just prior to the closing of the application).
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
    private static final String INITIALIZE = "import json; import inspect";

    /**
     * Name of the Python method that calls the instance's script's
     * <code>applyInterdependencies()</code> Python method, and returns the
     * result as a JSON string.
     */
    private static final String NAME_APPLY_SIDE_EFFECTS_WRAPPER = "_applyInterdependenciesWrapper";

    /**
     * Python script used for defining the method that calls the instance's
     * script's <code>applyInterdependencies()</code> Python method, and returns
     * the result as a JSON string.
     */
    private static final String DEFINE_APPLY_SIDE_EFFECTS_WRAPPER_METHOD = "def "
            + NAME_APPLY_SIDE_EFFECTS_WRAPPER
            + "(triggerIdentifier, mutableProperties, mutablePropertiesChanged):\n"
            + "   global _megawidgetMutableProperties\n"
            + "   if mutablePropertiesChanged:\n"
            + "      _megawidgetMutableProperties = json.loads(mutableProperties)\n"
            + "   result = applyInterdependencies(triggerIdentifier, _megawidgetMutableProperties)\n"
            + "   if result is not None:\n"
            + "      for identifier in result:\n"
            + "         for name in result[identifier]:\n"
            + "            _megawidgetMutableProperties[identifier][name] = result[identifier][name]\n"
            + "      return json.dumps(result)\n" + "   return None\n\n";

    /**
     * Name of the Python method for determining whether the <code>
     * applyInterdependencies()</code> Python method is defined and takes two
     * parameters.
     */
    private static final String NAME_IS_SIDE_EFFECTS_METHOD_DEFINED = "_isInterdependenciesMethodDefined";

    /**
     * Python script used for defining the method used to check to see if the
     * side effects application method has been defined by an instance's script.
     */
    private static final String DEFINE_CHECK_FOR_SIDE_EFFECTS_METHOD = "def "
            + NAME_IS_SIDE_EFFECTS_METHOD_DEFINED + "():\n" + "   try:\n"
            + "      argSpec = inspect.getargspec(applyInterdependencies)\n"
            + "   except:\n" + "      return False\n"
            + "   return len(argSpec.args) == 2\n\n";

    /**
     * Name of the Python method used to clean up between Jep context switches.
     */
    private static final String NAME_CLEANUP_FOR_CONTEXT_SWITCH = "cleanupForContextSwitch";

    /**
     * Python script used for defining the method used to clean up between Jep
     * context switches.
     */
    private static final String DEFINE_CLEANUP_FOR_CONTEXT_SWITCH_METHOD = "def "
            + NAME_CLEANUP_FOR_CONTEXT_SWITCH
            + "():\n"
            + "   g = globals()\n"
            + "   for i in g:\n"
            + "      if not i.startswith('__') "
            + "and not i == '"
            + NAME_CLEANUP_FOR_CONTEXT_SWITCH
            + "' "
            + "and not i == 'jep' and not i == '"
            + NAME_IS_SIDE_EFFECTS_METHOD_DEFINED
            + "' and not i == '"
            + NAME_APPLY_SIDE_EFFECTS_WRAPPER
            + "' and not i == 'json' and not i == 'inspect':\n"
            + "         g[i] = None\n\n";

    /**
     * Python script used for cleaning up between Jep context switches.
     */
    private static final String CLEANUP_FOR_CONTEXT_SWITCH = NAME_CLEANUP_FOR_CONTEXT_SWITCH
            + "(); " + NAME_CLEANUP_FOR_CONTEXT_SWITCH + " = None\n";

    /**
     * Name of the Python method used to clean up before shutdown.
     */
    private static final String NAME_CLEANUP_FOR_SHUTDOWN = "cleanupForShutdown";

    /**
     * Python script used for defining the method used to clean up before
     * shutdown.
     */
    private static final String DEFINE_CLEANUP_FOR_SHUTDOWN_METHOD = "def "
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
            + "uncollected = gc.collect(2); "
            + "uncollected = None; "
            + "gc = None\n";

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
     * Path for the script that is run by the side effects applier prior to
     * application of side effects. Either this or {@link #script} will be
     * <code>null</code>; whichever is not <code>null</code> is used.
     */
    private final String scriptPath;

    /**
     * Script that is run by the side effects applier prior to application of
     * side effects. Either this or {@link #scriptPath} will be <code>
     * null</code>; whichever is not <code>null</code> is used.
     */
    private final String script;

    // Public Static Methods

    /**
     * Initialize the single instance of this class.
     */
    public static void initialize() {
        synchronized (PythonSideEffectsApplier.class) {
            if ((++requestCounter == 1) && (jep == null)) {
                try {
                    jep = new Jep(false);
                    jep.eval(INITIALIZE);
                    jep.eval(DEFINE_CHECK_FOR_SIDE_EFFECTS_METHOD);
                    jep.eval(DEFINE_APPLY_SIDE_EFFECTS_WRAPPER_METHOD);
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
                    jep.eval(DEFINE_CLEANUP_FOR_SHUTDOWN_METHOD);
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

    // Public Constructors

    /**
     * Construct a standard instance with a path to a Python script.
     * 
     * @param scriptPath
     *            Path to Python script that defines the <code>
     *            applyInterdependencies()</code> method used by this instance
     *            to apply side effects.
     * @throws IllegalStateException
     *             If {@link #initialize()} has not been invoked already, or if
     *             {@link #prepareForShutDown()} has been invoked since the last
     *             invocation of <code>initialize()</code>.
     * @throws NullPointerException
     *             If <code>scriptPath</code> is <code>null</code>.
     * @throws IOException
     *             If the <code>scriptPath</code> does not resolve to a
     *             canonical path.
     */
    public PythonSideEffectsApplier(File scriptPath) throws IOException {
        ensureClassInitialized();
        this.scriptPath = scriptPath.getCanonicalPath();
        this.script = null;
    }

    /**
     * Construct a standard instance with a Python script.
     * 
     * @param script
     *            Python script that defines the <code>applyInterdependencies()
     *            </code> method used by this instance to apply side effects.
     * @throws IllegalStateException
     *             If {@link #initialize()} has not been invoked already, or if
     *             {@link #prepareForShutDown()} has been invoked since the last
     *             invocation of <code>initialize()</code>.
     */
    public PythonSideEffectsApplier(String script) {
        ensureClassInitialized();
        this.scriptPath = null;
        this.script = script;
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
                    jep.eval(DEFINE_CLEANUP_FOR_CONTEXT_SWITCH_METHOD);
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
                 * method. The script is either evaluated directly, if it was
                 * supplied as a string, or run from a file, if a path was
                 * supplied.
                 */
                try {
                    if (script != null) {
                        if (jep.eval(script) == false) {
                            throw new JepException(
                                    "script incomplete and/or not executed");
                        }
                    } else {
                        jep.runScript(scriptPath);
                    }
                } catch (JepException e) {
                    statusHandler.error("Internal error while performing "
                            + "context switch of Python side effects applier.",
                            e);
                    sideEffectsBeingApplied = false;
                    return null;
                }

                /*
                 * Ensure that the Python method applyInterdependencies() has
                 * been defined by the script that was run above.
                 */
                Object result = null;
                try {
                    result = jep.invoke(NAME_IS_SIDE_EFFECTS_METHOD_DEFINED);
                } catch (JepException e) {
                    statusHandler.error("Internal error while checking for "
                            + "presence of Python script for application "
                            + "of side effects within Python side "
                            + "effects applier.", e);
                    sideEffectsBeingApplied = false;
                    return null;
                }
                if ((result == null) || !(result instanceof Boolean)) {
                    statusHandler
                            .error("Internal error while checking for "
                                    + "presence of Python script for application of "
                                    + "side effects within Python side effects applier.",
                                    new IllegalStateException(
                                            "Could not execute Python "
                                                    + "method "
                                                    + NAME_IS_SIDE_EFFECTS_METHOD_DEFINED
                                                    + "()"));
                    sideEffectsBeingApplied = false;
                    return null;
                } else if ((Boolean) result == false) {
                    statusHandler.error("Could not find Python method "
                            + NAME_IS_SIDE_EFFECTS_METHOD_DEFINED
                            + "() defined "
                            + "within Python side effects applier.");
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
                        NAME_APPLY_SIDE_EFFECTS_WRAPPER,
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
