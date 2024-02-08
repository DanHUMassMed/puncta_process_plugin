final String COLOR_SPLIT           = 'Color Split'
final String MORPHOLOGICAL_FILTERS = 'Morphological Filters'
final String THREE_D_OC            = '3D Object Counter'
final String Z_PROJECT             = 'Z Project'   
final String BATCH_MEASURE         = 'Batch Measure' 
final String SUMMARY_EXCEL         = 'Summary Excel' 
final String[] process_labels = [COLOR_SPLIT, MORPHOLOGICAL_FILTERS, THREE_D_OC, Z_PROJECT, BATCH_MEASURE, SUMMARY_EXCEL]
processValues = [true,false,false]
def of = { indx -> process_labels.findIndexOf { it == indx } }

println(process_labels)

//def index = process_labels.indexOf(COLOR_SPLIT)
def index = process_labels.findIndexOf { it == MORPHOLOGICAL_FILTERS }
println(of(MORPHOLOGICAL_FILTERS))