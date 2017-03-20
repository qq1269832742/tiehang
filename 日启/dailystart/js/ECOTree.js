var canvasWidth = 0;
var canvasHeight = 0;

ECONode = function (id, pid, dsc, w, h, c, bc, target, meta,flag,taskNode,marketNode,parentTaskNode,detailUrl,bankNo,multiFlag) {
	this.id = id;
	this.pid = pid;
	this.dsc = dsc;
	this.w = w;
	this.h = h;
	this.c = c;
	this.bc = bc;
	this.target = target;
	this.meta = meta;
	//扩展属性
	this.flag = flag;
	this.taskNode = taskNode;
	this.marketNode = marketNode;
	this.parentTaskNode = parentTaskNode;
	this.detailUrl = detailUrl;
	//added for saas 20120307 begin
	this.bankNo = bankNo;
	this.multiFlag = multiFlag;
	//added for saas 20120307 end
	
	this.siblingIndex = 0;
	this.dbIndex = 0;
	
	this.XPosition = 0;
	this.YPosition = 0;
	this.prelim = 0;
	this.modifier = 0;
	this.leftNeighbor = null;
	this.rightNeighbor = null;
	this.nodeParent = null;	
	this.nodeChildren = [];
	//处理一个节点有多个父节点
	this.nodeParents = [];
	
	this.isCollapsed = false;
	this.canCollapse = false;
	
	this.isSelected = false;
}

ECONode.prototype._getLevel = function () {
	if (this.nodeParent.id == -1) {return 0;}
	else return this.nodeParent._getLevel() + 1;
}

ECONode.prototype._isAncestorCollapsed = function () {
	if (this.nodeParent.isCollapsed) { return true; }
	else 
	{
		if (this.nodeParent.id == -1) { return false; }
		else	{ return this.nodeParent._isAncestorCollapsed(); }
	}
}

ECONode.prototype._setAncestorsExpanded = function () {
	if (this.nodeParent.id == -1) { return; }
	else 
	{
		this.nodeParent.isCollapsed = false;
		return this.nodeParent._setAncestorsExpanded(); 
	}	
}

ECONode.prototype._getChildrenCount = function () {
	if (this.isCollapsed) return 0;
    if(this.nodeChildren == null)
        return 0;
    else
        return this.nodeChildren.length;
}

ECONode.prototype._getLeftSibling = function () {
    if(this.leftNeighbor != null && this.leftNeighbor.nodeParent == this.nodeParent)
        return this.leftNeighbor;
    else
        return null;	
}

ECONode.prototype._getRightSibling = function () {
    if(this.rightNeighbor != null && this.rightNeighbor.nodeParent == this.nodeParent)
        return this.rightNeighbor;
    else
        return null;	
}

ECONode.prototype._getChildAt = function (i) {
	return this.nodeChildren[i];
}

ECONode.prototype._getChildrenCenter = function (tree) {
    node = this._getFirstChild();
    node1 = this._getLastChild();
    return node.prelim + ((node1.prelim - node.prelim) + tree._getNodeSize(node1)) / 2;	
}

ECONode.prototype._getFirstChild = function () {
	return this._getChildAt(0);
}

ECONode.prototype._getLastChild = function () {
	return this._getChildAt(this._getChildrenCount() - 1);
}

ECONode.prototype._drawArrow = function (tree,x,y) {
	tree.ctx.save();
	tree.ctx.strokeStyle = tree.config.linkColor;
	tree.ctx.beginPath();			
				
	tree.ctx.moveTo(x-5,y-10);
	tree.ctx.lineTo(x+5,y-10);
	tree.ctx.lineTo(x,y);					
				
	tree.ctx.fill();
	tree.ctx.restore();	
}

ECONode.prototype._drawChildrenLinks = function (tree) {
	
	var xa = 0, ya = 0, xb = 0, yb = 0, xc = 0, yc = 0, xd = 0, yd = 0;
	var node1 = null;
	
	xa = this.XPosition + (this.w / 2);
	ya = this.YPosition + this.h;
	
	for (var k = 0; k < this.nodeChildren.length; k++)
	{
		node1 = this.nodeChildren[k];

		xd = xc = node1.XPosition + (node1.w / 2);
		yd = node1.YPosition;
		xb = xa;
		yb = yc = ya + (yd - ya) / 2;

		tree.ctx.save();
		tree.ctx.strokeStyle = tree.config.linkColor;
		tree.ctx.beginPath();			
				
		tree.ctx.moveTo(xa,ya);
		tree.ctx.lineTo(xb,yb);
		tree.ctx.lineTo(xc,yc);
		tree.ctx.lineTo(xd,yd);						
				
		tree.ctx.stroke();
		tree.ctx.restore();	
		
		this._drawArrow(tree,xd,yd);
	}	
}

