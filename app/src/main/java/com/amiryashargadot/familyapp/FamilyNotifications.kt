package com.amiryashargadot.familyapp
import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.security.MessageDigest
import java.util.concurrent.TimeUnit
object FamilyNotifications{
 fun show(context:Context,title:String,body:String,tag:String){val manager=context.getSystemService(NotificationManager::class.java);manager.createNotificationChannel(NotificationChannel("family","המשפחה שלנו",NotificationManager.IMPORTANCE_DEFAULT));if(Build.VERSION.SDK_INT>=33&&ContextCompat.checkSelfPermission(context,Manifest.permission.POST_NOTIFICATIONS)!=PackageManager.PERMISSION_GRANTED)return;val intent=PendingIntent.getActivity(context,0,Intent(context,MainActivity::class.java),PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE);manager.notify(tag,1,NotificationCompat.Builder(context,"family").setSmallIcon(R.drawable.ic_family).setContentTitle(title).setContentText(body).setContentIntent(intent).setAutoCancel(true).build())}
 fun schedule(context:Context,id:String,end:Long,now:Long){val work=OneTimeWorkRequestBuilder<DecisionEndWorker>().setInitialDelay((end-now).coerceAtLeast(0),TimeUnit.MILLISECONDS).setInputData(workDataOf("id" to id)).build();WorkManager.getInstance(context).enqueueUniqueWork("decision_$id",ExistingWorkPolicy.KEEP,work)}
}
class DecisionEndWorker(context:Context,params:WorkerParameters):Worker(context,params){override fun doWork():Result{FamilyNotifications.show(applicationContext,"נגמר הזמן!","חוזרים לדמוקרטיה.","decision_${inputData.getString("id")}");return Result.success()}}
class FamilyMessagingService:FirebaseMessagingService(){
 override fun onNewToken(token:String){saveToken(token)}
 override fun onMessageReceived(message:RemoteMessage){FamilyNotifications.show(this,message.notification?.title?:"המשפחה שלנו",message.notification?.body?:"יש עדכון חדש",message.notification?.tag?:"family")}
 companion object{fun saveToken(token:String){val uid=FirebaseAuth.getInstance().currentUser?.uid?:return;val id=MessageDigest.getInstance("SHA-256").digest(token.toByteArray()).joinToString(""){"%02x".format(it)};FirebaseFirestore.getInstance().document("users/$uid/devices/$id").set(mapOf("token" to token))}}
}
