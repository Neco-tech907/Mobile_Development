package ru.mirea.ivanovrr.yandexdriver;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.yandex.mapkit.MapKitFactory;
import com.yandex.mapkit.RequestPoint;
import com.yandex.mapkit.RequestPointType;
import com.yandex.mapkit.directions.DirectionsFactory;
import com.yandex.mapkit.directions.driving.DrivingOptions;
import com.yandex.mapkit.directions.driving.DrivingRoute;
import com.yandex.mapkit.directions.driving.DrivingRouter;
import com.yandex.mapkit.directions.driving.DrivingSession;
import com.yandex.mapkit.directions.driving.VehicleOptions;
import com.yandex.mapkit.geometry.Point;
import com.yandex.mapkit.map.CameraPosition;
import com.yandex.mapkit.map.MapObject;
import com.yandex.mapkit.map.MapObjectCollection;
import com.yandex.mapkit.map.MapObjectTapListener;
import com.yandex.mapkit.map.PlacemarkMapObject;
import com.yandex.mapkit.mapview.MapView;
import com.yandex.runtime.Error;
import com.yandex.runtime.image.ImageProvider;
import com.yandex.runtime.network.NetworkError;
import com.yandex.runtime.network.RemoteError;

import java.util.ArrayList;
import java.util.List;

import ru.mirea.ivanovrr.yandexdriver.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity implements DrivingSession.DrivingRouteListener {

    private ActivityMainBinding binding;


    private final Point ROUTE_START_LOCATION = new Point(55.751574, 37.573856);
    private final Point ROUTE_END_LOCATION = new Point(55.773121, 37.638278);

    private final Point SCREEN_CENTER = new Point(
            (ROUTE_START_LOCATION.getLatitude() + ROUTE_END_LOCATION.getLatitude()) / 2,
            (ROUTE_START_LOCATION.getLongitude() + ROUTE_END_LOCATION.getLongitude()) / 2);

    private MapView mapView;
    private MapObjectCollection mapObjects;
    private DrivingRouter drivingRouter;
    private DrivingSession drivingSession;
    private int[] colors = {0xFFFF0000, 0xFF00FF00, 0x00FFBBBB, 0xFF0000FF};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MapKitFactory.initialize(this);
        DirectionsFactory.initialize(this);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mapView = binding.mapview;
        mapView.getMap().setRotateGesturesEnabled(false);

        // Камера в центр между двумя точками
        mapView.getMap().move(new CameraPosition(SCREEN_CENTER, 10, 0, 0));

        drivingRouter = DirectionsFactory.getInstance().createDrivingRouter(com.yandex.mapkit.directions.driving.DrivingRouterType.COMBINED);
        mapObjects = mapView.getMap().getMapObjects().addCollection();

        submitRequest();
        addDestinationMarker(); // ВЫПОЛНЕНИЕ ПРАКТИЧЕСКОГО ЗАДАНИЯ
    }

    private void submitRequest() {
        DrivingOptions drivingOptions = new DrivingOptions();
        VehicleOptions vehicleOptions = new VehicleOptions();
        drivingOptions.setRoutesCount(4); // Кол-во альтернативных путей

        ArrayList<RequestPoint> requestPoints = new ArrayList<>();
        requestPoints.add(new RequestPoint(ROUTE_START_LOCATION, RequestPointType.WAYPOINT, null, null));
        requestPoints.add(new RequestPoint(ROUTE_END_LOCATION, RequestPointType.WAYPOINT, null, null));

        drivingSession = drivingRouter.requestRoutes(requestPoints, drivingOptions, vehicleOptions, this);
    }

    // --- ВЫПОЛНЕНИЕ ЗАДАНИЯ ИЗ МЕТОДИЧКИ ---
    private void addDestinationMarker() {
        // Добавляем маркер в конечной точке
        PlacemarkMapObject marker = mapObjects.addPlacemark(
                ROUTE_END_LOCATION,
                ImageProvider.fromResource(this, android.R.drawable.ic_dialog_map)); // Стандартная иконка

        // Слушатель нажатий
        marker.addTapListener(new MapObjectTapListener() {
            @Override
            public boolean onMapObjectTap(@NonNull MapObject mapObject, @NonNull Point point) {
                Toast.makeText(getApplicationContext(), "Мое любимое место! 🍔🍟", Toast.LENGTH_LONG).show();
                return true; // Возвращаем true, чтобы событие считалось обработанным
            }
        });
    }

    @Override
    public void onDrivingRoutes(@NonNull List<DrivingRoute> list) {
        int color;
        for (int i = 0; i < list.size(); i++) {
            // Настраиваем цвета для каждого маршрута (если маршрутов больше чем цветов в массиве, берем по модулю)
            color = colors[i % colors.length];
            // Рисуем маршрут на карте
            mapObjects.addPolyline(list.get(i).getGeometry()).setStrokeColor(color);
        }
    }

    @Override
    public void onDrivingRoutesError(@NonNull Error error) {
        String errorMessage = "Неизвестная ошибка";
        if (error instanceof RemoteError) {
            errorMessage = "Ошибка сервера";
        } else if (error instanceof NetworkError) {
            errorMessage = "Ошибка сети";
        }
        Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        MapKitFactory.getInstance().onStop();
        super.onStop();
    }

    @Override
    protected void onStart() {
        super.onStart();
        MapKitFactory.getInstance().onStart();
        mapView.onStart();
    }
}
