package com.amiryashargadot.familyapp
import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.compose.runtime.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.*
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.FirebaseFunctionsException
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.*
import java.util.UUID

class FamilyViewModel(app:Application):AndroidViewModel(app){
 private val prefs=app.getSharedPreferences("family_clock",0)
 private val configured=FirebaseApp.getApps(app).isNotEmpty()
 private val auth by lazy{FirebaseAuth.getInstance()}
 private val db by lazy{FirebaseFirestore.getInstance()}
 private val functions by lazy{FirebaseFunctions.getInstance("europe-west1")}
 private val listeners=mutableListOf<ListenerRegistration>()
 private var moodListener:ListenerRegistration?=null
 private var summaryListener:ListenerRegistration?=null
 private var itemListener:ListenerRegistration?=null
 private var routineListener:ListenerRegistration?=null
 private var anchor=SystemClock.elapsedRealtime()
 private var epoch=System.currentTimeMillis()+prefs.getLong("offset",0)
 var uid by mutableStateOf("");private set
 var profile by mutableStateOf<Record?>(null);private set
 var family by mutableStateOf(Record());private set
 var members by mutableStateOf<List<Record>>(emptyList());private set
 var tasks by mutableStateOf<List<Record>>(emptyList());private set
 var products by mutableStateOf<List<Record>>(emptyList());private set
 var vouchers by mutableStateOf<List<Record>>(emptyList());private set
 var decisions by mutableStateOf<List<Record>>(emptyList());private set
 var privateLists by mutableStateOf<List<Record>>(emptyList());private set
 var listItems by mutableStateOf<List<Record>>(emptyList());private set
 var moodRecords by mutableStateOf<List<Record>>(emptyList());private set
 var routineDays by mutableStateOf<List<Record>>(emptyList());private set
 var ledger by mutableStateOf<List<Record>>(emptyList());private set
 var summary by mutableStateOf<Record?>(null);private set
 var summaryLoaded by mutableStateOf(false);private set
 var loading by mutableStateOf(true);private set
 var busy by mutableStateOf(false);private set
 var error by mutableStateOf<String?>(null)
 var notice by mutableStateOf<String?>(null)
 var recovery by mutableStateOf<String?>(null)
 var invitation by mutableStateOf<String?>(null)
 var cached by mutableStateOf(false);private set
 private var boundFamily=""
 private var boundDay=""
 val me:Record get()=members.find{it.id==uid}?:Record()
 val isAdult:Boolean get()=me.s("role")=="adult"
 fun now()=epoch+SystemClock.elapsedRealtime()-anchor
 fun today():LocalDate=Instant.ofEpochMilli(now()).atZone(familyZone).toLocalDate()
 fun memberName(id:String)=members.find{it.id==id}?.s("name")?:"בן משפחה"
 init{start()}
 fun start(){
  if(!configured){loading=false;error="חסרה הגדרת החיבור לשירות. יש להתקין את קובץ האפליקציה המחובר לפרויקט המשפחתי.";return}
  loading=true;error=null
  viewModelScope.launch{try{if(auth.currentUser==null)auth.signInAnonymously().await();bindUser();refreshClock();registerToken()}catch(e:Exception){loading=false;error=friendly(e)}}
 }
 private fun bindUser(){
  listeners.forEach{it.remove()};listeners.clear();boundFamily="";boundDay="";uid=auth.currentUser!!.uid
  listeners+=db.document("users/$uid").addSnapshotListener{s,e->
   if(e!=null){loading=false;error=friendly(e);return@addSnapshotListener};if(s==null)return@addSnapshotListener
   profile=if(s.exists())Record(s.id,s.data?:emptyMap())else null;loading=false;cached=s.metadata.isFromCache
   val fid=profile?.s("familyId")?:"";if(fid.isNotEmpty()&&boundFamily!=fid)bindFamily(fid)
  }
 }
 private fun listen(path:String,setter:(List<Record>)->Unit){listeners+=db.collection(path).addSnapshotListener(MetadataChanges.INCLUDE){s,e->if(e!=null){error=friendly(e);return@addSnapshotListener};if(s!=null){setter(s.documents.map{Record(it.id,it.data?:emptyMap())});cached=s.metadata.isFromCache}}}
 private fun bindFamily(fid:String){
  boundFamily=fid
  listeners+=db.document("families/$fid").addSnapshotListener{s,e->if(e!=null)error=friendly(e)else if(s!=null)family=Record(s.id,s.data?:emptyMap())}
  listen("families/$fid/members"){members=it;val active=me.map("activeDecision");val end=(active["endsAt"] as? Number)?.toLong()?:0;if(end>now())FamilyNotifications.schedule(getApplication(),active["id"].toString(),end,now())}
  listen("families/$fid/tasks"){tasks=it.sortedByDescending{r->r.n("createdAt")}}
  listen("families/$fid/products"){products=it}
  listen("families/$fid/vouchers"){vouchers=it.sortedByDescending{r->r.n("boughtAt")}}
  listen("families/$fid/decisions"){decisions=it}
  listeners+=db.collection("privateLists").whereArrayContains("participantIds",uid).addSnapshotListener{s,e->if(e!=null)error=friendly(e)else if(s!=null)privateLists=s.documents.map{Record(it.id,it.data?:emptyMap())}}
  listeners+=db.collection("users/$uid/ledger").orderBy("createdAt",Query.Direction.DESCENDING).limit(40).addSnapshotListener{s,e->if(e!=null)error=friendly(e)else if(s!=null)ledger=s.documents.map{Record(it.id,it.data?:emptyMap())}}
  refreshDay();watchMonth(YearMonth.from(today()))
 }
 fun refreshDay(){if(boundFamily.isEmpty()||boundDay==today().toString())return;boundDay=today().toString();routineListener?.remove();routineListener=db.collection("users/$uid/routineDays").whereEqualTo("date",boundDay).addSnapshotListener{s,e->if(e!=null)error=friendly(e)else if(s!=null)routineDays=s.documents.map{Record(it.id,it.data?:emptyMap())}}}
 fun watchMonth(month:YearMonth){if(boundFamily.isEmpty())return;moodListener?.remove();moodRecords=emptyList();moodListener=db.collection("families/$boundFamily/moods").whereGreaterThanOrEqualTo("date",month.atDay(1).toString()).whereLessThanOrEqualTo("date",month.atEndOfMonth().toString()).addSnapshotListener{s,e->if(e!=null)error=friendly(e)else if(s!=null)moodRecords=s.documents.map{Record(it.id,it.data?:emptyMap())}}}
 fun watchSummary(date:LocalDate){summaryListener?.remove();summary=null;summaryLoaded=false;summaryListener=db.document("users/$uid/summaries/$date").addSnapshotListener{s,e->if(e!=null){error=friendly(e);summaryLoaded=true}else if(s!=null){summary=if(s.exists())Record(s.id,s.data?:emptyMap())else null;summaryLoaded=true}}}
 fun watchList(id:String){itemListener?.remove();listItems=emptyList();itemListener=db.collection("privateLists/$id/items").addSnapshotListener{s,e->if(e!=null)error=friendly(e)else if(s!=null)listItems=s.documents.map{Record(it.id,it.data?:emptyMap())}.sortedBy{it.b("done")}}}
 suspend fun refreshClock(){try{val before=SystemClock.elapsedRealtime();val r=call("clock");val after=SystemClock.elapsedRealtime();epoch=(r["now"] as Number).toLong()+(after-before)/2;anchor=after;prefs.edit().putLong("offset",epoch-System.currentTimeMillis()).apply()}catch(_:Exception){}}
 @Suppress("UNCHECKED_CAST") private suspend fun call(action:String,args:Map<String,Any?> = emptyMap()):Map<String,Any?> = functions.getHttpsCallable("familyApi").call(args+mapOf("action" to action)).await().getData() as? Map<String,Any?>?:emptyMap()
 fun act(action:String,args:Map<String,Any?> = emptyMap(),onDone:(Map<String,Any?>)->Unit={}){if(busy)return;busy=true;error=null;notice=null;viewModelScope.launch{try{val r=call(action,args);notice="נשמר";onDone(r)}catch(e:Exception){error=friendly(e)}finally{busy=false}}}
 fun onboard(name:String,avatar:String,role:String,code:String,familyName:String,routines:List<RoutineStep>){act("onboard",mapOf("name" to name,"avatar" to avatar,"role" to role,"inviteCode" to code.trim(),"familyName" to familyName,"routines" to routines.map{it.payload()})){recovery=it["recovery"] as? String}}
 fun restore(code:String){if(busy)return;busy=true;error=null;viewModelScope.launch{try{val r=call("restore",mapOf("secret" to code.trim()));auth.signInWithCustomToken(r["token"] as String).await();bindUser();registerToken()}catch(e:Exception){error=friendly(e)}finally{busy=false}}}
 private fun registerToken(){FirebaseMessaging.getInstance().token.addOnSuccessListener{FamilyMessagingService.saveToken(it)}}
 fun requestId()=UUID.randomUUID().toString()
 override fun onCleared(){listeners.forEach{it.remove()};moodListener?.remove();summaryListener?.remove();itemListener?.remove();routineListener?.remove();super.onCleared()}
 private fun friendly(e:Exception):String{
  if(e is FirebaseFunctionsException)return when(e.code){FirebaseFunctionsException.Code.NOT_FOUND->e.message?.takeUnless{it=="NOT_FOUND"}?:"שירות המשפחה עדיין לא הופעל. יש להשלים את פריסת השרת.";FirebaseFunctionsException.Code.UNAVAILABLE->"אין כרגע חיבור לשרת. הפעולה לא נשמרה; אפשר לנסות שוב.";FirebaseFunctionsException.Code.INTERNAL->"השרת לא השלים את הפעולה. בהתקנה ראשונה נדרשת הפעלת השרת.";else->e.message?:"הפעולה לא נשמרה"}
  if(e is FirebaseFirestoreException&&e.code==FirebaseFirestoreException.Code.PERMISSION_DENIED)return "אין הרשאה לקריאת הנתונים. בהתקנה ראשונה יש לפרוס את כללי הגישה של המשפחה."
  val msg=e.message.orEmpty();if(msg.contains("CONFIGURATION_NOT_FOUND")||msg.contains("operation is not allowed",true)||msg.contains("ADMIN_ONLY_OPERATION"))return "הכניסה עדיין לא הופעלה בפרויקט. צריך להפעיל כניסה אנונימית בהגדרות Firebase."
  if(msg.contains("network",true))return "אין חיבור לרשת. נסו שוב כשתהיה קליטה."
  return "לא ניתן להתחבר לשירות המשפחתי כרגע. נסו שוב."
 }
}
