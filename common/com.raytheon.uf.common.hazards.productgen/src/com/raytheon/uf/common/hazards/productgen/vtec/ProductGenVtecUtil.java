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
package com.raytheon.uf.common.hazards.productgen.vtec;

import java.util.Calendar;

import com.raytheon.uf.common.activetable.ActiveTableMode;
import com.raytheon.uf.common.activetable.GetNextEtnRequest;
import com.raytheon.uf.common.activetable.response.GetNextEtnResponse;
import com.raytheon.uf.common.serialization.comm.RequestRouter;
import com.raytheon.uf.common.time.SimulatedTime;

/**
 * Utility class to set ETNs on Hazard Services legacy VTEC products prior to
 * transmission.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Nov 20, 2013  #2462     dgilling     Initial creation
 * 
 * </pre>
 * 
 * @author dgilling
 * @version 1.0
 */

public class ProductGenVtecUtil {

    /**
     * A private constructor so that Java does not attempt to create one for us.
     * As this class should not be instantiated, do not attempt to ever call
     * this constructor; it will simply throw an AssertionError.
     * 
     */
    private ProductGenVtecUtil() {
        throw new AssertionError();
    }

    public static int getLastEtn(String office, String phensig)
            throws Exception {
        return getNextEtn(office, phensig, false, false, false, null)
                .getNextEtn() - 1;
    }

    /**
     * Gets the next available ETN for a specific product and office.
     * 
     * @param office
     *            The 4-character site ID of the office.
     * @param phensig
     *            The phenomenon and significance of the hazard concatenated
     *            with a '.' (e.g., TO.W or DU.Y)
     * @param lockEtn
     *            Whether or not to request an exclusive ETN--if true, this will
     *            cause the server to increment its running ETN sequence to the
     *            next number after determining the next ETN for this request.
     *            If false, the next ETN will be returned, but it will not
     *            increment the server's running sequence, so the ETN return
     *            could be used by another client that makes a
     *            GetNextEtnRequest.
     * @return The next ETN in sequence, given the office and phensig.
     * @throws Exception
     *             If an error occurred sending the request to the server.
     */
    public static int getNextEtn(String office, String phensig, boolean lockEtn)
            throws Exception {
        return getNextEtn(office, phensig, lockEtn, false, false, null)
                .getNextEtn();
    }

    /**
     * Gets the next available ETN for a specific product and office.
     * 
     * @param office
     *            The 4-character site ID of the office.
     * @param phensig
     *            The phenomenon and significance of the hazard concatenated
     *            with a '.' (e.g., TO.W or DU.Y)
     * @param lockEtn
     *            Whether or not to request an exclusive ETN--if true, this will
     *            cause the server to increment its running ETN sequence to the
     *            next number after determining the next ETN for this request.
     *            If false, the next ETN will be returned, but it will not
     *            increment the server's running sequence, so the ETN return
     *            could be used by another client that makes a
     *            GetNextEtnRequest.
     * @param performISC
     *            Whether or not to collaborate with neighboring sites to
     *            determine the next ETN. See {@link
     *            GetNextEtnUtil#getNextEtnFromPartners(String, ActiveTableMode,
     *            String, Calendar, List<IRequestRouter>)} for more information.
     * @param reportOnlyConflict
     *            Affects which kinds of errors get reported back to the
     *            requestor. If true, only cases where the value of
     *            <code>etnOverride</code> is less than or equal to the last ETN
     *            used by this site or any of its partners will be reported.
     *            Else, all significant errors will be reported back.
     * @param etnOverride
     *            Allows the user to influence the next ETN assigned by using
     *            this value unless it is less than or equal to the last ETN
     *            used by this site or one of its partners.
     * @return The next ETN in sequence, given the office and phensig.
     * @throws Exception
     *             If an error occurs while submitting or processing the remote
     *             request.
     */
    public static GetNextEtnResponse getNextEtn(String office, String phensig,
            boolean lockEtn, boolean performISC, boolean reportOnlyConflict,
            Integer etnOverride) throws Exception {
        Calendar currentTime = Calendar.getInstance();
        currentTime.setTime(SimulatedTime.getSystemTime().getTime());
        // TODO: Set ActiveTableMode based on some session state variable
        // For now we'll hard code to OPERATIONAL, but we should have the option
        // to fall back to PRACTICE in the future.
        ActiveTableMode activeTable = ActiveTableMode.OPERATIONAL;
        GetNextEtnRequest req = new GetNextEtnRequest(office, activeTable,
                phensig, currentTime, lockEtn, performISC, reportOnlyConflict,
                etnOverride);

        GetNextEtnResponse resp = (GetNextEtnResponse) RequestRouter.route(req);
        GetNextEtnResponse rval = (resp != null) ? resp
                : new GetNextEtnResponse(1, phensig);
        return rval;
    }
}
