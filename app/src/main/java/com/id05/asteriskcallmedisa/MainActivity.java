package com.id05.asteriskcallmedisa;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.DragEvent;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import static java.lang.Thread.sleep;

public class MainActivity extends AppCompatActivity {

    EditText mySeachText;
    static TelnetTask telnetTask;
    public static int SERVERPORT;
    public static String SERVER_IP;
    public static String amiuser;
    public static String amisecret;
    public static String astercontext;
    public static String myphonenumber;
    private static MyTelnetClient mtc;
    public SharedPreferences sPref;
    private final int PERMISSIONS_REQUEST_READ_CONTACTS = 10;
    private static boolean READ_CONTACTS_GRANTED =false;
    public String TAG = "aster";
    public RecordAdapter adapter;
    public ArrayList<Contact> contacts = new ArrayList<>();
    public ArrayList<Contact> bufcontacts = new ArrayList<>();
    ImageView imgDialer;
    LinearLayout layoutDialer;
    RelativeLayout keypadlayout;
    RecyclerView recyclerView;
    EditText inputNumber;
    Button but0;
    Button but1;
    Button but2;
    Button but3;
    Button but4;
    Button but5;
    Button but6;
    Button but7;
    Button but8;
    Button but9;
    Button butDel;
    Button butCall;
    InputMethodManager imm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        recyclerView = findViewById(R.id.recyclerView);
        imgDialer = findViewById(R.id.imageDialer);
        imgDialer.setOnClickListener(dialerClick);
        layoutDialer = findViewById(R.id.layoutDialer);
        keypadlayout = findViewById(R.id.keypadLayout);
        inputNumber = findViewById(R.id.inputNumber);
        but0 = findViewById(R.id.but0);
        but0.setOnClickListener(digitClick);
        but1 = findViewById(R.id.but1);
        but1.setOnClickListener(digitClick);
        but2 = findViewById(R.id.but2);
        but2.setOnClickListener(digitClick);
        but3 = findViewById(R.id.but3);
        but3.setOnClickListener(digitClick);
        but4 = findViewById(R.id.but4);
        but4.setOnClickListener(digitClick);
        but5 = findViewById(R.id.but5);
        but5.setOnClickListener(digitClick);
        but6 = findViewById(R.id.but6);
        but6.setOnClickListener(digitClick);
        but7 = findViewById(R.id.but7);
        but7.setOnClickListener(digitClick);
        but8 = findViewById(R.id.but8);
        but8.setOnClickListener(digitClick);
        but9 = findViewById(R.id.but9);
        but9.setOnClickListener(digitClick);
        butDel = findViewById(R.id.butDel);
        butDel.setOnClickListener(digitClick);

        butCall = findViewById(R.id.butCall);
        butCall.setOnClickListener(digitClick);

