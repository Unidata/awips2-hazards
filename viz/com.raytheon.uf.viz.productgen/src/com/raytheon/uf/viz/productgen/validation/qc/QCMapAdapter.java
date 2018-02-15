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
package com.raytheon.uf.viz.productgen.validation.qc;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * Imported and integrated from Warn Gen: com.raytheon.viz.texteditor
 * 
 * AWIPS2_baseline/cave/com.raytheon.viz.texteditor/src/com/raytheon
 * /viz/texteditor/qc
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Sep 29, 2015 6617       Chris.Cody  Initial Import. Integrate WarnGen Product Validation.
 * </pre>
 * 
 * @author rferrel
 * @version 1.0
 */
public class QCMapAdapter
        extends XmlAdapter<QCMapElements[], Map<String, String>> {

    @Override
    public Map<String, String> unmarshal(QCMapElements[] v) throws Exception {
        Map<String, String> qcMap = new HashMap<String, String>();
        for (QCMapElements element : v) {
            qcMap.put(element.key, element.value);
        }
        return qcMap;
    }

    @Override
    public QCMapElements[] marshal(Map<String, String> v) throws Exception {
        int index = 0;
        QCMapElements[] qcMapElements = new QCMapElements[v.size()];
        for (Map.Entry<String, String> entry : v.entrySet()) {
            qcMapElements[index++] = new QCMapElements(entry.getKey(),
                    entry.getValue());

        }
        return qcMapElements;
    }
}
