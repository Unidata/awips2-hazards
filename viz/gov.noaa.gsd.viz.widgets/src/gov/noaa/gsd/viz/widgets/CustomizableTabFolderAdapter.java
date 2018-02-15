/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.widgets;

/**
 * Adapter for customizable tab folder listener implementations that do not want
 * to have to implement all the methods of the interface.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Apr 20, 2018   30277    Chris.Golden  Initial creation.
 * </pre>
 *
 * @author Chris.Golden
 */
public class CustomizableTabFolderAdapter
        implements ICustomizableTabFolderListener {

    @Override
    public void close(CustomizableTabFolderEvent event) {
    }

    @Override
    public void minimize(CustomizableTabFolderEvent event) {
    }

    @Override
    public void maximize(CustomizableTabFolderEvent event) {
    }

    @Override
    public void restore(CustomizableTabFolderEvent event) {
    }

    @Override
    public void showList(CustomizableTabFolderEvent event) {
    }
}
