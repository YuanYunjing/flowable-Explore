/*
 * output grid
 */
'use strict';

var FLOWABLE = FLOWABLE || {};


angular.module('flowableApp').controller('OutputStringCtrl',['$scope', '$http', 'editorManager','$timeout','$window',
    function ($scope, $http, editorManager,$timeout,$window) {


        console.log($scope.result);

        $scope.status = {
            loading: true
        };

        $scope.stringResult = {
            result: []
        };
        $scope.resultGridString = {
            data: $scope.stringResult.result,
            headerRowHeight: 28,
            enableRowSelection: true,
            enableRowHeaderSelection: false,
            multiSelect: false,
            modifierKeysToMultiSelect: false,
            enableHorizontalScrollbar: 0,
            enableColumnMenus: false,
            enableSorting: false,
            columnDefs: [
                {field: 'value', displayName: 'result'}
            ]
        };

        $scope.resultGridString.onRegisterApi = function(gridApi) {
            //set gridApi on scope
            $scope.gridApi = gridApi;
        };
        //一行结果显示
        // var stringResultsList=$scope.result;
        // //将一维数组转换成json
        // var stringResultsListData=[]; stringResultsListData[0]={};
        // stringResultsListData[0]['value']=''+stringResultsList;
        // var resStringResultsListData=JSON.stringify(stringResultsListData);
        // //将获得结果写入list
        // $scope.status.loading = false;
        // stringResultsListData.forEach(function (row) {
        //     $scope.stringResult.result.push(row);
        // });

        //在多行演示
        var stringResultsList=$scope.result;
        //将一维数组转换成json
        var stringResultsListData=[];
        // stringResultsListData[0]={};
        // stringResultsListData[0]['value']=''+stringResultsList;
        for(var i=0;i<stringResultsList.length;i++){
            stringResultsListData[i]={};
            stringResultsListData[i]['value']=''+stringResultsList[i];
        }
        // console.log(stringResultsListData);
        var resStringResultsListData=JSON.stringify(stringResultsListData);
        // console.log(resStringResultsListData);
        //将获得结果写入list
        $scope.status.loading = false;
        stringResultsListData.forEach(function (row) {
            $scope.stringResult.result.push(row);
        });

    }
]);

angular.module('flowableApp').controller('OutputObjectCtrl',['$scope', '$http', 'editorManager','$timeout','$window',
    function ($scope, $http, editorManager,$timeout,$window) {
        console.log($scope.result);

        $scope.status = {
            loading: true
        };

        $scope.objectResult = {
            result: []
        };
        $scope.status.loading = false;
        $scope.resultGridObject = {
            data: $scope.result,
            headerRowHeight: 28,
            enableRowSelection: true,
            enableRowHeaderSelection: false,
            multiSelect: false,
            modifierKeysToMultiSelect: false,
            enableHorizontalScrollbar: 0,
            enableColumnMenus: false,
            enableSorting: false,
            columnDefs: [
                {field: 'name', displayName: 'name'},
                {field: 'subordinate units', displayName: 'subordinate units'},
                {field: 'focus area',displayName: 'focus area'}
            ]
        };

        $scope.resultGridObject.onRegisterApi = function(gridApi) {
            //set gridApi on scope
            $scope.gridApi = gridApi;
        };
    }
]);
