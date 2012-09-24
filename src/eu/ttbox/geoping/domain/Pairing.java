package eu.ttbox.geoping.domain;

public class Pairing {

    public long id = -1;
    public String name;
    public String phone;
    public int color;

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

    public Pairing setColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    public String toString() {
        return "Person [id=" + id + ", name=" + name + ", phone=" + phone + "]";
    }

}
