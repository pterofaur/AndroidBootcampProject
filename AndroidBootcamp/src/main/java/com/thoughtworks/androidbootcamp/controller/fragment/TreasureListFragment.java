package com.thoughtworks.androidbootcamp.controller.fragment;


import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.location.Location;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import com.thoughtworks.androidbootcamp.R;
import com.thoughtworks.androidbootcamp.controller.adapter.TreasureListAdapter;
import com.thoughtworks.androidbootcamp.util.FileUtils;
import com.thoughtworks.androidbootcamp.util.TreasureLoader;

import java.io.File;
import java.io.IOException;

import static java.lang.String.format;

/**
 * A simple {@link android.support.v4.app.Fragment} subclass.
 *
 */
public class TreasureListFragment extends Fragment {

    private static final String TAG = "TreasureListFragment";
    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;

    TreasureLoader treasureLoader;

    private String mCurrentPhotoPath;
    private String mSelectedTreasurePath;

    private TreasureListAdapter mTreasureListAdapter;

    public TreasureListFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        treasureLoader = new TreasureLoader(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_treasure_list, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        GridView gridView = (GridView) view.findViewById(R.id.treasure_list);
        mTreasureListAdapter = new TreasureListAdapter(getActivity(), treasureLoader.getSampleImagePaths());
        gridView.setAdapter(mTreasureListAdapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                mSelectedTreasurePath = (String) mTreasureListAdapter.getItem(position);
                takePhoto();
            }
        });

    }

    public void takePhoto() {
        // Create a new intent for taking a photo
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // Create the File where the photo should go
        try {
            File photoFile = FileUtils.getExternalPublicFile(Environment.DIRECTORY_PICTURES,
                    getString(R.string.app_name));

            // store the path so we know where it is later
            mCurrentPhotoPath = photoFile.getAbsolutePath();

            // tell camera app when to put the photo
            intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(photoFile));

            if(intent.resolveActivity(getActivity().getPackageManager()) != null) {
                // ask to use the camera
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }

        } catch (IOException ex) {
            Log.e(TAG, "Error opening file", ex);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // check that its the right result and that it was successful
        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            // Retrieve geocode info from the taken photo and treasure
            final Location photoLocation = getLocationForImage(mCurrentPhotoPath);
            final Location treasureLocation = getLocationForImage(mSelectedTreasurePath);

            // calculate distance between points
            final float distance = treasureLocation.distanceTo(photoLocation);

            Toast.makeText(getActivity(), format("Your photo is %s metres from treasure", distance),
                    Toast.LENGTH_LONG).show();
        }
    }

    private Location getLocationForImage(String imagePath) {
        try {
            // Note: genymotion doesnt seem to want to add the appropriate location tags to the photo
            // to test this you need to use a real device
            ExifInterface exifInterface = new ExifInterface(new File(imagePath).getCanonicalPath());
            float latlng [] = new float[2];
            exifInterface.getLatLong(latlng);

            Location location = new Location(imagePath);
            location.setLatitude(latlng[0]);
            location.setLongitude(latlng[1]);

            return location;
        } catch (IOException e) {
            Log.e(TAG, "Unable to retrieve exif tags from image", e);
            return null;
        }
    }
}