//处理一个节点有多个父节点情况的连线
ECONode.prototype._drawParentLinks = function (tree) {
	
	var xa = 0, ya = 0, xb = 0, yb = 0, xc = 0, yc = 0, xd = 0, yd = 0;
	var pnode1 = null;
	
	xd = this.XPosition + (this.w / 2);
	yd = this.YPosition;
	
	for(var k = 0; k < this.nodeParents.length; k++){
		pnode1 = this.nodeParents[k];
		
		xc = xd;
		
		xa = xb = pnode1.XPosition + (pnode1.w / 2);
		ya = pnode1.YPosition + pnode1.h;
		
		yb = yc = ya + (yd - ya) / 2;
		
		tree.ctx.save();
		tree.ctx.strokeStyle = tree.config.linkColor;
		tree.ctx.beginPath();			
				
		tree.ctx.moveTo(xa,ya);
		tree.ctx.lineTo(xb,yb);
		tree.ctx.lineTo(xc,yc);
		tree.ctx.lineTo(xd,yd);						
				
		tree.ctx.stroke();
		tree.ctx.restore();	
		
		this._drawArrow(tree,xd,yd);
	}
}

ECOTree = function (obj, elm) {
	this.config = {
		iMaxDepth : 100,
		iLevelSeparation : 40,
		iSiblingSeparation : 40,
		iSubtreeSeparation : 80,
		iRootOrientation : ECOTree.RO_TOP,
		iNodeJustification : ECOTree.NJ_TOP,
		topXAdjustment : 0,
		topYAdjustment : 0,		
		render : "AUTO",
		linkType : "M",
		linkColor : "blue",
		nodeColor : "#D9D6C3",
		nodeFill : ECOTree.NF_FLAT,
		nodeBorderColor : "blue",
		nodeSelColor : "#FFFFCC",
		levelColors : ["#5555FF","#8888FF","#AAAAFF","#CCCCFF"],
		levelBorderColors : ["#5555FF","#8888FF","#AAAAFF","#CCCCFF"],
		colorStyle : ECOTree.CS_NODE,
		useTarget : true,
		searchMode : ECOTree.SM_DSC,
		selectMode : ECOTree.SL_MULTIPLE,
		defaultNodeWidth : 80,
		defaultNodeHeight : 40,
		defaultTarget : 'javascript:void(0);',
		expandedImage : './img/less.gif',
		collapsedImage : './img/plus.gif',
		transImage : './img/trans.gif'
	}
	
	this.version = "1.1";
	this.obj = obj;
	this.elm = document.getElementById(elm);
	this.self = this;
	this.render = "CANVAS";
	this.ctx = null;
	this.canvasoffsetTop = 0;
	this.canvasoffsetLeft = 0;
	
	this.maxLevelHeight = [];
	this.maxLevelWidth = [];
	this.previousLevelNode = [];
	
	this.rootYOffset = 0;
	this.rootXOffset = 0;
	
	this.nDatabaseNodes = [];
	this.mapIDs = {};
	
	this.root = new ECONode(-1, null, null, 2, 2);
	this.iSelectedNode = -1;
	this.iLastSearch = 0;
	
}

//Tree orientation
ECOTree.RO_TOP = 0;
ECOTree.RO_BOTTOM = 1;
ECOTree.RO_RIGHT = 2;
ECOTree.RO_LEFT = 3;

//Level node alignment
ECOTree.NJ_TOP = 0;
ECOTree.NJ_CENTER = 1;
ECOTree.NJ_BOTTOM = 2;

//Node fill type
ECOTree.NF_GRADIENT = 0;
ECOTree.NF_FLAT = 1;

//Colorizing style
ECOTree.CS_NODE = 0;
ECOTree.CS_LEVEL = 1;

//Search method: Title, metadata or both
ECOTree.SM_DSC = 0;
ECOTree.SM_META = 1;
ECOTree.SM_BOTH = 2;

//Selection mode: single, multiple, no selection
ECOTree.SL_MULTIPLE = 0;
ECOTree.SL_SINGLE = 1;
ECOTree.SL_NONE = 2;

