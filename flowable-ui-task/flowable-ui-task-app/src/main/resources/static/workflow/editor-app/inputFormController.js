'use strict';
// angular.module('flowableApp')
//     .controller('InOutbtnGroupController',['$scope', '$modal', '$timeout', '$translate'
//             , function ($scope, $modal, $timeout, $translate) {
//
// }]);
angular.module('flowableApp')
    .controller('inputFormController',['$rootScope', '$scope', '$http', '$modal', '$timeout', '$window', 'editorManager',
        function ($rootScope, $scope, $http, $modal, $timeout, $window, editorManager) {
        // $scope.inputForm = {
        //         inputParamsName:'',
        //         inputParamsType:'',
        //         inputParamsValue:''
        // }
        var selectedTaskListensers=$scope.selectedItem.properties[18].value.taskListeners;

        $scope.send =function(inputForm){
                if($scope.selectedItem.properties[18].value!=undefined){
                        //加 判断value是否为undefined
                        if(selectedTaskListensers){
                                for(var i=0;i<selectedTaskListensers.length;i++){
                                        if(selectedTaskListensers[i].delegateExpression == '${apiHandler}'){
                                                var selectedTaskListenserInputParams=selectedTaskListensers[i].fields[1].stringValue;
                                                for(var j=0;j<selectedTaskListensers[i].fields.length;j++){
                                                        if(selectedTaskListensers[i].fields[j].name==selectedTaskListensers[i].fields[1].stringValue){
                                                                console.log($scope.inputForm.inputParamsValue);
                                                                selectedTaskListensers[i].fields[j].stringValue=$scope.inputForm.inputParamsValue;
                                                                console.log($scope.selectedItem.properties[18].value.taskListeners);
                                                                // $scope.updatePropertyInModel( $scope.selectedItem.properties[18]);
                                                        }
                                                }
                                        }
                                }

                        }

                }else{
                        $scope.selectedItem.properties[18].value={};
                }

              // $scope.updatePropertyInModel( $scope.selectedItem.properties[18]);
              $scope.close();

        };
        $scope.cancel=function () {
                $scope.close();
        }
        $scope.close=function () {
                $scope.selectedItem.properties[18].mode = 'read';
                $scope.$hide();
        }


        }])