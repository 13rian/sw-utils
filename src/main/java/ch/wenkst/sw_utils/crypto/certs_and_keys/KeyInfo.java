package ch.wenkst.sw_utils.crypto.certs_and_keys;

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
