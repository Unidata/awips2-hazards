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
package gov.noaa.gsd.viz.hazards.utilities;

import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import org.junit.Test;

import com.raytheon.uf.viz.productgen.validation.util.VtecObject;

/**
 * JUnit test for the ProductParserUtil class.
 * 
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jan 27, 2017 22308      Robert.Blum Initial creation
 *
 * </pre>
 *
 * @author Robert.Blum
 */

public class ProductParserUtilTest {

    private final String FFA_PRODUCT = "WGUS63 KOAX 241715\n" + "FFAOAX\n"
            + "\n" + "URGENT - IMMEDIATE BROADCAST REQUESTED\n"
            + "FLOOD WATCH\n" + "NATIONAL WEATHER SERVICE OMAHA/VALLEY NE\n"
            + "1115 AM CST TUE JAN 24 2017\n" + "\n"
            + "NEZ017-018-031-032-250115-\n"
            + "/O.NEW.KOAX.FF.A.0003.170124T1715Z-170125T0200Z/\n"
            + "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/\n"
            + "PIERCE-WAYNE-MADISON-STANTON-\n"
            + "INCLUDING THE CITIES OF PIERCE, STANTON, OSMOND, WAYNE, PLAINVIEW, \n"
            + "AND NORFOLK\n" + "1115 AM CST TUE JAN 24 2017\n" + "\n"
            + "...FLASH FLOOD WATCH IN EFFECT UNTIL 8 PM CST THIS EVENING...\n"
            + "\n"
            + "THE NATIONAL WEATHER SERVICE IN OMAHA/VALLEY HAS ISSUED A\n"
            + "\n"
            + "* FLASH FLOOD WATCH FOR A PORTION OF NORTHEAST NEBRASKA, INCLUDING \n"
            + "  THE FOLLOWING AREAS, MADISON, PIERCE, STANTON AND WAYNE.\n"
            + "\n" + "* UNTIL 8 PM CST THIS EVENING\n" + "\n"
            + "* |* CURRENT HYDROMETEOROLOGICAL BASIS\n" + "\n"
            + "* |* CURRENT HYDROMETEOROLOGICAL IMPACTS\n" + "\n" + "\n"
            + "$$\n" + "\n" + "USERNAME";

    private final String FFW_PRODUCT = "WGUS53 KOAX 201520\n" + "FFWOAX\n"
            + "NEC155-202315-\n"
            + "/O.NEW.KOAX.FF.W.0003.170120T1520Z-170120T2315Z/\n"
            + "/00000.0.ER.000000T0000Z.000000T0000Z.000000T0000Z.OO/\n" + "\n"
            + "BULLETIN - EAS ACTIVATION REQUESTED\n" + "FLASH FLOOD WARNING\n"
            + "NATIONAL WEATHER SERVICE OMAHA/VALLEY NE\n"
            + "920 AM CST FRI JAN 20 2017\n" + "\n"
            + "THE NATIONAL WEATHER SERVICE IN OMAHA/VALLEY HAS ISSUED A\n"
            + "\n" + "* FLASH FLOOD WARNING FOR...\n"
            + "  CENTRAL SAUNDERS COUNTY IN EAST CENTRAL NEBRASKA...\n" + "\n"
            + "* UNTIL 515 PM CST FRIDAY\n" + "\n"
            + "* AT 920 AM CST, DOPPLER RADAR INDICATED THUNDERSTORMS PRODUCING \n"
            + "  HEAVY RAIN ACROSS THE WARNED AREA. FLASH FLOODING IS EXPECTED TO \n"
            + "  BEGIN SHORTLY.\n" + "\n"
            + "* SOME LOCATIONS THAT WILL EXPERIENCE FLASH FLOODING INCLUDE...\n"
            + "  WAHOO, WESTON, ITHACA, MALMO AND COLON.\n" + "\n"
            + "LAT...LON 4131 9659 4115 9652 4114 9653 4113 9655\n"
            + "      4113 9669 4114 9671 4114 9675 4115 9677\n"
            + "      4116 9678 4118 9679 4125 9679 4126 9678\n"
            + "      4128 9678 4129 9677 4131 9673\n" + "\n" + "\n" + "$$\n"
            + "\n" + "USERNAME";

