package com.example.calendar;
import android.content.SharedPreferences;
import android.graphics.*;
import android.util.DisplayMetrics;
import android.os.Bundle;
import android.widget.*;
import android.view.View;
import android.app.AlertDialog;
import android.content.DialogInterface;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import android.text.InputType;
import java.util.*;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity {

    String display = "Stats here";              //Значение текстового поля textview
    String dateformat = "yyyy.MM.dd.HH.mm.ss";  //Унифицированный для приложения формат даты
    List<String> timeline = new LinkedList();   //График времени - список отметок во времени, когда был нажат переключатель
    String undo1,undo2;                         //Временное (:^)) хранилище для удалённых отметок
    ImageView imageView;
    DisplayMetrics displayMetrics;
    int height = 6;
    int width;

    private boolean saveArray(){                 //Сохраняет график в памяти
        SharedPreferences sp = this.getSharedPreferences("preferences", this.MODE_PRIVATE);
        SharedPreferences.Editor mEdit1 = sp.edit();
        mEdit1.putInt("Status_size", timeline.size());
        for(int i=0;i<timeline.size();i++){      //Первый элемент списка сохраняется как Status_0 и т.д.
            mEdit1.remove("Status_" + i);
            mEdit1.putString("Status_" + i, timeline.get(i));
        }
        return mEdit1.commit();
    }

    private void loadArray(){                    //Загружает график из памяти
        SharedPreferences mSharedPreference1 = this.getSharedPreferences("preferences", this.MODE_PRIVATE);
        timeline.clear();
        int size = mSharedPreference1.getInt("Status_size", 0);
        for(int i=0;i<size;i++) {
            timeline.add(mSharedPreference1.getString("Status_" + i, null));
        }
    }

    private void updateDisplay(){               //Обновляет UI: график (значения, внешний вид) и текстовое поле.
        int sum = 0;
        float w = width;
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        cal.add(Calendar.DATE, -1);
        Date yesterday = cal.getTime();         //Временная точка "ровно 24 часа назад" - предел
        Date push = new Date();                 //Делит точки графика на нажатие и отпуск переключателя. Сон есть отрезки между парами точек
        Date pull = new Date();
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.parseColor("#FAE7C0"));
        Paint paint = new Paint();
        paint.setColor(Color.parseColor("#2d3647"));
        loadArray();

        if (timeline.size()%2!=0)                       //Добавляем недостающий конец отрезка - например, в случае, когда переключатель нажат
            timeline.add(new SimpleDateFormat(dateformat).format(new Date()));
        for (int i=0;i<timeline.size();i+=2) try {
            push = new SimpleDateFormat(dateformat).parse(timeline.get(i));
            pull = new SimpleDateFormat(dateformat).parse(timeline.get(i+1));
            if (pull.getTime()<=yesterday.getTime()){   //Если конец отрезка более чем 24 часа назад, удаляем отрезок и начинаем заново
                timeline.remove(0);
                timeline.remove(0);
                i-=2;
                continue;
            }else if (push.getTime()<yesterday.getTime()) {  //Если начало отрезка более чем 24 часа назад, но конец актуальный, сдвигаем начало до предела
                timeline.set(0, new SimpleDateFormat(dateformat).format(yesterday));
                push = new SimpleDateFormat(dateformat).parse(timeline.get(0));
            }
            sum+=(pull.getTime()-push.getTime());            //Считаем общее время
            int start = (int) ((push.getTime()-yesterday.getTime())*w/(60*1000*1440f));
            int end = (int) ((pull.getTime()-yesterday.getTime())*w/(60*1000*1440f));
            canvas.drawRect(start, 0, end+5, height, paint); //Изображаем отрезки на графике. Время переводим в пиксели по формуле:
        } catch (ParseException e) {                                    //минуты от начала графика умножить на (длину графика/минут в сутках)
            e.printStackTrace();
        }
        imageView.setImageBitmap(bitmap);
        TextView tv = findViewById(R.id.textView);
        display = "Slept\n"+sum/(1000*60*60)+" hours\n"+(sum/(1000*60))%60+" minutes\n"+(sum/1000)%60+" seconds\n";
        tv.setText(display);
    }

    ArrayList<View> allButtons;

    private void blockButtons(ToggleButton button){     //Блокирует кнопки управления во время сна
        if (allButtons == null)
            allButtons = (findViewById(R.id.coordinatorLayout)).getTouchables();
        allButtons.remove(button);
        for(View view:allButtons){
            Button control = (Button) view;
            control.setClickable(!button.isChecked());
            if (button.isChecked()) {
                control.setTextColor(Color.parseColor("#191e28"));
            }
            else {
                control.setTextColor(Color.parseColor("#FAE7C0"));
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences sharedPreferences = this.getSharedPreferences("preferences", this.MODE_PRIVATE);
        final SharedPreferences.Editor editor = sharedPreferences.edit();

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        loadArray();

        imageView = findViewById(R.id.graph);           //Инициализация графика на экране
        displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        height = 6;
        width = displayMetrics.widthPixels;
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.parseColor("#FAE7C0"));
        imageView.setImageBitmap(bitmap);

        updateDisplay();

        Thread clock = new Thread(){                    //Тред обновляет график и текст в реальном времени
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

        final Button add = findViewById(R.id.buttonadd);        //Кнопка ручного добавления времени
        add.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.MyDialogTheme);
                final EditText input = new EditText(MainActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                try {
                    input.setLayoutParams(lp);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                input.setTextColor(getResources().getColor(R.color.colorAccent));
                input.setInputType(InputType.TYPE_CLASS_NUMBER);
                builder.setView(input);
                builder.setMessage("Adjust time in minutes")
                        .setCancelable(true)
                        .setNeutralButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                })
                        .setPositiveButton("+", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                loadArray();
                                int adjustment = (Integer.parseInt(input.getText().toString()))*60*1000;
                                    if (timeline.size()<2 || timeline==null){
                                        timeline.add(new SimpleDateFormat(dateformat).format(new Date().getTime()-adjustment));
                                        timeline.add(new SimpleDateFormat(dateformat).format(new Date()));
                                    } else try {
                                        Date date1 = new SimpleDateFormat(dateformat).parse(timeline.get(timeline.size() - 1));
                                        Date date2 = new SimpleDateFormat(dateformat).parse(timeline.get(timeline.size() - 2));
                                        if (date1.getTime() + adjustment > new Date().getTime()) {
                                            adjustment -= (new Date().getTime() - date1.getTime());
                                            timeline.set(timeline.size() - 1, new SimpleDateFormat(dateformat).format(new Date()));
                                            timeline.set(timeline.size() - 2, new SimpleDateFormat(dateformat).format(date2.getTime() - adjustment));
                                        } else
                                            timeline.set(timeline.size() - 1, new SimpleDateFormat(dateformat).format(date1.getTime() + adjustment));
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                saveArray();
                                updateDisplay();
                            }
                        })
                        .setNegativeButton("-", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                loadArray();
                                int adjustment = (Integer.parseInt(input.getText().toString()))*60*1000;
                                while(adjustment>0 && timeline.size()>1) try {
                                    Date date1 = new SimpleDateFormat(dateformat).parse(timeline.get(timeline.size()-1));
                                    Date date2 = new SimpleDateFormat(dateformat).parse(timeline.get(timeline.size()-2));
                                    if (date1.getTime()-date2.getTime()<adjustment){               //При вычитании: если последний отрезок короче правки, удаляем целиком
                                        adjustment-=(date1.getTime()-date2.getTime());
                                        timeline.remove(timeline.size()-1);
                                        timeline.remove(timeline.size()-1);
                                    } else {
                                        date2 = new Date(date1.getTime()-adjustment);              //Иначе укорачиваем отрезок
                                        timeline.set(timeline.size()-1,new SimpleDateFormat(dateformat).format(date2));
                                        adjustment=0;
                                    }
                                } catch (ParseException e) {
                                    e.printStackTrace();
                                }
                                saveArray();
                                updateDisplay();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        final Button clear = findViewById(R.id.buttonclear);//Кнопка сброса графика с диалогом подтверждения
        clear.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this,R.style.MyDialogTheme);
                builder.setMessage("Reset stats?")
                        .setCancelable(true)
                        .setNeutralButton("No",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        dialog.cancel();
                                    }
                                })
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                editor.clear();
                                editor.commit();
                                timeline.clear();
                                undo1=null;
                                undo2=null;
                                updateDisplay();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        });

        final ToggleButton undo = findViewById(R.id.buttonundo);  //Кнопка отмены. Убирает или возвращает последние две точки на графике
        final CompoundButton.OnCheckedChangeListener undolistener;
        undo.setOnCheckedChangeListener(undolistener = new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                loadArray();
                    if (isChecked&&timeline.size()>1) {
                        undo1 = timeline.get(timeline.size() - 1);
                        undo2 = timeline.get(timeline.size() - 2);
                        timeline.remove(timeline.size() - 1);
                        timeline.remove(timeline.size() - 1);
                    } else if (undo1!=null&&undo2!=null){
                        timeline.add(undo2);
                        timeline.add(undo1);
                    }
                    saveArray();
                    updateDisplay();
                }
        });

        final ToggleButton button = findViewById(R.id.button);    //Переключатель сна. Добавляет отметку с временем взаимодействия на график и сохраняет его; обновляет статистику.
        button.setChecked(sharedPreferences.getBoolean("toggle_value", false));
        button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, final boolean isChecked) {
                editor.putBoolean("toggle_value", isChecked).apply();
                loadArray();
                timeline.add(new SimpleDateFormat(dateformat).format(new Date()));
                saveArray();
                updateDisplay();
                ToggleButton undo = findViewById(R.id.buttonundo);
                blockButtons(button);
                undo.setOnCheckedChangeListener (null); //Изменение графика сбрасывает состояние кнопки отмены. Чтобы она при этом не срабатывала,
                undo.setChecked(false);                 //временно убираем детектор срабатывания с кнопки. Для этого undolistener объявлен отдельно.
                undo.setOnCheckedChangeListener (undolistener);
            }
        });
        blockButtons(button);
    }
}

//TODO: make thread only run in foreground
