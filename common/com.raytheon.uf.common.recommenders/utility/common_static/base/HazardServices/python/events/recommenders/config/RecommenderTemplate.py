# #
# This software was developed and / or modified by Raytheon Company,
# pursuant to Contract DG133W-05-CQ-1067 with the US Government.
# 
# U.S. EXPORT CONTROLLED TECHNICAL DATA
# This software product contains export-restricted data whose
# export/transfer/disclosure is restricted by U.S. law. Dissemination
# to non-U.S. persons whether in the United States or abroad requires
# an export license or other authorization.
# 
# Contractor Name:        Raytheon Company
# Contractor Address:     6825 Pine Street, Suite 340
#                         Mail Stop B8
#                         Omaha, NE 68106
#                         402.291.0100
# 
# See the AWIPS II Master Rights File ("Master Rights File.pdf") for
# further licensing information.
# #


#
# The abstract recommender module that all other recommenders will be drawn from.  
#  
#    
#     SOFTWARE HISTORY
#    
#    Date            Ticket#       Engineer       Description
#    ------------    ----------    -----------    --------------------------
#    01/22/13                      mnash          Initial Creation.
#    01/29/15        3626          Chris.Golden   Added EventSet to arguments for getting dialog info.
#    11/10/15       12762          Chris.Golden   Added comments about what sort of metadata is
#                                                 expected from defineScriptMetadata(). 
#    06/23/16       19537          Chris.Golden   Changed to use visual features for spatial info.
# 
#

import abc

