package ch.wenkst.sw_utils.file;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import ch.wenkst.sw_utils.file.FileUtils.CopyDirMode;


public class FileUtilsTest {
	private static String fileUtilsDir = null; 		// the directory containing the file handler test files
	private static String manyLinesFile = null;		// path to the file that holds many lines
	private static String oneLineFile = null; 		// file that contains one line
	
	private static String copyDir = null; 			// the directory to which all the resources are copied
	private static String dirToCopy = null; 		// the directory to copy
	
	
	/**
	 * loads the resources that are needed for the test
	 */
	@BeforeAll
	public static void initializeExternalResources() {
		// define the directory of the file handler tests
		fileUtilsDir = System.getProperty("user.dir") + File.separator +
				"resource" + File.separator + 
				"fileUtils" + File.separator;
		
		// file that contains many lines
		manyLinesFile = fileUtilsDir + "dead_mans_suit.txt";
		
		
		// file that contains just one line
		oneLineFile = fileUtilsDir + "oneLineFile.txt";
		
		
		// directory for the copy tests
		copyDir = fileUtilsDir + "copyDir";
		
		
		dirToCopy = fileUtilsDir + "dirToCopy";
		
		
		// ensure that the directory in which things are copied is empty
		FileUtils.deleteDirContent(copyDir);	
	}
	
	
	/**
	 * read the last lines of a file
	 */
	@Test
	@DisplayName("last lines of a file")
	public void lastLinesReadTest() {
		// read the last four lines of the test file
		ArrayList<String> lines = FileUtils.readLastLines(manyLinesFile, 4);
		
		Assertions.assertEquals("I thought that it was black", lines.get(0), "fourth last line of the file");
		Assertions.assertEquals("I found out it was blue", lines.get(1), "third last line of the file");
		Assertions.assertEquals("Now I’m seeing things", lines.get(2), "second last line of the file");
		Assertions.assertEquals("That only dead men do", lines.get(3), "fourth last line of the file");
		
		
		// read all lines of the file
		lines = FileUtils.readLastLines(manyLinesFile, 37);
		Assertions.assertEquals(35, lines.size(), "read all lines of the file");
	}
	
	
	/**
	 * read the content of a file to a string
	 * in this test a file with only one line is used in order to avoid the problems of
	 * carriage returns and line feeds that are different on windows and linux systems
	 */
	@Test
	@DisplayName("get the file content as string")
	public void readFileToStringTest() {
		// get the content of a file as String
		String fileContent = FileUtils.readStrFromFile(oneLineFile);
		Assertions.assertEquals("First line of the text file.", fileContent, "read the content of a file");
	}
	
	
	/**
	 * copy a file
	 */
	@Test
	@DisplayName("copy a file")
	public void copyFileTest() {
		// copy a file
		String dest = copyDir + File.separator + "copiedFile.txt";
		boolean fileCopied = FileUtils.copyFile(oneLineFile, dest, true);
		
		Assertions.assertEquals(true, fileCopied, "file copied");
		Assertions.assertEquals(true, new File(dest).exists(), "copied file exists");
	}
	
	
	/**
	 * move a file
	 */
	@Test
	@DisplayName("move a file")
	public void moveFileTest() {
		// create a new file to move
		String fileToMove = fileUtilsDir + "fileToMove.txt";
		
		Assertions.assertDoesNotThrow(() -> {
			new File(fileToMove).createNewFile();
		}, "create a new file to move");
		
		// move the file
		String destFile = copyDir + File.separator + "fileToMove.txt";
		boolean isMoved = FileUtils.moveFile(fileToMove, destFile, true);
		
		Assertions.assertEquals(true, isMoved, "file moved");
		Assertions.assertEquals(true, new File(destFile).exists(), "file does exist");
	}
	
	
	
