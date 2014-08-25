/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package com.raytheon.uf.viz.python;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.raytheon.uf.common.python.concurrent.IPythonJobListener;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;

/**
 * Runs a PythonJob in an Eclipse job to allow us to track when they run and
 * stop better.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 19, 2013            mnash     Initial creation
 * 
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class VizPythonJob<T extends Object> extends Job implements
        IPythonJobListener<T> {

    private static final IUFStatusHandler theHandler = UFStatus
            .getHandler(VizPythonJob.class);

    IPythonJobListener<T> listener;

    Object lock = new Object();

    /**
     * @param name
     */
    public VizPythonJob(String name, IPythonJobListener<T> listener) {
        super("Executing " + name);
        this.listener = listener;
        schedule();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.
     * IProgressMonitor)
     */
    @Override
    protected IStatus run(IProgressMonitor monitor) {
        while (monitor.isCanceled() == false) {
            synchronized (lock) {
                try {
                    lock.wait();
                } catch (InterruptedException e) {
                    theHandler.error("Job was interrupted", e);
                }
            }

        }
        return Status.OK_STATUS;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.python.concurrent.IPythonJobListener#jobFinished
     * (java.lang.Object)
     */
    @Override
    public void jobFinished(T result) {
        listener.jobFinished(result);
        cancel();
        synchronized (lock) {
            lock.notify();
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.raytheon.uf.common.python.concurrent.IPythonJobListener#jobFailed
     * (java.lang.Throwable)
     */
    @Override
    public void jobFailed(Throwable e) {
        listener.jobFailed(e);
        cancel();
        synchronized (lock) {
            lock.notify();
        }
    }
}
