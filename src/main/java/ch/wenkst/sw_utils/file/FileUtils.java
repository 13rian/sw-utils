package ch.wenkst.sw_utils.file;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.wenkst.sw_utils.file.visitors.DeleteFileVisitor;
import ch.wenkst.sw_utils.file.visitors.MergeFileVisitor;

public class FileUtils {
	private static final Logger logger = LoggerFactory.getLogger(FileUtils.class);
	
	
	private FileUtils() {
		
	}
	

	/**
	 * opens a file and returns its content as String
	 * @param filePath	 	path to the file to read
	 * @return	 			String content of the file
	 * @throws FileNotFoundException 
	 */
	public static String readStrFromFile(String filePath) throws FileNotFoundException {
		File file = new File(filePath);
		Scanner scanner = new Scanner(file);
		String content = scanner.useDelimiter("\\Z").next();
		scanner.close();
		return content;
	}


	/**
	 * opens a file and returns its content as String
	 * @param filePath	 	path to the file to read
	 * @param charset 		the encoding of the file (use the constants form StandardCharsets)
	 * @return	 			String content of the file
	 * @throws FileNotFoundException 
	 */
	public static String readStrFromFile(String filePath, Charset charset) throws FileNotFoundException {
		File file = new File(filePath);
		Scanner scanner = new Scanner(file, charset.toString());
		String content = scanner.useDelimiter("\\Z").next();
		scanner.close();
		return content;
	}



	/**
	 * opens a file and returns its content as byte array
	 * @param filePath	 	path to the file to read
	 * @return	 			byte array content of the file
	 * @throws IOException 
	 */
	public static byte[] readByteArrFromFile(String filePath) throws IOException {
		return Files.readAllBytes(Paths.get(filePath, new String[0]));	
	}


	/**
	 * returns the absolute file path of a file in the searchDir that contains pattern and ends with end
	 * @param searchDir 	the directory in which the file is searched
	 * @param pattern 		some fraction of the file name, can be null
	 * @param end 			the ending of the file
	 * @return	 			an absolute file path or null if no file was found matching the pattern
	 */
	public static String findFileByPattern(String searchDir, String pattern, String end) {
		File dir = new File(searchDir);
		if (!dir.exists()) { 
			return null;
		}

		File[] files = dir.listFiles((directory, fileName) -> {
			return fileName.endsWith(end) && (pattern == null || fileName.contains(pattern));
		});

		if (files.length == 0) {
			return null;
		} else {
			return files[0].getAbsolutePath();
		}
	}


	/**
	 * returns an array of all absolute file paths of files in the searchDir that contain pattern and end with end
	 * @param searchDir 	the directory in which the file is searched
	 * @param pattern 		some fraction of the file name, can be null
	 * @param end 			the ending of the file
	 * @return	 			list of absolute file paths, an empty array if no files matching the pattern were found
	 */
	public static List<String> findFilesByPattern(String searchDir, String pattern, String end) {
		File dir = new File(searchDir);
		if (!dir.exists()) { 
			return new ArrayList<>();
		}

		File[] files = dir.listFiles((directory, fileName) -> fileName.endsWith(end) && (pattern == null || fileName.contains(pattern)));

		List<String> result = new ArrayList<>();
		for (File file : files) {
			result.add(file.getAbsolutePath());
		}

		return result;
	}



