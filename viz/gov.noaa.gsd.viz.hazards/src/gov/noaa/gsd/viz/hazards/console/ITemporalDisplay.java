/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.console;

/**
 * Description: Interface describing the methods that must be implemented by a
 * class intended to be used as a temporal display that may be manipulated
 * (allowing the panning and zooming of time).
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Dec 01, 2016   15556    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
interface ITemporalDisplay {

    // Public Enumerated Types

    /**
     * Selected time modes.
     */
    public enum SelectedTimeMode {

        // Values

        SINGLE("Single"), RANGE("Range");

        // Private Variables

        /**
         * Text name.
         */
        private final String name;

        // Private Constructors

        /**
         * Construct a standard instance.
         * 
         * @param name
         *            Text name.
         */
        private SelectedTimeMode(String name) {
            this.name = name;
        }

        // Public Methods

        /**
         * Get the text name.
         * 
         * @return Text name.
         */
        public String getName() {
            return name;
        }
    };

    // Public Methods

    /**
     * Zoom the visible time range out by one level.
     */
    public void zoomTimeOut();

    /**
     * Page the time range backward.
     */
    public void pageTimeBack();

    /**
     * Pan the time range backward.
     */
    public void panTimeBack();

    /**
     * Pan the time line to ensure that the current time is shown.
     */
    public void showCurrentTime();

    /**
     * Pan the time range forward.
     */
    public void panTimeForward();

    /**
     * Page the time range forward.
     */
    public void pageTimeForward();

    /**
     * Zoom the visible time range in by one level.
     */
    public void zoomTimeIn();

    /**
     * Get the selected time mode.
     * 
     * @return Selected time mode.
     */
    public SelectedTimeMode getSelectedTimeMode();

    /**
     * Set the selected time mode.
     * 
     * @param mode
     *            New selected time mode.
     */
    public void setSelectedTimeMode(SelectedTimeMode mode);
}
