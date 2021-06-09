"use strict";

var choices = null;

window.onload = function() {
	choices = document.getElementById('choicesList');
}

function addChoice () {
	if(choices.lastElementChild.lastElementChild.value) {
		var listItem = document.createElement('li');
		var monChoix = document.createElement('input');
		monChoix.placeholder = "Quand soudain...";
		listItem.appendChild(monChoix);
		choices.appendChild(listItem);
	}
}

function removeChoice () {
	if (choices.childElementCount > 1) { //Buttons are choice's first child
		var monChoix = choices.removeChild(choices.lastChild);
	}
}