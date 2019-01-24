package com.marcindziedzic.smartparkingsandroid.loginFeature;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.marcindziedzic.smartparkingsandroid.R;
import com.marcindziedzic.smartparkingsandroid.mapsFeature.MapsActivity;

/**
 * This activity enables user to log in or sign up to the application.
 */
public class LoginActivity extends AppCompatActivity implements LoginContract.View {

    private EditText agentNameTV;
    private Button loginButton;

    private LoginContract.UserActionsListener loginPresenter;

    private LocationServices locationServices;

    private ServiceBinder serviceBinder;

    private TextWatcher agentNameTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (agentNameTV.getText() != null && !agentNameTV.getText().toString().equals("")) {
                loginButton.setEnabled(true);
            } else {
                loginButton.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        loginPresenter = new LoginPresenter(this);
        initViews();
        initLocationServices();
        loginPresenter.resolveGooglePlayRequirements(locationServices);
        initServiceBinder();

    }

    private void initServiceBinder() {
        serviceBinder = new ServiceBinderImpl(getApplicationContext());
    }

    private void initLocationServices() {
        locationServices = new LocationServicesUtil(this);
    }

    private void initViews() {
        agentNameTV = findViewById(R.id.agentNameText);
        agentNameTV.setEnabled(false);
        agentNameTV.addTextChangedListener(agentNameTextWatcher);

        loginButton = findViewById(R.id.loginButton);
        loginButton.setEnabled(false);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loginPresenter.connectToService(serviceBinder, agentNameTV.getText().toString());
            }
        });
    }

    @Override
    public void enableLoginViews() {
        agentNameTV.setEnabled(true);

    }

    @Override
    public void showGPSError() {
        Toast.makeText(this, "Wystąpil bląd z GPS", Toast.LENGTH_LONG).show();

    }

    @Override
    public void startMapsActivity() {
        openMapsActivity();
    }

    private void openMapsActivity() {
        Intent intent = new Intent(this, MapsActivity.class);
        intent.putExtra("agentName", agentNameTV.getText().toString());
        startActivity(intent);
    }


}
