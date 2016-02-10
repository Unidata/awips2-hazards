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
package gov.noaa.gsd.viz.hazards.servicebackup;

import gov.noaa.gsd.viz.hazards.console.ConsolePresenter;

import java.util.Arrays;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;

import com.raytheon.uf.common.localization.LocalizationContext.LocalizationLevel;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.viz.core.localization.LocalizationManager;

/**
 * Site action for changing the site during Service Backup
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Aug 28, 2013            mnash     Initial creation
 * Dec 13, 2014 4959       Dan Schaffer Spatial Display cleanup and other bug fixes
 * Sep 28, 2015 10302,8167 hansen       Re-wrote retrieveSites to use get Backup Sites from StartupConfig
 * </pre>
 * 
 * @author mnash
 * @version 1.0
 */

public class ChangeSiteAction extends Action {

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ChangeSiteAction.class);

    private String site;

    private static ConsolePresenter presenter;

    public ChangeSiteAction(ConsolePresenter presenter) {
        super("Change Site", Action.AS_DROP_DOWN_MENU);
        ChangeSiteAction.presenter = presenter;

        MenuCreator creator = new MenuCreator();
        setMenuCreator(creator);
    }

    public ChangeSiteAction(String site) {
        super(site, Action.AS_RADIO_BUTTON);
        this.site = site;
        if (site.equals(LocalizationManager
                .getContextName(LocalizationLevel.SITE))) {
            setChecked(true);
        }
    }

    @Override
    public void run() {
        if (!this.isChecked()) {
            return;
        }
        presenter.publish(this);
    }

    private static class MenuCreator implements IMenuCreator {

        private Menu menu;

        @Override
        public void dispose() {
            menu.dispose();
        }

        @Override
        public Menu getMenu(Control parent) {
            menu = new Menu(parent);
            fill();
            return menu;
        }

        @Override
        public Menu getMenu(Menu parent) {
            menu = new Menu(parent);
            fill();
            return menu;
        }

        private void fill() {
            // read in file
            List<String> sites = retrieveSites();

            for (int index = 0; index < sites.size(); ++index) {
                addAction(sites.get(index));
            }
        }

        private void addAction(String site) {
            Action action = new ChangeSiteAction(site);
            IContributionItem contrib = new ActionContributionItem(action);
            contrib.fill(menu, -1);
        }

        private List<String> retrieveSites() {
            return Arrays.asList(presenter.getSessionManager()
                    .getConfigurationManager().getStartUpConfig()
                    .getBackupSites());
        }
    };

    /**
     * @return the site
     */
    public String getSite() {
        return site;
    }
}
