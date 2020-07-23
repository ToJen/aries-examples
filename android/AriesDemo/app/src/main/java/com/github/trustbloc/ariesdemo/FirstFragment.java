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
import org.hyperledger.aries.ariesagent.Ariesagent;
import org.hyperledger.aries.config.Options;
import org.hyperledger.aries.models.RequestEnvelope;
import org.hyperledger.aries.models.ResponseEnvelope;

import java.nio.charset.StandardCharsets;

public class FirstFragment extends Fragment {

    String url = "", retrievedCredentials = "";
    AriesController agent;
    boolean useLocalAgent;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void setAgent() {
        // create options
        Options opts = new Options();
        opts.setAgentURL(url);
        opts.setUseLocalAgent(useLocalAgent);

        // create an aries agent instance
        try {
            agent = Ariesagent.new_(opts);
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
            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
                if(s.length() != 0)
                    url = s.toString();
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
            }
        });

        view.findViewById(R.id.button_newAgent).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                setAgent();

                Button getCredsBtn = (Button) requireView().findViewById(R.id.button_get_credentials);
                getCredsBtn.setEnabled(true);
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
    }
}