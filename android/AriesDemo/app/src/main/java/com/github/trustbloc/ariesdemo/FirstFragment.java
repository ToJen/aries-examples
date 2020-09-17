/*
 * Copyright SecureKey Technologies Inc. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.github.trustbloc.ariesdemo;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import org.hyperledger.aries.api.AriesController;
import org.hyperledger.aries.api.VerifiableController;
import org.hyperledger.aries.api.DIDExchangeController;
import org.hyperledger.aries.ariesagent.Ariesagent;
import org.hyperledger.aries.config.Options;
import org.hyperledger.aries.models.RequestEnvelope;
import org.hyperledger.aries.models.ResponseEnvelope;
import org.hyperledger.aries.api.Handler;

import java.nio.charset.StandardCharsets;

class MyHandler implements Handler {

    String lastTopic, lastMessage;

    public String getLastNotification() {
        return lastTopic+"\n"+lastMessage;
    }
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @SuppressLint("LongLogTag")
    @Override
    public void handle(String topic, byte[] message) {
        lastTopic = topic;
        lastMessage = new String(message, StandardCharsets.UTF_8);

        Log.d("received notification topic: ", lastTopic);
        Log.d("received notification message: ", lastMessage);
    }
}

public class FirstFragment extends Fragment {

    String url = "", websocketURL = "", retrievedCredentials = "";
    String reqData = "{\n\t\t\"serviceEndpoint\":\"http://alice.agent.example.com:8081\",\n\t\t\"recipientKeys\":[\"FDmegH8upiNquathbHZiGBZKwcudNfNWPeGQFBt8eNNi\"],\n\t\t\"@id\":\"a35c0ac6-4fc3-46af-a072-c1036d036057\",\n\t\t\"label\":\"agent\",\n\t\t\"@type\":\"https://didcomm.org/didexchange/1.0/invitation\"}";
    boolean useLocalAgent;

    AriesController agent;
    MyHandler handler;

    @SuppressLint("LongLogTag")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void setAgent() {
         // create options
        Options opts = new Options();
        opts.setAgentURL(url);
        opts.setUseLocalAgent(useLocalAgent);

        opts.setWebsocketURL(websocketURL);

        // create an aries agent instance
        try {
            agent = Ariesagent.new_(opts);

            // register handler
            handler = new MyHandler();
            String registrationID = agent.registerHandler(handler, "didexchange_states");
            Log.d("handler registration id: ", registrationID);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void getCredentials() {
        if (!useLocalAgent && url.equals("")) {
            TextView credentials = requireView().findViewById(R.id.credentials);
            credentials.setText("A remote agent URL must be provided");
        }
        else {
            ResponseEnvelope res = new ResponseEnvelope();
            try {

                // create a controller
                VerifiableController v = agent.getVerifiableController();

                // perform an operation
                byte[] data = "{}".getBytes(StandardCharsets.UTF_8);
                res = v.getCredentials(new RequestEnvelope(data));
            } catch (Exception e) {
                e.printStackTrace();
            }

            if(res.getError() != null) {
                if(!res.getError().getMessage().equals("")) {
                    System.out.println(res.getError().getMessage());
                }
            }

            retrievedCredentials = new String(res.getPayload(), StandardCharsets.UTF_8);
        }
    }

    @SuppressLint({"SetTextI18n", "LongLogTag"})
    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void receiveInvitation() {
        if (!useLocalAgent && url.equals("") && websocketURL.equals("")) {
            TextView text = requireView().findViewById(R.id.notification_result);
            text.setText("An agent URL and websocket URL must be provided for remote agents");
        }
        else {
            ResponseEnvelope res = new ResponseEnvelope();
            try {

                // call did exchange method
                byte[] data = reqData.getBytes(StandardCharsets.UTF_8);

                RequestEnvelope requestEnvelope = new RequestEnvelope(data);
                DIDExchangeController didex = agent.getDIDExchangeController();
                res = didex.receiveInvitation(requestEnvelope);

                if(res.getError() != null && !res.getError().getMessage().isEmpty()) {
                    Log.d("failed to receive invitation: ", res.getError().getMessage());
                } else {
                    String receiveInvitationResponse = new String(res.getPayload(), StandardCharsets.UTF_8);
                    Log.d("received invitation with: ", receiveInvitationResponse);
                }

            } catch (Exception e) {
                e.printStackTrace();
            }

            if(res.getError() != null) {
                if(!res.getError().getMessage().equals("")) {
                    System.out.println(res.getError().getMessage());
                }
            }

        }
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull final View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        final EditText urlInput = view.findViewById(R.id.agent_url);
        urlInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0)
                    url = s.toString();
            }
        });

        final EditText websocketURLInput = view.findViewById(R.id.websocket_url);
        websocketURLInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0)
                    websocketURL = s.toString();
            }
        });

        final EditText receiveInvitationInput = view.findViewById(R.id.didex_receiveInvitation_req);
        receiveInvitationInput.setText(reqData);
        receiveInvitationInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {}

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.length() != 0)
                    reqData = s.toString();
            }
        });

        view.findViewById(R.id.use_local_agent).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                useLocalAgent = !useLocalAgent;

                int bool = useLocalAgent ? View.INVISIBLE : View.VISIBLE;
                urlInput.setVisibility(bool);

                RadioButton btn = requireView().findViewById(R.id.use_local_agent);
                btn.setChecked(useLocalAgent);

                TextView credentials = requireView().findViewById(R.id.credentials);
                credentials.setText("");

                Button getCredsBtn = (Button) requireView().findViewById(R.id.button_get_credentials);
                getCredsBtn.setEnabled(false);

                TextView notifs = requireView().findViewById(R.id.notification_result);
                notifs.setText("");
            }
        });

        view.findViewById(R.id.button_newAgent).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                setAgent();

                Button getCredsBtn = (Button) requireView().findViewById(R.id.button_get_credentials);
                getCredsBtn.setEnabled(true);

                Button rcvInvitationBtn = (Button) requireView().findViewById(R.id.button_receiveInvitation);
                rcvInvitationBtn.setEnabled(true);
            }
        });

        view.findViewById(R.id.button_get_credentials).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                getCredentials();
                TextView credentials = requireView().findViewById(R.id.credentials);
                credentials.setText(retrievedCredentials);
            }
        });

        view.findViewById(R.id.button_receiveInvitation).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                receiveInvitation();
                TextView notifs = requireView().findViewById(R.id.notification_result);
                notifs.setText(handler.getLastNotification());
            }
        });
    }
}
