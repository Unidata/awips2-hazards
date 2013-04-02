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

import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * Label megawidget created by a label megawidget specifier.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see LabelSpecifier
 */
public class LabelMegawidget extends Megawidget {

    // Private Variables

    /**
     * Label.
     */
    private final Label label;

    // Protected Constructors

    /**
     * Construct a standard instance.
     * 
     * @param specifier
     *            Specifier.
     * @param parent
     *            Parent of this megawidget.
     * @param paramMap
     *            Hash table mapping megawidget creation time parameter
     *            identifiers to values.
     */
    protected LabelMegawidget(LabelSpecifier specifier, Composite parent,
            Map<String, Object> paramMap) {
        super(specifier);

        // Create a label widget.
        label = new Label(parent, SWT.WRAP);
        label.setText(specifier.getLabel());
        label.setEnabled(specifier.isEnabled());

        // Place the widget in the grid. If the widget may end
        // up wrapping, then it must be registered as a lis-
        // tener for its parent's resize events so that it can
        // have its width hint set each time the parent is re-
        // sized.
        GridData gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
        gridData.horizontalSpan = specifier.getWidth();
        specifier.ensureChildIsResizedWithParent(parent, label);
        gridData.verticalIndent = specifier.getSpacing();
        label.setLayoutData(gridData);
    }

    // Protected Methods

    /**
     * Change the component widgets to ensure their state matches that of the
     * enabled flag.
     * 
     * @param enable
     *            Flag indicating whether the widget components are to be
     *            enabled or disabled.
     */
    @Override
    protected void doSetEnabled(boolean enable) {
        label.setEnabled(enable);
    }

    /**
     * Change the component widgets to ensure their state matches that of the
     * editable flag.
     * 
     * @param editable
     *            Flag indicating whether the widget components are to be
     *            editable or read-only.
     */
    @Override
    protected final void doSetEditable(boolean editable) {

        // No action.
    }
}