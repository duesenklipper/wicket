(function() {
    var checkBoxIDs = ${checkBoxIDs};
    for (i in checkBoxIDs) {
        var checkBox = wicketGet(checkBoxIDs[i]);
        if (checkBox.checked == false) {
            checkBox.click();
        }
    }
})();
