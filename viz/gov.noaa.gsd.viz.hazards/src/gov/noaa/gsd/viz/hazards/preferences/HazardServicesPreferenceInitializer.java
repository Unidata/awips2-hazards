/**
 * This software was developed and / or modified by Raytheon Company,
 * pursuant to Contract DG133W-05-CQ-1067 with the US Government.
 * 
 * U.S. EXPORT CONTROLLED TECHNICAL DATA
 * This software product contains export-restricted data whose
 * export/transfer/disclosure is restricted by U.S. law. Dissemination
 * to non-U.S. persons whether in the United States or abroad requires
 * an export license or other authorization*.
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
package gov.noaa.gsd.viz.hazards.preferences;

import static com.raytheon.uf.common.dataplugin.events.hazards.registry.services.HazardServicesClient.ENCRYPTION_KEY;
import static com.raytheon.uf.common.dataplugin.events.hazards.registry.services.HazardServicesClient.PASSWORD;
import static com.raytheon.uf.common.dataplugin.events.hazards.registry.services.HazardServicesClient.REGISTRY_LOCATION;
import static com.raytheon.uf.common.dataplugin.events.hazards.registry.services.HazardServicesClient.TRUST_STORE_LOCATION;
import static com.raytheon.uf.common.dataplugin.events.hazards.registry.services.HazardServicesClient.TRUST_STORE_PASSWORD;
import static com.raytheon.uf.common.dataplugin.events.hazards.registry.services.HazardServicesClient.USER_NAME;
import gov.noaa.gsd.viz.hazards.display.HazardServicesActivator;

import java.io.File;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;

/**
 * Initializes the hazard services preference page
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 29, 2015 6895      Ben.Phillippe Refactored Hazard Service data access
 * 
 * </pre>
 * 
 * @author bphillip
 * @version 1.0
 */
public class HazardServicesPreferenceInitializer extends
        AbstractPreferenceInitializer {

    @Override
    public void initializeDefaultPreferences() {
        IPreferenceStore store = HazardServicesActivator.getDefault()
                .getPreferenceStore();
        store.setDefault(REGISTRY_LOCATION, "https://localhost:8082");
        store.setDefault(USER_NAME, "NCF");
        store.setDefault(PASSWORD, "password");
        store.setDefault(TRUST_STORE_LOCATION, getTrustStoreFilePath());
        store.setDefault(TRUST_STORE_PASSWORD, "changeit");
        store.setDefault(ENCRYPTION_KEY, "encrypt");
    }

    /**
     * Gets the path to the jre's cert store
     * 
     * @return The trust store path
     */
    private String getTrustStoreFilePath() {
        File file = new File("jssecacerts");
        if (!file.isFile()) {
            File dir = new File(
                    new File(System.getProperty("java.home"), "lib"),
                    "security");
            file = new File(dir, "jssecacerts");
            if (!file.isFile()) {
                file = new File(dir, "cacerts");
            }
        }
        return file.getPath();
    }
}
