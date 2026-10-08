'use strict';
const crypto=require('node:crypto');
const QUESTIONS=[{id:'good',text:'מה היה הדבר הכי טוב שקרה לי היום?'},{id:'hard',text:'מה היה לי קשה היום?'},{id:'thanks',text:'על מה אני רוצה להגיד תודה?'},{id:'remember',text:'מה אני רוצה לזכור מהיום?'}];
const MOODS=['😭','😞','😠','😐','🙂','😄','😂','🤩'];
function requireValue(ok,message){if(!ok){const e=new Error(message);e.code='invalid-argument';throw e}}
function text(v,max=120,optional=false){requireValue(typeof v==='string'&&v.trim().length<=max&&(optional||v.trim().length>0),'טקסט חסר או ארוך מדי');return v.trim()}
function integer(v,min,max){requireValue(Number.isSafeInteger(v)&&v>=min&&v<=max,'מספר לא תקין');return v}
function id(v){requireValue(typeof v==='string'&&/^[A-Za-z0-9_-]{1,128}$/.test(v),'מזהה לא תקין');return v}
function dateAt(ms){return new Intl.DateTimeFormat('en-CA',{timeZone:'Asia/Jerusalem',year:'numeric',month:'2-digit',day:'2-digit'}).format(new Date(ms))}
function editableDate(date,now){const today=dateAt(now),yesterday=new Date(`${today}T12:00:00Z`);yesterday.setUTCDate(yesterday.getUTCDate()-1);requireValue(date===today||date===yesterday.toISOString().slice(0,10),'אפשר למלא רק היום או אתמול');return date}
function routines(v){requireValue(Array.isArray(v)&&v.length<=60,'עד 60 שלבים ברוטינות');const seen=new Set();return v.map(r=>{const rid=id(r.id);requireValue(!seen.has(rid),'מזהה שלב כפול');seen.add(rid);requireValue(['morning','evening'].includes(r.kind),'רוטינה לא תקינה');requireValue(Array.isArray(r.days)&&r.days.length>0&&r.days.length<=7,'יש לבחור ימים');return {id:rid,title:text(r.title),kind:r.kind,days:[...new Set(r.days.map(d=>integer(d,1,7)))]}})}
function summaryDelta(old,answers){requireValue(answers&&typeof answers==='object'&&!Array.isArray(answers),'תשובות לא תקינות');requireValue(Object.keys(answers).every(k=>QUESTIONS.some(q=>q.id===k)),'שאלה לא מוכרת');const clean=Object.fromEntries(QUESTIONS.map(q=>[q.id,text(answers[q.id]??'',4000,true)])),awarded=new Set(old?.awardedQuestions||[]),fresh=QUESTIONS.filter(q=>clean[q.id]&&!awarded.has(q.id)).map(q=>q.id);return {answers:clean,awardedQuestions:[...awarded,...fresh],delta:fresh.length*5}}
const hash=s=>crypto.createHash('sha256').update(s).digest('hex');const secret=()=>crypto.randomBytes(24).toString('hex');const code=()=>crypto.randomBytes(12).toString('hex').toUpperCase();const taskPoints=(giver,assignee,points)=>giver===assignee?0:integer(points,0,10000);const isActive=(s,now)=>!!s&&s.status==='active'&&s.endsAt>now;
module.exports={QUESTIONS,MOODS,requireValue,text,integer,id,dateAt,editableDate,routines,summaryDelta,hash,secret,code,taskPoints,isActive};
