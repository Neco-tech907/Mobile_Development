package ru.mirea.ivanovrr.osmmaps;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.preference.PreferenceManager;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;
import org.osmdroid.views.overlay.ScaleBarOverlay;
import org.osmdroid.views.overlay.compass.CompassOverlay;
import org.osmdroid.views.overlay.compass.InternalCompassOrientationProvider;
import org.osmdroid.views.overlay.mylocation.GpsMyLocationProvider;
import org.osmdroid.views.overlay.mylocation.MyLocationNewOverlay;

import ru.mirea.ivanovrr.osmmaps.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION = 100;

    private MapView mapView;
    private ActivityMainBinding binding;
    private MyLocationNewOverlay locationNewOverlay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        mapView = binding.mapView;

        mapView.setZoomRounding(true);
        mapView.setMultiTouchControls(true);

        IMapController mapController = mapView.getController();
        mapController.setZoom(11.0);
        mapController.setCenter(new GeoPoint(55.751244, 37.618423));

        checkPermissionsAndInitOverlays();
    }

    private void checkPermissionsAndInitOverlays() {
        int locPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION);
        if (locPermission == PackageManager.PERMISSION_GRANTED) {
            initMapFeatures();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQUEST_CODE_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE_PERMISSION && grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            initMapFeatures();
        }
    }

    private void initMapFeatures() {
        locationNewOverlay = new MyLocationNewOverlay(new GpsMyLocationProvider(getApplicationContext()), mapView);
        locationNewOverlay.enableMyLocation();
        mapView.getOverlays().add(locationNewOverlay);

        CompassOverlay compassOverlay = new CompassOverlay(
                getApplicationContext(),
                new InternalCompassOrientationProvider(getApplicationContext()),
                mapView
        );
        compassOverlay.enableCompass();
        mapView.getOverlays().add(compassOverlay);

        DisplayMetrics dm = getApplicationContext().getResources().getDisplayMetrics();
        ScaleBarOverlay scaleBarOverlay = new ScaleBarOverlay(mapView);
        scaleBarOverlay.setCentred(true);
        scaleBarOverlay.setScaleBarOffset(dm.widthPixels / 2, 10);
        mapView.getOverlays().add(scaleBarOverlay);

        addMarker(
                new GeoPoint(55.753544, 37.621202),
                "Парк Зарядье",
                "Современный парк рядом с Кремлем с парящим мостом и красивыми видами на центр Москвы.",
                org.osmdroid.library.R.drawable.marker_default
        );
        addMarker(
                new GeoPoint(55.741389, 37.620556),
                "Третьяковская галерея",
                "Один из главных музеев русского искусства с известной коллекцией картин.",
                android.R.drawable.ic_menu_gallery
        );
        addMarker(
                new GeoPoint(55.760186, 37.582503),
                "Московский зоопарк",
                "Старейший зоопарк России, популярное место для прогулок и посещения павильонов.",
                android.R.drawable.ic_menu_compass
        );
        addMarker(
                new GeoPoint(55.819722, 37.611667),
                "Останкинская башня",
                "Знаменитая телебашня со смотровой площадкой и панорамным видом на город.",
                android.R.drawable.star_big_on
        );
    }

    private void addMarker(GeoPoint point, String title, String description, int iconResId) {
        Marker marker = new Marker(mapView);
        marker.setPosition(point);
        marker.setIcon(ResourcesCompat.getDrawable(getResources(), iconResId, null));
        marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        marker.setTitle(title);
        marker.setSnippet(description);

        marker.setOnMarkerClickListener(new Marker.OnMarkerClickListener() {
            @Override
            public boolean onMarkerClick(Marker clickedMarker, MapView clickedMapView) {
                clickedMarker.showInfoWindow();
                clickedMapView.getController().animateTo(point);
                Toast.makeText(getApplicationContext(), title + ": " + description, Toast.LENGTH_LONG).show();
                return true;
            }
        });

        mapView.getOverlays().add(marker);
    }

    @Override
    public void onResume() {
        super.onResume();
        Configuration.getInstance().load(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        );
        if (mapView != null) {
            mapView.onResume();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        Configuration.getInstance().save(
                getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
        );
        if (mapView != null) {
            mapView.onPause();
        }
    }
}