//CANVAS functions...
ECOTree._roundedRect = function (ctx,x,y,width,height,radius) {
  ctx.beginPath();
  ctx.moveTo(x,y+radius);
  ctx.lineTo(x,y+height-radius);
  ctx.quadraticCurveTo(x,y+height,x+radius,y+height);
  ctx.lineTo(x+width-radius,y+height);
  ctx.quadraticCurveTo(x+width,y+height,x+width,y+height-radius);
  ctx.lineTo(x+width,y+radius);
  ctx.quadraticCurveTo(x+width,y,x+width-radius,y);
  ctx.lineTo(x+radius,y);
  ctx.quadraticCurveTo(x,y,x,y+radius);
  ctx.fill();
  ctx.stroke();
}

ECOTree._canvasNodeClickHandler = function (tree,target,nodeid) {
	if (target != nodeid) return;
	tree.selectNode(nodeid,true);
}

//Layout algorithm
ECOTree._firstWalk = function (tree, node, level) {
		var leftSibling = null;
		
        node.XPosition = 0;
        node.YPosition = 0;
        node.prelim = 0;
        node.modifier = 0;
        node.leftNeighbor = null;
        node.rightNeighbor = null;
        tree._setLevelHeight(node, level);
        tree._setLevelWidth(node, level);
        tree._setNeighbors(node, level);
        if(node._getChildrenCount() == 0 || level == tree.config.iMaxDepth)
        {
            leftSibling = node._getLeftSibling();
            if(leftSibling != null)
                node.prelim = leftSibling.prelim + tree._getNodeSize(leftSibling) + tree.config.iSiblingSeparation;
            else
                node.prelim = 0;
        } 
        else
        {
            var n = node._getChildrenCount();
            for(var i = 0; i < n; i++)
            {
                var iChild = node._getChildAt(i);
                ECOTree._firstWalk(tree, iChild, level + 1);
            }

            var midPoint = node._getChildrenCenter(tree);
            midPoint -= tree._getNodeSize(node) / 2;
            leftSibling = node._getLeftSibling();
            if(leftSibling != null)
            {
                node.prelim = leftSibling.prelim + tree._getNodeSize(leftSibling) + tree.config.iSiblingSeparation;
                node.modifier = node.prelim - midPoint;
                ECOTree._apportion(tree, node, level);
            } 
            else
            {            	
                node.prelim = midPoint;
            }
        }	
}

ECOTree._apportion = function (tree, node, level) {
        var firstChild = node._getFirstChild();
        var firstChildLeftNeighbor = firstChild.leftNeighbor;
        var j = 1;
        for(var k = tree.config.iMaxDepth - level; firstChild != null && firstChildLeftNeighbor != null && j <= k;)
        {
            var modifierSumRight = 0;
            var modifierSumLeft = 0;
            var rightAncestor = firstChild;
            var leftAncestor = firstChildLeftNeighbor;
            for(var l = 0; l < j; l++)
            {
                rightAncestor = rightAncestor.nodeParent;
                leftAncestor = leftAncestor.nodeParent;
                modifierSumRight += rightAncestor.modifier;
                modifierSumLeft += leftAncestor.modifier;
            }

            var totalGap = (firstChildLeftNeighbor.prelim + modifierSumLeft + tree._getNodeSize(firstChildLeftNeighbor) + tree.config.iSubtreeSeparation) - (firstChild.prelim + modifierSumRight);
            if(totalGap > 0)
            {
                var subtreeAux = node;
                var numSubtrees = 0;
                for(; subtreeAux != null && subtreeAux != leftAncestor; subtreeAux = subtreeAux._getLeftSibling())
                    numSubtrees++;

                if(subtreeAux != null)
                {
                    var subtreeMoveAux = node;
                    var singleGap = totalGap / numSubtrees;
                    for(; subtreeMoveAux != leftAncestor; subtreeMoveAux = subtreeMoveAux._getLeftSibling())
                    {
                        subtreeMoveAux.prelim += totalGap;
                        subtreeMoveAux.modifier += totalGap;
                        totalGap -= singleGap;
                    }

                }
            }
            j++;
            if(firstChild._getChildrenCount() == 0)
                firstChild = tree._getLeftmost(node, 0, j);
            else
                firstChild = firstChild._getFirstChild();
            if(firstChild != null)
                firstChildLeftNeighbor = firstChild.leftNeighbor;
        }
}

