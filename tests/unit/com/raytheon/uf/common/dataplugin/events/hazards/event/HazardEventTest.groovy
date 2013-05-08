package com.raytheon.uf.common.dataplugin.events.hazards.event;

import static org.junit.Assert.*;

import com.google.common.collect.Maps
import spock.lang.*


class HazardEventTest extends spock.lang.Specification {

    @Ignore
    def "Attributes" () {
        when: "Set Attributes"
        HazardEvent hazardEvent = new HazardEvent();
        Map<String, Serializable> attrs = Maps.newHashMap();
        hazardEvent.addHazardAttribute("key", "value")
        Map<String, Serializable> returnedAttrs = hazardEvent.getHazardAttributes();

        then:
        returnedAttrs.size() == 1;
        returnedAttrs.get("key").equals("value");
    }
}
