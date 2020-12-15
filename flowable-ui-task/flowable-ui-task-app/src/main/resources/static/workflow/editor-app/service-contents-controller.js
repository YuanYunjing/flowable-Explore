/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

angular.module('flowableApp')
    .controller('ServiceContentsController',['editorManager','$modal', '$rootScope', '$scope', '$q', '$http',
    function(editorManager, $modal, $rootScope, $scope, $q, $http){
        $scope.serviceContentsTree = {};

        $scope.getServiceContentsFields = function(){
            var deferred = $q.defer();//创建一个deferred延迟对象实例,实例实例旨在暴露派生的Promise 实例，Promise就是一种对执行结果不确定的一种预先定义，如果成功，就xx；如果失败，就xx，就像事先给出了一些承诺。
            $http({method: 'GET', url: FLOWABLE.URL.getServiceContentsFieldsUrl()}).
            success(function (response) {
                deferred.resolve(response);
            }).
            error(function (response) {
                deferred.reject(response);
                console.log('get service contents fields error');
            });
            return deferred.promise;
        }

        $scope.getServices = function(id, limitNum){
            var deferred = $q.defer();
            $http({method: 'GET', url: FLOWABLE.URL.getServicesUrl(id, limitNum)}).
            success(function (response) {
                deferred.resolve(response);
            }).
            error(function (response) {
                deferred.reject(response);
                console.log('get services error');
            });
            return deferred.promise;
        }
        //查询-领域目录下原子服务高亮显示-代码修改版本2
        var fieldServices=[];
        $scope.looktype = function(filedname){
            $scope.TaskclearColor();

            if(filedname==='科技') {

                //fieldServices =[];
                //获取对应树的名字
                getTreetype(filedname);
                //遍历获取出相应的名字
                //fieldServices = ["成果查询结果聚合", "机构查询结果聚合", "查看服务列表", "技术服务申请", "查新服务", "技术预见服务", "意见反馈", "中信所成果查询", "河南院成果查询", "中信所机构查询", "河南院机构查询", "行业专家主题咨询", "专家库查询", "聚合结果查询", "文献查询结果聚合", "专利查询结果聚合", "所有查询结果聚合", "主题发现", "河南院文献查询", "中信所文献查询", "河南院专利查询", "中信所专利查询"];
                var changedColor2='yellow'
                changeFieldServiceColor(changedColor2);
            }else if(filedname=='燃料电池堆'){
                getTreetype(filedname);
                //fieldServices = ["中信所成果查询","河南院成果查询","中信所机构查询","河南院机构查询","专家库查询", "中信所文献查询","河南院专利查询","主题发现","中信所专利查询","河南院文献查询"];
                var changedColor1='greenyellow'
                changeFieldServiceColor(changedColor1);
            }else{
                fieldServices=[];
            }
        }

        $scope.TaskclearColor = function() {
            var stencilsNode=editorManager.getCanvas().node.children[0];
            var stencilNodeChildrenList=stencilsNode.children[1].children;

            for (var i=0;i<stencilNodeChildrenList.length;i++){
                // console.log(stencilNodeChildrenList[i]);
                var colorNode=stencilNodeChildrenList[i].children[0].children[0].children[0];
                colorNode.children[1].setAttribute('fill','white');
            }
        }
        function changeFieldServiceColor(changedColor){
            // console.log(editorManager.getCanvas().node);//<g>标签，class=stencils的父标签
            var stencilsNode=editorManager.getCanvas().node.children[0];
            var stencilNodeChildrenList=stencilsNode.children[1].children;

            for (var i=0;i<stencilNodeChildrenList.length;i++){
                // console.log(stencilNodeChildrenList[i]);
                var colorNode=stencilNodeChildrenList[i].children[0].children[0].children[0];
                var textNode=stencilNodeChildrenList[i].children[0].children[0].children[0].children[2];

                if(textNode!=undefined&&textNode.children[0]!=undefined){
                    // console.log(textNode.children[0].innerHTML);
                    for(var j=0;j<fieldServices.length;j++){
                        if(textNode.children[0].innerHTML==fieldServices[j]){  //画板j任务中存在科技领域目录下原子服务
                            // var colorNode=document.getElementById('svg-'+gNodeId).children[0].children[0].children[0];
                            colorNode.children[1].setAttribute('fill',changedColor);
                        }
                    }
                }
            }

        }
        function getTreetype(type){
            let servicenames = $scope.serviceContentsTree;
            let length = $scope.serviceContentsTree.length;
            for (var i = 0; i < length; i++) {

                let servername = servicenames[i];
                var k=0;
                if (servername.fieldName === type) {
                    console.log(servername.children);
                    for(var j=0;j<servername.children.length;j++){
                        fieldServices[k]=servername.children[j].userTask.name;
                        k++;
                    }
                }
            }
        }
        $scope.getServiceInfo = function(id){
            var deferred = $q.defer();
            $http({method: 'GET', url: FLOWABLE.URL.getServiceInfoUrl(id)}).
            success(function (response) {
                deferred.resolve(response);
            }).
            error(function (response) {
                deferred.reject(response);
                console.log('get serviceInfo error');
            });
            return deferred.promise;
        }

        function toTreeData(data, pid){
            function tree(id){
                let arr = [];
                // 过滤出数组中符合条件的元素，构成一个新数组
                let parents = data.filter(item =>{
                    return item.parentId ==id
                })
                // if (parents.length == 0) {
                if (id == 17 || id == 18) {
                    // console.time('for');
                    $scope.getServices(id, 15).then(function (response) {  // limitNum: 设置获取服务个数
                        let services = response.data;
                        let stencilItem = $scope.getStencilItemById('UserTask');
                        let length = services.length
                        // if(length > 10) length = 10;

                        for(let i=0; i<length; i++){
                            let service = services[i];
                            $scope.getServiceInfo(service.serviceId).then(function (response) {
                                // response是一个具体服务的返回信息，返回的有输入参数，输出参数类型等
                                let service_name=service.serviceId;
                                let inputParamsEntityList=response.inputParamEntityList;

                                // console.log(response.inputParamEntityList[0].parameterName);
                                // console.log(inputParamsEntityList);
                                let userTask = Object.assign({}, stencilItem,
                                    {name: service.serviceName,
                                        accessPoint: service.accessPoint,
                                        description: service.serviceDescription,
                                        service_name:service.serviceId,
                                        inputParamsEntityList:response.inputParamEntityList
                                    });
                                // 用户任务写进树中作为叶子结点
                                arr.push({
                                    userTask: userTask
                                })
                            })
                        }

                    })
                    // console.timeEnd('for');
                }
                if(id == 0) {
                    // 对每个元素执行一次给定的函数
                    for(let i=0; i<parents.length; i++){
                        let item = parents[i];
                        if(item.fieldId == 17 || item.fieldId == 18) {
                            arr.push({
                                fieldId: item.fieldId,
                                fieldName:item.fieldName,
                                children: tree(item.fieldId)
                            })
                        }

                    }
                }
                return arr
            }
            return tree(pid)// 第一级节点的父id，是null或者0，视情况传入
        }

        $scope.getServiceContentsFields().then(function(response){
            let fields = response.data;
            $scope.serviceContentsTree = toTreeData(fields,0);
        });


}]);
