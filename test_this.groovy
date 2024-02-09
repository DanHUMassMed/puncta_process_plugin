def inputString = "slice=9 threshold=26 min.=5 max.=700 exclude_objects_on_edges statistics summary"
def newThresholdValue = 3033

def updatedString = inputString.replaceFirst(/threshold=\d+/, "threshold=$newThresholdValue")

println updatedString

