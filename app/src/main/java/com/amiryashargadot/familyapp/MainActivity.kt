package com.amiryashargadot.familyapp

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.delay
import java.time.format.DateTimeFormatter
import java.util.Locale

class MainActivity:ComponentActivity(){override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);setContent{FamilyTheme{FamilyApp()}}}}
@Composable fun FamilyTheme(content: @Composable () -> Unit){CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl){MaterialTheme(colorScheme=lightColorScheme(primary=Color(0xFF286B60),secondary=Color(0xFF9A612C),background=Color(0xFFF7F6F1),surface=Color.White,primaryContainer=Color(0xFFDCEEE7)),content=content)}}
@Composable fun FamilyApp(vm:FamilyViewModel=viewModel()){
 var page by rememberSaveable{mutableStateOf("home")};var tick by remember{mutableLongStateOf(vm.now())}
 LaunchedEffect(vm.uid){while(true){tick=vm.now();vm.refreshDay();delay(1000)}}
 val permission=rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()){}
 val active=vm.me.map("activeDecision");val locked=((active["endsAt"] as? Number)?.toLong()?:0)>tick
 BackHandler(enabled=page!="home"||locked){if(!locked)page="home"}
 Surface(Modifier.fillMaxSize(),color=MaterialTheme.colorScheme.background){
  if(locked)ActiveDecision(vm,active,tick)else Column(Modifier.fillMaxSize().safeDrawingPadding()){
   if(vm.busy)LinearProgressIndicator(Modifier.fillMaxWidth())
   vm.error?.let{msg->Card(Modifier.fillMaxWidth().padding(12.dp),colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.errorContainer)){Column(Modifier.padding(12.dp)){Text(msg);Row{TextButton(onClick={vm.error=null}){Text("סגירה")};if(vm.profile==null)TextButton(onClick={vm.start()}){Text("ניסיון חוזר")}}}}}
   when{
    vm.loading->Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){CircularProgressIndicator()}
    vm.uid.isEmpty()->Column(Modifier.padding(24.dp)){Title("הבית שלנו","מתחברים לבית המשפחתי");Text("נדרש חיבור ראשוני לשירות כדי ליצור את הפרופיל.");Action("ניסיון חוזר"){vm.start()}}
    vm.profile==null->Onboarding(vm)
    else->{
     if(page!="home")Row(Modifier.fillMaxWidth().padding(horizontal=12.dp),verticalAlignment=Alignment.CenterVertically){TextButton(onClick={page="home"}){Text("‹ הבית")};Spacer(Modifier.weight(1f));Text("⭐ ${vm.me.n("balance")} נקודות",style=MaterialTheme.typography.labelLarge)}
     when(page){"home"->Home(vm,{page=it},{if(Build.VERSION.SDK_INT>=33)permission.launch(Manifest.permission.POST_NOTIFICATIONS)});"morning","evening"->RoutineScreen(vm,page);"tasks"->TasksScreen(vm);"summary"->SummaryScreen(vm);"shop"->ShopScreen(vm);"decide"->DecisionScreen(vm);"lists"->PrivateListsScreen(vm);"settings"->SettingsScreen(vm);"storeAdmin"->if(vm.isAdult)StoreAdmin(vm)else Text("אין הרשאת ניהול")}
    }
   }
  }
 }
 vm.recovery?.let{SecretDialog("קוד השחזור האישי שלך",it,"שמרו במקום פרטי. הקוד מאפשר לחזור לאותו חשבון במכשיר אחר, כולל הסיכומים הפרטיים."){vm.recovery=null}}
 vm.invitation?.let{SecretDialog("הזמנה למשפחה",it,"הקוד תקף ל־24 שעות ולשימוש אחד. הזינו אותו במסך ההצטרפות של בן המשפחה."){vm.invitation=null}}
}
@Composable fun Screen(content:@Composable ColumnScope.()->Unit){Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).imePadding().padding(horizontal=20.dp,vertical=12.dp),verticalArrangement=Arrangement.spacedBy(12.dp),content=content)}
@Composable fun Title(title:String,subtitle:String=""){Text(title,style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Bold);if(subtitle.isNotEmpty())Text(subtitle,style=MaterialTheme.typography.bodyMedium,color=MaterialTheme.colorScheme.onSurfaceVariant)}
@Composable fun Action(label:String,enabled:Boolean=true,onClick:()->Unit){Button(onClick,Modifier.fillMaxWidth().heightIn(min=48.dp),enabled=enabled,shape=RoundedCornerShape(16.dp)){Text(label)}}
@Composable fun Field(value:String,label:String,onChange:(String)->Unit){OutlinedTextField(value,onChange,label={Text(label)},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(14.dp))}
@Composable fun Block(content:@Composable ColumnScope.()->Unit){Card(Modifier.fillMaxWidth(),shape=RoundedCornerShape(20.dp),colors=CardDefaults.cardColors(containerColor=Color.White)){Column(Modifier.padding(16.dp),verticalArrangement=Arrangement.spacedBy(8.dp),content=content)}}
@OptIn(ExperimentalLayoutApi::class)
@Composable fun Choices(values:List<Pair<String,String>>,selected:String,onSelect:(String)->Unit){FlowRow(horizontalArrangement=Arrangement.spacedBy(6.dp)){values.forEach{(id,label)->FilterChip(selected==id,{onSelect(id)},label={Text(label)})}}}
@Composable fun SecretDialog(title:String,code:String,body:String,onClose:()->Unit){val clipboard=LocalClipboardManager.current;AlertDialog(onDismissRequest=onClose,title={Text(title)},text={Column(verticalArrangement=Arrangement.spacedBy(12.dp)){Text(body);androidx.compose.foundation.text.selection.SelectionContainer{CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr){Text(code)}}}},confirmButton={TextButton(onClick={clipboard.setText(AnnotatedString(code))}){Text("העתקת הקוד")}},dismissButton={TextButton(onClick=onClose){Text("שמרתי")}})}
@Composable fun Home(vm:FamilyViewModel,go:(String)->Unit,notifications:()->Unit){Screen{
 Text("${vm.me.s("avatar")} שלום, ${vm.me.s("name")}",style=MaterialTheme.typography.titleMedium)
 Title(vm.family.s("name","הבית שלנו"),vm.today().format(DateTimeFormatter.ofPattern("EEEE, d בMMMM",Locale("he"))))
 Card(colors=CardDefaults.cardColors(containerColor=MaterialTheme.colorScheme.primaryContainer),modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(20.dp)){Text("הנקודות שלי");Text("⭐ ${vm.me.n("balance")}",style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Bold)}}
 if(vm.cached)Text("מציגים מידע שמור. שינויים דורשים חיבור לשרת.",style=MaterialTheme.typography.bodySmall)
 listOf("morning" to "☀️  בוקר","evening" to "🌙  ערב","tasks" to "✓  משימות","summary" to "😊  סיכום יום","shop" to "🎁  החנות והדברים שלי","decide" to "🎲  מי מחליט?").forEach{(key,label)->Card(onClick={go(key)},modifier=Modifier.fillMaxWidth(),shape=RoundedCornerShape(18.dp)){Text(label,Modifier.padding(20.dp),style=MaterialTheme.typography.titleLarge)}}
 val pending=vm.vouchers.count{it.s("status")=="pending"&&it.s("owner")!=vm.uid&&it.n("expiresAt")>vm.now()};if(pending>0)Action("יש $pending בקשות מימוש שמחכות לכם"){go("shop")}
 if(vm.decisions.any{it.s("other")==vm.uid&&it.s("status")=="waiting"&&it.n("expiresAt")>vm.now()})Action("הזמינו אותך ל׳מי מחליט׳"){go("decide")}
 TextButton(onClick={go("lists")}){Text("🔒 הרשימות הפרטיות שלי")};TextButton(onClick={go("settings")}){Text(if(vm.isAdult)"הפרופיל וניהול המשפחה"else "הפרופיל שלי")};if(vm.isAdult)TextButton(onClick={go("storeAdmin")}){Text("ניהול החנות")};TextButton(onClick=notifications){Text("הפעלת התראות")}
}}
@Composable fun Onboarding(vm:FamilyViewModel){
 var mode by rememberSaveable{mutableStateOf("create")};var name by rememberSaveable{mutableStateOf("")};var avatar by rememberSaveable{mutableStateOf(avatars[0])};var role by rememberSaveable{mutableStateOf("adult")};var code by rememberSaveable{mutableStateOf("")};var familyName by rememberSaveable{mutableStateOf("הבית שלנו")};var step by rememberSaveable{mutableIntStateOf(0)};var routines by remember{mutableStateOf(initialRoutines())}
 Screen{Title("נעים להכיר 👋","המשפחה שלנו, קצת יותר יחד")
  if(step==0){Choices(listOf("create" to "משפחה חדשה","join" to "הצטרפות","restore" to "יש לי קוד שחזור"),mode){mode=it;code=""}
   if(mode=="restore"){Field(code,"קוד שחזור אישי"){code=it};Action("כניסה לחשבון שלי",!vm.busy&&code.isNotBlank()){vm.restore(code)}}else{
    Field(name,"איך קוראים לך?"){name=it.take(40)};Choices(avatars.map{it to it},avatar){avatar=it}
    if(mode=="join"){Field(code,"קוד הזמנה למשפחה"){code=it};Choices(listOf("adult" to "מבוגר/ת","child" to "ילד/ה"),role){role=it}}else Field(familyName,"שם המשפחה באפליקציה"){familyName=it.take(60)}
    Text(if(mode=="create")"מבוגר יוצר את המשפחה ומזמין את השאר."else "סוג המשתמש צריך להתאים להזמנה שקיבלתם.",style=MaterialTheme.typography.bodySmall)
    Action("לרוטינות שלי",name.isNotBlank()&&(mode=="create"||code.isNotBlank())){step=1}
   }
  }else{Text("הבוקר והערב שלי",style=MaterialTheme.typography.titleLarge);Text("אפשר לשנות, להוסיף או להסיר שלבים. אין נקודות על רוטינות.");RoutineEditor(routines){routines=it};Action("בואו ניכנס הביתה",!vm.busy&&routines.all{it.title.isNotBlank()&&it.days.isNotEmpty()}){vm.onboard(name,avatar,if(mode=="create")"adult"else role,if(mode=="join")code else "",familyName,routines)};TextButton(onClick={step=0}){Text("חזרה לפרטים")}}
 }
}
@OptIn(ExperimentalLayoutApi::class)
@Composable fun RoutineEditor(rows:List<RoutineStep>,onChange:(List<RoutineStep>)->Unit){
 rows.forEach{r->key(r.id){Block{
  Field(r.title,"השלב שלי"){title->onChange(rows.map{if(it.id==r.id)it.copy(title=title.take(120))else it})}
  Choices(listOf("morning" to "☀️ בוקר","evening" to "🌙 ערב"),r.kind){kind->onChange(rows.map{if(it.id==r.id)it.copy(kind=kind)else it})}
  FlowRow(horizontalArrangement=Arrangement.spacedBy(3.dp)){dayLabels.forEach{(d,label)->FilterChip(d in r.days,{onChange(rows.map{if(it.id==r.id)it.copy(days=if(d in r.days)r.days-d else r.days+d)else it})},label={Text(label)})}}
  Row{TextButton(onClick={onChange(rows.map{if(it.id==r.id)it.copy(days=(1..7).toList())else it})}){Text("כל יום")};TextButton(onClick={onChange(rows.map{if(it.id==r.id)it.copy(days=listOf(7,1,2,3,4))else it})}){Text("ימי לימודים")};TextButton(onClick={onChange(rows.filter{it.id!=r.id})}){Text("הסרה")}}
 }}}
 OutlinedButton(onClick={onChange(rows+RoutineStep())},modifier=Modifier.fillMaxWidth()){Text("＋ הוספת שלב")}
}
@Composable fun RoutineScreen(vm:FamilyViewModel,kind:String){var edit by rememberSaveable{mutableStateOf(false)};var rows by remember(vm.profile?.routines()){mutableStateOf(vm.profile?.routines()?:emptyList())}
 Screen{Title(if(kind=="morning")"בוקר טוב ☀️"else "ערב נעים 🌙","הרוטינה האישית שלי להיום")
  if(edit){RoutineEditor(rows){rows=it};Action("שמירת הרוטינות",!vm.busy&&rows.all{it.title.isNotBlank()&&it.days.isNotEmpty()}){vm.act("routines",mapOf("routines" to rows.map{it.payload()})){edit=false}}}
  else{val steps=visibleSteps(vm.profile?.routines()?:emptyList(),kind,vm.today());val done=vm.routineDays.find{it.s("kind")==kind}?.map("steps")?:emptyMap();if(steps.isEmpty())Text("אין שלבים מתוכננים להיום. אפשר להוסיף בעריכה.")
   steps.forEach{r->Block{Row(verticalAlignment=Alignment.CenterVertically){Checkbox((done[r.id] as? Map<*,*>)?.get("done")==true,{value->vm.act("routineCheck",mapOf("id" to r.id,"kind" to kind,"done" to value))},enabled=!vm.busy);Text(r.title,style=MaterialTheme.typography.titleMedium)}}}
   Text("כל יום הוא התחלה חדשה. סימוני הביצוע נשמרים בנפרד מהרוטינה.",style=MaterialTheme.typography.bodySmall);TextButton(onClick={edit=true}){Text("עריכת בוקר וערב")}
  }
 }
}
