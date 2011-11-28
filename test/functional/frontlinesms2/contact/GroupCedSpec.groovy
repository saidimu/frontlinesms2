package frontlinesms2.contact

import frontlinesms2.*

class GroupCedSpec extends GroupBaseSpec {
	
	def 'button to save new group is displayed and works'() {
		when:
			to PageContactCreateGroup
			def initNumGroups = Group.count()
			$("li#create-group a").click()
			waitFor { $("#modalBox").displayed}
			$('#group-details input', name: "name").value('People')
			$("#done").click()
			waitFor {$("a", text: "People").displayed}
		then:
			assert Group.count() == (initNumGroups + 1)
	}
	
	def 'More action dropdown has option to rename the group'(){
		given:
			createTestGroupsAndContacts()
		when:
			go "group/show/${Group.findByName('Friends').id}"
		then:
			at PageContactShowGroupFriends
			$('#group-actions').displayed
		when:
			$('#group-actions').value("rename").click()
		then:
			waitFor{ $('#ui-dialog-title-modalBox').displayed}
		when:
			$("#name").value("Renamed Group")
			$('#done').click()
		then:
			waitFor{ $('a', text:'Renamed Group') }
			$('#contact-header h3').text() == 'Renamed Group (2)'
	}
	
	def 'More action dropdown has option to delete the group and opens a confirmation popup'(){
		given:
			createTestGroupsAndContacts()
		when:
			go "group/show/${Group.findByName('Friends').id}"
		then:
			at PageContactShowGroupFriends
			$('#group-actions').displayed
		when:
			$('#group-actions').value("delete").click()
		then:
			waitFor{ $('#ui-dialog-title-modalBox').displayed}
		when:
			$('#done').click()
		then:
			!Group.findByName('Friends')
	}
}

