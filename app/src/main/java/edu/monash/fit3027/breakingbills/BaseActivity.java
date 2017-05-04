package edu.monash.fit3027.breakingbills;

import android.app.ProgressDialog;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

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
}
