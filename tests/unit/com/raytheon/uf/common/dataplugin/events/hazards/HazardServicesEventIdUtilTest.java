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
package com.raytheon.uf.common.dataplugin.events.hazards;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Calendar;
import java.util.TimeZone;

import org.junit.Before;
import org.junit.Test;

import com.raytheon.uf.common.dataplugin.events.hazards.event.AbstractHazardServicesEventIdUtil;
import com.raytheon.uf.common.dataplugin.events.hazards.event.AbstractHazardServicesEventIdUtil.IdDisplayType;
import com.raytheon.uf.common.dataplugin.events.hazards.event.HazardServicesEventIdUtil;
import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.client.HazardEventRequestServices;

/**
 * Event Id format: HZ-2017-OAX-DMX-000001 where OAX is the site for the product
 * and DMX is the issuing site
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer      Description
 * ------------ ---------- ------------- --------------------------
 * Feb 14, 2017  28708     mpduff        Initial creation
 * Mar 13, 2017  28708     Chris.Golden  Further work with new HazardServicesEventIdUtil.
 * Apr 19, 2017  33275     mpduff        Event ID generation changed so no more leading zeroes.
 * </pre>
 *
 * @author mpduff
 */

public class HazardServicesEventIdUtilTest {

    private static final String BACKUP_SITE = "DMX";

    private static final String LOCAL_SITE = "OAX";

    private static final String EVENT_ID = "1";

    private HazardEventRequestServices requestService = mock(
            HazardEventRequestServices.class);

    @Before
    public void setUp() {

        when(requestService.requestEventId(LOCAL_SITE)).thenReturn(EVENT_ID);
        when(requestService.requestEventId(BACKUP_SITE)).thenReturn(EVENT_ID);
    }

    @Test
    public void testCanGenerateIdForLocalSiteOperationalModeAlwaysFull() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        int year = cal.get(Calendar.YEAR);

        String expected = "HZ-" + year + "-" + LOCAL_SITE + "-" + LOCAL_SITE
                + "-" + EVENT_ID;

        AbstractHazardServicesEventIdUtil util = HazardServicesEventIdUtil
                .getInstance(false);
        util.setRequestService(requestService);
        util.setIdDisplayType(IdDisplayType.ALWAYS_FULL);
        String eventId = util.getNewEventID(LOCAL_SITE, LOCAL_SITE);
        String displayId = util.getDisplayId(eventId, LOCAL_SITE);

