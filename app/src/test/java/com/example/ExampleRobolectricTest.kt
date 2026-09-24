package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Final kook - کوک", appName)
    val copyright = context.getString(R.string.copyright_notice)
    assertEquals("تمام حق اثر متعلق به Orange IT است", copyright)
  }

  @Test
  fun `test osmdroid marker and basic info window`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    org.osmdroid.config.Configuration.getInstance().load(context, context.getSharedPreferences("osmdroid", Context.MODE_PRIVATE))
    val mapView = org.osmdroid.views.MapView(context)
    val marker = org.osmdroid.views.overlay.Marker(mapView)
    marker.title = "Test Marker"
  }

  @Test
  fun `launch main activity`() {
    val controller = org.robolectric.Robolectric.buildActivity(MainActivity::class.java).setup()
    org.junit.Assert.assertNotNull(controller.get())
  }
}
