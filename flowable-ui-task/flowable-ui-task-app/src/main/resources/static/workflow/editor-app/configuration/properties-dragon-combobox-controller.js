angular.module('flowableModeler').controller('DragonNodetypeComboboxCtrl',
    [ '$scope', '$modal', '$http', function($scope, $modal, $http) {
        if ($scope.property.value == undefined && $scope.property.value == null) {
            $scope.property.value = '';
        }
        //请求数据
        //url 你可以请求你后台的rest接口来获取数据对象
        $http({
            method: 'GET',
            url: FLOWABLE.URL.getNodeProertyInfos('node_type')
        }).then(function successCallback(response) {
            $scope.nodeTypes = response.data.data;
        }, function errorCallback(response) {
            // 请求失败执行代码
        });
        $scope.comboValueChanged = function (item) {
            $scope.property.value = item;
            for (var i = 0; i < $scope.nodeTypes.length; i++) {
                if ($scope.nodeTypes[i].sn == item) {
                    $scope.property.text = $scope.nodeTypes[i].name;
                }
            }
            $scope.updatePropertyInModel($scope.property);
        };
    }]);
