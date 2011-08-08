<%@ page contentType="text/html;charset=UTF-8" %>
<html>
	<head>
		<title><g:layoutTitle default="Archive"/></title>
		<g:layoutHead />
		<g:render template="/css"/>
		<link rel="shortcut icon" href="${resource(dir:'images',file:'favicon.ico')}" type="image/x-icon" />
		<g:javascript library="jquery" plugin="jquery"/>
		<jqui:resources />
    </head>
	<body>
		<g:render template="/tabs"/>
        <g:render template="/flash"/>
		<div id="main">
			<g:render template="menu"/>
			<g:layoutBody/>
		</div>
	</body>
</html>
