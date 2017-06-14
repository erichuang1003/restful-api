(function($) {
	
	var RestTable = function(container, options) {
		this.container = container;
		this.options = $.extend({}, RestTable.DEFAULTS, options);
		this.init();
	}

	RestTable.DEFAULTS = {
	    local : {},
	    search : false,
	    showRefresh : true,
	    showColumns : true,
	    pagination : true,
	    idField : 'id',
	    dataField : 'list',
	    sidePagination : 'server',
	    pageSize : 20,
	    pageList : [
	            20, 50, 100, 200
	    ],
	    datetimeFormat : 'yyyy-mm-dd hh:ii:ss'
	};
	
	RestTable.LOCALES = {};
	
	RestTable.LOCALES['zh-CN'] = {
	    warning : '警告',
	    close : '关闭',
	    hint : '提示',
	    cancel : '取消',
	    confirm : '确认',
	    confirmTitle : '操作确认',
	    create : '添加',
	    update : '修改',
	    'delete' : '删除',
	    deleteConfirmTitle : function(num) {
		    return '确认删除勾选的' + num + '条记录？';
	    },
	    loadDataError : function(status) {
		    return '数据加载失败（错误码：' + status + '）';
	    },
	    requestSuccess : '操作成功',
	    requestError : function(status) {
		    return '操作失败（错误码：' + status + '）';
	    }
	};
	
	$.extend(RestTable.DEFAULTS.local, RestTable.LOCALES['zh-CN']);
	
	RestTable.prototype.init = function() {
		this.initColumns();
		this.initToolbar();
		this.initDialog();
		this.initTable();
	};
	
	RestTable.prototype.initToolbar = function() {
		this.toolbar = $('<div class="btn-group"></div>');
		this.container.append(this.toolbar);
		if(this.options.creatable){
			this.initCreate();
		}
		if(this.options.updatable){
			this.initUpdate();
		}
		if(this.options.deletable){
			this.initDelete();
		}
	};
	
	RestTable.prototype.initDialog = function() {
		this.messageDialog = $([
		        '<div class="modal fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">',
		        '<div class="modal-dialog">',
		        '<div class="modal-content">',
		        '<div class="modal-header">',
		        '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>',
		        '<h4 class="modal-title"></h4>', '</div>', '<div class="modal-body"></div>',
		        '<div class="modal-footer">', '<button type="button" class="btn btn-default" data-dismiss="modal">',
		        this.options.local.close, '</button>', '</div>', '</div>', '</div>', '</div>'
		].join(''));
		
		this.container.append(this.messageDialog);
		
		this.confirmDialog = $([
		        '<div class="modal fade" tabindex="-1" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">',
		        '<div class="modal-dialog">',
		        '<div class="modal-content">',
		        '<div class="modal-header">',
		        '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>',
		        '<h4 class="modal-title">', this.options.local.confirmTitle, '</h4>', '</div>',
		        '<div class="modal-body"></div>', '<div class="modal-footer">',
		        '<button type="button" class="btn btn-default" data-dismiss="modal">', this.options.local.cancel,
		        '</button>', '<button type="button" class="btn btn-danger">', this.options.local.confirm, '</button>',
		        '</div>', '</div>', '</div>', '</div>'
		].join(''));
		
		this.container.append(this.confirmDialog);
	};
	
	RestTable.prototype.showMessageDialog = function(title, msg) {
		this.messageDialog.find('.modal-title').html(title);
		this.messageDialog.find('.modal-body').html(msg);
		this.messageDialog.modal('show');
	};
	
	RestTable.prototype.showConfirmDialog = function(msg, func) {
		var me = this;
		me.confirmDialog.find('.modal-body').html(msg);
		var btn = me.confirmDialog.find('.btn-danger');
		btn.off('click');
		btn.on('click', function() {
			func(me.confirmDialog);
		});
		me.confirmDialog.modal('show');
	};
	
	RestTable.prototype.initColumns = function() {
		var me = this;
		me.idField = me.options.idField;
		me.createFields = [];
		me.updateFields = [];
		$.each(me.options.columns,
		        function(index, item) {
			        if(item.field){
				        if(item.creatable != false){
					        me.createFields.push(me.createField(item));
				        }
				        if(item.updatable != false){
					        me.updateFields.push(me.createField(item));
				        }
				        if(item.editor && (item.editor == 'datetime' || item.editor.type == 'datetime')
				                && !item.formatter){
					        item.formatter = function(value, row, index) {
						        return me.datetimeFormatter(item.editor.format || me.options.datetimeFormat, value,
						                row, index);
					        }
				        }
			        }
		        });
	};
	
	RestTable.prototype.createField = function(item) {
		var field = [];
		field.push('<div class="form-group"><label>');
		field.push(item.label || item.title);
		field.push('</label>');
		if(item.editor){
			if(item.editor.type == 'select'){
				if(item.editor.data){
					field.push('<select name="');
					field.push(item.field);
					field.push('" class="form-control" data-editor="select">');
					$.each(item.editor.data, function(index, item) {
						if(typeof item == 'object'){
							field.push('<option value="');
							field.push(item[1]);
							field.push('">');
							field.push(item[0]);
							field.push('</option>');
						} else{
							field.push('<option>');
							field.push(item);
							field.push('</option>');
						}
					});
					field.push('</select>');
				}
			} else if(item.editor == 'datetime' || item.editor.type == 'datetime'){
				field.push('<input type="text" class="form-control form-datetime" name="');
				field.push(item.field);
				field.push('" data-date-format="');
				var format = item.editor.format || this.options.datetimeFormat;
				field.push(format);
				field.push('"data-min-view="');
				if(format.indexOf('i') >= 0){
					field.push(0);
				} else if(format.indexOf('h') >= 0){
					field.push(1);
				} else if(format.indexOf('d') >= 0){
					field.push(2);
				} else if(format.indexOf('m') >= 0){
					field.push('3" data-start-view="3');
				} else{
					field.push('4" data-start-view="4');
				}
				field.push('" data-date-language="zh-CN" data-editor="datetime" />');
			}
		} else{
			field.push('<input type="');
			field.push(item.type || 'text');
			field.push('"');
			field.push(' class="form-control" name="');
			field.push(item.field);
			field.push('" />');
		}
		field.push('</div>');
		return field.join('');
	};
	
	RestTable.prototype.initTable = function() {
		var me = this;
		me.table = $('<table></table>');
		me.container.append(me.table);
		me.options.toolbar = me.toolbar, me.options.height = me.container.height() - me.toolbar.outerHeight(true);
		me.options.onLoadError = function(status) {
			me.onLoadError(status);
		};
		me.table.bootstrapTable(me.options);
		me.table.on('check.bs.table uncheck.bs.table check-all.bs.table uncheck-all.bs.table load-success.bs.table',
		        function() {
			        me.updateBtn.prop('disabled', me.getSelections().length != 1);
		        });
		me.table.on('check.bs.table uncheck.bs.table check-all.bs.table uncheck-all.bs.table load-success.bs.table',
		        function() {
			        if(me.options.deletable != 'multi'){
				        me.deleteBtn.prop('disabled', me.getSelections().length != 1);
			        } else{
				        me.deleteBtn.prop('disabled', !me.getSelections().length);
			        }
		        });
	};
	
	RestTable.prototype.onLoadError = function(status) {
		this.showMessageDialog(this.options.local.warning, this.options.local.loadDataError(status));
	}

	RestTable.prototype.initCreate = function() {
		var me = this;
		var btn = $([
		        '<button class="btn btn-success"><i class="glyphicon glyphicon-plus"></i>', me.options.local.create,
		        '</button>'
		].join(''));
		me.createBtn = btn;
		me.toolbar.append(btn);
		
		var form = $([
		        '<div class="modal fade" tabindex="-1" role="dialog">',
		        '<div class="modal-dialog" role="document">',
		        '<div class="modal-content">',
		        '<div class="modal-header">',
		        '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>',
		        '<h4 class="modal-title">', me.options.local.create, '</h4>', '</div>', '<div class="modal-body">',
		        '<form>', me.createFields.join(''), '</form>', '</div>', '<div class="modal-footer">',
		        '<button type="button" class="btn btn-default" data-dismiss="modal">', me.options.local.cancel,
		        '</button>', '<button type="button" class="btn btn-success">', me.options.local.create, '</button>',
		        '</div>', '</div>', '</div>', '</div>'
		].join(''));
		
		me.container.append(form);
		
		form.find('.form-datetime').datetimepicker();
		
		btn.on('click', function() {
			form.find('.form-control').val('');
			form.modal('show');
		});
		
		form.find('.btn-success').on('click', function() {
			var data = {};
			var id = 0;
			
			$.each(form.find('.form-control'), function() {
				if(this.name == me.idField){
					id = $(this).val();
				} else if($(this).data('editor') == 'datetime'){
					var format = $(this).data('dateFormat');
					var val = $(this).val();
					if(format.indexOf('m') < 0){
						val += '-00-00 00:00:00';
					} else if(format.indexOf('d') < 0){
						val += '-00 00:00:00';
					} else if(format.indexOf('h') < 0){
						val += ' 00:00:00';
					} else if(format.indexOf('m') < 0){
						val += ':00:00';
					} else if(format.indexOf('s') < 0){
						val += ':00';
					}
					data[this.name] = new Date(val).getTime();
				} else{
					data[this.name] = $(this).val();
				}
			});
			$.ajax({
			    url : me.options.url + id,
			    type : 'post',
			    contentType : 'application/json; charset=utf-8',
			    data : JSON.stringify(data),
			    success : function(result) {
				    form.modal('hide');
				    me.showMessageDialog(me.options.local.hint, me.options.local.requestSuccess);
				    me.table.bootstrapTable('refresh');
			    },
			    error : function(response, text, e) {
				    form.modal('hide');
				    me.showMessageDialog(me.options.local.warning, me.options.formatRequestError(response.status));
			    }
			});
		});
		
	};
	
	RestTable.prototype.initUpdate = function() {
		var me = this;
		
		var btn = $([
		        '<button class="btn btn-warning" disabled="true"><i class="glyphicon glyphicon-pencil"></i>',
		        me.options.local.update, '</button>'
		].join(''));
		me.updateBtn = btn;
		me.toolbar.append(btn);
		
		var form = $([
		        '<div class="modal fade" tabindex="-1" role="dialog">',
		        '<div class="modal-dialog" role="document">',
		        '<div class="modal-content">',
		        '<div class="modal-header">',
		        '<button type="button" class="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>',
		        '<h4 class="modal-title">', me.options.local.update, '</h4>', '</div>', '<div class="modal-body">',
		        '<form>', me.updateFields.join(''), '</form>', '</div>', '<div class="modal-footer">',
		        '<button type="button" class="btn btn-default" data-dismiss="modal">', me.options.local.cancel,
		        '</button>', '<button type="button" class="btn btn-warning">', me.options.local.update, '</button>',
		        '</div>', '</div>', '</div>', '</div>'
		].join(''));
		
		me.container.append(form);
		
		form.find('.form-datetime').datetimepicker({
			language : 'zh-CN'
		});
		
		btn.on('click', function() {
			form.modal('show');
			var record = me.getSelections()[0];
			$.each(form.find('.form-control'), function() {
				var editor = $(this).data('editor') || 'text';
				switch(editor) {
					case 'datetime':
						if(record[this.name]){
							$(this).val(new Date(record[this.name]).format($(this).data('dateFormat')));
						}
						break;
					default:
						$(this).val(record[this.name]);
				}
			});
		});
		
		form.find('.btn-warning').on('click', function() {
			var data = {};
			var record = me.getSelections()[0];
			var id = record[me.idField];
			$.each(form.find('.form-control'), function() {
				if($(this).data('editor') == 'datetime'){
					var format = $(this).data('dateFormat');
					var val = $(this).val();
					if(format.indexOf('m') < 0){
						val += '-00-00 00:00:00';
					} else if(format.indexOf('d') < 0){
						val += '-00 00:00:00';
					} else if(format.indexOf('h') < 0){
						val += ' 00:00:00';
					} else if(format.indexOf('m') < 0){
						val += ':00:00';
					} else if(format.indexOf('s') < 0){
						val += ':00';
					}
					data[this.name] = new Date(val).getTime();
				} else{
					data[this.name] = $(this).val();
				}
			});
			$.ajax({
			    url : me.options.url + id,
			    type : 'put',
			    contentType : 'application/json; charset=utf-8',
			    data : JSON.stringify(data),
			    success : function(result) {
				    form.modal('hide');
				    me.showMessageDialog(me.options.local.hint, me.options.local.requestSuccess);
				    me.table.bootstrapTable('refresh');
			    },
			    error : function(response, text, e) {
				    form.modal('hide');
				    me.showMessageDialog(me.options.local.warning, me.options.local.requestError(response.status));
			    }
			});
		});
	};
	
	RestTable.prototype.initDelete = function() {
		var me = this;
		var btn = $([
		        '<button class="btn btn-danger" disabled="true"><i class="glyphicon glyphicon-remove"></i>',
		        me.options.local['delete'], '</button>'
		].join(''));
		me.deleteBtn = btn;
		me.toolbar.append(btn);
		btn.on('click', function() {
			me.showConfirmDialog(me.options.local.deleteConfirmTitle(me.getSelections().length), function(dialog) {
				var ids = me.getIdSelections();
				if(ids.length > 0){
					if(ids.length == 1){
						$.ajax({
						    url : me.options.url + ids[0],
						    type : 'delete',
						    contentType : 'application/json; charset=utf-8',
						    success : function(result) {
							    dialog.modal('hide');
							    me.showMessageDialog(me.options.local.hint, me.options.local.requestSuccess);
							    me.table.bootstrapTable('refresh');
						    },
						    error : function(response, text, e) {
							    dialog.modal('hide');
							    me.showMessageDialog(me.options.local.warning, me.options.local
							            .requestError(response.status));
						    }
						});
					} else{
						$.ajax({
						    url : me.options.url,
						    type : 'delete',
						    contentType : 'application/json; charset=utf-8',
						    data : JSON.stringify(ids),
						    success : function(result) {
							    dialog.modal('hide');
							    me.showMessageDialog(me.options.local.hint, me.options.local.requestSuccess);
							    me.table.bootstrapTable('refresh');
						    },
						    error : function(response, text, e) {
							    dialog.modal('hide');
							    me.showMessageDialog(me.options.local.warning, me.options.local
							            .requestError(response.status));
						    }
						});
					}
				}
			});
		});
	};
	
	RestTable.prototype.getSelections = function() {
		return this.table.bootstrapTable('getSelections');
	}

	RestTable.prototype.getIdSelections = function() {
		var me = this;
		return $.map(me.table.bootstrapTable('getSelections'), function(row) {
			return row[me.idField];
		});
	};
	
	RestTable.prototype.datetimeFormatter = function(format, value, row, index) {
		if(value){
			return new Date(value).format(format);
		}
	};
	
	$.fn.restTable = function(options) {
		new RestTable(this, options);
	};
})(jQuery);
