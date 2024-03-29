package com.id05.asteriskcallmedisa;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ActionMenuItemView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.ViewPager;
import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SoundEffectConstants;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import com.google.android.material.tabs.TabLayout;
import com.id05.asteriskcallmedisa.data.AmiState;
import com.id05.asteriskcallmedisa.data.Call;
import com.id05.asteriskcallmedisa.data.Contact;
import com.id05.asteriskcallmedisa.util.AbstractAsyncWorker;
import com.id05.asteriskcallmedisa.util.ConnectionCallback;
import com.id05.asteriskcallmedisa.util.DateBase;
import com.id05.asteriskcallmedisa.util.MyTelnetClient;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import static com.id05.asteriskcallmedisa.CallsFragment.calls;

public class MainActivity extends AppCompatActivity implements  ConnectionCallback, RecordAdapter.OnContactClickListener {

    boolean callingState;
    static Drawable wait;
    AmiState amistate = new AmiState();
    static int SERVER_PORT;
    static String SERVER_IP;
    static String amiuser;
    static String amisecret;
    static String astercontext;
    static String myphonenumber;
    MyTelnetClient mtc;
    SharedPreferences sPref;
    int PERMISSIONS_REQUEST_READ_CONTACTS = 10;
    static ArrayList<Contact> contacts = new ArrayList<>();
    static DateBase dbHelper;
    @SuppressLint("StaticFieldLeak")
    static EditText inputNumber;
    Button but0,but1,but2,but3, but4, but5, but6, but7, but8, but9;
    @SuppressLint("StaticFieldLeak")
    static ImageButton butDel;
    ImageButton butCall;
    static SlidingUpPanelLayout slPanel;
    Context context;
    Animation animationRotateRight = null;
    static Animation animationRotateLeft = null;
    static Animation animationWait = null;
    static Drawable dial, backspace;
    AudioManager audioManager;
    ClipboardManager clipboardManager;
    ViewPager viewPager;

