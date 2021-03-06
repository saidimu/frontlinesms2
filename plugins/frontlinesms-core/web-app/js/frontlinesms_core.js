// Array.indexOf is not available in IE before IE9, so
// add it here using the jQuery implementation
if(!Array.prototype.indexOf) {
	Array.prototype.indexOf = function(index) {
		return jQuery.inArray(index, this);
	};
}

// String.trim is not available in IE before IE9, so
// add it here using the jQuery implementation
if(!String.prototype.trim) {
	String.prototype.trim = function(s) {
		return jQuery.trim(s);
	};
}

// Standardise the onclick/onchange firing in IE before IE9
function addChangeHandlersForRadiosAndCheckboxes() {
	$('input:radio, input:checkbox').click(function() {
		this.blur();
		this.focus();
	});
}
if(jQuery.browser.msie) { $(function() {
	addChangeHandlersForRadiosAndCheckboxes();
});}

(function($) {
	$.fn.disableField = function(){
	    return this.each(function(){
	        this.disabled = true;
	    });
	};
	$.fn.enableField = function(){
	    return this.each(function(){
	        this.disabled = false;
	    });
	};
}(jQuery));

function refreshMessageCount() {
	$.ajax({
			url: url_root + 'message/unreadMessageCount',
			cache: false,
			success: function(data) { $('#inbox-indicator').html(data); }
	});
}

function isEmpty(val) {
	return val.trim().length === 0;
}

function isElementEmpty(selector) {
	return isEmpty($(selector).val());
}

function getSelectedGroupElements(groupName) {
	return $('input[name=' + groupName + ']:checked');
}

function isGroupChecked(groupName) {
	return getSelectedGroupElements(groupName).length > 0;
}

function isDropDownSelected(id) {
	var selectedOptions = $("#" + id + " option:selected");
	return selectedOptions.length > 0  && (!isEmpty(selectedOptions[0].value));
}

$('.check-bound-text-area').live('focus', function() {
	var checkBoxId = $(this).attr('checkbox_id');
	$('#' + checkBoxId).attr('checked', true);
});

function findInputWithValue(value) {
	return $('input[value=' + "'" + value + "'" + ']');
}

function isCheckboxSelected(value) {
	return findInputWithValue(value).is(':checked');
}

$.fn.renderDefaultText = function() {
	return this.focus(function() {
			$(this).toggleClass('default-text-input', false);
			var element = $(this).val();
			$(this).val(element === this.defaultValue ? '' : element);
		}).blur(function() {
			var element = $(this).val();
			$(this).val(element.match(/^\s+$|^$/) ? this.defaultValue : element);
			$(this).toggleClass('default-text-input', $(this).val() === this.defaultValue); });
};

function showThinking() {
	$('#thinking').fadeIn();
}

function hideThinking() {
	$('#thinking').fadeOut();
}

function insertAtCaret(areaId, text) {
	var front, back, range,
	txtarea = document.getElementById(areaId),
	scrollPos = txtarea.scrollTop,
	strPos = 0,
	browser = ((txtarea.selectionStart || txtarea.selectionStart === '0') ?
			"ff" : (document.selection ? "ie" : false ) );
	if (browser === "ie") {
		txtarea.focus();
		range = document.selection.createRange();
		range.moveStart ('character', -txtarea.value.length);
		strPos = range.text.length;
	} else if (browser === "ff") {
		strPos = txtarea.selectionStart;
	}

	front = (txtarea.value).substring(0, strPos);
	back = (txtarea.value).substring(strPos, txtarea.value.length);
	txtarea.value=front + text + back;
	strPos = strPos + text.length;
	if (browser === "ie") {
		txtarea.focus();
		range = document.selection.createRange();
		range.moveStart ('character', -txtarea.value.length);
		range.moveStart ('character', strPos);
		range.moveEnd ('character', 0);
		range.select();
	} else if (browser === "ff") {
		txtarea.selectionStart = strPos;
		txtarea.selectionEnd = strPos;
		txtarea.focus();
	}
	txtarea.scrollTop = scrollPos;
}

$(function() {
	setInterval(refreshMessageCount, 30000);
	$.extend($.validator.messages, {
		required: i18n("jquery.validation.required"),
		remote: i18n("jquery.validation.remote"),
		email: i18n("jquery.validation.email"),
		url: i18n("jquery.validation.url"),
		date: i18n("jquery.validation.date"),
		dateISO: i18n("jquery.validation.dateISO"),
		number: i18n("jquery.validation.number"),
		digits: i18n("jquery.validation.digits"),
		creditcard: i18n("jquery.validation.creditcard"),
		equalTo: i18n("jquery.validation.equalto"),
		accept: i18n("jquery.validation.accept"),
		maxlength: jQuery.validator.format(i18n("jquery.validation.maxlength")),
		minlength: jQuery.validator.format(i18n("jquery.validation.minlength")),
		rangelength: jQuery.validator.format(i18n("jquery.validation.rangelength")),
		range: jQuery.validator.format(i18n("jquery.validation.range")),
		max: jQuery.validator.format(i18n("jquery.validation.max")),
		min: jQuery.validator.format(i18n("jquery.validation.min"))
	});
});


