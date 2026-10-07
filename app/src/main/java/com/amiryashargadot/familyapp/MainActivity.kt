package com.amiryashargadot.familyapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

data class HomeFeature(val title:String,val subtitle:String,val icon:String)
private val features = listOf(
    HomeFeature("בוקר","הרוטינה שלי להיום","☀️"),
    HomeFeature("ערב","מסיימים את היום","🌙"),
    HomeFeature("משימות","שלי ושל המשפחה","✓"),
    HomeFeature("סיכום יום","איך עבר עליי היום?","😊"),
    HomeFeature("חנות","נקודות, פרסים ושוברים","🛍️"),
    HomeFeature("מי מחליט?","הגרלה וטיימר משותף","🎲")
)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { MaterialTheme(colorScheme = lightColorScheme()) { FamilyApp() } }
    }
}

@Composable fun FamilyApp() {
    var selected by remember { mutableStateOf<HomeFeature?>(null) }
    Surface(Modifier.fillMaxSize()) {
        if (selected == null) HomeScreen { selected = it }
        else FeatureScreen(selected!!) { selected = null }
    }
}

@Composable fun HomeScreen(onOpen:(HomeFeature)->Unit) {
    LazyColumn(
        modifier=Modifier.fillMaxSize().padding(horizontal=20.dp),
        verticalArrangement=Arrangement.spacedBy(12.dp),
        contentPadding=PaddingValues(top=40.dp,bottom=32.dp)
    ) {
        item {
            Text("הבית שלנו", fontSize=32.sp, style=MaterialTheme.typography.headlineLarge)
            Text("ערב טוב 👋", style=MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.height(12.dp))
            Card {
                Row(Modifier.fillMaxWidth().padding(18.dp),horizontalArrangement=Arrangement.SpaceBetween) {
                    Column { Text("הנקודות שלי"); Text("0",fontSize=30.sp) }
                    Text("⭐",fontSize=34.sp)
                }
            }
        }
        items(features) { f ->
            ElevatedCard(onClick={onOpen(f)},modifier=Modifier.fillMaxWidth()) {
                Row(Modifier.padding(18.dp),verticalAlignment=Alignment.CenterVertically) {
                    Text(f.icon,fontSize=34.sp)
                    Spacer(Modifier.width(16.dp))
                    Column { Text(f.title,fontSize=20.sp); Text(f.subtitle,style=MaterialTheme.typography.bodyMedium) }
                }
            }
        }
    }
}

@Composable fun FeatureScreen(feature:HomeFeature,onBack:()->Unit) {
    Column(Modifier.fillMaxSize().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally) {
        Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically) {
            TextButton(onClick=onBack){ Text("חזרה") }
            Spacer(Modifier.weight(1f))
            Text(feature.title,fontSize=24.sp)
        }
        Spacer(Modifier.weight(1f))
        Text(feature.icon,fontSize=72.sp)
        Spacer(Modifier.height(16.dp))
        Text(feature.title,fontSize=28.sp)
        Text("המסך מחובר לשלד האפליקציה. הלוגיקה המלאה נכנסת בשלב הבא.")
        Spacer(Modifier.weight(1f))
    }
}