    private final String FLW_PRODUCT = "WGUS43 KOAX 242111\n" + "FLWOAX\n"
            + "\n" + "BULLETIN - EAS ACTIVATION REQUESTED\n" + "FLOOD WARNING\n"
            + "NATIONAL WEATHER SERVICE OMAHA/VALLEY NE\n"
            + "311 PM CST TUE JAN 24 2017\n" + "\n"
            + "...THE NATIONAL WEATHER SERVICE IN OMAHA/VALLEY NE HAS ISSUED A \n"
            + "FLOOD WARNING FOR THE FOLLOWING RIVERS IN NEBRASKA...IOWA...\n"
            + "\n"
            + "  MISSOURI RIVER NEAR DECATUR AFFECTING BURT, MONONA AND THURSTON \n"
            + "  COUNTIES.\n" + "\n" + "PRECAUTIONARY/PREPAREDNESS ACTIONS...\n"
            + "\n"
            + "STAY TUNED TO FURTHER DEVELOPMENTS BY LISTENING TO YOUR LOCAL RADIO, \n"
            + "TELEVISION, OR NOAA WEATHER RADIO FOR FURTHER INFORMATION.\n"
            + "\n" + "ADDITIONAL INFORMATION IS AVAILABLE AT WWW.WEATHER.GOV.\n"
            + "\n"
            + "THE NEXT STATEMENT WILL BE ISSUED LATE TONIGHT AT 315 AM CST.\n"
            + "\n" + "&&\n" + "\n" + "\n" + "IAC133-NEC021-173-250915-\n"
            + "/O.NEW.KOAX.FL.W.0019.170124T2111Z-000000T0000Z/\n"
            + "/DCTN1.0.ER.000000T0000Z.170124T2100Z.000000T0000Z.NO/\n"
            + "311 PM CST TUE JAN 24 2017\n" + "\n"
            + "THE NATIONAL WEATHER SERVICE IN OMAHA/VALLEY HAS ISSUED A\n"
            + "\n" + "* FLOOD WARNING FOR\n"
            + "  THE MISSOURI RIVER NEAR DECATUR.\n"
            + "* FROM THIS AFTERNOON UNTIL FURTHER NOTICE.\n"
            + "* AT 300 PM CST TUESDAY THE STAGE WAS 18.3 FEET.\n"
            + "* FLOOD STAGE IS 35.0 FEET.\n"
            + "* FLOODING IS OCCURRING AND FLOODING IS FORECAST.\n"
            + "* FORECAST...A FORECAST IS NOT AVAILABLE AT THIS TIME. THIS WARNING \n"
            + "  WILL REMAIN IN EFFECT UNTIL THE RIVER FALLS BELOW FLOOD STAGE.\n"
            + "* FLOOD HISTORY...NO AVAILABLE FLOOD HISTORY AVAILABLE.\n" + "\n"
            + "&&\n" + "\n" + "\n"
            + "LAT...LON 4228 9643 4228 9626 4203 9594 4180 9595\n"
            + "      4180 9624\n" + "\n" + "\n" + "$$\n" + "\n" + "USERNAME";