ECOTree._secondWalk = function (tree, node, level, X, Y) {
        if(level <= tree.config.iMaxDepth)
        {
            var xTmp = tree.rootXOffset + node.prelim + X;
            var yTmp = tree.rootYOffset + Y;
            var maxsizeTmp = 0;
            var nodesizeTmp = 0;
            var flag = false;
            
            switch(tree.config.iRootOrientation)
            {            
	            case ECOTree.RO_TOP:
	            case ECOTree.RO_BOTTOM:	        	            	    	
	                maxsizeTmp = tree.maxLevelHeight[level];
	                nodesizeTmp = node.h;	                
	                break;

	            case ECOTree.RO_RIGHT:
	            case ECOTree.RO_LEFT:            
	                maxsizeTmp = tree.maxLevelWidth[level];
	                flag = true;
	                nodesizeTmp = node.w;
	                break;
            }
            switch(tree.config.iNodeJustification)
            {
	            case ECOTree.NJ_TOP:
	                node.XPosition = xTmp;
	                node.YPosition = yTmp;
	                break;
	
	            case ECOTree.NJ_CENTER:
	                node.XPosition = xTmp;
	                node.YPosition = yTmp + (maxsizeTmp - nodesizeTmp) / 2;
	                break;
	
	            case ECOTree.NJ_BOTTOM:
	                node.XPosition = xTmp;
	                node.YPosition = (yTmp + maxsizeTmp) - nodesizeTmp;
	                break;
            }
            if(flag)
            {
                var swapTmp = node.XPosition;
                node.XPosition = node.YPosition;
                node.YPosition = swapTmp;
            }
            switch(tree.config.iRootOrientation)
            {
	            case ECOTree.RO_BOTTOM:
	                node.YPosition = -node.YPosition - nodesizeTmp;
	                break;
	
	            case ECOTree.RO_RIGHT:
	                node.XPosition = -node.XPosition - nodesizeTmp;
	                break;
            }
            if(node._getChildrenCount() != 0)
                ECOTree._secondWalk(tree, node._getFirstChild(), level + 1, X + node.modifier, Y + maxsizeTmp + tree.config.iLevelSeparation);
            var rightSibling = node._getRightSibling();
            if(rightSibling != null)
                ECOTree._secondWalk(tree, rightSibling, level, X, Y);
        }	
}

ECOTree.prototype._positionTree = function () {	
	this.maxLevelHeight = [];
	this.maxLevelWidth = [];			
	this.previousLevelNode = [];		
	ECOTree._firstWalk(this.self, this.root, 0);
	
	switch(this.config.iRootOrientation)
	{            
	    case ECOTree.RO_TOP:
	    case ECOTree.RO_LEFT: 
	    		this.rootXOffset = this.config.topXAdjustment + this.root.XPosition;
	    		this.rootYOffset = this.config.topYAdjustment + this.root.YPosition;
	        break;    
	        
	    case ECOTree.RO_BOTTOM:	
	    case ECOTree.RO_RIGHT:             
	    		this.rootXOffset = this.config.topXAdjustment + this.root.XPosition;
	    		this.rootYOffset = this.config.topYAdjustment + this.root.YPosition;
	}	
	
	ECOTree._secondWalk(this.self, this.root, 0, 0, 0);	
}

ECOTree.prototype._positionTree1 = function (cols) {
	var node = null;
	
	var rowSpace, colSpace, startX, startY;
	rowSpace = colSpace = 40;
	startX = startY = 50;
	
    for (var n=0; n < this.nDatabaseNodes.length; n++)
	{ 	
    	node = this.nDatabaseNodes[n];
    	var modVal = n%cols;
    	var divVal = this._div(n,cols);
    	
    	node.XPosition = (modVal == 0) ? startX : ((node.w + colSpace)*modVal + startX);
    	node.YPosition = (divVal == 0) ? startY : ((node.h + rowSpace)*divVal + startY);
	}
}

ECOTree.prototype._setLevelHeight = function (node, level) {	
	if (this.maxLevelHeight[level] == null) 
		this.maxLevelHeight[level] = 0;
    if(this.maxLevelHeight[level] < node.h)
        this.maxLevelHeight[level] = node.h;	
}

