package ch.wenkst.sw_utils.files;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.nio.charset.Charset;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.stream.Stream;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class FileHandler {
	final static Logger logger = LogManager.getLogger(FileHandler.class);    // initialize the logger
	
	// define the constants to recursively copy a directory
	public static final int COMPLETE_REPLACE = 0; 	// the directory is completely replaced
	public static final int NO_REPLACE = 1; 		// if the directory already exists it will not be replaced
	public static final int MERGE_REPLACE = 2; 		// the directories will be merged, files that already exist will be replace 
	public static final int MERGE_NO_REPLACE = 3; 	// the directories will be merged, files that already exist will not be replace 
	


	/**
	 * opens a file and returns its content as String
	 * @param filePath	 	path to the file to read
	 * @return	 			String content of the file
	 */
	public static String readStrFromFile(String filePath) {
		String result = null;
		try {
			File file = new File(filePath);
			Scanner scanner = new Scanner(file);
			result = scanner.useDelimiter("\\Z").next();
			scanner.close();

		} catch (Exception e) {
			logger.error("error reading the String of the file: " + filePath, e);
		}

		return result;
	}


	/**
	 * opens a file and returns its content as String
	 * @param filePath	 	path to the file to read
	 * @param charset 		the encoding of the file (use the constants form StandardCharsets)
	 * @return	 			String content of the file
	 */
	public static String readStrFromFile(String filePath, Charset charset) {
		String result = null;
		try {
			File file = new File(filePath);
			Scanner scanner = new Scanner(file, charset.toString());
			result = scanner.useDelimiter("\\Z").next();
			scanner.close();

		} catch (Exception e) {
			logger.error("error reading the String of the file: " + filePath, e);
		}

		return result;
	}



	/**
	 * opens a file and returns its content as byte array
	 * @param filePath	 	path to the file to read
	 * @return	 			byte array content of the file
	 */
	public static byte[] readByteArrFromFile(String filePath) {
		try {
			return Files.readAllBytes(Paths.get(filePath, new String[0]));

		} catch (Exception e) {
			logger.error("error reading the byte array or the file: " + filePath, e);
			return null;
		}		
	}


	/**
	 * returns the absolute file path of a file in the searchDir that contains pattern and ends with end
	 * @param searchDir 	the directory in which the file is searched
	 * @param pattern 		some fraction of the file name, can be null
	 * @param end 			the ending of the file
	 * @return	 			an absolute file path
	 */
	public static String findFileByPattern(String searchDir, String pattern, String end) {
		File dir = new File(searchDir);
		if (!dir.exists()) { 
			return null;
		}

		File[] files = dir.listFiles();			// get all files in the directory
		for (File file : files) {
			if (file.isFile()) {
				String filename = file.getName();
				boolean isMatchingEnd = filename.endsWith(end);
				boolean isPatternFound = (pattern == null) || (filename.contains(pattern));
				if (isMatchingEnd && isPatternFound) {
					return file.getAbsolutePath();
				}
			}
		}
		return null;
	}



	/**
	 * writes a String to a file. If the passed directory does not exist it will be created. If the file already exists
	 * it will be overwritten.
	 * @param filePath	 	path of the file in which the String is written
	 * @param content 		content of the file
	 * @return	 			true if successfully written, false otherwise
	 */
	public static boolean writeStrToFile(String filePath, String content) {

		// create the file and ensure that the parent directories exist
		File file = new File(filePath);
		file.getParentFile().mkdirs();

		// write the new file
		try {
			PrintWriter pw = new PrintWriter(filePath);
			pw.println(content);
			pw.close();

			return true;

		} catch (FileNotFoundException e) {
			logger.error("error writing file: " + filePath, e);
			return false;
		}		
	}
	
	
	/**
	 * copies a file, the destination directory will be created if it does not exist
	 * @param srcFilePath 		the path of the source file to copy
	 * @param destFilePath 		the path of the destination file
	 * @param replaceExisting 	true if the file should be replaced if it already exists
	 * @return 					true if the file was successfully copied, false if an error occurred
	 */
	public static boolean copyFile(String srcFilePath, String destFilePath, boolean replaceExisting) {
		try {
			Path sourcePath = Paths.get(srcFilePath);
			Path destinationPath = Paths.get(destFilePath);

			// create the parent directory if it does not already exist
			Files.createDirectories(destinationPath.getParent());

			if (replaceExisting) {
				Files.copy(sourcePath, destinationPath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
			} else {
				Files.copy(sourcePath, destinationPath, StandardCopyOption.COPY_ATTRIBUTES);
			}
			return true;
			
		} catch (Exception e) {
			logger.error("error copying file form " + srcFilePath + " to " + destFilePath, e);
			return false;
		}
	}


	/**
	 * moves a file, the destination directory will be created if it does not exist
	 * @param srcFilePath 		the path of the source file to move
	 * @param destFilePath 		the path of the destination file
	 * @param replaceExisting 	true if the file should be replaced if it already exists
	 * @return 					true if the file was successfully moved, flase if an error occurred
	 */
	public static boolean moveFile(String srcFilePath, String destFilePath, boolean replaceExisting) {
		try {
			Path sourcePath = Paths.get(srcFilePath);
			Path destinationPath = Paths.get(destFilePath);

			// create the parent directory if it does not already exist
			Files.createDirectories(destinationPath.getParent());

			if (replaceExisting) {
				Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
			} else {
				Files.move(sourcePath, destinationPath);
			}
			return true;

		} catch (Exception e) {
			logger.error("error moving file form " + srcFilePath + " to " + destFilePath, e);
			return false;
		}
	}


	/**
	 * deletes a file, if the file does not exist nothing happens
	 * @param filePath	path of the file to delete
	 * @return 			true if the file was successfully deleted, false if an error occurred
	 */
	public static boolean deleteFile(String filePath) {
		try {
			// check if the file exists
			if (!Files.exists(Paths.get(filePath))) {
				return true;
			}
			
			Path path = Paths.get(filePath);
			Files.delete(path);
			return true;

		} catch (Exception e) {
			logger.error("error deleting the file " + filePath, e);
			return false;
		}
	}


	/**
	 * recursively copies a directory 
	 * @param srcDir 		the path of the source directory to copy
	 * @param destDir 		the path of the destination directory
	 * @param option		FileHandler.REPLACE: 			if the directory already exists it will be deleted first
	 * 						FileHAndler.NO_REPLACE			if the directory already exists nothing happens
	 *  					FileHandler.MERGE_REPLACE: 		if the directory already exists existing files will be overwritten
	 *  					FileHandler.MERGE_NO_REPLACE: 	if the directory already exists existing files will not be overwritten
	 * @return 				true if the directory was copied successfully, false if an error occurred	
	 */
	public static boolean copyDir(String srcDir, String destDir, int option) {
		try {
			// check if the destination directory already exists, do not follow symbolic links
			boolean dirExists = Files.exists(Paths.get(destDir), new LinkOption[] {LinkOption.NOFOLLOW_LINKS});
			
			// if the directory should not be replaced, check if it exists
			if (option == NO_REPLACE && dirExists) {
				logger.error("error copying dir " + srcDir + " to " + destDir + " destination directory already exists");
				return false;
			}
			
			// if the directory should be replaced delete it first
			if (option == COMPLETE_REPLACE  && dirExists) {
				boolean deleted = deleteDir(destDir);
				if (!deleted) {
					logger.error("error copying dir " + srcDir + " to " + destDir + " destination directory could not be deleted first");
					return false;
				}
			}
			
			// create the instance of the copy visitor
			MergeFileVisitor copyVisitor = null;
			if (option == MERGE_NO_REPLACE) {
				copyVisitor = new MergeFileVisitor(false);
			} else {
				copyVisitor = new MergeFileVisitor(true);
			}			
			
			copyVisitor.cursor = Paths.get(destDir);
			Files.walkFileTree(Paths.get(srcDir), copyVisitor);
			return true;

		} catch (Exception e) {
			logger.error("error copying the directory " + srcDir + " to " + destDir, e);
			return false;
		}
	}


	/**
	 * recursively deletes a directory
	 * @param dir 	the directory to delete
	 * @return 		true if the directory was deleted, false if an error occurred
	 */
	public static boolean deleteDir(String dir) {
		Path rootPath = Paths.get(dir);

		try {
			Files.walkFileTree(rootPath, new DeleteFileVisitor());
			return true;
			
		} catch(Exception e){
			logger.error("error deleting the directory " + dir, e);
			return false;
		}
	}



	/**
	 * returns all files in the passed directory and all its subdirectories
	 * @param dirName	name of the directory of which all files are returned as an array 	
	 * @return 			array of files
	 */
	public static File[] getFilesFromDir(String dirName) {
		File file = new File(dirName);
		File files[] = file.listFiles();

		return files;
	}



	/**
	 * reads the last lines of the passed file. The methods does not load the whole file into the memory
	 * by using RandomAccessFile. 
	 * @param filePath 		the path of the file from which the lines should be extracted
	 * @param lineCount 	the number of lines to extract, newlines are not ignored
	 * @return 				array list that contains the last lines of the passed file
	 */
	public static ArrayList<String> readLastLines(String filePath, int lineCount) {
		try {

			ArrayList<String> lines = new ArrayList<>();

			// create a new random access file instance
			RandomAccessFile raFile = new RandomAccessFile(filePath, "r");

			ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream(); 	// bytes in reversed order that hold one line
			long length = raFile.length();
			long bytePointer = length - 1; 		// points to the last byte in the file

			while (lines.size() < lineCount && bytePointer >=0) {
				// set the pointer of the raFile to the passed position 
				raFile.seek(bytePointer); 	// seek sets only a pointer and does not read in the whole file
				int b = raFile.read(); 		// read the byte at the current pointer position

				// check if the byte is a line feed
				if (b == 10) {
					// extract the line and add it to the array list of lines
					String line = getLine(baOutputStream);
					lines.add(0, line);
					baOutputStream.reset(); 	// reset the buffer

					// do not write carriage returns to the byte array output stream
				} else if (b != 13) {
					baOutputStream.write(b);
				}

				// decrease the pointer by one in order to set it to the next last byte
				bytePointer--;
			}

			// close the used buffers
			baOutputStream.close();
			raFile.close();

			return lines;

		} catch (Exception e) {
			logger.error("error reading the last lines of the passed file: ", e);
			return new ArrayList<>();
		}
	}


	/**
	 * extract a line from the passed baOutputStream, which is in reverse order
	 * @param baOutputStream 	the buffer containing the bytes of the line in reversed order
	 * @return 					the line as string
	 */
	private static String getLine(ByteArrayOutputStream baOutputStream) {
		byte[] a = baOutputStream.toByteArray();

		// reverse bytes
		for (int i = 0, j = a.length - 1; j > i; i++, j--) {
			byte tmp = a[j];
			a[j] = a[i];
			a[i] = tmp;
		}
		return new String(a);
	}


	
	/**
	 * reads the first line of a file
	 * @param filePath 	the path to the file
	 * @return 			the first line of the file or null if the file is empty or an error occurred 
	 */
	public static String readFirstLine(String filePath) {
		String result = null;

		try {
			File file = new File(filePath);
			BufferedReader reader = new BufferedReader(new FileReader(file));
			result = reader.readLine();
			reader.close();
			
		} catch (Exception e) {
			logger.error("first line of the file " + filePath + " could not be read: ", e);
		}
		
		return result;
	}
	
	
	/**
	 * reads the nth line of a file
	 * @param filePath 		the path of the file
	 * @param lineNumber 	the line number that should be read (starting form 1)
	 * @return 				the nth line or null if an error occurred
	 */
	public static String readNthLine(String filePath, long lineNumber) {
		String result = null;
		
		try {
			// for small files
			// String line32 = Files.readAllLines(Paths.get("file.txt")).get((int)lineNumber);

			// for large files
			Stream<String> lines = Files.lines(Paths.get(filePath)); 
			result = lines.skip(lineNumber-1).findFirst().get();
			lines.close();
			
		} catch (Exception e) {
			 logger.error("nth line number of the file " + filePath + "could not be read: ", e);
		}
		
		return result;
	}
	
	
	/**
	 * appends text to a file with random access file, the method does not get slower with increasing file size
	 * @param filePath 		the path of the file
	 * @param text 			the string that is appended at the end of the file
	 * @param charset 		the encoding of the file (use the constants form StandardCharsets)
	 */
	public static void append(String filePath, String text, Charset charset) {
		byte[] bytes = text.getBytes(charset);
		append(filePath, bytes);
	}

	
	/**
	 * appends bytes to a file with random access file, the method does not get slower with increasing file size
	 * @param filePath 	the path of the file
	 * @param bytes 	the bytes that are appended
	 */
	public static void append(String filePath, byte[] bytes) {
		try {
			File f = new File(filePath);
			long fileLength = f.length();
			RandomAccessFile raf = new RandomAccessFile(f, "rw");
			raf.seek(fileLength);
			raf.write(bytes);
			raf.close();
			
		} catch (Exception e) {
			logger.error("error appending to the file " + filePath + ": ", e);
		}
	}


	
	/**
	 * the file visitor that is needed in order to copy a directory recursively
	 * files that already exist in the destination will be replaced
	 */
	private static class MergeFileVisitor implements FileVisitor<Path> {
		private boolean replaceFiles = true;
		private boolean isFirst = true; 	// true if no file was copied yet in this directory
		private Path cursor; 				// destination path
		
		private MergeFileVisitor(boolean replaceFiles) {
			this.replaceFiles = replaceFiles;
		}

		@Override
		public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
			// Move path forward
			if (!isFirst) {
				// but not for the first time since cursor is already in there
				Path target = cursor.resolve(dir.getName(dir.getNameCount() - 1));
				cursor = target;
			}
			
			// only copy the directory if it does not already exist
			boolean dirExists = Files.exists(cursor, new LinkOption[] {LinkOption.NOFOLLOW_LINKS});
			if (!dirExists) {
				Files.copy(dir, cursor, StandardCopyOption.COPY_ATTRIBUTES);
			}
			
			isFirst = false;
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			Path target = cursor.resolve(file.getFileName());
			
			if (replaceFiles) {
				// replace the file in the destination directory
				Files.copy(file, target, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
			} else {
				boolean fileExists = Files.exists(target, new LinkOption[] {LinkOption.NOFOLLOW_LINKS});
				if (!fileExists) {
					Files.copy(file, target, StandardCopyOption.COPY_ATTRIBUTES);
				}
			}				
			
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc) throws IOException {
			throw exc;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			// move the cursor backwards
			Path target = cursor.getParent();
			cursor = target;
			return FileVisitResult.CONTINUE;
		}
	};	
	

	
	/**
	 * the file visitor that is used to recursively delete a directory
	 */
	private static class DeleteFileVisitor extends SimpleFileVisitor<Path> {
		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
			Files.delete(file);
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
			Files.delete(dir);
			return FileVisitResult.CONTINUE;
		}
	}




}
