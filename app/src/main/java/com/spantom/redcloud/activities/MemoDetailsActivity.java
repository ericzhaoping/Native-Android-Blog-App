package com.spantom.redcloud.activities;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.spantom.redcloud.R;

public class MemoDetailsActivity extends AppCompatActivity {

    private TextView memo_title, memo_tagline, memo_description, memo_location;
    private ImageView memo_image;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_memo_details);

        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        memo_title = findViewById(R.id.memo_title);
        memo_tagline = findViewById(R.id.memo_tagline);
        memo_description = findViewById(R.id.memo_description);
        memo_location = findViewById(R.id.memo_location);
        memo_image = findViewById(R.id.memo_image);

        //Receive data

        Intent intent = getIntent();
        String Title = intent.getExtras().getString("Title");
        String Tagline = intent.getExtras().getString("Tagline");
        String Description = intent.getExtras().getString("Description");
        String Location = intent.getExtras().getString("Location");
        String image = intent.getExtras().getString("Thumbnail");

        getSupportActionBar().setTitle(Title);

        //Setting values
        memo_title.setText(Title);
        memo_tagline.setText(Tagline);
        memo_description.setText(Description);
        memo_location.setText(Location);
        //memo_image.setImageResource(image);
        RequestOptions requestOptions = new RequestOptions();
        requestOptions.placeholder(R.drawable.image_placeholder);

        Glide.with(this).applyDefaultRequestOptions(requestOptions).load(image).thumbnail(
                Glide.with(this).load(image)
        ).into(memo_image);
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_memo_details, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.share_app :
                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "RedCloud");
                sharingIntent.putExtra(Intent.EXTRA_TEXT, "Check out my RedCloud app");
                startActivity(Intent.createChooser(sharingIntent, "Tell a friend via..."));
                break;
            default:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
