/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.config.types;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBException;

import org.junit.Test;

import com.google.common.collect.Lists;
import com.raytheon.uf.common.serialization.JAXBManager;

/**
 * Description: Test of serialization of {@link Settings} and dependent classes.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Feb 07, 2014  2182      daniel.s.schaffer@noaa.gov      Initial creation
 * Dec 05, 2014  2124      Chris.Golden     Changed to use ISettings.
 * </pre>
 * 
 * @author daniel.s.schaffer@noaa.gov
 * @version 1.0
 */
public class SettingsJaxbTest {

    @Test
    public void basic() {
        try {
            ISettings settings = new Settings();
            settings.setSettingsID("id0");
            Set<String> visibleTypes = new HashSet<>();
            visibleTypes.add("vt0");
            visibleTypes.add("vt1");
            settings.setVisibleTypes(visibleTypes);
            Tool tool = new Tool();
            tool.setToolName("riverFlood");
            settings.setToolbarTools(Lists.newArrayList(tool));
            MapCenter mapCenter = new MapCenter(2.0, 3.0, 4.0);
            settings.setMapCenter(mapCenter);

            Column column = new Column("type0", "fieldName0", "sortDir0");
            Map<String, Column> columns = new HashMap<>();
            columns.put("column0", column);
            settings.setColumns(columns);

            settings.setAddToSelected(true);
            JAXBManager manager = new JAXBManager(Settings.class);
            String xml = manager.marshalToXml(settings);
            ISettings newSettings = (ISettings) manager.unmarshalFromXml(xml);
            assertEquals(settings, newSettings);
            String newXml = manager.marshalToXml(newSettings);
            assertEquals(xml, newXml);

        } catch (JAXBException e) {
            fail(e.toString());
            ;
        }
    }

}
