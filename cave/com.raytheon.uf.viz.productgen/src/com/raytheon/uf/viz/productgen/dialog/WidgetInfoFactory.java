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
package com.raytheon.uf.viz.productgen.dialog;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Prepares the data in the format expected by the ParametersEditorFactory.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Dec 16, 2013            jsanchez     Initial creation
 * 
 * </pre>
 * 
 * @author jsanchez
 * @version 1.0
 */

public class WidgetInfoFactory {

    private static Map<String, WidgetInfo> map = new HashMap<String, WidgetInfo>();

    private WidgetInfoFactory() {

    }

    /**
     * Prepares the data in the format expected by the ParametersEditorFactory.
     * 
     * @param data
     * @return
     */
    public static Map<String, WidgetInfo> createWidgetInfoMap(
            Map<String, Serializable> data) {
        map.clear();
        addInputs(null, data, false);
        return map;
    }

    private static void addInputs(List<String> parentPath,
            Map<String, Serializable> data, boolean isParentEditable) {

        if (data != null) {
            for (Entry<String, Serializable> entry : data.entrySet()) {
                boolean isEditable = entry.getKey().contains(
                        ProductGenerationDialogUtility.EDITABLE)
                        || isParentEditable;
                String key = entry.getKey().replace(
                        ProductGenerationDialogUtility.EDITABLE, "");
                if (entry.getValue() instanceof Map<?, ?>) {
                    Map<String, Serializable> subdata = (Map<String, Serializable>) entry
                            .getValue();
                    List<String> path = new ArrayList<String>();
                    if (parentPath != null) {
                        path.addAll(parentPath);
                    }
                    path.add(entry.getKey());
                    addInputs(path, subdata, isEditable);
                } else if (entry.getValue() instanceof ArrayList) {
                    ArrayList<?> list = (ArrayList<?>) entry.getValue();
                    if (list != null && !list.isEmpty()) {
                        Object firstItem = list.get(0);
                        if (firstItem instanceof Map<?, ?>) {
                            List<String> path = new ArrayList<String>();
                            if (parentPath != null) {
                                path.addAll(parentPath);
                            }
                            path.add(entry.getKey());
                            for (Object item : list) {
                                addInputs(path,
                                        (Map<String, Serializable>) item,
                                        isEditable);
                            }
                        } else if (isEditable) {
                            WidgetInfo info = new WidgetInfo(key, parentPath,
                                    entry.getValue());
                            map.put(info.getLabel(), info);
                        }
                    }
                } else if (isEditable) {
                    WidgetInfo info = new WidgetInfo(key, parentPath,
                            entry.getValue());
                    map.put(info.getLabel(), info);
                }
            }
        }
    }

}
