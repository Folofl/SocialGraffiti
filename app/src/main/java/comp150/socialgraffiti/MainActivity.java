package comp150.socialgraffiti;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;




public class MainActivity extends Activity {
    private ImageView photoThumbnail;
    private Bitmap bitmap;
    private static final int REQUEST_CODE = 1;
    MarshmallowPermission marshmallowPermission = new MarshmallowPermission(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        photoThumbnail = (ImageView) findViewById(R.id.image_thumbnail);
    }


    /* Called when the user clicks the button */
    public void openCamera(View view) {
        // Check permissions, then open Camera in response to button
        if (!marshmallowPermission.checkPermissionForCamera()) {
            marshmallowPermission.requestPermissionForCamera();
        } else if (!marshmallowPermission.checkPermissionForExternalStorage()) {
            marshmallowPermission.requestPermissionForExternalStorage();
        } else {

            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(takePictureIntent, REQUEST_CODE);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            photoThumbnail.setImageBitmap(imageBitmap);
        }
    }
}
