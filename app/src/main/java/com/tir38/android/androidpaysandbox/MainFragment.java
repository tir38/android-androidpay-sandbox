package com.tir38.android.androidpaysandbox;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.BooleanResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wallet.Wallet;

public class MainFragment extends Fragment {

    private Button mStartButton;
    private GoogleApiClient mGoogleApiClient;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);
        mStartButton = (Button) view.findViewById(R.id.fragment_main_start_button);
        mStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = WalletActivity.newIntent(getContext());
                startActivity(intent);
            }
        });
        mStartButton.setEnabled(false);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        setupGoogleApiClient();
        mGoogleApiClient.connect();
        checkStatusOfAndroidPay();
    }

    @Override
    public void onStop() {
        super.onStop();

        // TODO when do I want to disconnect?
//        if (mGoogleApiClient != null) {
//            mGoogleApiClient.disconnect();
//        }
    }

    private void checkStatusOfAndroidPay() {
        // TODO show progress dialog?

        Wallet.Payments.isReadyToPay(mGoogleApiClient)
                .setResultCallback(new ResultCallback<BooleanResult>() {
                    @Override
                    public void onResult(@NonNull BooleanResult booleanResult) {

                        // TODO hide progress dialog?

                        if (booleanResult.getStatus().isSuccess()) {
                            if (booleanResult.getValue()) {
                                initAndroidPay();
                            } else {
                                initSomeOtherPaymentMethod();
                            }
                        } else {
                            somethingWentWrongTryingToDetermineIfPayIsReady();
                        }
                    }
                });
    }

    private void setupGoogleApiClient() {

        GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {

            @Override
            public void onConnected(@Nullable Bundle bundle) {
                Toast.makeText(getContext(), "GoogleApiClient connected", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onConnectionSuspended(int i) {
                Toast.makeText(getContext(), "GoogleApiClient connection suspended", Toast.LENGTH_SHORT).show();
            }
        };

        GoogleApiClient.OnConnectionFailedListener onConnectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
            @Override
            public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                // TODO handle lack of Google Play Services here
                Toast.makeText(getContext(), "GoogleApiClient connection failed: " + connectionResult.getErrorMessage(), Toast.LENGTH_SHORT).show();
            }
        };

        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addApi(Wallet.API, new Wallet.WalletOptions.Builder().build())
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(onConnectionFailedListener)
                .build();
    }

    private void somethingWentWrongTryingToDetermineIfPayIsReady() {
        Toast.makeText(getContext(), "Something went wrong checking status of Android Pay", Toast.LENGTH_SHORT).show();
        mStartButton.setEnabled(false);
    }

    private void initSomeOtherPaymentMethod() {
        Toast.makeText(getContext(), "Android Pay is not available. Install Android Pay app or navigate to Android Pay to setup your first card.", Toast.LENGTH_SHORT).show();
        mStartButton.setEnabled(false);
    }

    private void initAndroidPay() {
        Toast.makeText(getContext(), "Android Pay is ready", Toast.LENGTH_SHORT).show();
        mStartButton.setEnabled(true);
    }
}

