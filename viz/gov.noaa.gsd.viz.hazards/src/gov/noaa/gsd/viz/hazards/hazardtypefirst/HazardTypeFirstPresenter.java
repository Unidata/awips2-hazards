/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.hazardtypefirst;

import gov.noaa.gsd.common.eventbus.BoundedReceptionEventBus;
import gov.noaa.gsd.viz.hazards.display.HazardServicesPresenter;
import gov.noaa.gsd.viz.hazards.display.action.ToolAction;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvocationHandler;
import gov.noaa.gsd.viz.mvp.widgets.ICommandInvoker;
import gov.noaa.gsd.viz.mvp.widgets.IStateChangeHandler;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.engio.mbassy.listener.Handler;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.Element;
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.RecommenderTriggerOrigin;
import com.raytheon.uf.viz.hazards.sessionmanager.ISessionManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.ISessionConfigurationManager;
import com.raytheon.uf.viz.hazards.sessionmanager.config.SettingsModified;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Choice;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.HazardInfoConfig;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ISettings;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ToolType;
import com.raytheon.uf.viz.hazards.sessionmanager.events.impl.ObservedHazardEvent;
import com.raytheon.uf.viz.hazards.sessionmanager.originator.IOriginator;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.RecommenderExecutionContext;

/**
 * Description: Presenter for the hazard-type-first dialog.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Jan 21, 2015    3626    Chris.Golden Initial creation.
 * Jan 29, 2015    3626    Chris.Golden Added the passing of the event type
 *                                      chosen to a recommender that is being
 *                                      run as a result of that choice.
 * Jan 29, 2015    4375    Dan Schaffer Console initiation of RVS product generation
 * Nov 10, 2015    2762    Chris.Golden Added support for use of new recommender
 *                                      manager.
 * Mar 16, 2016   15676    Chris.Golden Changed to work with new recommender execution
 *                                      context.
 * Feb 01, 2017   15556    Chris.Golden Changed to work with finer-grained settings
 *                                      change messages.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class HazardTypeFirstPresenter extends
        HazardServicesPresenter<IHazardTypeFirstViewDelegate<?, ?>> implements
        IOriginator {

    // Package-Private Enumerated Types

    /**
     * Commands that may be invoked within the dialog.
     */
    public enum Command {
        OK, CANCEL
    };

    // Private Static Constants

    /**
     * String to be displayed as a blank type choice.
     */
    private static final String BLANK_TYPE_CHOICE = "";

    // Private Variables

    /**
     * List of hazard categories for which there exist types that have
     * associated hazard-type-first recommenders.
     */
    private final ImmutableList<String> categories;

    /**
     * Map of each of the hazard categories found within {@link #categories} to
     * lists of the types that fall within those categories. Only types that
     * have associated hazard-type-first recommenders are included.
     */
    private final ImmutableMap<String, ImmutableList<String>> typesForCategories;

    /**
     * Map of each of the hazard categories found within {@link #categories} to
     * lists of the descriptions of the types that fall within those categories.
     * Only types that have associated hazard-type-first recommenders have
     * descriptions included. Note that for any list that is a value within this
     * map, the description at a given index describes the type at the
     * corresponding index within the list associated with the same category
     * within {@link #typesForCategories}.
     */
    private final ImmutableMap<String, ImmutableList<String>> typeDescriptionsForCategories;

    /**
     * Currently selected category.
     */
    private String selectedCategory = null;

    /**
     * Last recorded default category, from the settings.
     */
    private String lastDefaultCategory = null;

    /**
     * Currently selected type.
     */
    private String selectedType = BLANK_TYPE_CHOICE;

    /**
     * Category state change handler. The identifier is ignored.
     */
    private final IStateChangeHandler<Object, String> categoryChangeHandler = new IStateChangeHandler<Object, String>() {

        @Override
        public void stateChanged(Object identifier, String value) {
            selectedCategory = value;
            selectedType = BLANK_TYPE_CHOICE;
            getView().getTypeChanger()
                    .setChoices(null, typesForCategories.get(value),
                            typeDescriptionsForCategories.get(value),
                            BLANK_TYPE_CHOICE);
            updateOkCommandEnabledState();
        }

        @Override
        public void statesChanged(Map<Object, String> valuesForIdentifiers) {
            handleUnsupportedOperationAttempt("category");
        }
    };

    /**
     * Type state change handler. The identifier is ignored.
     */
    private final IStateChangeHandler<Object, String> typeChangeHandler = new IStateChangeHandler<Object, String>() {

        @Override
        public void stateChanged(Object identifier, String value) {
            selectedType = value;
            updateOkCommandEnabledState();
            if (selectedType.equals(BLANK_TYPE_CHOICE) == false) {
                runRecommender();
                getView().hide();
            }
        }

        @Override
        public void statesChanged(Map<Object, String> valuesForIdentifiers) {
            handleUnsupportedOperationAttempt("type");
        }
    };

    /**
     * Show dialog invocation handler. The identifier is ignored.
     */
    private final ICommandInvocationHandler<Object> showDialogInvocationHandler = new ICommandInvocationHandler<Object>() {
        @Override
        public void commandInvoked(Object identifier) {

            /*
             * Show the dialog with the specified starting choices and
             * selections.
             */
            getView().show(selectedCategory,
                    typesForCategories.get(selectedCategory),
                    typeDescriptionsForCategories.get(selectedCategory),
                    selectedType);

            /*
             * Register the various handlers with the view, and make sure that
             * the OK command is only enabled if a legitimate type is currently
             * selected.
             */
            getView().getCommandInvoker().setCommandInvocationHandler(
                    commandInvocationHandler);
            updateOkCommandEnabledState();
            getView().getCategoryChanger().setStateChangeHandler(
                    categoryChangeHandler);
            getView().getTypeChanger().setStateChangeHandler(typeChangeHandler);
        }
    };

    /**
     * Dialog command invocation handler.
     */
    private final ICommandInvocationHandler<Command> commandInvocationHandler = new ICommandInvocationHandler<Command>() {
        @Override
        public void commandInvoked(Command identifier) {
            switch (identifier) {
            case OK:

                /*
                 * Run the recommender for the type that is currently selected,
                 * then fall through to hide the dialog.
                 */
                runRecommender();
            case CANCEL:
                getView().hide();
            }
        }
    };

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param model
     *            Model to be handled by this presenter.
     * @param eventBus
     *            Event bus used to signal changes.
     */
    public HazardTypeFirstPresenter(
            ISessionManager<ObservedHazardEvent, ObservedSettings> model,
            BoundedReceptionEventBus<Object> eventBus) {
        super(model, eventBus);

        /*
         * Compile the list of categories and the lists of associated types that
         * themselves have associated hazard-type-first recommenders.
         */
        ISessionConfigurationManager<? extends ISettings> configManager = model
                .getConfigurationManager();
        HazardInfoConfig categoriesAndTypes = configManager
                .getHazardInfoConfig();
        List<String> categories = new ArrayList<>();
        Map<String, ImmutableList<String>> typesForCategories = new HashMap<>();
        Map<String, ImmutableList<String>> typeDescriptionsForCategories = new HashMap<>();
        for (Choice categoryAndTypes : categoriesAndTypes.getHazardCategories()) {
            List<Choice> typeChoices = categoryAndTypes.getChildren();
            List<String> types = new ArrayList<String>(typeChoices.size());
            List<String> typeDescriptions = new ArrayList<String>(
                    typeChoices.size());
            for (Choice hazardType : categoryAndTypes.getChildren()) {
                if (configManager.getTypeFirstRecommender(hazardType
                        .getIdentifier()) != null) {
                    types.add(hazardType.getIdentifier());
                    typeDescriptions.add(hazardType.getDisplayString());
                }
            }
            if (types.isEmpty() == false) {
                types.add(0, BLANK_TYPE_CHOICE);
                typeDescriptions.add(0, BLANK_TYPE_CHOICE);
                categories.add(categoryAndTypes.getDisplayString());
                typesForCategories.put(categoryAndTypes.getDisplayString(),
                        ImmutableList.copyOf(types));
                typeDescriptionsForCategories.put(
                        categoryAndTypes.getDisplayString(),
                        ImmutableList.copyOf(typeDescriptions));
            }
        }
        this.categories = ImmutableList.copyOf(categories);
        this.typesForCategories = ImmutableMap.copyOf(typesForCategories);
        this.typeDescriptionsForCategories = ImmutableMap
                .copyOf(typeDescriptionsForCategories);

        /*
         * Get the default category and type.
         */
        resetCategoryAndType();
    }

    // Public Methods

    @Override
    @Deprecated
    public void modelChanged(EnumSet<Element> changed) {

        /*
         * No action.
         */
    }

    /**
     * Respond to changes in the current settings by ensuring that the default
     * category is used when the dialog is shown.
     * 
     * @param change
     *            Change that occurred.
     */
    @Handler
    public void settingsModified(final SettingsModified change) {
        if (change.getChanged()
                .contains(ObservedSettings.Type.DEFAULT_CATEGORY)) {
            resetCategoryAndType();
        }
    }

    // Protected Methods

    @Override
    protected void initialize(IHazardTypeFirstViewDelegate<?, ?> view) {

        /*
         * Initialize the view, giving it the categories.
         */
        getView().initialize(categories);

        /*
         * Set the show-dialog invocation handler so that the presenter knows
         * when to tell the view to show the dialog, and enable this command
         * only if there is at least one category available with types that have
         * associated type-first recommenders.
         */
        ICommandInvoker<Object> showDialogInvoker = getView()
                .getShowDialogInvoker();
        showDialogInvoker
                .setCommandInvocationHandler(showDialogInvocationHandler);
        showDialogInvoker.setEnabled(null, !categories.isEmpty());
    }

    @Override
    protected void reinitialize(IHazardTypeFirstViewDelegate<?, ?> view) {

        /*
         * No action.
         */
    }

    // Private Methods

    /**
     * Set the category and type to default.
     * <p>
     * <strong>NOTE</strong>: It is assumed that this method will never be
     * invoked while the dialog is showing. This is because this dialog is
     * modal, and the settings should only change as the result of a user action
     * that would require this dialog to be dismissed first.
     */
    private void resetCategoryAndType() {

        /*
         * Determine the default category for the current setting; if that
         * category has no types with associated type-first recommenders, use
         * the first category that does as the default.
         */
        String defaultCategory = getModel().getConfigurationManager()
                .getSettings().getDefaultCategory();
        boolean defaultCategoryHasTypes = categories.contains(defaultCategory);
        if ((defaultCategoryHasTypes == false)
                && (categories.isEmpty() == false)) {
            defaultCategory = categories.get(0);
        }

        /*
         * If no category is currently selected, remember the default category.
         */
        if ((selectedCategory == null)
                || (defaultCategory.equals(lastDefaultCategory) == false)) {
            lastDefaultCategory = defaultCategory;

            /*
             * If the selected category is null or is not the same as the new
             * settings-specified default category, set it to be the same as the
             * default, and clear the selected type.
             */
            if ((selectedCategory == null)
                    || (defaultCategoryHasTypes && (selectedCategory
                            .equals(defaultCategory) == false))) {
                this.selectedCategory = defaultCategory;
                this.selectedType = BLANK_TYPE_CHOICE;
            }
        }
    }

    /**
     * Update the OK command's enabled state to ensure that it can only be
     * invoked if a proper type is currently selected.
     */
    private void updateOkCommandEnabledState() {
        getView().getCommandInvoker().setEnabled(Command.OK,
                !selectedType.equals(BLANK_TYPE_CHOICE));
    }

    /**
     * Run the recommender associated with the selected hazard type.
     */
    private void runRecommender() {

        /*
         * TODO: Currently, the business logic for running recommenders exist
         * within the HazardServicesMessageHandler. We do not have time to
         * extract said logic and place it in some more appropriate place, and
         * thus will use the deprecated publish() method to send a deprecated
         * notification to run the tool, which the message handler will receive
         * and deal with.
         * 
         * When the message handler is refactored into oblivion, there will be
         * either some sort of helper class for running recommenders, or the
         * session manager will handle it. At that time, this will be changed to
         * directly run the tool, instead of using this deprecated code.
         */
        publish(new ToolAction(ToolAction.RecommenderActionEnum.RUN_RECOMENDER,
                getModel().getConfigurationManager().getTypeFirstRecommender(
                        selectedType), ToolType.RECOMMENDER,
                RecommenderExecutionContext.getHazardTypeFirstContext(
                        selectedType, RecommenderTriggerOrigin.USER)));
    }

    /**
     * Throw an unsupported operation exception for attempts to change multiple
     * states that are not appropriate.
     * 
     * @param description
     *            Description of the element for which an attempt to change
     *            multiple states was made.
     * @throws UnsupportedOperationException
     *             Whenever this method is called.
     */
    private void handleUnsupportedOperationAttempt(String description)
            throws UnsupportedOperationException {
        throw new UnsupportedOperationException(
                "cannot change multiple states associated with hazard type first "
                        + description);
    }
}
