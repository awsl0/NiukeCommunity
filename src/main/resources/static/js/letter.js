$(function(){
	$("#sendBtn").click(send_letter);
	$(".close").click(delete_msg);
});

function send_letter() {
	$("#sendModal").modal("hide");
	var name=$("#recipient-name").val();
	var content=$("#message-text").val();
	$.post(
		CONTEXT_PATH+"/message/send",
		{toName:name,content:content},
		function (data) {
			data = $.parseJSON(data);
			if(data.code == 200) {
				$("#hintBody").text("发送成功!");
			} else {
				$("#hintBody").text(data.msg);
			}
			$("#hintModal").modal("show");
			setTimeout(function(){
				$("#hintModal").modal("hide");
				location.reload();
			}, 2000);
		}

	)
}

function delete_msg() {
	// TODO 删除数据
	$(this).parents(".media").remove();
}