        loadCONFIG();
        mySeachText = findViewById(R.id.editText);
        //recyclerView.setOnDragListener(dragContactlist);
        // Проверка разрешения
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED)
        {
            Log.d(TAG, "Permission is granted");
            readContacts(this);
        } else {
            Log.d(TAG, "Permission is not granted");
            Log.d(TAG, "Request permissions");
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission
                                    .READ_CONTACTS},
                    PERMISSIONS_REQUEST_READ_CONTACTS);
        }

        mySeachText.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                //
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // TODO Auto-generated method stub
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(mySeachText.getText().length()>=1) {
                    Log.d("aster",">0");
                    bufcontacts.clear();
                    for (Contact contact : contacts) {
                        //if(mySeachText.getText().length()<=contact.getName().length()) {
                        //    String buf1 = mySeachText.getText().toString();
                        //    if (mySeachText.getText().toString().equals(contact.getName().substring(0, mySeachText.getText().length()))) {
                        //        bufcontacts.add(contact);
                        //    }
                        //}
                        if(contact.getName().contains(mySeachText.getText())){
                            bufcontacts.add(contact);
                        }
                        onSetContacnts(bufcontacts);
                    }

                }else{
                    onSetContacnts(contacts);
                }
            }
        });

        mySeachText.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                layoutDialer.setVisibility(View.VISIBLE);
                keypadlayout.setVisibility(View.INVISIBLE);
            }
        });

        inputNumber.setKeyListener(null);
    }

    View.OnClickListener digitClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            String buf = inputNumber.getText().toString();
            switch(v.getId()) {

                case R.id.but0: inputNumber.setText(buf+"0");
                    break;
                case R.id.but1: inputNumber.setText(buf+"1");
                    break;
                case R.id.but2: inputNumber.setText(buf+"2");
                    break;
                case R.id.but3: inputNumber.setText(buf+"3");
                    break;
                case R.id.but4: inputNumber.setText(buf+"4");
                    break;
                case R.id.but5: inputNumber.setText(buf+"5");
                    break;
                case R.id.but6: inputNumber.setText(buf+"6");
                    break;
                case R.id.but7: inputNumber.setText(buf+"7");
                    break;
                case R.id.but8: inputNumber.setText(buf+"8");
                    break;
                case R.id.but9: inputNumber.setText(buf+"9");
                    break;
                case R.id.butDel:
                    if(buf.length()!=0){
                        inputNumber.setText(buf.substring(0,buf.length()-1));
                    }
                    break;


                case R.id.butCall:{
                    RecordAdapter.CallTask callTask = new RecordAdapter.CallTask();
                    callTask.execute(inputNumber.getText().toString());
                    layoutDialer.setVisibility(View.VISIBLE);
                    keypadlayout.setVisibility(View.INVISIBLE);
                    Toast toast = Toast.makeText(MainActivity.this,
                            "Wait Call", Toast.LENGTH_LONG);
                    toast.show();
                }
                    break;
            }
        }
    };


    View.OnClickListener dialerClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            layoutDialer.setVisibility(View.INVISIBLE);
            keypadlayout.setVisibility(View.VISIBLE);
            //inputNumber.setFocusable(true);
            inputNumber.requestFocus();

            //imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            //imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_IMPLICIT_ONLY);
        }
    };

    @Override
    public void onBackPressed() {
        if(keypadlayout.getVisibility()==View.VISIBLE){
            //imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            layoutDialer.setVisibility(View.VISIBLE);
            keypadlayout.setVisibility(View.INVISIBLE);
            //Log.d("aster","нажали назад");
            //imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.settings:
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onSetContacnts(ArrayList<Contact> contactslist){
        recyclerView.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new GridLayoutManager(this,1);
        recyclerView.setLayoutManager(linearLayoutManager);
        adapter = new RecordAdapter(contactslist,this);
        recyclerView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults)
    {
        switch (requestCode) {
            case PERMISSIONS_REQUEST_READ_CONTACTS : {
                // При отмене запроса массив результатов пустой
                if ((grantResults.length > 0) &&
                        (grantResults[0] ==
                                PackageManager.PERMISSION_GRANTED)) {
                    // Разрешения получены
                    Log.d(TAG, "Разрешения получены  onRequestPermissionsResult !");

                    // Чтение контактов11
                    readContacts(this);
                } else {
                    // Разрешения НЕ получены.
                    Log.d(TAG, "Permission denied!");
                }
                return;
            }
        }
    }

    public static void OpenTelnetClient(){
        telnetTask = new TelnetTask();
        telnetTask.execute("open");
    }

    public static void LoginTelnetClient(){
        telnetTask = new TelnetTask();
        String com1 = "Action: Login\n"+
                "Events: off\n"+
                "Username: "+amiuser+"\n"+
                "Secret: "+amisecret+"\n";
        telnetTask.execute("login",com1);
    }

    public static void SendCallMeFile(String mynumber, String externalnumber){
        telnetTask = new TelnetTask();
        String comenter = "Action: Originate\n" +
                "Channel: Local/"+mynumber+"@"+astercontext+"\n" +
                "Exten: "+externalnumber+"\n" +
                "Context: from-internal\n" +
                "Priority: 1\n" +
                "Async: true\n" +
                "CallerID: "+mynumber+"\n" +
                "ActionID: 123\n";
        telnetTask.execute("call",comenter);
    }

    public static void CloseTelnetClient(){
        telnetTask = new TelnetTask();
        String com1 = "Action: Logoff\n";
        telnetTask.execute("exit",com1);
    }

    public static void MainProd(String mynumber, String externalnumber) throws IOException {
        OpenTelnetClient();

        LoginTelnetClient();
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        SendCallMeFile(mynumber, externalnumber);
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        CloseTelnetClient();
        try {
            sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        //mtc.close();
    }

    static class TelnetTask extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }
        @Override
        protected String doInBackground(String... param) {
            if(param[0].equals("open")){
                Log.d("aster","open telnet");
                try {
                    mtc = new MyTelnetClient(SERVER_IP,SERVERPORT);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(param[0].equals("login")){
                try {
                    Log.d("aster","LOGIN " +mtc.getResponse(param[1]));
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            if(param[0].equals("call")){
                Log.d("aster","CALL " +mtc.sendCommand(param[1]));
            }
            if(param[0].equals("exit")){
                Log.d("aster","EXIT " +mtc.sendCommand(param[1]));
            }

            return param[0];
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Log.d("aster","return task "+result);
        }
    }

    private void readContacts(Context context)
    {
        Contact contact;
        Cursor cursor=context.getContentResolver().query(
                ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);

        if(cursor.getCount() > 0) {
            while(cursor.moveToNext()) {
                contact = new Contact();
                String id = cursor.getString(
                        cursor.getColumnIndex(
                                ContactsContract.Contacts._ID));
                contact.setId(id);

                String name = cursor.getString(
                        cursor.getColumnIndex(
                                ContactsContract.Contacts
                                        .DISPLAY_NAME));
                contact.setName(name);

                String has_phone = cursor.getString(
                        cursor.getColumnIndex(
                                ContactsContract.Contacts
                                        .HAS_PHONE_NUMBER));
                if (Integer.parseInt(has_phone) > 0) {
                    // extract phone number
                    Cursor cursor2;
                    cursor2 = context.getContentResolver().query(
                            ContactsContract.CommonDataKinds
                                    .Phone.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds
                                    .Phone.CONTACT_ID + " = ?",
                            new String[]{id},
                            null);
                    while(cursor2.moveToNext()) {
                        String phone = cursor2.getString(
                                cursor2.getColumnIndex(
                                        ContactsContract.
                                                CommonDataKinds.
                                                Phone.NUMBER));
                        contact.setPhone(phone);
                    }
                    cursor2.close();
                }
                contacts.add(contact);
            }
        }
        if(contacts.size()>2) {
          //  Log.d("aster","контакты проитаны  ");
          // Collections.sort(contacts, new NameSorter());
        }
        onSetContacnts(contacts);

    }

    public class NameSorter implements Comparator<Contact> {
        @Override
        public int compare(Contact contact1, Contact contact2) {
            return contact1.getName().compareTo(contact2.getName());
        }
    }

    public void loadCONFIG() {
        sPref = getSharedPreferences("config",MODE_PRIVATE);
        SERVER_IP = sPref.getString("IP", "");
        SERVERPORT = sPref.getInt("PORT", 5038);
        amiuser = sPref.getString("AMIUSER", "");
        amisecret = sPref.getString("AMISECRET", "");
        astercontext = sPref.getString("CONTEXT", "from-internal");
        myphonenumber = sPref.getString("MYPHONE", "");
    }
}