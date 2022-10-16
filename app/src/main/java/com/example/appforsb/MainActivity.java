package com.example.appforsb;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        EditText editText = (EditText) findViewById(R.id.inputText);
        editText.setOnKeyListener(new View.OnKeyListener(){
            public boolean onKey(View v, int keyCode, KeyEvent event){
                if(event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER)){ // считываем строку, если введен символ '\n'
                    String searchText = editText.getText().toString();
                    Intent intent = new Intent(MainActivity.this, ResultActivity.class);
                    intent.putExtra("text", searchText);
                    startActivity(intent);
                    return true;
                }
                else return false;
            }
        });
    }


    public void search(View view){ // при нажатии на картинку поиска
        EditText editText = (EditText) findViewById(R.id.inputText);
        String searchText = editText.getText().toString();
        Intent intent = new Intent(MainActivity.this, ResultActivity.class);
        intent.putExtra("text", searchText);
        startActivity(intent);
    }
}