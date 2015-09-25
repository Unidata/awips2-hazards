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

import gov.noaa.gsd.viz.megawidgets.displaysettings.IDisplaySettings;
import gov.noaa.gsd.viz.megawidgets.displaysettings.TextSettings;
import gov.noaa.gsd.viz.megawidgets.validators.TextValidator;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.text.TextViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Range;
import com.raytheon.uf.viz.spellchecker.text.SpellCheckTextViewer;

/**
 * Text megawidget.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Oct 22, 2013   2168     Chris.Golden      Replaced some GUI creation code with
 *                                           calls to UiBuilder methods to avoid
 *                                           code duplication and encourage uni-
 *                                           form look, and changed to implement
 *                                           new IControl interface.
 * Nov 04, 2013   2336     Chris.Golden      Added multi-line option. Also added
 *                                           option of not notifying listeners of
 *                                           state changes caused by ongoing text
 *                                           alerations, instead saving notifi-
 *                                           cations for when the megawidget
 *                                           loses focus. Also changed to use main
 *                                           label as state label if there is only
 *                                           one state identifier and it has no
 *                                           associated state label.
 * Dec 13, 2013   2545     Chris.Golden      Replaced Text widget with StyledText
 *                                           to provide a component that only
 *                                           shows a vertical scrollbar when
 *                                           needed.
 * Apr 24, 2014   2925     Chris.Golden      Changed to work with new validator
 *                                           package, updated Javadoc and other
 *                                           comments.
 * Jun 17, 2014   3982    Chris.Golden       Changed to have correct look when
 *                                           disabled.
 * Jun 24, 2014   4010     Chris.Golden      Changed to no longer be a subclass
 *                                           of NotifierMegawidget.
 * Aug 05, 2014   3777     Robert.Blum       Added inline spell check functionality.
 * Feb 17, 2015   4756     Chris.Golden      Added display settings saving and
 *                                           restoration.
 * Feb 19, 2015   4959     Dan Schaffer      Fixed bug where wasn't ensuring non-null
 * Mar 30, 2015   7272     mduff             Changes to support Guava upgrade.
 * Mar 31, 2015   6873     Chris.Golden      Added code to ensure that mouse
 *                                           wheel events are not processed by
 *                                           the megawidget, but are instead
 *                                           passed up to any ancestor that is a
 *                                           scrolled composite.
 * Apr 10, 2015   6935     Chris.Golden      Added optional prompt text that if
 *                                           provided is displayed when the text
 *                                           field is empty.
 * Jul 29, 2015   9686     Robert.Blum       Changed the composite to not expand
 *                                           vertically. This was needed in the Product
 *                                           Editor, so when the labels are removed the
 *                                           text fields don't expand to take the extra
 *                                           space. Allowing more fields to be viewed
 *                                           without scrolling.
 * Oct 08, 2015  12165     Chris.Golden      Added option to show no border, so that
 *                                           a read-only text field can look like a
 *                                           label.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see TextSpecifier
 */
public class TextMegawidget extends StatefulMegawidget implements IControl {

    // Protected Static Constants

    /**
     * Set of all mutable property names for instances of this class.
     */
    protected static final Set<String> MUTABLE_PROPERTY_NAMES;
    static {
        Set<String> names = new HashSet<>(
                StatefulMegawidget.MUTABLE_PROPERTY_NAMES);
        names.add(IControlSpecifier.MEGAWIDGET_EDITABLE);
        MUTABLE_PROPERTY_NAMES = ImmutableSet.copyOf(names);
    };

    // Private Constants

    /**
     * Disabled foreground color. This is required because the
     * {@link StyledText} does not take on a proper disabled look when it is
     * disabled. See <a
     * href="https://bugs.eclipse.org/bugs/show_bug.cgi?id=4745">this SWT
     * bug</a> for details.
     */
    protected final Color DISABLED_FOREGROUND_COLOR = new Color(
            Display.getCurrent(), 186, 182, 180);

    // Private Variables

    /**
     * Label associated with this megawidget, if any.
     */
    private final Label label;

    /**
     * Text component associated with this megawidget.
     */
    private final TextViewer textViewer;

