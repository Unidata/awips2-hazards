package gov.noaa.gsd.viz.hazards
import static org.junit.Assert.*
import gov.noaa.gsd.viz.hazards.utilities.HazardEventsBuilderForTesting
import gov.noaa.gsd.viz.hazards.utilities.Utilities

import org.joda.time.DateTime

import spock.lang.*

import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.HazardStatus
import com.raytheon.uf.common.dataplugin.events.hazards.HazardConstants.ProductClass
import com.raytheon.uf.common.dataplugin.events.hazards.event.IHazardEvent
import com.vividsolutions.jts.geom.Polygon

/**
 *
 * Description: Test of {@link HazardEventsBuilder}
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Mar 13, 2013            daniel.s.schaffer      Initial creation
 * Dec 3, 2013  1472       bkowal      subtype is now subType
 *
 * </pre>
 *
 * @author daniel.s.schaffer
 * @version 1.0
 */
class HazardEventsBuilderTest extends spock.lang.Specification {


    def setup() {
    }


    def "Basic" () {
        when: "Build from JSON"
        String eventsAsJson = Utilities.getCannedEventsAsJSON()
        HazardEventsBuilderForTesting builder = new HazardEventsBuilderForTesting(eventsAsJson)
        List<IHazardEvent> events = builder.getEvents()
        IHazardEvent anEvent = null;
        for (IHazardEvent event : events) {
            if (event.getEventID() == "11") {
                anEvent = event
                break
            }
        }
        Date issueTime = new DateTime(1297137600000L).toDate()
        Date startTime = issueTime
        Date endTime = new DateTime(1297162800000L).toDate()
        Map<String, Serializable> attributes = anEvent.getHazardAttributes()
        List<String> callsToAction = attributes.get("cta")
        Polygon geometry = anEvent.getGeometry()


        then: "Number of events is correct"
        events.size() == 16

        and: "A particular event is correct"
        anEvent.getSiteID() == "BOU"
        anEvent.getStatus() == HazardStatus.ISSUED
        anEvent.getPhenomenon() == "FF"
        anEvent.getSignificance() == "W"
        anEvent.getSubType() == "Convective"
        anEvent.getStartTime() == startTime
        anEvent.getEndTime() == endTime
        anEvent.getCreationTime() == issueTime
        anEvent.getHazardMode() == ProductClass.OPERATIONAL
        geometry.getNumPoints() == 5
    }
}
