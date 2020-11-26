function like(btn, entityType, entityId, entityUserId,postId) {
    $.post(
        CONTEXT_PATH + "/like",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId,"postId":postId},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 200) {
                $(btn).children("i").text(data.likeCount);
                $(btn).children("b").text(data.linkStatus==1?'已赞':'赞');
            } else {
                alert(data.msg);
            }
        }
    );
}
$(function(){
    $("#topBtn").click(setTop);
    $("#wonderfulBtn").click(setWonderful);
    $("#deleteBtn").click(setDelete);
});

// 置顶
function setTop() {
    $.post(
        CONTEXT_PATH + "/discuss-post/top",
        {"discussPostId":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 200) {
                $("#topBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    );
}

// 加精
function setWonderful() {
    $.post(
        CONTEXT_PATH + "/discuss-post/wonderful",
        {"discussPostId":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 200) {
                $("#wonderfulBtn").attr("disabled", "disabled");
            } else {
                alert(data.msg);
            }
        }
    );
}

// 删除
function setDelete() {
    $.post(
        CONTEXT_PATH + "/discuss-post/delete",
        {"discussPostId":$("#postId").val()},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 200) {
                location.href = CONTEXT_PATH + "/index";
            } else {
                alert(data.msg);
            }
        }
    );
}