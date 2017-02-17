/**
 * This software was developed and / or modified by the
 * National Oceanic and Atmospheric Administration (NOAA), 
 * Earth System Research Laboratory (ESRL), 
 * Global Systems Division (GSD), 
 * Evaluation & Decision Support Branch (EDS)
 * 
 * Address: Department of Commerce Boulder Labs, 325 Broadway, Boulder, CO 80305
 */
package gov.noaa.gsd.common.visuals;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

/**
 * Description: Base class for serializers of {@link VisualFeaturesList}
 * objects. Subclasses use the methods provided here to help with serializing to
 * whatever forms the subclasses provide. Note that any such subclass that is
 * created requires that a subclass of {@link VisualFeaturesListDeserializer} be
 * used to deserialize whatever the subclass of this class serializes.
 * <p>
 * This class provides methods to allow a {@link VisualFeature} object's
 * templates (that is, other <code>VisualFeature</code> objects it references)
 * to be serialized as said templates' identifiers, so that a list of visual
 * features in serialized form does <i>not</i> include a given visual feature
 * serialized multiple times, once as a member of the list, and 0 to N times as
 * a template referenced by other visual features. Thus, the
 * {@link #getIdentifiersOfVisualFeatureTemplates(VisualFeature)} method should
 * be used to generate temporally variant identifiers from a visual feature's
 * template. The temporally variant identifiers may then be serialized by the
 * subclass.
 * </p>
 * <p>
 * Note that the methods in this class are thread-safe.
 * </p>
 * 
 * <pre>
 * 
 * SOFTWARE HISTORY
 * Date         Ticket#    Engineer     Description
 * ------------ ---------- ------------ --------------------------
 * Feb 10, 2017   28892    Chris.Golden Initial creation.
 * </pre>
 * 
 * @author Chris.Golden
 * @version 1.0
 */
class VisualFeaturesListSerializer {

    // Protected Static Methods

    /**
     * Get the identifiers of the specified visual feature's templates.
     * 
     * @param visualFeature
     *            Visual feature from which to fetch the identifiers of the
     *            templates.
     * @return Temporally variant property holding the template identifiers as
     *         strings, or <code>null</code> if there are no templates.
     */
    protected static TemporallyVariantProperty<ImmutableList<String>> getIdentifiersOfVisualFeatureTemplates(
            VisualFeature visualFeature) {
        TemporallyVariantProperty<ImmutableList<VisualFeature>> features = visualFeature
                .getTemplates();
        if (features == null) {
            return null;
        }
        TemporallyVariantProperty<ImmutableList<String>> templates = new TemporallyVariantProperty<>(
                convertVisualFeaturesToIdentifiers(features
                        .getDefaultProperty()));
        for (Map.Entry<Range<Date>, ImmutableList<VisualFeature>> entry : features
                .getPropertiesForTimeRanges().entrySet()) {
            templates.addPropertyForTimeRange(entry.getKey(),
                    convertVisualFeaturesToIdentifiers(entry.getValue()));
        }
        return templates;
    }

    // Private Static Methods

    /**
     * Convert the specified list of visual features to a list of the features'
     * identifiers.
     * 
     * @param features
     *            List of visual feature identifiers; may be <code>null</code>.
     * @return List of identifiers, or <code>null</code> if the specified list
     *         was <code>null</code>.
     */
    private static ImmutableList<String> convertVisualFeaturesToIdentifiers(
            List<VisualFeature> features) {
        if (features == null) {
            return null;
        }
        List<String> identifiers = new ArrayList<>(features.size());
        for (VisualFeature feature : features) {
            identifiers.add(feature.getIdentifier());
        }
        return ImmutableList.copyOf(identifiers);
    }
}
