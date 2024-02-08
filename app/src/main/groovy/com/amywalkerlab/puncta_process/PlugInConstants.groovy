
package com.amywalkerlab.puncta_process

class PlugInConstants {
    static final String START_MESSAGE = "Note: The Base Directory is the location where all Pipeline processing will take place.\nThis is the directory where the crop_trim_<flourescence> directory can be found."
    static final String MORPHOLOGICAL_FILTERS_DFLT = "operation=[White Top Hat] element=Ball x-radius=2 y-radius=2 z-radius=2"
    static final String THREE_D_OBJECT_COUNTER_DFLT = "threshold=26 slice=9 min.=5 max.=700 exclude_objects_on_edges statistics summary"
    static final String Z_PROJECT_DFLT = "projection=[Max Intensity]" 
    static final String ROOT_DIR_OPT = "AmyWalkerLab-Puncta-Process.rootDirOpt"
    static final String COLOR_SPLIT_OPT = "AmyWalkerLab-Puncta-Process.colorSplitOpt"
    static final String MORPHOLOGICAL_FILTERS_OPT = "AmyWalkerLab-Puncta-Process.morphologicalFiltersOpt"
    static final String THREE_D_OBJECT_COUNTER_OPT = "AmyWalkerLab-Puncta-Process.3DObjectCounterOpt"
    static final String Z_PROJECT_OPT = "AmyWalkerLab-Puncta-Process.zProjectOpt"
    static final String BATCH_MEASURE_OPT = "AmyWalkerLab-Puncta-Process.batchMeasureOpt"
    static final String SUMMARY_EXCEL_OPT = "AmyWalkerLab-Puncta-Process.summaryExcelOpt"
    static final String FOUR_88_OPT = "AmyWalkerLab-Puncta-Process.488Opt"
    static final String FIVE_61_OPT = "AmyWalkerLab-Puncta-Process.561Opt"
    static final String MORPHOLOGICAL_FILTERS_ARGS = "AmyWalkerLab-Puncta-Process.morphologicalFiltersArgs"
    static final String THREE_D_OBJECT_COUNTER_ARGS = "AmyWalkerLab-Puncta-Process.3DObjectCounterArgs"
    static final String Z_PROJECT_ARGS = "AmyWalkerLab-Puncta-Process.zProjectArgs"

}
