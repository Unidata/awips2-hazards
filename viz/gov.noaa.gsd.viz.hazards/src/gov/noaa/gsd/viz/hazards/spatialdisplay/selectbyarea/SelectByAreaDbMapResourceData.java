/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.selectbyarea;

import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.maps.rsc.AbstractDbMapResourceData;
import com.raytheon.uf.viz.core.rsc.LoadProperties;

/**
 * Constructs an SelectByAreaDbMapResource object. Based upon RTS code
 * originallhy by randerso.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * November 2011           Bryon.Lawrence    Initial creation
 * 
 * </pre>
 * 
 * @author randerso
 * @version 1.0
 */

public class SelectByAreaDbMapResourceData extends AbstractDbMapResourceData {

    public SelectByAreaDbMapResourceData() {
        super();
    }

    @Override
    public SelectByAreaDbMapResource construct(LoadProperties loadProperties,
            IDescriptor descriptor) throws VizException {
        return new SelectByAreaDbMapResource(this, loadProperties);
    }
}