    @SuppressLint("UseCompatLoadingForDrawables")
    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        dbHelper = new DateBase(this);
        viewPager = findViewById(R.id.viewPage);
        TabLayout tabLayout = findViewById(R.id.tabLayout);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED);
        audioManager = (AudioManager) this.getSystemService(Context.AUDIO_SERVICE);
        inputNumber = findViewById(R.id.inputNumber);
        clipboardManager=(ClipboardManager)getSystemService(CLIPBOARD_SERVICE);
        inputNumber.setOnLongClickListener(v -> {
            ClipData data = clipboardManager.getPrimaryClip();
            ClipData.Item item = data.getItemAt(0);
            String text = item.getText().toString();
            String buf = "+0123456789";
            StringBuilder stringBuilder = new StringBuilder();
            for(int i=0; i<text.length(); i++){
                if(buf.contains(String.valueOf(text.charAt(i)))){
                    stringBuilder.append(text.charAt(i));
                }
            }
            inputNumber.setText(stringBuilder.toString());
            return false;
        });

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

        slPanel = findViewById(R.id.sliding_layout);
        slPanel.setShadowHeight(0);
        slPanel.addPanelSlideListener(panelSlideListener);
        dial = getResources().getDrawable(R.drawable.ic_baseline_dialpad_24);
        backspace = getResources().getDrawable(R.drawable.ic_baseline_backspace_24);
        wait = getResources().getDrawable(R.drawable.ic_baseline_wait_24);

        loadCONFIG();

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_CONTACTS) ==
                PackageManager.PERMISSION_GRANTED)
        {
            readContacts(this);
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{
                            Manifest.permission
                                    .READ_CONTACTS},
                    PERMISSIONS_REQUEST_READ_CONTACTS);
        }

        inputNumber.setKeyListener(null);
        animationRotateRight = AnimationUtils.loadAnimation(this, R.anim.rotateright);
        animationRotateRight.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                butDel.setImageDrawable(backspace);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        animationRotateLeft = AnimationUtils.loadAnimation(this, R.anim.rotateleft);
        animationRotateLeft.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                butDel.setImageDrawable(dial);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        animationWait = AnimationUtils.loadAnimation(this, R.anim.rotate);
        animationWait.setRepeatCount(Animation.INFINITE);
        animationWait.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                butDel.startAnimation(animationWait);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        try {
            AmiPhonePageAdapter amiPhonePageAdapter = new AmiPhonePageAdapter(getSupportFragmentManager(), tabLayout.getTabCount());
            viewPager.setAdapter(amiPhonePageAdapter);
            viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
            tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    viewPager.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

        }catch (Exception e){
           //
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSIONS_REQUEST_READ_CONTACTS) {
            if ((grantResults.length > 0) &&
                    (grantResults[0] ==
                            PackageManager.PERMISSION_GRANTED)) {
                readContacts(this);
            }
        }
    }

    public static ArrayList<Call> getCallList(){
        SQLiteDatabase userDB = dbHelper.getWritableDatabase();
        ArrayList<Call> bufList = new ArrayList<>();
            Cursor cursor = userDB.query("calls", null, null, null, null, null, null);
            if (cursor.moveToFirst()) {
                do {
                    Call call = new Call(
                            cursor.getString(cursor.getColumnIndex("name")),
                            cursor.getString(cursor.getColumnIndex("number")),
                            cursor.getString(cursor.getColumnIndex("datecall")));
                    bufList.add(call);
                }
                while (cursor.moveToNext());
                cursor.close();
            }
            return bufList;
    }

    public static void callAddBase(Call call){
        ContentValues newValues = new ContentValues();
        newValues.put("name",call.getName());
        newValues.put("number",call.getNumber());
        newValues.put("datecall",call.getCallDate());
        try {
            SQLiteDatabase userDB = dbHelper.getWritableDatabase();
            userDB.insertOrThrow("calls", null, newValues);
            userDB.close();
        }catch (SQLException e){
            print("error add to base "+e.toString());
        }
    }

    public static void deleteAllCalls(){
        SQLiteDatabase userDB = dbHelper.getWritableDatabase();
        userDB.delete("calls",null,null);
    }

    public static String getFullCurrentDate(){
        Date currentDate = new Date();
        DateFormat dateFormat = new SimpleDateFormat("dd.MM.yy", Locale.getDefault());
        String dateText = dateFormat.format(currentDate);
        DateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String timeText = timeFormat.format(currentDate);
        return (timeText+" "+dateText);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void readContacts(Context context)
    {
        Contact contact;
        @SuppressLint("Recycle") Cursor cursor=context.getContentResolver().query(
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

        try{
        Collections.sort(contacts, (o1, o2) -> o1.getName().compareTo(o2.getName()));
        }catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    View.OnClickListener digitClick = new View.OnClickListener() {
        @SuppressLint({"NonConstantResourceId", "SetTextI18n"})
        @Override
        public void onClick(View v) {
            String buf = inputNumber.getText().toString();
            audioManager.playSoundEffect(SoundEffectConstants.CLICK,1.0f);
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
                    v.playSoundEffect(SoundEffectConstants.CLICK);
                    if(slPanel.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED){
                        slPanel.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                    }
                    if(slPanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                        if (buf.length() != 0) {
                            inputNumber.setText(buf.substring(0, buf.length() - 1));
                        }else {
                            slPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                        }
                    }
                    break;
                case R.id.butCall:{
                    if((!SERVER_IP.equals("")) & (SERVER_PORT > 0)
                            & (!amiuser.equals("")) & (!amisecret.equals(""))
                            & (!astercontext.equals("")) & (!myphonenumber.equals(""))) {
                        calling(inputNumber.getText().toString());
                    }else{
                        Toast toast = Toast.makeText(context,
                                "Сheck your settings", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
                    break;
            }
        }
    };

    @SuppressLint("StaticFieldLeak")
    public void doSomethingAsyncOperaion(final AmiState amistate) {
        new AbstractAsyncWorker(this, amistate) {
            @SuppressLint("StaticFieldLeak")
            @Override
            protected AmiState doAction() throws Exception {
                if(amistate.getAction().equals("open")){
                    mtc = new MyTelnetClient(SERVER_IP,SERVER_PORT);
                    amistate.setResultOperation(mtc.isConnected());
                }
                if(amistate.getAction().equals("login")){
                    String com1 = "Action: Login\n"+
                            "Events: off\n"+
                            "Username: "+amiuser+"\n"+
                            "Secret: "+amisecret+"\n";
                    String buf = mtc.getResponse(com1);
                    amistate.setResultOperation(buf.contains("Response: SuccessMessage: Authentication accepted"));
                    amistate.setDescription(buf);
                }
                if(amistate.getAction().equals("call")){
                    String comenter = "Action: Originate\n" +
                            "Channel: Local/"+myphonenumber+"@"+astercontext+"\n" +
                            "Exten: "+amistate.instruction+"\n" +
                            "Context: "+astercontext+"\n" +
                            "Priority: 1\n" +
                            "Async: true\n" +
                            "CallerID: "+myphonenumber+"\n" +
                            "ActionID: 123\n";
                    Boolean buf = mtc.sendCommand(comenter);
                    amistate.setResultOperation(buf);
                }
                if(amistate.getAction().equals("exit")){
                    String com1 = "Action: Logoff\n";
                    mtc.sendCommand(com1);
                    amistate.setResultOperation(true);
                    amistate.setDescription("");
                }
                return amistate;
            }
        }.execute();
    }

    @SuppressLint("SetTextI18n")
    public void calling(String number){
        Call call = new Call(number,number,getFullCurrentDate());
        callAddBase(new Call(call.getName(),call.getNumber(),getFullCurrentDate()));
        try{
            Collections.reverse(calls);
            calls.add(call);
            Collections.reverse(calls);
            CallsFragment.adapter.notifyDataSetChanged();
        }catch (Exception e){
            print("error callng = "+e.getMessage());
        }

        String buf = number.replace(" ","");
        number = buf.replace("-","");
        callingState = true;
        slPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
        butDel.setImageDrawable(wait);
        butDel.startAnimation(animationWait);
        inputNumber.setText("WAIT");
        amistate.action = "open";
        amistate.instruction = number;
        doSomethingAsyncOperaion(amistate);
    }


    private final SlidingUpPanelLayout.PanelSlideListener panelSlideListener =
            new SlidingUpPanelLayout.PanelSlideListener() {
                @Override
                public void onPanelSlide(View view, float v) {

                }

                @Override
                public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
                    if(!callingState){
                        if(slPanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                            butDel.startAnimation(animationRotateRight);
                        }
                        if(slPanel.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED){
                            inputNumber.setText("");
                            butDel.startAnimation(animationRotateLeft);
                            slPanel.setPanelHeight((int) (40 * context.getResources().getDisplayMetrics().density));
                        }
                    }else{
                        if(slPanel.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                            slPanel.setEnabled(false);
                        }
                    }
                }
            };


    @Override
    public void onBackPressed() {
        if(slPanel.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                slPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            @SuppressLint("UseCompatLoadingForDrawables") Drawable myDrawable = getResources().getDrawable(R.drawable.ic_baseline_dialpad_24);
            butDel.setImageDrawable(myDrawable);
        }else {
            if(callingState){
                inputNumber.setText("");
                butDel.setImageDrawable(dial);
                butDel.startAnimation(animationRotateLeft);
                callingState = false;
                slPanel.setEnabled(true);
            }else {
                super.onBackPressed();
            }
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
        if (id == R.id.settings) {
            ActionMenuItemView image = findViewById(R.id.settings);
            image.startAnimation(animationRotateLeft);
            Intent i = new Intent(MainActivity.this, SettingsActivity.class);
            slPanel.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
            inputNumber.setText("");
            startActivity(i);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onBegin() {

    }

    @Override
    public void onSuccess(AmiState amistate) {
        String buf = amistate.getAction();
        if(buf.equals("open")){
            amistate.setAction("login");
            doSomethingAsyncOperaion(amistate);
        }
        if(buf.equals("login")){
            amistate.setAction("call");
            doSomethingAsyncOperaion(amistate);
        }
        if(buf.equals("call")){
            amistate.setAction("exit");
            doSomethingAsyncOperaion(amistate);
        }
        if(buf.equals("exit")){
            inputNumber.setText("");
            butDel.setImageDrawable(dial);
            butDel.startAnimation(animationRotateLeft);
            callingState = false;
            slPanel.setEnabled(true);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onFailure(AmiState amistate) {
//        inputNumber.setText(amistate.getAction()+" error");
//        butDel.setImageDrawable(dial);
//        butDel.startAnimation(animationRotateLeft);
//        callingState = false;
//        slPanel.setEnabled(true);
        inputNumber.setText(amistate.getAction()+" error");
        butDel.setImageDrawable(dial);
        callingState = true;
    }

    @Override
    public void onEnd() {

    }

    public void loadCONFIG() {
        sPref = getSharedPreferences("config",MODE_PRIVATE);
        SERVER_IP = sPref.getString("IP", "");
        SERVER_PORT = sPref.getInt("PORT", 5038);
        amiuser = sPref.getString("AMIUSER", "");
        amisecret = sPref.getString("AMISECRET", "");
        astercontext = sPref.getString("CONTEXT", "from-internal");
        myphonenumber = sPref.getString("MYPHONE", "");
    }

    @Override
    public void onContactClick(int position) {

    }

    public static void print(String str){
        Log.d("aster",str);
    }
}