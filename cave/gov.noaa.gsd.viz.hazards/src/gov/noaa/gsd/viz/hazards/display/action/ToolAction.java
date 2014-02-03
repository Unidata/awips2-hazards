/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display.action;

import gov.noaa.gsd.viz.hazards.jsonutilities.Dict;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;

/**
 * Tool action, generated by <code>ToolDialog</code>. The tool dialog is
 * generated from "dialog JSON" associated with tools in the data transformation
 * framework. The tool dialog provides user-input required to run a tool.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan, 2012               Chris.Golden      Initial induction into repo
 * 3/22/13                 Bryon.Lawrence    Added support for asynchronous results
 *                                           from recommenders and product generators.
 *                                           Added enum for action types.
 * Aug 21, 2013 1921       daniel.s.schaffer@noaa.gov  Call recommender framework directly
 * Dex 13, 2013 2266       jsanchez          Added ProductInformation and GeneratedProductList
 * </pre>
 * 
 * @author Chris Golden
 */
public class ToolAction {

    /**
     * Types of actions supported by ToolAction.
     */
    public enum ToolActionEnum {
        RUN_TOOL_WITH_PARAMETERS, RUN_TOOL, TOOL_RECOMMENDATIONS
    };

    // Private Variables

    /**
     * Identifier of the action that occurred.
     */
    private ToolActionEnum actionType;

    private String toolName;

    /**
     * Auxiliary details text, if any.
     * 
     * TODO This should probably be replaced with a Map<String, Serializable> or
     * modify {@link Dict} to extend that map.
     */
    private Dict aux;

    /**
     * List of recommender generated events, if any.
     */
    private EventSet<IEvent> recommendedEventList;

    /**
     * List of generated products, if any.
     */
    private GeneratedProductList productList;

    private String productGeneratorName;

    // Public Constructors

    /**
     * Construct a standard instance with no additional details.
     * 
     * @param actionType
     *            Identifier of the action that is occurring.
     */
    public ToolAction(ToolActionEnum actionType) {
        this(actionType, null);
    }

    /**
     * Construct a standard instance.
     * 
     * @param action
     *            Identifier of the action that is occurring.
     * @param toolName
     * 
     */
    public ToolAction(ToolActionEnum action, String toolName) {
        this.actionType = action;
        this.toolName = toolName;
    }

    /**
     * Construct a standard instance.
     * 
     * @param actionType
     *            Identifier of the action that is occurring.
     * @param toolName
     * 
     * @param aux
     *            Optional auxiliary details.
     */
    public ToolAction(ToolActionEnum actionType, String toolName, Dict aux) {
        this.actionType = actionType;
        this.toolName = toolName;
        this.aux = aux;
    }

    /**
     * Construct an action instance for handling recommender results.
     * 
     * @param actionType
     *            Identifier of the action that is occurring.
     * @param eventList
     *            List of recommended events
     * @param toolName
     *            Name of the tool which generated these events
     */

    public ToolAction(ToolActionEnum actionType, EventSet<IEvent> eventList,
            String toolName) {
        this.actionType = actionType;
        this.recommendedEventList = eventList;
        this.toolName = toolName;
    }

    /**
     * Construct an action instance for handling product generator results.
     * 
     * @param actionType
     *            Identifier of the action that is occurring.
     * @param productList
     *            List of recommended events
     * @param productGeneratorName
     *            Name of the tool which generated these events
     */
    public ToolAction(ToolActionEnum actionType, String productGeneratorName,
            GeneratedProductList productList) {
        this.actionType = actionType;
        this.productList = productList;
        this.productGeneratorName = productGeneratorName;
    }

    // Public Methods

    /**
     * Get the action identifier.
     * 
     * @return Action identifier.
     */
    public ToolActionEnum getActionType() {
        return actionType;
    }

    /**
     * Set the action identifier as specified.
     * 
     * @param actionType
     *            New action identifier.
     */
    public void setActionType(ToolActionEnum actionType) {
        this.actionType = actionType;
    }

    public String getToolName() {
        return toolName;
    }

    /**
     * Get the auxiliary details.
     * 
     * @return auxiliary details.
     */
    public Dict getAuxiliaryDetails() {
        return aux;
    }

    /**
     * @param recommendedEventList
     *            the recommended event list to set
     */
    public void setRecommendedEventList(EventSet<IEvent> recommendedEventList) {
        this.recommendedEventList = recommendedEventList;
    }

    /**
     * @return the recommended event list
     */
    public EventSet<IEvent> getRecommendedEventList() {
        return recommendedEventList;
    }

    /**
     * @param productList
     *            the generated product list to set
     */
    public void setProductList(GeneratedProductList productList) {
        this.productList = productList;
    }

    /**
     * @return the generated product list
     */
    public GeneratedProductList getProductList() {
        return productList;
    }

    /**
     * @return the productGeneratorName
     */
    public String getProductGeneratorName() {
        return productGeneratorName;
    }
}
