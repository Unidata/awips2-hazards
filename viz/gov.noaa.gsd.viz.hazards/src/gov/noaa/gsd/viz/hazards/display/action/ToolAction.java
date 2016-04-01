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
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.ToolType;
import com.raytheon.uf.viz.hazards.sessionmanager.recommenders.RecommenderExecutionContext;

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
 * Nov 10, 2015 12762      Chris.Golden      Added support for use of new recommender manager.
 * Apr 01, 2016 16225      Chris.Golden      Added ability to cancel tasks that are scheduled to run
 *                                           at regular intervals.
 * </pre>
 * 
 * @author Chris Golden
 */
public class ToolAction {

    /**
     * Recommender actions
     */
    public enum RecommenderActionEnum {
        RUN_RECOMMENDER_WITH_PARAMETERS, RUN_RECOMENDER, RECOMMENDATIONS, ENABLE_EVENT_DRIVEN_TOOLS
    };

    // Private Variables

    /**
     * Identifier of the action that occurred.
     */
    private RecommenderActionEnum recommenderActionType;

    private final boolean enable;

    private final String toolIdentifier;

    private final ToolType toolType;

    private final RecommenderExecutionContext context;

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
    public ToolAction(RecommenderActionEnum recommenderActionType,
            String toolIdentifier, ToolType toolType,
            RecommenderExecutionContext context) {
        this.recommenderActionType = recommenderActionType;
        this.toolIdentifier = toolIdentifier;
        this.toolType = toolType;
        this.context = context;
        this.enable = false;
    }

    /**
     * 
     * 
     * @param recommenderActionType
     *            Type of the recommender action that is occurring.
     * @param tool
     * 
     */
    public ToolAction(RecommenderActionEnum recommenderActionType,
            String toolIdentifier, ToolType toolType) {
        this.recommenderActionType = recommenderActionType;
        this.toolIdentifier = toolIdentifier;
        this.toolType = toolType;
        this.context = RecommenderExecutionContext.getEmptyContext();
        this.enable = false;
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
     * @param context
     */
    public ToolAction(RecommenderActionEnum recommenderActionType,
            String toolIdentifier, ToolType toolType,
            Map<String, Serializable> auxiliarlyDetails,
            RecommenderExecutionContext context) {
        this.recommenderActionType = recommenderActionType;
        this.toolIdentifier = toolIdentifier;
        this.toolType = toolType;
        this.auxiliaryDetails = auxiliarlyDetails;
        this.context = context;
        this.enable = false;
    }

    /**
     * Construct a standard instance for enabling or disabling the running of
     * event-driven tools at regular intervals.
     * 
     * @param enable
     *            Flag indicating whether or not the running of event-driven
     *            tools should be enabled.
     */
    public ToolAction(boolean enable) {
        this.recommenderActionType = RecommenderActionEnum.ENABLE_EVENT_DRIVEN_TOOLS;
        this.toolIdentifier = null;
        this.toolType = ToolType.RECOMMENDER;
        this.auxiliaryDetails = null;
        this.context = null;
        this.enable = enable;
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
     * Determine whether the enable flag is true.
     */
    public boolean isEnabled() {
        return enable;
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

    public String getToolName() {
        return toolIdentifier;
    }

    public RecommenderExecutionContext getContext() {
        return context;
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
        return toolType;
    }
}
