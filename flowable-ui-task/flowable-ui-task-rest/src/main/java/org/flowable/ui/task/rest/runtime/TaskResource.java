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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.flowable.bpmn.model.BpmnModel;
import org.flowable.editor.language.json.converter.BpmnJsonConverter;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.history.HistoricProcessInstance;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.ui.common.service.exception.InternalServerErrorException;
import org.flowable.ui.modeler.repository.ModelRepository;
import org.flowable.ui.modeler.serviceapi.ModelService;
import org.flowable.ui.task.model.runtime.TaskRepresentation;
import org.flowable.ui.task.model.runtime.TaskUpdateRepresentation;
import org.flowable.ui.task.service.runtime.FlowableTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.UUID;

import com.alibaba.fastjson.JSON;
import com.arronlong.httpclientutil.HttpClientUtil;
import com.arronlong.httpclientutil.common.HttpConfig;
import com.arronlong.httpclientutil.exception.HttpProcessException;
@RestController
@RequestMapping("/app")
public class TaskResource {


    @Autowired
    protected FlowableTaskService taskService;




    @Autowired
    protected HistoryService historyService;

    @Autowired
    protected RepositoryService repositoryService;

    @Autowired
    protected ObjectMapper objectMapper;

    @GetMapping(value = "/rest/tasks/{taskId}", produces = "application/json")
    public TaskRepresentation getTask(@PathVariable String taskId   ) {
        return taskService.getTask(taskId);
    }

    @PutMapping(value = "/rest/tasks/{taskId}", produces = "application/json")
    public TaskRepresentation updateTask(@PathVariable("taskId") String taskId, @RequestBody TaskUpdateRepresentation updated) {
        return taskService.updateTask(taskId, updated);
    }

    @GetMapping(value = "/rest/tasks/{taskId}/subtasks", produces = "application/json")
    public List<TaskRepresentation> getSubTasks(@PathVariable String taskId) {
        return taskService.getSubTasks(taskId);
    }


}
