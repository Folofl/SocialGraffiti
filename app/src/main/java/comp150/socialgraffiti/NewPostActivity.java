package comp150.socialgraffiti;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

import java.io.ByteArrayOutputStream;

public class NewPostActivity extends Activity {
    private final static int REQUEST_CODE_PHOTO = 1;

    private EditText contentText;
    private ImageView photoThumbnail;
    private TextView durationText;
    private SeekBar durationBar;
    private Button resetButton;
    private boolean addedPhoto;
    private Location mCurrentLocation;
    Bitmap imageBitmap;

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

    public void openCamera(View view) {
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
                startActivityForResult(takePictureIntent, REQUEST_CODE_PHOTO);
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
            addedPhoto = true;

            Bundle extras = data.getExtras();
            imageBitmap = (Bitmap) extras.get("data");
            photoThumbnail.setImageBitmap(imageBitmap);
        }
    }

    public void submitPost (View view) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            graffiti.setUser(user.getUid());
        }

        graffiti.setPhotoURL("");

        graffiti.setContent(contentText.getText().toString());

        if (addedPhoto) {
            graffiti.setHasPhoto(true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            imageBitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
            String imageEncoded = Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
            graffiti.setPhotoURL(imageEncoded);
        } else {
            graffiti.setHasPhoto(false);
            graffiti.setPhotoURL("");
        }

        graffiti.setDuration(durationBar.getProgress() + 1);
        graffiti.setLocation(mCurrentLocation);

        FirebaseDatabase.getInstance()
                .getReference()
                .push()
                .setValue(graffiti);

        Intent returnIntent = new Intent();
        returnIntent.putExtra("GRAFFITI_EXTRA", graffiti);

        if (getParent() == null) {
            setResult(Activity.RESULT_OK, returnIntent);
        } else {
            getParent().setResult(Activity.RESULT_OK, returnIntent);
        }
        finish();
    }

    public void cancelPost (View view) {
        Intent returnIntent = new Intent();
        if (getParent() == null) {
            setResult(Activity.RESULT_OK, returnIntent);
        } else {
            getParent().setResult(Activity.RESULT_OK, returnIntent);
        }
        finish();
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
