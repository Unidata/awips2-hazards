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
 * Interface describing the methods required in any sort of "qualified" HMI
 * widget created by an {@link IView} and manipulated by it and by a
 * {@link Presenter}. The qualifier, together with the identifier, provides an
 * additional level of differentiation between instances of this interface in
 * comparison with those of {@link IWidget}. The generic parameter
 * <code>Q</code> provides the type of widget qualifier to be used, while
 * <code>I</code> provides the type of widget identifier to be used.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 15, 2014    4243    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IQualifiedWidget<Q, I> {

    // Public Methods

    /**
     * Set the enabled state of the specified widget.
     * 
     * @param qualifier
     *            Qualifier of the widget to have its enabled state changed.
     * @param identifier
     *            Identifier of the widget to have its enabled state changed.
     * @param enable
     *            Flag indicating whether or not the widget should be enabled.
     */
    public void setEnabled(Q qualifier, I identifier, boolean enable);
}