ECOTree.prototype._setLevelWidth = function (node, level) {
	if (this.maxLevelWidth[level] == null) 
		this.maxLevelWidth[level] = 0;
    if(this.maxLevelWidth[level] < node.w)
        this.maxLevelWidth[level] = node.w;		
}

ECOTree.prototype._setNeighbors = function(node, level) {
    node.leftNeighbor = this.previousLevelNode[level];
    if(node.leftNeighbor != null)
        node.leftNeighbor.rightNeighbor = node;
    this.previousLevelNode[level] = node;	
}

ECOTree.prototype._getNodeSize = function (node) {
    switch(this.config.iRootOrientation)
    {
    case ECOTree.RO_TOP: 
    case ECOTree.RO_BOTTOM: 
        return node.w;

    case ECOTree.RO_RIGHT: 
    case ECOTree.RO_LEFT: 
        return node.h;
    }
    return 0;
}

ECOTree.prototype._getLeftmost = function (node, level, maxlevel) {
    if(level >= maxlevel) return node;
    if(node._getChildrenCount() == 0) return null;
    
    var n = node._getChildrenCount();
    for(var i = 0; i < n; i++)
    {
        var iChild = node._getChildAt(i);
        var leftmostDescendant = this._getLeftmost(iChild, level + 1, maxlevel);
        if(leftmostDescendant != null)
            return leftmostDescendant;
    }

    return null;	
}

ECOTree.prototype._selectNodeInt = function (dbindex, flagToggle) {
	if (this.config.selectMode == ECOTree.SL_SINGLE)
	{
		if ((this.iSelectedNode != dbindex) && (this.iSelectedNode != -1))
		{
			this.nDatabaseNodes[this.iSelectedNode].isSelected = false;
		}		
		this.iSelectedNode = (this.nDatabaseNodes[dbindex].isSelected && flagToggle) ? -1 : dbindex;
	}	
	this.nDatabaseNodes[dbindex].isSelected = (flagToggle) ? !this.nDatabaseNodes[dbindex].isSelected : true;	
}

ECOTree.prototype._collapseAllInt = function (flag) {
	var node = null;
	for (var n = 0; n < this.nDatabaseNodes.length; n++)
	{ 
		node = this.nDatabaseNodes[n];
		if (node.canCollapse) node.isCollapsed = flag;
	}	
	this.UpdateTree();
}

ECOTree.prototype._selectAllInt = function (flag) {
	var node = null;
	for (var k = 0; k < this.nDatabaseNodes.length; k++)
	{ 
		node = this.nDatabaseNodes[k];
		node.isSelected = flag;
	}	
	this.iSelectedNode = -1;
	this.UpdateTree();
}

//重新调整节点位置
ECOTree.prototype._rePositionNode = function (node) {
	var leftX = rightX = node.nodeParents[0].XPosition;
	var pnode = null;
	var oldPosition = node.XPosition;
	
	for(var k = 0; k < node.nodeParents.length; k++){
		pnode = node.nodeParents[k];
		
		if(pnode.XPosition > rightX) rightX = pnode.XPosition;
		if(pnode.XPosition < leftX) leftX = pnode.XPosition;
	}
	
	node.XPosition = (rightX + (leftX - rightX) / 2);
	
	if(canvasWidth < node.XPosition) canvasWidth = node.XPosition;
	
	for(var m = 0; m < this.nDatabaseNodes.length; m++){
		if(this.nDatabaseNodes[m].id == node.id) this.nDatabaseNodes[m] = node;
	}
	
	var moveSpace = node.XPosition - oldPosition;
	this._changeSpace(node,moveSpace);
}

//递归修改子节点的位置 node-待修改位置的节点, moveSpace-修改的值
ECOTree.prototype._changeSpace = function(node,moveSpace){
	var cnode = null;
	
	if(node.nodeChildren.length>0){
		for(var n = 0; n < node.nodeChildren.length; n++){
			cnode = node.nodeChildren[n];
			cnode.XPosition += moveSpace;
			if(canvasWidth < cnode.XPosition) canvasWidth = cnode.XPosition;
			this._changeSpace(cnode,moveSpace);
		} 
	}
}

