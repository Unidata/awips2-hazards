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
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Slider;
import org.eclipse.swt.widgets.Text;

/**
 * Slider megawidget, allowing for the selection of an integer using a slider.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Bryon.Lawrence      Initial induction into repo
 * 
 * </pre>
 * 
 * @version 1.0
 * @author Bryon.Lawrence
 * @see SliderSpecifier
 */
public class SliderMegawidget extends StatefulMegawidget {

    // Private Variables

    /**
     * Label associated with this megawidget, if any.
     */
    private final Label label;

    /**
     * Slider component associated with this megawidget.
     */
    private final Slider slider;

    /**
     * Label which displays the currently selected value of the slider.
     */
    private final Text sliderValueText;

    /**
     * Current value.
     */
    private Integer state = null;

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
    protected SliderMegawidget(SliderSpecifier specifier, Composite parent,
            Map<String, Object> paramMap) {
        super(specifier, paramMap);

        // Create a panel in which to place the widgets.
        // This is done so that it may be rendered read-only
        // by disabling the panel, which in SWT has the
        // effect of disabling mouse and keyboard input for
        // the child widgets without making them look dis-
        // abled; in order to group the widgets properly
        // into a single megawidget; and to allow the space
        // for the label, if it exists, to be sized to match
        // other labels.
        Composite panel = new Composite(parent, SWT.NONE);
        GridLayout gridLayout = new GridLayout(2, false);
        gridLayout.marginWidth = gridLayout.marginHeight = 0;
        gridLayout.horizontalSpacing = 10;
        panel.setLayout(gridLayout);
        GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.horizontalSpan = specifier.getWidth();
        gridData.verticalIndent = specifier.getSpacing();
        panel.setLayoutData(gridData);

        // Add a label if one is required.
        if ((specifier.getLabel() != null)
                && (specifier.getLabel().length() > 0)) {

            // Create a label widget.
            label = new Label(panel, SWT.NONE);
            label.setText(specifier.getLabel());
            label.setEnabled(specifier.isEnabled());

            // Place the label in the parent's grid.
            gridData = new GridData(SWT.LEFT, SWT.CENTER, false, false);
            gridData.horizontalSpan = 2;
            label.setLayoutData(gridData);
        } else {
            label = null;
        }

        // Create the slider.
        slider = new Slider(panel, SWT.BORDER + SWT.WRAP);
        slider.setMinimum(specifier.getMinimumValue());
        slider.setThumb(1);
        slider.setMaximum(specifier.getMaximumValue() + slider.getThumb());
        slider.setPageIncrement(specifier.getIncrementDelta());
        slider.setEnabled(specifier.isEnabled());

        // Place the slider in the parent's grid.
        gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
        gridData.horizontalSpan = 1;
        slider.setLayoutData(gridData);

        // Label sliderValueLabel = new Label(panel, SWT.NONE);
        sliderValueText = new Text(panel, SWT.SINGLE | SWT.BORDER);
        gridData = new GridData(SWT.LEFT, SWT.LEFT, false, false);

        /*
         * TO DO: The width should be based on the maximum slider value.
         */
        gridData.widthHint = 30;
        sliderValueText.setLayoutData(gridData);
        sliderValueText.setText("100");
        // sliderValueText.setEditable(false);

        // Bind the slider selection event to trigger
        // a change in the state.
        slider.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int state = SliderMegawidget.this.slider.getSelection();

                // int state = slider.getMaximum() - slider.getSelection() +
                // slider.getMinimum() - slider.getThumb();
                if ((SliderMegawidget.this.state == null)
                        || (state != SliderMegawidget.this.state.intValue())) {
                    SliderMegawidget.this.state = state;
                    SliderMegawidget.this.sliderValueText.setText(Integer
                            .toString(state));
                    notifyListener(getSpecifier().getIdentifier(), state);
                    notifyListener();
                }
            }
        });

        /*
         * Add a listener to verify that the text entered is a digit or a
         * keyboard control character.
         */
        sliderValueText.addVerifyListener(new VerifyListener() {
            @Override
            public void verifyText(VerifyEvent e) {
                if (e.character == SWT.BS || e.keyCode == SWT.ARROW_LEFT
                        || e.keyCode == SWT.ARROW_RIGHT || e.keyCode == SWT.DEL) {
                    e.doit = true;
                    return;
                }

                /*
                 * Verify the text.
                 */
                try {
                    Integer.parseInt(e.text);
                    e.doit = true;
                } catch (NumberFormatException ne) {
                    e.doit = false;
                }

            }
        });

        /*
         * Add a listener to handle the characters entered by the user
         */
        sliderValueText.addKeyListener(new KeyAdapter() {
            @Override
            public void keyReleased(KeyEvent e) {
                String textEntered = sliderValueText.getText();

                if (textEntered.length() > 0) {
                    try {
                        int value = Integer.parseInt(textEntered);

                        int max = SliderMegawidget.this.slider.getMaximum();
                        int min = SliderMegawidget.this.slider.getMinimum();

                        if (value >= min && value <= max) {
                            SliderMegawidget.this.slider.setSelection(value);
                        } else {
                            value = SliderMegawidget.this.slider.getSelection();
                            sliderValueText.setText(Integer.toString(value));
                        }

                        SliderMegawidget.this.state = SliderMegawidget.this.slider
                                .getSelection();
                        notifyListener(getSpecifier().getIdentifier(), state);
                        notifyListener();
                    } catch (NumberFormatException except) {
                        int value = SliderMegawidget.this.slider.getSelection();
                        sliderValueText.setText(Integer.toString(value));
                    }
                }
            }
        });

        // Render the slider uneditable if necessary.
        if (isEditable() == false) {
            doSetEditable(false);
        }
    }

    // Public Methods

    /**
     * Determine the left decoration width for this megawidget, if the widget
     * has a decoration (label, etc.) to the left of its main component
     * widget(s). This is used to query all sibling megawidgets to determine
     * which one has the largest left decoration.
     * 
     * @return Width in pixels required for the left decoration of this
     *         megawidget, or 0 if the megawidget has no left decoration.
     */
    @Override
    public int getLeftDecorationWidth() {
        return (label == null ? 0 : getWidestWidgetWidth(label));
    }

    /**
     * Set the left decoration width for this megawidget to that specified, if
     * the widget has a decoration to the left of its main component widget(s).
     * This is used to set sibling megawidgets to all use the width of the
     * largest left decoration used by the siblings, if any.
     * 
     * @param width
     *            Width to be used if this megawidget has a left decoration.
     */
    @Override
    public void setLeftDecorationWidth(int width) {
        if (label != null) {
            setWidgetsWidth(width, label);
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
        if (label != null) {
            label.setEnabled(enable);
        }
        slider.setEnabled(enable);
        sliderValueText.setEnabled(enable);
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
        slider.getParent().setEnabled(editable);
        slider.setBackground(getBackgroundColor(editable, slider, label));
        sliderValueText.setBackground(getBackgroundColor(editable,
                sliderValueText, label));
    }

    /**
     * Get the current state for the specified identifier. This method is called
     * by <code>getState()</code> only after the latter has ensured that the
     * supplied state identifier is valid.
     * 
     * @param identifier
     *            Identifier for which state is desired. Implementations may
     *            assume that the state identifier supplied by this parameter is
     *            valid for this megawidget.
     * @return Object making up the current state for the specified identifier.
     */
    @Override
    protected final Object doGetState(String identifier) {
        return state;
    }

    /**
     * Set the current state for the specified identifier. This method is called
     * by <code>setState()</code> only after the latter has ensured that the
     * supplied state identifier is valid, and has set a flag that indicates
     * that this setting of the state will not trigger the megawidget to notify
     * its listener of an invocation.
     * 
     * @param identifier
     *            Identifier for which state is to be set. Implementations may
     *            assume that the state identifier supplied by this parameter is
     *            valid for this megawidget.
     * @param state
     *            Object making up the state to be used for this identifier, or
     *            <code>null</code> if this state should be reset.
     * @throws MegawidgetStateException
     *             If new state is not of a valid type for this <code>
     *             StatefulWidget</code> implementation.
     */
    @Override
    protected final void doSetState(String identifier, Object state)
            throws MegawidgetStateException {
        SliderSpecifier specifier = getSpecifier();
        int value = getStateIntegerValueFromObject(state, identifier,
                specifier.getMinimumValue());
        if ((value < specifier.getMinimumValue())
                || (value > specifier.getMaximumValue())) {
            throw new MegawidgetStateException(identifier, specifier.getType(),
                    value, "out of bounds (minimum = "
                            + specifier.getMinimumValue() + ", maximum = "
                            + specifier.getMaximumValue() + " (inclusive))");
        }
        this.state = value;
        slider.setSelection(value);
        sliderValueText.setText(Integer.toString(value));
    }

    /**
     * Get a shortened description of the specified state for the specified
     * identifier. This method is called by
     * <code>getStateDescription() only after
     * the latter has ensured that the supplied state
     * identifier is valid.
     * 
     * @param identifier
     *            Identifier to which the state would be assigned.
     *            Implementations may assume that the state identifier supplied
     *            by this parameter is valid for this megawidget.
     * @param state
     *            State for which to generate a shortened description.
     * @return Description of the specified state.
     * @throws MegawidgetStateException
     *             If the specified state is not of a valid type for this
     *             <code>StatefulWidget </code> implementation.
     */
    @Override
    protected final String doGetStateDescription(String identifier, Object state)
            throws MegawidgetStateException {
        return (state == null ? null : state.toString());
    }
}
