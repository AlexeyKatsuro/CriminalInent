package com.dedalexey.criminalintent;

import android.app.Activity;
import android.app.Dialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Created by Alexey on 06.09.2017.
 */

public class PhotoDialogFragment extends DialogFragment {
    private static  final String ARG_PHOTO_PATH = "photoPath";

    ImageView mPhotoView;
    String mPhotoPath;
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        mPhotoPath = getArguments().getString(ARG_PHOTO_PATH);
        //Log.d("TAG - - - - ", mPhotoPath);
        View view = LayoutInflater.from(getActivity()).inflate(R.layout.dialog_photo,null);

        Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoPath,getActivity());
        mPhotoView = (ImageView) view.findViewById(R.id.image_photo);
        mPhotoView.setImageBitmap(bitmap);

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .create();
    }

    public static PhotoDialogFragment newInstanse(String photoPath){
        Bundle bundle = new Bundle();
        bundle.putString(ARG_PHOTO_PATH,photoPath);
        PhotoDialogFragment fragment = new PhotoDialogFragment();
        fragment.setArguments(bundle);
        return fragment;
    }
}
