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
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;

/**
 * Description: Base class for deserializers of {@link VisualFeaturesList}
 * objects. Subclasses use the methods provided here to help with deserializing
 * from whatever forms the subclasses expect. Note that any such subclass that
 * is created requires that a subclass of {@link VisualFeaturesListSerializer}
 * be used to serialize whatever the subclass of this class deserializes.
 * <p>
 * This class's methods are designed to:
 * </p>
 * <ol>
 * <li>ensure that the identifiers of individual deserialized
 * {@link VisualFeature} instances have unique identifiers within the list;</li>
 * <li>allow the serialized <code>VisualFeature</code> to include its templates
 * as identifiers, as produced by a subclass of
 * {@link VisualFeaturesListSerializer}; see that class's description for more
 * information; and</li>
 * <li>ensure that all templates referenced by deserialized
 * <code>VisualFeature</code> instances are found in the deserialized list, and
 * that no circular dependencies result from the various templates of the
 * various features.</li>
 * </ol>
 * <p>
 * To those ends, this class provides methods that are to be used as follows:
 * </p>
 * <dl>
 * <dt>{@link #prepareForDeserialization()}</dt>
 * <dd>Must be invoked before deserialization begins.</dd>
 * <dt>{@link #isVisualFeatureIdentifierUnique(String)}</dt>
 * <dd>During deserialization of an individual {@link VisualFeature}, must be
 * invoked to ensure that the deserialized identifier is unique within the list
 * of visual features. If it returns <code>false</code>, the deserialization
 * should fail.</dd>
 * <dt>
 * {@link #recordTemplateIdentifiersForVisualFeature(VisualFeature, TemporallyVariantProperty)}
 * </dt>
 * <dd>During deserialization of an individual <code>VisualFeature</code>, must
 * be invoked when the temporally variant template identifiers are encountered,
 * so that they may be checked for validity and converted to templates at the
 * conclusion of the deserialization process.</dd>
 * <dt>{@link #setTemplatesForVisualFeatures(VisualFeaturesList)}</dt>
 * <dd>Must be invoked at the conclusion of the deserialization; this method
 * checks to ensure that there are no unresolved or circular dependencies in the
 * templates referenced by any of the <code>VisualFeature</code> elements in the
 * specified list, and converts the template identifiers recorded earlier into
 * templates that are then assigned to said <code>VisualFeature</code> elements.
 * The method throws an exception if a problematic dependency is encountered, in
 * which case the deserialization should be considered to have failed.</dd>
 * </dl>
 * <p>
 * Note that the methods in this class are thread-safe, as thread-local storage
 * is used for recording information during the deserialization process. The
 * same thread must, of course, perform the entire deserialization process for a
 * given instance of <code>VisualFeaturesList</code>, that is, the class methods
 * must be invoked as described above by the same thread.
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
class VisualFeaturesListDeserializer {

    // Private Static Constants

    /**
     * Set of visual feature identifiers that are being deserialized. This is
     * used during deserialization to allow checking for non-unique identifiers.
     * It is thread-local because multiple threads might attempt to use it at
     * once, and each thread must be able to track the references of visual
     * features to one another separately in order to avoid cross-thread
     * pollution.
     */
    private static final ThreadLocal<Set<String>> features = new ThreadLocal<Set<String>>() {

        @Override
        protected Set<String> initialValue() {
            return new HashSet<>();
        }
    };

    /**
     * Mapping of visual feature identifiers to sets of any visual feature
     * identifiers that they are using as templates. This is used during
     * deserialization to track the sets of all features that are referenced by
     * features as templates, as this information must be analyzed
     * post-deserialization to ensure that there are no circular or unresolved
     * dependencies. It is thread-local because multiple threads might attempt
     * to use it at once, and each thread must be able to track the references
     * of visual features to one another separately in order to avoid
     * cross-thread pollution.
     */
    private static final ThreadLocal<Map<String, Set<String>>> referencedTemplatesForFeatures = new ThreadLocal<Map<String, Set<String>>>() {

        @Override
        protected Map<String, Set<String>> initialValue() {
            return new HashMap<>();
        }
    };

    /**
     * Mapping of visual feature identifiers to temporally variant properties
     * holding lists of visual feature identifiers that they are to use as
     * templates. This is used after deserialization to assign the templates
     * property of the visual features properly. It is thread-local because
     * multiple threads might attempt to use it at once, and each thread must be
     * able to track the references of visual features to one another separately
     * in order to avoid cross-thread pollution.
     */
    private static final ThreadLocal<Map<String, TemporallyVariantProperty<? extends List<String>>>> templatesForFeatures = new ThreadLocal<Map<String, TemporallyVariantProperty<? extends List<String>>>>() {

        @Override
        protected Map<String, TemporallyVariantProperty<? extends List<String>>> initialValue() {
            return new HashMap<>();
        }
    };

    // Protected Static Classes

    /**
     * Dependency exception, used when circular or unresolved dependencies are
     * found while converting visual feature template identifiers to their
     * corresponding visual features in a visual features list.
     */
    protected static class DependencyException extends Exception {

        // Private Static Constants

        /**
         * Serial version UID.
         */
        private static final long serialVersionUID = -7981194797683850139L;

        // Private Variables

        /**
         * Identifier of the first visual feature.
         */
        private final String firstIdentifier;

        /**
         * Identifier of the second visual feature.
         */
        private final String secondIdentifier;

        /**
         * Description of the problem.
         */
        private final String description;

        // Public Constructors

        /**
         * Construct a standard instance.
         * 
         * @param firstIdentifier
         *            Identifier of the first visual feature.
         * @param secondIdentifier
         *            Identifier of the second visual feature.
         * @param description
         *            Description of the problem.
         */
        public DependencyException(String firstIdentifier,
                String secondIdentifier, String description) {
            this.firstIdentifier = firstIdentifier;
            this.secondIdentifier = secondIdentifier;
            this.description = description;
        }

        // Public Methods

        @Override
        public String toString() {
            return "visual feature \"" + firstIdentifier
                    + "\": \"templates\" property includes bad dependency "
                    + "upon visual feature \"" + secondIdentifier + "\": "
                    + description;
        }
    }

    // Private Static Classes

    /**
     * Dependency, an encapsulation of the relationship created by one visual
     * feature having a dependency upon another.
     */
    private static class Dependency {

        // Private Variables

        /**
         * Feature that depends upon the other.
         */
        private final VisualFeature depender;

        /**
         * Feature upon which the other depends.
         */
        private final VisualFeature dependee;

        // Public Constructors

        /**
         * Construct a standard instance.
         * 
         * @param depender
         *            Feature that depends upon the other.
         * @param dependee
         *            Feature upon which the other depends.
         */
        public Dependency(VisualFeature depender, VisualFeature dependee) {
            this.depender = depender;
            this.dependee = dependee;
        }

        // Public Methods

        @Override
        public boolean equals(Object other) {
            return ((other instanceof Dependency)
                    && (depender == ((Dependency) other).depender) && (dependee == ((Dependency) other).dependee));
        }

        @Override
        public int hashCode() {
            return (int) ((((long) depender.hashCode()) + ((long) dependee
                    .hashCode())) % Integer.MAX_VALUE);
        }

        /**
         * Translate the dependency to a descriptive string.
         * 
         * @return Descriptive string.
         */
        @Override
        public String toString() {
            return depender.getIdentifier() + " -> " + dependee.getIdentifier();
        }
    }

    // Protected Static Methods

    /**
     * Prepare for deserialization of a visual features list performed by this
     * thread.
     */
    protected static void prepareForDeserialization() {

        /*
         * Clear the thread-local set of display features, map of display
         * features to their referenced templates, and map of display features
         * to all their referenced templates, for this thread. These will be
         * used for the deserialization to track which visual features are to be
         * used as templates by which, and to avoid duplicate identifiers.
         */
        features.get().clear();
        referencedTemplatesForFeatures.get().clear();
        templatesForFeatures.get().clear();
    }

    /**
     * Determine whether or not the specified identifier is unique, and if it
     * is, add it to the set of identifiers that have been claimed during the
     * current ongoing deserialization being performed by this thread, so that
     * future invocations of this method during this deserialization will
     * include this identifier as one to check against.
     * 
     * @param identifier
     *            Identifier to be checked for uniqueness in the set of
     *            identifiers of visual features found so far during the current
     *            ongoing deserialization being performed by this thread.
     * @return <code>true</code> if the identifier is unique, <code>false</code>
     *         otherwise.
     */
    protected static boolean isVisualFeatureIdentifierUnique(String identifier) {
        if (features.get().contains(identifier)) {
            return false;
        }
        features.get().add(identifier);
        return true;
    }

    /**
     * Record the association of the specified temporally variant template
     * identifiers with the specified visual feature for the currently ongoing
     * deserialization being performed by this thread, so that at the conclusion
     * of the deserialization,
     * {@link #setTemplatesForVisualFeatures(VisualFeaturesList)} may be invoked
     * to assign the actual temporally variant templates to the visual features
     * in the list in place of their identifiers. The latter invocation will
     * also check to ensure that there are no circular or unresolved template
     * dependencies.
     * 
     * @param visualFeature
     *            Visual feature with which template identifiers are to be
     *            associated.
     * @param templateIdentifiers
     *            Temporally variant template identifiers that are to be
     *            associated with the visual feature; must not be
     *            <code>null</code>.
     */
    protected static void recordTemplateIdentifiersForVisualFeature(
            VisualFeature visualFeature,
            TemporallyVariantProperty<? extends List<String>> templateIdentifiers) {

        /*
         * Compile all the identifiers of templates that might be associated
         * with this visual feature into a single set and remember it for later.
         * Also remember the temporally variant property so that, assuming no
         * problems are found when the dependencies are checked
         * post-deserialization, a new temporally variant property may be
         * constructed using references to the visual features instead of their
         * identifiers.
         */
        String identifier = visualFeature.getIdentifier();
        Set<String> referencedFeatures = new HashSet<>();
        List<String> features = templateIdentifiers.getDefaultProperty();
        if (features != null) {
            referencedFeatures.addAll(features);
        }
        for (List<String> otherFeatures : templateIdentifiers
                .getPropertiesForTimeRanges().values()) {
            referencedFeatures.addAll(otherFeatures);
        }
        referencedTemplatesForFeatures.get()
                .put(identifier, referencedFeatures);
        templatesForFeatures.get().put(identifier, templateIdentifiers);
    }

    /**
     * Set the templates for the visual features within the specified list
     * according to the information compiled by the invocations of
     * {@link #recordTemplateIdentifiersForVisualFeature(VisualFeature, TemporallyVariantProperty)}
     * during the deserialization process, ensuring that the templates do not
     * lead to any circular or unresolved dependencies.
     * 
     * @param visualFeatures
     *            List of visual features, which include no temporally variant
     *            templates as of yet, for which to check the previously
     *            recorded template identifiers for problematic dependencies,
     *            and to which to assign the temporally variant templates. If
     *            the method is successful (no exception is thrown), the list's
     *            elements will have been modified to include their temporally
     *            variant templates when the method returns.
     * @throws DependencyException
     *             If bad dependencies are found.
     */
    protected static void setTemplatesForVisualFeatures(
            VisualFeaturesList visualFeatures) throws DependencyException {

        /*
         * Compile a mapping of visual feature identifiers to the features
         * themselves.
         */
        Map<String, VisualFeature> visualFeaturesForIdentifiers = new HashMap<>(
                visualFeatures.size(), 1.0f);
        for (VisualFeature visualFeature : visualFeatures) {
            visualFeaturesForIdentifiers.put(visualFeature.getIdentifier(),
                    visualFeature);
        }

        /*
         * Create a set of checked dependencies, into which will be placed any
         * dependencies that have been checked during the dependency problem
         * checking. This will be done to avoid rechecking any dependencies that
         * have already been checked once.
         */
        Set<Dependency> checkedDependencies = new HashSet<>();

        /*
         * Iterate through the sets of all potential templates associated with
         * each visual feature, checking each of the resulting dependencies to
         * make sure that there are no circular or unresolved ones.
         */
        for (Map.Entry<String, Set<String>> entry : referencedTemplatesForFeatures
                .get().entrySet()) {

            /*
             * Iterate through the individual dependencies for this visual
             * feature, checking each in turn.
             */
            String identifier = entry.getKey();
            for (String otherIdentifier : entry.getValue()) {
                checkedDependencies.addAll(ensureDependencyIsLegal(identifier,
                        otherIdentifier, visualFeaturesForIdentifiers,
                        checkedDependencies, new LinkedHashSet<String>(
                                visualFeatures.size(), 1.0f), ""));
            }
        }

        /*
         * If execution has made it to this point, then no bad dependencies were
         * found. Thus, fill in the templates for the visual features.
         */
        for (Map.Entry<String, TemporallyVariantProperty<? extends List<String>>> entry : templatesForFeatures
                .get().entrySet()) {

            /*
             * Convert the temporally variant property holding lists of
             * identifiers to one holding lists of visual features, and assign
             * it.
             */
            TemporallyVariantProperty<? extends List<String>> templateIdentifiers = entry
                    .getValue();
            TemporallyVariantProperty<ImmutableList<VisualFeature>> templates = new TemporallyVariantProperty<>(
                    convertIdentifiersToVisualFeatures(
                            templateIdentifiers.getDefaultProperty(),
                            visualFeaturesForIdentifiers));
            for (Map.Entry<Range<Date>, ? extends List<String>> subEntry : templateIdentifiers
                    .getPropertiesForTimeRanges().entrySet()) {
                templates.addPropertyForTimeRange(
                        subEntry.getKey(),
                        convertIdentifiersToVisualFeatures(subEntry.getValue(),
                                visualFeaturesForIdentifiers));
            }
            visualFeaturesForIdentifiers.get(entry.getKey()).setTemplates(
                    templates);
        }
    }

    // Private Static Methods

    /**
     * Ensure that the specified dependency is legal (that is, does not result
     * in any circular or unresolved dependencies).
     * 
     * @param depender
     *            Identifier of the visual feature that is dependent upon the
     *            other.
     * @param dependee
     *            Identifier of the visual feature that is depended upon by the
     *            depender.
     * @param visualFeaturesForIdentifiers
     *            Map of visual features' identifiers to the visual features
     *            themselves.
     * @param checkedDependencies
     *            Set of all dependencies that have already been checked and
     *            found to be legal.
     * @param dependencyPath
     *            Ordered set of identifiers of visual features, ordered by
     *            dependency relationships (element 0 depends upon 1, 1 upon 2,
     *            and so on).
     * @return Set of all dependencies that have been checked as a result of
     *         this method executing successfully.
     * @throws DependencyException
     *             If a dependency is found to be circular or unresolved.
     */
    private static Set<Dependency> ensureDependencyIsLegal(String depender,
            String dependee,
            Map<String, VisualFeature> visualFeaturesForIdentifiers,
            Set<Dependency> checkedDependencies,
            LinkedHashSet<String> dependencyPath, String prefix)
            throws DependencyException {

        /*
         * Ensure that the dependee exists; if not, an error has occurred.
         */
        VisualFeature otherFeature = visualFeaturesForIdentifiers.get(dependee);
        if (otherFeature == null) {
            throw new DependencyException(depender, dependee,
                    "missing visual feature with latter identifier");
        }

        /*
         * See if this dependency has already been checked, and if so, do
         * nothing more with it.
         */
        Dependency dependency = new Dependency(
                visualFeaturesForIdentifiers.get(depender), otherFeature);
        if (checkedDependencies.contains(dependency)) {
            return Collections.emptySet();
        }

        /*
         * Determine whether or not this dependency is part of a circular
         * dependency by checking the dependency path as it was until now to
         * determine whether the dependee is already in the path. If so, an
         * error has occurred.
         */
        if (dependencyPath.contains(dependee)) {
            throw new DependencyException(dependencyPath.iterator().next(),
                    dependee, "encountered circular dependency: "
                            + Joiner.on(" -> ").join(dependencyPath) + " -> "
                            + dependee);
        }

        /*
         * Add the dependee to the dependency path so that it may be passed onto
         * recursive calls for checking any dependees of this dependee.
         */
        dependencyPath.add(dependee);

        /*
         * Create a set of just-checked dependencies and add the one just
         * checked to the set. This set will have all the other dependencies
         * checked by recursive calls to this method added to it as well.
         */
        Set<Dependency> justCheckedDependencies = new HashSet<>();
        justCheckedDependencies.add(dependency);

        /*
         * Iterate through any dependencies that the dependee has, handling each
         * by calling this method recursively, and add any dependencies that the
         * recursive calls check successfully to the set of just-checked ones.
         */
        Set<String> subdependees = referencedTemplatesForFeatures.get().get(
                dependee);
        if (subdependees != null) {
            for (String subdependee : subdependees) {
                justCheckedDependencies.addAll(ensureDependencyIsLegal(
                        dependee, subdependee, visualFeaturesForIdentifiers,
                        checkedDependencies,
                        new LinkedHashSet<>(dependencyPath), prefix + "    "));
            }
        }

        /*
         * Return the set of just-checked dependencies.
         */
        return justCheckedDependencies;
    }

    /**
     * Convert the specified list of visual feature identifiers to a list of the
     * corresponding visual features.
     * 
     * @param identifiers
     *            List of visual feature identifiers; may be <code>null</code>.
     * @param visualFeaturesForIdentifiers
     *            Map of visual feature identifiers to their corresponding
     *            visual features.
     * @return List of visual features, or <code>null</code> if the specified
     *         list was <code>null</code>.
     */
    private static ImmutableList<VisualFeature> convertIdentifiersToVisualFeatures(
            List<String> identifiers,
            Map<String, VisualFeature> visualFeaturesForIdentifiers) {
        List<VisualFeature> features = null;
        if (identifiers != null) {
            features = new ArrayList<>(identifiers.size());
            for (String identifier : identifiers) {
                features.add(visualFeaturesForIdentifiers.get(identifier));
            }
        }
        return ImmutableList.copyOf(features);
    }
}
