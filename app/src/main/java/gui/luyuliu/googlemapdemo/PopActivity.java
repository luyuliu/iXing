package gui.luyuliu.googlemapdemo;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.DisplayMetrics;
import android.widget.ImageView;
import android.widget.Toast;

import com.squareup.picasso.Picasso;

public class PopActivity extends Activity {

    private String url="";
    ImageView imageView;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_pop);


        DisplayMetrics dm = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(dm);

        int width = dm.widthPixels;
        int height = dm.heightPixels;

        getWindow().setLayout((int)(width),(int)(height*0.5));


        imageView=findViewById(R.id.imageView);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            url = extras.getString("url");
            //The key argument here must match that used in the other activity
        }
        loadImageFromUrl(url);
    }


    private void loadImageFromUrl(String url) {
        Picasso.with(this).invalidate(url);
        Picasso.with(this).load (url).placeholder(R.mipmap.loading)
                .error(R.mipmap.ic_launcher)
                .into(imageView,new com.squareup.picasso.Callback(){

                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError() {

                    }
                });
    }
}
