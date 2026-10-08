package com.amiryashargadot.familyapp
import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
class DomainTest {
 @Test fun sundaySchoolDays(){val r=RoutineStep(title="תיק",days=listOf(7,1,2,3,4));assertEquals(1,visibleSteps(listOf(r),"morning",LocalDate.of(2026,10,11)).size);assertTrue(visibleSteps(listOf(r),"morning",LocalDate.of(2026,10,9)).isEmpty())}
 @Test fun onlyTodayAndYesterdayEditable(){val d=LocalDate.of(2026,10,9);assertTrue(editableSummary(d,d));assertTrue(editableSummary(d.minusDays(1),d));assertFalse(editableSummary(d.minusDays(2),d));assertFalse(editableSummary(d.plusDays(1),d))}
 @Test fun selfTaskNoPoints(){assertEquals(0,effectiveTaskPoints("a","a",50));assertEquals(50,effectiveTaskPoints("a","b",50))}
}
