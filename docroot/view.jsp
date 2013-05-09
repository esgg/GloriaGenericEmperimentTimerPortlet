<%
/**
 * Copyright (c) 2000-2011 Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
%>

<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@ taglib uri="http://liferay.com/tld/aui" prefix="aui"%>
<%@ taglib uri="http://liferay.com/tld/ui" prefix="liferay-ui" %>


<portlet:defineObjects />

<portlet:resourceURL var="timer" id="timer">
	<portlet:param name="control" value="operation"/>
</portlet:resourceURL>

<body onLoad="<portlet:namespace />startTimer()">
<fieldset Class="infoField">
	<legend><span><liferay-ui:message key="legend-reserveInfo"/></span></legend>
	<span class="status-span">
		<label class="status-label"> <liferay-ui:message key="label-remaining-time"/></label>
		<label class="status-label" id="<portlet:namespace />status"></label>
		<label class="status-label"><liferay-ui:message key="label-minutes"/></label>
	</span>
</fieldset>
</body>

<script>
function <portlet:namespace />startTimer(){
	var status = "<portlet:namespace />status";
	setInterval(function(){ AUI().use('aui-io-request', function(A){
		var status = "<portlet:namespace />status";
		var url = '<%=timer%>';
		A.io.request(url, {
			method : 'POST',
			data: {
				"action":"res"
			},
			dataType: 'json',
			on: {
				success: function() {    
					var message = this.get('responseData');
					if (message.success == true){
						if (message.hasReserve == true){
							document.getElementById(status).innerHTML = message.reserve;
						} else {
							var url = location.href;
							var new_url = url.substring(0,url.indexOf("experiment_"))+"home";
							alert('<liferay-ui:message key="msg-end-reservation"/>');
							location.replace("http://users.gloria-project.eu/online-experiments");
						}
					} else {
						var url = location.href;
						var new_url = url.substring(0,url.indexOf("experiment_"))+"home";
						alert('<liferay-ui:message key="msg-problem"/>');
						location.replace("http://users.gloria-project.eu/online-experiments");
					}
				}
			}
		});
	});
	},10000);
}
</script>