package edu.commonwealthu.hw3_mchenry;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

/**
 * Helper class for Main Activity for displaying custom dialogs. This is a slightly
 * modified version of the DialogHelper class originally written by Dr. Drue Coles.
 *
 * @author Drue Coles
 * @author Ryan McHenry
 */
public class DialogHelper {

    /**
     * Shows a confirmation dialog with a generic layout.
     *
     * @param context context in which the dialog should be shown
     * @param message text to be displayed
     */
    public static void showInformationDialog(Context context, String message) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_generic, null);

        TextView messageTextView = dialogView.findViewById(R.id.dialog_generic_text_view);
        messageTextView.setText(message);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView)
                .setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.color.toolbar_background);
        }

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
    }

    /**
     * Shows a confirmation dialog with a given layout.
     * This method is unused by hw3 but I figured I'd keep it just in case.
     *
     * @param context    context in which the dialog should be shown
     * @param dialogView layout for the dialog
     */
    public static void showInformationDialog(Context context, View dialogView) {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView)
                .setPositiveButton(android.R.string.ok, null);
        AlertDialog dialog = builder.create();
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.color.toolbar_background);
        }

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
    }

    /**
     * Shows a dialog with customizable layout and actions for Yes and No buttons.
     *
     * @param context   context in which the dialog should be shown
     * @param message   text to be displayed
     * @param yesAction action to perform if the user clicks Yes
     * @param noAction  action to perform if the user clicks No
     */
    public static void showConfirmationDialog(Context context, String message,
                                              DialogInterface.OnClickListener yesAction,
                                              DialogInterface.OnClickListener noAction) {
        LayoutInflater inflater = LayoutInflater.from(context);
        View dialogView = inflater.inflate(R.layout.dialog_generic, null);

        TextView messageTextView = dialogView.findViewById(R.id.dialog_generic_text_view);
        messageTextView.setText(message);

        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setView(dialogView)
                .setPositiveButton(android.R.string.ok, yesAction)
                .setNegativeButton(android.R.string.no, noAction);

        AlertDialog dialog = builder.create();
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawableResource(R.color.toolbar_background);
        }

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(Color.WHITE);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(Color.WHITE);
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setText(R.string.button_positive);
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setText(R.string.button_negative);
    }
}