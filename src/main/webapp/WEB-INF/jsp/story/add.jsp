<!DOCTYPE html>
<%@ page contentType="text/html;charset=UTF-8" language="java"%>
<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form"%>
<%@ taglib prefix="spring" uri="http://www.springframework.org/tags"%>
<html>
<head>
<title></title>
<script type="text/javascript" src="/static/js/story.form.js"></script>
</head>
<body>
	<h1>
		<spring:message code="label.story.add.page.title" />
	</h1>
	<div class="well page-content">
		<form:form action="/story/add" commandName="story" method="POST"
			enctype="utf8">
			<div id="control-group-title" class="control-group">
				<label for="story-title"><spring:message
						code="label.story.title" />:</label>

				<div class="controls">
					<form:input id="story-title" path="title" />
					<form:errors id="error-title" path="title" cssClass="help-inline" />
				</div>
			</div>
			<div id="control-group-description" class="control-group">
				<label for="story-description"><spring:message
						code="label.story.description" />:</label>

				<div class="controls">
					<form:textarea id="story-description" path="description" />
					<form:errors id="error-description" path="description"
						cssClass="help-inline" />
				</div>
			</div>
			<div id="control-group-start-time" class="control-group">
				<label for="story-start-time"><spring:message
						code="label.story.view.time.start.title" />:</label>

				<div id="story-start-time" class="control input-append date form_datetime">
					<form:input size="16" type="text" value=""  path="startTime" /> <span
						class="add-on"><i class="icon-th"></i></span>
				</div>

			</div>
			<div id="control-group-end-time" class="control-group">
				<label for="story-end-time"><spring:message
						code="label.story.view.time.end.title" />:</label>

				<div id="story-end-time" class="control input-append date form_datetime">
					<form:input size="16" type="text" value="" path="endTime"/> <span
						class="add-on"><i class="icon-th"></i></span>
				</div>

			</div>
			
			<div class="action-buttons">
				<a href="/" class="btn"><spring:message code="label.cancel" /></a>
				<button id="add-story-button" type="submit" class="btn btn-primary">
					<spring:message code="label.add.story.button" />
				</button>
			</div>
		</form:form>
	</div>
</body>
</html>