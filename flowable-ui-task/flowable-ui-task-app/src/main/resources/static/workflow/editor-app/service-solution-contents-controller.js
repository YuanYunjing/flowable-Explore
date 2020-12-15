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
    .controller('ServiceSolutionContentsController',['editorManager','$modal', '$rootScope', '$scope', '$q', '$http',
    function(editorManager, $modal, $rootScope, $scope, $q, $http){

        $scope.serviceSolutions = {};

        $scope.getServiceSolutionContents = function(userId){
            var deferred = $q.defer();  // 创建一个deferred延迟对象实例,实例实例旨在暴露派生的Promise 实例，Promise就是一种对执行结果不确定的一种预先定义，如果成功，就xx；如果失败，就xx，就像事先给出了一些承诺。
            $http({method: 'GET', url: FLOWABLE.URL.getServiceSolutionContentsUrl(userId)}).
            success(function (response) {
                deferred.resolve(response);
            }).
            error(function (response) {
                deferred.reject(response);
                console.log('get service solution contents error');
            });
            return deferred.promise;
        }


        function unique(arr){
            var hash=[];
            for (var i = 0; i < arr.length; i++) {
                if(hash.indexOf(arr[i])==-1){
                    hash.push(arr[i]);
                }
            }
            return hash;
        }

        // function getChildren (arr, name){  // 版本没有递增排序
        //     var children = [];
        //     arr.forEach(item => {
        //         if(item.name == name) {
        //             children.push({
        //                 version: item.version
        //             })
        //         }
        //     })
        //
        //     return children;
        // }

        function getChildren (arr, name){  // 通过获得最大版本，实现递增排序
            var children = [];
            var latestVersion = 0;
            arr.forEach(item => {
                if(item.name == name) {
                    if(item.version > latestVersion) latestVersion = item.version;
                }
            })
            for(var i=1; i<=latestVersion; i++){
                children.push({
                    version: i
                })
            }
            return children;
        }

        $scope.getServiceSolutionContents($rootScope.account.id).then(function(response){
            var processDefinitions = response.data;
            var length = processDefinitions.length;
            var arr = [];
            for(let i=0; i<length; i++){  // 构建名字列表
                arr.push(processDefinitions[i].name);
            }
            arr = unique(arr);  // 去重

            var serviceSolutions = [];
            arr.forEach(name => {  // 构建一棵名字-版本树（名字作为根节点、版本作为叶节点）
                serviceSolutions.push({
                    name: name,
                    children: getChildren(processDefinitions, name)
                })
            })
            $scope.serviceSolutions = serviceSolutions;
        })




}]);
