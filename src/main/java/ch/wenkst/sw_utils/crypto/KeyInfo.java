package ch.wenkst.sw_utils.crypto;

import ch.wenkst.sw_utils.crypto.SecurityUtils.FileFormat;
import ch.wenkst.sw_utils.crypto.SecurityUtils.KeyFormat;
import ch.wenkst.sw_utils.crypto.SecurityUtils.KeyType;

public class KeyInfo {
	public byte[] pkcs8KeyBytes;
	public FileFormat fileFormat;
	public KeyType keyType;
	public KeyFormat keyFormat;
	
	public void addKeyInfo(FileFormat fileFormat, KeyType keyType, KeyFormat keyFormat) {
		this.fileFormat = fileFormat;
		this.keyType = keyType;
		this.keyFormat = keyFormat;
	}
}
