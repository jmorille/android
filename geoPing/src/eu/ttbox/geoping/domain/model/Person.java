package eu.ttbox.geoping.domain.model;

import eu.ttbox.geoping.core.AppConstants;

public class Person {

	public long id = AppConstants.UNSET_ID;
	public String displayName;
	public String phone;
	public int color;
	public String contactId;
	public long pairingTime;

	// Encryption
	public String encryptionPubKey;
	public String encryptionPrivKey;
	public String encryptionRemotePubKey;
	public long encryptionRemoteTime;
	public String encryptionRemoteWay;
	
	public Person setId(long id) {
		this.id = id;
		return this;
	}

	public Person setDisplayName(String name) {
		this.displayName = name;
		return this;
	}

	public Person setPhone(String phone) {
		this.phone = phone;
		return this;
	}

	public Person setColor(int color) {
		this.color = color;
		return this;
	}

	public Person setContactId(String contactId) {
		this.contactId = contactId;
		return this;
	}

	public Person setPairingTime(long pairingTime) {
		this.pairingTime = pairingTime;
		return this;
	}

	public Person setEncryptionRemoteTime(long timeInMs) {
		this.encryptionRemoteTime =timeInMs;
		return this;
	}
	
	@Override
	public String toString() {
		return "Person [id=" + id + ", displayName=" + displayName + ", phone=" + phone + "]";
	}

	

}