	/**
	 * writes a String to a file. If the passed directory does not exist it will be created. If the file already exists
	 * it will be overwritten.
	 * @param filePath	 	path of the file in which the string is written
	 * @param content 		content of the file
	 * @throws FileNotFoundException 
	 */
	public static void writeStrToFile(String filePath, String content) throws FileNotFoundException {
		File file = new File(filePath);
		if(!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}

		PrintWriter pw = new PrintWriter(filePath);
		pw.println(content);
		pw.close();	
	}
	
	
	/**
	 * writes the passed input stream to a file, if the file already exists its content will be replaced
	 * @param filePath 			path of the file that is written
	 * @param is				the input stream with the data
	 * @throws IOException
	 */
	public static void inputStreamToFile(String filePath, InputStream is) throws IOException {
		File file = new File(filePath);
		if(!file.getParentFile().exists()) {
			file.getParentFile().mkdirs();
		}

		Files.copy(
				is, 
				file.toPath(), 
				StandardCopyOption.REPLACE_EXISTING);
	}
	
	
	/**
	 * writes the passed object to a file
	 * @param filePath 			file to which the objects are written to
	 * @param serializable 		object to dump to file
	 * @throws IOException
	 */
	public static void objectToFile(String filePath, Serializable serializable) throws IOException {
		File file = new File(filePath);
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(serializable);

		oos.close();
		fos.close();
	}
	
	
	/**
	 * reads an object from the passed file
	 * @param filePath 			file containing the objects
	 * @return 					object that was read from the file
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	public static Serializable objectFromFile(String filePath) throws IOException, ClassNotFoundException {
		File file = new File(filePath);
		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(fis);
		Serializable serializable = (Serializable) ois.readObject();

		ois.close();
		fis.close();
		return serializable;
	}	
	
	
	/**
	 * writes the passed objects to a file
	 * @param filePath 			file to which the objects are written to
	 * @param serializables 	list of object to dump to file
	 * @throws IOException
	 */
	public static void objectsToFile(String filePath, List<? extends Serializable> serializables) throws IOException {
		File file = new File(filePath);
		FileOutputStream fos = new FileOutputStream(file);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		
		for (Serializable serializable : serializables) {
			oos.writeObject(serializable);
		}

		oos.close();
		fos.close();
	}
	
	
	/**
	 * reads a list of objects form the passed file
	 * @param <T>
	 * @param filePath 			file containing the objects
	 * @return 					list of objects that were read from the file
	 * @throws IOException
	 * @throws ClassNotFoundException 
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> objectsFromFile(String filePath) throws IOException, ClassNotFoundException {
		File file = new File(filePath);
		FileInputStream fis = new FileInputStream(file);
		ObjectInputStream ois = new ObjectInputStream(fis);
	
		List<T> serializables = new ArrayList<>();
		boolean endOfFile = false;
		while (!endOfFile) {
			try {
				T serializable = (T) ois.readObject();
				serializables.add(serializable);
			} catch (EOFException e) {
				endOfFile = true;
			}
		}

		ois.close();
		fis.close();
		return serializables;
	}


	/**
	 * copies a file, the destination directory will be created if it does not exist
	 * @param srcFilePath 		the path of the source file to copy
	 * @param destFilePath 		the path of the destination file
	 * @param replaceExisting 	true if the file should be replaced if it already exists
	 * @return 					true if the file was successfully copied, false if an error occurred
	 * @throws IOException 
	 */
	public static void copyFile(String srcFilePath, String destFilePath, boolean replaceExisting) throws IOException {
		Path sourcePath = Paths.get(srcFilePath);
		Path destinationPath = Paths.get(destFilePath);

		Files.createDirectories(destinationPath.getParent());

		if (replaceExisting) {
			Files.copy(sourcePath, destinationPath, StandardCopyOption.COPY_ATTRIBUTES, StandardCopyOption.REPLACE_EXISTING);
		} else {
			Files.copy(sourcePath, destinationPath, StandardCopyOption.COPY_ATTRIBUTES);
		}
	}


	/**
	 * moves a file, the destination directory will be created if it does not exist
	 * @param srcFilePath 		the path of the source file to move
	 * @param destFilePath 		the path of the destination file
	 * @param replaceExisting 	true if the file should be replaced if it already exists
	 * @return 					true if the file was successfully moved, false if an error occurred
	 * @throws IOException 
	 */
	public static void moveFile(String srcFilePath, String destFilePath, boolean replaceExisting) throws IOException {
		Path sourcePath = Paths.get(srcFilePath);
		Path destinationPath = Paths.get(destFilePath);

		Files.createDirectories(destinationPath.getParent());

		if (replaceExisting) {
			Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
		} else {
			Files.move(sourcePath, destinationPath);
		}
	}


	/**
	 * deletes a file, if the file does not exist nothing happens
	 * @param filePath	path of the file to delete
	 * @return 			true if the file was successfully deleted, false if an error occurred
	 * @throws IOException 
	 */
	public static void deleteFile(String filePath) throws IOException {
		if (!Files.exists(Paths.get(filePath))) {
			return;
		}

		Path path = Paths.get(filePath);
		Files.delete(path);
	}


	/**
	 * recursively copies a directory 
	 * @param srcDir 		the path of the source directory to copy
	 * @param destDir 		the path of the destination directory
	 * @param copyDirMode	COMPLETE_REPLACE: 	if the directory already exists it will be deleted first
	 * 						NO_REPLACE:			if the directory already exists nothing happens
	 *  					MERGE_REPLACE: 		if the directory already exists existing files will be overwritten
	 *  					MERGE_NO_REPLACE: 	if the directory already exists existing files will not be overwritten
	 * @return 				true if the directory was copied successfully, false if an error occurred	
	 * @throws IOException 
	 */
	public static void copyDir(String srcDir, String destDir, CopyDirMode copyDirMode) throws IOException {
		boolean dirExists = Files.exists(Paths.get(destDir), new LinkOption[] {LinkOption.NOFOLLOW_LINKS});

		// if the directory should not be replaced, check if it exists
		if (copyDirMode.equals(CopyDirMode.NO_REPLACE) && dirExists) {
			logger.error("error copying dir " + srcDir + " to " + destDir + " destination directory already exists");
			return;
		}

		// if the directory should be replaced delete it first
		if (copyDirMode.equals(CopyDirMode.COMPLETE_REPLACE)  && dirExists) {
			deleteDir(destDir);
		}

		// create the instance of the copy visitor
		MergeFileVisitor copyVisitor;
		Path destDirPath = Paths.get(destDir);
		if (copyDirMode.equals(CopyDirMode.MERGE_NO_REPLACE)) {
			copyVisitor = new MergeFileVisitor(false, destDirPath);
		} else {
			copyVisitor = new MergeFileVisitor(true, destDirPath);
		}			

		Files.walkFileTree(Paths.get(srcDir), copyVisitor);
	}


