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
import java.util.Map;

import javax.xml.bind.JAXB;

import jep.Jep;
import jep.JepException;
import jep.NamingConventionClassEnquirer;

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
 * Aug 31, 2015 9757       Robert.Blum  Removed TODO since we dont want to incrementally override non-class
 *                                      based config files.
 * Apr 25, 2016 17611      Robert.Blum  Implemented incremental overrides for python files.
 * 
 * </pre>
 * 
 * @author bsteffen
 * @version 1.0
 */
public class ConfigLoader<T> implements Runnable {

    private static final transient IUFStatusHandler statusHandler = UFStatus
            .getHandler(ConfigLoader.class);

    private static final String INCREMENTAL_OVERRIDE = "incrementalOverride";

    private static final String DATA_TYPE = "dataType";

    private static final String SITE = "site";

    private final LocalizationFile lfile;

    private final Class<T> clazz;

    private final String pyVarName;

    private final String pyIncludes;

    private final Map<String, Object> parameters;

    private T config;

    public ConfigLoader(LocalizationFile lfile, Class<T> clazz) {
        this(lfile, clazz, null);
    }

    public ConfigLoader(LocalizationFile lfile, Class<T> clazz, String pyVarName) {
        this(lfile, clazz, pyVarName, null);
    }

    public ConfigLoader(LocalizationFile lfile, Class<T> clazz,
            String pyVarName, String pyIncludes) {
        this(lfile, clazz, pyVarName, pyIncludes, null);
    }

    public ConfigLoader(LocalizationFile lfile, Class<T> clazz,
            String pyVarName, String pyIncludes, Map<String, Object> parameters) {
        this.lfile = lfile;
        this.clazz = clazz;
        this.pyVarName = pyVarName;
        this.pyIncludes = pyIncludes;
        this.parameters = parameters;
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
        String fileName = file.getName();
        String configDir = lfile.getName().replace(fileName, "");
        fileName = fileName.replaceFirst("[.][^.]+$", "");
        String varName = pyVarName;
        if (varName == null) {
            varName = fileName;
        }
        Jep jep = new Jep(false, pyIncludes,
                ConfigLoader.class.getClassLoader(),
                new NamingConventionClassEnquirer());
        jep.eval("import json");

        if (parameters != null) {
            Boolean incrementalOverride = (Boolean) parameters
                    .get(INCREMENTAL_OVERRIDE);
            if (incrementalOverride != null && incrementalOverride) {
                String dataType = (String) parameters.get(DATA_TYPE);
                if (dataType == null) {
                    statusHandler
                            .error("ConfigLoader: Error no data type specified.");
                    jep.close();
                    return null;
                }

                String site = (String) parameters.get(SITE);
                if (site == null) {
                    site = "None";
                }

                jep.eval("from Bridge import Bridge");
                jep.eval("criteria = {'dataType':'" + dataType
                        + "', 'filter': {'name': '" + fileName + "'}, 'site':'"
                        + site + "', 'configDir':'" + configDir + "'}");

                jep.eval("bridge = Bridge()");
                jep.eval(varName
                        + " = bridge.getConfigFile(json.dumps(criteria))");
            } else {
                jep.runScript(file.getAbsolutePath());
            }
        } else {
            jep.runScript(file.getAbsolutePath());
        }

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