    /**
     * Standard foreground color for the styled text component.
     */
    private final Color defaultForegroundColor;

    /**
     * Current value.
     */
    private String state = null;

    /**
     * Flag indicating whether state changes that occur as a result of a text
     * entry change without a focus loss or text validation invocation should be
     * forwarded or not.
     */
    private final boolean onlySendEndStateChanges;

    /**
     * Control component helper.
     */
    private final ControlComponentHelper helper;

    /**
     * State validator.
     */
    private final TextValidator stateValidator;

    /**
     * Last text value that the state change listener knows about.
     */
    private String lastForwardedValue;

    /**
     * Display settings.
     */
    private final TextSettings<String, Integer, Point> displaySettings = new TextSettings<>(
            getClass());

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
    protected TextMegawidget(TextSpecifier specifier, Composite parent,
            Map<String, Object> paramMap) {
        super(specifier, paramMap);
        helper = new ControlComponentHelper(specifier);
        stateValidator = specifier.getStateValidator().copyOf();
        state = (String) specifier.getStartingState(specifier.getIdentifier());
        displaySettings.setText(state);
        displaySettings.setScrollOrigin(new Point(0, 0));

        /*
         * Create the composite holding the components, and the label if
         * appropriate.
         */
        boolean multiLine = (specifier.getNumVisibleLines() > 1);
        Composite panel = UiBuilder
                .buildComposite(
                        parent,
                        (multiLine ? 1 : 2),
                        SWT.NONE,
                        (multiLine ? UiBuilder.CompositeType.MULTI_ROW_VERTICALLY_CONSTRAINED
                                : UiBuilder.CompositeType.SINGLE_ROW),
                        specifier);
        label = UiBuilder.buildLabel(panel, specifier);

        /*
         * Create the text component.
         */
        onlySendEndStateChanges = !specifier.isSendingEveryChange();

        if (specifier.isSpellCheck()) {
            textViewer = new SpellCheckTextViewer(panel,
                    (specifier.isShowBorder() ? SWT.BORDER : SWT.NONE)
                            | (multiLine ? SWT.MULTI : SWT.SINGLE)
                            | (multiLine ? SWT.WRAP | SWT.V_SCROLL : SWT.NONE));
        } else {
            textViewer = new TextViewer(panel,
                    (specifier.isShowBorder() ? SWT.BORDER : SWT.NONE)
                            | (multiLine ? SWT.MULTI : SWT.SINGLE)
                            | (multiLine ? SWT.WRAP | SWT.V_SCROLL : SWT.NONE));
        }
        textViewer.getTextWidget().setAlwaysShowScrollBars(false);
        int limit = specifier.getMaxTextLength();
        if (limit > 0) {
            textViewer.getTextWidget().setTextLimit(limit);
        }
        textViewer.getTextWidget().setEnabled(specifier.isEnabled());
        defaultForegroundColor = textViewer.getTextWidget().getForeground();
        textViewer.getTextWidget().addDisposeListener(new DisposeListener() {

            @Override
            public void widgetDisposed(DisposeEvent e) {
                DISABLED_FOREGROUND_COLOR.dispose();
            }
        });
        UiBuilder.ensureMouseWheelEventsPassedUpToAncestor(textViewer
                .getTextWidget());

        /*
         * Place the text component in the grid.
         */
        GridData gridData = new GridData((multiLine
                || specifier.isHorizontalExpander() ? SWT.FILL : SWT.LEFT),
                (multiLine ? SWT.FILL : SWT.CENTER), true, multiLine);
        gridData.horizontalSpan = ((multiLine == false) && (label == null) ? 2
                : 1);
        GC gc = new GC(textViewer.getTextWidget());
        FontMetrics fontMetrics = gc.getFontMetrics();
        gridData.widthHint = textViewer.getTextWidget().computeSize(
                (specifier.getVisibleTextLength() + 1)
                        * fontMetrics.getAverageCharWidth(), SWT.DEFAULT).x;
        if (multiLine) {
            gridData.heightHint = textViewer.getTextWidget().computeSize(
                    SWT.DEFAULT,
                    specifier.getNumVisibleLines() * fontMetrics.getHeight()).y;
        }
        gc.dispose();
        textViewer.getTextWidget().setLayoutData(gridData);

        /*
         * If only ending state changes are to result in notifications, bind
         * entry field focus loss to trigger a notification if the value has
         * changed in such a way that the state change listener was not
         * notified.
         */
        if (onlySendEndStateChanges) {
            textViewer.getTextWidget().addFocusListener(new FocusAdapter() {
                @Override
                public void focusLost(FocusEvent e) {
                    notifyListenersOfEndingStateChange();
                }
            });
        }

        /*
         * Bind selection changes to update the display settings appropriately,
         * and if only ending state changes are to result in notifications, bind
         * default selection (Enter key) events to trigger a notification if the
         * value has changed in such a way that the state change listener was
         * not notified.
         */
        textViewer.getTextWidget().addSelectionListener(
                new SelectionListener() {
                    @Override
                    public void widgetSelected(SelectionEvent e) {
                        Display.getCurrent().asyncExec(new Runnable() {
                            @Override
                            public void run() {
                                Point selection = textViewer.getTextWidget()
                                        .getSelection();
                                displaySettings.setSelectionRange(Range.closed(
                                        selection.x, selection.y));
                            }
                        });
                    }

                    @Override
                    public void widgetDefaultSelected(SelectionEvent e) {
                        if (onlySendEndStateChanges) {
                            notifyListenersOfEndingStateChange();
                        }
                    }
                });

        /*
         * Bind the text's change event to trigger a change in the record of the
         * state for the widget, and a change in the scale component to match.
         */
        textViewer.getTextWidget().addModifyListener(new ModifyListener() {
            @Override
            public void modifyText(ModifyEvent e) {
                String value = textViewer.getTextWidget().getText();
                state = value;
                displaySettings.setText(state);
                notifyListenersOfRapidStateChange();
            }
        });

        /*
         * If prompt text has been supplied, add a paint listener that renders
         * this text (in the standard color if the widget is disabled, or in a
         * dark gray if enabled so as to differentiate it from actual entered
         * text) if there is no text entered.
         */
        final String promptText = specifier.getPromptText();
        if ((promptText != null) && (promptText.isEmpty() == false)) {
            textViewer.getTextWidget().addPaintListener(new PaintListener() {
                @Override
                public void paintControl(PaintEvent event) {
                    StyledText styledText = textViewer.getTextWidget();
                    if (styledText.getText().isEmpty()) {
                        Font font = styledText.getFont();
                        event.gc.setFont(font);
                        event.gc.getFontMetrics().getAscent();
                        if (styledText.isEnabled()) {
                            event.gc.setForeground(event.display
                                    .getSystemColor(SWT.COLOR_DARK_GRAY));
                        }
                        event.gc.drawText(
                                promptText,
                                styledText.getLeftMargin(),
                                styledText.getTopMargin()
                                        + styledText.getBaseline()
                                        - event.gc.getFontMetrics().getAscent());
                    }
                }
            });
        }

        /*
         * If the text area is multi-line, bind vertical scrollbar movements to
         * be recorded as part of the display settings.
         */
        if (specifier.getNumVisibleLines() > 1) {
            textViewer.getTextWidget().getVerticalBar()
                    .addSelectionListener(new SelectionAdapter() {
                        @Override
                        public void widgetSelected(final SelectionEvent e) {
                            Display.getCurrent().asyncExec(new Runnable() {
                                @Override
                                public void run() {
                                    displaySettings.getScrollOrigin().y = textViewer
                                            .getTextWidget().getTopPixel();
                                }
                            });
                        }
                    });
        }

        /*
         * Bind caret changes to be recorded as part of the display settings,
         * and if a single-line text field, to record the horizontal offset of
         * the viewport as well.
         */
        textViewer.getTextWidget().addCaretListener(new CaretListener() {
            @Override
            public void caretMoved(CaretEvent event) {
                Display.getCurrent().asyncExec(new Runnable() {
                    @Override
                    public void run() {
                        StyledText textWidget = textViewer.getTextWidget();
                        displaySettings.setCaretPosition(textWidget
                                .getCaretOffset());
                        if (((TextSpecifier) getSpecifier())
                                .getNumVisibleLines() == 1) {
                            displaySettings.getScrollOrigin().x = textWidget
                                    .getHorizontalPixel();
                        }
                    }
                });
            }
        });

        /*
         * Set the editability of the megawidget to false if necessary.
         */
        if (isEditable() == false) {
            doSetEditable(false);
        }

        /*
         * Synchronize user-facing widgets to the starting state.
         */
        synchronizeComponentWidgetsToState();
    }