    private final String MULTI_SEGMENT_FLW = "WGUS43 KOAX 170219\n" + "FLWOAX\n"
            + "BULLETIN - IMMEDIATE BROADCAST REQUESTED\n" + "FLOOD WARNING\n"
            + "NATIONAL WEATHER SERVICE OMAHA/VALLEY NEBRASKA\n"
            + "919 PM CDT FRI SEP 16 2016\n" + "\n"
            + "  Missouri River At Nebraska City affecting Fremont and Otoe \n"
            + "  Counties.\n" + "\n"
            + "  Missouri River At Brownville affecting Atchison and Nemaha \n"
            + "  Counties.\n" + "\n"
            + "  Missouri River At Rulo affecting Holt and Richardson Counties.\n"
            + "\n" + "\n" + "PRECAUTIONARY/PREPAREDNESS ACTIONS...\n" + "\n"
            + "Do not drive cars through areas where water covers the road.  The \n"
            + "water depth may be too great to allow your vehicle to pass safely. \n"
            + "Turn around...don't drown!\n" + "\n"
            + "Additional information is available at:\n"
            + "http://www.water.weather.gov/ahps2/index.php?wfo=oax\n" + "\n"
            + "&&\n" + "\n" + "IAC071-NEC131-171419-\n"
            + "/O.EXT.KOAX.FL.W.0054.160918T0600Z-160918T1200Z/\n"
            + "/NEBN1.1.ER.160918T0600Z.160918T0600Z.160918T0600Z.NO/\n"
            + "919 PM CDT FRI SEP 16 2016\n" + "\n"
            + "The Flood Warning continues for \n"
            + " the Missouri River At Nebraska City.\n"
            + "* from late Saturday night to Sunday morning...or until the warning \n"
            + "  is cancelled.\n"
            + "* At  9:00 PM Friday the stage was 13.1 feet...or 4.9 feet below \n"
            + "  flood stage.\n" + "* Flood stage is 18.0 feet.\n"
            + "* Minor flooding is forecast.\n"
            + "* Forecast...the river is expected to rise to near flood stage early \n"
            + "  Sunday morning.\n"
            + "* Impact...at 18.0 feet...Widespread lowland flooding begins.\n"
            + "\n" + "&&\n" + "\n"
            + "LAT...LON 4078 9587 4078 9572 4048 9554 4048 9580\n"
            + "      4074 9590                       \n" + "\n" + "$$\n" + "\n"
            + "MOC005-NEC127-171419-\n"
            + "/O.EXT.KOAX.FL.W.0055.160917T2154Z-160919T1030Z/\n"
            + "/BRON1.1.ER.160917T2154Z.160918T1200Z.160919T0430Z.NO/\n"
            + "919 PM CDT FRI SEP 16 2016\n" + "\n"
            + "The Flood Warning continues for \n"
            + " the Missouri River At Brownville.\n"
            + "* from Saturday afternoon to late Sunday night...or until the warning\n"
            + "  is cancelled.\n"
            + "* At  8:00 PM Friday the stage was 26.9 feet...or 6.1 feet below \n"
            + "  flood stage.\n" + "* Flood stage is 33.0 feet.\n"
            + "* Minor flooding is forecast.\n"
            + "* Forecast...rise above flood stage by tomorrow afternoon and \n"
            + "  continue to rise to near 35.5 feet by Sunday morning the river will\n"
            + "  fall below flood stage by Sunday before midnight.\n" + "\n"
            + "\n" + "&&\n" + "\n"
            + "LAT...LON 4048 9580 4048 9554 4026 9544 4026 9563\n"
            + "      4044 9574                       \n" + "\n" + "$$\n" + "\n"
            + "MOC087-NEC147-171419-\n"
            + "/O.EXT.KOAX.FL.W.0056.160918T1130Z-160919T1030Z/\n"
            + "/RULN1.1.ER.160918T1130Z.160918T1800Z.160919T0430Z.NO/\n"
            + "919 PM CDT FRI SEP 16 2016\n" + "\n"
            + "The Flood Warning continues for \n"
            + " the Missouri River At Rulo.\n"
            + "* from Sunday morning to late Sunday night...or until the warning is \n"
            + "  cancelled.\n"
            + "* At  8:30 PM Friday the stage was 9.8 feet...or 7.2 feet below flood\n"
            + "  stage.\n" + "* Flood stage is 17.0 feet.\n"
            + "* Minor flooding is forecast.\n"
            + "* Forecast...rise above flood stage by Sunday morning and continue to\n"
            + "  rise to near 17.8 feet by early Sunday afternoon the river will \n"
            + "  fall below flood stage by Sunday before midnight.\n"
            + "* Impact...at 17.0 feet...Agricultural lowlands along both sides of \n"
            + "  the river begin to flood.\n" + "\n" + "&&\n" + "\n"
            + "LAT...LON 4026 9563 4026 9544 3991 9501 3982 9503\n"
            + "      3997 9536                       \n" + "\n" + "$$";

    @Test
    public void testParseVTEC() {
        List<VtecObject> vtecs = ProductParserUtil
                .getVTECsFromProduct(FLW_PRODUCT);

        assertTrue("Checking correct number of vtecs", vtecs.size() == 1);

        VtecObject vtec = vtecs.get(0);
        assertTrue("Checking correct vtec format",
                vtec.getProduct().equals("O"));
        assertTrue("Checking correct vtec action",
                vtec.getAction().equals("NEW"));
        assertTrue("Checking correct vtec site",
                vtec.getOffice().equals("KOAX"));
        assertTrue("Checking correct vtec phensig",
                vtec.getPhensig().equals("FL.W"));
        assertTrue("Checking correct vtec etn", vtec.getSequence() == 19);
    }

    @Test
    public void testParseMultipleVTEC() {
        List<VtecObject> vtecs = ProductParserUtil
                .getVTECsFromProduct(MULTI_SEGMENT_FLW);

        assertTrue("Checking correct number of vtecs", vtecs.size() == 3);
        assertTrue("Checking correct vtec order",
                vtecs.get(0).getSequence() == 54);
        assertTrue("Checking correct vtec order",
                vtecs.get(1).getSequence() == 55);
        assertTrue("Checking correct vtec order",
                vtecs.get(2).getSequence() == 56);
    }

    @Test
    public void testParseIssueTime() {
        Date issueTime = ProductParserUtil.getIssueTimeFromProduct(FFA_PRODUCT);
        assertTrue("Checking correct issueTime",
                issueTime.getTime() == 1485278100000L);
    }

    @Test
    public void testParseIssueTimeLeadingZeroDropped() {
        Date issueTime = ProductParserUtil.getIssueTimeFromProduct(FFW_PRODUCT);
        System.out.println(issueTime.getTime());
        assertTrue("Checking correct issueTime",
                issueTime.getTime() == 1484925600000L);
    }
}
