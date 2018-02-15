/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.selectbyarea;

import com.raytheon.uf.common.colormap.Color;
import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.maps.rsc.AbstractDbMapResourceData;
import com.raytheon.uf.viz.core.rsc.LoadProperties;

/**
 * Select by area database map resource data, providing information about the
 * select by area database map resource.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#     Engineer        Description
 * ------------ ---------- ---------------- --------------------------
 * Nov 2011                Bryon.Lawrence   Initial creation.
 * Jun 27, 2017 14789      Robert.Blum      Select by Area now has a configurable color.
 * </pre>
 * 
 * @author Bryon.Lawrence
 * @version 1.0
 */
public class SelectByAreaDbMapResourceData extends AbstractDbMapResourceData {

    // Private Variables

    /**
     * Color to be used for select by area editing operations.
     */
    private Color editColor;

    // Public Methods

    /**
     * Set the color to be used for select by area editing operations.
     * 
     * @param editColor
     *            Color to be used.
     */
    public void setEditColor(Color editColor) {
        this.editColor = editColor;
    }

    @Override
    public SelectByAreaDbMapResource construct(LoadProperties loadProperties,
            IDescriptor descriptor) throws VizException {
        return new SelectByAreaDbMapResource(this, loadProperties, editColor);
    }
}
