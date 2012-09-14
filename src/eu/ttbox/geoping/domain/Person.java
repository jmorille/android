package eu.ttbox.geoping.domain;

public class Person {
 
    public long id = -1;
    public String name;
    public String phone;
    
    
    public Person setId(long id) {
        this.id = id;
        return this;
    }
    public Person setName(String name) {
        this.name = name;
        return this;
    }
    public Person setPhone(String phone) {
        this.phone = phone;
        return this;
    }
    @Override
    public String toString() {
        return "Person [id=" + id + ", name=" + name + ", phone=" + phone + "]";
    }
    
    
    
 
}