ECOTree.prototype._drawTree = function () {
	var node = null;
	var color = "";
	var border = "";
	
	for (var k = 0; k < this.nDatabaseNodes.length; k++)
	{ 
		var reNode = null;
		reNode = this.nDatabaseNodes[k];
		
		if(canvasWidth < reNode.XPosition) canvasWidth = reNode.XPosition;
		if(canvasHeight < reNode.YPosition) canvasHeight = reNode.YPosition;
		
		if(reNode.nodeParents.length > 1) this._rePositionNode(reNode);
	}

	this._setCanvas();
			
	for (var n = 0; n < this.nDatabaseNodes.length; n++)
	{ 
		node = this.nDatabaseNodes[n];
		
		switch (this.config.colorStyle) {
			case ECOTree.CS_NODE:
				color = node.c;
				border = node.bc;
				break;
			case ECOTree.CS_LEVEL:
				var iColor = node._getLevel() % this.config.levelColors.length;
				color = this.config.levelColors[iColor];
				iColor = node._getLevel() % this.config.levelBorderColors.length;
				border = this.config.levelBorderColors[iColor];
				break;
		}
		
		if (!node._isAncestorCollapsed())
		{
			this.ctx.save();
			this.ctx.strokeStyle = border;
			switch (this.config.nodeFill) {
				case ECOTree.NF_GRADIENT:							
					var lgradient = this.ctx.createLinearGradient(node.XPosition,0,node.XPosition+node.w,0);
					lgradient.addColorStop(0.0,((node.isSelected)?this.config.nodeSelColor:color));
					lgradient.addColorStop(1.0,"#F5FFF5");
					this.ctx.fillStyle = lgradient;
					break;
					
				case ECOTree.NF_FLAT:
					this.ctx.fillStyle = ((node.isSelected)?this.config.nodeSelColor:color);
					break;
			}					
			
			ECOTree._roundedRect(this.ctx,node.XPosition,node.YPosition,node.w,node.h,5);
			this.ctx.restore();
			
			if (!node.isCollapsed) {
				node._drawChildrenLinks(this.self);
				if(node.nodeParents.length > 1) node._drawParentLinks(this.self);
			}
		}
	}		
}

ECOTree.prototype._drawTree1 = function () {
	var node = null;
	var color = "";
	var border = "";
	
	for (var k = 0; k < this.nDatabaseNodes.length; k++)
	{ 
		var reNode = null;
		reNode = this.nDatabaseNodes[k];
		
		if(canvasWidth < reNode.XPosition) canvasWidth = reNode.XPosition;
		if(canvasHeight < reNode.YPosition) canvasHeight = reNode.YPosition;
		
		if(reNode.nodeParents.length > 1) this._rePositionNode(reNode);
	}

	this._setCanvas();
			
	for (var n = 0; n < this.nDatabaseNodes.length; n++)
	{ 
		node = this.nDatabaseNodes[n];
		
		switch (this.config.colorStyle) {
			case ECOTree.CS_NODE:
				color = node.c;
				border = node.bc;
				break;
			case ECOTree.CS_LEVEL:
				var iColor = node._getLevel() % this.config.levelColors.length;
				color = this.config.levelColors[iColor];
				iColor = node._getLevel() % this.config.levelBorderColors.length;
				border = this.config.levelBorderColors[iColor];
				break;
		}
		
		if (!node._isAncestorCollapsed())
		{
			this.ctx.save();
			this.ctx.strokeStyle = border;
			switch (this.config.nodeFill) {
				case ECOTree.NF_GRADIENT:							
					var lgradient = this.ctx.createLinearGradient(node.XPosition,0,node.XPosition+node.w,0);
					lgradient.addColorStop(0.0,((node.isSelected)?this.config.nodeSelColor:color));
					lgradient.addColorStop(1.0,"#F5FFF5");
					this.ctx.fillStyle = lgradient;
					break;
					
				case ECOTree.NF_FLAT:
					this.ctx.fillStyle = ((node.isSelected)?this.config.nodeSelColor:color);
					break;
			}					
			
			ECOTree._roundedRect(this.ctx,node.XPosition,node.YPosition,node.w,node.h,5);
			this.ctx.restore();
		}
	}		
}

