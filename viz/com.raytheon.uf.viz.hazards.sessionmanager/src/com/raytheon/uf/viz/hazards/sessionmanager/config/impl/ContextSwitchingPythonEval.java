/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.config.impl;

import java.io.File;
import java.util.List;

import jep.JepException;

import com.raytheon.uf.common.python.PythonEval;

/**
 * Description: Python evaluator capable of switching contexts between
 * invocations of Python code. Context-switching is done via a call to
 * {@link #prepareForContextSwitch()}. When invoked, this method clears the
 * Python interpreter of any state set by the scripts that have been run since
 * the object was constructed, or since the last invocation of this method,
 * whichever is more recent.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 14, 2014    4243    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class ContextSwitchingPythonEval extends PythonEval {

    // Private Static Constants

    /**
     * Name of the Python cleanup function definition.
     */
    private static final String CLEANUP_FUNCTION_NAME = "_cleanup_";

    /**
     * String to be used for invocation of the Python cleanup function.
     */
    private static final String CLEANUP_FUNCTION_INVOCATION = CLEANUP_FUNCTION_NAME
            + "()";

    /**
     * First part of the Python cleanup function definition used to delete
     * variables, functions, and the like when preparing for a context switch.
     * To this must be appended the contents of {@link #variablesToKeep}, and
     * then {@link #CLEANUP_FUNCTION_END}. The function returns a string holding
     * one or more names of Python variables to be deleted, each separated from
     * the next by a comma and a space. This string will always hold the name of
     * the cleanup function itself.
     */
    private static final String CLEANUP_FUNCTION_START = "def "
            + CLEANUP_FUNCTION_INVOCATION + ":\n" + "  toBeDeleted = []\n"
            + "  g = globals()\n" + "  for i in g:\n" + "    if not i == '"
            + CLEANUP_FUNCTION_NAME + "' and not i in ";

    /**
     * Second part of the Python cleanup function definition used to delete
     * variables, functions, and the like when preparing for a context switch.
     * To this must be prepended {@link #CLEANUP_FUNCTION_START}, followed by
     * the contents of {@link #variablesToKeep}.
     */
    private static final String CLEANUP_FUNCTION_END = ":\n"
            + "      g[i] = None\n" + "      toBeDeleted.append(i)\n"
            + "  toBeDeleted.append('" + CLEANUP_FUNCTION_NAME + "')\n"
            + "  return ', '.join(toBeDeleted)\n\n";

    /**
     * String to be used to set the cleanup function name to None in Python.
     */
    private static final String FORGET_CLEANUP_FUNCTION_INVOCATION = CLEANUP_FUNCTION_NAME
            + " = None";

    /**
     * Prefix to which to append the string returned by an evaluation of
     * {@link #CLEANUP_FUNCTION_INVOCATION} to delete variables.
     */
    private static final String DELETE_INVOCATON_PREFIX = "del ";

    // Private Variables

    /**
     * String holding a list of strings, in JSON form, with each string being a
     * Python variable within the Jep instance that is not to be deleted when
     * context-switching.
     */
    private String variablesToKeep;

    /**
     * Script executor currently using this interpreter, if any.
     */
    private ContextuallyAwareScriptExecutor<?> executor;

    // Public Constructors

    /**
     * Construct a standard instance with no pre-evaluations.
     * 
     * @param includePath
     *            Include path to be used by the Python interpreter when
     *            searching for imported modules.
     * @param classLoader
     *            Class loader to be used.
     * @throws JepException
     *             If an error occurs initializing the interpreter.
     */
    public ContextSwitchingPythonEval(String includePath,
            ClassLoader classLoader) throws JepException {
        super(includePath, classLoader);
        initialize();
    }

    /**
     * Construct a standard instance.
     * 
     * @param includePath
     *            Include path to be used by the Python interpreter when
     *            searching for imported modules.
     * @param classLoader
     *            Class loader to be used.
     * @param preEvals
     *            List of strings to be evaluated by the interpreter in the
     *            order given.
     * @throws JepException
     *             If an error occurs initializing the interpreter.
     */
    public ContextSwitchingPythonEval(String includePath,
            ClassLoader classLoader, List<String> preEvals) throws JepException {
        super(includePath, classLoader, preEvals);
        initialize();
    }

    // Public Methods

    /**
     * Set the script executor that is currently using this interpreter to that
     * specified, causing the invocation of {@link #prepareForContextSwitch()}
     * if the new script executor is not contextually equivalent to whichever
     * script executor was previously set via an invocation of this method (or
     * if there was no previous invocation). The decision as to whether or not
     * to two executors are contextually equivalent is made by invoking one of
     * the executor's
     * {@link ContextuallyAwareScriptExecutor#isContextuallyEqual(ContextuallyAwareScriptExecutor)}
     * and going by its result.
     * <p>
     * This method does not have to be called to handle context switches; if the
     * user of an instance of this class would rather decide when to prompt a
     * context switch, it can invoke <code>prepareForContextSwitch()</code>
     * directly and not work through executors.
     * </p>
     * 
     * @param executor
     *            New script executor.
     * @throws JepException
     *             If a problem occurs while attempting to prepare the
     *             interpreter for a context switch.
     */
    public void setCurrentScriptExecutor(
            ContextuallyAwareScriptExecutor<?> executor) throws JepException {
        if ((this.executor == null)
                || (this.executor.isContextuallyEqual(executor) == false)) {
            prepareForContextSwitch();
        }
        this.executor = executor;
    }

    /**
     * Prepare for a context switch by returning the interpreter to the state it
     * was in when first constructed. This means that any variables, functions,
     * etc. created or defined since then are deleted.
     * 
     * @throws JepException
     *             If a problem occurs while attempting to prepare the
     *             interpreter for a context switch.
     */
    public void prepareForContextSwitch() throws JepException {

        /*
         * Define and then invoke the cleanup function, remembering the string
         * of comma-and-space-delineated variable names that it returns.
         */
        jep.eval(CLEANUP_FUNCTION_START + variablesToKeep
                + CLEANUP_FUNCTION_END);
        String toBeDeleted = (String) jep.getValue(CLEANUP_FUNCTION_INVOCATION);

        /*
         * Remove the reference to the cleanup function, and then delete it and
         * the other variables that are to be removed as specified by the
         * cleanup function's invocation result from above.
         */
        jep.eval(FORGET_CLEANUP_FUNCTION_INVOCATION);
        jep.eval(DELETE_INVOCATON_PREFIX + toBeDeleted);
    }

    /**
     * Set the specified variable to the specified value. Note that additional
     * implementations of <code>set()</code> could be provided that would set
     * variables to different types of values (booleans, integers, etc.), but
     * since those are not being used right now, they are not included for the
     * sake of brevity.
     * 
     * @param variable
     *            Variable name.
     * @param value
     *            Value to which to set the variable.
     * @throws JepException
     *             If a problem occurs while attempting to set the variable.
     */
    public void set(String variable, Object value) throws JepException {
        jep.set(variable, value);
    }

    /**
     * Run the script in the specified file.
     * 
     * @param file
     *            File in which the script is to be found.
     * @throws JepException
     *             If a problem occurs while attempting to run the script.
     */
    public void run(File file) throws JepException {
        jep.runScript(file.getPath());
    }

    // Private Methods

    /**
     * Initialize the newly-instantiated object. This is only to be called
     * during construction.
     * 
     * @throws JepException
     *             If an error occurs initializing the interpreter.
     */
    private void initialize() throws JepException {
        variablesToKeep = (String) jep.getValue("json.dumps(globals().keys())");
    }
}
