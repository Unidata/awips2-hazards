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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

/**
 * Megawidget providing a simple push button.
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
 * @see ButtonSpecifier
 */
public class ButtonMegawidget extends NotifierMegawidget {

    // Private Variables

    /**
     * Button.
     */
    private final Button button;

    // Protected Constructors

    /**
     * Construct a standard instance.
     * 
     * @param specifier
     *            Specifier.
     * @param parent
     *            Parent of the megawidget.
     * @param paramMap
     *            Hash table mapping megawidget creation time parameter
     *            identifiers to values.
     */
    protected ButtonMegawidget(ButtonSpecifier specifier, Composite parent,
            Map<String, Object> paramMap) {
        super(specifier, paramMap);

        // Create a button widget.
        button = new Button(parent, SWT.PUSH);
        button.setText(specifier.getLabel());
        button.setEnabled(specifier.isEnabled());

        // Place the widget in the grid.
        GridData gridData = new GridData(SWT.CENTER, SWT.CENTER, false, false);
        gridData.horizontalSpan = specifier.getWidth();
        gridData.verticalIndent = specifier.getSpacing();
        button.setLayoutData(gridData);

        // If the button should notify a listener when
        // it is invoked, set up a listener to do so.
        if (specifier.isToNotify()) {
            button.addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    notifyListener();
                }
            });
        }

        // Disable the button if not editable.
        if (isEditable() == false) {
            doSetEditable(false);
        }
    }

    // Protected Methods

    /**
     * Change the component widgets to ensure their state matches that of the
     * enabled flag.
     * 
     * @param enable
     *            Flag indicating whether the component widgets are to be
     *            enabled or disabled.
     */
    @Override
    protected final void doSetEnabled(boolean enable) {
        button.setEnabled(enable && isEditable());
    }

    /**
     * Change the component widgets to ensure their state matches that of the
     * editable flag.
     * 
     * @param editable
     *            Flag indicating whether the component widgets are to be
     *            editable or read-only.
     */
    @Override
    protected final void doSetEditable(boolean editable) {
        button.setEnabled(editable && isEnabled());
    }
}