        System.out.println(displayId);
        System.out.println(expected);
        assertEquals(expected, displayId);
    }

    @Test
    public void testCanGenerateIdForLocalSiteOperationalModeAlwaysSite() {
        String expected = LOCAL_SITE + "-" + LOCAL_SITE + "-" + EVENT_ID;

        AbstractHazardServicesEventIdUtil util = HazardServicesEventIdUtil
                .getInstance(false);
        util.setRequestService(requestService);
        util.setIdDisplayType(IdDisplayType.ALWAYS_SITE);
        String eventId = util.getNewEventID(LOCAL_SITE, LOCAL_SITE);
        String displayId = util.getDisplayId(eventId, LOCAL_SITE);

        System.out.println(displayId);
        System.out.println(expected);
        assertEquals(expected, displayId);
    }

    @Test
    public void testCanGenerateIdForLocalSiteOperationalModeOnlySerial() {

        String expected = EVENT_ID;

        AbstractHazardServicesEventIdUtil util = HazardServicesEventIdUtil
                .getInstance(false);
        util.setRequestService(requestService);
        util.setIdDisplayType(IdDisplayType.ONLY_SERIAL);
        String eventId = util.getNewEventID(LOCAL_SITE, LOCAL_SITE);
        String displayId = util.getDisplayId(eventId, LOCAL_SITE);

        System.out.println(displayId);
        System.out.println(expected);
        assertEquals(expected, displayId);
    }

    // Backup Sites
    @Test
    public void testCanGenerateIdForSiteOperationalModeAlwaysFull() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        int year = cal.get(Calendar.YEAR);

        String expected = "HZ-" + year + "-" + BACKUP_SITE + "-" + LOCAL_SITE
                + "-" + EVENT_ID;

        AbstractHazardServicesEventIdUtil util = HazardServicesEventIdUtil
                .getInstance(false);
        util.setRequestService(requestService);
        util.setIdDisplayType(IdDisplayType.ALWAYS_FULL);
        String eventId = util.getNewEventID(BACKUP_SITE, LOCAL_SITE);
        String displayId = util.getDisplayId(eventId, LOCAL_SITE);

        System.out.println(displayId);
        System.out.println(expected);
        assertEquals(expected, displayId);
    }

    @Test
    public void testCanGenerateIdForBackupSiteOperationalModeAlwaysSite() {
        String expected = BACKUP_SITE + "-" + LOCAL_SITE + "-" + EVENT_ID;

        AbstractHazardServicesEventIdUtil util = HazardServicesEventIdUtil
                .getInstance(false);
        util.setRequestService(requestService);
        util.setIdDisplayType(IdDisplayType.ALWAYS_SITE);
        String eventId = util.getNewEventID(BACKUP_SITE, LOCAL_SITE);
        String displayId = util.getDisplayId(eventId, LOCAL_SITE);

        System.out.println(displayId);
        System.out.println(expected);
        assertEquals(expected, displayId);
    }

    @Test
    public void testCanGenerateIdForBackupSiteOperationalModeOnlySerial() {

        String expected = EVENT_ID;

        AbstractHazardServicesEventIdUtil util = HazardServicesEventIdUtil
                .getInstance(false);
        util.setRequestService(requestService);
        util.setIdDisplayType(IdDisplayType.ONLY_SERIAL);
        String eventId = util.getNewEventID(BACKUP_SITE, LOCAL_SITE);
        String displayId = util.getDisplayId(eventId, LOCAL_SITE);

        System.out.println(displayId);
        System.out.println(expected);
        assertEquals(expected, displayId);
    }

    @Test
    public void testCanGenerateIdForLocalSiteOperationalModeProgOnDiffYear() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        int year = cal.get(Calendar.YEAR);

        String expected = 2015 + "-" + LOCAL_SITE + "-" + LOCAL_SITE + "-"
                + EVENT_ID;

        AbstractHazardServicesEventIdUtil util = HazardServicesEventIdUtil
                .getInstance(false);
        util.setRequestService(requestService);
        util.setIdDisplayType(IdDisplayType.PROG_ON_DIFF);
        String eventId = util.getNewEventID(LOCAL_SITE, LOCAL_SITE);

        // Change the year of the eventId so the prog diff works
        eventId = eventId.replace(year + "", "2015");
        String displayId = util.getDisplayId(eventId, LOCAL_SITE);
        System.out.println(displayId);
        System.out.println(expected);
        assertEquals(expected, displayId);
    }

    @Test
    public void testCanGenerateIdForLocalSiteOperationalModeProgOnDiffSiteSameYear() {

        String expected = LOCAL_SITE + "-" + LOCAL_SITE + "-" + EVENT_ID;

        AbstractHazardServicesEventIdUtil util = HazardServicesEventIdUtil
                .getInstance(false);
        util.setRequestService(requestService);
        util.setIdDisplayType(IdDisplayType.PROG_ON_DIFF);
        String eventId = util.getNewEventID(LOCAL_SITE, LOCAL_SITE);

        String displayId = util.getDisplayId(eventId, BACKUP_SITE);
        System.out.println(displayId);
        System.out.println(expected);
        assertEquals(expected, displayId);
    }

    @Test
    public void testCanGenerateIdForLocalSiteOperationalModeProgOnDiffYearSameSite() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        int year = cal.get(Calendar.YEAR);

        String expected = "2015-" + LOCAL_SITE + "-" + LOCAL_SITE + "-"
                + EVENT_ID;

        AbstractHazardServicesEventIdUtil util = HazardServicesEventIdUtil
                .getInstance(false);
        util.setRequestService(requestService);
        util.setIdDisplayType(IdDisplayType.PROG_ON_DIFF);
        String eventId = util.getNewEventID(LOCAL_SITE, LOCAL_SITE);

        // Change the year of the eventId so the prog diff works
        eventId = eventId.replace(year + "", "2015");
        String displayId = util.getDisplayId(eventId, LOCAL_SITE);
        System.out.println(displayId);
        System.out.println(expected);
        assertEquals(expected, displayId);
    }

    // Practice Mode
    @Test
    public void testCanGenerateIdForLocalSitePracticeModeAlwaysFull() {
        Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        int year = cal.get(Calendar.YEAR);

        String expected = "HZ-" + year + "-" + LOCAL_SITE + "-" + LOCAL_SITE
                + "-" + EVENT_ID;

        AbstractHazardServicesEventIdUtil util = HazardServicesEventIdUtil
                .getInstance(true);
        util.setRequestService(requestService);
        util.setIdDisplayType(IdDisplayType.ALWAYS_FULL);
        String eventId = util.getNewEventID(LOCAL_SITE, LOCAL_SITE);
        String displayId = util.getDisplayId(eventId, LOCAL_SITE);
        System.out.println(displayId);
        System.out.println(expected);
        assertEquals(expected, displayId);
    }
}
