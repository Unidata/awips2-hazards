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

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabFolderEvent;
import org.eclipse.swt.events.TypedEvent;
import org.eclipse.swt.widgets.Widget;

/**
 * Description: Customizable tab folder event, to be used with a
 * {@link CustomizableTabFolder}.
 * <p>
 * Note that this class is a copy of {@link CTabFolderEvent} changed to work
 * with a <code>CustomizableTabFolder</code> instead of a {@link CTabFolder}.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Feb 05, 2017   15556    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
class CustomizableTabFolderEvent extends TypedEvent {
    /**
     * The tab item for the operation.
     */
    public Widget item;

    /**
     * A flag indicating whether the operation should be allowed. Setting this
     * field to <code>false</code> will cancel the operation. Applies to the
     * close and showList events.
     */
    public boolean doit;

    /**
     * The widget-relative, x coordinate of the chevron button at the time of
     * the event. Applies to the showList event.
     * 
     * @since 3.0
     */
    public int x;

    /**
     * The widget-relative, y coordinate of the chevron button at the time of
     * the event. Applies to the showList event.
     * 
     * @since 3.0
     */
    public int y;

    /**
     * The width of the chevron button at the time of the event. Applies to the
     * showList event.
     * 
     * @since 3.0
     */
    public int width;

    /**
     * The height of the chevron button at the time of the event. Applies to the
     * showList event.
     * 
     * @since 3.0
     */
    public int height;

    static final long serialVersionUID = 3760566386225066807L;

    /**
     * Constructs a new instance of this class.
     * 
     * @param w
     *            the widget that fired the event
     */
    CustomizableTabFolderEvent(Widget w) {
        super(w);
    }

    /**
     * Returns a string containing a concise, human-readable description of the
     * receiver.
     * 
     * @return a string representation of the event
     */
    @Override
    public String toString() {
        String string = super.toString();
        return string.substring(0, string.length() - 1) // remove trailing '}'
                + " item=" + item + " doit=" + doit + " x=" + x + " y="
                + y
                + " width=" + width + " height=" + height + "}";
    }
}
