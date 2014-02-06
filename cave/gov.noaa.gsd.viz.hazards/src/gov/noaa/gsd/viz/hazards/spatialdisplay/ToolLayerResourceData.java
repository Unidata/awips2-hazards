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

import gov.noaa.gsd.viz.hazards.display.HazardServicesAppBuilder;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.EqualsBuilder;

import com.raytheon.uf.viz.core.drawables.IDescriptor;
import com.raytheon.uf.viz.core.exception.VizException;
import com.raytheon.uf.viz.core.rsc.LoadProperties;
import com.raytheon.uf.viz.core.rsc.tools.GenericToolsResourceData;
import com.raytheon.uf.viz.hazards.sessionmanager.config.types.Settings;

/**
 * Description: Tool layer resource data, providing information about the Hazard
 * Services viz resource.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * Jul 05, 2013     585    Chris.Golden      Initial creation
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
@XmlType(name = "HazardServicesToolLayerResourceData", factoryMethod = "newDeserializedInstance")
@XmlAccessorType(XmlAccessType.NONE)
public class ToolLayerResourceData extends GenericToolsResourceData<ToolLayer> {

    // Private Variables

    /**
     * settings currently in use.
     */
    @XmlElement
    private Settings settings;

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
    public static ToolLayerResourceData newDeserializedInstance() {
        return new ToolLayerResourceData(true);
    }

    // Public Constructors

    /**
     * Construct a brand-new (not deserialized) standard instance.
     */
    public ToolLayerResourceData() {
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
    private ToolLayerResourceData(boolean instantiatedViaDeserialization) {
        super(ToolLayer.DEFAULT_NAME, ToolLayer.class);
        this.instantiatedViaDeserialization = instantiatedViaDeserialization;
    }

    // Public Methods

    @Override
    public ToolLayer construct(LoadProperties loadProperties,
            IDescriptor descriptor) throws VizException {
        return new ToolLayer(this, loadProperties,
                instantiatedViaDeserialization, appBuilder);
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

    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }
}
