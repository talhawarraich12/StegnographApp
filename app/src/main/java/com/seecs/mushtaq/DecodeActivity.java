package com.seecs.mushtaq;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.seecs.mushtaq.utils.Encryption;

public class DecodeActivity extends ImageActivity {

    static final String EXTRA_MESSAGE = "Extra Message";
    static final String EXTRA_KEY = "Extra KEY";
    static final String KEY_MESSAGE = "Key Message";
    static final String KEY_KEY= "Encryption Key";

    EditText mEditText;
    EditText kEditText;
    Button mDecodeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.seecs.mushtaq.R.layout.activity_decode);

        mEditText  = (EditText)  findViewById(com.seecs.mushtaq.R.id.d_editText_message);
        kEditText  = (EditText)  findViewById(com.seecs.mushtaq.R.id.d_editText_key);
        mDecodeButton = (Button) findViewById(com.seecs.mushtaq.R.id.button_decode_message);

        if (savedInstanceState != null) {
            mEditText.setText(savedInstanceState.getString(KEY_MESSAGE));
            kEditText.setText(savedInstanceState.getString(KEY_KEY));
        }


        kEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mDecodeButton.setEnabled(kEditText.getText().length() > 0);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDecodeButton.setEnabled(mEditText.getText().length() > 0);
        mDecodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v("Stegnography Decode","Intent Message :" + getIntent().getStringExtra(EXTRA_MESSAGE) );
                Log.v("Stegnography Decode","Key EDIT TEXT :" + kEditText.getText() );
                try {
                    byte[] decodedMessage = Base64.decode(getIntent().getStringExtra(EXTRA_MESSAGE),Base64.DEFAULT);
                    String decryptedMessage = Encryption.decrypt(decodedMessage, kEditText.getText().toString());
                    Log.v("Stegnography Decode","Decrypt Text :" + decryptedMessage );
                    mEditText.setText(decryptedMessage);
                }
                catch (Exception e) {
                    Log.e("Stegnography", "Decrypt Exception: " + e.getLocalizedMessage() );
                    e.printStackTrace();
                }

            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
        b.putString(KEY_MESSAGE, mEditText.getText().toString());
        b.putString(KEY_KEY, kEditText.getText().toString());
    }
}