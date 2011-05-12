// anonymous function to scope the contained variables. JavaScript is weird.
(function() {
    var inputNode = wicketGet('${selectorId}');
    Wicket.Event.add(inputNode, 'click', function() {
            CheckBoxSelector.updateAllCheckboxes(${checkBoxIdArrayLiteral},
                    inputNode.checked);
        });
})();
