//服务层
app.service('searchService',function($http){
	    	
	//读取列表数据绑定到表单中
	this.search=function(searchMap){
		return $http.post('../item/search.do',searchMap);
	}

});