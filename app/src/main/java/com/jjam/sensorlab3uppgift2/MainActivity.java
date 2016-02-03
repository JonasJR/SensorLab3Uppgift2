package com.jjam.sensorlab3uppgift2;

import android.app.PendingIntent;
import android.content.Intent;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.NfcManager;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;

import java.nio.charset.Charset;

public class MainActivity extends AppCompatActivity {

    Button loadButton;
    Button sendButton;
    EditText editText;

    NfcManager mNfcManager;
    NfcAdapter mNfcAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        loadButton = (Button) findViewById(R.id.loadButton);
        sendButton = (Button) findViewById(R.id.sendButton);
        editText = (EditText) findViewById(R.id.editText);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mNfcManager = (NfcManager) getSystemService(NFC_SERVICE);
        mNfcAdapter = mNfcManager.getDefaultAdapter();

        if(mNfcAdapter == null){
            return;
        }

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                writeToTagg(MainActivity.this.getIntent());
            }
        });

        loadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                readFromTagg(MainActivity.this.getIntent());
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        Intent intent = new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, 0);
        mNfcAdapter.enableForegroundDispatch(this, pIntent, null, null);
    }

    @Override
    protected void onPause() {
        mNfcAdapter.disableForegroundDispatch(this);
        super.onPause();
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void writeToTagg(Intent intent) {
        Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        Ndef ndef = Ndef.get(tag);
        if(ndef.isWritable()) {
            byte[] mimeBytes = "text/plain".getBytes(Charset.forName("UTF-8"));
            byte[] dataBytes = editText.getText().toString().getBytes(Charset.forName("ISO-8859-1"));
            NdefRecord record = new NdefRecord(NdefRecord.TNF_MIME_MEDIA,mimeBytes,new
                    byte[0],dataBytes);
            NdefMessage message = new NdefMessage(new NdefRecord[]{record});
            try {
                ndef.connect();
                ndef.writeNdefMessage(message);
                Snackbar.make(findViewById(R.id.mainView), message + " was sent to TAG", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
                ndef.close();
            } catch (Exception e) {
            }
        }
    }

    private void readFromTagg(Intent intent) {
        if(intent!=null && NfcAdapter.ACTION_NDEF_DISCOVERED.equals(intent.getAction())) {
            Parcelable[] ndefMessages =
                    intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            for(Parcelable message : ndefMessages) {
                NdefRecord[] records = ((NdefMessage)message).getRecords();
                for(NdefRecord record : records){
                    String msg = new String(record.getPayload(),Charset.forName("ISO-8859-1"));
                    editText.setText(msg);
                    Snackbar.make(findViewById(R.id.mainView), msg, Snackbar.LENGTH_LONG)
                            .setAction("Action", null).show();
                }
            }
        }
    }
}
