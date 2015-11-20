package com.tutsplus.code.appinvites;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.appinvite.AppInvite;
import com.google.android.gms.appinvite.AppInviteInvitation;
import com.google.android.gms.appinvite.AppInviteInvitationResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.ResultCallback;

public class MainActivity extends AppCompatActivity {

    private static final
        String INVITATION_TITLE = "Call your friends",
            INVITATION_MESSAGE = "Hey! Would you like to get a 50% discount for this awesome app? :)",
            INVITATION_CALL_TO_ACTION = "Share";

    private static final int REQUEST_INVITE = 0;
    private View button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = findViewById(R.id.invite);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new AppInviteInvitation.IntentBuilder(INVITATION_TITLE)
                        .setMessage(INVITATION_MESSAGE)
                        .setDeepLink(Uri.parse("tutsplus://code.coupon/50"))
                        .setCallToActionText(INVITATION_CALL_TO_ACTION)
                        .build();
                startActivityForResult(intent, REQUEST_INVITE);
            }
        });

        GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(AppInvite.API)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(ConnectionResult connectionResult) {
                        Log.d(getString(R.string.app_name), "onConnectionFailed:" + connectionResult);
                        showMessage("Sorry, the connection has failed.");
                    }
                })
                .build();

        AppInvite.AppInviteApi.getInvitation(googleApiClient, this, true)
                .setResultCallback(
                    new ResultCallback<AppInviteInvitationResult>() {
                        @Override
                        public void onResult(AppInviteInvitationResult result) {
                            Log.d(getString(R.string.app_name), "Invitation accepted: " + result.getStatus());
                        }
                    });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_INVITE) {
            if (resultCode == RESULT_OK) {
                // You successfully sent the invite,
                // we can dismiss the button.
                button.setVisibility(View.GONE);

                // Quoting Google: "The ids array contains the unique invitation ids for each invitation sent
                // (one for each contact select by the user). You can use these for analytics
                // as the ID will be consistent on the sending and receiving devices."
                String[] ids = AppInviteInvitation.getInvitationIds(resultCode, data);
                StringBuilder sb = new StringBuilder();
                sb.append("Sent ").append(Integer.toString(ids.length)).append(" invitations: ");
                for (String id : ids) sb.append("[").append(id).append("]");
                Log.d(getString(R.string.app_name), sb.toString());
            } else {
                // Sending failed or it was canceled by clicking on back button
                showMessage("Sorry, I wasn't able to send the invites");
            }
        }
    }

    private void showMessage(String message) {
        ViewGroup container = (ViewGroup) findViewById(R.id.container);
        Snackbar.make(container, message, Snackbar.LENGTH_LONG).show();
    }

}
