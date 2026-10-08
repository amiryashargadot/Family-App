package com.amiryashargadot.familyapp
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

data class Record(val id:String="",val fields:Map<String,Any?> = emptyMap()){
 fun s(k:String,fallback:String="")=fields[k] as? String?:fallback
 fun n(k:String)=(fields[k] as? Number)?.toLong()?:0L
 fun b(k:String)=fields[k]==true
 @Suppress("UNCHECKED_CAST") fun map(k:String)=fields[k] as? Map<String,Any?>?:emptyMap()
 fun strings(k:String)=(fields[k] as? List<*>)?.filterIsInstance<String>()?:emptyList()
}
data class RoutineStep(val id:String=UUID.randomUUID().toString(),val title:String="",val kind:String="morning",val days:List<Int> = (1..7).toList()){
 fun payload():Map<String,Any> = mapOf("id" to id,"title" to title,"kind" to kind,"days" to days)
}
val familyZone:ZoneId=ZoneId.of("Asia/Jerusalem")
val dayLabels=listOf(7 to "א׳",1 to "ב׳",2 to "ג׳",3 to "ד׳",4 to "ה׳",5 to "ו׳",6 to "ש׳")
val avatars=listOf("🦊","🐻","🐱","🦁","🐼","🦄","🐸","🐨")
val moods=listOf("😭","😞","😠","😐","🙂","😄","😂","🤩")
val questions=listOf("good" to "מה היה הדבר הכי טוב שקרה לי היום?","hard" to "מה היה לי קשה היום?","thanks" to "על מה אני רוצה להגיד תודה?","remember" to "מה אני רוצה לזכור מהיום?")
fun initialRoutines()=listOf(RoutineStep(title="לצחצח שיניים"),RoutineStep(title="להתלבש"),RoutineStep(title="תיק ובקבוק",days=listOf(7,1,2,3,4)),RoutineStep(title="מקלחת",kind="evening"),RoutineStep(title="לצחצח שיניים",kind="evening"))
fun Record.routines():List<RoutineStep> = (fields["routines"] as? List<*>)?.mapNotNull{v->val m=v as? Map<*,*>?:return@mapNotNull null;RoutineStep(m["id"] as? String?:return@mapNotNull null,m["title"] as? String?:"",m["kind"] as? String?:"morning",(m["days"] as? List<*>)?.mapNotNull{(it as? Number)?.toInt()}?:emptyList())}?:emptyList()
fun visibleSteps(steps:List<RoutineStep>,kind:String,date:LocalDate)=steps.filter{it.kind==kind&&date.dayOfWeek.value in it.days}
fun editableSummary(date:LocalDate,today:LocalDate)=date==today||date==today.minusDays(1)
fun effectiveTaskPoints(giver:String,assignee:String,points:Int)=if(giver==assignee)0 else points
