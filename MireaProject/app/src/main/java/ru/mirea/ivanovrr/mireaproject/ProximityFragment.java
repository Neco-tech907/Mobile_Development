package ru.mirea.ivanovrr.mireaproject;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.Locale;

import ru.mirea.ivanovrr.mireaproject.databinding.FragmentProximityBinding;

public class ProximityFragment extends Fragment implements SensorEventListener {

    private FragmentProximityBinding binding;
    private SensorManager sensorManager;
    private Sensor proximitySensor;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProximityBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sensorManager = (SensorManager) requireContext().getSystemService(Context.SENSOR_SERVICE);
        if (sensorManager != null) {
            proximitySensor = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        }

        if (proximitySensor == null) {
            binding.distanceTextView.setText("Текущее значение: недоступно");
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (sensorManager != null && proximitySensor != null) {
            sensorManager.registerListener(this, proximitySensor, SensorManager.SENSOR_DELAY_NORMAL);
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (binding == null || event.sensor.getType() != Sensor.TYPE_PROXIMITY) {
            return;
        }

        float distance = event.values[0];

        binding.distanceTextView.setText(String.format(
                Locale.getDefault(),
                "Текущее значение: %.1f см",
                distance
        ));
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
