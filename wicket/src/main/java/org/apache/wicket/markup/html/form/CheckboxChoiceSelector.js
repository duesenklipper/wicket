if (typeof (CheckboxChoiceSelector) == "undefined") {
    CheckboxChoiceSelector = {};

    // adapted from AjaxFormChoiceComponentUpdatingBehavior
    CheckboxChoiceSelector.updateAllCheckboxes = function(parentChoiceId, newCheckedState) {
        var inputNodes = wicketGet(parentChoiceId).getElementsByTagName('input');
        for ( var i = 0; i < inputNodes.length; i++) {
            var inputNode = inputNodes[i];
            if (inputNode.id.indexOf(parentChoiceId + '-') >= 0) {
                inputNode.checked = newCheckedState;
            }
        }
    };

    CheckboxChoiceSelector.updateSelectorState = function(parentChoiceId, selectorId) {
        var inputNodes = wicketGet(parentChoiceId).getElementsByTagName('input');
        var allChecked = true;
        for ( var i = 0; i < inputNodes.length; i++) {
            var inputNode = inputNodes[i];
            if (inputNode.id.indexOf(parentChoiceId + '-') >= 0) {
                if (!inputNode.checked) {
                    allChecked = false;
                    break;
                }
            }
        }
        var selector = wicketGet(selectorId);
        selector.checked = allChecked;
    };

    CheckboxChoiceSelector.attachUpdateHandlers = function(parentChoiceId, selectorId) {
        var inputNodes = wicketGet(parentChoiceId).getElementsByTagName('input');
        var allChecked = true;
        for ( var i = 0; i < inputNodes.length; i++) {
            var inputNode = inputNodes[i];
            if (inputNode.id.indexOf(parentChoiceId + '-') >= 0) {
                Wicket.Event.add(inputNode, 'click', function() {
                    CheckboxChoiceSelector.updateSelectorState(parentChoiceId, selectorId);
                });
            }
        }
        CheckboxChoiceSelector.updateSelectorState(parentChoiceId, selectorId);
    };
}