ECOTree.prototype.drawDiv = function () {
	var s = [];
	var node = null;
	
	for (var n = 0; n < this.nDatabaseNodes.length; n++)
	{ 
		node = this.nDatabaseNodes[n];
		s.push('<div id="' + node.id + '" class="econode" style="top:'+(node.YPosition+this.canvasoffsetTop)
				+'; left:'+(node.XPosition+this.canvasoffsetLeft)
				+'; width:'+node.w+'; height:'+node.h+';text-align:center;word-break:break-all;');
		if('1'==node.flag || '2'==node.flag){
			s.push('cursor:pointer;" ');
		}else{
			s.push('" ');
		}
		s.push('taskNode="' + node.taskNode + '" marketNode="' + node.marketNode 
				+'" parentTaskNode="' + node.parentTaskNode 
				+'" detailUrl="' + node.detailUrl + '" '
				+' bankNo="' + node.bankNo + '" multiFlag="' + node.multiFlag + '" ');
		if('0'==node.flag){
			s.push('>');
		}
		else if('1'==node.flag){
			s.push('onclick=\'leftClick(this)\' onContextMenu=\'rightClick(this,event)\' ');
			s.push('>');
		}
		else if('2'==node.flag){
			s.push('onclick=\'taLeftClick(this)\' ');
			s.push('>');
		}
		s.push('<font size=2>');
		s.push(node.dsc);
		s.push('</font>');
		s.push('</div>');
	}
	return s.join('');
}

ECOTree.prototype.toString = function () {	
	var s = [];
	
	this._positionTree();
	
	s.push('<canvas id="canvas" width=2000 height=2000></canvas>');
	
	return s.join('');
}

//ECOTree API begins here...

ECOTree.prototype.UpdateTree = function () {	
	this.elm.innerHTML = this.toString();
	
	var canvas = document.getElementById("canvas");
	
	if (canvas && canvas.getContext)  {
		this.canvasoffsetLeft = canvas.offsetLeft;
		this.canvasoffsetTop = canvas.offsetTop;
		
		this.ctx = canvas.getContext('2d');
		
		var h = this._drawTree();
		var r = this.elm.ownerDocument.createRange();
		r.setStartBefore(this.elm);
		var parsedHTML = r.createContextualFragment(h);								
		this.elm.appendChild(parsedHTML);
	}
}

ECOTree.prototype.setCtx = function (ctx) {	
	this.ctx = ctx;
}

//判断一个节点是否已经在树中存在
ECOTree.prototype.hasNodeAdded = function(id) {
	for(var k = 0; k < this.nDatabaseNodes.length; k++){
		if(id == this.nDatabaseNodes[k].id) return true;
	}
	return false;
}

ECOTree.prototype.add = function (id, pid, dsc, w, h, c, bc, target, meta,flag,taskNode,marketNode,parentTaskNode,detailUrl,bankNo,multiFlag) {
	//如果已经增加过该节点,说明这个节点有多个父节点
	if(this.hasNodeAdded(id))
		{
			var node = null;
			var pnode = null;
			
			for(var k = 0; k < this.nDatabaseNodes.length; k++){
				if(this.nDatabaseNodes[k].id == id) node = this.nDatabaseNodes[k];
				if(this.nDatabaseNodes[k].id == pid) pnode = this.nDatabaseNodes[k];
			}

			var i = node.nodeParents.length;
			node.nodeParents[i] = pnode;
		}	
	else
		{
			var nw = w || this.config.defaultNodeWidth; //Width, height, colors, target and metadata defaults...
			var nh = h || this.config.defaultNodeHeight;
			var color = c || this.config.nodeColor;
			var border = bc || this.config.nodeBorderColor;
			var tg = (this.config.useTarget) ? ((typeof target == "undefined") ? (this.config.defaultTarget) : target) : null;
			var metadata = (typeof meta != "undefined")	? meta : "";
			
			var pnode = null; //Search for parent node in database
			if (pid == -1) 
				{
					pnode = this.root;
				}
			else
				{
					for (var k = 0; k < this.nDatabaseNodes.length; k++)
					{
						if (this.nDatabaseNodes[k].id == pid)
						{
							pnode = this.nDatabaseNodes[k];
							break;
						}
					}	
				}
			
			var node = new ECONode(id, pid, dsc, nw, nh, color, border, tg, metadata,flag,taskNode,marketNode,parentTaskNode,detailUrl,bankNo,multiFlag);	//New node creation...
			node.nodeParent = pnode;  //Set it's parent
			//调整父节点数组
			node.nodeParents[0] = pnode;
			
			pnode.canCollapse = true; //It's obvious that now the parent can collapse	
			var i = this.nDatabaseNodes.length;	//Save it in database
			node.dbIndex = this.mapIDs[id] = i;	 
			this.nDatabaseNodes[i] = node;	
			var h = pnode.nodeChildren.length; //Add it as child of it's parent
			node.siblingIndex = h;
			pnode.nodeChildren[h] = node;
		}
}

