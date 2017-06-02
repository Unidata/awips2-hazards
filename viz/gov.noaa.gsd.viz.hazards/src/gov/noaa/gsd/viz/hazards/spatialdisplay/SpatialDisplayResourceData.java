/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay;

import gov.noaa.gsd.viz.hazards.UIOriginator;
import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.EqualsBuilder;

import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.map.MapDescriptor;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.tools.GenericToolsResourceData;
import com.raytheon.uf.viz.hazards.sessionmanager.config.impl.ObservedSettings;

/**
 * Description: Spatial Display resource data, providing information about the
 * Hazard Services viz resource.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 05, 2013     585    Chris.Golden Initial creation
 * Dec 05, 2014    4124    Chris.Golden Changed to work with newly parameterized
 *                                      config manager and with ObservedSettings.
 * Jul 25, 2016   19537    Chris.Golden Moved app builder use from spatial display
 *                                      to here, since this class should be doing
 *                                      any interaction with the app builder.
 * Jun 22, 2017   15561    Chris.Golden Use the specified Descriptor when
 *                                      constructing a Spatial Display.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
@XmlType(name = "HazardServicesSpatialDisplayResourceData", factoryMethod = "newDeserializedInstance")
@XmlAccessorType(XmlAccessType.NONE)
public class SpatialDisplayResourceData extends
        GenericToolsResourceData<SpatialDisplay> {

    // Private Variables

    /**
     * Settings currently in use. A record must be kept here so that it is saved
     * and can be restored when loading from a bundle (deserialization).
     */
    @XmlElement
    private ObservedSettings settings;

    /**
     * Flag indicating whether or not this object has been brought into being
     * via deserialization.
     */
    private final transient boolean instantiatedViaDeserialization;

    /**
     * App builder; this will be <code>null</code> if Hazard Services has not
     * yet been constructed, but once an app builder comes into existence, a
     * reference to it is passed to this resource data object.
     */
    private transient HazardServicesAppBuilder appBuilder = null;

    // Public Static Methods

    /**
     * Create an instance for deserialization purposes.
     */
    public static SpatialDisplayResourceData newDeserializedInstance() {
        return new SpatialDisplayResourceData(true);
    }

    // Public Constructors

    /**
     * Construct a brand-new (not deserialized) standard instance.
     */
    public SpatialDisplayResourceData() {
        this(false);
    }

    // Private Constructors

    /**
     * Construct a standard instance.
     * 
     * @param instantiatedViaDeserialization
     *            Flag indicating whether or not this object is being
     *            instantiated as a result of deserialization of a previously
     *            serialized object.
     */
    private SpatialDisplayResourceData(boolean instantiatedViaDeserialization) {
        super(SpatialDisplay.LAYER_NAME, SpatialDisplay.class);
        this.instantiatedViaDeserialization = instantiatedViaDeserialization;
    }

    // Public Methods

    @Override
    public SpatialDisplay construct(LoadProperties loadProperties,
            IDescriptor descriptor) throws VizException {
        SpatialDisplay spatialDisplay = new SpatialDisplay(this, loadProperties);
        spatialDisplay.setDescriptor((MapDescriptor) descriptor);
        return spatialDisplay;
    }

    @Override
    public boolean equals(Object obj) {
        return EqualsBuilder.reflectionEquals(this, obj);
    }

    /**
     * Set the app builder.
     * 
     * @param appBuilder
     *            App builder.
     */
    public void setAppBuilder(HazardServicesAppBuilder appBuilder) {
        this.appBuilder = appBuilder;
    }

    /**
     * Get the current settings.
     * 
     * @return Current settings.
     */
    public ObservedSettings getSettings() {
        return settings;
    }

    /**
     * Set the current settings.
     * 
     * @param settings
     *            Current settings.
     */
    public void setSettings(ObservedSettings settings) {
        this.settings = settings;
    }

    // Package-Private Methods

    /**
     * Finish construction of the application.
     * 
     * @param spatialDisplay
     *            Spatial display that is the basis of this application.
     */
    void finishConstruction(SpatialDisplay spatialDisplay) {

        /*
         * Create the app builder if necessary.
         */
        if (appBuilder == null) {
            appBuilder = new HazardServicesAppBuilder(spatialDisplay);
            appBuilder.buildGUIs(instantiatedViaDeserialization);
        }

        /*
         * If the resource data has a setting to be used, use that; otherwise,
         * give the resource data the setting already in use by the app builder
         * so that it will have it in case it is saved as part of a bundle.
         */
        if (settings != null) {
            appBuilder.setCurrentSettings(settings,
                    UIOriginator.SPATIAL_DISPLAY);
        } else {
            ObservedSettings observedSettings = appBuilder.getCurrentSettings();
            setSettings(observedSettings);
        }
    }
}
