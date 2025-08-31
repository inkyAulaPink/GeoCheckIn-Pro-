package com.example.checkin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.checkin.databinding.ActivityMainBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CheckInPage extends AppCompatActivity implements LocationListener {

    Button button_location;
    ImageView backIcon;
    TextView text_location, latitText, longiText, timeText;

    ActivityMainBinding binding;
    LocationManager locationManager;
    DatabaseReference reference;
    FirebaseDatabase db;

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in_page);

        text_location = findViewById(R.id.text_location);
        button_location = findViewById(R.id.button_location);
        backIcon = findViewById(R.id.backIcon);
        latitText = findViewById(R.id.latitText);
        longiText = findViewById(R.id.longiText);
        timeText = findViewById(R.id.timeText);

        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String matric = intent.getStringExtra("matric");
        String phone = intent.getStringExtra("phone");
        String email = intent.getStringExtra("email");

        button_location.setOnClickListener(v -> {
            getLocation();
            Date currentDate = Calendar.getInstance().getTime();
            timeText.setText(currentDate.toString());
        });

        backIcon.setOnClickListener(v -> {
            Intent backIntent = new Intent(CheckInPage.this, MainActivity.class);
            startActivity(backIntent);
            finish(); // Optional: Finish the current activity to prevent going back to it on back press
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        try {
            locationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 5, CheckInPage.this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChanged(@NonNull Location location) {

        double latitude = location.getLatitude();
        double longitude = location.getLongitude();

        latitText.setText(String.valueOf(latitude));
        longiText.setText(String.valueOf(longitude));


        Intent intent = getIntent();
        String name = intent.getStringExtra("name");
        String matric = intent.getStringExtra("matric");
        String phone = intent.getStringExtra("phone");
        String email = intent.getStringExtra("email");


        Toast.makeText(this, "" + location.getLatitude() + "," + location.getLongitude(), Toast.LENGTH_SHORT).show();

        try {
            Geocoder geocoder = new Geocoder(CheckInPage.this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            String address = addresses.get(0).getAddressLine(0);

            text_location.setText(address);

            sendUserDataToFirebase(name, matric, phone, email, text_location.getText().toString(), latitude, longitude, timeText.getText().toString());

        } catch (Exception e) {
            e.printStackTrace();
        }
        sendUserDataToFirebase(name, matric, phone, email, text_location.getText().toString(), latitude, longitude, timeText.getText().toString());


    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted, start requesting location updates
                getLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                // Handle permission denied scenario
            }
        }
    }

    public void sendUserDataToFirebase(String name, String matric, String phone, String email, String location, double latitude, double longitude, String currentDate) {
        CheckInData checkInData = new CheckInData(name, matric, phone, email, location, latitude, longitude, currentDate);
        db = FirebaseDatabase.getInstance();
        reference = db.getReference("checkin").child(name); // Use 'name' as child key

        // Set value to this child node
        reference.setValue(checkInData)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            Toast.makeText(CheckInPage.this, "Check-in successful", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(CheckInPage.this, "Failed to check in", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }

    protected void onPause() {
        super.onPause();
        // Stop listening to location updates to preserve battery when the activity is paused
        locationManager.removeUpdates(this);
    }

    @Override
    public void onProviderEnabled(@NonNull String provider) {
        // Handle provider enabled event if needed
    }

    @Override
    public void onProviderDisabled(@NonNull String provider) {
        // Handle provider disabled event if needed
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // Handle status changed event if needed
    }

}
