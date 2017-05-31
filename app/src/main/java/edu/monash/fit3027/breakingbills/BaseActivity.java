package edu.monash.fit3027.breakingbills;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import static android.support.design.widget.Snackbar.LENGTH_SHORT;

/**
 * A base activity containing logic for progress and message dialogs.
 *
 * Reference:
 *  1. https://github.com/firebase/quickstart-android for the idea of a base activity
 *  2. https://developer.android.com/guide/topics/ui/dialogs.html for alert dialogs
 *
 * Created by Callistus on 29/4/2017.
 */

public class BaseActivity extends AppCompatActivity {

    private ProgressDialog mProgressDialog;

    /**
     * A helper method to show a loading animation with an informative message
     * @param message
     */
    public void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.setMessage(message + "...");

        mProgressDialog.show();
    }

    /**
     * Method overloading for default message to be Loading...
     */
    public void showProgressDialog() {
        showProgressDialog("Loading");
    }

    /**
     * A helper method to hide the progress dialog on the activity if it is being shown.
     */
    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    /**
     * A helper method to get the current user's Uid
     * @return a String representing the current user's Uid
     */
    public String getCurrentUserUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    /**
     * A helper method to show a snackbar with a message
     * @param view
     * @param message
     */
    public void showSnackbar(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, LENGTH_SHORT);
        snackbar.show();
    }

    /**
     * A helper method to show a simple message dialog with a positive and negative button
     * @param resource
     * @param title
     * @param message
     * @param positive
     * @param negative
     */
    public void showMessageDialog(int resource, String title, String message, String positive, String negative) {
        // init the alert dialog builder
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // set the layout for the alert dialog
        LayoutInflater inflater = getLayoutInflater();
        final View dialogView = inflater.inflate(resource, null);

        // init text views
        TextView normal_dialog_title = (TextView) dialogView.findViewById(R.id.message_dialog_title);
        TextView normal_dialog_message = (TextView) dialogView.findViewById(R.id.message_dialog_message);

        normal_dialog_title.setText(title);
        normal_dialog_message.setText(message);

        // set the positive and negative buttons' onclicks
        builder.setView(dialogView)
                .setPositiveButton(positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

        if (!negative.equals(""))
            builder.setView(dialogView)
                    .setNegativeButton(negative, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                        }
                    });

        // create and show this dialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
