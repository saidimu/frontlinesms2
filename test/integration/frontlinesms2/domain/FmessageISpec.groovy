package frontlinesms2.domain

import frontlinesms2.*

class FmessageISpec extends grails.plugin.spock.IntegrationSpec {

	def "message doesn't have to have an activity"() {
		given:
		when:
			new Fmessage().save(failOnError:true, flush:true)
		then:
			Fmessage.count() == 1
	}

	def 'message can have an activity'() {
		given:
			new Poll(title:'Test poll').save()
			PollResponse response = new PollResponse(value:'yes').save()
		when:
			def m = new Fmessage(src:"+123456789", messageOwner:response).save()
		then:
			Fmessage.count() == 1
			Fmessage.get(m.id).messageOwner == response
	}
	
	def 'get deleted messages gets all messages with deleted flag'() {
		setup:
				(1..3).each {
				    new Fmessage(deleted:true,src:"+123456789").save(flush:true)
				}
				(1..2).each {
					new Fmessage(deleted:false,src:"+987654321").save(flush:true)
				}
		when:
			def deletedMessages = Fmessage.deleted(false)
		then:
			deletedMessages.count() == 3
	}
	
	def "should return all message counts"() {
		setup:
			setUpMessages()
			
		when:
			def messageCounts = Fmessage.countAllMessages(['archived':false, 'starred': false])
		then:
			messageCounts['inbox'] == 2
			messageCounts['sent'] == 3
			messageCounts['pending'] == 2
			messageCounts['deleted'] == 1
	}
	
	def "should return unread messages count"() {
		setup:
			new Fmessage(inbound:true, deleted:false, text:'A read message', read:true).save(flush:true)
			new Fmessage(inbound:true,deleted:false, text:'An unread message', read:false).save(flush:true)
			new Fmessage(inbound:true,deleted:false, text:'An unread message', read:false, archived: true).save(flush:true)
		when:
			def unreadMessageCount = Fmessage.countUnreadMessages()
		then:
			unreadMessageCount == 1
	}

	def "should return search results with a given max and offset"() {
		setup:
			setUpMessages()
			def search = new Search(searchString: 'inbox')
		when:
			def firstInboxMessage = Fmessage.search(search).list(max: 1,  offset:0)
			def firstTwoInboxMessages = Fmessage.search(search).list(max: 2, offset: 0)
			def allMsgsWithTheGivenSearchString = Fmessage.search(search).list()
		then:
			firstInboxMessage.size() == 1
			firstTwoInboxMessages.size() == 2
			allMsgsWithTheGivenSearchString.size() == 3
			Fmessage.search(search).count() == 3
	}

	def "should fetch messages based on message status"() {
		setup:
			setUpMessages()
			def search = new Search(status: ['INBOUND'])
			def search2 = new Search(status: ['SENT', 'PENDING', 'FAILED'])
			def search3 = new Search(searchString: "")
		when:
			def allInboundMessages = Fmessage.search(search).list()
			def allSentMessages = Fmessage.search(search2).list()
			def allMessages = Fmessage.search(search3).list()
		then:
			allInboundMessages*.every { it.inbound }
			allSentMessages*.every { !it.inbound }
			allMessages.size() == 7

	}
	
	def "should not error when searched for a group with no members"() {
		setup:
			def footballGroup = new Group(name: "football").save(flush: true)
			def search = new Search(group: footballGroup)
		when:
			def searchMessages = Fmessage.search(search).list()
			def searchMessagesCount = Fmessage.search(search).count()
		then:
			!searchMessages
			searchMessagesCount == 0
	}

