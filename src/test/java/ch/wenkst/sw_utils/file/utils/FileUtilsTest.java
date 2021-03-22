package ch.wenkst.sw_utils.file.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import ch.wenkst.sw_utils.file.CopyDirMode;
import ch.wenkst.sw_utils.file.FileUtils;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class FileUtilsTest {
	private FileUtilsFiles testFiles = new FileUtilsFiles();

	
	@BeforeAll
	@AfterAll
	public void initializeExternalResources() throws IOException {
		testFiles.deleteAllTestFiles();	
	}
	
	
	@Test
	public void readLastLinesOfFile() throws IOException {
		List<String> lines = FileUtils.readLastLines(testFiles.manyLinesFile, 2);
		Assertions.assertEquals("Now I’m seeing things", lines.get(0));
		Assertions.assertEquals("That only dead men do", lines.get(1));
	}
	
	
	@Test
	public void readAllLinesOfFile() throws IOException {
		List<String> lines = FileUtils.readLastLines(testFiles.manyLinesFile, 37);
		Assertions.assertEquals(35, lines.size());
	}
	
	
	@Test
	public void readFileToString() throws FileNotFoundException {
		String fileContent = FileUtils.readStrFromFile(testFiles.oneLineFile);
		Assertions.assertEquals("First line of the text file.", fileContent);
	}
	
	
	@Test
	public void dumpOneObjToFile() throws IOException, ClassNotFoundException {
		DataStorage dataStorage = DataStorage.getRandomStorage();
	
		FileUtils.objectToFile(testFiles.dumpObjectFile, dataStorage);
		DataStorage readDataStorage = (DataStorage) FileUtils.objectFromFile(testFiles.dumpObjectFile);
		
		Assertions.assertEquals(dataStorage, readDataStorage);
	}

	
	public void dumpMultipleObjsToFile() throws IOException, ClassNotFoundException {
		DataStorage dataStorage1 = DataStorage.getRandomStorage();
		DataStorage dataStorage2 = DataStorage.getRandomStorage();
		List<DataStorage> dataStorageList = Arrays.asList(dataStorage1, dataStorage2);
		
		
		FileUtils.objectsToFile(testFiles.dumpObjectFile, dataStorageList);
		List<DataStorage> readDataStorageList = FileUtils.objectsFromFile(testFiles.dumpObjectFile);
		
		Assertions.assertEquals(dataStorage1, readDataStorageList.get(0));
		Assertions.assertEquals(dataStorage2, readDataStorageList.get(0));
	}
	
	
	@Test
	public void copyFile() throws IOException {
		boolean replaceExisting = true;
		String dest = testFiles.copyDir + File.separator + "copiedFile.txt";
		FileUtils.copyFile(testFiles.oneLineFile, dest, replaceExisting);
		Assertions.assertTrue(new File(dest).exists());
	}
	
	
	@Test
	public void moveFile() throws IOException {
		String fileToMove = testFiles.fileUtilsDir + "fileToMove.txt";
		new File(fileToMove).createNewFile();
		
		boolean replaceExisting = true;
		String destFile = testFiles.copyDir + File.separator + "fileToMove.txt";
		FileUtils.moveFile(fileToMove, destFile, replaceExisting);
		
		Assertions.assertTrue(new File(destFile).exists());
	}
	
	
	@Test
	public void deleteFile() throws IOException {
		String fileToDelete = testFiles.fileUtilsDir + "fileToDelete.txt";
		new File(fileToDelete).createNewFile();

		FileUtils.deleteFile(fileToDelete);
		Assertions.assertFalse(new File(fileToDelete).exists());
	}
	
	
	@Test
	public void copyDirectory() throws IOException {
		String destDir = testFiles.copyDir + File.separator + "copiedDir";
		String dirToCopy = testFiles.dirToCopy;
		FileUtils.copyDir(dirToCopy, destDir, CopyDirMode.COMPLETE_REPLACE);
		
		Assertions.assertTrue(new File(destDir).exists());
		

		String fileInTopDir = destDir + File.separator + "test2.txt";
		Assertions.assertTrue(new File(fileInTopDir).exists());
		
		String fileInSubDir = destDir + File.separator + "subdir1" + File.separator + "test3.txt";
		Assertions.assertTrue(new File(fileInSubDir).exists());
		
		String fileInSubSubDir = destDir + File.separator + "subdir1" + File.separator + "subsubdir2" + File.separator + "test1.txt";
		Assertions.assertTrue(new File(fileInSubSubDir).exists());
	}
	
	
	@Test
	public void deleteDirectory() throws IOException {		
		String dirToDelete = testFiles.createDirToDelete();		
		FileUtils.deleteDir(dirToDelete);
		Assertions.assertFalse(new File(dirToDelete).exists());
	}
	
	
	@Test
	public void readNthLineOfFile() throws IOException {
		String firstLine = FileUtils.readFirstLine(testFiles.manyLinesFile);
		Assertions.assertEquals("I bought a dead mans suit", firstLine);
		
		String line24 = FileUtils.readNthLine(testFiles.manyLinesFile, 24);
		Assertions.assertEquals("Is this sour taste", line24);
		
		String line30 = FileUtils.readNthLine(testFiles.manyLinesFile, 30);
		Assertions.assertEquals("I couldn’t take it back", line30);
	}
	
	
	@Test
	public void findOneFileByPattern() {
		String filePath = FileUtils.findFileByPattern(testFiles.fileUtilsDir, "2", "csv");
		String expectedFilePath = testFiles.fileUtilsDir + "test2.csv";
		Assertions.assertEquals(expectedFilePath, filePath);
	}
	
	
	@Test
	public void findOneFileByPatternWithNonExistingEnding() {
		String filePath = FileUtils.findFileByPattern(testFiles.fileUtilsDir, "test", "der");
		Assertions.assertNull(filePath);
	}
	
	
	@Test
	public void findOneFileByPatternWithNonExistingPattern() {
		String filePath = FileUtils.findFileByPattern(testFiles.fileUtilsDir, "noFile", "csv");
		Assertions.assertEquals(null, filePath);	
	}
	
	
	@Test
	public void findMultipleFilesByPattern() {
		List<String> fileList = FileUtils.findFilesByPattern(testFiles.fileUtilsDir, "test", "csv");
		String[] expectedPaths = {
				testFiles.fileUtilsDir + "test1.csv",
				testFiles.fileUtilsDir + "test2.csv"};
		
		Assertions.assertEquals(2, fileList.size());
		MatcherAssert.assertThat(fileList, Matchers.containsInAnyOrder(expectedPaths));
	}
	
	
	@Test
	public void findMultipleFilesByPatternWithNonExistingEnding() {
		List<String> fileList = FileUtils.findFilesByPattern(testFiles.fileUtilsDir, "test", "der");
		Assertions.assertEquals(0, fileList.size());
	}
	
	
	@Test
	public void findMultipleFilesByPatternWithNonExistingPattern() {
		List<String> fileList = FileUtils.findFilesByPattern(testFiles.fileUtilsDir, "noFile", "csv");
		Assertions.assertEquals(0, fileList.size());	
	}
	
	
	@Test
	public void readFileExtension() {
		String fileExtension = FileUtils.fileExtension("hallo/test.json");
		Assertions.assertEquals(".json", fileExtension);
	}
	
	
	@Test
	public void readRawFileName() {
		String rawName = FileUtils.rawFileName("hallo/test.xml");
		Assertions.assertEquals("test", rawName);
	}
}
