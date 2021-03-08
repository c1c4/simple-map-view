package com.example.myapplication

import android.annotation.SuppressLint
import android.app.Activity
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.PointF
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.anchorsheet.AnchorSheetBehavior
import com.example.myapplication.anchorsheet.AnchorSheetBehavior.AnchorSheetCallback
import com.example.myapplication.repository.Repository
import com.example.myapplication.utils.Constants.Companion.CALLOUT_LAYER_ID
import com.example.myapplication.utils.Constants.Companion.DESCRIPTION_SELECTED
import com.example.myapplication.utils.Constants.Companion.GEOJSON_SOURCE_ID
import com.example.myapplication.utils.Constants.Companion.ID_SELECTED
import com.example.myapplication.utils.Constants.Companion.MARKER_IMAGE_ID
import com.example.myapplication.utils.Constants.Companion.MARKER_LAYER_ID
import com.example.myapplication.utils.Constants.Companion.SUBTITLE_SELECTED
import com.example.myapplication.utils.Constants.Companion.TITLE_SELECTED
import com.example.myapplication.utils.Constants.Companion.treatNullValue
import com.mapbox.android.core.permissions.PermissionsListener
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.geojson.Feature
import com.mapbox.geojson.FeatureCollection
import com.mapbox.geojson.Point
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions
import com.mapbox.mapboxsdk.location.LocationComponentOptions
import com.mapbox.mapboxsdk.location.modes.CameraMode
import com.mapbox.mapboxsdk.location.modes.RenderMode
import com.mapbox.mapboxsdk.maps.MapboxMap
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback
import com.mapbox.mapboxsdk.maps.Style
import com.mapbox.mapboxsdk.style.layers.Property.ICON_ANCHOR_BOTTOM
import com.mapbox.mapboxsdk.style.layers.PropertyFactory.*
import com.mapbox.mapboxsdk.style.layers.SymbolLayer
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource
import kotlinx.android.synthetic.main.activity_mapview.*
import kotlinx.android.synthetic.main.bottomsheet_map.*
import kotlinx.android.synthetic.main.bottomsheet_map.view.*
import java.util.*
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity(), OnMapReadyCallback, MapboxMap.OnMapClickListener,
    PermissionsListener {
    private lateinit var viewModel: MainViewModel
    private var mapboxMap: MapboxMap? = null
    private var source: GeoJsonSource? = null
    private var featureCollection: FeatureCollection? = null
    private var bsBehavior: AnchorSheetBehavior<*>? = null
    private val mLoc: LatLng? = null
    private var permissionsManager: PermissionsManager = PermissionsManager(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true)
        }
        if (Build.VERSION.SDK_INT >= 19) {
            window.decorView.systemUiVisibility =
                View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
        }
        //make fully Android Transparent Status bar
        //make fully Android Transparent Status bar
        if (Build.VERSION.SDK_INT >= 21) {
            setWindowFlag(this, WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false)
            window.statusBarColor = Color.TRANSPARENT
        }

        // Hide ActionBar to leave only the map
        supportActionBar?.hide()

        Mapbox.getInstance(this, getString(R.string.mapbox_access_token))

        //Create a singleton for retrofit and get for map points
        val repository = Repository()
        val viewModelFactory = MainViewModelFactory(repository)

        setContentView(R.layout.activity_mapview)

        initAnchorSheet()

        // Initialize
        viewModel = ViewModelProvider(this, viewModelFactory).get(MainViewModel::class.java)
        loadPoints()

        mapView?.onCreate(savedInstanceState)
        mapView?.getMapAsync(this)
    }

    private fun initAnchorSheet() {
        bsBehavior = AnchorSheetBehavior.from(bottomsheet_map)
        (bsBehavior as AnchorSheetBehavior<*>).state = AnchorSheetBehavior.STATE_COLLAPSED

        //anchor offset. any value between 0 and 1 depending upon the position u want
        (bsBehavior as AnchorSheetBehavior<*>).setAnchorOffset(0.8f)
        (bsBehavior as AnchorSheetBehavior<*>).setAnchorSheetCallback(object :
            AnchorSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
                if (newState == AnchorSheetBehavior.STATE_COLLAPSED) {
                    //action if needed
                }
                if (newState == AnchorSheetBehavior.STATE_EXPANDED) {
                }
                if (newState == AnchorSheetBehavior.STATE_DRAGGING) {
                }
                if (newState == AnchorSheetBehavior.STATE_ANCHOR) {
                }
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
                val h = bottomSheet.height.toFloat()
                val off = h * slideOffset
                when ((bsBehavior as AnchorSheetBehavior<*>).state) {
                    AnchorSheetBehavior.STATE_DRAGGING -> {
                        setMapPaddingBotttom(off)
                        //reposition marker at the center
                        if (mLoc != null) mapboxMap?.moveCamera(CameraUpdateFactory.newLatLng(mLoc))
                    }
                    AnchorSheetBehavior.STATE_SETTLING -> {
                        setMapPaddingBotttom(off)
                        //reposition marker at the center
                        if (mLoc != null) mapboxMap?.moveCamera(CameraUpdateFactory.newLatLng(mLoc))
                    }
                    AnchorSheetBehavior.STATE_HIDDEN -> {
                    }
                    AnchorSheetBehavior.STATE_EXPANDED -> {
                    }
                    AnchorSheetBehavior.STATE_COLLAPSED -> {
                    }
                }
            }
        })
    }

    private fun setMapPaddingBotttom(offset: Float) {
        //From 0.0 (min) - 1.0 (max) // bsExpanded - bsCollapsed;
        val maxMapPaddingBottom = 1.0f
        mapboxMap?.setPadding(0, 0, 0, (offset * maxMapPaddingBottom).roundToInt())
    }

    fun setWindowFlag(activity: Activity, bits: Int, on: Boolean) {
        val win = activity.window
        val winParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    override fun onMapReady(mapboxMap: MapboxMap) {
        this.mapboxMap = mapboxMap

        mapboxMap.setStyle(Style.MAPBOX_STREETS) {
            mapboxMap.addOnMapClickListener(this)
            enableLocationComponent(it)
        }
    }

    @SuppressLint("MissingPermission")
    private fun enableLocationComponent(loadedMapStyle: Style) {
        // Check if permissions are enabled and if not request
        if (PermissionsManager.areLocationPermissionsGranted(this)) {

            // Create and customize the LocationComponent's options
            val customLocationComponentOptions = LocationComponentOptions.builder(this)
                .trackingGesturesManagement(true)
                .accuracyColor(ContextCompat.getColor(this, R.color.mapboxGreen))
                .build()

            val locationComponentActivationOptions = LocationComponentActivationOptions.builder(this, loadedMapStyle)
                .locationComponentOptions(customLocationComponentOptions)
                .build()

            // Get an instance of the LocationComponent and then adjust its settings
            mapboxMap!!.locationComponent.apply {

                // Activate the LocationComponent with options
                activateLocationComponent(locationComponentActivationOptions)

                // Enable to make the LocationComponent visible
                isLocationComponentEnabled = true

                // Set the LocationComponent's camera mode
                cameraMode = CameraMode.TRACKING

                // Set the LocationComponent's render mode
                renderMode = RenderMode.COMPASS
            }
        } else {
            permissionsManager = PermissionsManager(this)
            permissionsManager.requestLocationPermissions(this)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionsManager.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onExplanationNeeded(permissionsToExplain: List<String>) {
        Toast.makeText(this, R.string.user_location_permission_explanation, Toast.LENGTH_LONG).show()
    }

    override fun onPermissionResult(granted: Boolean) {
        if (granted) {
            enableLocationComponent(mapboxMap!!.style!!)
        } else {
            Toast.makeText(this, R.string.user_location_permission_not_granted, Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onMapClick(point: LatLng): Boolean {
        return handleClickIcon(mapboxMap!!.projection.toScreenLocation(point));
    }

    /**
     * Sets up all of the sources and layers needed for this example
     *
     * @param collection the FeatureCollection to set equal to the globally-declared FeatureCollection
     */
    private fun setUpData(collection: FeatureCollection) {
        featureCollection = collection
        if (mapboxMap != null) {
            mapboxMap!!.getStyle { style: Style ->
                setupSource(style)
                setUpImage(style)
                setUpMarkerLayer(style)
                setUpInfoWindowLayer(style)
            }

            refreshSource()
        }
    }

    /**
     * Adds the GeoJSON source to the map
     */
    private fun setupSource(loadedStyle: Style) {
        source = GeoJsonSource(GEOJSON_SOURCE_ID, featureCollection)
        loadedStyle.addSource(source!!)
    }

    /**
     * Adds the marker image to the map for use as a SymbolLayer icon
     */
    private fun setUpImage(loadedStyle: Style) {
        loadedStyle.addImage(
            MARKER_IMAGE_ID, BitmapFactory.decodeResource(
                this.resources, R.drawable.mapbox_marker_icon_default
            )
        )
    }

    /**
     * Updates the display of data on the map after the FeatureCollection has been modified
     */
    private fun refreshSource() {
        if (source != null && featureCollection != null) {
            source!!.setGeoJson(featureCollection)
        }
    }

    /**
     * Setup a layer with maki icons, eg. west coast city.
     */
    private fun setUpMarkerLayer(loadedStyle: Style) {
        loadedStyle.addLayer(
            SymbolLayer(MARKER_LAYER_ID, GEOJSON_SOURCE_ID)
                .withProperties(
                    iconImage(MARKER_IMAGE_ID),
                    iconAllowOverlap(true),
                    iconOffset(arrayOf(0f, -8f))
                )
        )
    }

    /**
     * Setup a layer with Android SDK call-outs
     *
     *
     * name of the feature is used as key for the iconImage
     *
     */
    private fun setUpInfoWindowLayer(loadedStyle: Style) {
        loadedStyle.addLayer(
            SymbolLayer(CALLOUT_LAYER_ID, GEOJSON_SOURCE_ID)
                .withProperties( /* show image with id title based on the value of the name feature property */
                    iconImage("{title}"),  /* set anchor of icon to bottom-left */
                    iconAnchor(ICON_ANCHOR_BOTTOM),  /* all info window and marker image to appear at the same time*/
                    iconAllowOverlap(true),  /* offset the info window to be above the marker */
                    iconOffset(arrayOf(-2f, -28f))
                )
        )
    }

    /**
     * This method handles click events for SymbolLayer symbols.
     *
     *
     * When a SymbolLayer icon is clicked, we moved that feature to the selected state.
     *
     *
     * @param screenPoint the point on screen clicked
     */
    private fun handleClickIcon(screenPoint: PointF): Boolean {
        val features = mapboxMap!!.queryRenderedFeatures(screenPoint, MARKER_LAYER_ID)
        return if (!features.isEmpty()) {
            val id = features[0].getNumberProperty(ID_SELECTED).toInt()
            val featureList = featureCollection!!.features()
            if (featureList != null) {
                for (i in featureList.indices) {
                    if (featureList[i].getNumberProperty(ID_SELECTED) == id) {
                        val title = featureList[i].getStringProperty(TITLE_SELECTED)
                        val subtitle = featureList[i].getStringProperty(SUBTITLE_SELECTED)
                        val description = featureList[i].getStringProperty(
                            DESCRIPTION_SELECTED
                        )
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                            bottomsheet_map.title.text = Html.fromHtml(
                                title,
                                Html.FROM_HTML_MODE_LEGACY
                            )
                            bottomsheet_map.subtitle.text =
                                Html.fromHtml(subtitle, Html.FROM_HTML_MODE_LEGACY)
                            bottomsheet_map.description.text =
                                Html.fromHtml(description, Html.FROM_HTML_MODE_LEGACY)
                        } else {
                            bottomsheet_map.title.text = Html.fromHtml(title)
                            bottomsheet_map.subtitle.text = Html.fromHtml(subtitle)
                            bottomsheet_map.description.text = Html.fromHtml(description)
                        }

                        bottomsheet_map.description.movementMethod = LinkMovementMethod.getInstance()
                    }
                }
            }
            bottomsheet_map.visibility = View.VISIBLE
            if(bsBehavior?.state == AnchorSheetBehavior.STATE_COLLAPSED) {
                bsBehavior?.state = AnchorSheetBehavior.STATE_ANCHOR
            }
            true
        } else {
            bottomsheet_map.visibility = View.GONE
            bsBehavior?.state = AnchorSheetBehavior.STATE_COLLAPSED
            false
        }
    }

    // Load the Map points, filter to get only with the type is SimplePoi, and create a list of features who will display when clicking on a marker
    private fun loadPoints() {
        val featuresList = mutableListOf<Feature>()
        viewModel.getMapInfoPoints()
        viewModel.myResponse.observe(this, Observer { response ->
            if (response.isSuccessful) {
                response.body()?.forEach { mapPoints ->
                    if (mapPoints.type == "SimplePoi") {
                        val lng = mapPoints.position[0]
                        val lat = mapPoints.position[1]
                        val feature = Feature.fromGeometry(Point.fromLngLat(lng, lat))

                        feature.addNumberProperty(ID_SELECTED, mapPoints.id)

                        if (Locale.getDefault().language == Locale.GERMAN.language || mapPoints.title_en.isNullOrEmpty())
                            feature.addStringProperty(
                                TITLE_SELECTED,
                                treatNullValue(mapPoints.title)
                            )
                        else
                            feature.addStringProperty(TITLE_SELECTED, mapPoints.title_en)

                        if (Locale.getDefault().language == Locale.GERMAN.language || mapPoints.subtitle_en.isNullOrEmpty())
                            feature.addStringProperty(
                                SUBTITLE_SELECTED,
                                treatNullValue(mapPoints.subtitle)
                            )
                        else
                            feature.addStringProperty(SUBTITLE_SELECTED, mapPoints.subtitle_en)

                        if (Locale.getDefault().language == Locale.GERMAN.language || mapPoints.description_en.isNullOrEmpty())
                            feature.addStringProperty(
                                DESCRIPTION_SELECTED,
                                treatNullValue(mapPoints.description)
                            )
                        else
                            feature.addStringProperty(DESCRIPTION_SELECTED, mapPoints.description)

                        featuresList.add(feature)
                    }
                }
                setUpData(FeatureCollection.fromFeatures(featuresList))
            } else {
                Log.d("Response", response.errorBody().toString())
            }
        })
    }

    override fun onStart() {
        super.onStart()
        mapView?.onStart()
    }

    public override fun onResume() {
        super.onResume()
        mapView?.onResume()
        refreshSource()
    }

    override fun onStop() {
        super.onStop()
        mapView?.onStop()
    }

    public override fun onPause() {
        super.onPause()
        mapView?.onPause()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView?.onLowMemory()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mapboxMap != null) {
            mapboxMap?.removeOnMapClickListener(this);
        }
        mapView?.onDestroy();
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView?.onSaveInstanceState(outState)
    }
}