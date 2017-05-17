package edu.monash.fit3027.breakingbills;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;

import static android.support.design.widget.Snackbar.LENGTH_SHORT;


public class BaseActivity extends AppCompatActivity {

    private ProgressDialog mProgressDialog;

    public void showProgressDialog() {
        showProgressDialog("Loading");
    }

    public void showProgressDialog(String message) {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setCancelable(false);
        }
        mProgressDialog.setMessage(message + "...");

        mProgressDialog.show();
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }

    public String getCurrentUserUid() {
        return FirebaseAuth.getInstance().getCurrentUser().getUid();
    }

    public void showSnackbar(View view, String message) {
        Snackbar snackbar = Snackbar.make(view, message, LENGTH_SHORT);
        snackbar.show();
    }

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
