<div class="multiple-contact hide">
	<div class="buttons">
		<ol>
			<li><g:actionSubmit id="update-all" action="updateMultipleContacts" value="Save All" disabled="disabled"/></li>
			<li><input type="button" class="cancel" value="Cancel" disabled="disabled"/></li>
			<li>
				<a id="btn_delete_all" onclick="launchConfirmationPopup('Delete all');">
					Delete all
				</a>
			</li>
		</ol>
	</div>
	<div id="contact-count">&nbsp;</div>
	<div class="multiple-contact">
		<div>
			<ol id='multi-group-list'>
				<g:each in="${sharedGroupInstanceList}" status="i" var="g">
					<li id="${g.name}" class="${g == groupInstance ? 'selected' : ''}">
						<span>${g.name}</span>
						<a class="remove-group" id="remove-group-${g.id}"><img class='remove' src='${resource(dir:'images/icons',file:'remove.png')}' /></a>
					</li>
				</g:each>
			</ol>
		</div>
		<div id='multi-group-add' class="dropdown">
			<g:select name="multi-group-dropdown"
					noSelection="['_':'Add to group...']"
					from="${nonSharedGroupInstanceList}"
					optionKey="id"
					optionValue="name"/>
		</div>
	</div>
</div>
