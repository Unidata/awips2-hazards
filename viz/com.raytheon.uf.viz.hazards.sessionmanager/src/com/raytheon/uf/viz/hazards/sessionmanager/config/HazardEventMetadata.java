/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package com.raytheon.uf.viz.hazards.sessionmanager.config;

import gov.noaa.gsd.viz.megawidgets.MegawidgetSpecifierManager;

import java.io.File;
import java.util.Map;

/**
 * Description: Encapsulation of the description of all metadata parameters for
 * a hazard event, including the megawidget specifiers, the path to the script
 * file, and any event modifying scripts.
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Aug 20, 2014    4243    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
public class HazardEventMetadata {

    // Private Variables

    /**
     * Megawidget specifier manager.
     */
    private final MegawidgetSpecifierManager megawidgetSpecifierManager;

    /**
     * Script file.
     */
    private final File scriptFile;

    /**
     * Map of event modifying script identifiers to their corresponding entry
     * point function names.
     */
    private final Map<String, String> eventModifyingFunctionNamesForIdentifiers;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param megawidgetSpecifierManager
     *            Megawidget specifier manager.
     * @param eventModifyingFunctionNamesForIdentifiers
     *            Map of event modifying script identifiers to their
     *            corresponding entry point function names.
     */
    public HazardEventMetadata(
            MegawidgetSpecifierManager megawidgetSpecifierManager,
            File scriptFile,
            Map<String, String> eventModifyingFunctionNamesForIdentifiers) {
        this.megawidgetSpecifierManager = megawidgetSpecifierManager;
        this.scriptFile = scriptFile;
        this.eventModifyingFunctionNamesForIdentifiers = eventModifyingFunctionNamesForIdentifiers;
    }

    // Public Methods

    /**
     * Get the megawidget specifier manager.
     * 
     * @return Megawidget specifier manager.
     */
    public final MegawidgetSpecifierManager getMegawidgetSpecifierManager() {
        return megawidgetSpecifierManager;
    }

    /**
     * Get the script file.
     * 
     * @return Script file.
     */
    public final File getScriptFile() {
        return scriptFile;
    }

    /**
     * Get the map of event modifying script identifiers to their corresponding
     * entry point function names.
     * 
     * @return Map.
     */
    public final Map<String, String> getEventModifyingFunctionNamesForIdentifiers() {
        return eventModifyingFunctionNamesForIdentifiers;
    }
}
