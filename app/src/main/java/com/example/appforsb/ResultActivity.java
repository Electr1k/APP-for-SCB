package com.example.appforsb;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.appforsb.adapter.PatentAdapter;
import com.example.appforsb.model.Patent;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.utils.ColorTemplate;

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
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ResultActivity extends AppCompatActivity {
    String searchText; // Текст для поиска
    EditText editText; // Поле ввода текста
    int filter = 0; // Номер фильтра: 0-релевантнсть, 1-по дате вниз. 2-по дате вверх, 3-по дате подачи вниз, 4-по дате подачи вверх
    int offset = 0; // Количество патентов для запроса
    int offsetLoad = 0; // Количество реально загруженных карточек патентов (Т.к. некторые карточки могут быть не полные, исключаем их)
    RecyclerView patentRecycler; // View, в которой находятся карточки
    List<Patent> patentsList = new ArrayList<>(); // Лист патентов (добавляется из ответов сервера)
    PatentAdapter patentAdapter; // адаптер
    HashMap<String, Integer> countryCount = new HashMap<>(); // Мэп для диаграммы стран (Название, кол-во патентов)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result); // Грузим разметку
        init(); // Инициализируем страницу (ставим кликлистнеры и настраиваем поле ввода)
        new SetPatentList().execute(searchText); // Устанавливаем карточки, передавая текст поиска
    }

    // Вложенный класс для POST запроса к серверу и генирирую карточки патентов
    public class SetPatentList extends AsyncTask<String, String, String> {
        JSONObject obj = null; // json ответ сервера
        String pre_tag = "<font style=\\\"background:#00FF00\\\" color=\\\"#014DAA\\\"><b>"; // Теги для выделения текста

        protected void onPriExecute() {
            super.onPreExecute();
        }

        @Override
        protected String doInBackground(String... strings) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    viewLoadVisable(); // Устанавливаем текст "Загрузка"
                    TextView loadBtn = findViewById(R.id.loadMore); // Ещем кнопку "загрузить еще"
                    loadBtn.setOnClickListener(null); // убираем слушатель событий
                    loadBtn.setText("Загрузка..."); // изменяем текст
                    findViewById(R.id.patentItems).setVisibility(View.VISIBLE); // Показываем уже загруженные карточки
                    findViewById(R.id.error).setVisibility(View.GONE); // скрываем уведомление об ошибке
                }
            });
            String url_s = "https://searchplatform.rospatent.gov.ru/patsearch/v0.2/search/";
            String key = "26a213594e7f4f6e8cd89064d885ea93"; // токен
            String sort = "";
            switch (filter) { // выбираем сортировку (Можно реализовать через enum или массив)
                case 0:
                    sort = "relevance";
                    break;
                case 1:
                    sort = "publication_date:asc";
                    break;
                case 2:
                    sort = "publication_date:desc";
                    break;
                case 3:
                    sort = "filing_date:asc";
                    break;
                case 4:
                    sort = "filing_date:desc";
                    break;
            }
            URL url;
            HttpURLConnection connection = null;
            BufferedReader reader = null;
            String query = strings[0]; // Текст поиска
            String offset_s = Integer.toString(offset); // оффсет в строку
            try {
                url = new URL(url_s);
                // тело запроса
                String post_tag = "</font></b>";
                String jsonInputString = "{\"q\": \"" + query + "\", \"sort\": \"" + sort + "\", \"limit\": 100,\"pre_tag\": \""+pre_tag+"\", \"post_tag\": \""+post_tag+"\", \"offset\": " + offset_s + "}";
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
                        TextView text = findViewById(R.id.error);
                        text.setText("Упс, что-то пошло не так\nПроверьте интернет-соединение");
                        text.setVisibility(View.VISIBLE); // Устанавливаем уведомление об ошибке
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
                    if (offset == 0) { // если загружаем первую порцию карточек
                        patentsList.clear(); // удаляем информацию о старых патентах
                        countryCount.clear(); // отчищаем мэп стран
                        offsetLoad = 0; // обнуляаем кол-во загруженных карточек
                    }
                    JSONArray patents = null; // json массив патентов
                    try {
                        patents = obj.getJSONArray("hits");
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
                            name = name.toLowerCase(); // Название состоит из букв нижнего регистра
                            int k = 0; // Сколько симвлов пропустить перед символом, который нужно сделать заглавным
                            if (name.charAt(0) == '<') k = pre_tag.length()-4; // Если строка начинается со слова, которое ищет пользователь
                            name = name.substring(0, k) + name.substring(k, k + 1).toUpperCase() + name.substring(k + 1, name.length()); // Делаем первую букву заглавной
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
                                    if (surname && word.length()>0){
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
                            if (countryCount.containsKey(country_s)) { // если страна есть уже есть в мэпе, то +1, иначе добавляем ее
                                countryCount.put(country_s, countryCount.get(country_s) + 1);
                            } else countryCount.put(country_s, 1);
                            patentsList.add(pat);
                        }
                    }
                    setPatentRecycler(patentsList); // запускаем адаптер
                    if (patentsList.size() == 0) { // Если ничего не удалось найти
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
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
                        TextView text = findViewById(R.id.error);
                        text.setText("Упс, что-то пошло не так\nПроверьте интернет-соединение");
                        text.setVisibility(View.VISIBLE);
                    }
                });
            }
            runOnUiThread(new Runnable() { // убираем вью загрузки и делаем кнопку загрузки еще активной
                @Override
                public void run() {
                    TextView loadBtn = findViewById(R.id.loadMore);
                    loadBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            LoadMore(view);
                        }
                    });
                    loadBtn.setText("Загрузить еще");
                    viewLoadInvisable();
                }
            });

        }
    }

    //Метод для инициализации сцены
    private void init(){
        editText = findViewById(R.id.inputText); // Находим поле ввода текста поиска
        searchText = getIntent().getStringExtra("text"); // Получаем текст для поиска с главной сцены
        editText.setText(searchText); // Устанавливаем его в поле ввода
        editText.setOnKeyListener(new View.OnKeyListener(){ // При нажатии на поле
            public boolean onKey(View v, int keyCode, KeyEvent event){
                if(event.getAction() == KeyEvent.ACTION_DOWN && (keyCode == KeyEvent.KEYCODE_ENTER)){ // Если был введен символ '\n'
                    searchText = editText.getText().toString(); // Получаем содержимое поля ввода
                    setRelevanceTrue(findViewById(R.id.byRelevance)); // Устанавливаем сортировку по релевантности
                    offset = offsetLoad = 0; // Отсчет загрузки с нуля
                    countryCount.clear(); // Очищаем мэп стран
                    new SetPatentList().execute(searchText); // Устанавливаем карточки патентов
                    return true;
                }
                else return false;
            }
        });
        findViewById(R.id.sortedLayout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //При нажатии на иконку сортировки отображаем слой
                ConstraintLayout co =  findViewById(R.id.sortedLayout);
                co.setVisibility(View.INVISIBLE);
            }
        });

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
    public void LoadMore(View view){ // Метод для загрузки следеющей порции карточек
        offset += 100;
        offsetLoad = patentsList.size();
        new SetPatentList().execute(searchText);
    }

    // Запуск адаптера
    private void setPatentRecycler(List<Patent> PatentsList) {
        // передаем лист с патентами в адаптер и создаем карточки
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(ResultActivity.this, RecyclerView.VERTICAL, false);
        patentRecycler = findViewById(R.id.patentItems);
        patentRecycler.setLayoutManager(layoutManager);
        patentAdapter = new PatentAdapter(ResultActivity.this, PatentsList);
        patentRecycler.setAdapter(patentAdapter);
        // После создания скроллим в начало последней порции
        patentRecycler.scrollToPosition(offsetLoad-4);
        PieChart pieChart = findViewById(R.id.PieCharst); // Обнавляем диаграмму
        Set<String> keys = countryCount.keySet();
        ArrayList<PieEntry> list = new ArrayList<>();
        for (String i : keys){
            int val = countryCount.get(i);
            list.add(new PieEntry(val, i));
        }
        PieDataSet pieDataSet = new PieDataSet(list,"Страны");
        pieDataSet.setColors(ColorTemplate.COLORFUL_COLORS);
        pieDataSet.setValueTextColor(Color.BLACK);
        pieDataSet.setValueTextSize(16);
        PieData pieData = new PieData(pieDataSet);
        pieChart.setData(pieData);
        pieChart.getDescription().setEnabled(false);
        pieChart.setCenterText("Страны");
        pieChart.animate();
    }
    public void openAnalitic(View view){ // открыть слой с анализом
        findViewById(R.id.analitics).setVisibility(View.VISIBLE);
    }
    public void closeAnalitic(View view){ // скрыть слой с анализом
        findViewById(R.id.analitics).setVisibility(View.INVISIBLE);
    }
    public void viewLoadInvisable(){
        findViewById(R.id.LoadText).setVisibility(View.GONE);
    } // надпись загрузки убирается
    public void viewLoadVisable(){
        findViewById(R.id.LoadText).setVisibility(View.VISIBLE);
    }// надпись загрузки открывается
    public void deleteString(View view) { // удаление поля поисковой строки
        searchText = "";
        editText.setText(searchText);
    }
    public void SetSorted(View view){ findViewById(R.id.sortedLayout).setVisibility(View.VISIBLE);  } // открыть слой сортировки
    public void setRelevanceTrue(View view) { // сортировка по релевантности
        if (filter!=0){
            switch (filter){ // Переставляем галочку
                case 1:
                    findViewById(R.id.dateDownImg).setVisibility(View.INVISIBLE);
                    break;
                case 2:
                    findViewById(R.id.dateUpImg).setVisibility(View.INVISIBLE);
                    break;
                case 3:
                    findViewById(R.id.dateDownPodImg).setVisibility(View.INVISIBLE);
                    break;
                case 4:
                    findViewById(R.id.dateUpPodImg).setVisibility(View.INVISIBLE);
                    break;
            }
            findViewById(R.id.byRelevance).setVisibility(View.VISIBLE);
            offset=offsetLoad=0; // поиск с нелевого патента
            filter = 0; // фильтр релевантности
            findViewById(R.id.sortedLayout).setVisibility(View.INVISIBLE);
            new SetPatentList().execute(searchText);
        }
    }
    public void setDateDownTrue(View view) { // сортировка по дате вниз
        if (filter!=1){
            switch (filter){
                case 0:
                    findViewById(R.id.relevanceImg).setVisibility(View.INVISIBLE);
                    break;
                case 2:
                    findViewById(R.id.dateUpImg).setVisibility(View.INVISIBLE);
                    break;
                case 3:
                    findViewById(R.id.dateDownPodImg).setVisibility(View.INVISIBLE);
                    break;
                case 4:
                    findViewById(R.id.dateUpPodImg).setVisibility(View.INVISIBLE);
                    break;
            }
            findViewById(R.id.dateDownImg).setVisibility(View.VISIBLE);
            filter = 1;
            findViewById(R.id.sortedLayout).setVisibility(View.INVISIBLE);
            offset=offsetLoad=0;
            new SetPatentList().execute(searchText);
        }
    }
    public void setDateUpTrue(View view) { // сортировка по дате вверх
        if (filter!=2){
            switch (filter){
                case 0:
                    findViewById(R.id.relevanceImg).setVisibility(View.INVISIBLE);
                    break;
                case 1:
                    findViewById(R.id.dateDownImg).setVisibility(View.INVISIBLE);
                    break;
                case 3:
                    findViewById(R.id.dateDownPodImg).setVisibility(View.INVISIBLE);
                    break;
                case 4:
                    findViewById(R.id.dateUpPodImg).setVisibility(View.INVISIBLE);
                    break;
            }
            findViewById(R.id.dateUpImg).setVisibility(View.VISIBLE);
            filter = 2;
            offset=offsetLoad=0;
            findViewById(R.id.sortedLayout).setVisibility(View.INVISIBLE);
            new SetPatentList().execute(searchText);
        }
    }
    public void setDatePodDownTrue(View view) { // сортировка по дате подачи вниз
        if (filter!=3){
            switch (filter){
                case 0:
                    findViewById(R.id.relevanceImg).setVisibility(View.INVISIBLE);
                    break;
                case 1:
                    findViewById(R.id.dateDownImg).setVisibility(View.INVISIBLE);
                    break;
                case 2:
                    findViewById(R.id.dateUpImg).setVisibility(View.INVISIBLE);
                    break;
                case 4:
                    findViewById(R.id.dateUpPodImg).setVisibility(View.INVISIBLE);
                    break;
            }
            findViewById(R.id.dateDownPodImg).setVisibility(View.VISIBLE);
            filter = 3;
            offset=offsetLoad=0;
            findViewById(R.id.sortedLayout).setVisibility(View.INVISIBLE);
            new SetPatentList().execute(searchText);
        }
    }
    public void setDatePodUpTrue(View view) { //сортировка опдачи вниз
        if (filter!=4){
            switch (filter){
                case 0:
                    findViewById(R.id.relevanceImg).setVisibility(View.INVISIBLE);
                    break;
                case 1:
                    findViewById(R.id.dateDownImg).setVisibility(View.INVISIBLE);
                    break;
                case 2:
                    findViewById(R.id.dateUpImg).setVisibility(View.INVISIBLE);
                    break;
                case 3:
                    findViewById(R.id.dateDownPodImg).setVisibility(View.INVISIBLE);
                    break;
            }
            findViewById(R.id.dateUpPodImg).setVisibility(View.VISIBLE);
            filter = 4;
            offset=offsetLoad=0;
            findViewById(R.id.sortedLayout).setVisibility(View.INVISIBLE);
            new SetPatentList().execute(searchText);
        }
    }
    public void goBack(View view) {
        onBackPressed();
    } // для кнопки назад
}