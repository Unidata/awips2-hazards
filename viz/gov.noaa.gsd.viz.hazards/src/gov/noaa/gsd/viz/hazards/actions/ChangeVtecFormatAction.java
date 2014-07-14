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
package gov.noaa.gsd.viz.hazards.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import com.raytheon.uf.viz.hazards.sessionmanager.product.ISessionProductManager;

/**
 * <code>Action</code> for changing the VTEC format when in practice mode.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 15, 2014  #696      dgilling     Initial creation
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public final class ChangeVtecFormatAction extends Action {

    private enum VTECFormatMode {
        /*
         * TODO: Remove the NORMAL_NO_VTEC and TEST_NO_VTEC types. Users have
         * tenatively agreed to remove these options as they probably aren't
         * needed. Leaving them for now until final approval to remove these
         * options are granted.
         */
        NORMAL_NO_VTEC("Normal: NoVTEC"), NORMAL_O_VTEC("Normal: O-Vtec"), NORMAL_E_VTEC(
                "Normal: E-Vtec"), NORMAL_X_VTEC("Normal: X-Vtec"), TEST_NO_VTEC(
                "Test: NoVTEC"), TEST_T_VTEC("Test: T-Vtec");

        private final String displayString;

        private VTECFormatMode(String displayString) {
            this.displayString = displayString;
        }

        @Override
        public String toString() {
            return displayString;
        }
    }

    private final class VtecMenuCreator implements IMenuCreator {

        private Menu menu;

        /*
         * (non-Javadoc)
         * 
         * @see org.eclipse.jface.action.IMenuCreator#dispose()
         */
        @Override
        public void dispose() {
            menu.dispose();
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets
         * .Control)
         */
        @Override
        public Menu getMenu(Control parent) {
            menu = new Menu(parent);
            populateMenu();
            return menu;
        }

        /*
         * (non-Javadoc)
         * 
         * @see
         * org.eclipse.jface.action.IMenuCreator#getMenu(org.eclipse.swt.widgets
         * .Menu)
         */
        @Override
        public Menu getMenu(Menu parent) {
            menu = new Menu(parent);
            populateMenu();
            return menu;
        }

        private void populateMenu() {
            for (VTECFormatMode vtecFormat : VTECFormatMode.values()) {
                /*
                 * TODO: Remove this if-check when NO_VTEC modes are removed
                 * from the VTECFormatMode enum.
                 */
                if ((!VTECFormatMode.NORMAL_NO_VTEC.equals(vtecFormat))
                        && (!VTECFormatMode.TEST_NO_VTEC.equals(vtecFormat))) {
                    Action action = new ChangeVtecFormatAction(vtecFormat);
                    IContributionItem contrib = new ActionContributionItem(
                            action);
                    contrib.fill(menu, -1);
                }
            }
        }
    }

    private static ISessionProductManager productMgr;

    private String vtecMode;

    private boolean testMode;

    public ChangeVtecFormatAction(ISessionProductManager productMgr) {
        super("Change VTEC Format", Action.AS_DROP_DOWN_MENU);
        ChangeVtecFormatAction.productMgr = productMgr;
        IMenuCreator creator = new VtecMenuCreator();
        setMenuCreator(creator);
    }

    public ChangeVtecFormatAction(VTECFormatMode selection) {
        super(selection.toString(), Action.AS_RADIO_BUTTON);
        switch (selection) {
        case NORMAL_E_VTEC:
            testMode = false;
            vtecMode = "E";
            break;
        case NORMAL_NO_VTEC:
            testMode = false;
            vtecMode = null;
            break;
        case NORMAL_O_VTEC:
            testMode = false;
            vtecMode = "O";
            setChecked(true);
            break;
        case NORMAL_X_VTEC:
            testMode = false;
            vtecMode = "X";
            break;
        case TEST_NO_VTEC:
            testMode = true;
            vtecMode = null;
            break;
        case TEST_T_VTEC:
            testMode = true;
            vtecMode = "T";
            break;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jface.action.Action#run()
     */
    @Override
    public void run() {
        if (isChecked()) {
            productMgr.setVTECFormat(vtecMode, testMode);
        }
    }
}