	/**
	 * delete a file
	 */
	@Test
	@DisplayName("delete a file")
	public void deleteFileTest() {
		// create a new file to delete
		String fileToDelete = fileUtilsDir + "fileToDelete.txt";
		
		Assertions.assertDoesNotThrow(() -> {
			new File(fileToDelete).createNewFile();
		}, "create a new file to delete");
		
		// delete the file
		boolean isDeleted = FileUtils.deleteFile(fileToDelete);
		
		Assertions.assertEquals(true, isDeleted, "file deleted");
		Assertions.assertEquals(false, new File(fileToDelete).exists(), "file does not exist");
	}
	
	
	/**
	 * copy a directory
	 */
	@Test
	@DisplayName("copy a directory")
	public void copyDirTest() {
		String destDir = copyDir + File.separator + "copiedDir";
		boolean isCopied = FileUtils.copyDir(dirToCopy, destDir, CopyDirMode.COMPLETE_REPLACE);
		
		Assertions.assertEquals(true, isCopied, "dir copied");
		Assertions.assertEquals(true, new File(destDir).exists(), "dir does exist");
		
		// check some file that should have been copied as well
		String fileInTopDir = destDir + File.separator + "test2.txt";
		String fileInSubDir = destDir + File.separator + "subdir1" + File.separator + "test3.txt";
		String fileInSubSubDir = destDir + File.separator + "subdir1" + File.separator + "subsubdir2" + File.separator + "test1.txt";
		Assertions.assertEquals(true, new File(fileInTopDir).exists(), "file in top folder copied");
		Assertions.assertEquals(true, new File(fileInSubDir).exists(), "file in sub folder copied");
		Assertions.assertEquals(true, new File(fileInSubSubDir).exists(), "file in sub sub folder copied");
	}
	
	
	/**
	 * delete a directory
	 */
	@Test
	@DisplayName("delete a directory")
	public void deleteDirTest() {
		// create a directory with some content to delete
		String dirToDelete = fileUtilsDir + "dirToDelete";
		
		Assertions.assertDoesNotThrow(() -> {
			String subdir1 = dirToDelete + File.separator + "subdir1";
			String subdir2 = dirToDelete + File.separator + "subdir2";
			
			new File(dirToDelete).mkdirs();
			new File(subdir1).mkdirs();
			new File(subdir2).mkdirs();
			
			new File (subdir1 + File.separator + "test1.txt").createNewFile();
			new File (subdir1 + File.separator + "test2.txt").createNewFile();
			new File (subdir2 + File.separator + "test1.txt").createNewFile();
			new File (subdir2 + File.separator + "test2.txt").createNewFile();
		}, "test dir to delete created");
		
		
		// delete the directory
		boolean isDeleted = FileUtils.deleteDir(dirToDelete);
		
		Assertions.assertEquals(true, isDeleted, "directory deleted");
		Assertions.assertEquals(false, new File(dirToDelete).exists(), "directory does not exist");
	}
	
	
	/**
	 * read the nth line of a file
	 */
	@Test
	@DisplayName("read the nth line of a file")
	public void readNthLineTest() {
		String firstLine = FileUtils.readFirstLine(manyLinesFile);
		Assertions.assertEquals("I bought a dead mans suit", firstLine, "first line matching");
		
		String line24 = FileUtils.readNthLine(manyLinesFile, 24);
		Assertions.assertEquals("Is this sour taste", line24, "line 24 matching");
		
		
		String line30 = FileUtils.readNthLine(manyLinesFile, 30);
		Assertions.assertEquals("I couldn’t take it back", line30, "line 30 matching");
	}
	
	
	/**
	 * finds a file by pattern
	 */
	@Test
	@DisplayName("find file by pattern")
	public void findFileByPatternTest() {
		// find a file that exists
		String filePath = FileUtils.findFileByPattern(fileUtilsDir, "2", "csv");
		String expectedFilePath = fileUtilsDir + "test2.csv";
		Assertions.assertEquals(expectedFilePath, filePath, "find existing file");
		
		// find a file with an ending that does not exist in the directory
		filePath = FileUtils.findFileByPattern(fileUtilsDir, "test", "der");
		Assertions.assertEquals(null, filePath, "find non-existing file-end");

		// find a file with a pattern that does not exist in the directory
		filePath = FileUtils.findFileByPattern(fileUtilsDir, "noFile", "csv");
		Assertions.assertEquals(null, filePath, "find non-existing file-pattern");	
	}
	
	
	/**
	 * finds a file by pattern
	 */
	@Test
	@DisplayName("find files by pattern")
	public void findFilesByPatternTest() {
		// find all csv files
		List<String> fileList = FileUtils.findFilesByPattern(fileUtilsDir, "test", "csv");
		String[] filePaths = fileList.toArray(new String[fileList.size()]);
		String[] expectedPaths = {fileUtilsDir + "test1.csv", fileUtilsDir + "test2.csv"};
		
		Assertions.assertEquals(2, filePaths.length, "find existing file");
		MatcherAssert.assertThat("all files found", Arrays.asList(filePaths), Matchers.containsInAnyOrder(expectedPaths));

				
		// find a file with an ending that does not exist in the directory
		fileList = FileUtils.findFilesByPattern(fileUtilsDir, "test", "der");
		filePaths = fileList.toArray(new String[fileList.size()]);
		Assertions.assertEquals(0, filePaths.length, "find non-existing file-end");

		// find a file with a pattern that does not exist in the directory
		fileList = FileUtils.findFilesByPattern(fileUtilsDir, "noFile", "csv");
		filePaths = fileList.toArray(new String[fileList.size()]);
		Assertions.assertEquals(0, filePaths.length, "find non-existing file-pattern");	
	}
	
	
	
	/**
	 * cleans up the test folders for the next test
	 */
	@AfterAll
	public static void tearDown() {
		// ensure that the directory in which things are copied is empty
		FileUtils.deleteDirContent(copyDir);
	}
	
	
	
	
	
}
