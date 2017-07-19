/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.toolbar;

import org.eclipse.jface.action.IContributionManager;
import org.eclipse.ui.IActionBars;

/**
 * Abstract class from which may be derived classes encapsulating toolbar combo
 * boxes.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo.
 * Jul 15, 2013      585   Chris.Golden      Added code to allow toolbar manager
 *                                           to be set after construction, so
 *                                           that this action may be created
 *                                           before the toolbar manager to which
 *                                           it will be assigned exists.
 * Oct 03, 2016    22299   Kevin.Bisanz      Implemented IActionBarsAware.
 * </pre>
 * 
 * @author Chris.Golden
 */
public abstract class ComboAction extends PulldownAction
        implements IContributionManagerAware, IActionBarsAware {

    // Private Static Constants

    /**
     * Placeholder text.
     */
    private static final String PLACEHOLDER_TEXT = "(none)";

    /**
     * Description-value separator text.
     */
    private static final String DESCRIPTION_VALUE_SEPARATOR_TEXT = ": ";

    /**
     * Placeholder tooltip text suffix.
     */
    private static final String PLACEHOLDER_TOOLTIP_TEXT_SUFFIX = DESCRIPTION_VALUE_SEPARATOR_TEXT
            + "(none)";

    // Private Variables

    /**
     * Description, to be used as a prefix for the tooltip text.
     */
    private final String description;

    /**
     * Contribution manager.
     */
    private IContributionManager contributionManager;

    /**
     * Action bar manager.
     */
    private IActionBars actionBars;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param description
     *            Description of this action.
     */
    public ComboAction(String description) {
        super(PLACEHOLDER_TEXT);
        this.description = description;
        setToolTipText(description + PLACEHOLDER_TOOLTIP_TEXT_SUFFIX);
    }

    // Public Methods

    @Override
    public void setContributionManager(
            IContributionManager contributionManager) {
        this.contributionManager = contributionManager;
    }

    /**
     * Set the visual state to indicate the specified choice is the current
     * choice.
     * 
     * @param choiceText
     *            Text of the choice to be shown as current.
     */
    public void setSelectedChoice(String choiceText) {

        /*
         * Set the text of the action to the newly selected choice, and force
         * the contribution manager and action bars to update; without the
         * forced updates, the drop-down widget disappears from the toolbar.
         */
        setText(choiceText);
        if (contributionManager != null) {
            contributionManager.update(true);
        }
        if (actionBars != null) {
            actionBars.updateActionBars();
        }

        /*
         * Set the tooltip text to include the choice.
         */
        setToolTipText(
                description + DESCRIPTION_VALUE_SEPARATOR_TEXT + choiceText);
    }

    @Override
    public void setActionBars(IActionBars actionBars) {
        this.actionBars = actionBars;
    }
}
