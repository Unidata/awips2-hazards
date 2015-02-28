/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization.
 * 
 * Contractor Name:        Raytheon Company
 * Contractor Address:     6825 Pine Street, Suite 340
 *                         Mail Stop B8
 *                         Omaha, NE 68106
 *                         402.291.0100
 * 
 * See the AWIPS II Master Rights File ("Master Rights File.pdf") for
 * further licensing information.
 **/
package gov.noaa.gsd.viz.hazards.risecrestfall;

import java.util.Date;

import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Region;

/**
 * Event line region used to determine if mouse pointer is on the event line.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 17, 2015    3847    mpduff      Initial creation
 * 
 * </pre>
 * 
 * @author mpduff
 * @version 1.0
 */

public class EventRegion {

    public enum EventType {
        BEGIN("B"), RISE("R"), CREST("C"), FALL("F"), END("E");

        private String abbreviation;

        EventType(String abbreviation) {
            this.abbreviation = abbreviation;
        }

        public String getAbbreviation() {
            return abbreviation;
        }
    }

    private Region region;

    private EventType eventType;

    private Color eventColor;

    private Date date;

    private boolean visible = true;

    public EventRegion() {

    }

    public EventRegion(Region region, EventType eventType, Color eventColor,
            Date date) {
        this.region = region;
        this.eventType = eventType;
        this.eventColor = eventColor;
        this.date = date;
    }

    /**
     * @return the region
     */
    public Region getRegion() {
        return region;
    }

    /**
     * @param region
     *            the region to set
     */
    public void setRegion(Region region) {
        this.region = region;
    }

    /**
     * @return the eventType
     */
    public EventType getEventType() {
        return eventType;
    }

    /**
     * @param eventType
     *            the eventType to set
     */
    public void setEventType(EventType eventType) {
        this.eventType = eventType;
    }

    public String getAbbreviation() {
        if (this.eventType != null) {
            return eventType.getAbbreviation();
        }

        return "";
    }

    /**
     * @return the eventColor
     */
    public Color getEventColor() {
        return eventColor;
    }

    /**
     * @param eventColor
     *            the eventColor to set
     */
    public void setEventColor(Color eventColor) {
        this.eventColor = eventColor;
    }

    /**
     * @return the date
     */
    public Date getDate() {
        return date;
    }

    /**
     * @param date
     *            the date to set
     */
    public void setDate(Date date) {
        this.date = date;
    }

    /**
     * @return the visible
     */
    public boolean isVisible() {
        return visible;
    }

    /**
     * @param visible
     *            the visible to set
     */
    public void setVisible(boolean visible) {
        this.visible = visible;
    }

    public void dispose() {
        if (region != null) {
            region.dispose();
        }
    }
}
