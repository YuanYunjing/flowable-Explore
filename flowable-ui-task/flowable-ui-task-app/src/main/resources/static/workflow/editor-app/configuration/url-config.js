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
var FLOWABLE = FLOWABLE || {};

FLOWABLE.URL = {

    getModel: function(processInstanceId) {
        return FLOWABLE.CONFIG.contextRoot + '/app/rest/models2/' + processInstanceId + '/editor/json?version=' + Date.now();    },

    getStencilSet: function() {
        return FLOWABLE.CONFIG.contextRoot + '/app/rest/stencil-sets/editor?version=' + Date.now();
    },
    
    getCmmnStencilSet: function() {
        return FLOWABLE.CONFIG.contextRoot + '/app/rest/stencil-sets/cmmneditor?version=' + Date.now();
    },

    putModel: function(processInstanceId) {
        return FLOWABLE.CONFIG.contextRoot + '/app/rest/models2/' + processInstanceId + '/editor/json';
    },
    
    validateModel: function(){
		return FLOWABLE.CONFIG.contextRoot + '/app/rest/model/validate';
    },
    evolutionModel:function(processInstanceId){
        return FLOWABLE.CONFIG.contextRoot + '/app/rest/evolution/' + processInstanceId + '/editor/json';
    },


    /* SERVICE CONTENTS URLS */
    getServiceContentsFieldsUrl: function () {
        return FLOWABLE.CONFIG.contextRoot + '/app/rest/service-contents';
    },
    getServicesUrl: function (id, limitNum) {
        return FLOWABLE.CONFIG.contextRoot + '/app/rest/service-contents/services' + '?id=' + id + '&limitnum=' + limitNum;
    },
    getServiceInfoUrl: function (id) {
        return FLOWABLE.CONFIG.contextRoot + '/app/rest/service-contents/services/serviceInfo' + '?id=' + id;
    },
    getServiceInfoResult: function (processInstanceId,key) {
        return FLOWABLE.CONFIG.contextRoot + '/app/rest/GetprocessVarible/'+processInstanceId+'/'+key;
    },
    /* SERVICE SOLUTION CONTENTS URLS */
    getServiceSolutionContentsUrl: function (userId) {
        return FLOWABLE.CONFIG.contextRoot + '/app/rest/service-solution-contents' + '?userId=' + userId;
    }
};