	/**
	 * recursively deletes a directory
	 * @param dir 	the directory to delete
	 * @return 		true if the directory was deleted, false if an error occurred
	 * @throws IOException 
	 */
	public static void deleteDir(String dir) throws IOException {
		Path rootPath = Paths.get(dir);
		Files.walkFileTree(rootPath, new DeleteFileVisitor());
	}


	/**
	 * deletes the content of the passed directory
	 * @param dir 	the path of the directory of which the content should e deleted
	 * @return 		true if the directory was emptied, false if an error occurred
	 * @throws IOException 
	 */
	public static void deleteDirContent(String dir) throws IOException {
		deleteDir(dir);
		new File(dir).mkdirs();
	}



	/**
	 * reads the last lines of the passed file. The methods does not load the whole file into the memory
	 * by using RandomAccessFile. As RandomAccessFile is used the eecution time of the method will be
	 * independent of the file size.
	 * @param filePath 		the path of the file from which the lines should be extracted
	 * @param lineCount 	the number of lines to extract, newlines are not ignored
	 * @return 				array list that contains the last lines of the passed file or null if an error occurred
	 * @throws IOException 
	 */
	public static List<String> readLastLines(String filePath, int lineCount) throws IOException {
		List<String> lines = new ArrayList<>();

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


		// get the first line of the file that has no new line character in front of it
		if (lines.size() < lineCount) {
			// extract the line and add it to the array list of lines
			String line = getLine(baOutputStream);
			lines.add(0, line);
		}

		// close the used buffers
		baOutputStream.close();
		raFile.close();

		return lines;
	}


	/**
	 * extract a line from the passed baOutputStream, which is in reverse order
	 * @param baOutputStream 	the buffer containing the bytes of the line in reversed order
	 * @return 					the line as string
	 */
	private static String getLine(ByteArrayOutputStream baOutputStream) {
		byte[] a = baOutputStream.toByteArray();

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
	 * @throws IOException 
	 */
	public static String readFirstLine(String filePath) throws IOException {
		return readNthLine(filePath, 1);
	}


	/**
	 * reads the nth line of a file
	 * @param filePath 		the path of the file
	 * @param lineNumber 	the line number that should be read (starting form 1)
	 * @return 				the nth line or null if an error occurred
	 * @throws IOException 
	 */
	public static String readNthLine(String filePath, long lineNumber) throws IOException {
		Stream<String> lines = Files.lines(Paths.get(filePath)); 
		String result = lines.skip(lineNumber-1).findFirst().get();
		lines.close();
		return result;
	}


	/**
	 * appends text to a file with random access file, the method does not get slower with increasing file size
	 * @param filePath 		the path of the file
	 * @param text 			the string that is appended at the end of the file
	 * @param charset 		the encoding of the file (use the constants form StandardCharsets)
	 * @throws IOException 
	 */
	public static void append(String filePath, String text, Charset charset) throws IOException {
		byte[] bytes = text.getBytes(charset);
		append(filePath, bytes);
	}


	/**
	 * appends bytes to a file with random access file, the method does not get slower with increasing file size
	 * @param filePath 	the path of the file
	 * @param bytes 	the bytes that are appended
	 * @throws IOException 
	 */
	public static void append(String filePath, byte[] bytes) throws IOException {
		File f = new File(filePath);
		long fileLength = f.length();
		RandomAccessFile raf = new RandomAccessFile(f, "rw");
		raf.seek(fileLength);
		raf.write(bytes);
		raf.close();
	}


	/**
	 * returns the extension of the passed file path
	 * @param filePath 		the path of the file from which the extension is extracted
	 * @return 				the extension of the file or null if there is none
	 */
	public static String fileExtension(String filePath) {
		String fileExtension = null;
		if (filePath.contains(".")) {
			Path path = Paths.get(filePath);
			String fileName = path.getFileName().toString();
			fileExtension = fileName.substring(fileName.lastIndexOf("."));
		}

		return fileExtension;
	}
	
	
	/**
	 * returns the fileName without the extension
	 * @param filePath 		the path of the file from which the name should be extracted
	 * @return 				the name of the file without the extension, or the file name
	 * 						if there is not extension
	 */
	public static String rawFileName(String filePath) {
		Path path = Paths.get(filePath);
		String fileName = path.getFileName().toString();
		if (filePath.contains(".")) {
			return fileName.substring(0, fileName.lastIndexOf("."));
		} else {
			return fileName;
		}
	}
}
