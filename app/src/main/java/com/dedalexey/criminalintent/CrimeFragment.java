package com.dedalexey.criminalintent;


import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ShareCompat.IntentBuilder;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;

import java.io.File;
import java.util.Date;
import java.util.UUID;


/**
 * A simple {@link Fragment} subclass.
 */
public class CrimeFragment extends Fragment {

    public static final String TAG = "CrimeFragment";

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String EXTRA_INDEX_FOR_NOTIFY = "index_for_notify";
    private static final String DIALOG_DATE = "DialogDate";
    private static final String DIALOG_PHOTO = "DialogPHOTO";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_PHOTO = 2;

    private Crime mCrime;
    private File mPhotoFile;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckBox;
    private Button mReportButton;
    private Button mSuspectButton;
    private ImageButton mCallImageButton;

    private ImageButton mPhotoView;
    private ImageButton mPhotoButton;

    private CallBacks mCallBacks;
    public interface CallBacks{
        void onCrimeUpdate(Crime crime);
    }

    public static CrimeFragment newInstance(UUID crimeId){
        Bundle agrs = new Bundle();
        agrs.putSerializable(ARG_CRIME_ID,crimeId);
        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(agrs);
        return fragment;
    }

    public CrimeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if(activity instanceof CallBacks) {
            mCallBacks = (CallBacks) activity;
        }

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        setHasOptionsMenu(true);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_crime,menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_delete_crime:
            {
                CrimeLab.get(getActivity()).deleteCrime(mCrime);
                getActivity().finish();
                return true;
            }

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_crime, container, false);
        mTitleField = view.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                mCrime.setTitle(charSequence.toString());
                updateCrime();
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });


        mDateButton = (Button)view.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment.newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this,REQUEST_DATE);
                dialog.show(manager,DIALOG_DATE);
            }
        });

        mSolvedCheckBox = (CheckBox) view.findViewById(R.id.crime_solved);
        mSolvedCheckBox.setChecked(mCrime.isSolved());
        mSolvedCheckBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                mCrime.setSolved(b);
                updateCrime();
            }
        });

        mReportButton = (Button) view.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT,getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT, getString(R.string.crime_report_subject));
                i = Intent.createChooser(i,getString(R.string.send_report));
                //startActivity(i);

               IntentBuilder.from(getActivity())
                       .setChooserTitle(R.string.send_report)
                       .setType("text/plain")
                       .setText(getCrimeReport())
                       .setSubject(getString(R.string.crime_report_subject))
                       .startChooser();


            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = (Button) view.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(pickContact,REQUEST_CONTACT);
            }
        });


        mCallImageButton = (ImageButton) view.findViewById(R.id.call_image_button);
        mCallImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //Toast.makeText(getActivity(),mCrime.getSuspect().getPhoneNumber(),Toast.LENGTH_SHORT).show();
                Uri number = Uri.parse("tel:"+mCrime.getSuspect().getPhoneNumber());
                Intent callIntent = new Intent(Intent.ACTION_CALL,number);
                startActivity(callIntent);
            }
        });
        if(mCrime.getSuspect().getName() != null){
            mSuspectButton.setText(mCrime.getSuspect().getName());
        }
        updateCallButton();

        PackageManager packageManager = getActivity().getPackageManager();
        if(packageManager.resolveActivity(pickContact,PackageManager.MATCH_DEFAULT_ONLY) == null){
            mSuspectButton.setEnabled(false);
        }


        mPhotoButton = (ImageButton) view.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent
                (MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);
        if (canTakePhoto) {
            Log.d("------", "canTakePhoto");
            Uri uri = Uri.fromFile(mPhotoFile);
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
        }
        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
        });
        mPhotoView = (ImageButton) view.findViewById(R.id.crime_photo);
        mPhotoView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentManager manager = getFragmentManager();
                PhotoDialogFragment dialogFragment = PhotoDialogFragment.newInstanse(mPhotoFile.getPath());
                dialogFragment.show(manager,DIALOG_PHOTO);
            }
        });
        updatePhotoView();
        return view;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mCallBacks = null;
    }

    private void updateCallButton() {
        if(mCrime.getSuspect().mPhoneNumber != null){
            mCallImageButton.setVisibility(View.VISIBLE);
        } else {
            mCallImageButton.setVisibility(View.GONE);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        CrimeLab.get(getActivity()).updateCrime(mCrime);
    }
    @Override
    public void onResume() {
        super.onResume();
        updateCallButton();
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        returnResult();
    }


    public void returnResult() {
        Intent data = new Intent();
        int index = CrimeLab.get(getActivity()).getCrimes()
                .indexOf(mCrime);
        Log.d(TAG, " Index = " + index);
        data.putExtra(EXTRA_INDEX_FOR_NOTIFY,index);
        getActivity().setResult(Activity.RESULT_OK,data);
    }

    public static int getIndexForNotify(Intent data){
       return data.getIntExtra(EXTRA_INDEX_FOR_NOTIFY,0);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(resultCode !=Activity.RESULT_OK){
            return;
        }

        if(requestCode == REQUEST_DATE){
            Date date = (Date) data.getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateCrime();
            updateDate();
        } else  if (requestCode == REQUEST_CONTACT && data != null){

            Uri contactUri = data.getData();
            String[] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME, ContactsContract.Contacts._ID
            };

            Cursor cursor = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null, null, null);


            try {
                 if(cursor.getCount() == 0){
                     return;
                 }

                 cursor.moveToFirst();

                 String suspectName = cursor.getString(0);
                 String susoectID= cursor.getString(1);


                String[] queryFields1 = new String[]{CommonDataKinds.Phone.NUMBER};
                Cursor phone = getActivity().getContentResolver()
                        .query(CommonDataKinds.Phone.CONTENT_URI, queryFields1, CommonDataKinds.Phone.CONTACT_ID + " = " + susoectID, null, null);
                try {
                    if (phone.getCount() == 0) {
                        return;
                    }

                    phone.moveToFirst();
                    String suspectNubmer = phone.getString(0);
                    Log.d("---------------", suspectName + " " + susoectID + " " + suspectNubmer);
                    mCrime.setSuspect(new Suspect(suspectName,suspectNubmer));
                    updateCrime();
                    mSuspectButton.setText(mCrime.getSuspect().getName());
                } finally {
                    phone.close();
                }
             } finally {
                 cursor.close();

             }

        } else if (requestCode == REQUEST_PHOTO){
            updateCrime();
            updatePhotoView();
        }
    }


    private void updateDate() {
        String dateFormat = "E, MMM d, yyyy";
        mDateButton.setText(DateFormat.format(dateFormat, mCrime.getDate()).toString());
    }

    private void updatePhotoView(){
        if(mPhotoFile ==null || !mPhotoFile.exists()){
            mPhotoView.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(mPhotoFile.getPath(),getActivity());
            mPhotoView.setImageBitmap(bitmap);
        }
    }

    private String getCrimeReport(){
        String solvedString = null;
        if(mCrime.isSolved()){
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }

        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();

        String suspect = mCrime.getSuspect().getName();
        if(suspect == null){
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect,suspect);
        }
        String report = getString(R.string.crime_report, mCrime.getTitle(),dateString,solvedString,suspect);

        return report;
    }

    private void updateCrime(){
        CrimeLab.get(getActivity()).updateCrime(mCrime);
        mCallBacks.onCrimeUpdate(mCrime);
    }
}
