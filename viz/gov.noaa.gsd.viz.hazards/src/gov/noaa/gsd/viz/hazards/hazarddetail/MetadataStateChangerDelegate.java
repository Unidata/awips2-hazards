/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.hazarddetail;

import gov.noaa.gsd.common.utilities.IRunnableAsynchronousScheduler;
import gov.noaa.gsd.viz.hazards.ui.PrincipalRunnableTask;
import gov.noaa.gsd.viz.hazards.ui.StateChangerDelegate;
import gov.noaa.gsd.viz.hazards.ui.ViewPartWidgetDelegateHelper;
import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;

import java.io.Serializable;
import java.util.Map;

/**
 * A metadata state changer delegate, used by a {@link HazardDetailView} object
 * to represent metadata state changers within its associated
 * {@link HazardDetailViewPart}.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * May 09, 2014    2925    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
class MetadataStateChangerDelegate extends
        StateChangerDelegate<String, Serializable, IMetadataStateChanger>
        implements IMetadataStateChanger {

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param helper
     *            Widget delegate helper.
     * @param handlerScheduler
     *            Handler invocation scheduler.
     */
    public MetadataStateChangerDelegate(
            ViewPartWidgetDelegateHelper<String, HazardDetailViewPart, IMetadataStateChanger> helper,
            IRunnableAsynchronousScheduler handlerScheduler) {
        super(helper, handlerScheduler);
    }

    // Public Methods

    @Override
    public void setMegawidgetSpecifierManager(final String eventIdentifier,
            final MegawidgetSpecifierManager specifierManager,
            final Map<String, Serializable> metadataStates) {
        runOrScheduleTask(new PrincipalRunnableTask<String, IMetadataStateChanger>() {

            @Override
            public void run() {
                getPrincipal().setMegawidgetSpecifierManager(eventIdentifier,
                        specifierManager, metadataStates);
            }
        });
    }
}
