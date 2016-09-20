/**
 * This software was developed and / or modified by the National Oceanic and
 * Atmospheric Administration (NOAA), Earth System Research Laboratory (ESRL),
 * Global Systems Division (GSD), Information Services Branch (ISB)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.viz.hazards.spatialdisplay.drawables;

import gov.noaa.gsd.common.utilities.geometry.IAdvancedGeometry;
import gov.noaa.gsd.viz.hazards.spatialdisplay.entities.IEntityIdentifier;
import gov.noaa.nws.ncep.ui.pgen.elements.Text;

import java.awt.Color;

import com.google.common.base.Joiner;
import com.vividsolutions.jts.geom.Coordinate;

/**
 * Text label drawable in the spatial display.
 * 
 * <pre>
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer    Description
 * ------------ ---------- ----------- --------------------------
 * April 2011              Bryon.Lawrence      Initial creation
 * Jul 18, 2013   1264     Chris.Golden        Added support for drawing lines and
 *                                             points.
 * Nov 23, 2013   1462     Bryon.Lawrence      Set text to bold.
 * Feb 09, 2015 6260       Dan Schaffer        Fixed bugs in multi-polygon handling
 * Mar 16, 2016 15676      Chris.Golden        Added code to support visual features.
 * Mar 24, 2016 15676      Chris.Golden        Added ability to change font size and
 *                                             specify color directly.
 * Mar 26, 2016 15676      Chris.Golden        Added visual feature identifier.
 * Jun 23, 2016 19537      Chris.Golden        Changed to use better identifiers.
 * Jul 25, 2016 19537      Chris.Golden        Renamed, and removed unneeded member
 *                                             data and methods.
 * Aug 22, 2016 19537      Chris.Golden        Removed unneeded layer constructor
 *                                             parameter. Also added new combinable
 *                                             flag to constructor. Added ability to
 *                                             recalculate the location when the
 *                                             zoom changes. Also added toString()
 *                                             method.
 * Sep 12, 2016 15934      Chris.Golden        Changed to work with advanced
 *                                             geometries.
 * </pre>
 * 
 * @author Bryon.Lawrence
 */
public class TextDrawable extends Text implements IDrawable<IAdvancedGeometry> {

    // Private Static Constants

    /**
     * Text PGEN category.
     */
    private static final String TEXT = "TEXT";

    // Private Variables

    private final IEntityIdentifier identifier;

    private final int geometryIndex;

    private final boolean combinable;

    private final TextPositioner textPositioner;

    private final Coordinate baseLocation;

    // Public Constructors

    /**
     * Construct a standard instance.
     * 
     * @param identifier
     *            Identifier.
     * @param attributes
     *            Drawable attributes.
     * @param pointSize
     *            Text point size.
     * @param color
     *            Text color.
     * @param location
     *            Location of the text.
     * @param combinable
     *            Flag indicating whether or not the drawable may be combined
     *            with others of the same type when they occupy the same
     *            location.
     */
    public TextDrawable(IEntityIdentifier identifier,
            DrawableAttributes attributes, float pointSize, Color color,
            Coordinate location, boolean combinable) {
        this.identifier = identifier;
        this.geometryIndex = attributes.getGeometryIndex();
        this.combinable = combinable;
        this.textPositioner = attributes.getTextPosition();
        this.baseLocation = location;
        setPgenType(TEXT);
        setText(attributes.getLabel());
        setColors(new Color[] { color });
        setFontSize(pointSize);
        setStyle(FontStyle.BOLD);
        setLocation(textPositioner.getLabelPosition(baseLocation));
    }

    /**
     * Construct a copy of the specified instance. Note that any member fields
     * of the copy reference the same objects as do the corresponding member
     * fields of the original (that is, this creates a shallow copy).
     * 
     * @param original
     *            Instance to be copied.
     */
    public TextDrawable(TextDrawable original) {
        this.identifier = original.identifier;
        this.geometryIndex = original.geometryIndex;
        this.combinable = original.combinable;
        this.textPositioner = original.textPositioner;
        this.baseLocation = original.baseLocation;
        setPgenType(TEXT);
        setText(original.getText());
        setColors(original.getColors());
        setFontSize(original.getFontSize());
        setStyle(original.getStyle());
        setLocation(original.getLocation());
    }

    // Public Methods

    @Override
    public DisplayType getDisplayType() {
        return DisplayType.NORMAL;
    }

    @Override
    public Boolean maskText() {
        return false;
    }

    @Override
    public IAdvancedGeometry getGeometry() {
        return null;
    }

    @Override
    public IEntityIdentifier getIdentifier() {
        return identifier;
    }

    @Override
    public boolean isEditable() {
        return false;
    }

    @Override
    public void setEditable(boolean editable) {
        throw new UnsupportedOperationException("text is not editable");
    }

    @Override
    public boolean isMovable() {
        return false;
    }

    @Override
    public void setMovable(boolean movable) {
        throw new UnsupportedOperationException("text is not movable.");
    }

    @Override
    public int getGeometryIndex() {
        return geometryIndex;
    }

    @Override
    public String toString() {
        return getIdentifier() + " (text: \""
                + Joiner.on("\\n").join(getText()) + "\")";
    }

    /**
     * Determine whether or not the drawable may be combined with others that
     * are themselves combinable if they occupy the same location.
     * 
     * @return <code>true</code> if the drawable may be combined with others
     *         sharing the same location, <code>false</code> otherwise.
     */
    public boolean isCombinable() {
        return combinable;
    }

    /**
     * Handle a change in zoom level by changing the location if necessary.
     * 
     * @return <code>true</code> if the zoom change resulted in a location
     *         change, <code>false</code> otherwise.
     */
    public boolean handleZoomChange() {
        if (textPositioner.isCentered() == false) {
            setLocation(textPositioner.getLabelPosition(baseLocation));
            return true;
        }
        return false;
    }
}
