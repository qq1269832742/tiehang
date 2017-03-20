
MsgWindow = Ext.extend(Ext.Window,{
    msgField:'msg',
    title: '警告信息',
    draggable :true,
    layout:'fit',
    minimizable:true,
    constrain: false,
    resizable:false,
    closable:true,
    closeAction:'hide',
    constructor : function(config){
	    var msgPanel = new Ext.Panel({autoScroll:true, frame:false,html:'<div id="$_msgWindow"/>'}); 
        config.items = [msgPanel];
        MsgWindow.superclass.constructor.call(this,config);
        this.on('minimize',function(){
        	this.toggleCollapse();
        });
    },
    creatView : function(){
    	if(!this.view){
    		this.tpl = new Ext.XTemplate(
    				'<tpl for=".">',
                    '<div  class="x-msgwindow-item"> {'+this.msgField+'}</div>',
                    '</tpl>'
                    );
            this.store = new Ext.data.JsonStore({
                    autoDestroy:true,
                    fields:[{name:this.msgField,type:'string'}],
                    idProperty:"$_id",
                    root:"dataSetResult[0].data",
                    successProperty:"dataSetResult[0].success",
                    totalProperty:"dataSetResult[0].totalCount",
                    scope: this
                });
            //创建DataView对象
            this.view = new Ext.DataView({
                store:this.store,
                tpl:this.tpl,
                deferEmptyText:true,
                emptyText:"&nbsp;",
                itemSelector:"div.x-msgwindow-item",
                renderTo:'$_msgWindow'
            });
            
    	}
    },
    /**
     * 清空窗口中的数据
     */
    clearMsg : function(){
    	if(this.store){
    		this.store.loadData({dataSetResult:[{data:[]}]});  
            this.view.refresh();
    	}
    },
    /**
     * 加载消息
     * @param msg
     */
    loadMsg : function(msg){
    	this.creatView();
        this.store.loadData(msg);  
        this.view.refresh();
    }
});
Ext.reg('msgwindow',MsgWindow);