package edu.tmeyer.ft_hangouts;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.DialogInterface;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.snackbar.Snackbar;

import edu.tmeyer.ft_hangouts.activity.PermissionsUtils;

public class SMSDialog implements View.OnClickListener, DialogInterface.OnClickListener {

    private final Activity activity;
    private final String toNum;

    public SMSDialog(Activity activity, String toNum) {
        this.activity = activity;
        this.toNum = toNum;
    }

    @Override
    public void onClick(View v) {
        click();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        click();
    }

    @SuppressLint("ClickableViewAccessibility")
    private void click() {
        MaterialAlertDialogBuilder messageDialog = new MaterialAlertDialogBuilder(this.activity);
        View dialogView = this.activity.getLayoutInflater().inflate(R.layout.message_dialog, null);
        EditText message = (EditText) dialogView.findViewById(R.id.dialog_message);

        message.setOnTouchListener((_view, _event) -> {
            if (_view.getId() == R.id.dialog_message) {
                _view.getParent().requestDisallowInterceptTouchEvent(true);
                switch (_event.getAction() & MotionEvent.ACTION_MASK) {
                    case MotionEvent.ACTION_UP:
                        _view.getParent().requestDisallowInterceptTouchEvent(false);
                        break;
                }
            }
            return false;
        });
        messageDialog
                .setTitle(R.string.send_a_message)
                .setView(dialogView)
                .setCancelable(true)
                .setPositiveButton(R.string.send, (dialog, which) -> {
                    String toSend = message.getText().toString();

                    if (!PermissionsUtils.hasPermission(this.activity, Manifest.permission.SEND_SMS)) {
                        PermissionsUtils.requestPermissions(this.activity, new String[]{Manifest.permission.SEND_SMS}, 0);
                        if (!PermissionsUtils.hasPermission(this.activity, Manifest.permission.SEND_SMS)) {
                            Snackbar.make(this.activity.findViewById(R.id.layout), R.string.need_sms_permission, Snackbar.LENGTH_LONG).show();
                            return;
                        }
                    }
                    if (SMSHandler.sendSMS(this.toNum, toSend)) {
                        Snackbar.make(this.activity.findViewById(R.id.layout), R.string.success_text, Snackbar.LENGTH_SHORT).show();
                    } else {
                        Snackbar.make(this.activity.findViewById(R.id.layout), R.string.fail_text, Snackbar.LENGTH_LONG).show();
                    }
                })
                .show();
    }
}
