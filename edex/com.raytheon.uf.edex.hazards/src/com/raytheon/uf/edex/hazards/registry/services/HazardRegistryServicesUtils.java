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
package com.raytheon.uf.edex.hazards.registry.services;

import java.util.ArrayList;
import java.util.List;

import com.raytheon.uf.common.registry.annotations.RegistryObject;
import com.raytheon.uf.common.registry.ebxml.RegistryUtil;

/**
 *
 * Utility methods for the Hazard Services registry.
 *
 * <pre>
 *
 * SOFTWARE HISTORY
 *
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jun 29, 2017 35633      Kevin.Bisanz Initial creation
 *
 * </pre>
 *
 * @author kbisanz
 */
public class HazardRegistryServicesUtils {
    public static String NO_REGISTRY_ID = "NoRegistryId";

    /**
     *
     * Returns the registry object id for this object.
     *
     * @param registryObject
     *            Object with {@link RegistryObject} annotation.
     * @return The object's registry object id or {@link #NO_REGISTRY_ID} if it
     *         cannot be determined.
     */
    public static String getRegistryId(Object registryObject) {
        String id = RegistryUtil.getRegistryObjectKey(registryObject);
        if (id == null || id.isEmpty()) {
            id = NO_REGISTRY_ID;
        }
        return id;
    }

    /**
     *
     * Returns the registry object ids for these objects.
     *
     * @param registryObjects
     *            List of objects with {@link RegistryObject} annotations.
     * @return List containing the object's registry object id or
     *         {@link #NO_REGISTRY_ID} if it cannot be determined.
     */
    public static String getRegistryId(List<? extends Object> registryObjects) {
        List<String> ids = new ArrayList<>(registryObjects.size());
        for (Object registryObject : registryObjects) {
            ids.add(getRegistryId(registryObject));
        }
        return ids.toString();
    }
}