    // Public Methods

    @Override
    public Set<String> getMutablePropertyNames() {
        return MUTABLE_PROPERTY_NAMES;
    }

    @Override
    public Object getMutableProperty(String name)
            throws MegawidgetPropertyException {
        if (IControlSpecifier.MEGAWIDGET_EDITABLE.equals(name)) {
            return isEditable();
        }
        return super.getMutableProperty(name);
    }

    @Override
    public void setMutableProperty(String name, Object value)
            throws MegawidgetPropertyException {
        if (IControlSpecifier.MEGAWIDGET_EDITABLE.equals(name)) {
            setEditable(ConversionUtilities.getPropertyBooleanValueFromObject(
                    getSpecifier().getIdentifier(), getSpecifier().getType(),
                    value, name, null));
        } else {
            super.setMutableProperty(name, value);
        }
    }

    @Override
    public final boolean isEditable() {
        return helper.isEditable();
    }

    @Override
    public final void setEditable(boolean editable) {
        helper.setEditable(editable);
        doSetEditable(editable);
    }

    @Override
    public int getLeftDecorationWidth() {
        return (((textViewer.getTextWidget().getStyle() & SWT.MULTI) != 0)
                || (label == null) ? 0 : helper.getWidestWidgetWidth(label));
    }

    @Override
    public void setLeftDecorationWidth(int width) {
        if (((textViewer.getTextWidget().getStyle() & SWT.MULTI) == 0)
                && (label != null)) {
            helper.setWidgetsWidth(width, label);
        }
    }

