/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.toolbar;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

/**
 * Action consisting of only a drop-down menu that holds other actions of
 * generic type <code>P</code>. It is used together with a
 * {@link ActionAndMenuAction} of the type specified by the generic parameter
 * <code>A</code>; when one of the actions is chosen from its drop-down menu,
 * that action is assigned as the principal of the associated instance of said
 * class, as well as being run. The generic parameter <code>H</code> is the
 * subclass's type.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Jan 03, 2018   33428    Chris.Golden  Initial creation.
 * Mar 22, 2018   15561    Chris.Golden  Refactored to make the drop-down
 *                                       provider be considered a helper,
 *                                       rather than the main-button-providing
 *                                       class, and to allow the helper and
 *                                       main button provider to communicate
 *                                       amongst themselves without needing
 *                                       help from code using them.
 * </pre>
 * 
 * @author Chris.Golden
 */
public abstract class ActionAndMenuHelperAction<A extends ActionAndMenuAction<A, H, P>, H extends ActionAndMenuHelperAction<A, H, P>, P extends BasicAction>
        extends BasicAction implements IPulldownArrowButtonOnly, IMenuCreator {

    // Private Classes

    /**
     * Menu item wrapper for action choices supplied at creation time; these are
     * placed in them drop-down menu.
     */
    private class ActionWrapperForMenuItem extends BasicAction {

        // Private Variables

        /**
         * Action that is wrapped.
         */
        private final P principal;

        // Public Constructors

        /**
         * Construct a standard instance.
         * 
         * @param principal
         *            Action to be wrapped.
         */
        public ActionWrapperForMenuItem(final P principal) {
            super(principal.getText(), AS_PUSH_BUTTON,
                    principal.getImageDescriptor(), principal.getToolTipText());
            setEnabled(principal.isEnabled());
            this.principal = principal;
            this.principal
                    .addPropertyChangeListener(new IPropertyChangeListener() {

                        @Override
                        public void propertyChange(PropertyChangeEvent event) {
                            String propertyName = event.getProperty();
                            if (propertyName.equals(ENABLED)) {
                                if (isEnabled() != principal.isEnabled()) {
                                    setEnabled(principal.isEnabled());
                                }
                            } else if (propertyName.equals(IMAGE)) {
                                if (((getImageDescriptor() == null)
                                        && (principal
                                                .getImageDescriptor() != null))
                                        || ((getImageDescriptor() != null)
                                                && (getImageDescriptor()
                                                        .equals(principal
                                                                .getImageDescriptor()) == false))) {
                                    setImageDescriptor(
                                            principal.getImageDescriptor());
                                }
                            } else if (propertyName.equals(TEXT)) {
                                if (((getText() == null)
                                        && (principal.getText() != null))
                                        || ((getText() != null)
                                                && (getText().equals(principal
                                                        .getText()) == false))) {
                                    setText(principal.getText());
                                }
                            } else if (propertyName.equals(TOOL_TIP_TEXT)) {
                                if (((getToolTipText() == null)
                                        && (principal.getToolTipText() != null))
                                        || ((getToolTipText() != null)
                                                && (getToolTipText()
                                                        .equals(principal
                                                                .getToolTipText()) == false))) {
                                    setToolTipText(principal.getToolTipText());
                                }
                            }
                        }
                    });
        }

        // Public Methods

        @Override
        public void run() {
            if (principal.getStyle() == AS_CHECK_BOX) {
                principal.setChecked(!principal.isChecked());
            } else if (principal.getStyle() == AS_RADIO_BUTTON) {
                principal.setChecked(true);
            }
            mainAction.setPrincipal(principal);
            principal.run();
        }
    }

    // Protected Static Constants

    /**
     * Set of all possible allowable styles.
     */
    protected static Set<Integer> ALL_ALLOWABLE_STYLES = ImmutableSet.of(
            IAction.AS_CHECK_BOX, IAction.AS_PUSH_BUTTON,
            IAction.AS_RADIO_BUTTON);

    // Private Variables

    /**
     * List of actions to be shown in the menu, one of which must always be the
     * action displayed for this action when the menu is collapsed.
     */
    private final List<P> actionChoices;

    /**
     * Action for which this action is to act as a helper. When an action is
     * chosen from this chooser's menu, it becomes the principal of said action.
     */
    private ActionAndMenuAction<A, H, P> mainAction;

    /**
     * Menu that drops down.
     */
    private Menu menu = null;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param actionChoices
     *            List of actions that are the choices for the drop-down menu.
     *            Note that all the specified actions must have the same style,
     *            and that style must be one of those specified within
     *            <code>allowableStyles</code>.
     * @param allowableStyles
     *            Set of allowable styles; must contain one or more of the
     *            following: {@link IAction#AS_PUSH_BUTTON},
     *            {@link IAction#AS_CHECK_BOX}, or
     *            {@link IAction#AS_RADIO_BUTTON}.
     */
    public ActionAndMenuHelperAction(List<? extends P> actionChoices,
            Set<Integer> allowableStyles) {
        super(null, Action.AS_DROP_DOWN_MENU, null, null);
        if (Sets.intersection(ALL_ALLOWABLE_STYLES, allowableStyles)
                .isEmpty()) {
            throw new IllegalArgumentException(
                    "allowable styles must contain at least one of the following: "
                            + "AS_CHECK_BOX, AS_PUSH_BUTTON, or AS_RADIO_BUTTON");
        }
        int style = -1;
        for (BasicAction action : actionChoices) {
            if (style == -1) {
                style = action.getStyle();
                if (allowableStyles.contains(style) == false) {
                    throw new IllegalArgumentException(
                            "at least one action choice has an illegal style");
                }
            } else if (style != action.getStyle()) {
                throw new IllegalArgumentException(
                        "action choices do not all have same style");
            }
        }
        this.actionChoices = ImmutableList.copyOf(actionChoices);
        setMenuCreator(this);
    }

    // Public Methods

    @Override
    public final Menu getMenu(Control parent) {
        if (menu == null) {
            menu = new Menu(parent);
            for (P action : actionChoices) {
                new ActionContributionItem(new ActionWrapperForMenuItem(action))
                        .fill(menu, -1);
            }
        }
        return menu;
    }

    @Override
    public final Menu getMenu(Menu parent) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void dispose() {
        if (menu != null) {
            for (MenuItem item : menu.getItems()) {
                if (item.getImage() != null) {
                    item.getImage().dispose();
                }
            }
            menu.dispose();
        }
    }

    @Override
    public void run() {

        /*
         * No action.
         */
    }

    // Package-Private Methods

    /**
     * Set the action and menu action that is to be using this helper.
     * 
     * @param actionAndMenuAction
     *            Action that this action is to be helping.
     */
    void setActionAndMenuAction(
            ActionAndMenuAction<A, H, P> actionAndMenuAction) {
        this.mainAction = actionAndMenuAction;
    }
}
