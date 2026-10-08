'use strict';
const {test}=require('node:test');const assert=require('node:assert/strict');const D=require('../domain');
test('self tasks always zero',()=>{assert.equal(D.taskPoints('a','a',900),0);assert.equal(D.taskPoints('a','b',15),15);assert.throws(()=>D.taskPoints('a','b',-1))});
test('summary replay and erase/refill do not farm points',()=>{const a={good:'good',hard:'',thanks:'thanks',remember:''};const first=D.summaryDelta(null,a);assert.equal(first.delta,10);assert.equal(D.summaryDelta(first,a).delta,0);const erased=D.summaryDelta(first,{});assert.equal(D.summaryDelta(erased,a).delta,0)});
test('Israel date window across midnight and DST',()=>{const now=Date.parse('2026-10-08T22:30:00Z');assert.equal(D.dateAt(now),'2026-10-09');assert.equal(D.editableDate('2026-10-08',now),'2026-10-08');assert.throws(()=>D.editableDate('2026-10-07',now));assert.equal(D.editableDate('2026-10-24',Date.parse('2026-10-25T12:00:00Z')),'2026-10-24')});
test('routines reject empty days and duplicate ids',()=>{const r={id:'a',title:'Teeth',kind:'morning',days:[7,1,2,3,4]};assert.deepEqual(D.routines([r]),[r]);assert.throws(()=>D.routines([{...r,days:[]}]));assert.throws(()=>D.routines([r,r]))});
test('decision expires exactly at shared timestamp',()=>{assert.equal(D.isActive({status:'active',endsAt:100},99),true);assert.equal(D.isActive({status:'active',endsAt:100},100),false)});
