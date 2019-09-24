package com.example.calendar;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Bundle;
import android.widget.*;
import android.view.View;
import android.app.AlertDialog;
import android.content.DialogInterface;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    String display = "Stats here";              //Значение текстового поля textview
    String dateformat = "yyyy.MM.dd.HH.mm.ss";  //Унифицированный для приложения формат даты
    List<String> timeline = new LinkedList();   //График времени - список отметок во времени, когда был нажат переключатель
    String undo1,undo2;                         //Временное хранилище для удалённых отметок

    private boolean saveArray()                  //Сохраняет график в памяти
    {
        SharedPreferences sp = this.getSharedPreferences("preferences", this.MODE_PRIVATE);
        SharedPreferences.Editor mEdit1 = sp.edit();
        mEdit1.putInt("Status_size", timeline.size());

        for(int i=0;i<timeline.size();i++)      //Первый элемент списка сохраняется как Status_0 и т.д.
        {
            mEdit1.remove("Status_" + i);
            mEdit1.putString("Status_" + i, timeline.get(i));
        }

        return mEdit1.commit();
    }

    private void clearArray(){                  //Удаляет все данные приложения
        SharedPreferences sp = this.getSharedPreferences("preferences", this.MODE_PRIVATE);
        SharedPreferences.Editor mEdit1 = sp.edit();
        mEdit1.clear();
        mEdit1.commit();
        timeline.clear();
    }

    private void loadArray()                     //Загружает график из памяти
    {
        SharedPreferences mSharedPreference1 = this.getSharedPreferences("preferences", this.MODE_PRIVATE);
        timeline.clear();
        int size = mSharedPreference1.getInt("Status_size", 0);

        for(int i=0;i<size;i++)
        {
            timeline.add(mSharedPreference1.getString("Status_" + i, null));
        }

    }

    private void trimArray()                    //Убирает устаревшие отметки (сделанные более 24 часов назад)
    {
        boolean done = false;
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1);
        Date yesterday = cal.getTime();         //Временная точка "ровно 24 часа назад" - предел
        Date push = new Date();                 //Делит точки графика на нажатие и отпуск переключателя. Сон есть отрезки между парами точек
        Date pull = new Date();

        while(!done){
            if (timeline.size()>1&&timeline.get(1)!=null) try {
                push = new SimpleDateFormat(dateformat).parse(timeline.get(0));
                pull = new SimpleDateFormat(dateformat).parse(timeline.get(1));
            } catch (ParseException e) {
                e.printStackTrace();
            }

            if (pull.getTime()<yesterday.getTime()){
                timeline.remove(0);
                timeline.remove(0);      //Кому вообще нужны циклы
            } else if (push.getTime()<yesterday.getTime()){
                timeline.set(0,new SimpleDateFormat(dateformat).format(yesterday));
                done = true;                    //Если устарел конец отрезка, убираем весь отрезок;
            } else done = true;                 //Если устарело начало, двигаем его до предела и прерываем цикл, т.к. предел постоянно меняется
        }
    }

    private void saveToggle(boolean isToggled) {//Сохраняет состояние переключателя в памяти, т.к. это элемент интерфейса
        SharedPreferences sharedPreferences = this.getSharedPreferences("preferences", this.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean("toggle_value", isToggled).apply();
    }

    private Boolean loadToggle(){               //Загружает состояние переключателя из памяти
        SharedPreferences sharedPreferences = this.getSharedPreferences("preferences", this.MODE_PRIVATE);
        return sharedPreferences.getBoolean("toggle_value", true);
    }

    private void updateDisplay(){               //Считает сумму отрезков и выводит её в текстовое поле на экране
        loadArray();                            //Не понял, почему без этой строки не работает
        trimArray();
        int sum = 0;
            if (timeline.size()%2!=0)           //Добавляем недостающий конец отрезка - например, в случае, когда переключатель нажат
                timeline.add(new SimpleDateFormat(dateformat).format(new Date()));
            for (int i=0;i<timeline.size();i+=2) try {
                Date push = new SimpleDateFormat(dateformat).parse(timeline.get(i));
                Date pull = new SimpleDateFormat(dateformat).parse(timeline.get(i+1));
                sum+=(pull.getTime()-push.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        TextView tv = findViewById(R.id.textView);
        display = "Slept\n"+sum/(1000*60*60)+" hours\n"+(sum/(1000*60))%60+" minutes\n"+(sum/1000)%60+" seconds\n"+"\n(past 24 hours)";
        tv.setText(display);
    }

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadArray();
        updateDisplay();

        Thread clock = new Thread(){
            public void run() {
                while (true) {
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                updateDisplay();
                            }
                        });
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    }
                }
            }
        };
        clock.start();

        ToggleButton undo = findViewById(R.id.buttonundo);  //Кнопка отмены. Убирает или возвращает последние две точки на графике
        final CompoundButton.OnCheckedChangeListener undolistener;
        undo.setOnCheckedChangeListener(undolistener = new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                loadArray();
                    if (isChecked&&timeline.size()>1) {
                        undo1 = timeline.get(timeline.size() - 1);
                        undo2 = timeline.get(timeline.size() - 2);
                        timeline.remove(timeline.size() - 1);
                        timeline.remove(timeline.size() - 1);
                    } else {
                        timeline.add(undo2);
                        timeline.add(undo1);
                    }
                    saveArray();
                    updateDisplay();
                }
        });

        ToggleButton button = findViewById(R.id.button);    //Переключатель. Добавляет отметку с временем взаимодействия на график и сохраняет его; обновляет статистику.
        button.setChecked(loadToggle());
        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                saveToggle(isChecked);
                loadArray();
                timeline.add(new SimpleDateFormat(dateformat).format(new Date()));
                saveArray();
                updateDisplay();
                ToggleButton undo = findViewById(R.id.buttonundo);
                undo.setClickable(!isChecked);               //Блокирует кнопку отмены во время сна
                undo.setOnCheckedChangeListener (null);     //Изменение графика сбрасывает состояние кнопки отмены. Чтобы она при этом не срабатывала,
                undo.setChecked(false);                     //временно убираем детектор срабатывания с кнопки. Для этого undolistener объявлен отдельно.
                undo.setOnCheckedChangeListener (undolistener);
            }
        });

        Button clear = findViewById(R.id.buttonclear);      //Кнопка сброса графика с диалогом подтверждения
        clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.MyDialogTheme);
                builder.setMessage("Reset stats?")
                        .setCancelable(true)
                        .setNeutralButton(android.R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                clearArray();
                                updateDisplay();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });
    }
}

//TODO: make thread only run in foreground
//TODO: bug: timer doesnt update if timeline is empty
//TODO: visualise graph
