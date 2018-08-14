package com.seecs.mushtaq;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

import com.seecs.mushtaq.utils.Encryption;

public class EncodeActivity extends ImageActivity {

    static final String EXTRA_MESSAGE = "Extra Message";
    static final String EXTRA_KEY = "Extra KEY";
    static final String KEY_MESSAGE = "Key Message";
    static final String KEY_KEY= "Encryption Key";

    EditText mEditText;
    EditText kEditText;
    Button mEncodeButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.seecs.mushtaq.R.layout.activity_encode);

        mImageView = (ImageView) findViewById(com.seecs.mushtaq.R.id.imageView_encode_thumbnail);
        mEditText  = (EditText)  findViewById(com.seecs.mushtaq.R.id.editText_message);
        kEditText  = (EditText)  findViewById(com.seecs.mushtaq.R.id.editText_key);
        mEncodeButton = (Button) findViewById(com.seecs.mushtaq.R.id.button_upload_encode);

        if (savedInstanceState != null) {
            mEditText.setText(savedInstanceState.getString(KEY_MESSAGE));
            mEditText.setText(savedInstanceState.getString(KEY_KEY));
        }

        mEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mEncodeButton.setEnabled(mEditText.getText().length() > 0 && kEditText.getText().length() > 0);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        kEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mEncodeButton.setEnabled(kEditText.getText().length() > 0 && kEditText.getText().length() > 0);

            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mEncodeButton.setEnabled(mEditText.getText().length() > 0);
        mEncodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent data = new Intent();
                String encodedMessage = null;
                try {
                   byte[] encryptedMessage = Encryption.encrypt(mEditText.getText().toString(), kEditText.getText().toString());
                   encodedMessage = Base64.encodeToString(encryptedMessage,Base64.DEFAULT);
                    Log.v("Stegno EncodeActivity","Sending message: " + encodedMessage);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
                data.putExtra(EXTRA_MESSAGE, encodedMessage);
                data.putExtra(EXTRA_KEY, kEditText.getText().toString());
                setResult(RESULT_OK, data);
                finish();
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
        b.putString(KEY_MESSAGE, mEditText.getText().toString());
        b.putString(KEY_KEY, mEditText.getText().toString());
    }
}