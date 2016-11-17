package comp150.socialgraffiti;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;


public class MessageActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_message);

        Intent intent = getIntent();
        Graffiti graffiti = (Graffiti) intent.getSerializableExtra(NewPostActivity.EXTRA_GRAFFITI);

        TextView textView = new TextView(this);
        textView.setTextSize(40);
        textView.setText(graffiti.getContent());

        ImageView imageView = new ImageView(this);
        if (graffiti.hasPhoto()) {
            imageView.setImageURI(Uri.parse(graffiti.getPhotoString()));
        }

        ViewGroup layout = (ViewGroup) findViewById(R.id.activity_message);
        layout.addView(textView);
        layout.addView(imageView);
    }
}
