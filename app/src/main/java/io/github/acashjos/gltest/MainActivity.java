package io.github.acashjos.gltest;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void withoutSplitter(View view) {
        Intent i= new Intent(this,CamActivity.class);
        i.putExtra(CamActivity.CAM_SPLITTER_FLAG,false);
        startActivity(i);
    }

    public void withSplitter(View view) {
        Intent i= new Intent(this,CamActivity.class);
        i.putExtra(CamActivity.CAM_SPLITTER_FLAG,true);
        startActivity(i);
    }
}
