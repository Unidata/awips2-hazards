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
import gov.noaa.gsd.viz.megawidgets.displaysettings.MultiPageScrollSettings;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

import com.google.common.collect.ImmutableSet;

/**
 * Detailed combo box megawidget, providing a dropdown combo box allowing the
 * selection of a single choice, and a group-style panel beneath the combo box
 * holding any detail megawidgets associated with the current choice.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jul 23, 2014    4122    Chris.Golden Initial creation.
 * Oct 20, 2014    4818    Chris.Golden Added option of providing scrollable
 *                                      detail panels for child megawidgets.
 *                                      Also added use of display settings,
 *                                      allowing the saving and restoring of
 *                                      scroll origins.
 * Aug 20, 2015    9617    Robert.Blum  Added ability for users to add entries to 
 *                                      comboboxes.
 * Aug 28, 2015    9617    Chris.Golden Added fixes for code added under this
 *                                      ticket.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see DetailedComboBoxSpecifier
 */
public class DetailedComboBoxMegawidget extends SingleBoundedChoiceMegawidget
        implements IControl, IContainer<IControl>, IResizer {

    // Protected Static Constants

    /**
     * Set of all mutable property names for instances of this class.
     */
    protected static final Set<String> MUTABLE_PROPERTY_NAMES;
    static {
        Set<String> names = new HashSet<>(
                SingleBoundedChoiceMegawidget.MUTABLE_PROPERTY_NAMES_WITHOUT_CHOICES);
        names.add(IControlSpecifier.MEGAWIDGET_EDITABLE);
        MUTABLE_PROPERTY_NAMES = ImmutableSet.copyOf(names);
    };

    // Private Static Constants

    /**
     * Placeholder text to be used for the group label in order to get SWT to
     * make the group have rounded corners.
     */
    private static final String PLACEHOLDER_TEXT = "-";

    // Private Variables

    /**
     * Label associated with this megawidget, if any.
     */
    private final Label label;

    /**
     * Combo box component helper, managing the combo box associated with this
     * megawidget.
     */
    private final ComboBoxComponentHelper comboBoxHelper;

    /**
     * Display settings.
     */
    private final MultiPageScrollSettings<Point> displaySettings = new MultiPageScrollSettings<>(
            getClass());

    /**
     * Map of choice identifiers to their names.
     */
    private final Map<String, String> choiceNamesForIdentifiers = new HashMap<>();

    /**
     * Map of choice names to their identifiers.
     */
    private final Map<String, String> choiceIdentifiersForNames = new HashMap<>();

    /**
     * Map of choice identifiers to their detail panels. Any choice that has no
     * detail panel will still have an entry here giving it an empty panel.
     */
    private final Map<String, Composite> choicePanelsForIdentifiers = new HashMap<>();

    /**
     * Map of detail panels to their primary choice identifiers, meaning the
     * first choice identifiers with which the detail panels were associated.
     */
    private final Map<Composite, String> choiceIdentifiersForPanels = new HashMap<>();

    /**
     * Map of choice identifiers to their scrolled composites. If this
     * megawidget is not scrollable, this will be <code>null</code>.
     */
    private final Map<String, ScrolledComposite> choiceScrolledCompositesForIdentifiers;

    /**
     * Detail panel for any user added choices.
     */
    private final Composite newChoiceDetailPanel;

    /**
     * Details panel, holding whichever choice panel is associated with the
     * currently selected choice.
     */
    private final Composite detailsPanel;

    /**
     * Details layout, the stack layout used to flip between different choice's
     * detail panels.
     */
    private final StackLayout detailsLayout;

    /**
     * All the detail megawidgets for this megawidget.
     */
    private final List<IControl> children;

    /**
     * Control component helper.
     */
    private final ControlComponentHelper helper;

    /**
     * Header height in pixels.
     */
    private final int headerHeight;

    /**
     * Group border width in pixels.
     */
    private final int groupBorderWidth;

    /**
     * Header width hint in pixels.
     */
    private final int headerWidthHint;

    /**
     * Grid layout data for the main panel.
     */
    private final GridData mainGridData;

    /**
     * Resize listener supplied at creation time.
     */
    private final IResizeListener resizeListener;

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
     * @throws MegawidgetException
     *             If a problem occurs while attempting to construct any detail
     *             megawidgets.
     */
    protected DetailedComboBoxMegawidget(DetailedComboBoxSpecifier specifier,
            Composite parent, Map<String, Object> paramMap)
            throws MegawidgetException {
        super(specifier, paramMap);
        helper = new ControlComponentHelper(specifier);

        /*
         * Create the composite holding the component widgets. This parent
         * composite uses the form layout in order to position an optional label
         * and a combo box over the top border of a group composite, which in
         * turn holds any detail field megawidgets associated with the currently
         * selected choice.
         */
        Composite panel = new Composite(parent, SWT.NONE);
        panel.setLayout(new FormLayout());
        mainGridData = new GridData(SWT.FILL, SWT.FILL,
                specifier.isHorizontalExpander(),
                specifier.isVerticalExpander());
        mainGridData.horizontalSpan = specifier.getWidth();
        mainGridData.verticalIndent = specifier.getSpacing();
        panel.setLayoutData(mainGridData);

        /*
         * Create the label if appropriate.
         */
        if ((specifier.getLabel() != null)
                && (specifier.getLabel().length() > 0)) {
            label = new Label(panel, SWT.NONE);
            label.setText(specifier.getLabel());
            label.setEnabled(specifier.isEnabled());
        } else {
            label = null;
        }

        /*
         * Create the combo box, and create bidirectional associations between
         * the choice names and their identifiers before adding the choice names
         * to the combo box.
         * 
         * Note: For some reason, the background color of the combo box's panel
         * needs to be set to something non-null, otherwise the label does not
         * show up. SWT bug?
         */
        Composite comboBoxPanel = new Composite(panel, SWT.NONE);
        comboBoxPanel.setBackground(Display.getDefault().getSystemColor(
                SWT.COLOR_WIDGET_BACKGROUND));
        GridLayout comboBoxLayout = new GridLayout(1, false);
        comboBoxLayout.marginWidth = comboBoxLayout.marginHeight = 0;
        comboBoxPanel.setLayout(comboBoxLayout);
        comboBoxHelper = new ComboBoxComponentHelper(comboBoxPanel,
                new IComboBoxComponentHolder() {

                    @Override
                    public String getSelection() {
                        return choiceNamesForIdentifiers.get(state);
                    }

                    @Override
                    public void setSelection(String item) {
                        handleSelectionChange(item);
                    }
                }, specifier.isAutocompleteEnabled(),
                specifier.isAllowNewChoiceEnabled(), specifier.isEnabled(),
                label, helper);
        List<String> choiceIdentifiers = populateComboBoxWithChoices();
        comboBoxHelper.getComboBox().setLayoutData(
                new GridData(SWT.FILL, SWT.FILL, true, true));

        /*
         * Create the group that will hold the detail field megawidgets. The
         * group is given a bogus text label because under the target window
         * manager, SWT groups' borders are drawn differently if they have
         * labels (rounded corners) or not (sharp corners), and this group
         * should look like other labeled groups.
         */
        Group group = new Group(panel, SWT.NONE);
        group.setText(PLACEHOLDER_TEXT);
        GridLayout groupLayout = new GridLayout(1, false);
        groupLayout.marginWidth = groupLayout.marginHeight = 0;
        group.setLayout(groupLayout);
        group.setEnabled(specifier.isEnabled());

        /*
         * Create the panel within the group that in turn holds the stack layout
         * to allow different detail field panels to be shown, one at a time.
         */
        detailsPanel = new Composite(group, SWT.NONE);
        detailsLayout = new StackLayout();
        detailsPanel.setLayout(detailsLayout);
        detailsPanel
                .setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

        /*
         * Determine the sizes of the label (if any) and combo box, and
         * determine the vertical offsets for the label and combo box to ensure
         * that these components are all vertically aligned correctly with
         * respect to one another: the label and combo box lined up to have
         * their text at the same vertical point, and the group's top border
         * running under the vertical center of the label and combo box.
         */
        int labelVerticalOffset = 0, comboBoxVerticalOffset = 0;
        Point labelSize;
        if (label != null) {
            labelSize = label.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        } else {
            Label tempLabel = new Label(panel, SWT.NONE);
            tempLabel.setText(PLACEHOLDER_TEXT);
            labelSize = tempLabel.computeSize(SWT.DEFAULT, SWT.DEFAULT);
            tempLabel.dispose();
        }
        Point comboBoxSize = comboBoxHelper.getComboBox().computeSize(
                SWT.DEFAULT, SWT.DEFAULT);
        if (labelSize.y > comboBoxSize.y) {
            comboBoxVerticalOffset = (labelSize.y - comboBoxSize.y) / 2;
            headerHeight = labelSize.y;
        } else {
            labelVerticalOffset = (comboBoxSize.y - labelSize.y) / 2;
            headerHeight = comboBoxSize.y;
        }

        /*
         * Ensure that the group within the panel is vertically offset down far
         * enough to be just below the combo box and label's bottommost area,
         * i.e. so that the latter two do not overlap this panel.
         */
        groupLayout.marginTop = labelVerticalOffset;

        /*
         * If there is a label, configure it to be inset a bit to the right of
         * the leftmost part of the group, and overlaying its border, vertically
         * centered on said border.
         */
        if (label != null) {
            FormData labelFormData = new FormData();
            labelFormData.left = new FormAttachment(0, 5);
            labelFormData.top = new FormAttachment(0, labelVerticalOffset);
            labelFormData.right = new FormAttachment(0, labelSize.x + 5);
            labelFormData.height = labelSize.y;
            label.setLayoutData(labelFormData);
        }

        /*
         * Configure the combo to be inset a bit to the right of the label (or
         * where the label would be, if there is no label) and overlaying the
         * group's border, vertically centered on said border. If the header is
         * to expand horizontally, make the right side of the combo box align
         * close to the right edge of the grouping.
         */
        FormData comboFormData = new FormData();
        if (label != null) {
            comboFormData.left = new FormAttachment(label, 0);
        } else {
            comboFormData.left = new FormAttachment(0, 5);
        }
        comboFormData.top = new FormAttachment(0, comboBoxVerticalOffset);
        if (specifier.isHeaderHorizontalExpander()) {
            comboFormData.right = new FormAttachment(100, -5);
        } else {
            comboFormData.width = comboBoxSize.x;
        }
        comboFormData.height = comboBoxSize.y;
        comboBoxPanel.setLayoutData(comboFormData);

        /*
         * Configure the group to take up the whole area of the parent, except
         * for the top, which is vertically offset down to ensure that its
         * border falls under the vertical center of the label and combo box.
         */
        FormData groupFormData = new FormData();
        groupFormData.left = new FormAttachment(0, 0);
        groupFormData.right = new FormAttachment(100, 0);
        groupFormData.top = new FormAttachment(0, labelVerticalOffset);
        groupFormData.bottom = new FormAttachment(100, 0);
        group.setLayoutData(groupFormData);

        /*
         * Remember the resize listener passed in, then create a copy of the
         * creation-time parameters map, so that alterations to its resize
         * listener do not affect the original. Then alter the copy to hold a
         * reference to a new resize listener that allows this megawidget to
         * adjust its preferred size before passing on notifications of a size
         * change to the original listener.
         */
        resizeListener = (IResizeListener) paramMap.get(RESIZE_LISTENER);
        paramMap = new HashMap<>(paramMap);
        paramMap.put(RESIZE_LISTENER, new IResizeListener() {

            @Override
            public void sizeChanged(IResizer megawidget) {
                updateRequestedSize();
                if (resizeListener != null) {
                    resizeListener.sizeChanged(DetailedComboBoxMegawidget.this);
                }
            }
        });

        /*
         * Iterate through the choices, creating detail megawidget panels for
         * each choice that has associated detail fields. An identity hash map
         * is used to track choice panels that have already been created and
         * their associated specifier lists so that if a specifier list that is
         * a repeat of that of another choice is encountered, the panel created
         * for that other choice is simply reused, instead of recreating the
         * same megawidgets again. While creating the panels, keep track of the
         * largest width and height needed to encompass any of them.
         */
        choiceScrolledCompositesForIdentifiers = (specifier.isScrollable() ? new HashMap<String, ScrolledComposite>()
                : null);
        Map<List<IControlSpecifier>, Composite> choicePanelsForSpecifierLists = new IdentityHashMap<>();
        List<IControl> allChildren = new ArrayList<>();
        Point neededDimensions = new Point(0, 0);
        Composite emptyPanel = new Composite(detailsPanel, SWT.NONE);
        choicePanelsForIdentifiers.put(null, emptyPanel);
        for (String identifier : choiceIdentifiers) {

            /*
             * Get the specifiers for the detail fields for this choice; if
             * there are none, use an empty panel for this choice's details
             * panel.
             */
            List<IControlSpecifier> specifiers = specifier
                    .getDetailFieldsForChoice(identifier);
            if (specifiers == null) {
                choicePanelsForIdentifiers.put(identifier, emptyPanel);
                continue;
            }

            /*
             * See if the choice panel has already been created for this list of
             * specifiers; if it has not, create it now.
             */
            Composite choicePanel = choicePanelsForSpecifierLists
                    .get(specifiers);
            if (choicePanel == null) {
                choicePanel = createChoicePanel(specifier, identifier,
                        specifiers, neededDimensions, allChildren, paramMap);
                choiceIdentifiersForPanels.put(choicePanel, identifier);
            }

            /*
             * Associate the choice panel with this list of specifiers, and with
             * this choice.
             */
            choicePanelsForSpecifierLists.put(specifiers, choicePanel);
            choicePanelsForIdentifiers.put(identifier, choicePanel);
        }

        /*
         * Get the specifiers for the detail fields for any new choice; if there
         * are none, use an empty panel.
         */
        List<IControlSpecifier> specifiers = specifier
                .getNewChoiceDetailFields();
        if (specifiers == null) {
            newChoiceDetailPanel = emptyPanel;
        } else {
            newChoiceDetailPanel = createChoicePanel(specifier, state,
                    specifiers, neededDimensions, allChildren, paramMap);
        }

        /*
         * Remember all children just created.
         */
        this.children = Collections.unmodifiableList(allChildren);

        /*
         * Remember the group border width and the hint as to what the header
         * needs for width. Then ensure that the main panel is large enough to
         * hold the header and the largest dimensions of the detail area.
         */
        groupBorderWidth = group.computeTrim(0, 0, 0, 0).width / 2;
        headerWidthHint = (label != null ? labelSize.x + 5 : 0) + 10
                + comboBoxSize.x;
        updateMainGridLayoutData(neededDimensions);

        /*
         * Render the components uneditable if necessary.
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
        if (name.equals(IControlSpecifier.MEGAWIDGET_EDITABLE)) {
            return isEditable();
        }
        return super.getMutableProperty(name);
    }

    @Override
    public void setMutableProperty(String name, Object value)
            throws MegawidgetPropertyException {
        if (name.equals(IControlSpecifier.MEGAWIDGET_EDITABLE)) {
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
        return (label != null ? label.computeSize(SWT.DEFAULT, SWT.DEFAULT).x + 5
                : 0);
    }

    @Override
    public void setLeftDecorationWidth(int width) {
        if (label != null) {
            ((FormData) comboBoxHelper.getComboBox().getParent()
                    .getLayoutData()).left = new FormAttachment(0, width + 10);
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
    public List<IControl> getChildren() {
        return children;
    }

    @Override
    public IDisplaySettings getDisplaySettings() {
        return displaySettings;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void setDisplaySettings(final IDisplaySettings displaySettings) {
        Display.getDefault().asyncExec(new Runnable() {

            @Override
            public void run() {
                if ((displaySettings.getMegawidgetClass() == DetailedComboBoxMegawidget.this
                        .getClass())
                        && (displaySettings instanceof MultiPageScrollSettings)) {
                    if (choiceScrolledCompositesForIdentifiers != null) {
                        Map<String, Point> scrollOriginsForDetailPages = ((MultiPageScrollSettings<Point>) displaySettings)
                                .getScrollOriginsForPages();
                        for (Map.Entry<String, Point> entry : scrollOriginsForDetailPages
                                .entrySet()) {
                            ScrolledComposite scrolledComposite = choiceScrolledCompositesForIdentifiers
                                    .get(entry.getKey());
                            if ((scrolledComposite != null)
                                    && (scrolledComposite.isDisposed() == false)) {
                                scrolledComposite.setOrigin(entry.getValue());
                                DetailedComboBoxMegawidget.this.displaySettings
                                        .setScrollOriginForPage(entry.getKey(),
                                                entry.getValue());
                            }
                        }
                    }
                }
            }
        });
    }

    // Protected Methods

    @Override
    protected final boolean isChoicesListMutable() {
        return false;
    }

    @Override
    protected final void prepareForChoicesChange() {
        throwChoicesChangeException();
    }

    @Override
    protected void cancelPreparationForChoicesChange() {
        throwChoicesChangeException();
    }

    @Override
    protected final void synchronizeComponentWidgetsToChoices() {
        throwChoicesChangeException();
    }

    @Override
    protected final void doSynchronizeComponentWidgetsToState() {
        comboBoxHelper.setSelection(choiceNamesForIdentifiers.get(state));
        synchronizeDetailsPanelWithState();
    }

    @Override
    protected final void doSetEnabled(boolean enable) {
        if (label != null) {
            label.setEnabled(enable);
        }
        comboBoxHelper.setEnabled(enable);
        for (IControl child : children) {
            child.setEnabled(enable);
        }
    }

    // Private Methods

    /**
     * Populate the combo box with choices.
     * 
     * @return List of choice identifiers for the choices available to the combo
     *         box.
     */
    private List<String> populateComboBoxWithChoices() {
        List<?> choices = getStateValidator().getAvailableChoices();
        String[] names = new String[choices.size()];
        List<String> choiceIdentifiers = new ArrayList<>(choices.size());
        DetailedComboBoxSpecifier specifier = getSpecifier();
        int index = 0;
        for (Object choice : choices) {
            String identifier = specifier.getIdentifierOfNode(choice);
            String name = specifier.getNameOfNode(choice);
            choiceNamesForIdentifiers.put(identifier, name);
            choiceIdentifiersForNames.put(name, identifier);
            names[index++] = name;
            choiceIdentifiers.add(identifier);
        }
        comboBoxHelper.getComboBox().setItems(names);
        return choiceIdentifiers;
    }

    /**
     * Create a choice panel.
     * 
     * @param specifier
     *            Specifier of this megawidget.
     * @param identifier
     *            Identifier of the choice for which the panel is being created;
     *            if <code>null</code>, it is assumed to be for new choices that
     *            the user might create later.
     * @param childSpecifiers
     *            List of child specifiers that together make up the
     *            specification for the choice panel to be created.
     * @param neededDimensions
     *            Needed dimensions to fit this and other choice panels; these
     *            are updated by this method if necessary during its invocation.
     * @param allChildren
     *            List of all child megawidgets of this megawidget; this list is
     *            updated by the addition of any child megawidgets created
     *            during this method's invocation.
     * @param paramMap
     *            Map of parameters to be used to create child megawidgets.
     * @return New choice panel.
     * @throws MegawidgetException
     *             If the child megawidget specifiers are illegal in some way.
     */
    private Composite createChoicePanel(DetailedComboBoxSpecifier specifier,
            String identifier, List<IControlSpecifier> childSpecifiers,
            Point neededDimensions, List<IControl> allChildren,
            Map<String, Object> paramMap) throws MegawidgetException {

        /*
         * If the megawidget is to be scrollable, create a scrolled composite
         * and use its client area composite as the choice panel. Otherwise,
         * just create the choice panel directly.
         */
        final ScrolledComposite scrolledComposite;
        Map<String, Object> choicePanelParamMap;
        Composite choicePanel;
        if (choiceScrolledCompositesForIdentifiers != null) {
            choicePanelParamMap = new HashMap<>(paramMap);
            scrolledComposite = UiBuilder.buildScrolledComposite(this,
                    detailsPanel, displaySettings, identifier,
                    choicePanelParamMap);
            if (identifier != null) {
                choiceScrolledCompositesForIdentifiers.put(identifier,
                        scrolledComposite);
            }
            choicePanel = (Composite) scrolledComposite.getContent();
        } else {
            choicePanelParamMap = paramMap;
            choicePanel = new Composite(detailsPanel, SWT.NONE);
            scrolledComposite = null;
        }

        /*
         * Create the child megawidgets to be the detail fields for this choice
         * panel, and see if this results in this choice panel being larger in
         * either dimension than the previously largest one; if so, record the
         * new dimensions.
         */
        List<IControl> children = UiBuilder.createChildMegawidgets(specifier,
                choicePanel, 1, specifier.isEnabled(), specifier.isEditable(),
                childSpecifiers, choicePanelParamMap);
        allChildren.addAll(children);
        if (scrolledComposite != null) {
            choicePanel = scrolledComposite;
        }
        updateSizeToEncompassComposite(neededDimensions, choicePanel);

        /*
         * If this choice panel is scrollable, give the scrolled composite a
         * chance to determine its client area's dimensions.
         */
        if (scrolledComposite != null) {
            Display.getDefault().asyncExec(new Runnable() {
                @Override
                public void run() {
                    UiBuilder.updateScrolledAreaSize(scrolledComposite);
                }
            });
        }

        /*
         * Return the resulting choice panel.
         */
        return choicePanel;
    }

    /**
     * Update the specified size to be big enough to encompass the specified
     * composite, if it is not already big enough to do so.
     * 
     * @param size
     *            Size to be updated.
     * @param composite
     *            Composite to ensure that the size is large enough to hold.
     */
    private void updateSizeToEncompassComposite(Point size, Composite composite) {
        Point choicePanelSize = composite.computeSize(SWT.DEFAULT, SWT.DEFAULT);
        if (size.x < choicePanelSize.x) {
            size.x = choicePanelSize.x;
        }
        if (size.y < choicePanelSize.y) {
            size.y = choicePanelSize.y;
        }
    }

    /**
     * Update the main grid layout data to able to encompass the specified
     * needed dimensions.
     * 
     * @param neededDimensions
     *            Dimensions that the main grid layout data must be able to
     *            encompass.
     */
    private void updateMainGridLayoutData(Point neededDimensions) {
        mainGridData.widthHint = Math.max(headerWidthHint, neededDimensions.x
                + (groupBorderWidth * 2));
        mainGridData.heightHint = neededDimensions.y + groupBorderWidth
                + headerHeight;
    }

    /**
     * Update the requested size for the megawidget's UI component.
     */
    private void updateRequestedSize() {

        /*
         * Go through the choice detail panels, determining the greatest width
         * and the greatest height needed to display them all without scrolling.
         */
        Point neededDimensions = new Point(0, 0);
        Set<Composite> alreadyProcessed = new HashSet<>(
                choicePanelsForIdentifiers.size());
        for (Composite choicePanel : choicePanelsForIdentifiers.values()) {
            if (alreadyProcessed.contains(choicePanel)) {
                continue;
            }
            alreadyProcessed.add(choicePanel);
            updateSizeToEncompassComposite(neededDimensions, choicePanel);
        }

        /*
         * Ensure that the main panel is large enough.
         */
        updateMainGridLayoutData(neededDimensions);

    }

    /**
     * Handle the combo box's selection having changed to that specified.
     * 
     * @param value
     *            New combo box selection.
     */
    private void handleSelectionChange(String value) {

        /*
         * If allowing new choices, see if a new entry was added, and if so,
         * generate an identifier for it, associate it with the new choices
         * detail panel, and notify the state validator.
         */
        if (((DetailedComboBoxSpecifier) getSpecifier())
                .isAllowNewChoiceEnabled()) {
            if (choiceIdentifiersForNames.keySet().contains(value) == false) {

                /*
                 * Generate an identifier for the new choice, and use it to
                 * record the choice name and to associate it with the new
                 * choice detail panel.
                 */
                String identifier = getUniqueIdentifierForNewName(value,
                        choiceIdentifiersForNames.values());
                choiceIdentifiersForNames.put(value, identifier);
                choiceNamesForIdentifiers.put(identifier, value);
                choicePanelsForIdentifiers
                        .put(identifier, newChoiceDetailPanel);

                /*
                 * If the new choice detail panel has never been associated with
                 * an identifier until now, associate it with this new choice.
                 */
                if (choiceIdentifiersForPanels
                        .containsKey(newChoiceDetailPanel) == false) {
                    choiceIdentifiersForPanels.put(newChoiceDetailPanel,
                            identifier);
                }

                /*
                 * If the new choice detail panel is scrollable, record its
                 * association with the identifier for scrolling purposes.
                 */
                if (newChoiceDetailPanel instanceof ScrolledComposite) {
                    choiceScrolledCompositesForIdentifiers.put(identifier,
                            (ScrolledComposite) newChoiceDetailPanel);
                }

                /*
                 * Tell the validator about the change.
                 */
                try {
                    getStateValidator()
                            .setAvailableChoices(
                                    new ArrayList<>(choiceIdentifiersForNames
                                            .values()));
                } catch (MegawidgetPropertyException e) {
                    throw new IllegalStateException(
                            "adding new choice caused internal error", e);
                }
            }
        }

        /*
         * Set the state to the new selection, ensure the details panel is
         * showing the right detail components, and notify the listener.
         */
        state = choiceIdentifiersForNames.get(value);
        synchronizeDetailsPanelWithState();
        notifyListener(getSpecifier().getIdentifier(), state);
    }

    /**
     * Throw an unsupported operation exception indicating that choices cannot
     * be changed.
     * 
     * @throws UnsupportedOperationException
     *             Whenever this method is invoked.
     */
    private void throwChoicesChangeException()
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "cannot change choices for radio buttons megawidget");
    }

    /**
     * Change the component widgets to ensure their state matches that of the
     * editable flag.
     * 
     * @param editable
     *            Flag indicating whether the component widgets are to be
     *            editable or read-only.
     */
    private void doSetEditable(boolean editable) {
        comboBoxHelper.setEditable(editable);
    }

    /**
     * Synchronize the details panel with the current state.
     */
    private void synchronizeDetailsPanelWithState() {
        Composite choicePanel = choicePanelsForIdentifiers.get(state);
        if (detailsLayout.topControl != choicePanel) {
            detailsLayout.topControl = choicePanel;
            detailsPanel.layout();
            if (choicePanel instanceof ScrolledComposite) {
                Point origin = displaySettings
                        .getScrollOriginForPage(choiceIdentifiersForPanels
                                .get(choicePanel));
                if (origin != null) {
                    ((ScrolledComposite) choicePanel).setOrigin(origin);
                }
            }
        }
    }
}
