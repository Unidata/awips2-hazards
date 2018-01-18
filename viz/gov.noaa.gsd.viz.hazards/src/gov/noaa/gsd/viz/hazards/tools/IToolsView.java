/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.tools;

import java.util.List;

import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Tool;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ToolType;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.RecommenderExecutionContext;

import gov.noaa.gsd.viz.mvp.IView;

/**
 * Interface describing the methods required for implementing a tools view, used
 * by the user to view, execute, and manipulate tools.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Chris.Golden      Initial induction into repo
 * Jan 29, 2015    4375    Dan Schaffer      Console initiation of RVS product generation
 * Jan 30, 2015    3626    Chris.Golden      Added ability to pass event type when
 *                                           running a recommender.
 * Nov 10, 2015   12762    Chris.Golden      Added support for use of new recommender manager.
 * Aug 15, 2017   22757    Chris.Golden      Added ability for recommenders to specify either
 *                                           a message to display, or a dialog to display,
 *                                           with their results (that is, within the returned
 *                                           event set).
 * Sep 27, 2017   38072    Chris.Golden      Changed to work with new recommender manager.
 * Jan 17, 2018   33428    Chris.Golden      Changed to work with new, more flexible toolbar
 *                                           contribution code.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IToolsView<I, C, E extends Enum<E>> extends IView<I, C, E> {

    // Public Static Constants

    /**
     * Tools pulldown identifier.
     */
    public static final String TOOLS_PULLDOWN_IDENTIFIER = "toolsPulldown";

    /**
     * Event-driven tools toggle identifier.
     */
    public static final String EVENT_DRIVEN_TOOLS_TOGGLE_IDENTIFIER = "eventDrivenToolsToggle";

    // Public Methods

    /**
     * Initialize the view.
     * 
     * @param presenter
     *            Presenter managing this view.
     * @param tools
     *            - a List of Tool objects
     */
    public void initialize(ToolsPresenter presenter, List<Tool> tools);

    /**
     * Show a tool subview that is used to gather parameter values for a tool
     * that is to be executed.
     * <p>
     * TODO: Consider renaming this whole package and its classes from "ToolXXX"
     * to "RecommenderXXX", since everything here is used for recommenders only.
     * It doesn't make sense that a {@link RecommenderExecutionContext} is being
     * passed in if it's for tools, not specifically recommenders.
     * 
     * @param type
     *            Type of the tool.
     * @param jsonParams
     *            JSON string giving the parameters for this subview. Within the
     *            set of all fields that are defined by these parameters, all
     *            the fields (megawidget specifiers) must have unique
     *            identifiers.
     */
    public void showToolParameterGatherer(ToolType type, String jsonParams);

    /**
     * Show a tool subview that is used to display results for a tool that was
     * executed.
     * <p>
     * TODO: Consider renaming this whole package and its classes from "ToolXXX"
     * to "RecommenderXXX", since everything here is used for recommenders only.
     * It doesn't make sense that a {@link RecommenderExecutionContext} is being
     * passed in if it's for tools, not specifically recommenders.
     * 
     * @param type
     *            Type of the tool.
     * @param jsonParams
     *            JSON string giving the parameters for this subview. Within the
     *            set of all fields that are defined by these parameters, all
     *            the fields (megawidget specifiers) must have unique
     *            identifiers.
     */
    public void showToolResults(ToolType type, String jsonParams);

    /**
     * Set the tools to those specified.
     * 
     * @param tools
     *            List of tool objects.
     */
    public void setTools(List<Tool> tools);
}
