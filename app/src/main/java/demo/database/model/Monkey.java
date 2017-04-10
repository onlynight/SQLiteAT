package demo.database.model;

import com.github.onlynight.sqlite.table.annotation.Table;

@Table
public class Monkey extends Animal {

    private String monkey;

    public String getMonkey() {
        return monkey;
    }

    public void setMonkey(String monkey) {
        this.monkey = monkey;
    }

}
