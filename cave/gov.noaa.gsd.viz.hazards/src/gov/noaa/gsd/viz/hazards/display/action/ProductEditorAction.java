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

import java.util.List;

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardAction;
import com.raytheon.uf.common.hazards.productgen.GeneratedProductList;

/**
 * Action class "fired" from the Product Editor. Registered observers receive
 * this object and act on it.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 04, 2013            Bryon.Lawrence      Initial induction into repo
 * Feb 07, 2014 2890       bkowal      Product Generation JSON refactor.
 * 
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class ProductEditorAction {
    private HazardAction hazardAction;

    private List<GeneratedProductList> generatedProductsList;

    private String eventID;

    public ProductEditorAction(HazardAction hazardAction) {
        this.hazardAction = hazardAction;
    }

    public ProductEditorAction(HazardAction hazardAction, String ID,
            List<GeneratedProductList> generatedProductsList) {
        this.hazardAction = hazardAction;
        this.generatedProductsList = generatedProductsList;
        this.eventID = ID;
    }

    public HazardAction getHazardAction() {
        return hazardAction;
    }

    public void setHazardAction(HazardAction hazardAction) {
        this.hazardAction = hazardAction;
    }

    public List<GeneratedProductList> getGeneratedProductsList() {
        return generatedProductsList;
    }

    public void setGeneratedProductsList(
            List<GeneratedProductList> generatedProductsList) {
        this.generatedProductsList = generatedProductsList;
    }

    public void setEventID(String ID) {
        this.eventID = ID;
    }

    public String getEventID() {
        return eventID;
    }
}