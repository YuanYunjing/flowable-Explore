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

import com.fasterxml.jackson.core.ObjectCodec;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.flowable.ui.task.model.runtime.CreateProcessInstanceRepresentation;
import org.flowable.ui.task.model.runtime.ProcessInstanceRepresentation;
import org.flowable.ui.task.service.runtime.FlowableProcessInstanceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app")
public class ProcessInstancesResource {

    @Autowired
    protected FlowableProcessInstanceService processInstanceService;

    @PostMapping(value = "/rest/process-instances")
    public ProcessInstanceRepresentation startNewProcessInstance(@RequestBody CreateProcessInstanceRepresentation startRequest) {
        return processInstanceService.startNewProcessInstance(startRequest);
    }
    @PostMapping(value = "/rest/process-instances(id)")
    public ProcessInstanceRepresentation startNewProcessInstance(@RequestBody String processDefinitionId) {
        return processInstanceService.startNewProcessInstanceById(processDefinitionId);
    }
}