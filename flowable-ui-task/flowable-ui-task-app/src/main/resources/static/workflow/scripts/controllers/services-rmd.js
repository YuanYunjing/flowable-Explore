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
 *
 * author by chengaojian
 */
'use strict';

var app = angular.module('flowableApp');
app.controller('RmdServesController', ['$rootScope','$scope', 'RMDServeService',
        function ($rootScope,$scope,RMDServeService) {
            $scope.$on('recall', function(e,rmdList,taskName) {
                $scope.Drawdiagram(rmdList,taskName);
            });
            //通过全局函数调用局部函数
            // $rootScope.DrawDiagram=function(rmdList,taskName){
            //     $scope.Drawdiagram(rmdList,taskName);
            // }s

            $scope.Drawdiagram=function(rmdList,taskName){
                var parkaccountChart = echarts.init(document.getElementById('id0001'));//div 标签id
                var seriesLabel = {
                    normal: {
                        show: true,
                        textBorderColor: '#333',
                        textBorderWidth: 2
                    }
                };
                //需要做的就是将rmdList转换到data中
                let data = {
                    nodes: [],
                    links: [],
                };
                let node = {name: taskName, category: '000', relevance:0};
                data.nodes.push(node);
                rmdList.forEach(rmd => {
                    let node = {name: rmd.serviceName, category: rmd.serviceID, relevance:rmd.relevance};
                    data.nodes.push(node);

                    let link = {source: taskName, target: rmd.serviceName, name: rmd.relevance};
                    data.links.push(link);

                })
                const color1 = '#006acc';
                const color2 = '#ff7d18';
                const color3 = '#10a050';

                data.nodes.forEach(node => {
                    if (node.relevance === 1) {
                        node.symbolSize = 35;
                        node.itemStyle = {
                            color: color1
                        };
                    } else if (node.relevance === 2) {
                        node.symbolSize = 30;
                        node.itemStyle = {
                            color: color2
                        };
                    }else if (node.relevance === 3) {
                        node.symbolSize = 25;
                        node.itemStyle = {
                            color: color3
                        };
                    }else if (node.relevance === 0) {
                        node.symbolSize = 35;
                        node.itemStyle = {
                            color: "blue"
                        };
                    }
                });

                data.links.forEach(link => {
                    link.label = {
                        align: 'center',
                        color: "blue",
                        fontSize: 20
                    };

                    if (link.name === 1) {
                        link.lineStyle = {
                            color: color1
                        }
                    } else if (link.name === 2) {
                        link.lineStyle = {
                            color: color2
                        }
                    } else if (link.name === 3) {
                        link.lineStyle = {
                            color: color3
                        }
                    }
                });

                let option = {
                    // title: {
                    //     text: '推荐拓扑图',
                    // },
                    series: [{
                        type: 'graph',
                        layout: 'force',
                        symbolSize: 28,
                        draggable: true,
                        roam: true,
                        focusNodeAdjacency: true,
                        edgeSymbol: ['', 'arrow'],
                        edgeLabel: {
                            normal: {
                                show: true,
                                textStyle: {
                                    fontSize: 20
                                },
                                formatter(x) {
                                    return x.data.name;
                                }
                            }
                        },
                        label: {
                            show: true
                        },
                        force: {
                            repulsion: 600,
                            edgeLength: 60
                        },
                        data: data.nodes,
                        links: data.links
                    }]
                }
                parkaccountChart.setOption(option);
                //获取点击事件进行相应的触发
                parkaccountChart.on('click', function (params) {
                    console.log(params);
                    console.log(params.name,params.data.category);
                    $scope.selectRMDServeByServiceID(params.name, params.data.category);
                });

            }

            $scope.selectRMDServeByServiceName = function(serviceName) {

                RMDServeService.getRMDServeByServiceName(serviceName).then(function(response) {
                    // 每一个属性都要单独赋值
                    $scope.selectedRMDServe = response;
                    $scope.selectedRMDServe.serviceName = response.serviceName;
                    $scope.selectedRMDServe.accessPoint = response.accessPoint;

                    let stencilItem = $scope.getStencilItemById('UserTask');
                    $scope.userTask = Object.assign({}, stencilItem,
                        {name: $scope.selectedRMDServe.serviceName, accessPoint: $scope.selectedRMDServe.accessPoint});
                });
            }


            // 根据Name, ID推荐, 只有name无法获得输入输出
            $scope.selectRMDServeByServiceID = function(serviceName, serviceID) {

                RMDServeService.getServiceInfoByServiceId(serviceID).then(function(responseInput) {

                    RMDServeService.getRMDServeByServiceName(serviceName).then(function(responseName) {

                        // 修改从父controller传来的$scope.selectedRMDServe，修改其属性
                        // 不可以在此处重新定义$scope.selectedRMDServe，否则父controller中的该变量将不起作用
                        $scope.selectedRMDServe.serviceID = serviceID;
                        $scope.selectedRMDServe.serviceName = responseName.serviceName;
                        $scope.selectedRMDServe.accessPoint = responseName.accessPoint;
                        $scope.selectedRMDServe.inputParamsEntityList = responseInput.inputParamEntityList;
                        $scope.selectedRMDServe.serviceName = responseName.serviceName;
                        $scope.selectedRMDServe.accessPoint = responseName.accessPoint;

                        let stencilItem = $scope.getStencilItemById('UserTask');
                        $scope.userTask.value = Object.assign({}, stencilItem,
                            {
                                name: $scope.selectedRMDServe.serviceName,
                                accessPoint: $scope.selectedRMDServe.accessPoint,
                                service_name: serviceID,
                                inputParamsEntityList: responseInput.inputParamEntityList
                            });


                    });


                });
            }

        }]);
