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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;

import com.raytheon.uf.common.dataplugin.text.db.OperationalStdTextProduct;
import com.raytheon.uf.common.dataplugin.text.db.PracticeStdTextProduct;
import com.raytheon.uf.common.dataplugin.text.db.StdTextProduct;
import com.raytheon.uf.common.site.SiteMap;
import com.raytheon.uf.common.status.IPerformanceStatusHandler;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.PerformanceStatus;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.time.FormattedDate;
import com.raytheon.uf.viz.core.catalog.DirectDbQuery;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.viz.core.mode.CAVEMode;

/**
 * Queries the Text database for Text Products.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 27, 2017 22308      Robert.Blum Initial creation
 * Jul 12, 2017 35941      Robert.Blum Test Mode now queries the correct database table.
 *
 * </pre>
 *
 * @author Robert.Blum
 */

public class TextProductQueryJob extends Job {

    private static final IUFStatusHandler handler = UFStatus
            .getHandler(TextProductQueryJob.class);

    private static String TEXT_DATABASE = "fxa";

    private static String OPERATIONAL_TABLE_NAME = "stdtextproducts";

    private static String PRACTICE_TABLE_NAME = "practicestdtextproducts";

    private final IPerformanceStatusHandler perfLog = PerformanceStatus
            .getHandler("TextProductQuery:");

    private boolean practice;

    private final Set<String> sites;

    private final Set<String> pils;

    private List<StdTextProduct> textProducts;

    public TextProductQueryJob(Set<String> sites, Set<String> pils) {
        super("TextProductQuery");
        this.practice = CAVEMode.getMode() != CAVEMode.OPERATIONAL;
        this.sites = sites;
        this.pils = pils;

    }

    @Override
    protected IStatus run(IProgressMonitor monitor) {
        /*
         * Get the possible sites.
         */
        SiteMap siteMap = SiteMap.getInstance();
        Set<String> possibleSites = new HashSet<>();
        for (String site : sites) {
            possibleSites.add(siteMap.getSite4LetterId(site));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("SELECT nnnid, site, inserttime, product FROM ");
        sb.append(practice ? PRACTICE_TABLE_NAME : OPERATIONAL_TABLE_NAME);
        sb.append(" WHERE site in (");
        int numSites = possibleSites.size();
        int count = 0;
        for (String site : possibleSites) {
            sb.append("'");
            sb.append(site);
            sb.append("'");
            count++;
            if (count < numSites) {
                sb.append(", ");
            }
        }
        sb.append(") and nnnid in (");
        count = 0;
        int numPils = pils.size();
        for (String pil : pils) {
            sb.append("'");
            sb.append(pil);
            sb.append("'");
            count++;
            if (count < numPils) {
                sb.append(", ");
            }
        }
        sb.append(")");
        long t0 = System.currentTimeMillis();

        List<Object[]> queryResult;
        try {
            queryResult = DirectDbQuery.executeQuery(sb.toString(),
                    TEXT_DATABASE, DirectDbQuery.QueryLanguage.SQL);
        } catch (VizException e) {
            handler.error("Failed to query for Text Products.");
            return Status.CANCEL_STATUS;
        }
        long t1 = System.currentTimeMillis();
        perfLog.logDuration("TextProduct Query: ", (t1 - t0));

        textProducts = new ArrayList<>(queryResult.size());
        for (Object[] objArr : queryResult) {
            StdTextProduct product;
            if (practice) {
                product = new PracticeStdTextProduct();
            } else {
                product = new OperationalStdTextProduct();
            }
            product.setNnnid((String) objArr[0]);
            product.setSite((String) objArr[1]);
            Long insertTime = ((FormattedDate) objArr[2]).getTime();
            Calendar insertCal = Calendar
                    .getInstance(TimeZone.getTimeZone("GMT"));
            insertCal.setTimeInMillis(insertTime);
            product.setInsertTime(insertCal);
            product.setProduct((String) objArr[3]);
            textProducts.add(product);
        }
        return Status.OK_STATUS;
    }

    public List<StdTextProduct> getTextProducts() {
        return textProducts;
    }
}
