package ch.wenkst.sw_utils.file.visitors;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.nio.file.attribute.BasicFileAttributes;

/**
 * the file visitor that is needed in order to copy a directory recursively
 * files that already exist in the destination will be replaced
 */
public class MergeFileVisitor implements FileVisitor<Path> {
	private boolean replaceFiles = true;
	private boolean isFirst = true; 	// true if no file was copied yet in this directory
	private Path cursor; 				// destination path

	public MergeFileVisitor(boolean replaceFiles, Path cursor) {
		this.replaceFiles = replaceFiles;
		this.cursor = cursor;
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
}
