package com.example.appforsb;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.example.appforsb.adapter.PatentAdapter;
import com.example.appforsb.model.Patent;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class PatentActivity extends AppCompatActivity {
    RecyclerView patentRecycler; // View, в которой находятся карточки
    PatentAdapter patentAdapter; // адаптер
    List<Patent> patentsList = new ArrayList<>(); // Лист патентов (добавляется из ответов сервера)
    String id ="";
    boolean flagClose = true; // флаг, скрыта ли информация
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patent);
        init(); // Заполняем все строки
    }

    private void init(){
        patentRecycler = findViewById(R.id.patentItems);
        String inventor = getIntent().getStringExtra("inventor");
        String number = getIntent().getStringExtra("number");
        String name = getIntent().getStringExtra("name");
        String publication_date = getIntent().getStringExtra("publication_date");
        String filing_date = getIntent().getStringExtra("filing_date");
        String patente = getIntent().getStringExtra("patente");
        String ipc = getIntent().getStringExtra("ipc");
        String priority = getIntent().getStringExtra("priority");
        String description = getIntent().getStringExtra("description");
        id = getIntent().getStringExtra("id");
        TextView namev = findViewById(R.id.name);
        namev.setText(Html.fromHtml(name));
        TextView numberv = findViewById(R.id.number);
        numberv.setText(number);
        TextView inventorv = findViewById(R.id.author);
        inventorv.setText("Авторы: " + inventor);
        TextView publication_datev = findViewById(R.id.datePulic);
        publication_datev.setText(publication_date);
        TextView filing_datev = findViewById(R.id.datePod);
        filing_datev.setText(filing_date);
        TextView patentev = findViewById(R.id.patente);
        patentev.setText(patente);
        TextView descriptionv = findViewById(R.id.description);
        descriptionv.setText(Html.fromHtml(description));
        TextView priorityv = findViewById(R.id.priority);
        priorityv.setText(priority);
        TextView ipcv = findViewById(R.id.ipc);
        ipcv.setText(ipc);
    }


    public class SetPatentList extends AsyncTask<String, String, String> {
        JSONObject obj = null; // json ответ сервера
        protected void onPriExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    findViewById(R.id.textPohozh).setVisibility(View.GONE); // Устанавливаем текст "Загрузка"
                    findViewById(R.id.patentItems).setVisibility(View.VISIBLE); // Устанавливаем текст "Загрузка"
                    TextView textView = findViewById(R.id.error);
                    textView.setVisibility(View.VISIBLE);
                    textView.setText("Загрузка...");
                }
            });
            String url_s = "https://searchplatform.rospatent.gov.ru/patsearch/v0.2/similar_search";
            String key = "26a213594e7f4f6e8cd89064d885ea93"; // токен
            URL url;
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            try {
                url = new URL(url_s);
                // тело запроса
                String jsonInputString = "{\"type_search\": \"id_search\", \"pat_id\": \"" + id + "\", \"count\": 10}";
                byte[] out = jsonInputString.getBytes("utf-8"); // тело запроса Json
                connection = (HttpURLConnection) url.openConnection(); // устанавливаем url в коннект
                connection.setRequestMethod("POST"); // устанавливаем метод
                connection.addRequestProperty("Authorization", key); // В headers указываем токен
                connection.addRequestProperty("Content-Type", "application/json"); // указываем тип тела запроса
                connection.connect(); // открываем коннект
                OutputStream os = connection.getOutputStream();
                os.write(out, 0, out.length); // вводим тело запроса
                os.close();
                InputStream stream = connection.getInputStream();
                reader = new BufferedReader(new InputStreamReader(stream));
                StringBuffer buffer = new StringBuffer();
                String line = "";
                while ((line = reader.readLine()) != null) {
                    buffer.append(line).append("\n"); // собираем ответ
                }
                return buffer.toString(); // Ответ от сервера в формате строки

            } catch (MalformedURLException e) {
                e.printStackTrace();
            } catch (IOException e) {
                //Устновить соединение не удалось
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.patentItems).setVisibility(View.GONE); // скрываем карточки патентов
                        findViewById(R.id.textPohozh).setVisibility(View.VISIBLE);
                        TextView text = findViewById(R.id.error);
                        text.setText("Произошла ошибка!");
                        text.setVisibility(View.VISIBLE);
                    }
                });
                e.printStackTrace();

            } finally {
                if (connection != null)
                    connection.disconnect(); // закрываем коннект
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();

                    }
                }

            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            if (result != null) { // Если считать ответ удалось
                try {
                    obj = new JSONObject(result); // Переводим ответ из строки в json
                    JSONArray patents = null; // json массив патентов
                    try {
                        patents = obj.getJSONArray("data");
                    }
                    catch (JSONException e) {
                        e.printStackTrace();
                    }
                    int count = 0; // Количество добавленных патентов в адаптер
                    for (int i = 0; i < patents.length(); i++) { // Проходим по массиву патентов
                        boolean flag_ru = true; // Если есть необходимые данные для создания карточки
                        String inventorCard = "", ipcCard = ""; // Автор и МПК для карточки
                        String number = "", name = "", publication_date = "", filing_date = ""; // поля для страницы с полной инфой
                        String inventor = "", patente = "", ipc = "";
                        String description = "";
                        String language = "";
                        String priority = "";
                        String id = "";
                        JSONObject patent = null;
                        String country_s = "";
                        try {
                            // Разбираем json
                            patent = patents.getJSONObject(i);
                            JSONObject common = patent.getJSONObject("common");
                            JSONObject application = common.getJSONObject("application");
                            language = patent.getJSONObject("snippet").getString("lang");
                            JSONObject lang = patent.getJSONObject("biblio").getJSONObject(language);
                            number = application.getString("number");
                            name = patent.getJSONObject("snippet").getString("title"); // Название
                            id = patent.getString("id");
                            while (name.charAt(0) == ' ' || name.charAt(0) == '\n' || name.charAt(0) == '\t') // Убираем пробелы в начале
                                name = name.substring(1);
                            name = name.toLowerCase(); // Название состоит из букв нижнего
                            name = name.substring(0, 1).toUpperCase() + name.substring(1, name.length()); // Делаем первую букву заглавной
                            name = "<b>"+name+"</b"; // Название жирное
                            publication_date = common.getString("publication_date");
                            filing_date = application.getString("filing_date");
                            priority="";
                            try {
                                JSONArray prior = common.getJSONArray("priority");
                                for (int j=0;j<prior.length();j++){
                                    try {
                                        priority+= prior.getJSONObject(0).getString("number") + ' ';
                                    }
                                    catch (JSONException e){
                                        priority+= "undefined ";
                                    }
                                    try{
                                        priority+= prior.getJSONObject(0).getString("filing_date") + ' ';
                                    }
                                    catch (JSONException e){
                                        priority+= "undefined ";
                                    }
                                    try{
                                        priority+= prior.getJSONObject(0).getString("publishing_office") + '\n';
                                    }
                                    catch (JSONException e){
                                        priority+= "undefined ";
                                    }
                                }
                            } catch (JSONException e) {
                                priority = "Не указаны";
                            }
                            //
                            try {
                                JSONArray inv = lang.getJSONArray("inventor"); // Массив авторов json
                                for (int j = 0; j < inv.length(); j++) {
                                    inventor += inv.getJSONObject(j).getString("name") + ", "; // Авторы в строке через запятую
                                }
                                inventor = reloadAuthor(inventor); // Авторы с заглавной буквы
                                inventorCard = inv.getJSONObject(0).getString("name") + ", ";
                                inventorCard = reloadAuthor(inventorCard); // Делаем имя автора как: Фамилия И. О.
                                String[] words = inventorCard.split(" ");
                                boolean surname = false;
                                for (String word : words) {
                                    if (surname){
                                        inventorCard+= word.charAt(0) + ". ";
                                    }
                                    else{
                                        inventorCard = word + " ";
                                        surname = true;
                                    }
                                }
                                // удаляем тег страны
                                for (int j=0;j<inventorCard.length();j++){
                                    if (inventorCard.charAt(j)=='('){
                                        while (j < inventorCard.length() && inventorCard.charAt(j)!= ')'){
                                            inventorCard = inventorCard.substring(0, j);
                                            if (j<inventor.length()) inventorCard+=inventorCard.substring(j,inventorCard.length());
                                        }
                                        if (j < inventorCard.length() && inventorCard.charAt(j) == ')') inventorCard = inventorCard.substring(0, j);
                                        if (j<inventor.length()) inventorCard+=inventorCard.substring(j,inventorCard.length());
                                    }
                                }
                                inventorCard = inventorCard.replaceAll(",", "");
                                if (inv.length() > 1)
                                    inventorCard += " + " + Integer.toString(inv.length() - 1); // Если автор не один, в карточке будет указан первый автор + кол-во авторов без него
                            } catch (JSONException e) {
                                inventor = patent.getJSONObject("snippet").getString("inventor") + ", "; // ищем авторов в другом месте
                                inventor = reloadAuthor(inventor);
                                inventorCard = inventor;
                                inventorCard = reloadAuthor(inventorCard);
                            }
                            try {
                                JSONArray patentee = patent.getJSONObject("biblio").getJSONObject(language).getJSONArray("patentee");
                                for (int j = 0; j < patentee.length(); j++) {
                                    patente += patentee.getJSONObject(j).getString("name") + ", "; // Строка с владельцами патента через запятую
                                }
                                patente = reloadAuthor(patente); // Владельцы с заглавной строки
                            } catch (JSONException e) {
                                try {
                                    patente = patent.getJSONObject("snippet").getString("patentee"); // ищем владельцев в другом месте
                                } catch (JSONException ex) {
                                    patente = "Не указаны"; // не удалось найти
                                }
                            }
                            ipcCard = patent.getJSONObject("snippet").getJSONObject("classification").getString("ipc");
                            ipc = ipcCard;
                            description = patent.getJSONObject("snippet").getString("description");
                            country_s = common.getString("publishing_office");

                        } catch (JSONException e) {
                            flag_ru = false; // Если не удалось получить необходимые данные
                            e.printStackTrace();
                        }

                        if (flag_ru) { // есть необходимые данные
                            //новый объект класса патент
                            Patent pat = new Patent(id, inventorCard, number, name, publication_date, filing_date, ipc, inventor, patente, description, priority);
                            count++; // +1 карточка
                            patentsList.add(pat);
                        }
                    }
                    setPatentRecycler(patentsList); // запускаем адаптер
                    if (patentsList.size() == 0) { // Если ничего не удалось найти
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                findViewById(R.id.patentItems).setVisibility(View.GONE); // скрываем карточки патентов
                                TextView text = findViewById(R.id.error);
                                text.setText("По Вашему запросу ничего не найдено");
                                text.setVisibility(View.VISIBLE);
                            }
                        });
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }

            }
            else { // нет инета, сеттим увед
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        findViewById(R.id.patentItems).setVisibility(View.GONE); // скрываем карточки патентов
                        TextView text = findViewById(R.id.error);
                        text.setText("Произошла ошибка!");
                        text.setVisibility(View.VISIBLE);
                        findViewById(R.id.textPohozh).setVisibility(View.VISIBLE);
                    }
                });
            }
        }
    }


    private void setPatentRecycler(List<Patent> PatentsList) {
        // передаем лист с патентами в адаптер и создаем карточки
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(PatentActivity.this, RecyclerView.VERTICAL, false);
        patentRecycler = findViewById(R.id.patentItems);
        patentRecycler.setLayoutManager(layoutManager);
        patentAdapter = new PatentAdapter(PatentActivity.this, PatentsList);
        patentRecycler.setAdapter(patentAdapter);
        patentRecycler.setVisibility(View.VISIBLE);
        findViewById(R.id.textPohozh).setVisibility(View.GONE);
        findViewById(R.id.error).setVisibility(View.GONE);
    }
    public String reloadAuthor(String author){ // Метод для исправления строки
        author = author.toLowerCase(Locale.ROOT); // символы в нижний регистр
        author = author.substring(0, 1).toUpperCase() + author.substring(1); // первый символ заглавный
        for (int i=1;i<author.length();i++){ // ищем пробел, после него делаем букву заглваной
            if (author.charAt(i-1)==' ') author = author.substring(0,i) + author.substring(i, i+1).toUpperCase() + author.substring(i+1,author.length());
        }
        author = author.substring(0,author.length()-2);// удаляем последнюю запятую
        return author;
    }
    public void openMoreInfo(View view){ // раскрыть/свернуть больше информации
        LinearLayout str = findViewById(R.id.str);
        LinearLayout value = findViewById(R.id.value);
        TextView textView = findViewById(R.id.viewMore);
        TextView similar = findViewById(R.id.similarText);
        TextView textload = findViewById(R.id.textPohozh);
        TextView texterror = findViewById(R.id.error);

        if (flagClose){ // если информация развернута
            flagClose = false;
            str.setVisibility(View.VISIBLE);
            value.setVisibility(View.VISIBLE);
            textload.setVisibility(View.VISIBLE);
            texterror.setVisibility(View.GONE);
            patentRecycler.setVisibility(View.GONE);
            similar.setVisibility(View.VISIBLE);
            textView.setText("Скрыть");
        }
        else{ // если информация скрыта
            str.setVisibility(View.GONE);
            value.setVisibility(View.GONE);
            textload.setVisibility(View.GONE);
            texterror.setVisibility(View.GONE);
            patentRecycler.setVisibility(View.GONE);
            similar.setVisibility(View.GONE);
            flagClose = true;
            textView.setText("Показать больше");
        }
    }
    public void loadSimilar(View view){
        new SetPatentList().execute();
    }
    public void goBack(View view) {
        onBackPressed();
    } // для кнопки назад
}