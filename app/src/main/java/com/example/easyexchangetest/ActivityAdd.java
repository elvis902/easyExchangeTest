package com.example.easyexchangetest;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.io.InputStream;
import java.util.Random;

//@Comment: This activity is used to add new Adds by the user

public class ActivityAdd extends AppCompatActivity {

    //All required variables
    EditText productName, productDescription;
    Button browseBtn, addBtn;
    ImageView img;

    //@Comment: Uri for the image from device
    Uri filepath;
    Bitmap bitmap;
    String email;
    FirebaseAuth mAuth;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        img=(ImageView)findViewById(R.id.imageView);
        browseBtn=(Button)findViewById(R.id.browse_btn);
        addBtn=(Button)findViewById(R.id.add_btn);

        mAuth = FirebaseAuth.getInstance();
        email = mAuth.getCurrentUser().getEmail();


                    /*@Comment: Set the browse button which allows user to select image from external storage
                   Dexter Dependency is used to manage permission for accessing device storage image easily.*/
                    browseBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            //@Comment: Dexer is helping with permission to select an image from External Storage
                            Dexter.withActivity(ActivityAdd.this)
                                    .withPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
                                    .withListener(new PermissionListener() {
                                        @Override
                                        public void onPermissionGranted(PermissionGrantedResponse response) {
                                            Intent intent = new Intent(Intent.ACTION_PICK);
                                            intent.setType("image/*");
                                            startActivityForResult(Intent.createChooser(intent,"Select Image File"),1);
                                        }

                                        @Override
                                        public void onPermissionDenied(PermissionDeniedResponse response) {

                                        }

                                        @Override
                                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {

                                            token.continuePermissionRequest();
                                        }
                                    }).check();
                        }
                    });


                    //@Comment: Set the add button which allows user to upload the Selected image to Firebase Storage
                    addBtn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            //@Comment: Custom function to upload the image to firebase storage
                            uploadtofirebase();
                        }
                    });

    }

    /*@Comment: Catching the intent produced by browse btn to select image from device using Dexter
    Data is received as Uri, which is changed to bitstream -> bitmap -> imageview and img is set in the UI*/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if(requestCode == 1 && resultCode == RESULT_OK)
        {
            filepath = data.getData();
            try {

                InputStream inputStream = getContentResolver().openInputStream(filepath);
                bitmap = BitmapFactory.decodeStream(inputStream);
                img.setImageBitmap(bitmap);

            }catch (Exception e){

            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }



    /*@Comment: Code to upload Image to Firebase using Uri
    //The Uri required was already found using Dexter above*/
    private void uploadtofirebase() {

        //@Comment: Dialog box used to show the Progress of upload to Users
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("File Uploader");
        dialog.show();

        productName = (EditText)findViewById(R.id.pName_et);
        productDescription = (EditText)findViewById(R.id.pDes_et);

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference uploader = storage.getReference("image "+new Random().nextInt(50));

        uploader.putFile(filepath)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        dialog.dismiss();

                        /*Code to get the url of image uploaded to Firenbase Storage.
                        Dataholder obj(which contains the other info of adds) along with url is send to Firebase Database, under the child "Adds"*/
                        uploader.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                FirebaseDatabase db = FirebaseDatabase.getInstance();
                                DatabaseReference root = db.getReference().child("Adds");

                                AddDataHolder obj = new AddDataHolder(productName.getText().toString(), productDescription.getText().toString(),email,
                                        uri.toString());
                                //@Comment: Dataholder obj(which contains the other info of adds) along with url is send to Firebase Database, under the child "Adds"
                                root.push().setValue(obj);

                                //@Comment: All the field are reset
                                productName.setText("");
                                productDescription.setText("");
                                img.setImageResource(R.drawable.ic_launcher_background);
                                Toast.makeText(getApplicationContext(),"File Uploaded Successfully",Toast.LENGTH_LONG).show();

                                // **It is not possible to create intent and add items to database in the same time. Have to see on it.
                               //startActivity(new Intent(ActivityAdd.this, ActivityDashboard.class));
                            }
                        });
                    }
                })
                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {

                        //@Comment: Things that we would like to show to user during the upload of image to fireabse storeage
                        long percentage = (100*snapshot.getBytesTransferred())/snapshot.getTotalByteCount();
                        dialog.setMessage("Uploaded" + (int)percentage+" %");
                    }
                });
    }
}