class Recommender(object):
    __metaclass__ = abc.ABCMeta
    
    def __init__(self):
        return
    
    @abc.abstractmethod
    def defineScriptMetadata(self):
        '''
        @summary: Get basic information about the recommender prior to executing it.
        The returned dictionary contains entries as follows:
                
            toolName:
                Name of the recommender.
                
            author:
                Optional entry providing the author.
                
            description:
                Optional entry providing the description.
                
            version:
                Optional entry providing the version.
                
            getDialogInfoNeeded:
                Optional entry providing a boolean indicating whether or not the
                defineDialog() method should be called prior to executing this
                recommender. If not provided, it defaults to True.
                
            handleDialogParameterChangeNeeded:
                Optional entry providing a boolean indicating whether or not the
                handleDialogParameterChange() method should be called each time the
                dialog and/or visual features provided by defineDialog() have been
                altered by the user. If not provided, it defaults to False.
            
            getSpatialInfoNeeded:
                Optional entry providing a boolean indicating whether or not the
                defineSpatialInfo() method should be called prior to executing this
                recommender. If not provided, it defaults to True.

            onlyIncludeTriggerEvents:
                Optional entry providing a boolean indicating whether or not, if
                the recommender is being run in response to one or more hazard events
                being modified in some way, only said modified hazard event should be
                included in the event set passed to its various methods.  If not
                provided, it defaults to False.
                
            includeEventTypes:
                Optional entry that, if provided, supplies a list of one or more
                hazard types in order to prune the hazard events included in the
                event set passed to its various methods. If provided, any hazard
                events with types other than those included in the list are not made
                part of the input event set. If not provided, no such pruning occurs.
            
            includeDataLayerTimes:
                Optional entry providing a boolean indicating whether or not data
                layer times should be included as an attribute of the event set
                passed to its various methods under the key "dataLayerTimes". If not
                provided, it defaults to False.
            
            includeCwaGeometry:
                Optional entry providing a boolean indicating whether or not the CWA
                geometry should be included as an attribute of the event set passed
                to its various methods under the key "cwaGeometry". If not provided,
                it defaults to False.

        @return: Dictionary which defines basic information about the recommender as
        specified above.
        '''
        return
    
    def defineDialog(self, eventSet):
        '''      
        @summary: Determine dialog-based information needed by the recommender. Each
        time this recommender is executed, this method is called prior to the
        execute() method (assuming "getDialogInfoNeeded" is not included in the
        dictionary returned by defineScriptMetadata(), or if it is included but is
        True).
        
        It returns either None, in which case no dialog-based information is sought
        from the user, or a dictionary holding entries for the following parameters:
        
            fields:
                List of dictionaries, with each of the latter defining a megawidget
                specifier. Alternatively, this may be a single megawidget specifier
                map.
                
            valueDict:
                Map pairing identifiers from any stateful megawidget specifiers
                defined in the "fields" entry with their initial states.
                
            title:
                Optional string to be used as the dialog title.
                
            minInitialWidth:
                Optional integer giving the minimum initial width the dialog should be
                be allowed in pixels.
                
            maxInitialWidth:
                Optional integer giving the maximum initial width the dialog should be
                be allowed in pixels.
                
            maxInitialHeight:
                Optional integer giving the maximum initial height the dialog should
                be be allowed in pixels.
                
            buttons:
                Optional parameter that indicates what command buttons should be
                present to dismiss the dialog. If provided, it must be a list of
                dictionaries, with each dictionary containing the following entries:
                
                    identifier:
                        Unique (within the list of button dictionaries) identifier of
                        the button.
                        
                    label:
                        Label to be displayed for the button.
                        
                    close:
                        Optional boolean indicating whether or not the button is
                        considered to be equivalent to the "X" button in the dialog's
                        title bar. Only  Only one of the button dictionaries should
                        have this property set to True. If multiple ones do, only the
                        last dictionary with such a value will be considered to be
                        equivalent to the "X" button. If none have this property set to
                        True, the last dictionary in the list will be considered
                        equivalent to the "X" button.
                        
                    cancel:
                        Optional boolean indicating whether or not the button is
                        considered the "cancel" button. None of the button dictionaries
                        need to have this property set to True, as cancellation does
                        not have to be an option. If more than one have this property
                        as True, only the last dictionary with such a value will be
                        considered to be the "cancel" button.
                        
                    default:
                        Optional boolean indicating whether or not the button is the
                        default for the dialog. Only one of the button dictionaries
                        should have this property set to True. If multiple ones do,
                        only the first button dictionary with such a value will be
                        considered to be the default button. If none have this property
                        set to True, the first dictionary in the list will be
                        considered the default button.
                    
            visualFeatures:
                Optional parameter that, if provided, must be a list of visual features
                to be displayed along with the dialog. Note that if this is included,
                either the visual features should be informational (and thus not
                editable by the user) in nature, or else handleDialogParameterChange()
                should be overridden to react to visual feature changes.
                
                Note that if the visual features list includes as its last element a
                visual feature with no geometry (either directly, or indirectly via
                inheritance from another visual feature), that visual feature will be
                used to take on any new geometry the user creates while the dialog is
                showing. This allows users to draw geometries from scratch and have
                them be provided to handleDialogParameterChange() if need be. 
        
        @param eventSet: Attributes providing the execution context of the recommender.
        TODO: Fill in information on what is included in the event set.
        @return: Dictionary holding attributes as detailed in the summary. 
        '''
        return

    def handleDialogParameterChange(self, eventSet, triggeringDialogIdentifiers, mutableDialogProperties, triggeringVisualFeatureIdentifiers, visualFeatures, collecting):
        '''      
        @summary: Handle initialization of the dialog initially provided by
        defineDialog(), or changes to the dialog or the its accompanying visual
        features if any were provided by said method. This method is called right
        after the dialog is initialized, in that case with no triggering dialog
        or visual feature identifiers, and also whenever the user changes the dialog
        parameters or visual features. Note that it will never be called unless
        "handleDialogParameterChangeNeeded" is included in the dictionary returned by
        defineScriptMetadata() with a value of True.
        
        Additionally, it will be called one last time immediately following the user's
        dismissal of the parameter dialog and prior to recommender execution, this time
        to determine what visual features should be displayed while the recommender
        runs, if any. In this case, the visual features from the previous call will be
        included, as before, but the collecting parameter will be False. It is a good
        idea to have this last call return only read-only visual features (or none at
        all), since allowing user modifications to these visual features is pointless
        and potentially confusing. In this case, any changed mutable dialog properties
        that are returned will be ignored.
        
        This method acts as a more comprehensive version of the method
        applyInterdependencies(), used to allow megawidget state changes to trigger
        configuration changes in said megawidgets. Unlike applyInterdependencies(),
        however, this method allows the event set and/or visual features to be taken
        into account, and it allows said visual features to be configured as well as
        the megawidgets.
        
        Note that if this method is provided, no applyInterdependencies() method
        should be defined within the recommender script file, as there is no point
        in splitting functionality between them. If a dialog only needs to implement
        megawidget interdependencies, and does not need to know about visual feature
        changes or the event set that was provided to defineDialog(), then use of
        applyInterdependencies() is a better, lighter-weight choice. In that case, the
        defineScriptMetadata() method may include "handleDialogParameterChangeNeeded"
        set to False in order to avoid having this method called, as calling this
        empty implementation of this method will potentially cause a pointless, albeit
        small, performance hit.
        
        @param eventSet: Attributes providing the execution context of the recommender.
        TODO: Fill in information on what is included in the event set.
        @param triggeringDialogIdentifiers: As with the triggerIdentifiers parameter
        of applyInterdependencies(), a collection of zero or more identifiers of
        megawidgets within the dialog that experienced a state change or were invoked
        by the user to cause this method's invocation. Note that except during dialog
        initialization, there will be at least one element in either this collection or
        the triggeringVisualFeatureIdentifiers parameter whenever this method is
        invoked, as something has to have been changed by the user to invoke it.
        @param mutableDialogProperties: As with the mutableProperties parameter of
        applyInterdependencies(), a dictionary with entries for each megawidget within
        the dialog that has mutable properties, with the key being the megawidget's
        "fieldName" and the value being a dictionary of said properties. The latter
        dictionary has an entry for each such property.
        @param triggeringVisualFeatureIdentifiers: Collection of zero or more
        identifiers of visual features being used with the dialog that experienced a
        state change caused by the user to trigger this method's invocation. Note that
        except during dialog initialization, there will be at least one element in
        either this collection or the triggeringDialogIdentifiers parameter whenever
        this method is invoked, as something has to have been changed by the user to
        invoke it.
        @param visualFeatures: Visual features being used with the dialog, modified
        by the user if such modifications have occurred. This will be None if no
        visual features were provided by the most recent call to defineDialog() or
        to this method.
        @param collecting: Flag indicating whether or not the visual features to be
        generated, if any, are for collecting information from the user, or (if the
        last call to the method for this execution) simply for displaying information
        to the user.
        @return: Either None, if no changes are to be made to the dialog's mutable
        properties or to the visual features, or else a two-element tuple holding
        as its first element new mutable dialog properties, and as its second element
        the new visual features. The first element may be None if no megawidgets'
        mutable properties need to be changed, and may include only entries for those
        properties of those megawidgets that require change (i.e. they do not need
        to include all the mutable properties that were passed in for the invocation).
        In contrast, the second element must include all the visual features to be
        displayed alongside the dialog; so for example if None is provided, any visual
        features that were previously showing for the dialog are removed. Note also
        that the visual features returned conform to the same rules as those provided
        under the "visualFeatures" entry of the dictionary returned by defineDialog()
        (i.e. if a single visual feature with no geometry is included at the end of
        the list, it will be used to take geometry input from the user).
        '''
        return (None, None)
    
    def defineSpatialInfo(self, eventSet, visualFeatures, collecting):
        '''
        @summary: Determine spatial information needed by the recommender. Each time
        this recommender is executed, this method is called prior to the execute()
        method (assuming "getSpatialInfoNeeded" is not included in the dictionary
        returned by defineScriptMetadata(), or if it is included but is True). It will
        be called with None for the visualFeatures parameter and True for the
        collecting parameter.
        
        If it returns a non-empty list of visual features, it will then be called again
        each time the user modifies one of the returned visual features and
        isSpatialInfoComplete() returns False, with each of these subsequent calls
        including the visual features generated by the previous call with the user's
        modifications applied, and collecting set to True.
        
        Additionally, it will be called one last time after isSpatialInfoComplete()
        returns True, or (if it returned no visual features the first time it was
        called) immediately after its first call, this time to determine what visual
        features should be displayed while the recommender runs, if any. In this case,
        the visual features from the previous call will be included, as before, but the
        collecting parameter will be False. It is a good idea to have this last call
        return only read-only visual features (or none at all), since allowing user
        modifications to these visual features is pointless and potentially confusing. 
        
        This method only needs to be overridden if the user must provide spatial input
        to the recommender, and/or if the recommender wants to provide read-only visual
        features to be displayed while the recommender is executing.
        
        @param eventSet: Attributes providing the execution context of the recommender.
        TODO: Fill in information on what is included in the event set.
        @param visualFeatures: Visual features returned by the previous call to this
        method, if any, with any modification made by the user applied.
        @param collecting: Flag indicating whether or not the visual features to be
        generated, if any, are for collecting information from the user, or (if the
        last call to the method for this execution) simply for displaying information
        to the user.
        @return: Visual features to be used by the user to provide spatial input;
        may be empty.
        '''
        return
    
    def isSpatialInfoComplete(self, eventSet, visualFeatures):
        '''
        @summary: Determines whether the the specified visual features, generated by
        getSpatialInfo() and modified by the user, provide enough information to run
        the recommender's execute() method.
        
        See the description of getSpatialInfo() for information on when this method
        is called.
        
        The base implementation of this method returns True. Subclasses only need to
        override the method if getSpatialInfo() is to be called to collect information
        from the user, and the first modification made by the user to the resulting
        visual features may not complete the information gathering.
        
        @param eventSet: Attributes providing the execution context of the recommender.
        TODO: Fill in information on what is included in the event set.
        @param visualFeatures: Visual features returned by the last call to the
        getSpatialInfo() method, with the user modification applied.
        @return: True if the provided visual features give enough information to
        run the recommender's execute() method, False if getSpatialInfo() should be
        called with a collecting value of True to continue collecting information from
        the user.
        '''
        return True
    
    @abc.abstractmethod
    def execute(self, eventSet, dialogInputMap, visualFeatures):
        '''
        @param eventSet: A set of event objects that the user can use to help determine 
        new objects to return. TODO: Fill in information on what is included in the
        event set.
        @param dialogInputMap: Dictionary containing entries for any values provided
        by megawidgets generated by the defineDialog() method.
        @param visualFeatures: List of visual features as created by either the
        defineDialog() or defineSpatialInfo() methods and modified by the user.
        @return: List of objects that will be later converted to Java IEvent objects.
        TODO: Fill in information on what is included in the event set.
        '''
        return
