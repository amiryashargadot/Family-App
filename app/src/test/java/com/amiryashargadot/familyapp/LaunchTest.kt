package com.amiryashargadot.familyapp
import android.view.ViewGroup
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
@RunWith(RobolectricTestRunner::class)
@Config(sdk=[35])
class LaunchTest {
 @Test fun activityCreatesNativeContentAndReopens(){
  val first=Robolectric.buildActivity(MainActivity::class.java).setup()
  assertFalse(first.get().isFinishing)
  assertTrue(first.get().findViewById<ViewGroup>(android.R.id.content).childCount>0)
  first.pause().stop().destroy()
  val second=Robolectric.buildActivity(MainActivity::class.java).setup()
  assertTrue(second.get().findViewById<ViewGroup>(android.R.id.content).childCount>0)
  second.pause().stop().destroy()
 }
}
