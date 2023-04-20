package com.pupu.pu1611;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Base64;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;
import com.pupu.pu1611.Activity.LognInActivity;
import com.pupu.pu1611.Activity.UserActivity;
import com.pupu.pu1611.Adapter.MainViewPagerAdapter;
import com.pupu.pu1611.Fragment.Fragment_List_Chat;
import com.pupu.pu1611.Fragment.Fragment_Phone_Book;
import com.pupu.pu1611.Fragment.Fragment_Status;
import com.pupu.pu1611.Fragment.Fragment_User;
import com.pupu.pu1611.databinding.ActivityMainBinding;
import com.pupu.pu1611.utilities.Constants;
import com.pupu.pu1611.utilities.PreferenceManager;

import java.util.HashMap;

public class MainActivity extends AppCompatActivity{
    private ActivityMainBinding binding;
    private PreferenceManager preferenceManager;

    private int REQUEST_CODE_BATTERY_OPTIMIZATIONS = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(getApplicationContext());


        ///////
        init();
        getToken();
        loadUserDetails();
        setListeners();
        checkForBatteryOptimizations();
        signOut();

    }

    private void init(){
        MainViewPagerAdapter mainViewPagerAdapter = new MainViewPagerAdapter(getSupportFragmentManager(),getLifecycle());
        mainViewPagerAdapter.addFragment(new Fragment_List_Chat());
        mainViewPagerAdapter.addFragment(new Fragment_Phone_Book());
        mainViewPagerAdapter.addFragment(new Fragment_Status());
        mainViewPagerAdapter.addFragment(new Fragment_User());
        binding.myViewPager.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL);
        binding.myViewPager.setAdapter(mainViewPagerAdapter);
        TabLayout tabLayout = findViewById(R.id.myTabLayout);
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.icons8_message_64));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.icons8_book_64));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.icons8_hourglass_64));
        tabLayout.addTab(tabLayout.newTab().setIcon(R.drawable.icons8_user_64));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.myViewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
        binding.myViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                tabLayout.selectTab(tabLayout.getTabAt(position));
            }
        });
    }

    private void loadUserDetails(){
        binding.textName.setText(preferenceManager.getString(Constants.KEY_FIRST_NAME)+" "+
                preferenceManager.getString(Constants.KEY_LAST_NAME));
        byte[] bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE),Base64.DEFAULT);
        Bitmap bitmap = BitmapFactory.decodeByteArray(bytes,0,bytes.length);
        binding.imageProfile.setImageBitmap(bitmap);
    }


    private void showMessage (String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void setListeners(){
        binding.fabNewChat.setOnClickListener(v ->{
            startActivity(new Intent(getApplicationContext(), UserActivity.class));
        });
    }


    private void signOut(){
        binding.imageFind.setOnClickListener(v -> {
            showMessage("Signing out ....");
            FirebaseFirestore database = FirebaseFirestore.getInstance();
            DocumentReference documentReference =
                    database.collection(Constants.KEY_COLLECTION_USERS).document(
                            preferenceManager.getString(Constants.KEY_USER_ID)
                    );
            HashMap<String,Object> update = new HashMap<>();
            update.put(Constants.KEY_FCM_TOKEN, FieldValue.delete());
            documentReference.update(update)
                    .addOnSuccessListener(unused -> {
                        preferenceManager.clear();
                        startActivity(new Intent(getApplicationContext(), LognInActivity.class));
                        finish();
                    })
                    .addOnFailureListener(e -> showMessage("loi dang xuat"));
        });

    }

    private void getToken(){
        FirebaseMessaging.getInstance().getToken().addOnSuccessListener(this::updateToken);
    }

    private void updateToken(String token){
        preferenceManager.putString(Constants.KEY_FCM_TOKEN,token);
        FirebaseFirestore database = FirebaseFirestore.getInstance();
        DocumentReference documentReference =
                database.collection(Constants.KEY_COLLECTION_USERS).document(
                        preferenceManager.getString(Constants.KEY_USER_ID)
                );
        documentReference.update(Constants.KEY_FCM_TOKEN,token)
                .addOnFailureListener(e -> showMessage("them token that bai"));

    }



    private void checkForBatteryOptimizations(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
            if(!powerManager.isIgnoringBatteryOptimizations(getPackageName())){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("warning");
                builder.setMessage("Battery optization i enable");
                builder.setPositiveButton("Disable", (dialog, which) -> {
                    Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
                    startActivityForResult(intent,REQUEST_CODE_BATTERY_OPTIMIZATIONS);
                });
                builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());
                builder.create().show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == REQUEST_CODE_BATTERY_OPTIMIZATIONS){
            checkForBatteryOptimizations();
        }
    }
}