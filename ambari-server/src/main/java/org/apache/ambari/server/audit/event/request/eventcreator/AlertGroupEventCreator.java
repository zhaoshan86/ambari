/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.ambari.server.audit.event.request.eventcreator;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.ambari.server.api.services.Request;
import org.apache.ambari.server.api.services.Result;
import org.apache.ambari.server.api.services.ResultStatus;
import org.apache.ambari.server.audit.event.AuditEvent;
import org.apache.ambari.server.audit.event.request.RequestAuditEventCreator;
import org.apache.ambari.server.audit.event.request.event.AddAlertGroupRequestAuditEvent;
import org.apache.ambari.server.audit.event.request.event.ChangeAlertGroupRequestAuditEvent;
import org.apache.ambari.server.audit.event.request.event.DeleteAlertGroupRequestAuditEvent;
import org.apache.ambari.server.controller.spi.Resource;
import org.apache.ambari.server.controller.utilities.PropertyHelper;
import org.joda.time.DateTime;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

/**
 * This creator handles alert group requests
 * For resource type {@link Resource.Type#AlertGroup}
 * and request types {@link Request.Type#POST}, {@link Request.Type#PUT} and {@link Request.Type#DELETE}
 */
public class AlertGroupEventCreator implements RequestAuditEventCreator {

  /**
   * Set of {@link Request.Type}s that are handled by this plugin
   */
  private Set<Request.Type> requestTypes = new HashSet<Request.Type>();

  {
    requestTypes.add(Request.Type.PUT);
    requestTypes.add(Request.Type.POST);
    requestTypes.add(Request.Type.DELETE);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Request.Type> getRequestTypes() {
    return requestTypes;
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<Resource.Type> getResourceTypes() {
    return Collections.singleton(Resource.Type.AlertGroup);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Set<ResultStatus.STATUS> getResultStatuses() {
    return null;
  }

  @Override
  public AuditEvent createAuditEvent(Request request, Result result) {
    String username = ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getUsername();

    switch (request.getRequestType()) {
      case POST:
        return AddAlertGroupRequestAuditEvent.builder()
          .withTimestamp(DateTime.now())
          .withRequestType(request.getRequestType())
          .withResultStatus(result.getStatus())
          .withUrl(request.getURI())
          .withRemoteIp(request.getRemoteAddress())
          .withUserName(username)
          .withName(getName(request))
          .withDefinitionIds(getDefinitionIds(request))
          .withNotificationIds(getNotificationIds(request))
          .build();
      case PUT:
        return ChangeAlertGroupRequestAuditEvent.builder()
          .withTimestamp(DateTime.now())
          .withRequestType(request.getRequestType())
          .withResultStatus(result.getStatus())
          .withUrl(request.getURI())
          .withRemoteIp(request.getRemoteAddress())
          .withUserName(username)
          .withName(getName(request))
          .withDefinitionIds(getDefinitionIds(request))
          .withNotificationIds(getNotificationIds(request))
          .build();
      case DELETE:
        return DeleteAlertGroupRequestAuditEvent.builder()
          .withTimestamp(DateTime.now())
          .withRequestType(request.getRequestType())
          .withResultStatus(result.getStatus())
          .withUrl(request.getURI())
          .withRemoteIp(request.getRemoteAddress())
          .withUserName(username)
          .withId(request.getResource().getKeyValueMap().get(Resource.Type.AlertGroup))
          .build();
      default:
        return null;
    }
  }

  private String getName(Request request) {
    if (!request.getBody().getNamedPropertySets().isEmpty()) {
      return String.valueOf(request.getBody().getNamedPropertySets().iterator().next().getProperties().get(PropertyHelper.getPropertyId("AlertGroup", "name")));
    }
    return null;
  }

  private List<String> getDefinitionIds(Request request) {
    if (!request.getBody().getNamedPropertySets().isEmpty()) {
      List<String> list = (List<String>) request.getBody().getNamedPropertySets().iterator().next().getProperties().get(PropertyHelper.getPropertyId("AlertGroup", "definitions"));
      if (list != null) {
        return list;
      }
    }
    return Collections.emptyList();
  }

  private List<String> getNotificationIds(Request request) {
    if (!request.getBody().getNamedPropertySets().isEmpty()) {
      List<String> list = (List<String>) request.getBody().getNamedPropertySets().iterator().next().getProperties().get(PropertyHelper.getPropertyId("AlertGroup", "targets"));
      if (list != null) {
        return list;
      }
    }
    return Collections.emptyList();
  }
}