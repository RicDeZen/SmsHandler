package com.dezen.riccardo.myapplication;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

import com.dezen.riccardo.smshandler.SMSMessage;
import com.dezen.riccardo.smshandler.SmsHandler;

import java.util.Set;

//CODE-REVIEW DE ZEN
/**
 * @author Riccardo De Zen.
 */
public class MainActivity extends AppCompatActivity implements SmsHandler.OnSmsEventListener {

    private SmsHandler smsHandler;
    /*If a second instance of smsHandler is created and registered, both trigger their events.
    This is intentional behaviour. Object pool or standard implementation should be considered as
    a future addition.*/
    //private SmsHandler smsHandler2;
    private Button button_send;
    private EditText editText_number;
    private EditText editText_message;
    private TextView textView_last_message;
    private LinearLayout linearLayout;
    final int REQUEST_CODE_SMS = 1;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        smsHandler = new SmsHandler();
        smsHandler.registerReceiver(getApplicationContext(), true, true, true);
        smsHandler.setListener(this);

        button_send = findViewById(R.id.button_send);
        editText_number = findViewById(R.id.editText_number);
        editText_message = findViewById(R.id.editText_message);
        textView_last_message = findViewById(R.id.textView_last_message);
        linearLayout = findViewById(R.id.linear_layout);
        button_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage(editText_number.getText().toString(), editText_message.getText().toString());
            }
        });
        if(!isNotificationListenerEnabled(getApplicationContext())) {
            openNotificationListenSettings(null);
        }
        new MyTask(getApplicationContext()).execute();

        requestSmsPermission();
    }

    /***
     * Asks sms-related permission to OS
     * and close application if user does not accept
     */
    public void requestSmsPermission()
    {
        if ((ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) +
                ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS))
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{"android.permission.SEND_SMS", "android.permission.RECEIVE_SMS"}, REQUEST_CODE_SMS);
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case  REQUEST_CODE_SMS: {
                if (!(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                  closeNow();
                }
                return;
            }
        }
    }

    private void closeNow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN){
            finishAffinity();
        }else{
            finish();
        }
    }

    public boolean isNotificationListenerEnabled(Context context) {
        Set<String> packageNames = NotificationManagerCompat.getEnabledListenerPackages(this);
        return packageNames.contains(context.getPackageName());
    }

    public void openNotificationListenSettings(View v) {
        try {
            Intent intent = new Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS);
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        smsHandler.clearListener();
        smsHandler.unregisterReceiver(getApplicationContext());
    }

    /**
     * Sends a message through this activity's instance of SmsHandler
     * @param destination the destination address for the message, in phone number format
     * @param message the body of the message to be sent, can't be neither null nor empty
     */
    private void sendMessage(String destination, @NonNull String message){
        smsHandler.sendSMS(getApplicationContext(),destination, message);
    }

    /**
     * Method from the OnSmsEventListener interface, reads the body of the message and
     * updates a TextView's content
     * @param message the received message
     */
    @Override
    public void onReceive(SMSMessage message) {
        String from = message.getPeer().getAddress().toString();
        String body = message.getData().toString();
        Log.d("Received message: ",body);
        StringBuilder sb = new StringBuilder();
        sb.append("Last message from: ").append(from).append("\n").append(body);
        textView_last_message.setText(sb.toString());
        View view = getLayoutInflater().inflate(R.layout.image_holder, null);
        ImageView imageView = view.findViewById(R.id.imageView);
        if(body.contains("smile")){
            imageView.setImageDrawable(getDrawable(R.drawable.vector_smile));
        }
        else if(body.contains("wrench")){
            imageView.setImageDrawable(getDrawable(R.drawable.vector_wrench));
        }
        linearLayout.addView(view);
    }
    @Override
    public void onSent(int resultCode, SMSMessage message) {
        Toast.makeText(getApplicationContext(), "May have been sent: "+message.getData(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDelivered(int resultCode, SMSMessage message) {
        Toast.makeText(getApplicationContext(), "May have been delivered: "+message.getData(), Toast.LENGTH_SHORT).show();
    }

    private class MyTask extends AsyncTask<String, Integer, Void>{
        Context context;
        public MyTask(Context context){
            super();
            this.context = context;
        }
        @Override
        protected Void doInBackground(String... strings) {
            smsHandler.fetchUnreadMessages(context);
            return null;
        }
    }
}