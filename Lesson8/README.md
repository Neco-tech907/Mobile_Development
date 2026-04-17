# ОТЧЕТ ПО ПРАКТИЧЕСКОЙ РАБОТЕ №8
**Тема:** Картографические сервисы.

## 1. Цель работы
Изучить основы работы с современными картографическими сервисами на платформе Android. Получить практические навыки интеграции проприетарного сервиса Yandex Maps (с использованием инструментария Yandex MapKit) и открытого сервиса OpenStreetMap (с использованием библиотеки osmdroid). Реализовать функции отображения текущего местоположения пользователя, построения автомобильных маршрутов, добавления пользовательских слоев интерфейса (компас, шкала масштаба) и интерактивных маркеров на карту.

## 2. Программная реализация

В рамках практической работы было создано три независимых модуля, каждый из которых демонстрирует работу с различными функциями картографических API.

### 2.1. Модуль `yandexmaps` — Интеграция Яндекс.Карт и геолокации
Для работы с Yandex MapKit был получен уникальный API-ключ в кабинете разработчика, который инициализируется в классе-наследнике `Application`:
```java
MapKitFactory.setApiKey(MAPKIT_API_KEY);
```

В `MainActivity` реализована логика запроса разрешений (`ACCESS_FINE_LOCATION`, `ACCESS_COARSE_LOCATION`) у пользователя в runtime. При успешном получении прав вызывается метод для инициализации слоя определения местоположения:
```java
private void loadUserLocationLayer() {
    MapKit mapKit = MapKitFactory.getInstance();
    mapKit.resetLocationManagerToDefault();
    userLocationLayer = mapKit.createUserLocationLayer(mapView.getMapWindow());
    userLocationLayer.setVisible(true);
    userLocationLayer.setHeadingEnabled(true);
    userLocationLayer.setObjectListener(this); // Подписка на события локации
}
```
Для кастомизации отображения текущей геопозиции был имплементирован интерфейс `UserLocationObjectListener`. В переопределенном методе `onObjectAdded` стандартные маркеры заменены на пользовательские иконки: иконка пина для состояния покоя и иконка стрелки (направления) при движении устройства. Также настроен цвет круга погрешности (accuracy circle).

### 2.2. Модуль `yandexdriver` — Построение маршрутов движения
Модуль демонстрирует возможности API `DirectionsFactory` для прокладки автомобильных маршрутов. Были заданы две гео-точки: стартовая (местоположение) и конечная (пункт назначения).

Формирование запроса на сервер Яндекса осуществляется через класс `DrivingRouter`:
```java
DrivingOptions drivingOptions = new DrivingOptions();
drivingOptions.setRoutesCount(4); // Запрос до 4 альтернативных маршрутов
ArrayList<RequestPoint> requestPoints = new ArrayList<>();
requestPoints.add(new RequestPoint(ROUTE_START_LOCATION, RequestPointType.WAYPOINT, null));
requestPoints.add(new RequestPoint(ROUTE_END_LOCATION, RequestPointType.WAYPOINT, null));

drivingSession = drivingRouter.requestRoutes(requestPoints, drivingOptions, new VehicleOptions(), this);
```
Результат обрабатывается в методе `onDrivingRoutes`, где полученные маршруты циклично отрисовываются на карте (добавляются объекты `Polyline`). Для визуального разделения альтернативных путей используется массив различных цветов.

**Выполнение задания из методички:** В конечной точке маршрута (любимое заведение) был программно установлен маркер (`PlacemarkMapObject`). К маркеру привязан слушатель нажатий `MapObjectTapListener`, который при тапе по иконке выводит всплывающее сообщение (Toast) с краткой информацией о заведении:
```java
PlacemarkMapObject marker = mapObjects.addPlacemark(ROUTE_END_LOCATION, iconProvider);
marker.addTapListener((mapObject, point) -> {
    Toast.makeText(getApplicationContext(), "Мое любимое заведение", Toast.LENGTH_LONG).show();
    return true;
});
```

### 2.3. Модуль `osmmaps` — Интеграция OpenStreetMap
Для работы с OSM используется открытая библиотека `osmdroid`. В отличие от Яндекса, данный сервис не требует API-ключа, но требует корректной настройки политики использования сети и хранилища (SharedPreferences) для кеширования тайлов карты:
```java
Context ctx = getApplicationContext();
Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
```

На карту (`MapView`) были добавлены дополнительные слои (Overlays) для расширения функционала:
1. **Слой локации:** `MyLocationNewOverlay` — отображает местоположение пользователя по GPS.
2. **Компас:** `CompassOverlay` — показывает текущую ориентацию устройства по сторонам света.
3. **Шкала масштаба:** `ScaleBarOverlay` — отображает метрическую линейку, размер которой динамически меняется при зуме карты.

**Выполнение задания из методички:** Была написана функция `addMarker`, принимающая координаты, название и описание интересующего места. На карту OSM добавлены три пользовательских маркера (объекты `Marker`). Для каждого из них переопределен метод `onMarkerClick`, который извлекает переданное название и описание, выводя их на экран пользователя:
```java
Marker marker = new Marker(mapView);
marker.setPosition(point);
marker.setTitle(title);
marker.setOnMarkerClickListener((m, mv) -> {
    Toast.makeText(getApplicationContext(), title + ": " + description, Toast.LENGTH_LONG).show();
    return true;
});
mapView.getOverlays().add(marker);
```

## 3. Заключение
В ходе выполнения практической работы №8 были успешно изучены механизмы работы с картографическими сервисами в мобильных приложениях Android.

Были получены и закреплены следующие компетенции:
* Интеграция сторонних SDK в проект через Gradle-зависимости (Yandex MapKit, osmdroid).
* Работа с разрешениями Android (Runtime Permissions) для доступа к службам геолокации.
* Управление жизненным циклом картографических компонентов (`onStart`, `onStop`, `onResume`, `onPause`) для корректного освобождения ресурсов устройства.
* Работа со слоями карты, добавление графических примитивов (маркеры, полилинии для маршрутов).
* Обработка пользовательского ввода и перехват событий нажатия на объекты карты.

Поставленная цель достигнута, все практические задания модулей выполнены в полном объеме.