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

import gov.noaa.gsd.viz.mvp.IView;

import java.util.List;

import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Tool;

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
 * Jan 30, 2015    3626    Chris.Golden      Added ability to pass event type when
 *                                           running a recommender.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public interface IToolsView<C, E extends Enum<E>> extends IView<C, E> {

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
     * 
     * @param toolName
     *            Name of the tool for which parameters are to be gathered.
     * @param eventType
     *            The type of the event that this tool is to create; if present,
     *            the tool is being run as a result of a hazard-type-first
     *            invocation. Otherwise, it will be <code>null</code>.
     * @param jsonParams
     *            JSON string giving the parameters for this subview. Within the
     *            set of all fields that are defined by these parameters, all
     *            the fields (megawidget specifiers) must have unique
     *            identifiers.
     */
    public void showToolParameterGatherer(String toolName, String eventType,
            String jsonParams);

    /**
     * Set the tools to those specified.
     * 
     * @param tools
     *            List of tool objects.
     */
    public void setTools(List<Tool> tools);
}
