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

    String display = "Stats here";
    String dateformat = "yyyy.MM.dd.HH.mm.ss";
    List<String> timeline = new LinkedList();   //График времени - список отметок во времени, когда был нажат переключатель

    public boolean saveArray()                  //Сохраняет график в памяти
    {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor mEdit1 = sp.edit();
        mEdit1.putInt("Status_size", timeline.size());

        for(int i=0;i<timeline.size();i++)      //Первый элемент списка сохраняется как Status_0 и т.д.
        {
            mEdit1.remove("Status_" + i);
            mEdit1.putString("Status_" + i, timeline.get(i));
        }

        return mEdit1.commit();
    }

    public void loadArray()                     //Загружает график из памяти
    {
        SharedPreferences mSharedPreference1 = PreferenceManager.getDefaultSharedPreferences(this);
        timeline.clear();
        int size = mSharedPreference1.getInt("Status_size", 0);

        for(int i=0;i<size;i++)
        {
            timeline.add(mSharedPreference1.getString("Status_" + i, null));
        }

    }

    public void trimArray()                     //Убирает устаревшие отметки (сделанные более 24 часов назад)
    {
        boolean done = false;
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1);
        Date yesterday = cal.getTime();         //Временная точка "ровно 24 часа назад" - предел

        while(!done){
            Date push = new Date();
            Date pull = new Date();

            if (timeline.size()>1&&timeline.get(1)!=null) try {
                push = new SimpleDateFormat(dateformat).parse(timeline.get(0));
                pull = new SimpleDateFormat(dateformat).parse(timeline.get(1));
            } catch (ParseException e) {
                e.printStackTrace();            //Делит точки графика на нажатие и отпуск переключателя. Сон есть отрезки между ними
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

        public int sumTimeline(){               //Считает сумму длин отрезков между "нажатие" и "отпуск"
        int sum = 0;
        if(timeline.size()>1&&timeline.get(1)!=null){
            if (timeline.size()%2!=0)           //Если переключатель не отпущен, используем текущее время в качестве времени отпуска
                timeline.add(new SimpleDateFormat(dateformat).format(new Date()));
            for (int i=0;i<timeline.size();i+=2) try {  //Выделяем на графике отрезки, обозначенные парами точек во времени
                Date push = new SimpleDateFormat(dateformat).parse(timeline.get(i));
                Date pull = new SimpleDateFormat(dateformat).parse(timeline.get(i+1));
                sum+=(pull.getTime()-push.getTime());
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        return sum;
    }

    void updateDisplay(){
        TextView tv = findViewById(R.id.textView);
        display = "Slept\n"+sumTimeline()/(1000*60*60)+" hours\n"+(sumTimeline()/(1000*60))%60+" minutes\n"+(sumTimeline()/1000)%60+" seconds\n"+"\n(past 24 hours)";
        tv.setText(display);
    }

    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadArray();
        updateDisplay();

        ToggleButton button = findViewById(R.id.button);    //Переключатель. Добавляет отметку на график и сохраняет его; обновляет статистику.
        button.setChecked(loadToggle());
        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                saveToggle(isChecked);
                String timeStamp = new SimpleDateFormat(dateformat).format(new Date());
                loadArray();
                timeline.add(timeStamp);
                trimArray();
                saveArray();
                updateDisplay();
            }
        });

        Button clear = findViewById(R.id.buttonclear);      //Кнопка сброса графика с диалогом подтверждения
        clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
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
                                loadArray();
                                timeline.clear();
                                saveArray();
                                updateDisplay();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

    }
}
//TODO: bug: togglebutton can be inverted. Save button state?

//TODO: fix thread

//TODO: visualise graph
