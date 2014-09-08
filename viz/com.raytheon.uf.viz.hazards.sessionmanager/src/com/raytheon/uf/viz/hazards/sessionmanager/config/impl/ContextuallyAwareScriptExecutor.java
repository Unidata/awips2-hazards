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

import jep.JepException;

import com.raytheon.uf.common.python.concurrent.IPythonExecutor;

/**
 * Description: Abstract base class for contextually aware script executors,
 * that is, those executors that can determine whether another instance of this
 * class is contextually equal to themselves.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 19, 2014    4243    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public abstract class ContextuallyAwareScriptExecutor<P extends Object>
        implements IPythonExecutor<ContextSwitchingPythonEval, P> {

    // Public Methods

    @Override
    public final P execute(ContextSwitchingPythonEval script)
            throws JepException {
        script.setCurrentScriptExecutor(this);
        return doExecute(script);
    }

    // Protected Methods

    /**
     * Determine whether or not this executor is contextually equal to the
     * specified executor. If they are contextually equal, the
     * {@link ContextSwitchingPythonEval} does not need to context-switch
     * between the running of the two executors.
     * <p>
     * This method is used by
     * {@link ContextSwitchingPythonEval#setCurrentScriptExecutor(ContextuallyAwareScriptExecutor)}
     * to determine whether or not they should invoke
     * {@link ContextSwitchingPythonEval#prepareForContextSwitch()} prior to
     * executing their scripts.
     * </p>
     * 
     * @param other
     *            Executor to which to compare this one.
     * @return True if this executor is contextually equal to the other one,
     *         false otherwise.
     */
    protected abstract boolean isContextuallyEqual(
            ContextuallyAwareScriptExecutor<?> other);

    /**
     * Execute the appropriate code using the specified Python evaluator and
     * return the result. This method is called after any context switching
     * within the evaluator has been done as necessary.
     * 
     * @param script
     *            Python evaluator.
     * @return Result.
     * @throws JepException
     *             If a problem occurs while attempting to execute the code.
     */
    protected abstract P doExecute(ContextSwitchingPythonEval script)
            throws JepException;
}