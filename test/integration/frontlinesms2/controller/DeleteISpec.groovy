package frontlinesms2.controller

import spock.lang.*
import frontlinesms2.*
import grails.plugin.spock.*

class DeleteISpec extends IntegrationSpec {
	def "deleted polls are not included in the pollInstanceList"() {
		given:
			def message1 = new Fmessage(src:'Bob', dst:'+254987654', text:'I like manchester', status:MessageStatus.INBOUND).save()
			def message2 = new Fmessage(src:'Alice', dst:'+2541234567', text:'go barcelona', status:MessageStatus.INBOUND).save()
			def p = Poll.createPoll(title: 'This is a poll', choiceA: 'Manchester', choiceB:'Barcelona').save(failOnError:true, flush:true)
			def messageController = new MessageController()
			def pollController = new PollController()
			PollResponse.findByValue('Manchester').addToMessages(message1).save(failOnError: true)
			PollResponse.findByValue('Barcelona').addToMessages(message2).save(failOnError: true)
			p.save(flush:true, failOnError:true)
		when:
			messageController.beforeInterceptor()
			def model1 = messageController.getShowModel()
		then:
			model1.pollInstanceList == [p]
		when:
			pollController.params.id = p.id
			pollController.delete()
			messageController.beforeInterceptor()
			def model2 = messageController.getShowModel()
		then:
			!model2.pollInstanceList
	}
	
	def "deleted polls are not included in the polls list"() {
		given:
			def message1 = new Fmessage(src:'Bob', dst:'+254987654', text:'I like manchester', status:MessageStatus.INBOUND).save()
			def message2 = new Fmessage(src:'Alice', dst:'+2541234567', text:'go barcelona', status:MessageStatus.INBOUND).save()
			def p = Poll.createPoll(title: 'This is a poll', choiceA: 'Manchester', choiceB:'Barcelona').save(failOnError:true, flush:true)
			def pollController = new PollController()
			PollResponse.findByValue('Manchester').addToMessages(message1).save(failOnError: true)
			PollResponse.findByValue('Barcelona').addToMessages(message2).save(failOnError: true)
			p.save(flush:true, failOnError:true)
		when:
			pollController.params.viewingArchive = false
			def model1 = pollController.index()
		then:
			model1.polls == [p]
		when:
			pollController.params.id = p.id
			pollController.delete()
			pollController.params.viewingArchive = false
			def model2 = pollController.index()
		then:
			!model2.polls
	}
	
	def "deleted folders are not included in the folderInstanceList"() {
		given:
			def f = new Folder(name:'test').save(failOnError:true)
			def m = new Fmessage()
			def messageController = new MessageController()
			def folderController = new FolderController()
			f.addToMessages(m)
			f.save(flush:true, failOnError:true)
		when:
			messageController.beforeInterceptor()
			def model1 = messageController.getShowModel()
		then:
			model1.folderInstanceList == [f]
		when:
			folderController.params.id = f.id
			folderController.delete()
			messageController.beforeInterceptor()
			def model2 = messageController.getShowModel()
		then:
			!model2.folderInstanceList
	}
	
	def "deleted folders are included in the trash list"() {
		given:
			def f = new Folder(name:'test', deleted:true).save(failOnError:true)
			def m = new Fmessage(dateReceived: new Date())
			def m2 = new Fmessage(dateReceived: new Date()-1)
			def messageController = new MessageController()
			f.addToMessages(m)
			f.addToMessages(m2)
			f.save(flush:true, failOnError:true)
			def m3 = new Fmessage(text:"not in folder", deleted: true).save(flush:true, failOnError:true)
		when:
			messageController.beforeInterceptor()
			messageController.trash()
			def model = messageController.modelAndView.model.trashInstanceList
		then:
			model == [m3,f]
	}
}

