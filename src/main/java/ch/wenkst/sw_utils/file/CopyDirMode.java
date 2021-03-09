package ch.wenkst.sw_utils.file;

public enum CopyDirMode {
	COMPLETE_REPLACE, 			// the directory is completely replaced
	NO_REPLACE, 				// if the directory already exists it will not be replaced
	MERGE_REPLACE, 				// the directories will be merged, files that already exist will be replace
	MERGE_NO_REPLACE 			// the directories will be merged, files that already exist will not be replace
}
