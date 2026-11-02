package com.example

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.unit.LayoutDirection
import com.example.data.AppState
import com.example.ui.auth.AuthScreen
import com.example.ui.discover.DiscoverScreen
import com.example.ui.feed.FeedScreen
import com.example.ui.map.MapScreen
import com.example.ui.profile.ProfileScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.wallet.WalletScreen
import com.github.takahirom.roborazzi.RobolectricDeviceQualifiers
import com.github.takahirom.roborazzi.captureRoboImage
import java.io.File
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = RobolectricDeviceQualifiers.Pixel8, sdk = [36])
class GreetingScreenshotTest {

  @get:Rule val composeTestRule = createComposeRule()

  private val artifactDir =
      File("../.aistudio/artifacts/brain/d313a653-bee1-43f6-a7c4-c8173a96c2a2").apply { mkdirs() }

  private fun captureScreen(name: String, summary: String) {
    val screenshotFile = File("src/test/screenshots/$name.png")
    composeTestRule.onRoot().captureRoboImage(filePath = screenshotFile.path)
    if (screenshotFile.exists()) {
      val destFile = File(artifactDir, "$name.png")
      screenshotFile.copyTo(destFile, overwrite = true)
      File(artifactDir, "$name.png.metadata.json")
          .writeText(
              """
              {
                "summary": "$summary",
                "updatedAt": "${java.time.Instant.now()}",
                "userFacing": true
              }
              """.trimIndent()
          )
    }
  }

  @Test
  fun feed_screen_screenshot() {
    composeTestRule.setContent {
      com.example.utils.DailyRewardManager(androidx.compose.ui.platform.LocalContext.current)
          .markRewardAsSeenToday()
      MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
          Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            FeedScreen(
                onNavigateToInbox = {},
                onNavigateToProfile = {},
                onNavigateToAuth = {},
                onNavigateToRegister = {}
            )
          }
        }
      }
    }
    captureScreen("01_feed_screen", "صفحه فید ویدیویی و ویترین محصولات اپلیکیشن کوک")
  }

  @Test
  fun discover_screen_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
          Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            DiscoverScreen(onNavigateToStore = {})
          }
        }
      }
    }
    captureScreen("02_discover_screen", "صفحه اکسپلور، بازارگردی و فروشگاه‌های منتخب اپلیکیشن کوک")
  }

  @Test
  fun map_screen_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
          Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            MapScreen(onNavigateToStore = {})
          }
        }
      }
    }
    captureScreen("03_map_screen", "صفحه نقشه تعاملی فروشگاه‌های تاییدشده اپلیکیشن کوک")
  }

  @Test
  fun wallet_screen_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
          Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            WalletScreen(
                onNavigateToChat = {},
                onNavigateToAuth = {},
                onNavigateToRegister = {}
            )
          }
        }
      }
    }
    captureScreen("04_wallet_screen", "صفحه سبد خرید، سفارشات و کیف پول اپلیکیشن کوک")
  }

  @Test
  fun profile_screen_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
          Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            ProfileScreen(
                userId = "store_1",
                onNavigateToSettings = {},
                onNavigateToEditProfile = {},
                onNavigateToStoreApplication = {},
                onNavigateToStore = {},
                onNavigateToAuth = {},
                onNavigateToRegister = {}
            )
          }
        }
      }
    }
    captureScreen("05_profile_screen", "صفحه پروفایل و ویترین اختصاصی فروشگاه در اپلیکیشن کوک")
  }

  @Test
  fun auth_screen_screenshot() {
    composeTestRule.setContent {
      MyApplicationTheme {
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
          Box(modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.background)) {
            AuthScreen(initialTab = 0, onAuthSuccess = {}, onSkipOrClose = {})
          }
        }
      }
    }
    captureScreen("06_auth_screen", "صفحه ورود و ثبت‌نام امن کاربران در اپلیکیشن کوک")
  }
}


