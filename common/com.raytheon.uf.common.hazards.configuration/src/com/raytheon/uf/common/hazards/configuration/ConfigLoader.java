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
package com.raytheon.uf.common.hazards.configuration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.xml.bind.JAXB;

import jep.Jep;
import jep.JepException;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;

import com.raytheon.uf.common.localization.FileLocker;
import com.raytheon.uf.common.localization.LocalizationFile;
import com.raytheon.uf.common.localization.exception.LocalizationException;
import com.raytheon.uf.common.status.IUFStatusHandler;
import com.raytheon.uf.common.status.UFStatus;
import com.raytheon.uf.common.status.UFStatus.Priority;

/**
 * This is the primary interface which allows asynchronous loading. At
 * initialization time these can be scheduled on a job pool but if they are
 * requested before the pool is available they will be loaded synchronously.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * 
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * May 21, 2013 1257       bsteffen    Initial creation
 * Aug 01, 2013 1325       daniel.s.schaffer@noaa.gov     Added support for alerting
 *                                                        by pulling it out of 
 *                                                        {@link SessionConfigurationManager}
 *                                                        Also, now loading XML using the file-based
 *                                                        JAXB unmarshal method 
 *                                                        unmarshal method that uses a file
 * Apr 28, 2014 3556       bkowal       Relocate to a common plugin.
 * Feb 24, 2015 6605       mpduff       Changed how loadJson reads files.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class ConfigLoader<T> implements Runnable {

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ConfigLoader.class);

    private final LocalizationFile lfile;

    private final Class<T> clazz;

    private final String pyVarName;

    private final String pyIncludes;

    private T config;

    public ConfigLoader(LocalizationFile lfile, Class<T> clazz) {
        this(lfile, clazz, null);
    }

    public ConfigLoader(LocalizationFile lfile, Class<T> clazz, String pyVarName) {
        this(lfile, clazz, pyVarName, null);
    }

    public ConfigLoader(LocalizationFile lfile, Class<T> clazz,
            String pyVarName, String pyIncludes) {
        this.lfile = lfile;
        this.clazz = clazz;
        this.pyVarName = pyVarName;
        this.pyIncludes = pyIncludes;
    }

    @Override
    public void run() {
        synchronized (this) {
            if (config != null) {
                return;
            }
            File file = lfile.getFile();
            String ext = file.getName().substring(
                    file.getName().lastIndexOf('.'));
            try {
                if (ext.equals(".py")) {
                    this.config = loadPython();
                } else if (ext.equals(".json")) {
                    this.config = loadJson();
                } else if (ext.equals(".xml")) {
                    this.config = loadXml();
                } else {
                    throw new UnsupportedOperationException(
                            "Cannot load config file " + lfile.getName());
                }
            } catch (Throwable e) {
                statusHandler
                        .handle(Priority.PROBLEM,
                                "Unable to load configuration from: "
                                        + lfile.getName(), e);
            }
            if (this.config == null) {
                try {
                    config = clazz.newInstance();
                } catch (Exception e) {
                    statusHandler.handle(Priority.PROBLEM,
                            e.getLocalizedMessage(), e);
                }
            }
        }
    }

    private T loadJson() throws LocalizationException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(
                DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        StringBuilder sb = new StringBuilder();
        char[] buffer = new char[1024];
        try (InputStream is = lfile.openInputStream()) {
            try (InputStreamReader isr = new InputStreamReader(is)) {
                int read = isr.read(buffer);
                while (read > -1) {
                    sb.append(buffer, 0, read);
                    read = isr.read(buffer);
                }
                return mapper.readValue(sb.toString(), clazz);
            }
        }
    }

    private T loadXml() throws LocalizationException {
        File file = lfile.getFile();
        FileLocker.lock(this, file, FileLocker.Type.READ);
        T result = JAXB.unmarshal(file, clazz);
        FileLocker.unlock(this, file);
        return result;
    }

    private T loadPython() throws JepException, IOException {
        File file = lfile.getFile();
        String varName = pyVarName;
        if (varName == null) {
            varName = file.getName().replaceFirst("[.][^.]+$", "");
        }
        // TODO use incremental python override and make sure localization
        // importing is being used.
        Jep jep = new Jep(false, pyIncludes);
        jep.runScript(file.getAbsolutePath());
        jep.eval("import json");
        String json = (String) jep.getValue("json.dumps(" + varName + ")");
        jep.close();
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(
                DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return mapper.readValue(json, clazz);

    }

    public T getConfig() {
        if (config == null) {
            run();
        }
        return config;
    }
}
