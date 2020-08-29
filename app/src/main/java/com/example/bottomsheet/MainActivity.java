package com.example.bottomsheet;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;
import java.io.IOException;


public class MainActivity extends AppCompatActivity {  //main activity



    private LinearLayout camera,gallery;
    private Uri imageUri,cameraImgUri,downloadUrl;
    private TextView ImageUser;
    private String currentPhotoPath;
    private ImageView userFullProfile ;
    private ImageView userProfile1,showImage;
    private StorageReference mStorageRef;
    private FirebaseDatabase database;
    private DatabaseReference myRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Firebase Storage Reference
         database = FirebaseDatabase.getInstance();
         myRef = database.getReference("ImageUrl");
         ImageUser=findViewById(R.id.profile_image);
         showImage = findViewById(R.id.profileUser);

         //----------------------------------------------------------------------------------------

        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.

                downloadUrl = Uri.parse(dataSnapshot.child("url").getValue(String.class));
                userProfile1 = findViewById(R.id.profileUser);

                Glide
                        .with(MainActivity.this)
                        .load(downloadUrl)
                        .into(userProfile1);

            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Toast.makeText(MainActivity.this, "Unable to fetch data from firebase", Toast.LENGTH_SHORT).show();
                }
        });

       //-------------------------------------------------------------------------

        showImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                displayUserProfile();
            }
        });

        //-------------------------------------------------------------------------

        //Permission for camera

        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){

                ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{
                            Manifest.permission.CAMERA
                    },
                    100);
        }


        //---------------------------------------------------------------------------

        //Task for update image

        ImageUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showBottomSheet();
               }  //onClick of button
        });

        //-----------------------------------------------------------------------------------------


    }  //OnCreate Method

    //***************************************OUTSIDE OF ON CREATE METHOD***********************************

    //---------------------------------------------------------------------------------------------

    //Method for displaying profile

    public void displayUserProfile() {


        Dialog dialog = new Dialog(MainActivity.this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.showprofile);
        dialog.show();

        userFullProfile = dialog.findViewById(R.id.userFullProfileImage);

        Glide
                .with(this)
                .load(downloadUrl)
                .into(userFullProfile);


    }

    //----------------------------------------------------------------------------------------------

    //Method to show bottom sheet

    public void showBottomSheet(){


        //Showing Bottom sheet Dialogue

        final BottomSheetDialog bottomSheetDialog= new BottomSheetDialog(MainActivity.this,R.style.bottomTheme);

        View bottomSheetView = LayoutInflater.from(getApplicationContext())
                .inflate(

                        R.layout.bottom_sheet,
                        (LinearLayout) findViewById(R.id.bottomContainer)
                );

        //Hooks of Bottom sheet

        camera = bottomSheetView.findViewById(R.id.cameraUpload);
        gallery = bottomSheetView.findViewById(R.id.gallery);

        bottomSheetDialog.setContentView(bottomSheetView);
        bottomSheetDialog.show();  //Show method will show bottom sheet on Activity

                 /*

                When user want to capture image and upload.

                User will click on this layout and this task will perform

                 */
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                cameraPhoto(); //calling method will take picture from camera
                bottomSheetDialog.dismiss();  //for hiding the bottom sheet

            }
        });


                /*

                When user want to upload image from
                gallery.

                User will click on this layout and this task will perform

                 */

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                choosePicture(); //calling method

                bottomSheetDialog.dismiss();  //for hiding the bottom sheet

            }

        });
    }   //BottomSheet


    //----------------------------------------------------------------------------------------------

    //Method for taking picture from camera

    public void cameraPhoto(){

        String fileName = "photo";

        File storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES);

        try {
            File imageFile= File.createTempFile(fileName,".jpeg",storageDirectory);
            currentPhotoPath = imageFile.getAbsolutePath();
            Uri imgUri = FileProvider.getUriForFile(MainActivity.this,"com.example.bottomsheet.fileprovider",imageFile);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT,imgUri);
            startActivityForResult(intent, 100);

        } catch (IOException e) {
            e.printStackTrace();
        }

    } //Camera Photo

    //-------------------------------------------------------------------------------------------

    //Method for choosing the picture from gallery


    public void choosePicture() {

        Intent intent = new Intent();
        intent.setType("image/*");  //inserting all images inside this Image folder
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, 1);

    } //Choose Picture

    //--------------------------------------------------------------------------------------------

//When User will inside the gallery folder

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==100 ){

           cameraImgUri=(Uri.fromFile(new File(currentPhotoPath)));

            CropImage.activity(cameraImgUri)  //cropping the image

                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                    .setFixAspectRatio(true)
                    .start(this);

        }

        else
        if (requestCode == 1  && resultCode == RESULT_OK && data != null && data.getData() != null) {

            //Getting the uri from gallery

            imageUri = data.getData();



            CropImage.activity(imageUri)  //cropping the image

                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setCropShape(CropImageView.CropShape.RECTANGLE)
                    .setFixAspectRatio(true)
                    .start(this);
        }

        //After image will crop again taking the image uri

            if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                CropImage.ActivityResult result = CropImage.getActivityResult(data);
                if (resultCode == RESULT_OK) {
                    Uri resultUri = result.getUri();


                    userProfile1.setImageURI(Uri.parse(""));
                    userFullProfile.setImageURI(Uri.parse(""));
                    UpdateImage(resultUri);

                } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                    Exception error = result.getError();
                    Toast.makeText(this, "Error While Getting uri", Toast.LENGTH_SHORT).show();
                }
            }


    }  //method

//--------------------------------------------------------------------------------------------------

    //Update Image

    private  void UpdateImage(Uri profileUri) {

        //Firebase Storage Reference

        database = FirebaseDatabase.getInstance();
        myRef = database.getReference("ImageUrl");
        mStorageRef = FirebaseStorage.getInstance().getReference("images/");
        final StorageReference riversRef = mStorageRef.child("Anurag123@");

        riversRef.putFile(profileUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {

                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        // Get a URL to the uploaded content
                        mStorageRef.child("Anurag123@").getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {

                                //Setting the url to database

                                myRef.child("url").setValue(uri.toString());
                                Toast.makeText(MainActivity.this, "Image Updated Successfully", Toast.LENGTH_SHORT).show();

                            }

                        });
                    }

                });

    }  //UpdateImage

}  //class