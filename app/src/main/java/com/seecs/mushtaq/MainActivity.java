package com.seecs.mushtaq;

import android.Manifest;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.seecs.mushtaq.async.AsyncResponse;
import com.seecs.mushtaq.async.DecodeTask;
import com.seecs.mushtaq.async.EncodeTask;
import com.seecs.mushtaq.async.SteganographyParams;
import com.seecs.mushtaq.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements AsyncResponse<SteganographyParams> {
    static final String TAG = "Steganography";

    static final String EXTRA_FILE_PATH = "Extra File Path";

    static final int PICK_IMAGE_ENCODE = 3;
    static final int PICK_IMAGE_DECODE = 4;
    static final int PICK_IMAGE_SEND   = 5;
    static final int ENCODE_IMAGE = 6;

    private static final int REQUEST_EXTERNAL_STORAGE = 7;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    static final String KEY_FILEPATH = "Filepath";
    private static final String KEY_CAMERA_IMAGE_URI = "Camera Image URI";
    private static final String KEY_LOADING = "Loading";

    String mFilePath;
    Uri mCameraImageUri;
    boolean mLoading;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(com.seecs.mushtaq.R.layout.activity_main);

        findViewById(com.seecs.mushtaq.R.id.loadingPanel).setVisibility(View.GONE);

        Button encodeButton = (Button) findViewById(com.seecs.mushtaq.R.id.button_encode);
        encodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCameraGalleryChooser();
            }
        });

        Button decodeButton = (Button) findViewById(com.seecs.mushtaq.R.id.button_decode);
        decodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage(PICK_IMAGE_DECODE);
            }
        });

        Button sendButton = (Button) findViewById(com.seecs.mushtaq.R.id.button_send);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImage(PICK_IMAGE_SEND);
            }
        });

        if (savedInstanceState != null) {
            mFilePath = savedInstanceState.getString(KEY_FILEPATH);
            mCameraImageUri = savedInstanceState.getParcelable(KEY_CAMERA_IMAGE_URI);
            mLoading = savedInstanceState.getBoolean(KEY_LOADING);

            if (mLoading) {
                findViewById(com.seecs.mushtaq.R.id.loadingPanel).setVisibility(View.VISIBLE);
            }
        }

        if (Build.VERSION.SDK_INT >= 23) {
            if (ContextCompat.checkSelfPermission(this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions(
                        this,
                        PERMISSIONS_STORAGE,
                        REQUEST_EXTERNAL_STORAGE
                );
            }
        }
    }


    private void startCameraGalleryChooser() {
        //Image gallery intent
        Intent galleryIntent;

        if (Build.VERSION.SDK_INT > 19) {
            galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        } else {
            galleryIntent = new Intent();
            galleryIntent.setType("image/*");
            galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
        }

        //Chooser
        Intent chooserIntent = Intent.createChooser(galleryIntent, "Image");

        //Initialize mCameraImageUri
        {
            File root = new File(Environment.getExternalStorageDirectory()
                    + File.separator
                    + "DCIM"
                    + File.separator
                    + "Steganography"
                    + File.separator);

            root.mkdirs();
            String fileName = System.currentTimeMillis() + ".jpg";
            File sdImageMainDirectory = new File(root, fileName);
            mCameraImageUri = Uri.fromFile(sdImageMainDirectory);
        }

        //Add Camera options
        {
            List<Intent> cameraIntents = new ArrayList<>();

            Intent captureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            PackageManager packageManager = getPackageManager();
            List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);

            for(ResolveInfo res : listCam) {
                String packageName = res.activityInfo.packageName;
                Intent intent = new Intent(captureIntent);
                intent.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
                intent.setPackage(packageName);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, mCameraImageUri);
                cameraIntents.add(intent);
            }

            //Camera apps were found
            if (!cameraIntents.isEmpty()) {
                //Add the camera options to chooser
                chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[cameraIntents.size()]));
            }
        }

        startActivityForResult(chooserIntent, PICK_IMAGE_ENCODE);
    }

    private void pickImage(int requestCode) {
        if (Build.VERSION.SDK_INT > 19) {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), requestCode);
        } else {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), requestCode);
        }
    }

    private void startSendActivity(Uri encodedImagePath) {
        Intent intent = new Intent(this, SendImageActivity.class);
        intent.putExtra(EXTRA_FILE_PATH, encodedImagePath);
        startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (resultCode == RESULT_OK) {
            SteganographyParams steganographyParams;
            Intent intent;

            switch (requestCode) {
                case PICK_IMAGE_ENCODE :

                    boolean isCamera;

                    if (data == null || data.getScheme().equals("file")) {
                        isCamera = true;
                    } else {
                        final String action = data.getAction();
                        isCamera = action != null && action.equals(MediaStore.ACTION_IMAGE_CAPTURE);
                    }

                    Uri selectedImageUri;
                    if (isCamera) {
                        selectedImageUri = mCameraImageUri;
                    } else {
                        selectedImageUri = data.getData();
                    }

                    mFilePath = FileUtils.uriToFilePath(this, selectedImageUri);

                    intent = new Intent(this, EncodeActivity.class);
                    intent.putExtra(EXTRA_FILE_PATH, selectedImageUri);

                    startActivityForResult(intent, ENCODE_IMAGE);

                    break;

                case PICK_IMAGE_DECODE :
                    findViewById(com.seecs.mushtaq.R.id.loadingPanel).setVisibility(View.VISIBLE);
                    mLoading = true;

                    mFilePath = FileUtils.uriToFilePath(this, data.getData());

                    steganographyParams = new SteganographyParams(mFilePath, null,null);

                    DecodeTask decodeTask = new DecodeTask(this);
                    decodeTask.execute(steganographyParams);

                    break;

                case PICK_IMAGE_SEND :
                    startSendActivity(data.getData());

                case ENCODE_IMAGE:
                    String message = data.getStringExtra(EncodeActivity.EXTRA_MESSAGE);
                    String key = data.getStringExtra(EncodeActivity.EXTRA_KEY);
                    if (message == null || message.equals("")) {
                        break;
                    }

                    findViewById(com.seecs.mushtaq.R.id.loadingPanel).setVisibility(View.VISIBLE);
                    mLoading = true;

                    steganographyParams = new SteganographyParams(mFilePath, message, key);

                    EncodeTask encodeTask = new EncodeTask(this);
                    encodeTask.execute(steganographyParams);

                    break;
            }
        }
    }

    @Override
    public void processResult(SteganographyParams result, Type t) {
        mLoading = false;
        findViewById(com.seecs.mushtaq.R.id.loadingPanel).setVisibility(View.GONE);

        switch (t) {
            case ENCODE_SUCCESS :
                Uri resultUri = result.getResultUri();
                FileUtils.scanFile(this, FileUtils.uriToFilePath(this, resultUri));
                startSendActivity(resultUri);
                break;

            case DECODE_SUCCESS :
                Intent i = new Intent(MainActivity.this,DecodeActivity.class);
                Log.v("Stegnography", "Intent put message: " + result.getMessage());
                i.putExtra(DecodeActivity.EXTRA_MESSAGE ,result.getMessage() );
                startActivity(i);
                break;

            case FAILURE:
                Toast.makeText(this, result.getMessage(), Toast.LENGTH_LONG).show();
                break;
        }
    }

    /**
     * Marshmallow requires permissions to be granted at run-time.
     * @param requestCode Request code
     * @param permissions Requested permissions
     * @param grantResults Whether permissions were granted.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_EXTERNAL_STORAGE: {
                if (grantResults.length == 0 || grantResults[0] == PackageManager.PERMISSION_DENIED) {
                    Toast.makeText(this, "The app needs to access storage to work.", Toast.LENGTH_LONG).show();
                    findViewById(com.seecs.mushtaq.R.id.button_encode).setEnabled(false);
                    findViewById(com.seecs.mushtaq.R.id.button_decode).setEnabled(false);
                    findViewById(com.seecs.mushtaq.R.id.button_send).setEnabled(false);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(com.seecs.mushtaq.R.menu.activity_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case com.seecs.mushtaq.R.id.menu_item_about:
                FragmentManager fm = getSupportFragmentManager();
                AboutDialogFragment dialog = AboutDialogFragment.newInstance();
                dialog.show(fm, TAG);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);

        if (mFilePath != null) {
            b.putString(KEY_FILEPATH, mFilePath);
        }

        if (mCameraImageUri != null) {
            b.putParcelable(KEY_CAMERA_IMAGE_URI, mCameraImageUri);
        }

        b.putBoolean(KEY_LOADING, mLoading);
    }
}