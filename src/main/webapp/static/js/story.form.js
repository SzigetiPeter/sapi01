$(function() {
	addValidationErrorClassesToForm();
	addDatetimePickes();
	
	function addValidationErrorClassesToForm() {
		$("form").find(".control-group").each(function() {
			var errorMessage = $(this).find(".help-inline").text();

			if (errorMessage) {
				$(this).addClass("error");
			}
		})
	}

	function addDatetimePickes() {
		$(".form_datetime").datetimepicker({
			format : "yyyy-mm-dd HH:ii",
			autoclose : true,
			todayBtn : true,
			pickerPosition : "bottom-left"
		});
	}
})