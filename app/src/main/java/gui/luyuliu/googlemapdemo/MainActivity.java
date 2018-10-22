package gui.luyuliu.googlemapdemo;

import android.app.Dialog;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

public class MainActivity extends AppCompatActivity{

    private static final String TAG="MainActivity";

    private static final int ERROR_DIALOG_REQUEST=9001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (isServiceOk()){
            init();
        }

    }

    private void init(){
        Button btnMap=(Button) findViewById(R.id.btnMap);
        btnMap.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                Intent intent=new Intent(MainActivity.this,MapActivity.class);
                startActivity(intent);
            }
        });

    }
    public boolean isServiceOk(){
        Log.d(TAG,"isServiceOk: checking google service version: ");
        int availble= GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this);
        if (availble== ConnectionResult.SUCCESS){
            Log.d(TAG,"isServiceOK: Google Play Services are working");
            return true;
        }
        else if (GoogleApiAvailability.getInstance().isUserResolvableError(availble)){
            Log.d(TAG, "isServiceOk: an error occered.");
            Dialog dialog=GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this,availble,ERROR_DIALOG_REQUEST);
            dialog.show();
            return false;
        }
        else{
            Toast.makeText(this,"Map requests fatal error.",Toast.LENGTH_SHORT).show();
            return false;
        }
    }
}
