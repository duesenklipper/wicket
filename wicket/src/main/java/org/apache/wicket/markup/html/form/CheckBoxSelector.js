if (typeof (CheckBoxSelector) == "undefined")
    CheckBoxSelector = {};

/**
 * THIS IS ONLY CALLED FROM THE SELECTOR's onClick EVENT!
 *
 * Walk over all targeted checkboxes. If a checkbox does not already match the selector's checked state, generate
 * a click event on the checkbox, so that its checked state will be updated and its onClick events will also be executed.
 */
// adapted from AjaxFormChoiceComponentUpdatingBehavior
CheckBoxSelector.updateAllCheckboxes = function(checkBoxIDs, newCheckedState) {
    for (i in checkBoxIDs) {
        var checkBox = wicketGet(checkBoxIDs[i]);
        if (checkBox.checked != newCheckedState) {
            checkBox.click();
        }
    }
};

/**
 * Walk over all targeted checkboxes. If they are all checked, check the selector. If not, uncheck the selector.
 */
CheckBoxSelector.updateSelectorState = function(checkBoxIDs, selectorId) {
    for (i in checkBoxIDs) {
        var inputNode = wicketGet(checkBoxIDs[i]);
        var allChecked = true;
        if (!inputNode.checked) {
            allChecked = false;
            break;
        }
    }
    var selector = wicketGet(selectorId);
    selector.checked = allChecked;
};

/**
 * Attach an onClick event handler to the targeted check boxes that will update the selector if one of the
 * targetted checkboxes is checked or unchecked.
 */
CheckBoxSelector.attachUpdateHandlers = function(checkBoxIDs, selectorId) {
    for (i in checkBoxIDs) {
        var inputNode = wicketGet(checkBoxIDs[i]);
        Wicket.Event.add(inputNode, 'click', function() {
            // on click simply call the update function, which will take care of everything
            CheckBoxSelector.updateSelectorState(checkBoxIDs, selectorId);
        });
    }
    // initialize the state of the selector once on page load:
    CheckBoxSelector.updateSelectorState(checkBoxIDs, selectorId);
};
