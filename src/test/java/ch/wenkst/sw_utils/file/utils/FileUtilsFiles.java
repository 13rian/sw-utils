package ch.wenkst.sw_utils.file.utils;

import java.io.File;
import java.io.IOException;

import ch.wenkst.sw_utils.BaseTest;
import ch.wenkst.sw_utils.file.FileUtils;

public class FileUtilsFiles extends BaseTest {
	public String fileUtilsDir = null;
	public String manyLinesFile = null;
	public String oneLineFile = null;
	public String dumpObjectFile = null;
	
	public String copyDir = null;
	public String dirToCopy = null;
	
	
	public FileUtilsFiles() {
		defineDirectories();
	}
	
	
	private void defineDirectories() {
		fileUtilsDir = System.getProperty("user.dir") + File.separator + "resource" + File.separator + "fileUtils" + File.separator;
		manyLinesFile = fileUtilsDir + "dead_mans_suit.txt";
		oneLineFile = fileUtilsDir + "oneLineFile.txt";
		dumpObjectFile = fileUtilsDir + "objectStorage";
		copyDir = fileUtilsDir + "copyDir";
		dirToCopy = fileUtilsDir + "dirToCopy";
	}

	
	public void deleteAllTestFiles() throws IOException {
		FileUtils.deleteDirContent(copyDir);
	}
	
	
	public String createDirToDelete() throws IOException {
		String dirToDelete = fileUtilsDir + "dirToDelete";
		String subdir1 = dirToDelete + File.separator + "subdir1";
		String subdir2 = dirToDelete + File.separator + "subdir2";
		
		new File(dirToDelete).mkdirs();
		new File(subdir1).mkdirs();
		new File(subdir2).mkdirs();
		
		new File (subdir1 + File.separator + "test1.txt").createNewFile();
		new File (subdir1 + File.separator + "test2.txt").createNewFile();
		new File (subdir2 + File.separator + "test1.txt").createNewFile();
		new File (subdir2 + File.separator + "test2.txt").createNewFile();
		return dirToDelete;
	}
}
