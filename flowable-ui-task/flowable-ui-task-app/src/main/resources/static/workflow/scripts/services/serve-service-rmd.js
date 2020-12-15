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
angular.module('flowableApp').service('RMDServeService', ['$http', '$q',
    function ($http, $q) {


        // 根据服务名称获得服务
        this.getRMDServeByServiceName = function(serviceName) {
            var url = FLOWABLE.CONFIG.contextRoot + '/app/rest/rmd-serve?serviceName=';
            // 拼接的方式可以，?以参数的方式也可以
            if (serviceName) {
                url += serviceName ;
            }

            var deferred = $q.defer();
            $http({
                method: 'GET',
                url: url,
                data: serviceName
            }).success(function (response) {
                deferred.resolve(response);

            }).error(function (response) {
                deferred.reject(response);
            });

            var promise = deferred.promise;
            return promise;

        };

        this.getServiceInfoByServiceId = function(id){
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


    }]
);
