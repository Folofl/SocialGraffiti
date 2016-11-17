package comp150.socialgraffiti;

import android.app.Activity;
import android.content.Intent;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class NewPostActivity extends Activity {
    private final static int REQUEST_CODE_PHOTO = 1;

    private EditText contentText;
    private ImageView photoThumbnail;
    private TextView durationText;
    private SeekBar durationBar;
    private Button resetButton;
    private boolean addedPhoto;
    private Location mCurrentLocation;

    String mCurrentPhotoPath;
    Uri photoURI;
    String photoString;

    public final static String EXTRA_GRAFFITI = "comp150.socialgraffiti.GRAFFITI";
    MarshmallowPermission marshmallowPermission = new MarshmallowPermission(this);
    Graffiti graffiti = new Graffiti();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_post);

        Intent intent = getIntent();
        if (intent != null) {
            mCurrentLocation = intent.getParcelableExtra("LOCATION_EXTRA");
        }

        contentText = (EditText) findViewById(R.id.text_message);
        photoThumbnail = (ImageView) findViewById(R.id.image_thumbnail);
        addedPhoto = false;
        resetButton = (Button) findViewById(R.id.button_resetPic);
        resetButton.setAlpha(.5f);
        resetButton.setClickable(false);
        durationText = (TextView) findViewById(R.id.text_duration);
        durationBar = (SeekBar) findViewById(R.id.seekbar_duration);
        durationBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (seekBar == durationBar) {
                    updateDurationText();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                //
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                //
            }
        });

        updateDurationText();
    }

    @Override
    public void onBackPressed() {
    }

    /* Called when the user clicks the appropriate button */
    public void openCamera(View view) {
        // Check permissions
        if (!marshmallowPermission.checkPermissionForCamera()) {
            marshmallowPermission.requestPermissionForCamera();
            return;
        }
        if (!marshmallowPermission.checkPermissionForExternalStorage()) {
            marshmallowPermission.requestPermissionForExternalStorage();
            return;
        }

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error
            }
            if (photoFile != null) {
                photoURI = FileProvider.getUriForFile(this,
                        "comp150.socialgraffiti.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_CODE_PHOTO);
            }
        }
    }

    public void resetPhoto(View view) {
        photoThumbnail.setImageResource(android.R.drawable.ic_menu_camera);
        resetButton.setAlpha(0.5f);
        resetButton.setClickable(false);
        addedPhoto = false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE_PHOTO && resultCode == RESULT_OK) {
            photoThumbnail.setImageURI(photoURI);
            resetButton.setAlpha(1f);
            resetButton.setClickable(true);
            addedPhoto = true;
        }
    }

    public void submitPost (View view) {
        graffiti.setContent(contentText.getText().toString());
        if (addedPhoto) {
            graffiti.setHasPhoto(true);
            photoString = photoURI.toString();
            graffiti.setPhotoString(photoString);
        }
        graffiti.setDuration(durationBar.getProgress());
        graffiti.setLocation(mCurrentLocation);

//        Intent submitPostIntent = new Intent(this, MessageActivity.class);
//        submitPostIntent.putExtra(EXTRA_GRAFFITI, graffiti);
//        startActivity(submitPostIntent);

        Intent returnIntent = new Intent(); // might have to do getIntent();
        returnIntent.putExtra("GRAFFITI_EXTRA", graffiti);
//        setResult(Activity.RESULT_OK, returnIntent);
//        finish();
        if (getParent() == null) {
            setResult(Activity.RESULT_OK, returnIntent);
        } else {
            getParent().setResult(Activity.RESULT_OK, returnIntent);
        }
        finish();

    }

    /* Taken from: https://developer.android.com/training/camera/photobasics.html */
    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,
                ".jpg",
                storageDir
        );

        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void updateDurationText() {
        int duration = durationBar.getProgress();
        duration += 1;

        if (duration == 1) {
            durationText.setText(duration + " hour");
        } else {
            durationText.setText(duration + " hours");
        }
    }
}
