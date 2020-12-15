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
package org.flowable.ui.task.rest.runtime;

import org.flowable.ui.common.model.ResultListDataRepresentation;
import org.flowable.ui.task.service.runtime.FlowableProcessDefinitionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * REST controller for managing the Engine process definitions.
 */
@RestController
@RequestMapping("/app")
public class ProcessDefinitionsResource {

    @Autowired
    protected FlowableProcessDefinitionService processDefinitionService;

    @GetMapping(value = "/rest/process-definitions")
    public ResultListDataRepresentation getProcessDefinitions(@RequestParam(value = "latest", required = false) Boolean latest,
            @RequestParam(value = "appDefinitionKey", required = false) String appDefinitionKey) {

        return processDefinitionService.getProcessDefinitions(latest, appDefinitionKey);
    }

//    @GetMapping(value = "/rest/rmd-process-definitions")
//    public ResultListDataRepresentation getRmdProcessDefinitions() {
//        Set<String> rmdProcessDefinitionIds = new HashSet<String>();
//                rmdProcessDefinitionIds.add("a111:2:3d9b2cbf-c1c1-11ea-82e9-fae4e3d6256d");
//                rmdProcessDefinitionIds.add("a222:1:3ac29147-c1c1-11ea-82e9-fae4e3d6256d");
//                rmdProcessDefinitionIds.add("a444:1:a6187147-c1c1-11ea-82e9-fae4e3d6256d");
//        return processDefinitionService.getRmdProcessDefinitions(rmdProcessDefinitionIds);
//    }
    @GetMapping(value = "/rest/rmd-process-definitions")
    public ResultListDataRepresentation getRmdProcessDefinitions(@RequestParam(value = "actId", required = false) String actId) {
        return processDefinitionService.getRmdProcessDefinitions(actId);
    }
}
