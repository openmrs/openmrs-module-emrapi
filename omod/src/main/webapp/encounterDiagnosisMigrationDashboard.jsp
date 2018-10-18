<%@ include file="/WEB-INF/template/include.jsp"%>
<%@ include file="/WEB-INF/template/header.jsp"%>

<ul id="menu">
	<li class="first">
		<a href="${pageContext.request.contextPath}/admin"><spring:message code="admin.title.short"/></a>
	</li>
</ul>

<h2>
	<spring:message code="emrapi.migrateDiagnosis.migrateDiagnosisLink.name"/>
</h2>

<fieldset>
	<legend><spring:message code="emrapi.migrateDiagnosis.verify.operation.name"/></legend>
	<br>
	<b><spring:message code="emrapi.migrateDiagnosis.operation.warning.message"/></b>
	<br><br>
	<table>
		<tr>
			<td><input type="button" value="Continue" onclick="document.location.href='migrateEncounterDiagnosis.form';"></td>
			<td><input type="button" value="Cancel" onclick="document.location.href='${pageContext.request.contextPath}/admin';"></td>
		</tr>
	</table>
	
</fieldset>

<%@ include file="/WEB-INF/template/footer.jsp"%>