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

import java.io.Serializable;
import java.util.Map;

import com.raytheon.uf.common.dataplugin.events.EventSet;
import com.raytheon.uf.common.dataplugin.events.IEvent;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Tool;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ToolType;

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
 * Dec 13, 2013 2266       jsanchez          Added ProductInformation and GeneratedProductList
 * Jan 29, 2015 3626       Chris.Golden      Added ability to pass event type when running
 *                                           a recommender.
 * Jan 29, 2015 4375       Dan Schaffer      Console initiation of RVS product generation
 * Feb 05, 2015 2331       Chris.Golden      Removed unused and ideologically suspect method.
 * Feb 15, 2015 2271       Dan Schaffer      Incur recommender/product generator init costs immediately
 * </pre>
 * 
 * @author Chris Golden
 */
public class ToolAction {

    /**
     * Recommender actions
     */
    public enum RecommenderActionEnum {
        RUN_RECOMMENDER_WITH_PARAMETERS, RUN_RECOMENDER, RECOMMENDATIONS
    };

    // Private Variables

    /**
     * Identifier of the action that occurred.
     */
    private RecommenderActionEnum recommenderActionType;

    private final Tool tool;

    private String eventType;

    /**
     * Auxiliary details text, if any.
     * 
     */
    private Map<String, Serializable> auxiliaryDetails;

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
     * Construct a standard instance.
     * 
     * 
     * @param recommenderActionType
     *            Type of the recommender action that is occurring.
     * @param tool
     * @param eventType
     * 
     */
    public ToolAction(RecommenderActionEnum recommenderActionType, Tool tool,
            String eventType) {
        this.recommenderActionType = recommenderActionType;
        this.tool = tool;
        this.eventType = eventType;
    }

    /**
     * 
     * 
     * @param recommenderActionType
     *            Type of the recommender action that is occurring.
     * @param tool
     * 
     */
    public ToolAction(RecommenderActionEnum recommenderActionType, Tool tool) {
        this.recommenderActionType = recommenderActionType;
        this.tool = tool;
        this.eventType = "";
    }

    /**
     * Construct a standard instance.
     * 
     * @param recommenderActionType
     *            Identifier of the action that is occurring.
     * @param tool
     * 
     * @param auxiliaryDetails
     *            Optional auxiliary details.
     * @param eventType
     */
    public ToolAction(RecommenderActionEnum recommenderActionType, Tool tool,
            Map<String, Serializable> auxiliarlyDetails, String eventType) {
        this.recommenderActionType = recommenderActionType;
        this.tool = tool;
        this.auxiliaryDetails = auxiliarlyDetails;
        this.eventType = eventType;
    }

    /**
     * Construct an action instance for handling recommender results.
     * 
     * @param actionType
     *            Identifier of the action that is occurring.
     * @param eventList
     *            List of recommended events
     * @param tool
     *            The tool which generated these events
     */

    public ToolAction(RecommenderActionEnum actionType,
            EventSet<IEvent> eventList, Tool tool) {
        this.recommenderActionType = actionType;
        this.recommendedEventList = eventList;
        this.tool = tool;
    }

    // Public Methods

    /**
     * Get the action identifier.
     * 
     * @return Action identifier.
     */
    public RecommenderActionEnum getRecommenderActionType() {
        return recommenderActionType;
    }

    /**
     * Set the recommender action as specified.
     * 
     * @param recommenderActionType
     *            New recommender action.
     */
    public void setActionType(RecommenderActionEnum recommenderActionType) {
        this.recommenderActionType = recommenderActionType;
    }

    public Tool getTool() {
        return tool;
    }

    public String getToolName() {
        return tool.getToolName();
    }

    public String getEventType() {
        return eventType;
    }

    /**
     * Get the auxiliary details.
     * 
     * @return auxiliary details.
     */
    public Map<String, Serializable> getAuxiliaryDetails() {
        return auxiliaryDetails;
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

    /**
     * @return the toolType
     */
    public ToolType getToolType() {
        return tool.getToolType();
    }
}
