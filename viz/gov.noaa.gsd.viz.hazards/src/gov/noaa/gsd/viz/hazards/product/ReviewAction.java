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
package gov.noaa.gsd.viz.hazards.product;

import gov.noaa.gsd.viz.hazards.console.ConsolePresenter;
import gov.noaa.gsd.viz.hazards.display.action.HazardDetailAction;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.MenuListener;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import com.raytheon.uf.common.hazards.productgen.data.ProductData;
import com.raytheon.uf.common.hazards.productgen.data.ProductDataUtil;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.SimulatedTime;
import com.raytheon.viz.core.mode.CAVEMode;

/**
 * Handles displaying correctable products.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 16, 2014            jsanchez     Initial creation
 * Dec 13, 2014 4959       Dan Schaffer Spatial Display cleanup and other bug fixes
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class ReviewAction extends Action {

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ReviewAction.class);

    public static final String PRODUCT_DATA_PARAM = "productData";

    private static class ReviewKey {
        String productGeneratorName;

        ArrayList<Integer> eventIDs;

        @Override
        public int hashCode() {
            final int prime = 31;
            int result = 1;
            result = prime * result
                    + ((eventIDs == null) ? 0 : eventIDs.hashCode());
            result = prime
                    * result
                    + ((productGeneratorName == null) ? 0
                            : productGeneratorName.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ReviewKey other = (ReviewKey) obj;
            if (eventIDs == null) {
                if (other.eventIDs != null)
                    return false;
            } else if (!eventIDs.equals(other.eventIDs))
                return false;
            if (productGeneratorName == null) {
                if (other.productGeneratorName != null)
                    return false;
            } else if (!productGeneratorName.equals(other.productGeneratorName))
                return false;
            return true;
        }

    }

    private ArrayList<ProductData> productData;

    private static ConsolePresenter presenter;

    public ReviewAction(ConsolePresenter presenter) {
        super("Review/Correct Product(s)", Action.AS_DROP_DOWN_MENU);
        ReviewAction.presenter = presenter;

        MenuCreator creator = new MenuCreator();
        setMenuCreator(creator);
    }

    public ReviewAction(String text) {
        super(text, Action.AS_PUSH_BUTTON);
    }

    @Override
    public void run() {
        HazardDetailAction action = new HazardDetailAction(
                HazardDetailAction.ActionType.REVIEW);
        Map<String, Serializable> parameters = new HashMap<String, Serializable>();
        parameters.put(PRODUCT_DATA_PARAM, productData);
        action.setParameters(parameters);
        presenter.publish(action);
    }

    private static class MenuCreator implements IMenuCreator {

        private Menu menu;

        MenuListener listener = new MenuListener() {
            @Override
            public void menuShown(MenuEvent e) {
                for (MenuItem item : menu.getItems()) {
                    item.dispose();
                }
                fill();
            }

            @Override
            public void menuHidden(MenuEvent e) {
            }
        };

        @Override
        public void dispose() {
            menu.removeMenuListener(listener);
            menu.dispose();
        }

        @Override
        public Menu getMenu(Control parent) {
            return getMenu(parent.getMenu());
        }

        @Override
        public Menu getMenu(Menu parent) {
            menu = new Menu(parent);
            menu.addMenuListener(listener);
            fill();
            return menu;
        }

        private void fill() {
            String mode = CAVEMode.getMode().toString();
            Date simulatedTime = SimulatedTime.getSystemTime().getTime();
            List<ProductData> correctableProductData = ProductDataUtil
                    .retrieveCorrectableProductData(mode, simulatedTime);

            if (correctableProductData.size() > 0) {
                Map<ReviewKey, ArrayList<ProductData>> map = new HashMap<ReviewKey, ArrayList<ProductData>>();
                for (ProductData productData : correctableProductData) {
                    ReviewKey key = new ReviewKey();
                    key.productGeneratorName = productData
                            .getProductGeneratorName();
                    key.eventIDs = productData.getEventIDs();
                    ArrayList<ProductData> list = map.get(key);
                    if (list == null) {
                        list = new ArrayList<ProductData>();
                    }
                    list.add(productData);
                    map.put(key, list);
                }

                ArrayList<ArrayList<ProductData>> values = new ArrayList(
                        map.values());
                Collections.sort(values,
                        new Comparator<ArrayList<ProductData>>() {

                            @Override
                            public int compare(ArrayList<ProductData> o1,
                                    ArrayList<ProductData> o2) {
                                String text1 = createText(o1);
                                String text2 = createText(o2);
                                return text1.compareTo(text2);
                            }
                        });
                for (ArrayList<ProductData> list : values) {
                    addAction(list);
                }

            } else {
                createEmpty();
            }
        }

        private void createEmpty() {
            MenuItem item = new MenuItem(menu, SWT.PUSH);
            item.setText("None Available");
            item.setEnabled(false);
        }

        private void addAction(ArrayList<ProductData> productData) {
            String text = createText(productData);
            ReviewAction action = new ReviewAction(text);
            action.productData = productData;
            IContributionItem contrib = new ActionContributionItem(action);
            contrib.fill(menu, -1);
        }

        private String createText(ArrayList<ProductData> productData) {
            ProductData first = productData.get(0);
            String productID = first.getProductGeneratorName().replace(
                    "_ProductGenerator", "");
            StringBuilder sb = new StringBuilder();
            for (Integer eventID : first.getEventIDs()) {
                if (sb.length() == 0) {
                    sb.append(productID);
                    sb.append(" - ");
                } else {
                    sb.append(",");
                }
                sb.append(eventID);
            }

            return sb.toString();
        }
    };
}
