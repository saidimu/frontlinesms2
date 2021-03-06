<div class="controls">
	<g:if test="${messageSection}">
		<div id="message-toggler">
			<g:message code="fmessage.footer.show"/>:
			<g:link action="${messageSection}" params="${params.findAll({it.key != 'starred' && it.key != 'failed' && it.key != 'max' && it.key != 'offset'})}"><g:message code="fmessage.footer.show.all"/></g:link>
			|
			<g:if test="${messageSection == 'pending'}">
				<g:link action="${messageSection}" params="${params.findAll({it.key != 'max' && it.key != 'offset'}) + [failed: true]}" ><g:message code="fmessage.footer.show.failed"/></g:link>
			</g:if>
			<g:else>
				<g:link action="${messageSection}" params="${params.findAll({it.key != 'max' && it.key != 'offset'}) + [starred: true]}" ><g:message code="fmessage.footer.show.starred"/></g:link>
			</g:else>
		</div>
		<div id="paging">
			<g:hiddenField name="offset" value="${params.offset}" />
			<g:if test="${messageInstanceTotal > 0}">
				<g:paginate next="Next" prev="Back"
					max="${grailsApplication.config.grails.views.pagination.max}"
					action="${messageSection}" total="${messageInstanceTotal ?: itemInstanceTotal}" params="${params.findAll({it.key != 'messageId'})}"/>
			</g:if>
		</div>
	</g:if>
</div>