ECOTree.prototype.searchNodes = function (str) {
	var node = null;
	var m = this.config.searchMode;
	var sm = (this.config.selectMode == ECOTree.SL_SINGLE);	 
	
	if (typeof str == "undefined") return;
	if (str == "") return;
	
	var found = false;
	var n = (sm) ? this.iLastSearch : 0;
	if (n == this.nDatabaseNodes.length) n = this.iLastSeach = 0;
	
	str = str.toLocaleUpperCase();
	
	for (; n < this.nDatabaseNodes.length; n++)
	{ 		
		node = this.nDatabaseNodes[n];				
		if (node.dsc.toLocaleUpperCase().indexOf(str) != -1 && ((m == ECOTree.SM_DSC) || (m == ECOTree.SM_BOTH))) { node._setAncestorsExpanded(); this._selectNodeInt(node.dbIndex, false); found = true; }
		if (node.meta.toLocaleUpperCase().indexOf(str) != -1 && ((m == ECOTree.SM_META) || (m == ECOTree.SM_BOTH))) { node._setAncestorsExpanded(); this._selectNodeInt(node.dbIndex, false); found = true; }
		if (sm && found) {this.iLastSearch = n + 1; break;}
	}	
	this.UpdateTree();	
}

ECOTree.prototype.selectAll = function () {
	if (this.config.selectMode != ECOTree.SL_MULTIPLE) return;
	this._selectAllInt(true);
}

ECOTree.prototype.unselectAll = function () {
	this._selectAllInt(false);
}

ECOTree.prototype.collapseAll = function () {
	this._collapseAllInt(true);
}

ECOTree.prototype.expandAll = function () {
	this._collapseAllInt(false);
}

ECOTree.prototype.collapseNode = function (nodeid, upd) {
	var dbindex = this.mapIDs[nodeid];
	this.nDatabaseNodes[dbindex].isCollapsed = !this.nDatabaseNodes[dbindex].isCollapsed;
	if (upd) this.UpdateTree();
}

ECOTree.prototype.selectNode = function (nodeid, upd) {		
	this._selectNodeInt(this.mapIDs[nodeid], true);
	if (upd) this.UpdateTree();
}

ECOTree.prototype.setNodeTitle = function (nodeid, title, upd) {
	var dbindex = this.mapIDs[nodeid];
	this.nDatabaseNodes[dbindex].dsc = title;
	if (upd) this.UpdateTree();
}

ECOTree.prototype.setNodeMetadata = function (nodeid, meta, upd) {
	var dbindex = this.mapIDs[nodeid];
	this.nDatabaseNodes[dbindex].meta = meta;
	if (upd) this.UpdateTree();
}

ECOTree.prototype.setNodeTarget = function (nodeid, target, upd) {
	var dbindex = this.mapIDs[nodeid];
	this.nDatabaseNodes[dbindex].target = target;
	if (upd) this.UpdateTree();	
}

ECOTree.prototype.setNodeColors = function (nodeid, color, border, upd) {
	var dbindex = this.mapIDs[nodeid];
	if (color) this.nDatabaseNodes[dbindex].c = color;
	if (border) this.nDatabaseNodes[dbindex].bc = border;
	if (upd) this.UpdateTree();	
}

ECOTree.prototype.getSelectedNodes = function () {
	var node = null;
	var selection = [];
	var selnode = null;	
	
	for (var n=0; n<this.nDatabaseNodes.length; n++) {
		node = this.nDatabaseNodes[n];
		if (node.isSelected)
		{			
			selnode = {
				"id" : node.id,
				"dsc" : node.dsc,
				"meta" : node.meta
			}
			selection[selection.length] = selnode;
		}
	}
	return selection;
}

ECOTree.prototype._setCanvas = function () {
	document.getElementById("canvas").width = canvasWidth + this.config.defaultNodeWidth + 50;
	document.getElementById("canvas").height = canvasHeight + this.config.defaultNodeHeight + 50;
}

ECOTree.prototype._div = function(exp1, exp2)
{
    var n1 = Math.round(exp1);
    var n2 = Math.round(exp2);
    
    var rslt = n1 / n2;
    
    if (rslt >= 0)
    {
        rslt = Math.floor(rslt); 
    }
    else
    {
        rslt = Math.ceil(rslt); 
    }
    
    return rslt;
}