package com.raytheon.uf.common.dataplugin.events.hazards.registry.xmladapters;

import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

//TODO: This class is just a stub and will be completed into a subsequent change set
public class ActiveTableXmlAdapter extends XmlAdapter<String,  List<Map<String, Object>> > {



    @Override
    public List<Map<String, Object>> unmarshal(String v) throws Exception {
        return null;
    }

    @Override
    public String marshal(List<Map<String, Object>> v) throws Exception {
        return null;
    }

}
