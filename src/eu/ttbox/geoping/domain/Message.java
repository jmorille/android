package eu.ttbox.geoping.domain;

public class Message {

    public long id = -1;
    public String name;
    public String phone;
    public int color;

    public Message setId(long id) {
        this.id = id;
        return this;
    }

    public Message setName(String name) {
        this.name = name;
        return this;
    }

    public Message setPhone(String phone) {
        this.phone = phone;
        return this;
    }

    public Message setColor(int color) {
        this.color = color;
        return this;
    }

    @Override
    public String toString() {
        return "Person [id=" + id + ", name=" + name + ", phone=" + phone + "]";
    }

}
