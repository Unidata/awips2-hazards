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

import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import com.raytheon.uf.common.dataplugin.events.hazards.registry.services.HazardServicesClient;
import com.raytheon.uf.common.security.encryption.AESEncryptor;

/**
 * The hazard services preference page. This preference page allows the user to
 * input the credentials necessary for communicating with the registry
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
public class HazardServicesPreferencePage extends FieldEditorPreferencePage
        implements IWorkbenchPreferencePage {

    /** The preference store */
    private final IPreferenceStore store = HazardServicesActivator.getDefault()
            .getPreferenceStore();

    /** Field holding the address of the registry */
    private StringFieldEditor registryAddressField;

    /** Field holding the user name */
    private StringFieldEditor userNameField;

    /** Field holding the password */
    private EncryptedStringFieldEditor passwordField;

    /** Field holding the trust store location */
    private StringFieldEditor trustStoreLocationField;

    /** Field holding the trust store password */
    private EncryptedStringFieldEditor trustStorePasswordField;

    /** Field holding the encryption key used to encrypt passwords */
    private StringFieldEditor encryptionKeyField;

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(store);
        setDescription("Hazard Services Preferences");

    }

    @Override
    protected void createFieldEditors() {
        Composite composite = getFieldEditorParent();
        registryAddressField = new StringFieldEditor(REGISTRY_LOCATION,
                "Registry URL: ", composite);
        userNameField = new StringFieldEditor(USER_NAME, "User Name: ",
                composite);
        passwordField = new EncryptedStringFieldEditor(PASSWORD,
                "User Password: ", composite);
        trustStoreLocationField = new StringFieldEditor(TRUST_STORE_LOCATION,
                "Truststore Path:", composite);
        trustStorePasswordField = new EncryptedStringFieldEditor(
                TRUST_STORE_PASSWORD, "Truststore Password:", composite);
        encryptionKeyField = new StringFieldEditor(ENCRYPTION_KEY,
                "Encryption Key:", composite);
        Button b = new Button(composite, SWT.PUSH);
        GridData gd = new GridData(SWT.RIGHT, SWT.TOP, false, true);
        gd.horizontalSpan = 2;
        b.setLayoutData(gd);
        b.setText("Check Connectivity");
        b.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                performOk();
            }
        });
        addField(registryAddressField);
        addField(userNameField);
        addField(passwordField);
        addField(trustStoreLocationField);
        addField(trustStorePasswordField);
        addField(encryptionKeyField);

    }

    @Override
    public boolean performOk() {
        boolean result = false;
        final String encryptionKey = encryptionKeyField.getStringValue();
        passwordField.setEncryptionKey(encryptionKey);
        trustStorePasswordField.setEncryptionKey(encryptionKey);
        try {
            result = super.performOk();
            HazardServicesClient.init(store.getString(REGISTRY_LOCATION),
                    store.getString(USER_NAME), store.getString(PASSWORD),
                    store.getString(TRUST_STORE_LOCATION),
                    store.getString(TRUST_STORE_PASSWORD),
                    store.getString(ENCRYPTION_KEY));
            HazardServicesClient.checkConnectivity();
            MessageBox success = new MessageBox(getShell(), SWT.ICON_WARNING
                    | SWT.OK);
            success.setText("Registry Connection Established");
            success.setMessage("Successfully established connection with registry at "
                    + registryAddressField.getStringValue());
            success.open();
        } catch (Exception e) {
            e.printStackTrace();
            MessageBox warning = new MessageBox(getShell(), SWT.ICON_WARNING
                    | SWT.OK);
            warning.setText("Registry Connection Error");
            warning.setMessage(e.getLocalizedMessage());
            warning.open();
            result = false;
        }

        return result;
    }

    private class EncryptedStringFieldEditor extends StringFieldEditor {

        private String encryptionKey;

        public EncryptedStringFieldEditor(String name, String labelText,
                Composite parent) {
            super(name, labelText, parent);
        }

        @Override
        protected void doStore() {
            try {
                getPreferenceStore().setValue(
                        getPreferenceName(),
                        new AESEncryptor().encrypt(encryptionKey,
                                getStringValue()));
            } catch (Exception e) {
                throw new RuntimeException(
                        "Error encrypting keystore passwords");
            }
        }

        @Override
        protected void doLoad() {
            try {
                String value = new AESEncryptor().decrypt(getPreferenceStore()
                        .getString(ENCRYPTION_KEY), getPreferenceStore()
                        .getString(getPreferenceName()));
                setStringValue(value);
                oldValue = value;
            } catch (Exception e) {
                throw new RuntimeException(
                        "Error decrypting keystore passwords", e);
            }
        }

        /**
         * @param encryptionKey
         *            the encryptionKey to set
         */
        public void setEncryptionKey(String encryptionKey) {
            this.encryptionKey = encryptionKey;
        }
    }
}
