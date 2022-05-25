Cms={};
Cms.lmenu = function(id) {
	var m = $('#' + id);
	m.addClass('lmenu');
	m.children().each(function() {
		$(this).children('a').bind('click', function() {
			$(this).parent().addClass("lmenu-focus");
			$(this).blur();
			var li = m.focusElement;
			if (li && li!=this) {
				$(li).parent().removeClass("lmenu-focus");
			}
			m.focusElement=this;
		});
	});
}

//获取编辑器中HTML内容
function getEditorHTMLContents(EditorName) {
    var oEditor = FCKeditorAPI.GetInstance(EditorName);
    return(oEditor.GetXHTML(true));
}

// 获取编辑器中文字内容
function getEditorTextContents(EditorName) {
    var oEditor = FCKeditorAPI.GetInstance(EditorName);
    alert(oEditor.EditorDocument.body.innerText);
    return(oEditor.EditorDocument.body.innerText);
}

// 配置编辑器中内容
function SetEditorContents(EditorName, ContentStr) {
    var oEditor = FCKeditorAPI.GetInstance(EditorName) ;
    oEditor.SetHTML(ContentStr) ;
}