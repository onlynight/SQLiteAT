package demo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import com.github.onlynight.sqliteat.R;

import java.util.List;
import java.util.TimerTask;

import demo.database.TestDataBase;
import demo.database.dao.AnimalDao;
import demo.database.model.Animal;

public class MainActivity extends AppCompatActivity {

    private TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.textView);

        database();
    }

    private void database() {
        textView.post(new TimerTask() {
            @Override
            public void run() {
                TestDataBase dataBase = new TestDataBase(MainActivity.this);
                AnimalDao dao = new AnimalDao(dataBase);

                Animal animal = new Animal();
                animal.setName("animal");
                animal.setDetails("details");
                dao.insert(animal);

                List<Animal> animals = dao.selectAll();
                for (Animal temp : animals) {
                    System.out.println(temp);
                }
            }
        });
    }
}
