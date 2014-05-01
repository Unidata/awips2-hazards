'''
    Description: Specification of Product Parts for Product Generators.
                 Method for processing Product Parts.
    
    SOFTWARE HISTORY
    Date         Ticket#    Engineer    Description
    ------------ ---------- ----------- --------------------------
    4/14         1633       thansen   Initial creation.
    
'''
import types, collections

class ProductParts:
    def __init__(self):
        pass
        
    ######################################################
    #  Product Part processing         
    ######################################################
   
    def _processProductParts(self, productGenerator, productDict, productSegmentGroup, productParts):            
        '''
        @param productDict
        @param productSegmentGroup
        @param productParts
        @return product dictionary created from the product parts
        
        Note that this method is called recursively such that a product part is allowed to be
        a set of subParts specified as follows:
          (subPartLabel, list of productParts for each subPart)
        For example, we have
          ('segments', [list of [segment product parts]])

        # Product Dictionary
        #   Contains information for all formats e.g.
        #   partner XML, CAP, and Legacy text 
        '''
        if type(productParts) is types.DictType:
            arguments = productParts.get('arguments')
            partsList = productParts.get('partsList')
        else:
            partsList = productParts
        for part in partsList:
            self.flush()
            if type(part) is types.TupleType:
                # e.g. subPart == 'segments', subPartsLists == list of parts for each segment
                subPart, subPartsLists = part
                subParts = []
                for subPartsList in subPartsLists:
                    subDict = collections.OrderedDict()
                    self._processProductParts(productGenerator, subDict, productSegmentGroup, subPartsList)
                    subParts.append(subDict)
                # e.g. productDict['segments'] = segment dictionaries
                productDict[subPart] = subParts
            else:
                if part not in self._noOpParts():
                    execString = 'productGenerator._'+part+'(productDict, productSegmentGroup, arguments)'
                    exec execString

    def _noOpParts(self):
        '''
        These represent product parts that should be skipped when calling product part methods.
        They will be handled automatically by the formatters.
        '''
        return ['CR', 'endSegment', 'issuanceTimeDate'] 

    
    def flush(self):
        ''' Flush the print buffer '''
        import os
        os.sys.__stdout__.flush()
    

