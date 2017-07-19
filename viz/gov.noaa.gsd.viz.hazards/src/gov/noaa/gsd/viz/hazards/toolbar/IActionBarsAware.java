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
package gov.noaa.gsd.viz.hazards.toolbar;

import org.eclipse.ui.IActionBars;

/**
 * Description: Interface that must be implemented by any actions that need to
 * know what action bars object is managing them.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Oct 03, 2016 22299      Kevin.Bisanz Initial creation, based on
 *                                      IContributionManagerAware.java
 * </pre>
 *
 * @author Kevin.Bisanz
 * @version 1.0
 */
public interface IActionBarsAware {

    /**
     * Set the action bars.
     *
     * @param actionBars
     *            New action bars.
     */
    public void setActionBars(IActionBars actionBars);
}
