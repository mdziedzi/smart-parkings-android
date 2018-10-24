package com.marcindziedzic.smartparkingsandroid;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.marcindziedzic.smartparkingsandroid.agent.DriverManagerAgent;
import com.marcindziedzic.smartparkingsandroid.agent.util.Localization;

import java.util.logging.Level;

import jade.android.AndroidHelper;
import jade.android.MicroRuntimeService;
import jade.android.MicroRuntimeServiceBinder;
import jade.android.RuntimeCallback;
import jade.core.MicroRuntime;
import jade.core.Profile;
import jade.util.Logger;
import jade.util.leap.Properties;
import jade.wrapper.AgentController;
import jade.wrapper.ControllerException;

public class MainActivity extends AppCompatActivity {

    private Logger logger = Logger.getJADELogger(this.getClass().getName());

    private MicroRuntimeServiceBinder microRuntimeServiceBinder;
    private ServiceConnection serviceConnection;

    private EditText agentNameTV;

    private Button button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        agentNameTV = findViewById(R.id.agentNameText);
        agentNameTV.addTextChangedListener(agentNameTextWatcher);

        button = findViewById(R.id.nextButton);
        button.setEnabled(false);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connectToService();
                openMapsActivity();
            }
        });

    }

    private TextWatcher agentNameTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            if (agentNameTV.getText() != null && !agentNameTV.getText().toString().equals("")) {
                button.setEnabled(true);
            } else {
                button.setEnabled(false);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    private void openMapsActivity() {
        Intent intent = new Intent(this, MapsActivity.class);
        startActivity(intent);
    }

    private void connectToService() {

        serviceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                // Bind successful
                microRuntimeServiceBinder = (MicroRuntimeServiceBinder) service;
                logger.log(Level.INFO, "Gateway successfully bound to MicroRuntimeService");
                startContainer();

            }

            @Override
            public void onServiceDisconnected(ComponentName className) { // Bind unsuccessful
                microRuntimeServiceBinder = null;
                logger.log(Level.INFO, "Gateway unbound from MicroRuntimeService");

            }
        };

        logger.log(Level.INFO, "Binding Gateway to MicroRuntimeService...");

        bindService(new Intent(getApplicationContext(), MicroRuntimeService.class), serviceConnection,
                Context.BIND_AUTO_CREATE);
    }

    private void startContainer() {

        Properties pp = new Properties();
        pp.setProperty(Profile.MAIN_HOST, "192.168.0.14");
        pp.setProperty(Profile.MAIN_PORT, "1099");
        pp.setProperty(Profile.JVM, Profile.ANDROID);

        if (AndroidHelper.isEmulator()) {
            // Emulator: this is needed to work with emulated devices
            pp.setProperty(Profile.LOCAL_HOST, AndroidHelper.LOOPBACK);
        } else {
            pp.setProperty(Profile.LOCAL_HOST,
                    AndroidHelper.getLocalIPAddress());
        }
        // Emulator: this is not really needed on a real device
        pp.setProperty(Profile.LOCAL_PORT, "2000");

        microRuntimeServiceBinder.startAgentContainer(pp, new RuntimeCallback<Void>() {

            @Override
            public void onSuccess(Void thisIsNull) {
                // Split container startup successful
                logger.log(Level.INFO, "Successfully start of the container...");
                startAgent(agentNameTV.getText().toString(), agentStartupCallback);
            }

            @Override
            public void onFailure(Throwable throwable) {
                // Split container startup error
            }
        });
    }

    private void startAgent(
            final String nickname,
            final RuntimeCallback<AgentController> agentStartupCallback) {

        microRuntimeServiceBinder.startAgent(
                nickname,
                DriverManagerAgent.class.getName(),
                new Object[]{new Localization(10, 10)},
                new RuntimeCallback<Void>() {
                    @Override
                    public void onSuccess(Void thisIsNull) {
                        logger.log(Level.INFO, "Successfully start of the "
                                + DriverManagerAgent.class.getName() + "...");
                        try {
                            agentStartupCallback.onSuccess(MicroRuntime
                                    .getAgent(nickname));
                        } catch (ControllerException e) {
                            // Should never happen
                            agentStartupCallback.onFailure(e);
                        }
                    }

                    @Override
                    public void onFailure(Throwable throwable) {
                        logger.log(Level.SEVERE, "Failed to start the "
                                + DriverManagerAgent.class.getName() + "...");
                        agentStartupCallback.onFailure(throwable);
                    }
                });
    }

    private RuntimeCallback<AgentController> agentStartupCallback = new RuntimeCallback<AgentController>() {

        @Override
        public void onSuccess(AgentController agent) {
        }

        @Override
        public void onFailure(Throwable throwable) {
            logger.log(Level.INFO, "Nickname already in use!");
        }
    };

}
