package eu.ttbox.smstraker.domain;

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
    
    
    
 
}
