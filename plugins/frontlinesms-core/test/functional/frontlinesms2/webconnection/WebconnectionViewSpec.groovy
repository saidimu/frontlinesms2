package frontlinesms2.webconnection

import frontlinesms2.*
import frontlinesms2.message.*
import frontlinesms2.popup.*
import frontlinesms2.announcement.*

class WebconnectionViewSpec extends WebconnectionBaseSpec {
	def setup() {
		createWebconnections()
		createTestActivities()
		createTestMessages(Webconnection.findByName("Sync"))
	}

	@spock.lang.Unroll
	def "Webconnection page should show the details of a generic Webconnection in the header"() {
		setup:
			def webconnection  = Webconnection.findByName("Sync")
		when:
			to PageMessageWebconnection, webconnection
		then:
			waitFor { title?.toLowerCase().contains("web connection") }
			header[item] == value
		where:
			item        | value
			'name'      | "sync web connection"
			'url'       | 'http://www.frontlinesms.com/sync'
			'sendMethod'| 'get'
			'subtitle'  | 'http web connection'
	}

	@spock.lang.Unroll
	def "Webconnection page should show the details of an Ushahidi Webconnection in the header"() {
		setup:
			def webconnection  = Webconnection.findByName("Ush")
		when:
			to PageMessageWebconnection, webconnection
		then:
			waitFor { title?.toLowerCase().contains("web connection") }
			header[item] == value
		where:
			item        | value
			'name'      | 'ush web connection'
			'url'       | 'http://www.ushahidi.com/frontlinesms'
			'sendMethod'| 'get'
			'subtitle'  | 'web connection to ushahidi'
	}

	def "clicking the archive button archives the Webconnection and redirects to inbox "() {
		when:
			to PageMessageWebconnection, Webconnection.findByName("Sync")
		then:
			waitFor { header.displayed }
		when:
			header.archive.click()
		then:
			waitFor { at PageMessageInbox }
			notifications.flashMessageText.contains("Activity archived")
	}

	def "clicking the edit option opens the Webconnection Dialog for editing"() {
		when:
			to PageMessageWebconnection, Webconnection.findByName("Sync")
		then:
			waitFor { header.displayed }
		when:
			header.moreActions.value("edit").jquery.click()
		then:
			waitFor("veryslow") { at WebconnectionWizard }
	}

	def "Clicking the Quick Message button brings up the Quick Message Dialog"() {
		when:
			to PageMessageWebconnection, Webconnection.findByName("Sync")
			waitFor { header.quickMessage.displayed }
			header.quickMessage.click()
		then:
			waitFor('veryslow'){ at QuickMessageDialog }
			waitFor{ compose.textArea.displayed }
	}

	def "clicking the rename option opens the rename small popup"() {
		when:
			to PageMessageWebconnection, Webconnection.findByName("Sync")
		then:
			waitFor { header.displayed }
		when:
			header.moreActions.value("rename").jquery.click()
		then:
			waitFor { at RenameDialog }
			name.jquery.val().contains("Sync")
	}

	def "clicking the delete option opens the confirm delete small popup"() {
		when:
			to PageMessageWebconnection, Webconnection.findByName("Sync")
		then:
			waitFor { header.displayed }
		when:
			header.moreActions.value("delete").jquery.click()
		then:
			waitFor { at DeleteActivity }
	}

	def "clicking the export option opens the export dialog"() {
		when:
			to PageMessageWebconnection, Webconnection.findByName("Sync")
		then:
			waitFor { header.displayed }
		when:
			header.moreActions.value("export").jquery.click()
		then:
			waitFor { at ExportDialog }
	}

	def "selecting a single message reveals the single message view"() {
		when:
			to PageMessageWebconnection, Webconnection.findByName("Sync")
		then:
			waitFor { messageList.displayed }
		when:
			messageList.messages[0].checkbox.click()
		then:
			waitFor { singleMessageDetails.displayed }
			waitFor { singleMessageDetails.text == "Test message 0" }
	}

	def "selecting multiple messages reveals the multiple message view"() {
		when:
			to PageMessageWebconnection, Webconnection.findByName("Sync")
		then:
			waitFor { messageList.displayed }
		when:
			messageList.messages[0].checkbox.click()
			waitFor { singleMessageDetails.displayed }
			messageList.messages[1].checkbox.click()		
		then:
			waitFor { multipleMessageDetails.displayed }
			multipleMessageDetails.checkedMessageCount == "2 messages selected"
	}

	def "clicking on a message reveals the single message view with clicked message"() {
		when:
			to PageMessageWebconnection, Webconnection.findByName("Sync")
		then:
			waitFor { messageList.displayed }
		when:
			messageList.messages[3].checkbox.click()
		then:
			waitFor { singleMessageDetails.displayed }
			messageList.messages[3].hasClass("selected")
			singleMessageDetails.text == "Test message 3"
	}

	def "delete single message action works "() {
		when:
			to PageMessageWebconnection, Webconnection.findByName("Sync")
		then:
			waitFor { messageList.displayed }
		when:
			messageList.messages[0].checkbox.click()
		then:
			waitFor { singleMessageDetails.displayed }
		when:
			singleMessageDetails.delete.click()
		then:
			waitFor { messageList.displayed }
			!messageList.messages*.text.contains("Test message 0")
	}

	def "delete multiple message action works for multiple select"(){
		when:
			to PageMessageWebconnection, Webconnection.findByName("Sync")
		then:
			waitFor { messageList.displayed }
		when:
			messageList.messages[0].checkbox.click()
			waitFor {singleMessageDetails.displayed }
			messageList.messages[1].checkbox.click()
		then:
			waitFor { multipleMessageDetails.displayed }
		when:
			multipleMessageDetails.deleteAll.click()
		then:
			waitFor { messageList.displayed }
			!messageList.messages*.text.containsAll("Test message 0", "Test message 1")
	}

	def "move single message action works"() {
		when:
			to PageMessageWebconnection, Webconnection.findByName("Sync")
		then:
			waitFor { messageList.displayed }
		when:
			messageList.messages[0].checkbox.click()
		then:
			waitFor { singleMessageDetails.displayed }
			waitFor { singleMessageDetails.text == "Test message 0" }
		when:
			singleMessageDetails.moveTo(Activity.findByName("Sample Announcement").id).click()
		then:
			waitFor("veryslow") { at PageMessageWebconnection }
			waitFor { notifications.flashMessageText.contains("updated") }
			!messageList.messages*.text.contains("Test message 0")
		when:
			to PageMessageAnnouncement, Activity.findByName("Sample Announcement")
		then:
			waitFor { messageList.displayed }
			messageList.messages*.text.contains("Test message 0")
	}

	def "move multiple message action works"() {
		when:
			to PageMessageWebconnection, Webconnection.findByName("Sync")
		then:
			waitFor { messageList.displayed }
		when:
			messageList.messages[0].checkbox.click()
			waitFor {singleMessageDetails.displayed }
			messageList.messages[1].checkbox.click()
		then:
			waitFor { multipleMessageDetails.displayed }
		when:
			multipleMessageDetails.moveTo(Activity.findByName("Sample Announcement").id).click()
		then:
			waitFor("veryslow") { notifications.flashMessageText.contains("updated") }
			!messageList.messages*.text.containsAll("Test message 0", "Test message 1")
		when:
			to PageMessageAnnouncement, Activity.findByName("Sample Announcement")
		then:
			waitFor { messageList.displayed }
			messageList.messages*.text.containsAll("Test message 0", "Test message 1")
	}
}
