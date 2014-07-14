/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.display.test;

import gov.noaa.gsd.viz.hazards.display.RCPMainUserInterfaceElement;
import gov.noaa.gsd.viz.hazards.tools.IToolsView;
import gov.noaa.gsd.viz.hazards.tools.ToolsPresenter;

import java.util.List;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.eclipse.jface.action.Action;

import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Tool;

/**
 * Description: Mock {@link IToolsView} for testing
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Oct 22, 2013 2166      daniel.s.schaffer@noaa.gov      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
@SuppressWarnings("rawtypes")
public class ToolsViewForTesting implements
        IToolsView<Action, RCPMainUserInterfaceElement> {

    @Override
    public void dispose() {
    }

    @Override
    public void initialize(ToolsPresenter presenter, List<Tool> tools) {
    }

    @Override
    public void showToolParameterGatherer(String toolName, String jsonParams) {
    }

    @Override
    public void setTools(List<Tool> tools) {
    }

    @Override
    public List contributeToMainUI(RCPMainUserInterfaceElement type) {
        return null;
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }
}
