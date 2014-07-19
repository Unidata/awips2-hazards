/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.megawidgets;

import org.eclipse.swt.widgets.Label;

/**
 * Description: Interface describing the methods that must be implemented in
 * order to provide an encapsulation of widget components used to represent time
 * in some form.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jun 27, 2014    3512    Chris.Golden Initial creation (refactored out of
 *                                      DateTimeComponent).
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface ITimeComponent {

    // Public Methods

    /**
     * Get the label, if any, for this component.
     * 
     * @return Label for this component, or <code>null</code> if there is no
     *         such label.
     */
    public Label getLabel();

    /**
     * Set the height of the component.
     * 
     * @param height
     *            Height in pixels of the component. The component widgets will
     *            be vertically centered within this height.
     */
    public void setHeight(int height);

    /**
     * Set the component to be enabled or disabled.
     * 
     * @param enable
     *            Flag indicating whether or not the component should be
     *            enabled.
     */
    public void setEnabled(boolean enable);

    /**
     * Set the component to be editable or read-only.
     * 
     * @param editable
     *            Flag indicating whether the component widgets are to be
     *            editable or read-only.
     * @param helper
     *            Control component helper to be used to determine what
     *            background color is appropriate for the widgets' fields.
     */
    public void setEditable(boolean editable, ControlComponentHelper helper);
}