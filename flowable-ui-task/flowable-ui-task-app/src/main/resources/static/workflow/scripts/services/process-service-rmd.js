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
'use strict';

// Task service
angular.module('flowableApp').service('ProcessService', ['$http', '$q', '$rootScope', 'RelatedContentService',
    function ($http, $q, $rootScope, RelatedContentService) {

        this.name1="gaojian1";
        var httpAsPromise = function(options) {
            var deferred = $q.defer();
            $http(options).
                success(function (response, status, headers, config) {
                    deferred.resolve(response);
                })
                .error(function (response, status, headers, config) {
                    deferred.reject(response);
                });
            return deferred.promise;
        };

        this.getProcessDefinitions = function(appDefinitionKey) {
            var url = FLOWABLE.CONFIG.contextRoot + '/app/rest/process-definitions?latest=true';
            if (appDefinitionKey) {
                url += '&appDefinitionKey=' + appDefinitionKey;
            }
            return httpAsPromise(
                {
                    method: 'GET',
                    url: url
                }
            );
        };
        // 获得推荐流程定义列表
        this.getRMDProcessDefinitions = function(appDefinitionKey) {
            var url = FLOWABLE.CONFIG.contextRoot + '/app/rest/rmd-process-definitions';
            return httpAsPromise(
                {
                    method: 'GET',
                    url: url
                }
            );
        };
        // 根据流程定义id获得流程定义process0
        this.getProcessDefinition = function(processDefinitionId) {
            var url = FLOWABLE.CONFIG.contextRoot + '/app/rest/process-definition?processDefinitionId=';
            // 拼接的方式可以，?以参数的方式也可以
            if (processDefinitionId) {
                url += processDefinitionId ;
            }
            console.log(url);

            var deferred = $q.defer();
            $http({
                method: 'GET',
                url: url,
                data: processDefinitionId
            }).success(function (response, status, headers, config) {
                deferred.resolve(response);

            }).error(function (response, status, headers, config) {
                deferred.reject(response);
            });

            var promise = deferred.promise;
            return promise;

        };

        this.getProcessDefinitions2 = function(Key1,Key2) {
            var deferred = $q.defer();
            $http({
                method: 'GET',
                url: FLOWABLE.CONFIG.contextRoot + '/app/rest/mergepro1?def1_id='+Key1+'&def2_id='+Key2,
            }).success(function (response, status, headers, config) {
                $rootScope.$broadcast('new-process-created', response);
                deferred.resolve(response);
            }).error(function (response, status, headers, config) {
                $rootScope.addAlert(response.message, 'error');
                deferred.reject(response);
            });

            var promise = deferred.promise;
            return promise;
        };
        this.getProcessDefinitions3 = function(Key1,Key2,Key3) {
            var deferred = $q.defer();
            $http({
                method: 'GET',
                url: FLOWABLE.CONFIG.contextRoot + '/app/rest/InjectSubProcess?currentTaskDefId='+Key1+'&currentProcessInstanceId='+Key2+'&recomment_service_processDefId='+Key3,
            }).success(function (response, status, headers, config) {
                $rootScope.$broadcast('new-process-created', response);
                deferred.resolve(response);
            }).error(function (response, status, headers, config) {
                $rootScope.addAlert(response.message, 'error');
                deferred.reject(response);
            });

            var promise = deferred.promise;
            return promise;
        };

        // 根据流程实例id流程实例
        this.getProcessInstance = function(processInstanceId){
            console.log(processInstanceId)
            var deferred = $q.defer();
            $http({method: 'GET', url: FLOWABLE.CONFIG.contextRoot + '/app/rest/process-instances/' + processInstanceId}).
                success(function (response, status, headers, config) {
                    deferred.resolve(response);
                }).
                error(function (response, status, headers, config) {
                    deferred.reject(response);
                    console.log('Something went wrong: ' + response);
                });
                var promise = deferred.promise;
                return promise;

        }

        this.createProcess = function(processData) {
            var deferred = $q.defer();
            $http({
                method: 'POST',
                url: FLOWABLE.CONFIG.contextRoot + '/app/rest/process-instances',
                data: processData
            }).success(function (response, status, headers, config) {
                $rootScope.$broadcast('new-process-created', response);
                deferred.resolve(response);
            }).error(function (response, status, headers, config) {
                $rootScope.addAlert(response.message, 'error');
                deferred.reject(response);
            });

            var promise = deferred.promise;
            return promise;
        },
        this.deleteProcess = function(processInstanceId) {
            var deferred = $q.defer();
            $http({
                method: 'DELETE',
                url: FLOWABLE.CONFIG.contextRoot + '/app/rest/process-instances/' + processInstanceId
            }).success(function (response, status, headers, config) {
                $rootScope.$broadcast('processinstance-deleted', response);
                deferred.resolve(response);
            }).error(function (response, status, headers, config) {
                $rootScope.addAlert(response.message, 'error');
                deferred.reject(response);
            });

            var promise = deferred.promise;
            return promise;
        }

    }]
);
