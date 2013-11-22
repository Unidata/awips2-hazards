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

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Queue;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Queues;

/**
 * Description: Manager of detail field megawidgets that are children of a
 * bounded choices megawidget. It creates and keeps track of such child
 * megawidgets so that the bounded choices megawidget may delegate this work.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 25, 2013    2168    Chris.Golden      Initial creation.
 * Oct 31, 2013    2336    Chris.Golden      Changed to accommodate alteration
 *                                           of framework to include notion
 *                                           of bounded (closed set) choices
 *                                           versus unbounded (sets to which
 *                                           arbitrary user-specified choices
 *                                           can be added) choice megawidgets.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see BoundedChoicesMegawidget
 */
public class BoundedChoicesDetailChildrenManager implements
        INotificationListener, IStateChangeListener {

    // Private Variables

    /**
     * List of all detail child megawidgets that have been created.
     */
    private final List<IControl> detailMegawidgets = Lists.newArrayList();

    /**
     * Map of identifiers of detail child megawidgets that are notifiers to the
     * choice buttons with which they are associated.
     */
    private final Map<String, Button> choiceButtonsForDetailIdentifiers = Maps
            .newHashMap();

    /**
     * Map of choice buttons to the listeners used to receive notification of
     * the buttons' selection.
     */
    private final SelectionListener choiceButtonListener;

    /**
     * Map of creation-time attribute identifiers to corresponding values; this
     * is passed to each megawidget at construction time.
     */
    private final Map<String, Object> creationParams;

    /**
     * Notification listener to which to pass any notifications intercepted from
     * the detail child megawidgets.
     */
    private final INotificationListener notificationListener;

    /**
     * State change listener to which to pass any state change notifications
     * intercepted from the detail child megawidgets.
     */
    private final IStateChangeListener stateChangeListener;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param choiceButtonListener
     *            Selection listener that receives notifications of the of any
     *            choice buttons' selection.
     * @param creationParams
     *            Map of creation-time attribute identifiers to corresponding
     *            values; this is passed to each megawidget at construction
     *            time.
     */
    public BoundedChoicesDetailChildrenManager(
            SelectionListener choiceButtonListener,
            Map<String, Object> creationParams) {
        this.choiceButtonListener = choiceButtonListener;
        this.creationParams = Maps.newHashMap(creationParams);
        notificationListener = (INotificationListener) creationParams
                .get(INotifier.NOTIFICATION_LISTENER);
        this.creationParams.put(INotifier.NOTIFICATION_LISTENER, this);
        stateChangeListener = (IStateChangeListener) creationParams
                .get(IStateful.STATE_CHANGE_LISTENER);
        this.creationParams.put(IStateful.STATE_CHANGE_LISTENER, this);
    }

    // Public Methods

    /**
     * Get the list of all detail child megawidgets.
     * 
     * @return List of all detail child megawidgets.
     */
    public List<IControl> getDetailMegawidgets() {
        return Lists.newArrayList(detailMegawidgets);
    }

    /**
     * Create the GUI components making up the specified choice identifier's
     * detail child megawidgets, placing them in the provided composite.
     * 
     * @param button
     *            Button used to select this choice.
     * @param adjacentComposite
     *            Composite into which to place the child megawidgets' GUI
     *            representations for those children that are to be adjacent to
     *            the choice button.
     * @param overallComposite
     *            Composite into which to place the child megawidgets' GUI
     *            representations for those children that are to be in
     *            subsequent rows under that of the choice button, if any. Note
     *            that this composite holds <code>adjacentComposite</code>.
     * @param buttonWidth
     *            Width in pixels of the choice button, used so as to allow
     *            subsequent rows of child megawidgets, if any, to have the
     *            correct indentation.
     * @param detailSpecifiers
     *            List of the choice's detail child megawidget specifiers.
     * @return List of any composites that were created to hold megawidgets on
     *         rows below the row in which the choice button is found.
     * @throws MegawidgetException
     *             If an error occurs while creating or initializing the
     *             associated megawidgets.
     */
    public List<Control> createDetailChildMegawidgetsForChoice(Button button,
            Composite adjacentComposite, Composite overallComposite,
            int buttonWidth, List<IControlSpecifier> detailSpecifiers)
            throws MegawidgetException {

        // If there are no child megawidgets, do nothing.
        if (detailSpecifiers.isEmpty()) {
            return Collections.emptyList();
        }

        // Determine how many rows of megawidgets are specified, and how
        // many specifiers will be on each row. Specifiers that exist
        // on rows after the first will be placed within a composite, one
        // per row.
        Queue<Integer> specifierCountPerRow = Queues.newLinkedBlockingQueue();
        int specifierCount = 0;
        for (int j = 0; j < detailSpecifiers.size(); j++) {
            if (detailSpecifiers.get(j).isFullWidthOfColumn()) {
                if ((specifierCount > 0) || specifierCountPerRow.isEmpty()) {
                    specifierCountPerRow.add(specifierCount);
                    specifierCount = 0;
                }
                specifierCountPerRow.add(1);
            } else {
                specifierCount++;
            }
        }
        if (specifierCount > 0) {
            specifierCountPerRow.add(specifierCount);
        }

        // Iterate through the detail megawidget specifiers, creating each
        // in turn, ensuring that each is nested in the proper composite.
        // First row ones are simply placed in the provided adjacent
        // composite, while any that belong in subsequent rows have com-
        // posites made for each such row and are placed in those.
        List<Control> subsequentRowComposites = Lists.newArrayList();
        Composite composite = adjacentComposite;
        specifierCount = specifierCountPerRow.remove();
        int index = 0;
        for (IControlSpecifier detailSpecifier : detailSpecifiers) {

            // If this megawidget belongs in a new row, create a new
            // composite in which to place the megawidget and use it
            // for this row.
            if ((index++ == specifierCount)
                    && (specifierCountPerRow.isEmpty() == false)) {
                specifierCount = specifierCountPerRow.remove();
                composite = new Composite(overallComposite, SWT.NONE);
                GridLayout layout = new GridLayout(specifierCount, false);
                layout.horizontalSpacing = 3;
                layout.marginWidth = layout.marginHeight = 0;
                layout.marginLeft = buttonWidth;
                composite.setLayout(layout);
                composite.setLayoutData(new GridData(
                        (specifierCount > 1 ? SWT.LEFT : SWT.FILL), SWT.TOP,
                        true, false));
                subsequentRowComposites.add(composite);
                index = 1;
            }

            // Create the megawidget.
            IControl megawidget = detailSpecifier.createMegawidget(composite,
                    IControl.class, creationParams);
            detailMegawidgets.add(megawidget);
            rememberChildMegawidgetsAssociationWithChoiceButton(megawidget,
                    button);
        }

        // Return any row composites that were created.
        return subsequentRowComposites;
    }

    @Override
    public void megawidgetInvoked(INotifier megawidget, String extraCallback) {

        // If the invoked megawidget is not stateful, first fire
        // off a selection event for the choice button associated
        // with this detail child megawidget. Stateful megawidgets
        // do not cause a selection event because the latter has
        // already been fired off when they experienced the state
        // change that preceded this notification.
        if ((megawidget instanceof IStateful) == false) {
            fireSelectionEventForDetailMegawidget(megawidget);
        }

        // Notify the real notification listener of the invocation.
        notificationListener.megawidgetInvoked(megawidget, extraCallback);
    }

    @Override
    public void megawidgetStateChanged(IStateful megawidget, String identifier,
            Object state) {

        // Fire off a selection event for the choice button asso-
        // ciated with this detail child megawidget so that the
        // button's choice is selected. Only after this has been
        // done is the state change forwarded onto the real
        // listener.
        fireSelectionEventForDetailMegawidget(megawidget);
        stateChangeListener.megawidgetStateChanged(megawidget, identifier,
                state);
    }

    // Private Methods

    /**
     * Remember the association of the specified megawidget (and any child
     * megawidgets of that megawidget) with the specified choice button.
     * 
     * @param megawidget
     *            Megawidget to be associated (along with any children it has)
     *            with the choice button.
     * @param button
     *            Choice button.
     */
    @SuppressWarnings("unchecked")
    private void rememberChildMegawidgetsAssociationWithChoiceButton(
            IControl megawidget, Button button) {
        if (megawidget instanceof INotifier) {
            choiceButtonsForDetailIdentifiers.put(megawidget.getSpecifier()
                    .getIdentifier(), button);
        } else if (megawidget instanceof IParent) {
            for (IControl child : ((IParent<IControl>) megawidget)
                    .getChildren()) {
                rememberChildMegawidgetsAssociationWithChoiceButton(child,
                        button);
            }
        }
    }

    /**
     * Fire off a selection event for the choice button associated with the
     * specified detail megawidget, if the choice button was not selected
     * already.
     * 
     * @param megawidget
     *            Megawidget that sent a notification that triggered this
     *            selection event firing.
     */
    private void fireSelectionEventForDetailMegawidget(IMegawidget megawidget) {
        Button choiceButton = choiceButtonsForDetailIdentifiers.get(megawidget
                .getSpecifier().getIdentifier());
        if (choiceButton.getSelection() == false) {
            choiceButton.setSelection(true);
            Event baseEvent = new Event();
            baseEvent.widget = choiceButton;
            SelectionEvent event = new SelectionEvent(baseEvent);
            choiceButtonListener.widgetSelected(event);
        }
    }
}
