/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.widgets;

/**
 * Interface describing the methods that must be implemented in order to be a
 * visible value zoom calculator. The latter may be used to calculate the new
 * visible value range that should be used in response to a zoom action
 * initiated by the user via mouse or keyboard input.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * 
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 * @see MultiValueRuler
 */
public interface IVisibleValueZoomCalculator {

    // Public Methods

    /**
     * Get the visible value range to be used for the specified ruler when
     * zoomed as given.
     * 
     * @param ruler
     *            Ruler that should be zoomed.
     * @param zoomIn
     *            Flag indicating whether the zoom is inward (so that greater
     *            detail is seen) or outward (so that a greater range is seen).
     * @param amplitude
     *            Amplitude of the zoom.
     * @return Visible value range to be used, or <code>0</code> if no zoom
     *         should occur.
     */
    public long getVisibleValueRangeForZoom(MultiValueRuler ruler,
            boolean zoomIn, int amplitude);
}
