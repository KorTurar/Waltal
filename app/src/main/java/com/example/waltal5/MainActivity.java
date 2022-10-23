package com.example.waltal5;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.DialogFragment;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class MainActivity extends AppCompatActivity {

    String myNameStr;
    Button btnDiscover;
    Button nameButton;
    ListView listView;
    TextView connectionStatus;
    ScrollView scrollView;
     short[] uiAnimBuffer;

    WifiManager wifiManager;
    WifiP2pManager mManager;
    WifiP2pManager.Channel mChannel;

    BroadcastReceiver mReceiver;
    IntentFilter mIntentFilter;

    List<WifiP2pDevice> peers = new ArrayList<WifiP2pDevice>();
    String[] deviceNameArray;
    WifiP2pDevice[] deviceArray;

    static final int MESSAGE_READ = 1;

    Socket socket;

    ServerClass serverClass;
    ClientClass clientClass;
    ConversationBackgr conversationBackgr;
    ConversationUI conversationUI;

    boolean isHost;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        try {
            initialWork();
            exqListener();
            testUiAnimWithMicSound();
            Log.d("orient","dfxghj");
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }

    }


    private void exqListener() {

        btnDiscover.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        connectionStatus.setText("Discover Started");
                    }

                    @Override
                    public void onFailure(int i) {
                        connectionStatus.setText("Discover Starting Failed");
                    }
                });
            }
        });
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final WifiP2pDevice device = deviceArray[position];
                WifiP2pConfig config = new WifiP2pConfig();
                config.deviceAddress = device.deviceAddress;

                /*if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }*/
                mManager.connect(mChannel, config, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        connectionStatus.setText("Connected to" + device.deviceName);
                    }

                    @Override
                    public void onFailure(int reason) {
                        connectionStatus.setText("Not Connnected");
                    }
                });
            }
        });


       nameButton.setOnClickListener(new View.OnClickListener() {
           @Override
           public void onClick(View v) {
               Dialog dialog = new Dialog(MainActivity.this);
               dialog.setCancelable(true);
               dialog.setContentView(R.layout.name_dialog);

               EditText nameEdiText = dialog.findViewById(R.id.dialogMyNameEditText);
               Button okButton = dialog.findViewById(R.id.dialogOkButton);
               okButton.setOnClickListener(new View.OnClickListener() {
                   @RequiresApi(api = Build.VERSION_CODES.KITKAT)
                   @Override
                   public void onClick(View v) {
                       myNameStr = nameEdiText.getText().toString();
                       nameButton.setText(myNameStr);
                       try {
                           setDeviceName();
                           //Log.d("name", Settings.System.getString(getContentResolver(), "device_name"));
                       } catch (NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                           e.printStackTrace();
                       }
                       dialog.dismiss();
                   }
               });
               dialog.show();
           }
       });
    }
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void initialWork() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        nameButton = (Button) findViewById(R.id.nameButton);
        btnDiscover = (Button) findViewById(R.id.discover);
        listView = (ListView) findViewById(R.id.peerListView);
        connectionStatus = (TextView) findViewById(R.id.connectionStatus);
        conversationUI = (ConversationUI) findViewById(R.id.conversationUI);
        conversationBackgr = new ConversationBackgr(this);
        scrollView = (ScrollView) findViewById(R.id.scroll);
        myNameStr = "";
        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        mManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
        if (mManager == null)
        {
            Log.d("mManagercheck", "initialWork: mManager = null");
        }
        mChannel = mManager.initialize(this,getMainLooper(),null);



        mReceiver = new WifiDirectBroadcastReceiver(mManager,mChannel,this);

        mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);


    }

    WifiP2pManager.PeerListListener peerListListener = new WifiP2pManager.PeerListListener() {
        @Override
        public void onPeersAvailable(WifiP2pDeviceList peerList) {
            if(!peerList.getDeviceList().equals(peers)){
                peers.clear();
                peers.addAll(peerList.getDeviceList());

                deviceNameArray = new String[peerList.getDeviceList().size()];
                deviceArray = new WifiP2pDevice[peerList.getDeviceList().size()];
                int index=0;

                for(WifiP2pDevice device: peerList.getDeviceList()){
                    deviceNameArray[index]=device.deviceName;
                    deviceArray[index]=device;
                    index++;
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<String>(getApplicationContext(),android.R.layout.simple_list_item_1,deviceNameArray);
                listView.setAdapter(adapter);
                //scrollView.setBackgroundColor(Color.argb(127,150,150,150));
            }

            if(peers.size()==0){
                Toast.makeText(getApplicationContext(),"No Device Found", Toast.LENGTH_SHORT).show();
                return;
            }
        }
    };

    WifiP2pManager.ConnectionInfoListener connectionInfoListener = new WifiP2pManager.ConnectionInfoListener() {
        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo info) {
            final InetAddress groupOwnerAddress = info.groupOwnerAddress;

            if (info.groupFormed && info.isGroupOwner){
                connectionStatus.setText("Host");
                isHost=true;
                serverClass = new ServerClass();
                serverClass.start();
            }else if(info.groupFormed){
                connectionStatus.setText("Client");
                isHost=false;
                clientClass = new ClientClass(groupOwnerAddress);
                clientClass.start();
            }
        }
    };

    public void setDeviceName() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        Method m = mManager.getClass().getMethod("setDeviceName", mChannel.getClass(), String.class,
                WifiP2pManager.ActionListener.class);

        m.invoke(mManager, mChannel, myNameStr.equals("")?"defaultName":myNameStr, new WifiP2pManager.ActionListener() {

            @Override
            public void onSuccess() {
            }
            @Override
            public void onFailure(int reason) {
            }
        });
    }
    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mReceiver,mIntentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mReceiver);

    }




    //Sensor Fin--------------------------------
    public class ServerClass extends Thread{

        ServerSocket serverSocket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public void write(byte[] bytes){
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            try {
                serverSocket = new ServerSocket(8888);
                socket = serverSocket.accept();
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            }catch (IOException e){
                e.printStackTrace();
            }

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void run() {
                    byte[] inBuffer = new byte[conversationBackgr.getIntBufferSize()];
                    byte[] outBuffer = new byte[conversationBackgr.getIntBufferSize()];
                    int bytes;
                    while (socket!=null){
                        try {
                            conversationBackgr.readSound(outBuffer );
                            //conversationBackgr.readSoundShort(uiAnimBuffer );
                            outputStream.write(outBuffer);
                            Log.d("outbuffer", Arrays.toString(outBuffer));
                            conversationUI.setMyVoiceAmplitude(Math.abs(outBuffer[0]));
                            bytes = inputStream.read(inBuffer);
                            conversationBackgr.playSound(inBuffer, bytes );
                            Log.d("inbuffer", Arrays.toString(inBuffer));
                            conversationUI.setFriendsVoiceAmplitude(Math.abs(inBuffer[0]));

                            /*if(bytes>0){
                                int finalBytes=bytes;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String tempMSG = new String(buffer,0,finalBytes);
                                        read_msg_box.setText(tempMSG);
                                    }
                                });
                            }*/
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    /*private class SendReceive extends Thread{
        private Socket socket;
        private InputStream inputStream;
        private OutputStream outputStream;

        public SendReceive(Socket skt){
            socket=skt;
            try {
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void run() {
            byte[] buffer = new byte[1024];
            int bytes;
            while (socket!=null){
                try {
                    bytes = inputStream.read(buffer);
                    if(bytes>0){
                        handler.obtainMessage(MESSAGE_READ,bytes,-1,buffer).sendToTarget();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void write(byte[] bytes){
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }*/

    public class ClientClass extends Thread{

        String hostAdd;
        private InputStream inputStream;
        private OutputStream outputStream;
        public ClientClass(InetAddress hostAddress){
            hostAdd = hostAddress.getHostAddress();
            socket = new Socket();
        }

        public void write(byte[] bytes){
            try {
                outputStream.write(bytes);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        @Override
        public void run() {
            try {
                socket.connect(new InetSocketAddress(hostAdd,8888),500);
                inputStream = socket.getInputStream();
                outputStream = socket.getOutputStream();
            }catch (IOException e){
                e.printStackTrace();
            }

            ExecutorService executor = Executors.newSingleThreadExecutor();
            Handler handler = new Handler(Looper.getMainLooper());

            executor.execute(new Runnable() {
                @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
                @Override
                public void run() {
                    byte[] inBuffer = new byte[conversationBackgr.getIntBufferSize()];
                    byte[] outBuffer = new byte[conversationBackgr.getIntBufferSize()];
                    int bytes;
                    while (socket!=null){
                        try {
                            conversationBackgr.readSound(outBuffer );
                            //conversationBackgr.readSoundShort(uiAnimBuffer );
                            outputStream.write(outBuffer);
                            Log.d("outbuffer", Arrays.toString(outBuffer));
                            conversationUI.setMyVoiceAmplitude(Math.abs(outBuffer[0]));
                            bytes = inputStream.read(inBuffer);
                            conversationBackgr.playSound(inBuffer, bytes );
                            Log.d("inbuffer", Arrays.toString(inBuffer));
                            conversationUI.setFriendsVoiceAmplitude(Math.abs(inBuffer[0]));


                            /*if(bytes>0){
                                int finalBytes=bytes;
                                handler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        String tempMSG = new String(buffer,0,finalBytes);
                                        read_msg_box.setText(tempMSG);
                                    }
                                });
                            }*/
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
            });
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
    @Override
    protected void onDestroy() {
        super.onDestroy();
        conversationBackgr.release();

        /*try {
            if (isHost)
                {
                    serverClass.inputStream.close();
                    serverClass.outputStream.close();

                }
            else
                {
                    clientClass.inputStream.close();
                    clientClass.outputStream.close();
                }
            mManager.cancelConnect(mChannel, null);
            mManager.stopPeerDiscovery(mChannel, null);

        } catch (IOException e) {
            e.printStackTrace();
        }*/

    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void testUiAnimWithMicSound (){
        uiAnimBuffer = new short[conversationBackgr.getIntBufferSize()];
        byte[] byteBuffer = new byte[conversationBackgr.getIntBufferSize()];
        new Thread(new Runnable() {

            @Override
            public void run() {
                while (true){
                    //Log.d("debug", "3");
                    conversationBackgr.readSound(byteBuffer);
                    //Log.d("debug", String.valueOf(ok));
                    //conversationBackgr.readSound(byteBuffer);
                    conversationUI.setMyVoiceAmplitude(Math.abs(byteBuffer[0]));
                    //Log.d("debug", String.valueOf(byteBuffer[0]));


                }
            }
        }).start();
    }


}

