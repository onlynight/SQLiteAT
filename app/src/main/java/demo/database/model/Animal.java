package demo.database.model;

import com.github.onlynight.sqlite.table.annotation.Table;

@Table
public class Animal {

    private int id;
    private String name;
    private String details;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDetails() {
        return details;
    }

    public void setDetails(String details) {
        this.details = details;
    }

    @Override
    public String toString() {
        return "name = " + name + " details = " + details;
    }
}
