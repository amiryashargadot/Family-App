package com.amiryashargadot.familyapp

import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.random.Random

data class Task(val title:String,val assignee:String,val giver:String,val points:Int,var done:Boolean=false)
data class Product(val title:String,val price:Int)
enum class Page{HOME,MORNING,EVENING,TASKS,SUMMARY,SHOP,DECIDE,ADMIN}
class MainActivity:ComponentActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContent{MaterialTheme{FamilyApp()}}}}

@Composable fun FamilyApp(){
 var page by remember{mutableStateOf(Page.HOME)};var points by remember{mutableIntStateOf(0)}
 val tasks=remember{mutableStateListOf<Task>()}
 val shop=remember{mutableStateListOf(Product("ערב סרט לבחירתי בשבוע הקרוב",1000),Product("ללכת לישון מאוחר",1000))}
 when(page){
  Page.HOME->Home(points){page=it}
  Page.MORNING->Routine("בוקר ☀️",listOf("להתלבש","ארוחת בוקר","לצחצח שיניים","תיק ובקבוק")){page=Page.HOME}
  Page.EVENING->Routine("ערב 🌙",listOf("מקלחת","לצחצח שיניים","להכין דברים למחר")){page=Page.HOME}
  Page.TASKS->Tasks(tasks,{points+=it}){page=Page.HOME}
  Page.SUMMARY->Summary({points+=it}){page=Page.HOME}
  Page.SHOP->Shop(points,shop,{points-=it}){page=Page.HOME}
  Page.DECIDE->Decide{page=Page.HOME}
  Page.ADMIN->Admin(shop){page=Page.HOME}
 }
}
@Composable fun Header(t:String,back:()->Unit){Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically){TextButton(onClick=back){Text("חזרה")};Spacer(Modifier.weight(1f));Text(t,fontSize=25.sp,fontWeight=FontWeight.Bold)}}
@Composable fun Home(points:Int,go:(Page)->Unit){
 val x=listOf(Page.MORNING to "☀️ בוקר",Page.EVENING to "🌙 ערב",Page.TASKS to "✓ משימות",Page.SUMMARY to "😊 סיכום יום",Page.SHOP to "🛍️ חנות",Page.DECIDE to "🎲 מי מחליט?")
 LazyColumn(Modifier.fillMaxSize().padding(20.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){
  item{Spacer(Modifier.height(18.dp));Text("הבית שלנו",fontSize=34.sp,fontWeight=FontWeight.Bold);Text("⭐ "+points+" נקודות",fontSize=21.sp)}
  items(x){pair->ElevatedCard(onClick={go(pair.first)},modifier=Modifier.fillMaxWidth()){Text(pair.second,fontSize=22.sp,modifier=Modifier.padding(22.dp))}}
  item{TextButton(onClick={go(Page.ADMIN)},modifier=Modifier.fillMaxWidth()){Text("ניהול המשפחה")}}
 }
}
@Composable fun Routine(title:String,defaults:List<String>,back:()->Unit){
 val rows=remember{mutableStateListOf<String>().apply{addAll(defaults)}};val done=remember{mutableStateListOf<Boolean>().apply{repeat(defaults.size){add(false)}}};var add by remember{mutableStateOf("")}
 Column(Modifier.fillMaxSize().padding(20.dp)){Header(title,back);Text("הרוטינה שלי להיום",fontSize=20.sp);rows.forEachIndexed{i,s->Row(verticalAlignment=Alignment.CenterVertically){Checkbox(done[i],{done[i]=it});Text(s,fontSize=19.sp)}};Spacer(Modifier.height(16.dp));OutlinedTextField(add,{add=it},label={Text("שלב חדש")},modifier=Modifier.fillMaxWidth());Button(onClick={if(add.isNotBlank()){rows.add(add);done.add(false);add=""}},modifier=Modifier.fillMaxWidth()){Text("הוסף לרוטינה")};Text("לכל שלב ניתן יהיה לבחור ימים א׳–ש׳.",style=MaterialTheme.typography.bodySmall)}
}
@Composable fun Tasks(list:MutableList<Task>,award:(Int)->Unit,back:()->Unit){
 var title by remember{mutableStateOf("")};var who by remember{mutableStateOf("")};var pts by remember{mutableStateOf("10")}
 Column(Modifier.fillMaxSize().padding(20.dp)){Header("משימות",back);OutlinedTextField(title,{title=it},label={Text("משימה")},modifier=Modifier.fillMaxWidth());OutlinedTextField(who,{who=it},label={Text("למי")},modifier=Modifier.fillMaxWidth());OutlinedTextField(pts,{pts=it.filter{c->c.isDigit()}},label={Text("נקודות")},modifier=Modifier.fillMaxWidth());Button(onClick={if(title.isNotBlank()){list.add(Task(title,who.ifBlank{"אני"},"אני",if(who.isBlank())0 else pts.toIntOrNull()?:0));title=""}},modifier=Modifier.fillMaxWidth()){Text("הוסף")}
  LazyColumn{items(list){t->Row(verticalAlignment=Alignment.CenterVertically){Checkbox(t.done,{v->if(v&&!t.done)award(t.points);t.done=v});Column{Text(t.title);Text(t.assignee+" · מאת "+t.giver+" · "+t.points+" נק׳",style=MaterialTheme.typography.bodySmall)}}}}
 }
}
@Composable fun Summary(award:(Int)->Unit,back:()->Unit){
 val qs=listOf("מה היה הדבר הכי טוב שקרה לי היום?","מה היה לי קשה היום?","על מה אני רוצה להגיד תודה?","מה אני רוצה לזכור מהיום?");val a=remember{qs.map{mutableStateOf("")}};var mood by remember{mutableStateOf("🙂")};var saved by remember{mutableStateOf(false)}
 Column(Modifier.fillMaxSize().padding(20.dp)){Header("סיכום יום",back);Row(Modifier.fillMaxWidth(),horizontalArrangement=Arrangement.SpaceEvenly){listOf("😭","😞","😐","🙂","😄","😂","🤩").forEach{e->TextButton(onClick={mood=e}){Text(e,fontSize=25.sp)}}};LazyColumn(Modifier.weight(1f)){items(qs.size){i->OutlinedTextField(a[i].value,{a[i].value=it},label={Text(qs[i])},modifier=Modifier.fillMaxWidth().padding(vertical=4.dp))}};Button(enabled=!saved,onClick={award(a.count{it.value.isNotBlank()}*5);saved=true},modifier=Modifier.fillMaxWidth()){Text(if(saved)"נשמר ✓" else "שמור")};Text("כל תשובה = 5 נקודות · התשובות פרטיות · האימוג׳י גלוי למשפחה")}
}
@Composable fun Shop(points:Int,products:List<Product>,spend:(Int)->Unit,back:()->Unit){
 Column(Modifier.fillMaxSize().padding(20.dp)){Header("חנות",back);Text("⭐ "+points+" נקודות");products.forEach{p->ElevatedCard(Modifier.fillMaxWidth().padding(vertical=6.dp)){Row(Modifier.padding(16.dp),verticalAlignment=Alignment.CenterVertically){Column(Modifier.weight(1f)){Text(p.title,fontWeight=FontWeight.Bold);Text(p.price.toString()+" נקודות")};Button(enabled=points>=p.price,onClick={spend(p.price)}){Text("קנייה")}}}};Text("רכישה יוצרת שובר. מימוש דורש אישור של בן משפחה נוסף.",style=MaterialTheme.typography.bodySmall)}
}
@Composable fun Decide(back:()->Unit){
 var minutes by remember{mutableIntStateOf(30)};var running by remember{mutableStateOf(false)};var left by remember{mutableLongStateOf(0)};var win by remember{mutableStateOf(false)}
 if(running){LaunchedEffect(Unit){win=Random.nextBoolean();object:CountDownTimer(minutes*60000L,1000){override fun onTick(ms:Long){left=ms/1000};override fun onFinish(){running=false}}.start()};Column(Modifier.fillMaxSize(),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Text(if(win)"🟢 ישש! ההחלטה שלך" else "🔴 באסה! הצד השני מחליט",fontSize=27.sp,fontWeight=FontWeight.Bold);Text(String.format("%02d:%02d",left/60,left%60),fontSize=70.sp);Text("אין ביטול עד סוף הטיימר")}}
 else Column(Modifier.fillMaxSize().padding(20.dp)){Header("מי מחליט?",back);Text("בחרו זמן",fontSize=22.sp);listOf(5,10,15,30,60).forEach{m->Row(verticalAlignment=Alignment.CenterVertically){RadioButton(selected=minutes==m,onClick={minutes=m});Text(m.toString()+" דקות")}};Button(onClick={running=true},modifier=Modifier.fillMaxWidth()){Text("5… 4… 3… 2… 1… מתחילים")}}
}
@Composable fun Admin(shop:MutableList<Product>,back:()->Unit){
 var name by remember{mutableStateOf("")};var price by remember{mutableStateOf("100")}
 Column(Modifier.fillMaxSize().padding(20.dp)){Header("ניהול המשפחה",back);Text("ניהול החנות",fontSize=22.sp,fontWeight=FontWeight.Bold);OutlinedTextField(name,{name=it},label={Text("פרס")},modifier=Modifier.fillMaxWidth());OutlinedTextField(price,{price=it.filter{c->c.isDigit()}},label={Text("מחיר")},modifier=Modifier.fillMaxWidth());Button(onClick={if(name.isNotBlank()){shop.add(Product(name,price.toIntOrNull()?:0));name=""}},modifier=Modifier.fillMaxWidth()){Text("הוסף לחנות")};Spacer(Modifier.height(20.dp));Text("מבוגרים מנהלים את החנות; כולם משתתפים בנקודות ובמימושים.")}
}
