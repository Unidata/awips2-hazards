package gov.noaa.gsd.viz.hazards.utilities;

import static gov.noaa.gsd.common.hazards.utilities.Utils.*;
import static org.junit.Assert.*;
import gov.noaa.gsd.common.hazards.utilities.Utils;

import java.util.Arrays;
import java.util.List;

import org.joda.time.DateTime;
import org.junit.Test;

/**
 * Description: Tests for {@link Utils}
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Apr 10, 2013            daniel.s.schaffer      Initial creation
 * 
 * </pre>
 * 
 * @author daniel.s.schaffer
 * @version 1.0
 */
public class UtilsTest {

    @Test
    public void basicSqueeze() {
        assertTrue(squeeze("hello  world").equals("hello world"));

    }

    @Test
    public void basicRemoveBlanks() {
        assertTrue(removeBlanks("hello  World ").equals("helloWorld"));
    }

    @Test
    public void basicDirName() {
        assertEquals(dirName("/top/middle/bottom"), "/top/middle");
    }

    @Test
    public void listToArray() {
        DateTime dt0 = new DateTime();
        DateTime dt1 = new DateTime();
        List<DateTime> dates = Arrays.asList(dt0, dt1);
        DateTime[] datesAsArray = Utils.listAsArray(dates);
        assertEquals(datesAsArray.length, 2);
        assertTrue(datesAsArray[0].equals(dt0));
    }

}
