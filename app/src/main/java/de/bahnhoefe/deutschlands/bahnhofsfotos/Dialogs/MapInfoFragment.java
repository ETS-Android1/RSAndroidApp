package de.bahnhoefe.deutschlands.bahnhofsfotos.Dialogs;

import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import androidx.appcompat.app.AlertDialog;
import android.text.method.LinkMovementMethod;
import android.view.ContextThemeWrapper;
import android.widget.TextView;

import de.bahnhoefe.deutschlands.bahnhofsfotos.R;

public class MapInfoFragment extends DialogFragment {


    @Override
    @NonNull
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        // Use the Builder class for convenient dialog construction
        final AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(getActivity(), R.style.AlertDialogCustom));

        final TextView textView = new TextView(getContext());
        textView.setTextSize((float) 18);
        textView.setPadding(50, 50, 50, 50);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setText(R.string.map_info_text);
        textView.setLinkTextColor(Color.parseColor("#c71c4d"));

        builder.setIcon(R.mipmap.ic_launcher)
                .setTitle(R.string.map_info_title)
                .setPositiveButton(R.string.app_info_ok, new DialogInterface.OnClickListener() {
                    public void onClick(final DialogInterface dialog, final int id) {
                    }
                });


        builder.setView(textView);

        // Creates the AlertDialog object and return it
        return builder.create();
    }


}
