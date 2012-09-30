package eu.ttbox.geoping.domain.model;

public class Pairing {

    public long id = -1;
    public String name;
    public String phone;
    public PairingAuthorizeTypeEnum authorizeType;
    public boolean showNotification = false;

    public Pairing setId(long id) {
        this.id = id;
        return this;
    }

    public Pairing setName(String name) {
        this.name = name;
        return this;
    }

    public Pairing setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public Pairing setAuthorizeType(PairingAuthorizeTypeEnum color) {
        this.authorizeType = color;
        return this;
    }

    public Pairing setShowNotification(boolean showNotification) {
        this.showNotification = showNotification;
        return this;
    }

    @Override
    public String toString() {
        return "Person [id=" + id + ", name=" + name + ", phone=" + phone + "]";
    }

}