    @Override
    public final int getRightDecorationWidth() {
        return 0;
    }

    @Override
    public final void setRightDecorationWidth(int width) {

        /*
         * No action.
         */
    }

    @Override
    public IDisplaySettings getDisplaySettings() {
        return displaySettings;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setDisplaySettings(IDisplaySettings displaySettings) {
        if ((displaySettings.getMegawidgetClass() == getClass())
                && (displaySettings instanceof TextSettings)) {
            final TextSettings<String, Integer, Point> textSettings = (TextSettings<String, Integer, Point>) displaySettings;
            if ((textSettings.getText() != null)
                    && textSettings.getText().equals(state)) {
                Display.getDefault().asyncExec(new Runnable() {

                    @Override
                    public void run() {
                        StyledText textWidget = textViewer.getTextWidget();
                        if (textWidget != null
                                && textWidget.isDisposed() == false) {

                            /*
                             * Use the caret offset specified.
                             */
                            Integer caretPosition = textSettings
                                    .getCaretPosition();
                            if (caretPosition != null) {
                                textWidget.setCaretOffset(caretPosition);
                                TextMegawidget.this.displaySettings
                                        .setCaretPosition(caretPosition);
                            }

                            /*
                             * Use whatever selection range was specified.
                             */
                            Range<Integer> selectionRange = textSettings
                                    .getSelectionRange();
                            if ((selectionRange != null)
                                    && (selectionRange.lowerEndpoint() != selectionRange
                                            .upperEndpoint())) {
                                textWidget.setSelection(
                                        selectionRange.lowerEndpoint(),
                                        selectionRange.upperEndpoint());
                                TextMegawidget.this.displaySettings
                                        .setSelectionRange(selectionRange);
                            }

                            /*
                             * Use only the vertical portion of the scroll
                             * origin if multi-line, and only the horizontal
                             * portion if single-line.
                             */
                            Point scrollOrigin = textSettings.getScrollOrigin();
                            if (scrollOrigin != null) {
                                if (((TextSpecifier) getSpecifier())
                                        .getNumVisibleLines() > 1) {
                                    textWidget.setTopPixel(scrollOrigin.y);
                                    TextMegawidget.this.displaySettings
                                            .getScrollOrigin().y = scrollOrigin.y;
                                } else {
                                    textWidget
                                            .setHorizontalPixel(scrollOrigin.x);
                                    TextMegawidget.this.displaySettings
                                            .getScrollOrigin().x = scrollOrigin.x;
                                }
                            }
                        }
                    }
                });
            }
        }
    }

    // Protected Methods

    @Override
    protected final void doSetEnabled(boolean enable) {
        if (label != null) {
            label.setEnabled(enable);
        }
        textViewer.getTextWidget().setEnabled(enable);
        textViewer.getTextWidget().setBackground(
                helper.getBackgroundColor((enable && isEditable()),
                        textViewer.getTextWidget(), label));
        textViewer.getTextWidget().setForeground(
                enable ? defaultForegroundColor : DISABLED_FOREGROUND_COLOR);
    }

    @Override
    protected final Object doGetState(String identifier) {
        return getStateAdjustingForEmptiness();
    }

    @Override
    protected final void doSetState(String identifier, Object state)
            throws MegawidgetStateException {
        try {
            String valueIfEmpty = ((TextSpecifier) getSpecifier())
                    .getValueIfEmpty();
            if ((state != null) && state.equals(valueIfEmpty)) {
                state = "";
            }
            this.state = stateValidator.convertToStateValue(state);
            updateDisplaySettingsForChangedState();
        } catch (MegawidgetException e) {
            throw new MegawidgetStateException(e);
        }
        synchronizeComponentWidgetsToState();
    }

    @Override
    protected final String doGetStateDescription(String identifier, Object state)
            throws MegawidgetStateException {
        return (state == null ? ((TextSpecifier) getSpecifier())
                .getValueIfEmpty() : state.toString());
    }

    @Override
    protected final void doSynchronizeComponentWidgetsToState() {
        textViewer.getTextWidget().setText(this.state);
        recordLastNotifiedState();
    }

    // Private Methods

    /**
     * Change the component widgets to ensure their state matches that of the
     * editable flag.
     * 
     * @param editable
     *            Flag indicating whether the component widgets are to be
     *            editable or read-only.
     */
    private void doSetEditable(boolean editable) {
        textViewer.getTextWidget().getParent().setEnabled(editable);
        textViewer.getTextWidget().setBackground(
                helper.getBackgroundColor(isEnabled() && editable,
                        textViewer.getTextWidget(), label));
    }

    /**
     * Update the display settings following a programmatic state change.
     */
    private void updateDisplaySettingsForChangedState() {
        displaySettings.setText(state);
        displaySettings.setCaretPosition(0);
        displaySettings.setScrollOrigin(new Point(0, 0));
        displaySettings.setSelectionRange(null);
    }

    /**
     * Get the current state, adjusting for it being empty by using the
     * value-if-empty value (if specified) if the current state is empty.
     * 
     * @return Current state, which may be <code>null</code>.
     */
    private String getStateAdjustingForEmptiness() {
        if ((state == null) || state.isEmpty()) {
            String valueIfEmpty = ((TextSpecifier) getSpecifier())
                    .getValueIfEmpty();
            return (valueIfEmpty != null ? valueIfEmpty : state);
        }
        return state;
    }

    /**
     * Record the current state as one of which the state change listener is
     * assumed to be aware.
     */
    private void recordLastNotifiedState() {
        lastForwardedValue = state;
    }

    /**
     * Notify the state change and notification listeners of a state change that
     * is part of a set of rapidly-occurring changes if necessary.
     */
    private void notifyListenersOfRapidStateChange() {
        if (onlySendEndStateChanges == false) {
            notifyListeners();
        }
    }

    /**
     * Notify the state change and notification listeners of a state change if
     * the current state is not the same as the last state of which the state
     * change listener is assumed to be aware.
     */
    private void notifyListenersOfEndingStateChange() {
        if (((lastForwardedValue != null) && (lastForwardedValue.equals(state) == false))
                || ((lastForwardedValue == null) && (lastForwardedValue != state))) {
            recordLastNotifiedState();
            notifyListeners();
        }
    }

    /**
     * Notify listeners of a state change.
     */
    private void notifyListeners() {
        notifyListener(getSpecifier().getIdentifier(),
                getStateAdjustingForEmptiness());
    }
}
