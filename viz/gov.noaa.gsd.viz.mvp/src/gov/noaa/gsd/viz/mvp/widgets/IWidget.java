/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.mvp.widgets;

import gov.noaa.gsd.viz.mvp.IView;
import gov.noaa.gsd.viz.mvp.Presenter;

/**
 * Interface describing the methods required in any sort of HMI widget created
 * by an {@link IView} and manipulated by it and by a {@link Presenter}. The
 * generic parameter <code>I</code> provides the type of widget identifier to be
 * used.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 08, 2014    2925    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IWidget<I> {

    // Public Methods

    /**
     * Set the enabled state of the specified widget.
     * 
     * @param identifier
     *            Identifier of the widget to have its enabled state changed.
     *            This may be <code>null</code> if this object only handles one
     *            type of invocation.
     * @param enable
     *            Flag indicating whether or not the widget should be enabled.
     */
    public void setEnabled(I identifier, boolean enable);
}
