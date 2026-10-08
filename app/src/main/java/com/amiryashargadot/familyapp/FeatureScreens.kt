package com.amiryashargadot.familyapp
import androidx.compose.foundation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.*
import java.time.*
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable fun TasksScreen(vm:FamilyViewModel){
 var title by rememberSaveable{mutableStateOf("")};var who by rememberSaveable{mutableStateOf(vm.uid)};var points by rememberSaveable{mutableStateOf("10")};var filter by rememberSaveable{mutableStateOf(vm.uid)};var requestId by rememberSaveable{mutableStateOf(vm.requestId())}
 Screen{Title("משימות","כל אחד יכול לעזור, וכל אחד יכול לבקש")
  Block{Field(title,"מה צריך לעשות?"){title=it.take(120)};Text("למי המשימה?");Choices(vm.members.map{it.id to "${it.s("avatar")} ${it.s("name")}"},who){who=it};if(who!=vm.uid)Field(points,"כמה נקודות?"){points=it.filter(Char::isDigit).take(5)}else Text("משימה לעצמי · ללא נקודות")
   Action("הוספת משימה",!vm.busy&&title.isNotBlank()&&(who==vm.uid||(points.toIntOrNull()?:-1) in 0..10000)){vm.act("createTask",mapOf("title" to title,"assignee" to who,"points" to effectiveTaskPoints(vm.uid,who,points.toIntOrNull()?:0),"requestId" to requestId)){title="";requestId=vm.requestId()}}
  }
  Choices(listOf("all" to "כל המשפחה")+vm.members.map{it.id to it.s("name")},filter){filter=it}
  val visible=vm.tasks.filter{filter=="all"||it.s("assignee")==filter}.sortedBy{it.b("done")};if(visible.isEmpty())Text("אין משימות ברשימה הזאת כרגע.")
  visible.forEach{t->Block{Row(verticalAlignment=Alignment.CenterVertically){Checkbox(t.b("done"),{if(it)vm.act("completeTask",mapOf("id" to t.id))},enabled=!vm.busy&&!t.b("done")&&t.s("assignee")==vm.uid);Column(Modifier.weight(1f)){Text(t.s("title"),fontWeight=FontWeight.Bold);Text("ל${vm.memberName(t.s("assignee"))} · מאת ${vm.memberName(t.s("giver"))}",style=MaterialTheme.typography.bodySmall);Text("${t.n("points")} נקודות${if(t.b("done"))" · הושלם ✓"else ""}")}};if(t.s("giver")==vm.uid&&!t.b("done"))TextButton(onClick={vm.act("deleteTask",mapOf("id" to t.id))},enabled=!vm.busy){Text("מחיקת המשימה")}}}
 }
}
@Composable fun SummaryScreen(vm:FamilyViewModel){
 var monthText by rememberSaveable{mutableStateOf(YearMonth.from(vm.today()).toString())};val month=YearMonth.parse(monthText)
 var dateText by rememberSaveable{mutableStateOf(vm.today().toString())};val date=LocalDate.parse(dateText)
 var mood by rememberSaveable(dateText){mutableStateOf("🙂")};var answers by remember(dateText){mutableStateOf(questions.associate{it.first to ""})};var loadedDate by remember{mutableStateOf("")}
 LaunchedEffect(monthText){vm.watchMonth(month)};LaunchedEffect(dateText){loadedDate="";vm.watchSummary(date)}
 LaunchedEffect(vm.summaryLoaded,vm.summary,dateText){if(vm.summaryLoaded&&loadedDate!=dateText){answers=questions.associate{it.first to (vm.summary?.map("answers")?.get(it.first) as? String?:"")};mood=vm.summary?.s("mood","🙂")?:"🙂";loadedDate=dateText}}
 Screen{Title("איך היה היום?","האימוג׳י למשפחה. המילים נשארות רק אצלך.")
  Row(Modifier.fillMaxWidth(),verticalAlignment=Alignment.CenterVertically,horizontalArrangement=Arrangement.SpaceBetween){TextButton(onClick={monthText=month.minusMonths(1).toString()}){Text("הקודם")};Text(month.atDay(1).format(DateTimeFormatter.ofPattern("MMMM yyyy",Locale("he"))),fontWeight=FontWeight.Bold);TextButton(onClick={monthText=month.plusMonths(1).toString()}){Text("הבא")}}
  CalendarGrid(month,date,vm.moodRecords){dateText=it.toString()}
  val dayMoods=vm.moodRecords.filter{it.s("date")==dateText};if(dayMoods.isNotEmpty())Block{dayMoods.forEach{Text("${it.s("mood")}  ${vm.memberName(it.s("uid"))}")}}
  Row{TextButton(onClick={dateText=vm.today().toString();monthText=YearMonth.from(vm.today()).toString()}){Text("היום")};TextButton(onClick={dateText=vm.today().minusDays(1).toString();monthText=YearMonth.from(vm.today().minusDays(1)).toString()}){Text("אתמול")}}
  Text("הסיכום האישי שלי · ${date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy"))}",style=MaterialTheme.typography.titleMedium)
  if(!vm.summaryLoaded||loadedDate!=dateText)CircularProgressIndicator()
  else if(editableSummary(date,vm.today())){Choices(moods.map{it to it},mood){mood=it};questions.forEach{(id,q)->Field(answers[id]?:"",q){answers=answers+(id to it.take(4000))}};Text("אפשר להשאיר שאלות ריקות. כל שאלה מזכה ב־5 נקודות פעם אחת ביום.",style=MaterialTheme.typography.bodySmall);Action("שמירת הסיכום",!vm.busy){vm.act("summary",mapOf("date" to dateText,"mood" to mood,"answers" to answers)){vm.notice="הסיכום נשמר · ${(it["awarded"] as? Number)?.toInt()?:0} נקודות חדשות"}};vm.notice?.let{Text(it,color=MaterialTheme.colorScheme.primary)}}
  else{if(vm.summary==null)Text("לא נשמר סיכום ליום הזה.")else Block{Text(vm.summary?.s("mood")?:"");questions.forEach{(id,q)->Text(q,fontWeight=FontWeight.Bold);Text((vm.summary?.map("answers")?.get(id) as? String)?.ifBlank{"דילגתי"}?:"דילגתי")}};Text("סיכומי עבר זמינים לקריאה. אפשר למלא רק היום או אתמול.",style=MaterialTheme.typography.bodySmall)}
 }
}
@Composable fun CalendarGrid(month:YearMonth,selected:LocalDate,moods:List<Record>,onPick:(LocalDate)->Unit){val offset=month.atDay(1).dayOfWeek.value%7
 Column(verticalArrangement=Arrangement.spacedBy(4.dp)){Row{dayLabels.forEach{(_,label)->Box(Modifier.weight(1f),contentAlignment=Alignment.Center){Text(label,style=MaterialTheme.typography.labelSmall)}}}
  val cells=(List(offset){0}+(1..month.lengthOfMonth()).toList()).let{it+List((7-it.size%7)%7){0}}
  cells.chunked(7).forEach{week->Row(horizontalArrangement=Arrangement.spacedBy(3.dp)){week.forEach{day->if(day==0)Spacer(Modifier.weight(1f))else{val date=month.atDay(day);val emoji=moods.filter{it.s("date")==date.toString()}.map{it.s("mood")};Column(Modifier.weight(1f).heightIn(min=62.dp).background(if(date==selected)MaterialTheme.colorScheme.primaryContainer else Color.White,RoundedCornerShape(10.dp)).clickable{onPick(date)}.padding(3.dp),horizontalAlignment=Alignment.CenterHorizontally){Text(day.toString(),fontWeight=if(date==selected)FontWeight.Bold else FontWeight.Normal);Text(emoji.take(2).joinToString(""),fontSize=13.sp);if(emoji.size>2)Text("+${emoji.size-2}",fontSize=10.sp)}}}}}
 }
}
@Composable fun ShopScreen(vm:FamilyViewModel){var tab by rememberSaveable{mutableStateOf("store")};var buying by remember{mutableStateOf<Record?>(null)};var requestId by rememberSaveable{mutableStateOf(vm.requestId())}
 Screen{Title("משהו טוב לעצמי","⭐ ${vm.me.n("balance")} נקודות לשימוש");Choices(listOf("store" to "החנות","mine" to "הדברים שלי","requests" to "בקשות מימוש"),tab){tab=it}
  when(tab){
   "store"->{val rows=vm.products.filter{it.b("available")};if(rows.isEmpty())Text("החנות עוד מתמלאת. מבוגר יכול להוסיף פרסים.");rows.forEach{p->Block{Text("${p.s("icon")} ${p.s("title")}",style=MaterialTheme.typography.titleLarge);Text(p.s("description"));Text("${p.n("price")} נקודות · תוקף ${p.n("validDays")} ימים לאחר קנייה");Action("קנייה",!vm.busy&&vm.me.n("balance")>=p.n("price")){buying=p;requestId=vm.requestId()}}}}
   "mine"->{val rows=vm.vouchers.filter{it.s("owner")==vm.uid};if(rows.isEmpty())Text("השוברים שתקנו יופיעו כאן.");rows.forEach{v->Block{Text("${v.s("icon")} ${v.s("title")}",style=MaterialTheme.typography.titleLarge);Text("בתוקף עד ${Instant.ofEpochMilli(v.n("expiresAt")).atZone(familyZone).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"))}");when{v.s("status")=="redeemed"->Text("מומש ✓ · באישור ${vm.memberName(v.s("approvedBy"))}");v.n("expiresAt")<=vm.now()->Text("פג תוקף");v.s("status")=="pending"->Text("ממתין לאישור של בן משפחה");else->Action("בקשת מימוש",!vm.busy){vm.act("redeem",mapOf("id" to v.id))}}}}}
   "requests"->{val rows=vm.vouchers.filter{it.s("status")=="pending"&&it.s("owner")!=vm.uid&&it.n("expiresAt")>vm.now()};if(rows.isEmpty())Text("אין בקשות שממתינות לאישור.");rows.forEach{v->Block{Text("${vm.memberName(v.s("owner"))} רוצה לממש:");Text("${v.s("icon")} ${v.s("title")}",style=MaterialTheme.typography.titleLarge);Action("מאשר/ת",!vm.busy){vm.act("approve",mapOf("id" to v.id))};TextButton(onClick={vm.act("reject",mapOf("id" to v.id))},enabled=!vm.busy&&vm.uid !in v.strings("rejectedBy")){Text(if(vm.uid in v.strings("rejectedBy"))"בחרת לא לאשר כרגע"else "לא מאשר/ת כרגע")};Text("אישור אחד מספיק. בחירה לא לאשר משאירה את הבקשה פתוחה לאחרים.",style=MaterialTheme.typography.bodySmall)}}}
  }
 }
 buying?.let{p->AlertDialog(onDismissRequest={if(!vm.busy)buying=null},title={Text("לקנות את השובר?")},text={Text("${p.s("title")} · ${p.n("price")} נקודות. השובר יישמר ב׳הדברים שלי׳.")},confirmButton={TextButton(enabled=!vm.busy,onClick={vm.act("buy",mapOf("id" to p.id,"requestId" to requestId)){buying=null;tab="mine"}}){Text("קנייה")}},dismissButton={TextButton(onClick={buying=null},enabled=!vm.busy){Text("חזרה")}})}
}
@Composable fun PrivateListsScreen(vm:FamilyViewModel){var selected by rememberSaveable{mutableStateOf("")};var title by rememberSaveable{mutableStateOf("")};var other by rememberSaveable{mutableStateOf("")};var item by rememberSaveable{mutableStateOf("")};LaunchedEffect(selected){if(selected.isNotEmpty())vm.watchList(selected)}
 Screen{Title("רק בינינו 🔒","רשימות משותפות לשני אנשים, בלי נקודות")
  if(selected.isEmpty()){
   vm.privateLists.forEach{r->Card(onClick={selected=r.id},modifier=Modifier.fillMaxWidth()){Column(Modifier.padding(18.dp)){Text(r.s("title"),style=MaterialTheme.typography.titleLarge);Text(r.strings("participantIds").joinToString(" ו"){vm.memberName(it)})}}}
   Block{Text("רשימה חדשה",style=MaterialTheme.typography.titleMedium);Field(title,"שם הרשימה"){title=it.take(120)};Choices(vm.members.filter{it.id!=vm.uid}.map{it.id to it.s("name")},other){other=it};Action("יצירת רשימה פרטית",!vm.busy&&title.isNotBlank()&&other.isNotBlank()){vm.act("createList",mapOf("title" to title,"other" to other)){selected=it["id"] as? String?:"";title=""}}}
  }else{TextButton(onClick={selected=""}){Text("לכל הרשימות הפרטיות")};Text(vm.privateLists.find{it.id==selected}?.s("title")?:"",style=MaterialTheme.typography.titleLarge);Field(item,"פריט חדש"){item=it.take(120)};Action("הוספה",!vm.busy&&item.isNotBlank()){vm.act("listItem",mapOf("listId" to selected,"title" to item,"done" to false)){item=""}};vm.listItems.forEach{r->Block{Row(verticalAlignment=Alignment.CenterVertically){Checkbox(r.b("done"),{done->vm.act("listItem",mapOf("listId" to selected,"id" to r.id,"title" to r.s("title"),"done" to done))},enabled=!vm.busy);Text(r.s("title"),Modifier.weight(1f));TextButton(onClick={vm.act("deleteListItem",mapOf("listId" to selected,"id" to r.id))},enabled=!vm.busy){Text("מחיקה")}}}}}
 }
}
@Composable fun DecisionScreen(vm:FamilyViewModel){var minutes by rememberSaveable{mutableStateOf("30")};var other by rememberSaveable{mutableStateOf("")};var invitationId by rememberSaveable{mutableStateOf(vm.requestId())}
 Screen{Title("מי מחליט? 🎲","שני אנשים. הגרלה אחת. קצת שקט לוויכוח.")
  vm.decisions.filter{it.s("status")=="waiting"&&it.n("expiresAt")>vm.now()&&it.s("other")==vm.uid}.forEach{d->Block{Text("${vm.memberName(d.s("creator"))} מזמין/ה אותך",style=MaterialTheme.typography.titleLarge);Text("${d.n("minutes")} דקות. ברגע שמאשרים, ההגרלה מתחילה ואי אפשר לבטל או להגריל שוב.");Action("מתחברים ומגרילים",!vm.busy){vm.act("decisionAccept",mapOf("id" to d.id))}}}
  Block{Text("את מי מזמינים?");Choices(vm.members.filter{it.id!=vm.uid}.map{it.id to "${it.s("avatar")} ${it.s("name")}"},other){other=it};Text("לכמה זמן?");Choices(listOf(5,10,15,30,45,60).map{it.toString() to "$it דקות"},minutes){minutes=it};Field(minutes,"מספר דקות · עד 180"){minutes=it.filter(Char::isDigit).take(3)};Action("שליחת הזמנה",!vm.busy&&other.isNotBlank()&&(minutes.toIntOrNull()?:0) in 1..180){vm.act("decisionInvite",mapOf("other" to other,"minutes" to minutes.toInt(),"requestId" to invitationId)){invitationId=vm.requestId()}}}
  vm.decisions.filter{it.s("creator")==vm.uid&&it.s("status")=="waiting"&&it.n("expiresAt")>vm.now()}.forEach{Text("ממתינים ל${vm.memberName(it.s("other"))} · ${it.n("minutes")} דקות")}
  Text("הצד השני פותח כאן את ׳מי מחליט׳ ומאשר. ההזמנה תקפה ל־5 דקות. בזמן ההחלטה שאר האפליקציה נעולה לשניכם.")
 }
}
@Composable fun ActiveDecision(vm:FamilyViewModel,active:Map<String,Any?>,now:Long){val starts=(active["startsAt"] as? Number)?.toLong()?:0;val end=(active["endsAt"] as? Number)?.toLong()?:0;val winner=active["winner"] as? String?:"";val mine=winner==vm.uid;val countdown=now<starts;val left=((end-now+999)/1000).coerceAtLeast(0);val color=if(countdown)Color(0xFF304E6B)else if(mine)Color(0xFF206C4A)else Color(0xFF923D42)
 Column(Modifier.fillMaxSize().background(color).safeDrawingPadding().padding(24.dp),horizontalAlignment=Alignment.CenterHorizontally,verticalArrangement=Arrangement.Center){Text(if(countdown)"רגע… מי מחליט?"else if(mine)"ישש! ההחלטה שלך!"else "הפעם ${vm.memberName(winner)} מחליט/ה",color=Color.White,style=MaterialTheme.typography.headlineLarge,fontWeight=FontWeight.Bold);Spacer(Modifier.height(32.dp));Text(if(countdown)((starts-now+999)/1000).coerceIn(1,5).toString()else "%02d:%02d".format(left/60,left%60),color=Color.White,fontSize=64.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(24.dp));Text("${(active["minutes"] as? Number)?.toInt()?:0} דקות · ההחלטה משותפת לשני המכשירים",color=Color.White);Text("כשהזמן נגמר, חוזרים לדמוקרטיה.",color=Color.White,modifier=Modifier.padding(top=12.dp))}
}
@Composable fun SettingsScreen(vm:FamilyViewModel){var name by rememberSaveable{mutableStateOf(vm.me.s("name"))};var avatar by rememberSaveable{mutableStateOf(vm.me.s("avatar"))}
 Screen{Title("הפרופיל שלי");Field(name,"השם שלי"){name=it.take(40)};Choices(avatars.map{it to it},avatar){avatar=it};Action("שמירת הפרופיל",!vm.busy&&name.isNotBlank()){vm.act("profile",mapOf("name" to name,"avatar" to avatar))}
  Block{Text("מעבר למכשיר אחר",style=MaterialTheme.typography.titleMedium);Text("קוד שחזור מאפשר לפתוח את אותו חשבון במכשיר אחר. שמרו אותו פרטי. יצירת קוד חדש מבטלת את הקוד הקודם.");TextButton(onClick={vm.act("rotateRecovery"){vm.recovery=it["recovery"] as? String}},enabled=!vm.busy){Text("יצירת קוד שחזור")}}
  if(vm.isAdult)Block{Text("הזמנה למשפחה",style=MaterialTheme.typography.titleLarge);Text("קוד נפרד לכל אדם. הזמנת מבוגר מעניקה גם ניהול חנות והזמנות.");Action("הזמנת ילד/ה",!vm.busy){vm.act("invite",mapOf("role" to "child")){vm.invitation=it["code"] as? String}};OutlinedButton(onClick={vm.act("invite",mapOf("role" to "adult")){vm.invitation=it["code"] as? String}},enabled=!vm.busy,modifier=Modifier.fillMaxWidth()){Text("הזמנת מבוגר/ת")}}
  Text("בני המשפחה",style=MaterialTheme.typography.titleLarge);vm.members.forEach{Text("${it.s("avatar")} ${it.s("name")} · ${if(it.s("role")=="adult")"מבוגר/ת"else "ילד/ה"}")}
  Text("תנועות הנקודות האחרונות שלי",style=MaterialTheme.typography.titleMedium);vm.ledger.forEach{r->Text("${if(r.n("delta")>0)"+"else ""}${r.n("delta")} נקודות · יתרה ${r.n("balanceAfter")}")};Text("גרסה ${BuildConfig.VERSION_NAME}",style=MaterialTheme.typography.bodySmall)
 }
}
@Composable fun StoreAdmin(vm:FamilyViewModel){var id by rememberSaveable{mutableStateOf("")};var title by rememberSaveable{mutableStateOf("")};var description by rememberSaveable{mutableStateOf("")};var icon by rememberSaveable{mutableStateOf("🎁")};var price by rememberSaveable{mutableStateOf("100")};var days by rememberSaveable{mutableStateOf("7")};var available by rememberSaveable{mutableStateOf(true)};var deleting by remember{mutableStateOf<Record?>(null)}
 Screen{Title("ניהול החנות");Block{Text(if(id.isBlank())"מוצר חדש"else "עריכת מוצר",style=MaterialTheme.typography.titleLarge);Field(title,"שם המוצר"){title=it.take(120)};Field(description,"תיאור"){description=it.take(500)};Choices(listOf("🎁","🎬","🌙","🍕","🎮","🍦","🎨","🎵").map{it to it},icon){icon=it};Field(price,"מחיר בנקודות"){price=it.filter(Char::isDigit).take(6)};Field(days,"תוקף בימים לאחר רכישה"){days=it.filter(Char::isDigit).take(3)};Row(verticalAlignment=Alignment.CenterVertically){Switch(available,{available=it});Text("זמין לקנייה")};Action("שמירת מוצר",!vm.busy&&title.isNotBlank()&&(price.toIntOrNull()?:0) in 1..100000&&(days.toIntOrNull()?:0) in 1..365){vm.act("saveProduct",mapOf("id" to id,"title" to title,"description" to description,"icon" to icon,"price" to price.toInt(),"validDays" to days.toInt(),"available" to available)){id="";title="";description=""}};if(id.isNotBlank())TextButton(onClick={id="";title="";description=""}){Text("יצירת מוצר אחר")}}
  vm.products.forEach{p->Block{Text("${p.s("icon")} ${p.s("title")} · ${p.n("price")} נקודות");Text(if(p.b("available"))"זמין"else "מוסתר מהחנות");Row{TextButton(onClick={id=p.id;title=p.s("title");description=p.s("description");icon=p.s("icon");price=p.n("price").toString();days=p.n("validDays").toString();available=p.b("available")}){Text("עריכה")};TextButton(onClick={deleting=p}){Text("הסרה")}}}}
 }
 deleting?.let{p->AlertDialog(onDismissRequest={deleting=null},title={Text("להסיר את המוצר?")},text={Text("${p.s("title")} יוסר מהחנות. שוברים שכבר נקנו נשמרים.")},confirmButton={TextButton(onClick={vm.act("deleteProduct",mapOf("id" to p.id)){deleting=null}},enabled=!vm.busy){Text("הסרה")}},dismissButton={TextButton(onClick={deleting=null}){Text("חזרה")}})}
}