	def "should get message traffic information for the filter criteria"() {
		setup:
			["Fsharp", 'Haskell'].each() { createGroup(it) }
			def fsharp = Group.findByName('Fsharp')
			def haskell = Group.findByName('Haskell')
			def jessy = createContact("Jessy", "+8139878055")
			def lucy = createContact("Lucy", "+8139878056")
			jessy.addToGroups(fsharp)
			lucy.addToGroups(haskell)
			
			int i =0
			(new Date()-6 ..new Date() + 5).each {
				new Fmessage(dateReceived: it, src:jessy.primaryMobile, inbound:true, text: "A message received on ${it}").save()
				new Fmessage(dateReceived: it, src:lucy.primaryMobile, hasSent:true, text: "A message sent on ${it}").save()
			}
			
			3.times {				
				new Fmessage(dateReceived: new Date()-1, src:jessy.primaryMobile, inbound:true, text: "Message {it}").save()
			}
			5.times {				
				new Fmessage(dateReceived: new Date()-1, src:jessy.primaryMobile, hasSent:true, text: "Message {it}").save()
			}

		when:
			def startDate = new Date()-2
			def endDate   = new Date()
			def messages = Fmessage.getMessageStats([groupInstance:fsharp, messageOwner:null, startDate:startDate, endDate:endDate])
		then:
			messages["${(startDate + 1).format('dd/MM')}"] == [sent:0, received:4]
			messages["${endDate.format('dd/MM')}"] == [sent:5, received:1]
	}

	def "should update message contact name"() {
		setup:
			new Contact(name: "Alice", primaryMobile:"1234", secondaryMobile: "4321").save(flush: true)
		when:
			def alice = Contact.findByName('Alice')
			def messageFromPrimaryNumber = new Fmessage(src:"1234", dst:"dst", inbound:true)
			def messageFromSecondaryNumber = new Fmessage(src:"4321", dst:"dst", inbound:true)
			def outBoundMessageToPrimaryNo = new Fmessage(src:"src", dst:"1234", hasSent:true)
			def outBoundMessageToSecondayNo = new Fmessage(src:"src", dst:"4321", hasSent:true)
			messageFromPrimaryNumber.save(flush: true)
			messageFromSecondaryNumber.save(flush: true)
			outBoundMessageToPrimaryNo.save(flush: true)
			outBoundMessageToSecondayNo.save(flush: true)
		then:
			println alice
			println messageFromPrimaryNumber.contactExists
			messageFromPrimaryNumber.contactName == "Alice"
			messageFromSecondaryNumber.contactName == "Alice"
			outBoundMessageToPrimaryNo.contactName == "Alice"
			outBoundMessageToSecondayNo.contactName == "Alice"
	}
	
	def "should fail when archive message has a message owner" () {
		setup:
			def f = new Folder(name:'test').save(failOnError:true)
			def m = new Fmessage(src:'+123456789', text:'hello').save(failOnError:true)
			f.addToMessages(m)
			f.save()
		when:
			m.archived = true
		then:
			!m.validate()
	}
	
	def "when number added as contact, all messages should have that contacts name and have contactExists set to true"() {
		when:
			def message = new Fmessage(src: "number", dst: "dst", inbound:true).save(flush: true)
		then:
			message.contactName == "number";
			message.contactExists == false;
		when:
			new Contact(name: "Alice", primaryMobile:"number", secondaryMobile: "secondaryNo").save(flush: true)
			message.refresh()
		then:
			message.contactName == "Alice";
			message.contactExists == true;
	}
	
	def "should be able to archive message when it has no message owner" () {
		setup:
			def m = new Fmessage(src:'+123456789', text:'hello').save(failOnError:true)
		when:
			m.archived = true
		then:
			m.validate()
	}
	
	def createGroup(String n) {
		new Group(name: n).save(failOnError: true)
	}

	def createContact(String n, String a) {
		def c = new Contact(name: n, primaryMobile: a)
		c.save(failOnError: true)
	}

	private def setUpMessages() {
			new Fmessage(inbound:true, deleted:false, text:'An inbox message').save(flush:true)
			new Fmessage(inbound:true,deleted:false, text:'Another inbox message').save(flush:true)
			new Fmessage(hasSent:true, deleted:false, text:'A sent message').save(flush:true)
			new Fmessage(hasSent:true,deleted:false, text:'Another sent message').save(flush:true)
			new Fmessage(hasSent:true,deleted:true, text:'Deleted sent message').save(flush:true)
			new Fmessage(hasSent:true,deleted:false, text:'This msg will not show up in inbox view').save(flush:true)
			new Fmessage(hasFailed:true, deleted:false, text:'A sent failed message').save(flush:true)
			new Fmessage(hasPending:true,deleted:false, text:'A pending message').save(flush:true)
	}
}