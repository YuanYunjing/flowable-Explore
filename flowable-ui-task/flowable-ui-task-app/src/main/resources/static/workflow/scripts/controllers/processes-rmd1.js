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

var app = angular.module('flowableApp');
app.controller('RmdProcessesController1', ['$rootScope', '$scope', '$q', '$translate', '$http', '$timeout', '$location', '$modal', '$routeParams', '$popover', 'appResourceRoot', 'ProcessService',
        function ($rootScope, $scope, $translate, $q, $http, $timeout, $location, $modal, $routeParams, $popover, appResourceRoot, ProcessService) {

        var processId = $routeParams.processId;
        // Ensure correct main page is set
        $rootScope.setMainPageById('processes');

        // Initialize model
        $scope.model = {
            page: 0,
            initialLoad: false,
            mode: 'process-create'
        };


        $scope.model.runtimeSorts = [
            { 'id': 'created-desc', 'title': 'PROCESS.FILTER.CREATED-DESC'},
            { 'id': 'created-asc', 'title': 'PROCESS.FILTER.CREATED-ASC' }
        ];

        $scope.model.completedSorts = [];
        $scope.model.completedSorts.push($scope.model.runtimeSorts[0]); // needs to be same reference!
        $scope.model.completedSorts.push($scope.model.runtimeSorts[1]); // needs to be same reference!
        $scope.model.completedSorts.push({ 'id': 'ended-asc', 'title': 'PROCESS.FILTER.ENDED-DESC' });
        $scope.model.completedSorts.push({ 'id': 'ended-desc', 'title': 'PROCESS.FILTER.ENDED-ASC' });

        $scope.model.sorts = $scope.model.runtimeSorts;

        $scope.model.stateFilterOptions = [
            { 'id': 'running', 'title': 'PROCESS.FILTER.STATE-RUNNING' },
            { 'id': 'completed', 'title': 'PROCESS.FILTER.STATE-COMPLETED' },
            { 'id': 'all', 'title': 'PROCESS.FILTER.STATE-ALL' }
        ];

        $scope.model.filter = {
            loading: false,
            expanded: false,
            param: {
                state: $scope.model.stateFilterOptions[0],
                sort: $scope.model.sorts[0].id
            }
        };

        $scope.appDefinitionKey = $routeParams.appDefinitionKey;
        $scope.missingAppdefinition = $scope.appDefinitionKey === false;

        // In case of viewing process instances in an app-context, need to make filter aware of this
        $scope.model.filter.param.appDefinitionKey = $scope.appDefinitionKey;

        // The filter is stored on the rootScope, which allows the user to switch back and forth without losing the filter.
        if ($rootScope.processFilter !== null && $rootScope.processFilter !== undefined) {
            $scope.model.filter.param = $rootScope.processFilter.param;
        } else {
            $rootScope.processFilter = { param: $scope.model.filter.param }
        }

        // Update app on rootScope. If app id present, it will fetch definition if not already fetched to update view and navigation accordingly

        $scope.selectProcessInstance = function (processInstance) {
            $scope.selectedProcessInstance = processInstance;
            $scope.state = {noProcesses:false};
        };

        $scope.expandFilter = function () {
            $scope.model.filter.expanded = true;
        };

        $scope.collapseFilter = function () {
            $scope.model.filter.expanded = false;
        };

        $scope.$watch("model.filter.param", function (newValue) {
            if (newValue) {
                if ($scope.model.initialLoad) {
                    $scope.loadProcessInstances();
                }

                if (newValue.state.id === 'completed' || newValue.state.id === 'all') {
                    $scope.model.sorts = $scope.model.completedSorts;
                } else {
                    $scope.model.sorts = $scope.model.runtimeSorts;
                    if (newValue.sort === 'ended-asc' || newValue.sort === 'ended-desc') {
                        $scope.model.filter.param.sort = $scope.model.sorts[0].id;
                    }
                }
            }
        }, true);

        $scope.nextPage = function () {
            console.log('here')
            $scope.loadProcessInstances(true);
            console.log('or there')
        };

        // TODO: move to service
        $scope.loadProcessInstances = function (nextPage) {

            $scope.model.filter.loading = true;

            var params = $scope.model.filter.param;

            if (nextPage) {
                $scope.model.page += 1;
            } else {
                $scope.model.page = 0;
            }

            var instanceQueryData = {
                sort: params.sort,
                page: $scope.model.page
            };

            if (params.appDefinitionKey) {
                instanceQueryData.appDefinitionKey = params.appDefinitionKey;
            }

            if (params.state) {
                instanceQueryData.state = params.state.id;
            }


            $http({method: 'POST', url: FLOWABLE.CONFIG.contextRoot + '/app/rest/query/process-instances', data: instanceQueryData}).
                success(function (response, status, headers, config) {
                    $scope.model.initialLoad = true;
                    var instances = response.data;

                    if (response.start > 0) {
                        // Add results instead of removing existing ones
                        for (var i = 0; i < instances.length; i++) {
                            $scope.model.processInstances.push(instances[i]);
                        }

                        $scope.state = {noProcesses: false};
                    } else {
                        $scope.model.processInstances = instances;
                        $scope.state = {noProcesses: (!response.data || response.data.length == 0)};
                    }

                    if (response.start + response.size < response.total) {
                        // More pages available
                        $scope.model.hasNextPage = true;
                    } else {
                        $scope.model.hasNextPage = false;
                    }

                    var isSelected = false;

                    if ($rootScope.root.selectedProcessId) {
                        for (var i = 0; i < instances.length; i++) {
                            if (instances[i].id == $rootScope.root.selectedProcessId) {
                                isSelected = true;
                                $scope.selectedProcessInstance = instances[i];
                                break;
                            }
                        }
                        $rootScope.root.selectedProcessId = undefined;
                    }
                    if (!isSelected && instances.length > 0) {
                        if (!$scope.selectedProcessInstance) {
                            $scope.selectedProcessInstance = instances[0];
                        }
                    }

                    // If there is a new process instance, we want it to be selected
                    if ($scope.newProcessInstance !== null && $scope.newProcessInstance !== undefined) {
                        if ($scope.newProcessInstance.id !== null && $scope.newProcessInstance.id !== undefined) {
                            for (var instanceIndex = 0; instanceIndex < $scope.model.processInstances.length; instanceIndex++) {
                                if ($scope.model.processInstances[instanceIndex].id === $scope.newProcessInstance.id) {
                                    $scope.selectedProcessInstance = $scope.model.processInstances[instanceIndex];
                                    break;
                                }
                            }
                        }
                       // Always reset when loading process instance
                        $scope.newProcessInstance = undefined;
                    }

                    $scope.model.filter.loading = false;
                    $rootScope.window.forceRefresh = true;
                }).
                error(function (response, status, headers, config) {
                    console.log('Something went wrong: ' + response);
                });
        };


        $scope.selectProcessDefinition = function (definition) {
            $scope.newProcessInstance.processDefinitionId = definition.id;
            $scope.newProcessInstance.id=definition.id;
            $scope.newProcessInstance.name = definition.name + ' - ' + new moment().format('MMMM Do YYYY');
            $scope.newProcessInstance.description = definition.description;
            $scope.newProcessInstance.category = definition.category;
            $scope.newProcessInstance.version = definition.version;
            $scope.newProcessInstance.processDefinition = definition;
            console.log("definition");
            console.log(definition);

//            $timeout(function () {
//                angular.element('#start-process-name').focus();
//            }, 20);
        };
        $scope.selectProcessDefinition2 = function (aid1,aid2) {

                ProcessService.getProcessDefinitions2(aid1,aid2).then(function(response) {
                    $rootScope.root.processDefinitions = response.data;
                    if ($scope.root.processDefinitions && $scope.root.processDefinitions.length > 0) {
                        for (var i=0; i< $scope.root.processDefinitions.length; i++) {
                            var def = $scope.root.processDefinitions[i];
                            if (def.id != 'default') {
                                $scope.selectProcessDefinition(def);
                                break;
                            }
                        }
                    }
                });

         };
        $scope.selectStateFilter = function (state) {
            if (state != $scope.model.filter.param.state) {
                $scope.model.filter.param.state = state;
                $scope.collapseFilter();
                $scope.selectedProcessInstance = undefined;
            }
        };

        $scope.sortChanged = function() {
            $scope.selectedProcessInstance = undefined;
        };

        // 根据appkey列出并选择流程定义
        $scope.selectDefaultDefinition = function() {
            // Select first non-default definition, if any
            ProcessService.getProcessDefinitions($scope.appDefinitionKey).then(function(response) {
            console.log("response1:");
            console.log(response);
            	$rootScope.root.processDefinitions = response.data;
	            if ($scope.root.processDefinitions && $scope.root.processDefinitions.length > 0) {
	                for (var i=0; i< $scope.root.processDefinitions.length; i++) {
	                    var def = $scope.root.processDefinitions[i];
	                    if (def.id != 'default') {
	                        $scope.selectProcessDefinition(def);
	                        break;
	                    }
	                }
	            }
	        });

        };

        // 固定要顯示的流程定義列表
        $scope.selectRmdDefaultDefinition = function() {
            // Select first non-default definition, if any
//            var rmdList = new List();
//            rmdList.append('askprocess:1:ef9dedc9-9b68-11ea-8fa1-dc5360336bee');
//            rmdList.append('helpprocess:1:ef9206e8-9b68-11ea-8fa1-dc5360336bee');
//            rmdList.append('programflow:3:5d46706a-99e1-11ea-ba06-dc5360336bee');
            $scope.appDefinitionKey="2";
            ProcessService.getRMDProcessDefinitions($scope.appDefinitionKey).then(function(response) {
                console.log("response1:");
                console.log(response);
                $rootScope.root.processDefinitions = response.data;
                for (var i=0; i< $scope.root.processDefinitions.length; i++)
                {
                    var def = $scope.root.processDefinitions[0];
                    $scope.root.processDefinitions[0]=$scope.root.processDefinitions[1];
                    $scope.root.processDefinitions[1]=def;
                }
                if ($scope.root.processDefinitions && $scope.root.processDefinitions.length > 0) {
                    for (var i=0; i< $scope.root.processDefinitions.length; i++) {
                        var def = $scope.root.processDefinitions[i];
                        if (def.id != 'default') {
                            $scope.selectProcessDefinition(def);
                            break;
                        }
                    }
                }
            });

        };

        // 根据流程定义id选择流程定义
        $scope.selectProcessDefinitionById = function(processDefinitionId) {
//            $rootScope.root.processDefinitionId1 = processDefinitionId;
            var definition;
            ProcessService.getProcessDefinition(processDefinitionId).then(function(response) {
               // 每一个属性都要单独赋值
                $scope.newProcessInstance.processDefinitionId = response.id;
                $scope.newProcessInstance.name = response.name;
                $scope.newProcessInstance.description = response.description;
                $scope.newProcessInstance.category = response.category;
                $scope.newProcessInstance.version = response.version;
                $scope.newProcessInstance.processDefinition= response;
            });

        };

        $scope.backToList = function(reloadProcessInstances) {

            $scope.newProcessInstance = undefined;

            $scope.model.mode = 'process-list';
            $scope.startFormError = undefined;

            // If param is true: reload, no questions asked
            if (reloadProcessInstances) {

                // Reset selection
                $scope.selectedProcessInstance = undefined;

                // Reset filters
                $scope.model.filter.param.state = $scope.model.stateFilterOptions[0];
                $scope.model.filter.param.sort = $scope.model.sorts[0].id;

                $scope.loadProcessInstances();
            }

            // In case we're coming from the task page, no process instances have been loaded
            if ($scope.model.processInstances === null || $scope.model.processInstances === undefined) {
                $scope.loadProcessInstances();
            }

        };

        // 点击 启动流程 按钮
        $scope.createRmdProcessInstance = function () {

            // Reset state
            $rootScope.root.showStartForm = false;

            $scope.model.mode = 'process-create';
            $scope.newProcessInstance = {};
            $scope.selectRmdDefaultDefinition();
        };

        $scope.createProcessInstance = function () {

            // Reset state
            $rootScope.root.showStartForm = false;

            $scope.model.mode = 'process-create';
            $scope.newProcessInstance = {};
            $scope.selectDefaultDefinition();
        };

        $scope.$on('process-started-error', function (event, data) {
            $rootScope.addAlert(data.error.message, 'error');
        });

        // Called after form is submitted
        $scope.$on('process-started', function (event, data) {
            $scope.newProcessInstance.id = data.id;
            $scope.backToList(true);
        });

        $scope.startProcessInstanceWithoutForm = function() {
            $scope.newProcessInstance.loading = true;
            var createInstanceData = {processDefinitionId: $scope.newProcessInstance.processDefinition.id, name: $scope.newProcessInstance.name};
            $http({method: 'POST', url: FLOWABLE.CONFIG.contextRoot + '/app/rest/process-instances', data: createInstanceData}).
                success(function (response, status, headers, config) {
                    $scope.newProcessInstance.id = response.id;
                    $scope.newProcessInstance.loading = false;
                    $scope.backToList(true);

                }).
                error(function (response, status, headers, config) {
                    $scope.newProcessInstance.loading = false;

                    if(response && response.messageKey) {
                        $translate(response.messageKey, response.customData).then(function(message) {
                            $scope.errorMessage = message;
                            console.log(message);
                        });
                    }
                });
        };
        $scope.startProcessInstanceWithoutFormById = function() {
                $scope.newProcessInstance.loading = true;
                $http({method: 'POST', url: FLOWABLE.CONFIG.contextRoot + '/app/rest/process-instances(id)', data: $scope.newProcessInstance.processDefinition.id}).
                    success(function (response, status, headers, config) {
                        $scope.newProcessInstance.id = response.id;
                        $scope.newProcessInstance.loading = false;
                        $scope.backToList(true);

                    }).
                    error(function (response, status, headers, config) {
                        $scope.newProcessInstance.loading = false;

                        if(response && response.messageKey) {
                            $translate(response.messageKey, response.customData).then(function(message) {
                                $scope.errorMessage = message;
                                console.log(message);
                            });
                        }
                    });
            };

//        $rootScope.loadProcessDefinitions($scope.appDefinitionKey);



//        $scope.merProcess() = function (processDefinitionIdA, processDefinitionIdB) {
//            console.log("A:");
//            console.log(processDefinitionIdA);
//            console.log("B:");
//            console.log(processDefinitionIdB);
//            alert("合并成功！");
//        };


        // If 'createProcessInstance' is set (eg from the task page)
        $rootScope.createRmdProcessInstance = true;
        if ($rootScope.createRmdProcessInstance) {
            $rootScope.createRmdProcessInstance = false;
            $scope.createRmdProcessInstance();
        } else {
            $scope.loadProcessInstances();
        }




    }]);

