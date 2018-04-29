package me.aflak.bluetoothterminal;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.s3.transferutility.TransferListener;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferObserver;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferState;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferType;
import com.amazonaws.mobileconnectors.s3.transferutility.TransferUtility;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import me.aflak.bluetooth.Bluetooth;

public class Chat extends AppCompatActivity implements Bluetooth.CommunicationCallback {
    private String name;
    private Bluetooth b;
//    private EditText message;
    private Button send;
//    private TextView text;
//    private ScrollView scrollView;
    private boolean registered=false;
    private static String SDPATH= Environment.getExternalStorageDirectory().getAbsolutePath();//path of the SD card
    private FileWriter fwriter;
    private boolean status = false;
    boolean res;
    boolean btnpush;
    ImageView good,bad,logo;
    private List<TransferObserver> observers;

    //Timer
    TextView Timer;
    Long startTime=0L;
    Long TimeInMs=0L;
    Long TimeSwapBuff=0L;
    Long updatedTime =0L;
    double timeToWrite = 0;
    private Handler handler = new Handler();
    private TransferUtility transferUtility;


    @TargetApi(Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        Timer = (TextView)findViewById(R.id.timer);
//        message = (EditText)findViewById(R.id.message);
        send = (Button)findViewById(R.id.send);
//        scrollView = (ScrollView) findViewById(R.id.scrollView);
        good = (ImageView) findViewById(R.id.good);
        bad = (ImageView) findViewById(R.id.bad);
        logo = (ImageView) findViewById(R.id.logo);
//        text.setMovementMethod(new ScrollingMovementMethod());
        send.setEnabled(false);


        b = new Bluetooth(this);
        b.enableBluetooth();

        b.setCommunicationCallback(this);

        int pos = getIntent().getExtras().getInt("pos");
        name = b.getPairedDevices().get(pos).getName();

        Display("Connecting...");
        b.connectToDevice(b.getPairedDevices().get(pos));
        btnpush = false;
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!btnpush) {
                    try {
                        FilesWriterGen();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    status = true;
                    btnpush = true;
                    send.setText("Stop Test");
                    startTime = SystemClock.uptimeMillis();
                    handler.postDelayed(updateTimeT,0);
                    logo.setVisibility(View.VISIBLE);
                    good.setVisibility(View.INVISIBLE);
                    bad.setVisibility(View.INVISIBLE);
                }
                else {
                    try {
                        fwriter.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    upload();
                    btnpush = false;
                    status = false;
                    send.setText("Start Test");
                    handler.removeCallbacks(updateTimeT);
                    Timer.setText(""+0+":" + 0 + ":"
                            + String.format("%02d", 000) + ":"
                            + String.format("%03d", 000));
                    timeToWrite = 0;

                }
//                String msg = message.getText().toString();
//                message.setText("");
//                b.send(msg);
//                Display("You: "+msg);
            }
        });

        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
        registerReceiver(mReceiver, filter);
        registered=true;

        ActivityCompat.requestPermissions(Chat.this,
                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                1);

        try {
            FilesWriterGen();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }



    @Override
    public void onDestroy() {
        super.onDestroy();
        if(registered) {
            unregisterReceiver(mReceiver);
            registered=false;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.close:
                b.removeCommunicationCallback();
                b.disconnect();
                Intent intent = new Intent(this, Select.class);
                startActivity(intent);
                finish();
                return true;

            case R.id.rate:
                Uri uri = Uri.parse("market://details?id=" + this.getPackageName());
                Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
                goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
                try {
                    startActivity(goToMarket);
                } catch (ActivityNotFoundException e) {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://play.google.com/store/apps/details?id=" + this.getPackageName())));
                }
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void Display(final String s){
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
//                text.append(s + "\n");
//                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        });
    }

    @Override
    public void onConnect(BluetoothDevice device) {
        Display("Connected to "+device.getName()+" - "+device.getAddress());
        this.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                send.setEnabled(true);
            }
        });
    }

    @Override
    public void onDisconnect(BluetoothDevice device, String message) {
        Display("Disconnected!");
        Display("Connecting again...");
        b.connectToDevice(device);
    }

    @Override
    public void onMessage(String message) {
        try {
            if (status){
            fwriter.write(message+"\n");}
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println(message);
//        Display(name+": "+message);
    }

    @Override
    public void onError(String message) {
        Display("Error: "+message);
    }

    @Override
    public void onConnectError(final BluetoothDevice device, String message) {
        Display("Error: "+message);
        Display("Trying again in 3 sec.");
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        b.connectToDevice(device);
                    }
                }, 2000);
            }
        });
    }

    private void upload(){
        txtFileOperation tfo = new txtFileOperation(getApplicationContext(), "PPG");
        File folder = new File(SDPATH + "/SuperGroup/");
        File[] listOfFiles = folder.listFiles();
        for (int i = 0; i < listOfFiles.length; i++) {
            if (listOfFiles[i].isFile()) {
                System.out.println("Upload File " + listOfFiles[i].getAbsoluteFile());

                if (listOfFiles[i].length()==0){
                    listOfFiles[i].delete();
                    continue;}
                tfo.beginUpload(listOfFiles[i]);

//                sendPost();
                new sendPostTask().execute();
            }
        }

    }


    private class sendPostTask extends AsyncTask<Void,Void,Void> {

        @Override
        protected Void doInBackground(Void... voids) {
            boolean bool =false;
            while (!bool){
            transferUtility = Util.getTransferUtility(getBaseContext());
            observers = transferUtility.getTransfersWithType(TransferType.UPLOAD);
            TransferObserver observer =observers.get(observers.size() - 1);
            TransferState state = observer.getState();
            if (state.equals(TransferState.COMPLETED)) {
                sendPost();
                bool =true;
            }}

            return null;
        }
    }

    private void FilesWriterGen() throws IOException {
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/SuperGroup");
        boolean bool = dir.mkdirs();
        if (bool){
            Toast.makeText(this,"make folder",Toast.LENGTH_SHORT).show();}
        String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/SuperGroup/";
        fwriter = new FileWriter(path +"test.csv");
    }

    private void sendPost(){
        // Instantiate the RequestQueue.
        RequestQueue queue = Volley.newRequestQueue(this);
        String url ="http://18.188.194.41/hello?name=SuperGroup";

        // Request a string response from the provided URL.
        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>(){
                    @Override
                    public void onResponse(String response) {
                        Log.i("Yes", "Name " + response);
                        if (response == "true") {
                            res = true;
                            logo.setVisibility(View.INVISIBLE);
                            good.setVisibility(View.INVISIBLE);
                            bad.setVisibility(View.VISIBLE);
                        }else{ res = false;
                            logo.setVisibility(View.INVISIBLE);
                            good.setVisibility(View.VISIBLE);

                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
            //mTextView.setText("That didn't work!");
            }
        });

// Add the request to the RequestQueue.
        queue.add(stringRequest);
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                Intent intent1 = new Intent(Chat.this, Select.class);

                switch (state) {
                    case BluetoothAdapter.STATE_OFF:
                        if(registered) {
                            unregisterReceiver(mReceiver);
                            registered=false;
                        }
                        startActivity(intent1);
                        finish();
                        break;
                    case BluetoothAdapter.STATE_TURNING_OFF:
                        if(registered) {
                            unregisterReceiver(mReceiver);
                            registered=false;
                        }
                        startActivity(intent1);
                        finish();
                        break;
                }
            }
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {

                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.
                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    Toast.makeText(Chat.this, "Permission denied to read your External storage", Toast.LENGTH_SHORT).show();
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    private  Runnable updateTimeT = new Runnable() {
        @Override
        public void run() {
            TimeInMs= SystemClock.uptimeMillis() - startTime;
            updatedTime = TimeSwapBuff+ TimeInMs;

            int secs = (int) (updatedTime / 1000);
            int mins = secs / 60;
            secs = secs % 60;
            int hours = mins/60;
            mins = mins % 60;
            int milliseconds = (int) (updatedTime % 1000);
            Timer.setText(""+hours+":" + mins + ":"
                    + String.format("%02d", secs) + ":"
                    + String.format("%03d", milliseconds));
            handler.post(this);
        }
    };
}
