#
# Upgrade Hazards Dictionary - upgradeHazardsDict is a dictionary of 
# phen/sig combinations defining upgrades. Each key is the incoming hazard. 
# The associated list are the hazards which are upgraded by the 
# incoming hazard.
#

upgradeHazardsDict = {
'WC.W': ['WC.A', 'WC.Y'], 
'WC.Y': ['WC.A'],
'BZ.W': ['ZR.Y', 'LE.Y', 'WW.Y',
         'BZ.A', 'WS.A', 'LE.A'],
'IS.W': ['ZR.Y', 'LE.Y', 'WW.Y',
         'BZ.A', 'WS.A', 'LE.A'],
'LE.W': ['ZR.Y', 'LE.Y', 'WW.Y',
         'BZ.A', 'WS.A', 'LE.A'],
'WS.W': ['ZR.Y', 'LE.Y', 'WW.Y',
         'BZ.A', 'WS.A', 'LE.A'],
'ZR.Y': ['BZ.A', 'WS.A', 'LE.A'],
'LE.Y': ['BZ.A', 'WS.A', 'LE.A'],
'WW.Y': ['BZ.A', 'WS.A', 'LE.A'],
'EH.W': ['EH.A', 'HT.Y'],
'HT.Y': ['EH.A'],
'FZ.W': ['FZ.A', 'FR.Y', 'HZ.A'],
'HZ.W': ['FZ.A', 'FR.Y', 'HZ.A'],
'FR.Y': ['FZ.A', 'HZ.A'],
'HI.W': ['TI.W', 'TI.A', 'HI.A'],
'TI.W': ['TI.A', 'HI.A'],
'HW.W': ['DU.Y', 'LW.Y', 'WI.Y', 'HW.A'],
'DS.W': ['DU.Y', 'LW.Y', 'WI.Y', 'HW.A'],
'WI.Y': ['HW.A'],
'EC.W': ['EC.A'],
'FW.W': ['FW.A'],
'CF.W': ['CF.A', 'CF.Y'],
'CF.Y': ['CF.A'],
'LS.W': ['LS.A', 'LS.Y'],
'LS.Y': ['LS.A'],
'BW.Y': ['GL.A', 'SR.A', 'HF.A', 'SE.A'],
'RB.Y': ['GL.A', 'SR.A', 'HF.A', 'SE.A'],
'SC.Y': ['GL.A', 'SR.A', 'HF.A', 'SE.A'],
'SI.Y': ['GL.A', 'SR.A', 'HF.A', 'SE.A'],
'SW.Y': ['SE.A'],
'UP.Y': ['UP.A'],
'HF.W': ['SR.W', 'GL.W', 'SC.Y', 'SW.Y', 'BW.Y', 'SI.Y', 'RB.Y', 'GL.A', 'SR.A', 'HF.A', 'SE.A'],
'SR.W': ['GL.W', 'SC.Y', 'SW.Y', 'BW.Y', 'SI.Y', 'RB.Y', 'GL.A', 'SR.A', 'HF.A', 'SE.A'],
'GL.W': ['SC.Y', 'SW.Y', 'BW.Y', 'SI.Y', 'RB.Y', 'GL.A', 'SR.A', 'HF.A', 'SE.A'],
'SE.W': ['SC.Y', 'RB.Y', 'GL.A', 'SR.A', 'HF.A', 'SE.A'],
'UP.W': ['UP.Y', 'UP.A'],
'SU.W': ['SU.Y'],
'HU.W': ['HU.A', 'TR.W', 'TR.A'],
'HU.A': ['TR.A'],
'TR.W': ['TR.A', 'HU.A', 'TY.A'],
'TY.W': ['TY.A', 'TR.W', 'TR.A'],
'TY.A': ['TR.A'],
}
 

#
# Downgrade Hazards Dictionary - downgradeHazardsDict is a dictionary of 
# phen/sig combinations defining downgrades. Each key is the incoming hazard. 
# The associated list are the hazards which are downgraded by the 
# incoming hazard.
#

downgradeHazardsDict = {
'ZR.Y': ['BZ.W', 'LE.W', 'IS.W', 'WS.W'],
'LE.Y': ['BZ.W', 'LE.W', 'IS.W', 'WS.W'],
'WW.Y': ['BZ.W', 'LE.W', 'IS.W', 'WS.W'],
'WC.Y': ['WC.W'],
'DU.Y': ['DS.W', 'HW.W'],
'LW.Y': ['DS.W', 'HW.W', 'WI.Y'],
'WI.Y': ['DS.W', 'HW.W'],
'HT.Y': ['EH.W'],
'FR.Y': ['FZ.W', 'HZ.W'],
'TI.W': ['HI.W'],
'TR.W': ['HU.W', 'TY.W'],
'UP.Y': ['UP.W'],
'SR.W': ['HF.W'],
'GL.W': ['HF.W', 'SR.W'],
'SC.Y': ['HF.W', 'SR.W', 'GL.W', 'SE.W'],
'SW.Y': ['SE.W'],
'RB.Y': ['HF.W', 'SR.W', 'GL.W', 'SE.W'],
'SU.Y': ['SU.W'],
'BW.Y': ['HF.W', 'SR.W', 'GL.W'],
'SI.Y': ['HF.W', 'SR.W', 'GL.W'],
'LS.Y': ['LS.W'],
'CF.Y': ['CF.W'],
}

