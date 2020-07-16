/*
 * Copyright SecureKey Technologies Inc. All Rights Reserved.
 * SPDX-License-Identifier: Apache-2.0
 */

package com.github.trustbloc.ariesdemo;

import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.RequiresApi;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.dandan.jsonhandleview.library.JsonViewLayout;

import org.hyperledger.aries.api.AriesController;
import org.hyperledger.aries.api.VerifiableController;
import org.hyperledger.aries.ariesagent.Ariesagent;
import org.hyperledger.aries.config.Options;
import org.hyperledger.aries.models.RequestEnvelope;
import org.hyperledger.aries.models.ResponseEnvelope;

import java.nio.charset.StandardCharsets;

public class FirstFragment extends Fragment {

    String url;
    AriesController agent;

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public void setAgent() {
        // create options
        Options opts = new Options();
        opts.setURL(url);
        opts.setUseLocalAgent(false);

        // create an aries agent instance
        try {
            agent = Ariesagent.newAriesAgent(opts);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public String getCredentials() {

        ResponseEnvelope res = new ResponseEnvelope();
        try {

            // create a controller
            VerifiableController v = agent.getVerifiableController();

            // perform an operation
            res = v.getCredentials(new RequestEnvelope());
        } catch (Exception e) {
            e.printStackTrace();
        }

        if(res.getError() != null) {
            if(!res.getError().getMessage().equals("")) {
                System.out.println(res.getError().getMessage());
            }
        }
        return new String(res.getPayload(), StandardCharsets.UTF_8);
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_first, container, false);
    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText et = view.findViewById(R.id.agent_url);
        et.addTextChangedListener(new TextWatcher() {

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

        view.findViewById(R.id.button_newAgent).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                setAgent();
            }
        });

        view.findViewById(R.id.button_first).setOnClickListener(new View.OnClickListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onClick(View view) {
                JsonViewLayout jsonViewLayout = getView().findViewById(R.id.jsonView);
                jsonViewLayout.bindJson(getCredentials());
            }
        });
    }
}